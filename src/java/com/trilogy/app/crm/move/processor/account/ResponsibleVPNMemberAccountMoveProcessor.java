/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.account;

import java.util.Collection;
import java.util.Collections;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.home.sub.SubscriberHomeFactory;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.app.crm.move.support.VPNSubscriptionMoveSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.StorageSupportHelper;


/**
 * This processor handles VPN account changes for all non-responsible subscriptions under it.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class ResponsibleVPNMemberAccountMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    public ResponsibleVPNMemberAccountMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        final Context moveCtx = super.setUp(ctx);
        
        AMR request = this.getRequest();
        
        vpnMoveSupport_ = new VPNSubscriptionMoveSupport<AMR>(moveCtx, request, true);

        if (vpnMoveSupport_.isVPNAccountChange())
        {
            // Retrieve the subscriptions that will need to be processed for this VPN related move.
            Account oldAccount = request.getOldAccount(moveCtx);
            if (oldAccount != null)
            {
                try
                {
                    // Get all subscriptions under this account in the hierarchy (including ones under nested responsible accounts).
                    // All subscriptions need their VPN auxiliary services adjusted.
                    subscriptions_ = AccountSupport.getTopologyEx(ctx, oldAccount, null, null, false, true, null, true);
                }
                catch (HomeException e)
                {
                    throw new MoveException(request, "Exception occurred retrieving all subscriptions under responsible account " + oldAccount.getBAN());
                }
            }
        }
        
        if (subscriptions_ == null)
        {
            subscriptions_ = Collections.emptyList();
        }

        if (!moveCtx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            /*
             * We want to primarily work at the level of the XDB home because we want to bypass all of
             * the usual provisioning services attached to the higher-level Subscriber home decorators.
             */
            //Home xdbHome = (Home) ctx.get(SubscriberXDBHome.class);
            Home xdbHome =  StorageSupportHelper.get(ctx).createHome(ctx, Subscriber.class, "SUBSCRIBER");
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
        
        return moveCtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        AMR request = this.getRequest();

        if (!ctx.has(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY))
        {
            cise.thrown(new IllegalStateException(
            "Custom subscriber home not installed in context."));
        }
        
        vpnMoveSupport_.validate(ctx, request, cise);

        Account oldAccount = request.getOldAccount(ctx);
        if (oldAccount != null)
        {
            validateOldAccount(ctx, request, cise);
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        AMR request = this.getRequest();

        /*
         * Create sub context to override the SubscriberHome.class key with the shortened
         * subscriber home used for moving. We do this because the create method in the
         * SubscriberAuxiliaryServiceHome will perform a store on the subscriber home and
         * we need to avoid all the unwanted side effects of using the normal
         * SubscriberHome
         */
        final Context subCtx = ctx.createSubContext().put(
                SubscriberHome.class, 
                ctx.get(MoveConstants.CUSTOM_SUBSCRIPTION_HOME_CTX_KEY));
        
        for (Subscriber sub : subscriptions_)
        {
            if (sub != null)
            {
                vpnMoveSupport_.removeVPNFromSubscription(subCtx, request, sub);
            }
        }

        // Pass the parent context to the delegate move, not the special sub-context
        super.move(ctx);

        for (Subscriber sub : subscriptions_)
        {
            if (sub != null)
            {
                final SubscriptionType subscriptionType = sub.getSubscriptionType(subCtx);
                if (subscriptionType == null)
                {
                    request.reportWarning(
                            subCtx, 
                            new MoveWarningException(request, "Subscription type " + sub.getSubscriptionType()
                                    + " not found for subscription " + sub.getId()
                                    + ".  Skipping VPN migration to subscription."));
                }
                else if (subscriptionType.isVoice())
                {
                    vpnMoveSupport_.addVPNToSubscription(subCtx, request, sub);
                }
            }
        }
    }

    private void validateOldAccount(Context ctx, AMR request, CompoundIllegalStateException cise)
    {
        final Account oldAccount = request.getOriginalAccount(ctx);
        if (oldAccount != null && oldAccount.isMom(ctx))
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.EXISTING_BAN,
                    "Account (BAN=" + request.getExistingBAN() + ") can't be moved because it is a VPN root account."));
        }
        else
        {
            final Account oldRootAccount = vpnMoveSupport_.getOldRootAccount();
            if (oldRootAccount != null
                    && oldRootAccount.isMom(ctx)
                    && oldRootAccount.getVpnMSISDN() != null
                    && oldRootAccount.getVpnMSISDN().trim().length() > 0)
            {
                for (Subscriber sub : subscriptions_)
                {
                    if (sub != null)
                    {
                        // moving from MOM account
                        // dont move the main msisdn subscriber until it is chnaged to be become the a
                        // normal vpn subscriber
                        if (sub.getMSISDN().equals(oldRootAccount.getVpnMSISDN()))
                        {
                            cise.thrown(new IllegalPropertyArgumentException(
                                    AccountMoveRequestXInfo.EXISTING_BAN,
                                    "Account (BAN=" + request.getExistingBAN() + ") can't be moved because it contains "
                                    + " the VPN subscription (Subscription ID=" + sub.getId() + ", MSISDN=" + sub.getMSISDN()
                                    + ") for the root VPN account (BAN=" + oldRootAccount.getBAN() + ").  "
                                    + "Change the VPN MSISDN of the VPN root account before moving this account."));
                            break;
                        }
                    }
                }
            }
        }
    }
    
    protected VPNSubscriptionMoveSupport<AMR> vpnMoveSupport_ = null;
    protected Collection<Subscriber> subscriptions_ = null;
}
