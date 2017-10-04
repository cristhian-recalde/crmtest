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
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.StorageSupportHelper;


/**
 * This processor is responsible for performing all validation that applies
 * to ANY subscription move scenario.  It is also responsible for performing any
 * setup that is common to ANY subscription move scenario.
 * 
 * It does not implement any subscription move business logic, modify the request,
 * or modify the subscriptions involved.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BaseSubscriptionMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{
    public BaseSubscriptionMoveProcessor(SMR request)
    {
        super(new DependencyMoveProcessor<SMR>(request));
    }
    
    public BaseSubscriptionMoveProcessor(MoveProcessor<SMR> delegate)
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

        if (!moveCtx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            /*
             * We want to primarily work at the level of the XDB home because we want to bypass all of
             * the usual provisioning services attached to the higher-level Subscriber home decorators.
             */
            Home xdbHome = StorageSupportHelper.get(ctx).createHome(ctx, Subscriber.class, "SUBSCRIBER");
            try
            {
                final Context serverCtx = moveCtx.createSubContext("RMI Server Context");

                // this gives us access to the RMIHomeServers and the context that is passed by BAS to the homes.
                moveCtx.put("RMI Server Context",serverCtx);

                moveCtx.put(
                        MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY, 
                        new SubscriberHomeFactory(xdbHome).createMovePipeline(moveCtx, serverCtx));
            }
            catch (Exception e)
            {
                throw new MoveException(request, "Error creating custom move pipeline for subscription storage operations.", e);
            }
        }

        Subscriber originalSubscription = request.getOriginalSubscription(moveCtx);

        moveCtx.put(Common.MOVE_SUBSCRIBER, originalSubscription);
        
        if (request.getSubscriptionType(ctx) == null)
        {
            throw new MoveException(request, "Error retrieving subscription type for subscription " + request.getOldSubscriptionId());
        }
        
        return moveCtx;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(SubscriberXDBHome.class))
        {
            cise.thrown(new IllegalStateException(
                    "XDB subscriber home not installed in context."));
        }
        
        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
                "Custom subscriber home not installed in context."));
        }
        
        SMR request = getRequest();

        validateSubscriptionType(ctx, cise, request);
        
        validateNewSubscriptionID(ctx, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }

    private void validateSubscriptionType(Context ctx, CompoundIllegalStateException cise, SMR request)
    {
        SubscriptionType subscriptionType = request.getSubscriptionType(ctx);
        if (subscriptionType != null
                && !subscriptionType.isMoveSupported(request))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID, 
                    "Operation not supported for subscription of type " + subscriptionType.getTypeEnum()));
        }
    }

    /**
     * Verify that the new subscription ID is set if required in the service provider config.
     * 
     * @param ctx Move context
     * @param cise Container for validation errors
     */
    private void validateNewSubscriptionID(Context ctx, CompoundIllegalStateException cise)
    {
        SMR request = this.getRequest();
        
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (oldSubscription != null)
        {
            try
            {
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, oldSubscription.getSpid());
                if (spid.getAllowToSpecifySubscriberId())
                {
                    final String newIdentifier = request.getNewSubscriptionId();
                    if (newIdentifier == null || newIdentifier.length() == 0)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                SubscriptionMoveRequestXInfo.NEW_SUBSCRIPTION_ID,
                                "Service Provider Level configuration requires to specify ID for the new subscriber record."));
                    }
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error occurred retrieving SPID " + oldSubscription.getSpid() + ": " + e.getMessage(), e).log(ctx);
            }
        }
    }
}
