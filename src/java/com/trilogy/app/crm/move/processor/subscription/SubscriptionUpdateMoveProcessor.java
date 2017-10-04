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
package com.trilogy.app.crm.move.processor.subscription;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.MoveProcessorSupport;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;


/**
 * This processor is responsible for setting the new subscription ID field = existing
 * subscription ID during the setup.  During the move it is responsible for updating 
 * the subscription via Home operations.  It does not modify the subscription.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SubscriptionUpdateMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{
    public SubscriptionUpdateMoveProcessor(SMR request)
    {
        this(new BaseSubscriptionMoveProcessor<SMR>(request));
    }
    
    
    public SubscriptionUpdateMoveProcessor(MoveProcessor<SMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);

        SMR request = this.getRequest();
        
        request.setNewSubscriptionId(request.getOldSubscription(moveCtx));
        
        return moveCtx;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        SMR request = this.getRequest();

        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom subscriber home not installed in context."));
        }

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        if (!SafetyUtil.safeEquals(request.getOldSubscriptionId(), request.getNewSubscriptionId()))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_BAN, 
                    "Old & new subscription identifiers do not match.  They must match when moving this type of subscription."));
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        SMR request = getRequest();
        Context subCtx = ctx.createSubContext();
        subCtx.put(HTMLExceptionListener.class, null);

        try
        {
    
            Subscriber subscription = request.getNewSubscription(ctx);
            try
            {
                Home subscriberHome = (Home) subCtx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY);
    
                new DebugLogMsg(this, "Updating subscription (ID=" + subscription.getId() + ") in subscriber home.", null).log(subCtx);
                Subscriber newSubscription = (Subscriber) subscriberHome.store(subCtx, subscription);
                if (newSubscription != null)
                {
                    new InfoLogMsg(this, "Subscription (ID=" + newSubscription.getId() + ") updated in subscriber home successfully.", null).log(subCtx);
                    request.setNewSubscriptionId(newSubscription);
                }
            }
            catch(HomeException he)
            {
                throw new MoveException(request, "Error occurred while updating subscription with ID " + request.getOldSubscriptionId(), he);
            }
            
            super.move(subCtx);
        }
        finally
        {
            MoveProcessorSupport.copyHTMLExceptionListenerExceptions(subCtx, ctx);
        }
            
    }
}
