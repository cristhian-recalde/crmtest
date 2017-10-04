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

import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * A general strategy for crediting or debiting some amount to/from a subscription.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public abstract class AbstractTransferCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public AbstractTransferCopyMoveStrategy(
            CopyMoveStrategy<SMR> delegate,
            AdjustmentTypeEnum creditAdjustmentType,
            AdjustmentTypeEnum debitAdjustmentType)
    {
        super(delegate);
        creditAdjustmentTypeEnum_ = creditAdjustmentType;
        debitAdjustmentTypeEnum_ = debitAdjustmentType;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, SMR request)
    {
        super.initialize(ctx, request);
        
        try
        {
            transferCreditAdj_ = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, creditAdjustmentTypeEnum_);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving " + creditAdjustmentTypeEnum_ + " adjustment type ' (" + creditAdjustmentTypeEnum_.getIndex() + ").", e).log(ctx);
        }
        
        try
        {
            transferDebitAdj_ = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, debitAdjustmentTypeEnum_);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving " + debitAdjustmentTypeEnum_ + " adjustment type ' (" + debitAdjustmentTypeEnum_.getIndex() + ").", e).log(ctx);
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        final Integer spidKey = Integer.valueOf(request.getOldSubscription(ctx).getSpid());
        
        if (transferCreditAdj_ == null)
        {
            cise.thrown(new IllegalStateException("No adjustment type found with type '"
                    + adjustmentEnumString(creditAdjustmentTypeEnum_)));
        }
        else
        {
            Object info = (Object) transferCreditAdj_.getAdjustmentSpidInfo().get(spidKey);
            if (info == null)
            {
                cise.thrown(new IllegalStateException("No service provider info for adjustment type '"
                        + adjustmentEnumString(creditAdjustmentTypeEnum_)
                        + " for SPID " + spidKey));
            }
        }
        
        if (transferDebitAdj_ == null)
        {
            cise.thrown(new IllegalStateException("No adjustment type found with type '"
                    + adjustmentEnumString(debitAdjustmentTypeEnum_)));
        }
        else
        {
            Object info = (Object) transferDebitAdj_.getAdjustmentSpidInfo().get(spidKey);
            if (info == null)
            {
                cise.thrown(new IllegalStateException("No service provider info for adjustment type '"
                        + adjustmentEnumString(debitAdjustmentTypeEnum_)
                        + " for SPID " + spidKey));
            }
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    protected final String adjustmentEnumString(AdjustmentTypeEnum adjustmentEnum)
    {
        return adjustmentEnum + "' (" + (adjustmentEnum != null ? adjustmentEnum.getIndex() : "") + ")";
    }

    protected void createAdjustmentTransaction(Context ctx, SMR request, long amount, Subscriber subscription) throws MoveException
    {
        AdjustmentType transferAdjType = getTransferAdjustmentType(ctx, amount);
        final Date adjustmentDate = getAdjustmentDate(ctx, request, subscription);
        try
        {
            // Make sure the right subscriber is in the context for the transaction home
            Context sCtx = ctx.createSubContext();
            sCtx.put(Subscriber.class, subscription);
            
            new DebugLogMsg(this, "Creating adjustment transaction "
                    + "(amount=" + amount + "/adjustment type=" + transferAdjType.ID() + ") "
                    + "for subscription " + subscription.getId() + "...", null).log(sCtx);
            TransactionSupport.createTransaction(
                    sCtx, 
                    subscription, 
                    amount, 
                    transferAdjType, 
                    true,
                    adjustmentDate);
            new InfoLogMsg(this, "Successfully created adjustment transaction "
                    + "(amount=" + amount + "/adjustment type=" + transferAdjType.ID() + ") "
                    + "for subscription " + subscription.getId(), null).log(sCtx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Error occurred creating adjustment transaction "
                    + "(amount=" + amount + "/adjustment type=" + transferAdjType.ID() + ") "
                    + "for subscription " + subscription.getId(), e);
        }
    }

    private AdjustmentType getTransferAdjustmentType(Context ctx, long amount)
    {
        final AdjustmentType adjustmentType;
        if (amount < 0)
        {
            adjustmentType = this.transferCreditAdj_;
            new DebugLogMsg(this, "Transfer amount < 0, so returning '" + creditAdjustmentTypeEnum_ + "' adjustment type.", null).log(ctx);
        }
        else
        {
            adjustmentType = this.transferDebitAdj_;
            new DebugLogMsg(this, "Transfer amount >= 0, so returning '" + debitAdjustmentTypeEnum_ + "' adjustment type.", null).log(ctx);
        }
        return adjustmentType;
    }

    private Date getAdjustmentDate(Context ctx, SMR request, Subscriber subscription)
    {
        final Date adjustmentDate;
        if (SafetyUtil.safeEquals(request.getOldSubscriptionId(), subscription.getId()))
        {
            // Adjustments against the old subscription must have transaction time = move start time
            // because by now the MSISDN is associated with the new subscription.
            adjustmentDate = (Date) ctx.get(MoveConstants.MOVE_START_TIME_CTX_KEY, new Date());
        }
        else
        {
            adjustmentDate = new Date();
        }
        return adjustmentDate;
    }
    
    protected AdjustmentType transferDebitAdj_ = null;
    private AdjustmentTypeEnum debitAdjustmentTypeEnum_;
    protected AdjustmentType transferCreditAdj_ = null;
    private AdjustmentTypeEnum creditAdjustmentTypeEnum_;
}
