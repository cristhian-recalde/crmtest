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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;


/**
 * Responsible for moving suspended entities from the old subscription to the new one.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class SuspendedEntityCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public SuspendedEntityCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, SMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
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

        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        // Move suspended entities
        if (oldSubscription.getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            /*
             * suspend services on the new subscriber which were suspended on the old
             * subscriber must happen before charges so suspended services will not be
             * charged
             */
            new DebugLogMsg(this, "Moving suspended entities from subscription " + oldSubscription.getId() + " to " + newSubscription.getId() + "...", null).log(ctx);
            SuspendedEntitySupport.moveSubscriber(ctx, oldSubscription, newSubscription);
            new InfoLogMsg(this, "Moved suspended entities successfully from subscription " + oldSubscription.getId() + " to " + newSubscription.getId() + ".", null).log(ctx);
        }
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
