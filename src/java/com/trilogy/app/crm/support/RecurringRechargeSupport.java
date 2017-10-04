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

package com.trilogy.app.crm.support;

import java.io.IOException;
import java.util.Date;

import com.trilogy.app.crm.bas.recharge.MonthlyRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.MultiDayRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.OptimizedRecurRecharge;
import com.trilogy.app.crm.bas.recharge.ProcessAccountInfo;
import com.trilogy.app.crm.bas.recharge.RechargeAccountVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeBillCycleVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeErrorReportSupport;
import com.trilogy.app.crm.bas.recharge.RechargeSubscriberVisitor;
import com.trilogy.app.crm.bas.recharge.WeeklyRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.RecurringRechargeFormXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.util.snippet.log.Logger;


/**
 * Support class for recurring recharge and general charging.
 *
 * @author cindy.wong@redknee.com
 * @since 9-May-08
 */
public final class RecurringRechargeSupport
{

    /**
     * Number of days in a week.
     */
    public static final int DAYS_IN_WEEK = 7;
    
    /**
     * Insufficient Balance Subscription Notification
     */
    public static final String INSUFFICIENT_BALANCE_NOTIFICATION = "INSUFFICIENT_BALANCE_NOTIFICATION";
    
    /**
     * Notification ONLY
     */
    public static final String NOTIFICATION_ONLY = "NOTIFICATION_ONLY";

    /**
     * Creates a new <code>RecurringRechargeSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private RecurringRechargeSupport()
    {
        // empty
    }


    /**
     * For auxiliary services (SubscriberSubscriptionHistory), if there is no transaction between start and end date
     * or last transaction is refund, subscriber is not charged (false). On the other hand, if there is a
     * charge transaction or its date is after the billing date, subscriber is charged (true).
     * 
     * For other items (Transaction), if there is no transaction between start and end date or last transaction is
     * refund, subscriber is not charged (false). Otherwise subscriber is charged (true).
     * 
     * @param context
     * @param subscriber
     * @param item
     * @param itemType
     * @param servicePeriod
     * @param adjustmentType
     * @param amount
     * @param billingDate
     * @return
     * @throws HomeException
     */
    public static boolean isSubscriberChargedAndNotRefunded(final Context parentCtx, final Subscriber subscriber,
            final Object item, final ChargedItemTypeEnum itemType, final ServicePeriodEnum servicePeriod,
            final int adjustmentType, final long amount, final Date billingDate)
            throws HomeException
    {
        Context context = parentCtx.createSubContext();
        context.put(Subscriber.class, subscriber);
        
        final int billCycleDay = SubscriberSupport.getBillCycleDay(context, subscriber);
        final CRMSpid spid = SpidSupport.getCRMSpid(context, subscriber.getSpid());

        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(context).getHandler(servicePeriod);
        Date startDate = handler.calculateCycleStartDate(context, billingDate, billCycleDay, spid.getId(), subscriber.getId(), item);
        final Date endDate = handler.calculateCycleEndDate(context, billingDate, billCycleDay, spid.getId(), subscriber.getId(), item);
 
        return !SubscriberSubscriptionHistorySupport.isChargeable(context, subscriber.getId(),
                    itemType, item, billingDate, startDate, endDate);
    }


    /**
    /**
     * For all items, if there is no transaction between start and end date
     * or last transaction is refund, subscriber is chargeable (true). On the other hand, if there is a
     * charge transaction or its date is after the billing date, subscriber is not chargeable (true).
     * 
     * @param context
     * @param subscriber
     * @param item
     * @param itemType
     * @param adjustmentType
     * @param amount
     * @param agentName
     * @param billingDate
     * @param startDate
     * @param endDate
     * @param isPrebilled
     * @return
     * @throws HomeException
     */
    public static boolean isSubscriberChargeable(final Context context, final Subscriber subscriber,
            final Object item, final ChargedItemTypeEnum itemType, final int adjustmentType, final long amount,
            final String agentName, final Date billingDate, final Date startDate, final Date endDate, final boolean isPrebilled) throws HomeException
    {
            final PMLogMsg pm = new PMLogMsg(RECURRING_RECHARGE_PM_MODULE, "Is chargeable (SubscriberSubscriptionHistory)", "");
            try
            {
                return SubscriberSubscriptionHistorySupport.isChargeable(context, subscriber.getId(),
                    itemType, item, billingDate, startDate, endDate);
            }
            finally
            {
                pm.log(context);
            }
    }

    /**
     * Create a transaction object with the provided recurring recharge detail. The
     * transaction is not actually added to transaction home.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being charged.
     * @param rate
     *            Prorating rate.
     * @param adj
     *            Adjustment type.
     * @param fee
     *            The full monthly fee to be charged.
     * @param agent
     *            The agent creating this transaction.
     * @param billingDate
     *            Billing date of this transaction.
     * @return A transaction with the provided recurring recharge detail.
     */
    public static Transaction createRechargeTransaction(final Context context, final Subscriber subscriber,
        final double rate, final int adj, final long fee, final String agent, final Date billingDate, final Date serviceEndDate, long subscriptionTypeId)
    {
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch (Exception exception)
        {
            new InfoLogMsg(RecurringRechargeSupport.class, "Unable to instantiate transaction bean", exception).log(context);
            return null;
        }

        transaction.setBAN(subscriber.getBAN());
        try
        {
            transaction.setResponsibleBAN(subscriber.getAccount(context).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(context, RecurringRechargeSupport.class,
                    "Unable to retrieve Account [" + subscriber.getBAN() + "]", e);
        }

        transaction.setMSISDN(subscriber.getMSISDN());
        transaction.setSubscriberID(subscriber.getId());
        transaction.setSpid(subscriber.getSpid());
        transaction.setAdjustmentType(adj);

        final long amount = Math.round(fee * rate);

        transaction.setFullCharge(fee);
        transaction.setAmount(amount);
        transaction.setReceiveDate(billingDate);
        transaction.setRatio(rate);

        transaction.setTaxPaid(0);

        transaction.setAgent(agent);

        transaction.setSubscriptionCharge(false);
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setExemptCreditLimitChecking(true);
        transaction.setSubscriptionTypeId(subscriptionTypeId);
        
        transaction.setServiceEndDate(serviceEndDate);
        
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, RecurringRechargeSupport.class, "Creating recurring charge [amount = " + amount
                + ", adjustment type = " + adj + "] for subscriber " + subscriber.getId() + " [MSISDN = "
                + subscriber.getMSISDN() + "] by agent " + agent);
        }
        return transaction;
    }
    
    /**
     * Create a transaction object with the provided recurring recharge detail. The
     * transaction is not actually added to transaction home.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            The subscriber being charged.
     * @param rate
     *            Prorating rate.
     * @param adj
     *            Adjustment type.
     * @param fee
     *            The full monthly fee to be charged.
     * @param agent
     *            The agent creating this transaction.
     * @param billingDate
     *            Billing date of this transaction.
     * @return A transaction with the provided recurring recharge detail.
     */
    public static Transaction createCUGOwnerRechargeTransaction(final Context context, final Subscriber subscriber,
        final double rate, final int adj, final long fee, final String agent, final Date billingDate, final Date serviceEndDate, long subscriptionTypeId, String supportedSub)
    {
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, context);
        }
        catch (Exception exception)
        {
            new InfoLogMsg(RecurringRechargeSupport.class, "Unable to instantiate transaction bean", exception).log(context);
            return null;
        }
        transaction.setBAN(subscriber.getBAN());
        try
        {
            transaction.setResponsibleBAN(subscriber.getAccount(context).getResponsibleBAN());
        }
        catch (HomeException e)
        {
            Logger.minor(context, RecurringRechargeSupport.class,
                    "Unable to retrieve Account [" + subscriber.getBAN() + "]", e);
        }

        transaction.setMSISDN(subscriber.getMSISDN());
        transaction.setSubscriberID(subscriber.getId());
        transaction.setSupportedSubscriberID(supportedSub);
        transaction.setSpid(subscriber.getSpid());
        transaction.setAdjustmentType(adj);

        final long amount = Math.round(fee * rate);

        transaction.setFullCharge(fee);
        transaction.setAmount(amount);
        transaction.setReceiveDate(billingDate);
        transaction.setRatio(rate);

        transaction.setTaxPaid(0);

        transaction.setAgent(agent);

        transaction.setSubscriptionCharge(false);
        transaction.setReasonCode(Long.MAX_VALUE);
        transaction.setExemptCreditLimitChecking(true);
        transaction.setSubscriptionTypeId(subscriptionTypeId);
        
        transaction.setServiceEndDate(serviceEndDate);
        
        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, RecurringRechargeSupport.class, "Creating recurring charge [amount = " + amount
                + ", adjustment type = " + adj + "] for subscriber " + subscriber.getId() + " [MSISDN = "
                + subscriber.getMSISDN() + "] by agent " + agent);
        }
        return transaction;
    }


    /**
     * Creates an ER for the recurring charge.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            Recurring charge transaction.
     * @param subscriber
     *            The subscriber being charged.
     * @param serviceId
     *            Identifier of the service being charged.
     * @param resultCode
     *            Result code.
     * @param ocgResultCode
     *            OCG result code.
     */
    public static void createRechargeER(final Context ctx, final Transaction trans, final Subscriber subscriber,
        final String serviceId, final int resultCode, final int ocgResultCode, long subscriptionTypeId)
    {
        Transaction transaction = trans;
        if (transaction == null)
        {
            transaction = new Transaction();
            transaction.setMSISDN(subscriber.getMSISDN());
            transaction.setBAN(subscriber.getBAN());
            transaction.setSpid(subscriber.getSpid());
            transaction.setAmount(0);
            transaction.setAdjustmentType(-1);
            transaction.setSubscriptionTypeId(subscriptionTypeId);

        }
        ERLogger.createRecurRechargeEr(ctx, transaction, subscriber.getPricePlan(), serviceId, ocgResultCode,
            resultCode);

    }


    /**
     * Creates recharge error report.
     *
     * @param ctx
     *            The operating context.
     * @param agentName
     *            Name of the agent failed to charge this subscriber.
     * @param billingDate
     *            Billing date of the charge.
     * @param subscriber
     *            The subscriber failed to be charged.
     * @param trans
     *            Transaction triggering the error.
     * @param resultCode
     *            Result code to report.
     * @param ocgResultCode
     *            OCG result code.
     * @param reason
     *            Reason of the failure.
     * @param chargedItemId
     *            Identifier of the item being charged.
     * @param chargedItemType
     *            The type of the item being charged.
     */
    public static void handleRechargeFailure(final Context ctx, final String agentName, final Date billingDate,
        final Subscriber subscriber, final Transaction trans, final int resultCode, final int ocgResultCode,
        final String reason, final long chargedItemId, final ChargedItemTypeEnum chargedItemType)
    {

        try
        {
            RechargeErrorReportSupport.createReport(ctx, agentName, trans, resultCode, ocgResultCode, reason,
                chargedItemId, subscriber.getBAN(), subscriber.getAccount(ctx).getResponsibleBAN(), subscriber.getSpid(), subscriber.getMSISDN(), subscriber.getId(),
                billingDate, chargedItemType);
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, RecurringRechargeSupport.class, "fail to create error report for transaction ", t);
        }
    }


    /**
     * Creates recharge error report.
     *
     * @param ctx
     *            The operating context.
     * @param agentName
     *            Name of the agent failed to charge this subscriber.
     * @param billingDate
     *            Billing date of the charge.
     * @param subscriber
     *            The subscriber failed to be charged.
     * @param resultCode
     *            Result code to report.
     * @param ocgResultCode
     *            OCG result code.
     * @param reason
     *            Reason of the failure.
     * @param chargedItemId
     *            Identifier of the item being charged.
     * @param chargedItemType
     *            The type of the item being charged.
     * @param amount
     *    
     */
    public static void handleRechargeFailure(final Context ctx, final String agentName, final Date billingDate,
            final Subscriber subscriber, final int resultCode, final int ocgResultCode, final String reason,
            final long chargedItemId, final ChargedItemTypeEnum chargedItemType, final long amount)
    {
        try
        {
            RechargeErrorReportSupport.createReport(ctx, agentName, resultCode, ocgResultCode, reason, chargedItemId,
                    subscriber.getBAN(), subscriber.getAccount(ctx).getResponsibleBAN(), subscriber.getSpid(),
                    subscriber.getMSISDN(), subscriber.getId(), billingDate, chargedItemType, amount);
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, RecurringRechargeSupport.class, "fail to create error report for transaction ", t);
        }
    }
    
    

    /**
     * Return the recurring recharge agent name based on the service period.
     *
     * @param servicePeriod
     *            Service period.
     * @return The name of the recurring recharge agent.
     */
    public static String getAgentName(final ChargingCycleEnum chargingCycle)
    {
        String agentName ="AgentNameDefault";
        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
        {
            agentName = WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME;
        }
        else if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MONTHLY))
        {
            agentName = MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME;
        }else if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MULTIDAY))
        {
        	 agentName = MultiDayRecurringRechargesLifecycleAgent.AGENT_NAME;
        }
        return agentName;
    }


    /**
     * Apply recurring recharge to the subscriber if it is eligible for the recurring
     * recharge (i.e. on bill cycle day for monthly charges, or on the SPID's billing
     * day-of-week for weekly charges).
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            Subscriber identifier.
     * @param servicePeriod
     *            Service period.
     * @param billingDate
     *            Billing date.
     * @throws HomeException
     *             Thrown if there are problems selecting the subscriber to charge.
     * @throws AgentException
     *             Thrown if there are problems applying the recurring recharge.
     */
    public static void applySubscriberRecurringRecharge(final Context context, final String subscriberId,
        final ChargingCycleEnum chargingCycle, final Date billingDate) throws HomeException, AgentException
    {
        final Subscriber subscriber = SubscriberSupport.getSubscriber(context, subscriberId);
        final CRMSpid spid = SpidSupport.getCRMSpid(context, subscriber.getSpid());
        final Account account = subscriber.getAccount(context);
        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(chargingCycle);
        
        final Date startDate = handler.calculateCycleStartDate(context, billingDate,  account.getBillCycleDay(context), spid.getId());
        final Date endDate = handler.calculateCycleEndDate(context, billingDate,  account.getBillCycleDay(context), spid.getId());

        final Visitor visitor = new RechargeSubscriberVisitor(billingDate, getAgentName(chargingCycle), chargingCycle, startDate, endDate, true, false, false);
        if (subscriber == null)
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.SUBSCRIBER_IDENTIFIER,
                "Subscriber does not exist");
        }

        final SubscriberTypeEnum applicableSubscriberType;

        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
        {
            applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurChargeSubscriberType());
        }
        else
        {
            applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurringChargeSubType());
        }

        if (applicableSubscriberType != null && subscriber.getSubscriberType() != applicableSubscriberType)
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.SUBSCRIBER_IDENTIFIER,
                "Recurring recharge is not applicable to this type of subscriber.");
        }

        /**
         * Since the subscriber is associated with an individual account, we can use that 
         * to fetch BillCycle (and perform operations related to BillCycle)
         */
        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MONTHLY)
            && !BillCycleSupport.isCycleStartOfAccount(context, billingDate, account))
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILL_CYCLE_ID,
                "Provided billing date does not fall on the same day of month "
                    + "as the bill cycle of the account owning this subscriber");
        }
        else if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
        {
            final WeekDayEnum billingDayOfWeek = WeekDayEnum.get((short) BillCycleSupport
                .computeBillingDayOfWeek(billingDate));
            if (!SafetyUtil.safeEquals(billingDayOfWeek, spid.getWeeklyRecurChargingDay()))
            {
                throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILLING_DATE,
                    "Provided billing date does not fall on the same day of week "
                        + "as the service provider's weekly recurring recharge settings");
            }
        }

        final ProcessAccountInfo info = new ProcessAccountInfo(billingDate, applicableSubscriberType);
        context.put(ProcessAccountInfo.class, info);
        Context subContext = context.createSubContext();
        subContext.put(Subscriber.class, subscriber);
        subContext.put(Account.class, account);
        visitor.visit(context, subscriber);
    }


    /**
     * Apply recurring recharge to all of the subscribers belonging the account who are
     * eligible for the recurring recharge (i.e. on bill cycle day for monthly charges, or
     * on the SPID's billing day-of-week for weekly charges).
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account identifier.
     * @param servicePeriod
     *            Service period.
     * @param billingDate
     *            Billing date.
     * @throws HomeException
     *             Thrown if there are problems selecting the account to charge.
     * @throws AgentException
     *             Thrown if there are problems applying the recurring recharge.
     */
    public static void applyAccountRecurringRecharge(final Context context, final String ban,
        final ChargingCycleEnum chargingCycle, final Date billingDate) throws HomeException, AgentException
    {
        final Visitor visitor = new RechargeAccountVisitor(billingDate, getAgentName(chargingCycle), chargingCycle, true, false, false);

        final Account account = AccountSupport.getAccount(context, ban);
        if (account == null)
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.ACCOUNT_IDENTIFIER,
                "Account does not exist");
        }
        final CRMSpid spid = SpidSupport.getCRMSpid(context, account.getSpid());

        final SubscriberTypeEnum applicableSubscriberType;

        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
        {
            applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurChargeSubscriberType());
        }
        else
        {
            applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurringChargeSubType());
        }

        final ProcessAccountInfo info = new ProcessAccountInfo(billingDate, applicableSubscriberType);
        context.put(ProcessAccountInfo.class, info);

        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MONTHLY)
            && !BillCycleSupport.isCycleStartOfAccount(context, billingDate, account))
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILL_CYCLE_ID,
                "Provided billing date does not fall on the same day of month as the bill cycle of the account");
        }
        else if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
        {
            final WeekDayEnum billingDayOfWeek = WeekDayEnum.get((short) BillCycleSupport
                .computeBillingDayOfWeek(billingDate));
            if (!SafetyUtil.safeEquals(billingDayOfWeek, spid.getWeeklyRecurChargingDay()))
            {
                throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILLING_DATE,
                    "Provided billing date does not fall on the same day of week "
                        + "as the service provider's weekly recurring recharge settings");
            }
        }

        visitor.visit(context, account);
    }


    /**
     * Apply recurring recharge to all of the subscribers belonging the bill cycle who are
     * eligible for the recurring recharge (i.e. on bill cycle day for monthly charges, or
     * on the SPID's billing day-of-week for weekly charges).
     *
     * @param context
     *            The operating context.
     * @param billCycleId
     *            Bill cycle ID.
     * @param servicePeriod
     *            Service period.
     * @param billingDate
     *            Billing date.
     * @throws HomeException
     *             Thrown if there are problems selecting the bill cycle to charge.
     * @throws AgentException
     *             Thrown if there are problems applying the recurring recharge.
     */
    public static void applyBillCycleRecurringRecharge(final Context context, final int billCycleId,
        final ChargingCycleEnum chargingCycle, final Date billingDate) throws HomeException, AgentException
    {
        BillCycle billCycle;
        billCycle = BillCycleSupport.getBillCycle(context, billCycleId);
        if (billCycle == null)
        {
            throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILL_CYCLE_ID,
                "Bill cycle does not exist");
        }
        final CRMSpid spid = SpidSupport.getCRMSpid(context, billCycle.getSpid());

        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MONTHLY))
        {
            final Visitor visitor = new RechargeBillCycleVisitor(billingDate, getAgentName(chargingCycle), chargingCycle, true, false, false);

            if (!BillCycleSupport.isBillCycleStartDay(billCycle, billingDate))
            {
                throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILL_CYCLE_ID,
                    "Provided billing date does not fall on the same day of month as the bill cycle");
            }

            visitor.visit(context, billCycle);
        }
        else
        {
            if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
            {
                final WeekDayEnum billingDayOfWeek = WeekDayEnum.get((short) BillCycleSupport
                        .computeBillingDayOfWeek(billingDate));
                if (!SafetyUtil.safeEquals(billingDayOfWeek, spid.getWeeklyRecurChargingDay()))
                {
                    throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILLING_DATE,
                            "Provided billing date does not fall on the same day of week "
                            + "as the service provider's weekly recurring recharge settings");
                }
            }

            OptimizedRecurRecharge charger = new OptimizedRecurRecharge(context, billingDate, chargingCycle, getAgentName(chargingCycle), -1, billCycleId);
            charger.execute();
        }
    }


    /**
     * Apply recurring recharge to all of the subscribers belonging the service provider
     * who are eligible for the recurring recharge (i.e. on bill cycle day for monthly
     * charges, or on the SPID's billing day-of-week for weekly charges).
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider ID.
     * @param servicePeriod
     *            Service period.
     * @param billingDate
     *            Billing date.
     * @throws HomeException
     *             Thrown if there are problems applying the recurring recharge.
     */
    public static void applySpidRecurringRecharge(final Context context, final int spid,
        final ChargingCycleEnum chargingCycle, final Date billingDate) throws HomeException
    {
        final CRMSpid serviceProvider = SpidSupport.getCRMSpid(context, spid);

        if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.MONTHLY))
        {
            final Visitor visitor = new RechargeBillCycleVisitor(billingDate, getAgentName(chargingCycle), chargingCycle, true, false, false);

            final Home billCycleHome = (Home) context.get(BillCycleHome.class);
            final And and = new And();
            and.add(new EQ(BillCycleXInfo.SPID, Integer.valueOf(spid)));

            final int dayOfMonth = CalendarSupportHelper.get(context).getDayOfMonth(billingDate);
            and.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, Integer.valueOf(dayOfMonth)));

            billCycleHome.forEach(context, visitor, and);
        }
        else
        {
            if (SafetyUtil.safeEquals(chargingCycle, ChargingCycleEnum.WEEKLY))
            {
                final WeekDayEnum billingDayOfWeek = WeekDayEnum.get((short) BillCycleSupport
                    .computeBillingDayOfWeek(billingDate));
                if (!SafetyUtil.safeEquals(billingDayOfWeek, serviceProvider.getWeeklyRecurChargingDay()))
                {
                    throw new IllegalPropertyArgumentException(RecurringRechargeFormXInfo.BILLING_DATE,
                        "Provided billing date does not fall on the same day of week "
                            + "as the service provider's weekly recurring recharge settings");
                }
            }

            OptimizedRecurRecharge charger = new OptimizedRecurRecharge(context, billingDate, chargingCycle, getAgentName(chargingCycle), spid, -1);
            charger.execute();
        }
    }


    /**
     * Apply all recurring charges of a subscriber if this is a charging day. Currently,
     * only monthly and weekly are supported.
     *
     * @param context
     *            The operating context.
     * @param subscriberId
     *            The subscriber identifier.
     * @param billingDate
     *            Billing date.
     * @throws HomeException
     *             Thrown if there are problems applying the recurring recharges.
     */
    public static void applySubscriberAllRecurringRecharges(final Context context, final String subscriberId,
        final Date billingDate) throws HomeException
    {
        try
        {
            applySubscriberRecurringRecharge(context, subscriberId, ChargingCycleEnum.MONTHLY, billingDate);
        }
        catch (final IllegalPropertyArgumentException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, RecurringRechargeSupport.class, "Recurring charge not applied to subscriber",
                    exception);
            }
        }
        catch (final AgentException exception)
        {
            throw new HomeException(exception);
        }
        try
        {
            applySubscriberRecurringRecharge(context, subscriberId, ChargingCycleEnum.WEEKLY, billingDate);
        }
        catch (final IllegalPropertyArgumentException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, RecurringRechargeSupport.class, "Recurring charge not applied to subscriber",
                    exception);
            }
        }
        catch (final AgentException exception)
        {
            throw new HomeException(exception);
        }
    }
    
    public static String getRecurringRechargePMModule(boolean preWarnNotificationOnly)
    {
        if (preWarnNotificationOnly)
        {
            return PRE_WARN_NOTIFICATION_PM_MODULE;
        }
        else
        {
            return RECURRING_RECHARGE_PM_MODULE;
        }
    }


    
    
    public static final String RECURRING_RECHARGE_PM_MODULE = "ServicesRecurringRecharges";
    
    public static final String PRE_WARN_NOTIFICATION_PM_MODULE = "RecurringRechargesPreWarning";

    public static final String PRORATED_RATE = "RecurringRechargeProratedRate";

    public static final String PRORATED_RATE_MAP = "RecurringRechargeProratedRateMap";

    public static final String RECURRING_RECHARGE_START_DATE = "RecurringRechargeStartDate";

    public static final String RECURRING_RECHARGE_END_DATE = "RecurringRechargeEndDate";
    
    public static final String RECURRING_RECHARGE_CHARGED_ITEM = "RecurringRechargeChargedItem";

    public static final String RECURRING_RECHARGE_PRE_WARNING_NOTIFICATION_BILLING_CYCLE_DAY = "RecurringRechargePreWarningNotificationBillingCycleDay";
    
}