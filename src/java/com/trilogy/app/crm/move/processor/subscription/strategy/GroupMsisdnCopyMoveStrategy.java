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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;


/**
 * This copy strategy updates the Group MSISDN for a subscription in CRM
 * and any other dependent applications (e.g. BMGT, ECP, SMSB).
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class GroupMsisdnCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public GroupMsisdnCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        if (newSubscription != null)
        {
            // Clear the current value so that it can re-evaluated
            newSubscription.clearGroupMSISDN();
        }
        
        super.initialize(ctx, request);
        
        bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException(
                    "No Balance Management provisioning client installed.  Unable to update external profile."));
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

        updateGroupIDInBMGT(ctx, request);
        
        updateGroupMSISDNInServices(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }

    private void updateGroupIDInBMGT(Context ctx, SMR request) throws MoveException
    {
        Account newAccount = request.getNewAccount(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);

        SubscriptionTypeEnum subscriptionType = null;
        SubscriptionType type = request.getSubscriptionType(ctx);
        if (type != null)
        {
            subscriptionType = type.getTypeEnum();
        }
        
        String newGroupID = newAccount.getPoolID(ctx, newSubscription.getSubscriptionType());
        try
        {
            new DebugLogMsg(this, 
                    "Setting pooled group ID to '" + newGroupID + "' for moving " + subscriptionType + " subscription "
                    + "(MSISDN=" + newSubscription.getMSISDN() + "/ID=" + newSubscription.getId() + ") in Balance Management application", null).log(ctx);
            bmClient_.updatePooledGroupID(ctx, newSubscription, newGroupID, false);
            new InfoLogMsg(this, 
                    "Setting pooled group ID to '" + newGroupID + "' successfully in Balance Management application "
                    + "for moving " + subscriptionType + " subscription "
                    + "(MSISDN=" + newSubscription.getMSISDN() + "/ID=" + newSubscription.getId() + ")", null).log(ctx);
        }
        catch (final Exception e)
        {
            throw new MoveException(request, "Failed to update pooled group owner to [" + newGroupID
                    + "] for subscription " + newSubscription.getId() + " in Balance Management application.", e);
        }
    }

    private void updateGroupMSISDNInServices(Context ctx, SMR request)
    {
        SubscriptionType subscriptionType = request.getSubscriptionType(ctx);
        
        Subscriber newSubscription = request.getNewSubscription(ctx);
        String newMsisdn = newSubscription.getMSISDN();
        
        if (subscriptionType != null
                && subscriptionType.isService())
        {
            boolean hasEcpService = true;
            try
            {
                hasEcpService = AppEcpClientSupport.hasServiceProvisioned(ctx, newSubscription);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error determining whether or not subscription " + newSubscription.getId() + " has ECP service provisioned.  Assuming it does so that we try to change the Group MSISDN.", e).log(ctx);
            }
            if (hasEcpService)
            {
                new DebugLogMsg(this, "Changing group MSISDN for moving subscriber " + newMsisdn
                        + " (ID=" + newSubscription.getId() + ") in ECP", null).log(ctx);
                AppEcpClientSupport.updateGroupMsisdn(ctx, newSubscription);
                new InfoLogMsg(this, "Changed group MSISDN successfully in ECP for moving subscriber " + newMsisdn
                        + " (ID=" + newSubscription.getId() + ")", null).log(ctx);
            }

            boolean hasSmsService = true;
            try
            {
                hasSmsService = AppSmsbClientSupport.hasServiceProvisioned(ctx, newSubscription);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error determining whether or not subscription " + newSubscription.getId() + " has SMSB service provisioned.  Assuming it does so that we try to change the Group MSISDN.", e).log(ctx);
            }
            if (hasSmsService)
            {
                new DebugLogMsg(this, "Changing group MSISDN for moving subscriber " + newMsisdn
                        + " (ID=" + newSubscription.getId() + ") in ECP", null).log(ctx);
                AppSmsbClientSupport.updateGroupMsisdn(ctx, newSubscription);
                new InfoLogMsg(this, "Changed group MSISDN successfully in SMSB for moving subscriber " + newMsisdn
                        + " (ID=" + newSubscription.getId() + ")", null).log(ctx);
            }
        }
    }

    private SubscriberProfileProvisionClient bmClient_ = null;
}
