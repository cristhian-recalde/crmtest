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
package com.trilogy.app.crm.home.validator;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.BillCycleHistoryXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.dunning.visitor.DunningBeforeEndOfBillCyclePredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleHistoryAccountStateValidator implements Validator
{

    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        Account account = null;
        
        BillCycleHistory hist = null;
        if (obj instanceof BillCycleHistory)
        {
            HomeOperationEnum op = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
            hist = (BillCycleHistory) obj;
            if (HomeOperationEnum.CREATE.equals(op)
                    && BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
            {
                try
                {
                    account = AccountSupport.getAccount(ctx, hist.getBAN());
                }
                catch (HomeException e)
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "Unable to retrieve account " + hist.getBAN()));
                }
            }

            if (hist.getNewBillCycleID() >= BillCycleSupport.AUTO_BILL_CYCLE_START_ID)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.NEW_BILL_CYCLE_ID, 
                        "Bill cycle not allowed to change to reserved prepaid auto-bill cycle " + hist.getNewBillCycleID()));
            }
            
            if (hist.getOldBillCycleID() == hist.getNewBillCycleID())
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.NEW_BILL_CYCLE_ID, 
                        "New bill cycle ID must differ from existing bill cycle ID"));
            }
        }
        
        if (account != null
                && hist != null)
        {
            BillCycle oldBillCycle = null;
            BillCycle newBillCycle = null;
            
            try
            {
                oldBillCycle = BillCycleSupport.getBillCycle(ctx, hist.getOldBillCycleID());
                newBillCycle = BillCycleSupport.getBillCycle(ctx, hist.getNewBillCycleID());
            }
            catch (HomeException e)
            {
                // NOP
            }

            if (account.getBillCycleID() != hist.getOldBillCycleID())
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.BAN, 
                        "Account " + account.getBAN() + "'s bill cycle ID does not match old bill cycle ID in change request."));
            }
            
            if (!account.isRootAccount())
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.BAN, 
                        "Account " + account.getBAN() + " is not a root account.  It is not eligible for bill cycle change."));
            }
            else if (newBillCycle == null)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.NEW_BILL_CYCLE_ID, 
                        "Bill cycle " + hist.getNewBillCycleID() + " not found."));
            }
            else if (oldBillCycle == null)
            {
                cise.thrown(new IllegalPropertyArgumentException(
                        BillCycleHistoryXInfo.OLD_BILL_CYCLE_ID, 
                        "Bill cycle " + hist.getOldBillCycleID() + " not found."));
            }
            else if (oldBillCycle.getDayOfMonth() != newBillCycle.getDayOfMonth())
            {
                PaymentPlanSupport paymentPlanSupport = PaymentPlanSupportHelper.get(ctx);
                if (paymentPlanSupport.isEnabled(ctx) 
                        && paymentPlanSupport.isValidPaymentPlan(ctx, account.getPaymentPlan()))
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "Account " + account.getBAN() + " is enrolled in a payment plan.  It is not eligible for bill cycle change."));
                }
                else  if (!AccountStateEnum.ACTIVE.equals(account.getState()))
                {
                    cise.thrown(new IllegalPropertyArgumentException(
                            BillCycleHistoryXInfo.BAN, 
                            "Account " + account.getBAN() + " is not active.  It is not eligible for bill cycle change."));
                }
                checkDescendentStates(ctx, account, cise);

            }
        }
        
        cise.throwAll();
    }


    public void checkDescendentStates(Context ctx, Account rootAccount, CompoundIllegalStateException cise)
    {
        List accountsAndSubs = new ArrayList<Account>();
        try
        {
            accountsAndSubs = AccountSupport.getTopologyEx(ctx, rootAccount, null, null, true, true, null, true);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving descendent accounts/subscriptions of " + rootAccount.getBAN(), e).log(ctx);
            cise.thrown(new IllegalPropertyArgumentException(
                    BillCycleHistoryXInfo.BAN, 
                    "Unable to validate sub-accounts/subscriptions for Account " + rootAccount.getBAN() + "."));
            return;
        }
        
        if (accountsAndSubs != null)
        {            
            for (Object obj : accountsAndSubs)
            {
                if (obj instanceof Account)
                {
                    Account account = (Account) obj;
                    if (account != null 
                            && !AccountStateEnum.ACTIVE.equals(account.getState())
                            && !AccountStateEnum.INACTIVE.equals(account.getState()))
                    {
                        new InfoLogMsg(this, "Account " + account.getBAN() + " is in an invalid state for bill cycle date change (" + account.getState() + ").", null).log(ctx);
                        cise.thrown(new IllegalPropertyArgumentException(
                                BillCycleHistoryXInfo.BAN, 
                                "Sub-Account " + account.getBAN() + " is in an invalid state (" + account.getState().getDescription() + ") for bill cycle date change."));
                    }
                }
                else if (obj instanceof Subscriber)
                {
                    Subscriber sub = (Subscriber) obj;
                    if (sub != null 
                            && !SubscriberStateEnum.ACTIVE.equals(sub.getState())
                            && !SubscriberStateEnum.INACTIVE.equals(sub.getState()))
                    {
                        new InfoLogMsg(this, "Subscription " + sub.getId() + " is in an invalid state for bill cycle date change (" + sub.getState() + ").", null).log(ctx);
                        cise.thrown(new IllegalPropertyArgumentException(
                                BillCycleHistoryXInfo.BAN, 
                                "Subscription " + sub.getId() + " is in an invalid state (" + sub.getState().getDescription() + ") for bill cycle date change."));
                    }
                }
            }
            
            // Do more expensive state checking in a second pass
            // The idea is to allow fast failure for the invalid state situation
            for (Object obj : accountsAndSubs)
            {
                if (obj instanceof Account)
                {
                    Account account = (Account) obj;
                    if (account != null 
                            && account.isResponsible()
                            && !account.isPrepaid())
                    {
                        if (new DunningBeforeEndOfBillCyclePredicate().f(ctx, account))
                        {
                            new InfoLogMsg(this, "Account " + account + " is slated to be dunned prior to end of bill cycle.  Bill cycle date change not allowed.", null).log(ctx);
                            cise.thrown(new IllegalPropertyArgumentException(
                                    BillCycleHistoryXInfo.BAN, 
                                    "Sub-Account " + account.getBAN() + " is forecasted to be dunned prior to the end of bill cycle, and therefore not eligible for bill cycle change until due amount is cleared."));
                        }
                    }
                }
            }
        }
    }
}
