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
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequestXInfo;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Responsible for extending the expiry date on the new subscription
 * at the same time as the move request is executed.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ExpiryExtensionCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR>
{
    public ExpiryExtensionCopyMoveStrategy(CopyMoveStrategy<SMR> delegate)
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

        Subscriber oldSubscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (oldSubscription != null
                && oldSubscription.isPostpaid())
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    ServiceBasedSubscriptionMoveRequestXInfo.OLD_SUBSCRIPTION_ID,
                    "Cannot apply expiry extension to postpaid subscription (ID=" + request.getOldSubscriptionId() + "."));
        }
        else if (request instanceof ServiceBasedSubscriptionMoveRequest)
        {
            ServiceBasedSubscriptionMoveRequest serviceBasedRequest = (ServiceBasedSubscriptionMoveRequest) request;

            if (serviceBasedRequest.getExpiryExtension() < 0)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        ServiceBasedSubscriptionMoveRequestXInfo.EXPIRY_EXTENSION,
                        "Cannot apply expiry extension < 0 to subscription (ID=" + request.getNewSubscriptionId() + "."));
            }
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

        Subscriber newSubscription = request.getNewSubscription(ctx);

        final int expiryExtension;
        if (request instanceof ServiceBasedSubscriptionMoveRequest)
        {
            expiryExtension = ((ServiceBasedSubscriptionMoveRequest)request).getExpiryExtension();
        }
        else
        {
            expiryExtension = 0;
        }

        if (expiryExtension > 0)
        {
            Date expiryDate = newSubscription.getExpiryDate();
            new DebugLogMsg(this, "Extending current expiry date (" + expiryDate + ") for subscription " + newSubscription.getId() + " by " + expiryExtension + " days...", null).log(ctx);
            newSubscription.setExpiryDate(CalendarSupportHelper.get(ctx).findDateDaysAfter(expiryExtension, expiryDate, newSubscription.getTimeZone(ctx)));
            try
            {
                SubscriberSupport.updateExpiryOnCrmAbmBM(ctx, newSubscription);
                new InfoLogMsg(this, "Expiry successfully updated to " + newSubscription.getExpiryDate() + " for subscription " + newSubscription.getId(), null).log(ctx);
            }
            catch (HomeException e)
            {
                newSubscription.setExpiryDate(expiryDate);
                throw new MoveException(request, "Failed to extend expiry of subscription "
                        + newSubscription.getId(), e);
            }
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
