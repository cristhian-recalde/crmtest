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

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;


/**
 * A transfer strategy that handles transferring an amount owing value from
 * the old subscription to the new subscription.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AmountOwingTransferCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends AbstractTransferCopyMoveStrategy<SMR>
{
    public AmountOwingTransferCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        if (!ctx.has(MoveConstants.AMOUNT_OWING_CTX_KEY))
        {
            cise.thrown(new IllegalStateException("Amount owing for subscription " + request.getOldSubscriptionId() + " not available."));
        }
        
        cise.throwAll();
        
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, SMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        long amountOwing = ctx.getLong(MoveConstants.AMOUNT_OWING_CTX_KEY);

        new DebugLogMsg(this, "Crediting owing amount of " + amountOwing
                + " to subscription " + oldSubscription.getId() + "...", null).log(ctx);
        createAdjustmentTransaction(ctx, request, -amountOwing, oldSubscription);
        new InfoLogMsg(this, "Successfully credited owing amount of " + amountOwing
                + " to subscription " + oldSubscription.getId(), null).log(ctx);

        try
        {
            super.createNewEntity(ctx, request);   
        }
        catch(MoveException e)
        {
            new DebugLogMsg(this, "Error moving subscription.  Debiting owing amount of " + amountOwing
                    + " back from subscription " + oldSubscription.getId() + "...", null).log(ctx);
            createAdjustmentTransaction(ctx, request, amountOwing, oldSubscription);
            new InfoLogMsg(this, "Successfully debited owing amount of " + amountOwing
                    + " back from subscription " + oldSubscription.getId() + " after error moving subscription.", null).log(ctx);
            throw e;
        }

        Subscriber newSubscription = request.getNewSubscription(ctx);
        new DebugLogMsg(this, "Debiting owing amount of " + amountOwing
                + " from subscription " + newSubscription.getId() + "...", null).log(ctx);
        createAdjustmentTransaction(ctx, request, amountOwing, newSubscription);
        new InfoLogMsg(this, "Successfully debited owing amount of " + amountOwing
                + " from subscription " + newSubscription.getId(), null).log(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }
}
