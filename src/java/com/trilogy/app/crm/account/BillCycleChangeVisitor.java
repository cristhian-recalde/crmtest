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
package com.trilogy.app.crm.account;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bas.recharge.ApplyBillCycleChangeRecurRecharge;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycleChangeStatusEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.subscriber.charge.BillCycleChangeRefundingCharger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleHistorySupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
class BillCycleChangeVisitor implements Visitor
{   
    protected final BillCycleChangeLifecycleAgent agent_;
    
    BillCycleChangeVisitor(BillCycleChangeLifecycleAgent agent)
    {
        agent_ = agent;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(Context parentCtx, Object obj) throws AgentException, AbortVisitException
    {
        Context ctx = parentCtx.createSubContext();
        
        if (!LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining bill cycle changes will be processed next time it is run.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }
        
        if (obj instanceof BillCycleHistory)
        {
            BillCycleHistory hist = (BillCycleHistory) obj;
            if (!isLatestPendingEvent(ctx, hist))
            {
                return;
            }
            
            PMLogMsg pm = new PMLogMsg(BillCycleChangeVisitor.class.getName(), "visit()");
            try
            {
                Account account = null;
                try
                {
                    account = AccountSupport.getAccount(ctx, hist.getBAN());
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error retrieving account " + hist.getBAN() + " while processing bill cycle change from " + hist.getOldBillCycleID() + " to " + hist.getNewBillCycleID(), e).log(ctx);
                }

                if (account == null)
                {
                    addFailureToHistory(ctx, hist, "Unable to find account " + hist.getBAN());
                    return;
                }
                com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, account.getSpid());
                // We'll need to make everything look like the bill cycle change is taking place on the old bill cycle day
                // i.e. transactions, history records, etc.
                Date startOfNextBillingPeriod = getStartOfNextBillingPeriod(ctx, account.getSpid(), hist.getOldBillCycleDay());
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Processing bill cycle change request [" + hist.ID() + "] with effective date " + startOfNextBillingPeriod, null).log(ctx);
                }

                ctx.put(CommonTime.RUNNING_DATE, startOfNextBillingPeriod);
                ctx.put(Common.DURING_BILL_CYCLE_CHANGE, true);

                boolean billCycleDayChanging = isBillCycleDayChanging(ctx, account, hist.getNewBillCycleDay());
                if (billCycleDayChanging)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Attempting to refund any excess charges for prior to bill cycle change [" + hist + "]", null).log(ctx);
                    }
                    executeProratedRefundsForOldBillCycle(ctx, account);
                }

                account.setBillCycleID(hist.getNewBillCycleID());

                try
                {
                    account = HomeSupportHelper.get(ctx).storeBean(ctx, account);
                    new InfoLogMsg(this, "Account " + account.getBAN() + " bill cycle changed to " + account.getBillCycleID() + " for change request [" + hist.ID() + "]", null).log(ctx);
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error changing bill cycle for account " + hist.getBAN() + " from " + hist.getOldBillCycleID() + " to " + hist.getNewBillCycleID(), e).log(ctx);
                    addFailureToHistory(ctx, hist, "Error updating bill cycle for account " + hist.getBAN());
                    return;
                }

                if (billCycleDayChanging)
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "Attempting to create prorated charges for account " + hist.getBAN() 
                                + " from old bill cycle date (" + startOfNextBillingPeriod
                                + ") to new bill cycle date (" + hist.getNewBillCycleDay() + ")", null).log(ctx);
                    }
                    executeRecurringChargesForNewBillCycle(ctx, account, hist);
                }
            }
            finally
            {
                pm.log(ctx);
            }
        }
    }

    protected boolean isBillCycleDayChanging(Context ctx, Account account, int newBillCycleDay)
    {
        int accountBillCycleDay = -1;
        
        try
        {
            accountBillCycleDay = account.getBillCycleDay(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error retrieving old bill cycle day for account " + account.getBAN() + " [BillCycleID=" + account.getBillCycleID() + "]", e).log(ctx);
            
        }
        return accountBillCycleDay != newBillCycleDay;
    }

    protected Date getStartOfNextBillingPeriod(Context ctx, int spid, int oldBillCycleDay)
    {
        CalendarSupport calendarSupport = CalendarSupportHelper.get(ctx);
        
        Date runningDate = calendarSupport.getRunningDate(ctx);
        
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);

        Calendar recurringChargeDate = calendarSupport.dateToCalendar(runningDate);
        recurringChargeDate.add(Calendar.MONTH, 1);
        
        return handler.calculateCycleStartDate(ctx, calendarSupport.calendarToDate(recurringChargeDate), oldBillCycleDay, spid);
    }

    protected void executeProratedRefundsForOldBillCycle(Context ctx, Account account)
    {
        PMLogMsg pm = new PMLogMsg(BillCycleChangeVisitor.class.getName(), "executeProratedRefundsForOldBillCycle()");
        try
        {
            And filter = new And();
            filter.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.ACTIVE));
            
            Collection<Subscriber> subs = AccountSupport.getTopologyEx(ctx, account, null, null, false, false, filter, true);
            for (Subscriber sub : subs)
            {
                try
                {
                    // Prorate refund each subscriber's monthly services
                    // Monthly: Net effect should be no refunds because we're running charges as of start of old bill cycle
                    // Multi-Monthly: Some refund may or may not take place depending on which month we're in
                    new BillCycleChangeRefundingCharger(ctx, sub).refund(ctx, null);
                    new InfoLogMsg(this, "Refund process for any excess charges executed successfully for subscription " + sub.getId() + " prior to bill cycle change for account " + account.getBAN(), null).log(ctx);
                }
                catch (Exception e)
                {
                    new MinorLogMsg(this, "Error occurred executing prorated refund for subscription " + sub.getId(), e).log(ctx);
                }
            }
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error occurred executing prorated refunds for subscriptions within account " + account.getBAN(), e).log(ctx);
        }
        finally
        {
            pm.log(ctx);
        }
    }

    /**
     *  Run recurring charges to cover charges between end of old bill cycle and start of new bill cycle
     * 
     * @param ctx Operating context
     * @param account Account 
     * @param hist
     */
    protected void executeRecurringChargesForNewBillCycle(Context ctx, Account account, BillCycleHistory hist)
    {
        PMLogMsg pm = new PMLogMsg(BillCycleChangeVisitor.class.getName(), "executeRecurringChargesForNewBillCycle()");
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        ApplyBillCycleChangeRecurRecharge recurringChargeExecutor = new ApplyBillCycleChangeRecurRecharge(account, 
                hist, 
                CoreCrmConstants.SYSTEM_AGENT, 
                runningDate);
        try
        {
            recurringChargeExecutor.execute(ctx);
            new InfoLogMsg(this, "Prorated charge task executed successfully for account " + hist.getBAN() 
                    + " from " + runningDate
                    + " to new bill cycle date (Day of Month=" + hist.getNewBillCycleDay() + ")", null).log(ctx);
        }
        catch (AgentException e)
        {
            String msg = e.getMessage();
            if (msg == null)
            {
                msg = "Error performing recurring charges for bill cycle change for account " + hist.getBAN();
            }
            else if (msg.length() > BillCycleHistory.FAILUREMESSAGE_WIDTH)
            {
                msg = msg.substring(0, BillCycleHistory.FAILUREMESSAGE_WIDTH);
            }
            addFailureToHistory(ctx, hist, msg);
        }
        finally
        {
            pm.log(ctx);
        }
    }

    protected boolean isLatestPendingEvent(Context ctx, BillCycleHistory hist)
    {
        if (!BillCycleChangeStatusEnum.PENDING.equals(hist.getStatus()))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Skipping non-pending bill cycle history record [" + hist.ID() + "]", null).log(ctx);
            }
            return false;
        }

        BillCycleHistory lastEvent = BillCycleHistorySupport.getLastEvent(ctx, hist.getBAN());
        if (lastEvent != null 
                && !BillCycleChangeStatusEnum.PENDING.equals(lastEvent.getStatus()))
        {
            // Update the pending record since it isn't really pending any more
            hist.setStatus(BillCycleChangeStatusEnum.PROCESSED);
            try
            {
                HomeSupportHelper.get(ctx).storeBean(ctx, hist);
            }
            catch (HomeException e2)
            {
                new MinorLogMsg(this, "Error saving bill cycle change history record to indicate processed operation for account " + hist.getBAN() + " while processing bill cycle change from " + hist.getOldBillCycleID() + " to " + hist.getNewBillCycleID(), e2).log(ctx);
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Skipping previously processed pending bill cycle history record [" + hist.ID() + "]", null).log(ctx);
            }
            return false;
        }
        
        return true;
    }

    protected void addFailureToHistory(Context ctx, BillCycleHistory source, String msg)
    {
        new MinorLogMsg(this, "Bill cycle change failed for pending request [" + source.ID() + "].  Error Message: " + msg, null).log(ctx);
        
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);

        BillCycleHistory hist = null;
        try
        {
            hist = (BillCycleHistory) XBeans.instantiate(BillCycleHistory.class, ctx);
        }
        catch (Exception e)
        {
            hist = new BillCycleHistory();
        }
        
        hist.setBAN(source.getBAN());
        hist.setBillCycleChangeDate(runningDate);
        hist.setStatus(BillCycleChangeStatusEnum.FAIL);
        hist.setFailureMessage(msg);
        hist.setOldBillCycleID(source.getOldBillCycleID());
        hist.setNewBillCycleID(source.getNewBillCycleID());
        try
        {
            hist = HomeSupportHelper.get(ctx).createBean(ctx, hist);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error saving bill cycle change history record to indicate failed operation for non-existent account " + hist.getBAN() + " while processing bill cycle change from " + hist.getOldBillCycleID() + " to " + hist.getNewBillCycleID(), e).log(ctx);
        }
    }

}
