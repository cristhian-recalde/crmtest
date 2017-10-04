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
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * Responsible for setting the initial balance of the new subscription during conversion.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class InitialAmountSubscriptionCopyMoveStrategy<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends CopyMoveStrategyProxy<CSBTR>
{
    public InitialAmountSubscriptionCopyMoveStrategy(CopyMoveStrategy<CSBTR> delegate)
    {
        super(delegate);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Context ctx, CSBTR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, CSBTR request) throws MoveException
    {
        super.createNewEntity(ctx, request);

        Subscriber newSubscription = request.getNewSubscription(ctx);

        long initialAmount = request.getInitialAmount();
        if (initialAmount >= 0)
        {
            AdjustmentTypeEnum initialBalanceType = AdjustmentTypeEnum.InitialBalance;
            AdjustmentType initialBalanceAdj = null;
            try
            {
                initialBalanceAdj = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, initialBalanceType);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving " + initialBalanceType + " adjustment type ' ("
                        + initialBalanceType.getIndex() + ").", e).log(ctx);
            }
            try
            {
                final Context subCtx = ctx.createSubContext();
                subCtx.put(SubscriberCreditLimitUpdateAgent.ENABLE_PROCESSING, false);
                TransactionSupport.createTransaction(subCtx, newSubscription, initialAmount, initialBalanceAdj);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, " Created initial amount transaction for " + initialAmount, null).log(ctx);
                }
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(this, "Unable to create the deposit transaction of amount " + initialAmount, homeEx)
                        .log(ctx);
            }
        }
    }
}
