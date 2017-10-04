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
package com.trilogy.app.crm.home.account;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;


/**
 * This home cancels pending bill cycle updates when account/subscriber
 * updates occur that break bill cycle update pre-conditions.
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class PendingBillCycleChangeCancellingHome extends HomeProxy
{

    public PendingBillCycleChangeCancellingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        try
        {
            if (obj instanceof Account)
            {
                Account account = (Account) obj;

                BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEventInHierarchy(ctx, account);
                if (lastEvent != null 
                        && BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus())
                        && lastEvent.getOldBillCycleDay() != lastEvent.getNewBillCycleDay())
                {
                    boolean mustCancelBCChange = false;
                    
                    if (account.getBillCycleID() != lastEvent.getOldBillCycleID())
                    {
                        // Account bill cycle ID is changing.  Cancel the old change request
                        mustCancelBCChange |= account.getBillCycleID() != lastEvent.getNewBillCycleID();
                    }

                    if (!AccountStateEnum.ACTIVE.equals(account.getState()))
                    {
                        // Root account must be active
                        // Sub-Accounts must be active or inactive
                        mustCancelBCChange |= account.isRootAccount();
                        mustCancelBCChange |= !AccountStateEnum.INACTIVE.equals(account.getState());
                    }

                    // No accounts can be in a payment plan
                    PaymentPlanSupport paymentPlanSupport = PaymentPlanSupportHelper.get(ctx);
                    mustCancelBCChange |= (paymentPlanSupport.isEnabled(ctx) && paymentPlanSupport.isValidPaymentPlan(ctx, account.getPaymentPlan()));

                    if (mustCancelBCChange)
                    {
                        lastEvent.setStatus(BillCycleChangeStatusEnum.CANCELLED);
                        lastEvent.setFailureMessage("Account " + account.getBAN() + " was modified and triggered cancellation of pending bill cycle change for account " + lastEvent.getBAN());
                        lastEvent = HomeSupportHelper.get(ctx).storeBean(ctx, lastEvent);
                        new InfoLogMsg(this, "Cancelled pending bill cycle change [" + lastEvent.ID()
                                + "] following change to account " + account.getBAN()
                                + " causing violation of bill cycle change pre-condition(s).", null).log(ctx);
                    }
                }
            }
            else if (obj instanceof Subscriber)
            {
                Subscriber sub = (Subscriber) obj;

                BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEventInHierarchy(ctx, sub.getBAN());
                if (lastEvent != null 
                        && BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus())
                        && lastEvent.getOldBillCycleDay() != lastEvent.getNewBillCycleDay()
                        && !SubscriberStateEnum.ACTIVE.equals(sub.getState()))
                {
                    lastEvent.setStatus(BillCycleChangeStatusEnum.CANCELLED);
                    lastEvent.setFailureMessage("Subscription " + sub.getId() + " was modified and triggered cancellation of pending bill cycle change for account " + lastEvent.getBAN());
                    lastEvent = HomeSupportHelper.get(ctx).storeBean(ctx, lastEvent);
                    new InfoLogMsg(this, "Cancelled pending bill cycle change [" + lastEvent.ID() + "] following subscription " + sub.getId() + " state change to " + sub.getState()
                            + ".  Subscriptions must be active in order for pending requests to exist.", null).log(ctx);
                }
            }
        }
        catch (HomeException e)
        {
            String msg = "Error occurred cancelling bill cycle change following violation of pre-conditions caused by " + obj.getClass().getSimpleName() + " update!";
            new MinorLogMsg(this, msg, e).log(ctx);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new HomeException(msg, e));
        }
        
        return super.store(ctx, obj);
    }
    
}
