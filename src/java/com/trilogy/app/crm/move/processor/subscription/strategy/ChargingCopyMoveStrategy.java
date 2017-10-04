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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.subscriber.charge.AbstractSubscriberCharger;
import com.trilogy.app.crm.subscriber.charge.MoveSubscriberCharger;
import com.trilogy.app.crm.support.MoveServicePeriodSupport;
import com.trilogy.app.crm.support.ServicePeriodSupport;
import com.trilogy.app.crm.support.SupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Responsible for all service, bundle, and auxiliary service refund/charging
 * related to move scenarios.
 * 
 * NOTE: Due to limitation of BundleCopyMoveStrategy (documented within), it
 * also handles bundle charging as of 8.2 (possibly newer).  When that strategy
 * is decoupled from charging, this strategy should be tested to ensure that it
 * handles bundle refund/charging properly. 
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ChargingCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{

    public ChargingCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        refundItems(ctx, request, oldSubscription, getInactiveCopy(oldSubscription));   
        
        super.createNewEntity(ctx, request);

        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        chargeItems(ctx, request, oldSubscription, newSubscription);   

    }
    
    private Subscriber getInactiveCopy(Subscriber oldSubscription)
    {
        /*
         * We clone the old subscriber and set the clone to inactive because the prorate
         * refund only occurs for active -> inactive, this way we can make it seem like a
         * normal deactivate
         */
        Subscriber oldInactiveSub = oldSubscription;
        try
        {
            oldInactiveSub = (Subscriber) oldSubscription.deepClone();
            oldInactiveSub.setState(SubscriberStateEnum.INACTIVE);
            oldInactiveSub.setContext(oldSubscription.getContext());
        }
        catch (CloneNotSupportedException e)
        {
        }
        return oldInactiveSub;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, SMR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }

    private void refundItems(Context context, SMR request, Subscriber oldSubscription, Subscriber newSubscription) throws MoveException
    {
        Context ctx = context.createSubContext();
        
        SupportHelper.register(ctx, ServicePeriodSupport.class, MoveServicePeriodSupport.instance());
        
        LogMsg pmSuccess = new PMLogMsg(this.getClass().getName(), "refundItems()");
        LogMsg pmFail = new PMLogMsg(this.getClass().getName(), "refundItems()-Exception");
        
        try
        {
            MoveSubscriberCharger charger = new MoveSubscriberCharger(ctx, newSubscription, oldSubscription);
            ctx.put(Subscriber.class, oldSubscription);
            ctx.put(AbstractSubscriberCharger.class, charger);
            charger.refund(ctx, null);

            pmSuccess.log(ctx);
        }
        catch (Exception e)
        {
            pmFail.log(ctx);
            throw new MoveException(request, "Error occurred adjusting item refunds for subscription "
                    + oldSubscription.getId(), e);
        }
    }

    private void chargeItems(Context context, SMR request, Subscriber oldSubscription, Subscriber newSubscription) throws MoveException
    {
        Context ctx = context.createSubContext();
        ctx.put(MovableExtension.MOVED_SUBSCRIBER_CTX_KEY, oldSubscription);
        LogMsg pmSuccess = new PMLogMsg(this.getClass().getName(), "refundItems()");
        LogMsg pmFail = new PMLogMsg(this.getClass().getName(), "refundItems()-Exception");
        
        try
        {
            MoveSubscriberCharger charger = new MoveSubscriberCharger(ctx, newSubscription, oldSubscription);
            ctx.put(AbstractSubscriberCharger.class, charger);
            ctx.put(Subscriber.class, newSubscription);
            charger.charge(ctx, null);

            pmSuccess.log(ctx);
        }
        catch (Exception e)
        {
            pmFail.log(ctx);
            throw new MoveException(request, "Error occurred adjusting item charges for subscription "
                    + newSubscription.getId(), e);
        }
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
}
