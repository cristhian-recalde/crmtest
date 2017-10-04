/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRConstants;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.factory.CRMMoveProcessorFactory;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.MoveProcessorSupport;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * This move strategy is responsible for creating the new subscription and updating
 * the old subscription via Home operations.  It does not modify the old or new
 * subscriptions.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionHomeCopyMoveStrategy<SMR extends SubscriptionMoveRequest> implements CopyMoveStrategy<SMR>
{
    /**
     * @{inheritDoc}
     */
    public void initialize(Context ctx, SMR request)
    {
    }
    
    /**
     * @{inheritDoc}
     */
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom subscriber home not installed in context."));
        }

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();
    }


    /**
     * @{inheritDoc
     */
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);
        try
        {
            Home subscriberHome = (Home) subCtx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY);
            new DebugLogMsg(this, "Creating new subscription (ID=" + newSubscription.getId() + ") in subscriber home.",
                    null).log(subCtx);
            newSubscription.setOldSubscription(oldSubscription.getId());
            newSubscription.setCreateSubscriptionReason("MOVE");
            newSubscription = (Subscriber) subscriberHome.create(subCtx, newSubscription);
            if (newSubscription != null)
            {
                new InfoLogMsg(this, "New subscription (ID=" + newSubscription.getId()
                        + ") created in subscriber home successfully.", null).log(subCtx);
                request.setNewSubscriptionId(newSubscription);
            }
            request.setNewSubscriptionId(newSubscription);
            
            Home crmSpidHome = (Home) ctx.get(CRMSpidHome.class);
            if (crmSpidHome == null)
            {
                throw new HomeException("System Error: CRMSpidHome does not exist in context");
            }
            CRMSpid crmSpidBean = null;
            try 
            {
                crmSpidBean = (CRMSpid) crmSpidHome.find(ctx, newSubscription.getSpid());
            } 
            catch(Exception e) 
            {
            	throw new HomeException("Configuration Error: Service Provider is mandatory, make sure it exists before continuing");
            }
            
            //--TT#13080558010 -- In Account move scenario BSS should not send any command to HLR.
            boolean isSubscriptionMoveRequest = ctx.getBoolean(CRMMoveProcessorFactory.SUBSCRIPTION_MOVE_REQUEST);
            if(!isSubscriptionMoveRequest && crmSpidBean.isSendHLRCommandOnCoversion())
            {
            	removeOldSubscriberFromHLR(ctx, oldSubscription);
                addNewSubscriberToHLR(ctx, newSubscription);
            }
        }
        catch (HomeException he)
        {
            throw new MoveException(request, "Error occurred while creating subscription with ID "
                    + newSubscription.getId() + " (required to move subscription " + request.getOldSubscriptionId()
                    + ")", he);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
    }
    
    private void updateHLRForSubscriber(
            final Context ctx,
            final Subscriber sub,
            final ProvisionCommand cmdId)
        throws ProvisionAgentException
    {
        final String message = "Account/Subscription Move :: attempt to send an HLR Command with Command-ID[" + String.valueOf(cmdId)
                + "] for Subscriber[ id (" + sub.getId() + ") , msisdn (" + sub.getMSISDN() + ")] ";
        new InfoLogMsg(this, message, null).log(ctx);
        HlrSupport.updateHlr(ctx, sub, cmdId);
    }
    
    private void removeOldSubscriberFromHLR(Context ctx, Subscriber oldSub)
    {
    	HTMLExceptionListener listener = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
    	
    	LogSupport.info(ctx, this, "Removing Old Subscription from HLR : " + oldSub.getId());
    	
    	try
    	{
	        if (SystemSupport.needsHlr(ctx))
	        {
	            String hlrCommand = HLRConstants.PRV_CMD_TYPE_INACTIVE;
	            final ProvisionCommand provisionCommand = HlrSupport.findCommand(ctx, oldSub, hlrCommand);
	            updateHLRForSubscriber(ctx, oldSub, provisionCommand);
	        }
    	}
    	catch(Exception e)
    	{
    		if(listener != null)
    		{
    			listener.thrown(e);
    		}
    	}
    }

    private void addNewSubscriberToHLR(Context ctx, Subscriber newSub)
    {
    	HTMLExceptionListener listener = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
    	
    	LogSupport.info(ctx, this, "Adding new Subscription to HLR : " + newSub.getId());
    	
    	try
    	{
	        if (SystemSupport.needsHlr(ctx))
	        {
	            String hlrCommand = HLRConstants.PRV_CMD_TYPE_CREATE;
	            final ProvisionCommand provisionCommand = HlrSupport.findCommand(ctx, newSub, hlrCommand);
	            updateHLRForSubscriber(ctx, newSub, provisionCommand);
	        }
    	}
    	catch(Exception e)
    	{
    		if(listener != null)
    		{
    			listener.thrown(e);
    		}
    	}    	
    }

    /**
     * @{inheritDoc
     */
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);
        try
        {
            Home subscriberHome = (Home) subCtx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY);
            new DebugLogMsg(this, "Updating old subscription (ID=" + request.getOldSubscriptionId()
                    + ") in subscriber home.", null).log(subCtx);
            oldSubscription = (Subscriber) subscriberHome.store(subCtx, oldSubscription);
            if (oldSubscription != null)
            {
                new InfoLogMsg(this, "Old subscription (ID=" + request.getOldSubscriptionId()
                        + ") updated in subscriber home successfully.", null).log(subCtx);
            }
            request.setOldSubscriptionId(oldSubscription);
        }
        catch (HomeException he)
        {
            throw new MoveException(request, "Error occurred while updating subscription with ID "
                    + request.getOldSubscriptionId(), he);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
    }
}
