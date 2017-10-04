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
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.move.support.VPNSubscriptionMoveSupport;


/**
 * Responsible for moving VPN related entities from the old subscription to the new one.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class VPNCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public VPNCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
        
        vpnMoveSupport_ = new VPNSubscriptionMoveSupport<SMR>(ctx, request, false);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
            "Custom subscriber home not installed in context."));
        }
        
        vpnMoveSupport_.validate(ctx, request, cise);

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null)
        {
            validateOldAccount(ctx, request, cise);
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        super.createNewEntity(ctx, request);

        if (vpnMoveSupport_.isVPNAccountChange())
        {
            /*
             * Create sub context to override the SubscriberHome.class key with the
             * shortened subscriber home used for moving. We do this because the create
             * method in the SubscriberAuxiliaryServiceHome will perform a store on the
             * subscriber home and we need to avoid all the unwanted side effects of using
             * the normal SubscriberHome
             */
            final Context subCtx = ctx.createSubContext().put(
                    SubscriberHome.class, 
                    ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY));
            
            vpnMoveSupport_.addVPNToSubscription(subCtx, request, request.getNewSubscription(ctx));
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        if (vpnMoveSupport_.isVPNAccountChange())
        {
            /*
             * Create sub context to override the SubscriberHome.class key with the
             * shortened subscriber home used for moving. We do this because the create
             * method in the SubscriberAuxiliaryServiceHome will perform a store on the
             * subscriber home and we need to avoid all the unwanted side effects of using
             * the normal SubscriberHome
             */
            final Context subCtx = ctx.createSubContext().put(
                    SubscriberHome.class, 
                    ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY));
            
            vpnMoveSupport_.removeVPNFromSubscription(subCtx, request, request.getOldSubscription(ctx));
        }
    
        super.removeOldEntity(ctx, request);
    }

    private void validateOldAccount(Context ctx, SMR request, CompoundIllegalStateException cise)
    {
        Subscriber oldSubscription = request.getOriginalSubscription(ctx);
        if (oldSubscription != null
                && vpnMoveSupport_.isVPNAccountChange())
        {
            Account oldRootAccount = vpnMoveSupport_.getOldRootAccount();

            // moving from MOM account
            // dont move the main msisdn subscriber until it is chnaged to be become the a
            // normal vpn subscriber
            if (oldSubscription.getMSISDN().equals(oldRootAccount.getVpnMSISDN()))
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        SubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                        "Subscription (ID=" + request.getOldSubscriptionId() + ") can't be moved because it is "
                        + " the VPN subscription for the root VPN account (BAN=" + oldRootAccount.getBAN() + ").  "
                        + "Change the VPN MSISDN of the account before moving this subscription."));
            }
        }
    }

    private VPNSubscriptionMoveSupport<SMR> vpnMoveSupport_ = null;
}
