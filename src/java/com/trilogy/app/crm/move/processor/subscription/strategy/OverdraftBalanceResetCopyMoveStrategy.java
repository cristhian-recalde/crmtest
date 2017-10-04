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

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.config.CRMConfigInfoForVRA;
import com.trilogy.app.crm.support.VRASupport;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtension;
import com.trilogy.app.crm.extension.spid.OverdraftBalanceSpidExtensionXInfo;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.s2100.ErrorCode;


/**
 * This copy strategy resets the Overdraft balance after creating new subscription, and charges for any pending charges.
 * 
 * @author Marcio Marques
 * @since 9.1.2
 */
public class OverdraftBalanceResetCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public OverdraftBalanceResetCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException("No Balance Management provisioning client installed."));
        }
        
        if (client_ == null)
        {
            cise.thrown(new IllegalStateException("No OCG client installed."));
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
        
        client_  = (AppOcgClient) ctx.get(AppOcgClient.class);
        bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        super.createNewEntity(ctx, request);
        
        Subscriber newSubscription = request.getNewSubscription(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Updating overdraft balance of subscription " + oldSubscription.getId() + "...", null).log(ctx);
        }
        updateOverdraftBalance(ctx, request, oldSubscription, newSubscription);
        new InfoLogMsg(this, "Successfully updated overdraft balance of subscription " + oldSubscription.getId(), null).log(ctx);
    }

    private void updateOverdraftBalance(Context ctx, SMR request, Subscriber oldSubscription, Subscriber newSubscription)
    {
        if (oldSubscription.isPrepaid() || newSubscription.isPostpaid())
        {
            removeOverdraftBalance(ctx, request, oldSubscription, newSubscription);
        }
        else if (oldSubscription.isPostpaid() || newSubscription.isPrepaid())
        {
            addOverdraftBalance(ctx, request, oldSubscription, newSubscription);
        }
    }
    
    private void removeOverdraftBalance(Context ctx, SMR request, Subscriber oldSubscription, Subscriber newSubscription)
    {
        LongHolder overdraftBalance = new LongHolder();
        final String erReference = "AppCrm-" + oldSubscription.getMSISDN();

        SubscriptionTypeEnum subscriptionType = null;
        SubscriptionType type = request.getSubscriptionType(ctx);
        if (type != null)
        {
            subscriptionType = type.getTypeEnum();
        }

        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, 
                    "Resetting overdraft balance of " + subscriptionType + " subscription profile "
                    + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ")...", null).log(ctx);
            }
            final short result =
                (short) client_.resetOverdraftBalance(
                        oldSubscription.getMSISDN(),
                        oldSubscription.getSubscriberType(),
                    (int) oldSubscription.getReactivationFee(),
                    oldSubscription.getCurrency(ctx),
                    true, 
                    erReference,
                    type.getId(),
                    overdraftBalance); 

            if (result != ErrorCode.NO_ERROR)
            {
                LogSupport.minor(ctx, this, "Failed to remove overdraft balance of subscription profile "
                        + "(ID=" + oldSubscription.getId() + "/MSISDN=" + oldSubscription.getMSISDN() + ").");
            }
            else
            {
                new InfoLogMsg(this, 
                        "Overdraft balance of " + subscriptionType + " subscription profile "
                        + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ") "
                        + "reset successfully.", null).log(ctx);
        
                if (overdraftBalance.value>0)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, 
                            "Creating clawback transaction for overdraft balance of " + subscriptionType + " subscription profile "
                            + "(MSISDN=" + newSubscription.getMSISDN() + "/ID=" + newSubscription.getId() + ")...", null).log(ctx);
                    }
                    final CRMConfigInfoForVRA config = VRASupport.getCRMConfigInfoForVRA(ctx,newSubscription.getSpid());
                    int adjustmentType = config.getClearedOverdraftAmountAdjustmentType();
        
                    AdjustmentType clawbackAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,  adjustmentType);
                    Transaction trans = TransactionSupport.createTransaction(ctx,
                            newSubscription,
                            overdraftBalance.value,
                            clawbackAdjustmentType);
                    new InfoLogMsg(this, 
                            "Overdraft balance clawback transaction of " + subscriptionType + " subscription profile "
                            + "(MSISDN=" + newSubscription.getMSISDN() + "/ID=" + newSubscription.getId() + ") "
                            + "successfully created.", null).log(ctx);
                }
            }
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Failed to remove overdraft balance of subscription profile "
                    + "(ID=" + oldSubscription.getId() + "/MSISDN=" + oldSubscription.getMSISDN() + ").", e).log(ctx);
        }
    }
    
    private void addOverdraftBalance(Context ctx, SMR request, Subscriber oldSubscription, Subscriber newSubscription)
    {
        long limit = 0;
        int spid = newSubscription.getSpid();
        SubscriptionTypeEnum subscriptionType = null;
        SubscriptionType type = request.getSubscriptionType(ctx);
        if (type != null)
        {
            subscriptionType = type.getTypeEnum();
        }

        try
        {
            if (spid!=-1)
            {
                try
                {
                    OverdraftBalanceSpidExtension extension = HomeSupportHelper.get(ctx).findBean(ctx,
                            OverdraftBalanceSpidExtension.class,
                            new EQ(OverdraftBalanceSpidExtensionXInfo.SPID, Integer.valueOf(spid)));
                    if (extension!=null)
                    {
                        limit = extension.getLimit();
                    }
                }
                catch (HomeException e)
                {
                    LogSupport.minor(ctx, this, "Unable to retrieve Overdraft Balance Limit SPID extension for SPID "
                            + newSubscription.getSpid() + ": " + e.getMessage(), e);
                }
            }
            
            if (limit>0)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, 
                        "Updating overdraft balance of " + subscriptionType + " subscription profile "
                        + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ")...", null).log(ctx);
                }
                bmClient_.updateOverdraftBalanceLimit(ctx, newSubscription, limit);
                new InfoLogMsg(this, 
                        "Overdraft balance of " + subscriptionType + " subscription profile "
                        + "(MSISDN=" + oldSubscription.getMSISDN() + "/ID=" + oldSubscription.getId() + ") "
                        + "reset successfully.", null).log(ctx);
            }

        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Failed to update overdraft balance of subscription profile "
                    + "(ID=" + oldSubscription.getId() + "/MSISDN=" + oldSubscription.getMSISDN() + ").", e).log(ctx);
        }
    }    

    protected AppOcgClient client_ = null;
    protected SubscriberProfileProvisionClient bmClient_ = null;
}
