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
package com.trilogy.app.crm.move.processor.subscription.strategy;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;


/**
 * This copy strategy resets the ABM balance before proceeding with new subscription creation.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class BalanceResetCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public BalanceResetCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);

        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException("No Balance Management provisioning client installed."));
        }
        
        cise.throwAll();
        
        super.validate(ctx, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
        
        bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        new DebugLogMsg(this, "Resetting balance of subscription " + oldSubscription.getId() + "...", null).log(ctx);
        resetBalance(ctx, request);
        new InfoLogMsg(this, "Successfully reset balance of subscription " + oldSubscription.getId(), null).log(ctx);
        
        super.createNewEntity(ctx, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }
    

    private void resetBalance(Context ctx, SMR request)
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        try
        {
            SubscriptionTypeEnum subscriptionType = null;
            SubscriptionType type = request.getSubscriptionType(ctx);
            if (type != null)
            {
                subscriptionType = type.getTypeEnum();
            }
            
            new DebugLogMsg(this, 
                    "Resetting balance of " + subscriptionType + " subscription profile "
                    + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ")...", null).log(ctx);
            bmClient_.updateBalance(ctx, oldSubscription, 0);
            new InfoLogMsg(this, 
                    "Balance of " + subscriptionType + " subscription profile "
                    + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ") "
                    + "reset successfully.", null).log(ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Failed to reset balance of subscription profile "
                    + "(ID=" + oldSubscription.getId() + "/MSISDN=" + oldSubscription.getMSISDN() + ").", e).log(ctx);
        }
    }

    protected SubscriberProfileProvisionClient bmClient_ = null;
}
