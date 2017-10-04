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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This CopyMoveStrategy is responsible for Reverse Activation 
 *
 * @author Mangaraj Sahoo
 * @since 9.2
 */
public class ReverseActivationCopyMoveStrategy<RAMR extends ReverseActivationMoveRequest> extends CopyMoveStrategyProxy<RAMR>
{
    public ReverseActivationCopyMoveStrategy(CopyMoveStrategy<RAMR> delegate)
    {
        super(delegate);
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, RAMR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        Subscriber subscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        
        Account oldAccount = request.getOldAccount(ctx);
        if (oldAccount == null)
        {
            cise.thrown(new IllegalStateException("Account for subscription " + subscription.getId() + " does not exist."));
        }
        
        cise.throwAll();

        super.validate(ctx, request);
    }


    /**
     * @{inheritDoc}
     * See com.redknee.app.crm.factory.SubscriberFactory.initSubscriber(Context, Subscriber)
     */
    @Override
    public void createNewEntity(Context ctx, RAMR request) throws MoveException
    {
        Subscriber newSubscription = request.getNewSubscription(ctx);
        newSubscription.setBAN(request.getNewBAN());
        newSubscription.setState(SubscriberStateEnum.AVAILABLE);
        
        newSubscription.setStartDate(null);
        newSubscription.setEndDate(null);

        final Date now = new Date();
        final Date endDate = SubscriberSupport.getFutureEndDate(now);
        
        newSubscription.setDateCreated(now);
        newSubscription.setLastModified(now);
        
        newSubscription.setSecondaryPricePlanStartDate(endDate);
        newSubscription.setSecondaryPricePlanEndDate(endDate);
        newSubscription.setExpiryDate(new Date(0)); //GTac TT# 12020222016
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "New Subscription -> " + newSubscription);
        }
        
        super.createNewEntity(ctx, request); 
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, RAMR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        oldSubscription.setState(SubscriberStateEnum.INACTIVE);
        oldSubscription.setLastModified(new Date());
        
        super.removeOldEntity(ctx, request);
        
        Account oldAccount = request.getOldAccount(ctx);
        oldAccount.setState(AccountStateEnum.INACTIVE);
        oldAccount.setLastModified(new Date());
        
        try
        {
            LogSupport.info(ctx, this, "Updating old account (BAN=" + oldAccount.getBAN() + ") in account home.");
            oldAccount = HomeSupportHelper.get(ctx).storeBean(ctx, oldAccount);
            LogSupport.info(ctx, this, "Account (BAN=" + oldAccount.getBAN() + ") updated successfully.");
        }
        catch (HomeException he)
        {
            throw new MoveException(request, "Failed to update account (BAN= " + oldAccount.getBAN() + ")", he);
        }
    }
}
