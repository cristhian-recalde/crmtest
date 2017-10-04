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
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;


/**
 * A transfer strategy that handles transferring the ABM balance from
 * the old subscription to the new subscription.  This type of operation
 * is useful for cases where we are moving a subscription from a non-pooled
 * account to a pooled account.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class BalanceTransferCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends AbstractTransferCopyMoveStrategy<SMR>
{
    public BalanceTransferCopyMoveStrategy(CopyMoveStrategy<SMR> delegate,
            AdjustmentTypeEnum creditAdjustmentType,
            AdjustmentTypeEnum debitAdjustmentType)
    {
        super(delegate, 
                creditAdjustmentType, 
                debitAdjustmentType);
    }
    
    public BalanceTransferCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate, 
                AdjustmentTypeEnum.SubscriberTransferCredit, 
                AdjustmentTypeEnum.SubscriberTransferDebit);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
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

        SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        Account newAccount = SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        if (newAccount != null)
        {
            Subscriber oldSubscription = request.getOriginalSubscription(ctx);
            if (oldSubscription != null && !oldSubscription.isPooled(ctx))
            {
                long subscriptionType = oldSubscription.getSubscriptionType();
                String newGroupMsisdn = newAccount.getGroupMSISDN(ctx, subscriptionType);
                if (newGroupMsisdn != null && newGroupMsisdn.trim().length() > 0)
                {
                    String newGroupBAN = newAccount.getPoolID(ctx, subscriptionType);
                    request.reportWarning(ctx, new MoveWarningException(request, 
                            "Prepaid balance of non-pooled " + request.getSubscriptionType(ctx)
                            + " subscription (ID=" + oldSubscription.getId()
                            + ") will be transferred to pool balance (Pool BAN=" + newGroupBAN
                            + "/MSISDN=" + newGroupMsisdn));   
                }   
            }
        }

        if (bmClient_ == null)
        {
            cise.thrown(new IllegalStateException("No Balance Management provisioning client installed."));
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
        long oldBalance = 0;
        if ( !(request instanceof ConvertSubscriptionBillingTypeRequest))
        {
            Subscriber oldSubscription = request.getOldSubscription(ctx);
            oldBalance = getBalance(ctx, request, oldSubscription);
            new DebugLogMsg(this, "Debiting balance of " + oldBalance + " from subscription " + oldSubscription.getId()
                    + "...", null).log(ctx);
            createAdjustmentTransaction(ctx, request, oldBalance, oldSubscription);
            new InfoLogMsg(this, "Successfully debited balance of " + oldBalance + " from subscription "
                    + oldSubscription.getId(), null).log(ctx);
        }
        super.createNewEntity(ctx, request);
        if (! (request instanceof ConvertSubscriptionBillingTypeRequest))
        {
            Subscriber newSubscription = request.getNewSubscription(ctx);
            new DebugLogMsg(this, "Crediting balance of " + oldBalance + " to subscription " + newSubscription.getId()
                    + "...", null).log(ctx);
            createAdjustmentTransaction(ctx, request, -oldBalance, newSubscription);
            new InfoLogMsg(this, "Successfully credited balance of " + oldBalance + " to subscription "
                    + newSubscription.getId(), null).log(ctx);
        }
    }

    protected long getBalance(Context ctx, SMR request, Subscriber newSubscription) throws MoveException
    {
        String msisdn = newSubscription.getMSISDN();
        
        Parameters subProfile = null;
        try
        {
            SubscriptionTypeEnum subscriptionType = null;
            SubscriptionType type = request.getSubscriptionType(ctx);
            if (type != null)
            {
                subscriptionType = type.getTypeEnum();
            }
            
            new DebugLogMsg(this, 
                    "Looking up " + subscriptionType + " subscription profile for "
                    + "MSISDN " + msisdn + " (ID=" + newSubscription.getId() + ") "
                    + "from Balance Management application...", null).log(ctx);
            subProfile = bmClient_.querySubscriptionProfile(ctx, newSubscription);
            new DebugLogMsg(this, 
                    "Retrieved " + subscriptionType + " subscription profile for "
                    + "MSISDN " + msisdn + " (ID=" + newSubscription.getId() + ") "
                    + "from Balance Management application.", null).log(ctx);
        }
        catch (Exception e)
        {
            throw new MoveException(request, "Error retrieving subscription profile (ID=" + newSubscription.getId()
                    + "/MSISDN=" + msisdn+ ") from Balance Management application.");
        }
        
        if (subProfile == null)
        {
            throw new MoveException(request, "Subscription profile (ID=" + newSubscription.getId()
                    + "/MSISDN=" + msisdn+ ") does not exist in Balance Management application.");
        }
        
        long balance = subProfile.getBalance();
        
        new DebugLogMsg(this, "Subscription profile (ID=" + newSubscription.getId()
                    + "/MSISDN=" + msisdn+ ") has balance of " + balance, null).log(ctx);
        return balance;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {        
        super.removeOldEntity(ctx, request);
    }

    protected SubscriberProfileProvisionClient bmClient_ = null;
}
