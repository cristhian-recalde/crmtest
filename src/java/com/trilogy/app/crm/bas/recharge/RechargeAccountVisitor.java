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
package com.trilogy.app.crm.bas.recharge;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * Apply charge for accounts with same billing cycle.
 *
 * @author larry.xia@redknee.com
 */
 
public class RechargeAccountVisitor extends AbstractRechargeVisitor implements ContextAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeAccountVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     */
    public RechargeAccountVisitor(final Date billingDate, final String agentName, final ChargingCycleEnum chargingCycle, boolean recurringRecharge, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        super(billingDate, agentName, chargingCycle, recurringRecharge, proRated, preWarnNotificationOnly);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        final Account account = (Account) ctx.get(Account.class);
        visit(ctx, account);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj)
    {
        final Context subContext = context.createSubContext();
        final Account account = (Account) obj;
        final StringBuilder sb = new StringBuilder();
        sb.append("Account = ");
        sb.append(account.getBAN());
        sb.append(", Recurring Period = ");
        sb.append(getChargingCycle());
        
        String pmModule = RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly());
        if (LogSupport.isDebugEnabled(subContext))
        {
            LogSupport.debug(subContext, this, pmModule + " - Processing " + sb.toString());
        }
        
        final PMLogMsg pmLogMsg = new PMLogMsg(pmModule, "Process Account", sb.toString());

        subContext.put(Account.class, account);
        

        /*
         * [Cindy Wong] 2008-05-06: Don't create payment plan charges if this is a weekly
         * recurring recharge run or if it's only pre-warning notification.
         */
        if (!isPreWarnNotificationOnly() && PaymentPlanSupportHelper.get(subContext).isEnabled(subContext) && SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.MONTHLY))
        {
            /*
             * For all accounts under a Payment Plan, apply the monthly payment plan
             * charge.
             */
            chargePaymentPlanLoanCharges(subContext, account);
        }
        try
        {
            final CRMSpid spid = SpidSupport.getCRMSpid(subContext, account.getSpid());

            Date startDate = (Date) subContext.get(RecurringRechargeSupport.RECURRING_RECHARGE_START_DATE);
            Date endDate = (Date) subContext.get(RecurringRechargeSupport.RECURRING_RECHARGE_END_DATE);
            
            if (startDate == null || endDate == null)
            {
                ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(getChargingCycle());
                startDate = handler.calculateCycleStartDate(context, getBillingDate(), account.getBillCycleDay(context), account.getSpid());
                if(!ChargingCycleEnum.MULTIDAY.equals(getChargingCycle()))
                {
                	/* 
                	 * For multi day , end-date is not same of all MULTI_DAY services
                	 */
                	endDate = handler.calculateCycleEndDate(context, getBillingDate(), account.getBillCycleDay(context), account.getSpid());
                }
                
            }
            

            final RechargeSubscriberVisitor subscriberCharges = new RechargeSubscriberVisitor(getBillingDate(),
                getAgentName(), getChargingCycle(), startDate, endDate, isRecurringRecharge(), isProRated(), isPreWarnNotificationOnly());
    
            try
            {
                if (LogSupport.isDebugEnabled(subContext))
                {
                    LogSupport.debug(subContext, this, "Processing account " + account.getBAN());
                }
    
                Home subHome = (Home) subContext.get(SubscriberHome.class);
    
                final Set<SubscriberStateEnum> rechargeSubStates = getSubscriberRechargeStateSet(spid.isApplyRecurringChargeForSuspendedSubscribers());
    
                subHome = subHome.where(subContext, new EQ(SubscriberXInfo.BAN, account.getBAN())).where(subContext,
                    new In(SubscriberXInfo.STATE, rechargeSubStates));
    
                final SubscriberTypeEnum applicableSubType;
                
                // For pre warn notification, only prepaid subscribers should be notified.
                if (isPreWarnNotificationOnly())
                {
                    applicableSubType = SubscriberTypeEnum.PREPAID;
                }
                else
                {
                    applicableSubType = getApplicableSubscriberType(subContext);
                }
    
                if (applicableSubType != null)
                {
                    subHome = subHome.where(subContext, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, applicableSubType));
                }
                final CompoundVisitor compoundVisit = new CompoundVisitor();
                compoundVisit.add(subscriberCharges);
    
                /*
                 * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
                 * Reason: TT#12051459060; Table; last row
                 * 
                 * For prepaid, we will suspend anyways on recurring charge calculations    .
                 * 
                 * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
                 * will be done only when the "restrict provisioning" flag is true. And we have the requirement
                 * in the TT to move it to suspended state.
                 * 
                 */
                compoundVisit.add(new SuspendEntitiesVisitor());
    
                subHome.forEach(subContext, compoundVisit);
            }
    
            catch (final Throwable t)
            {
                LogSupport.major(subContext, this, "Error when select subscriber with account Id" + account.getBAN(), t);
                handleException(subContext, "Database error when select subscriber with account Id" + account.getBAN() + ", "
                    + t.getMessage());
    
            }
            finally
            {
                accumulate(subscriberCharges);
                pmLogMsg.log(subContext);
            }
        } catch (final HomeException e)
        {
            LogSupport.major(subContext, this, "Error when retrieving SPID for account Id " + account.getBAN(), e);
            handleException(subContext, "Database error when retrieving SPID for account Id " + account.getBAN() + ", "
                + e.getMessage());
        }
    }


    /**
     * Returns a set of subscriber state that is applicable for recurring recharge.
     *
     * @return A Set of subscriber state that is applicable for recurring recharge.
     */
    public static final Set<SubscriberStateEnum> getSubscriberRechargeStateSet(boolean applyChargesToSuspendedAccounts)
    {
        final Set<SubscriberStateEnum> rechargeSubStates = new HashSet<SubscriberStateEnum>();
        rechargeSubStates.add(SubscriberStateEnum.ACTIVE);
        rechargeSubStates.add(SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        rechargeSubStates.add(SubscriberStateEnum.NON_PAYMENT_WARN);
        rechargeSubStates.add(SubscriberStateEnum.PROMISE_TO_PAY);
        
        if (applyChargesToSuspendedAccounts)
        {
            for (int state : RechargeConstants.RECHARGE_SUBSCRIBER_STATES_SUSPENDED)
            {
                rechargeSubStates.add(SubscriberStateEnum.get((short)state));
            }
        }
        return rechargeSubStates;

    }


    /**
     * Create charges for payment plan.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account to be charged.
     */
    public void chargePaymentPlanLoanCharges(final Context ctx, final Account account)
    {
        Transaction trans = null;
        int result = RECHARGE_SUCCESS;
        int ocgResult = OCG_RESULT_SUCCESS;
        try
        {
            if (PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan()))
            {
                /*
                 * [Cindy Wong] 2008-03-27: Payment plan loan adjustment calculation.
                 */
                Date billingDateWithNoTimeOfDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(getBillingDate());

                /*
                 * We want to avoid over charging. Do not create the charge if the payment
                 * plan loan has been fully charged.
                 */
                
                CalculationService service = (CalculationService) ctx.get(CalculationService.class);
                
                final long unchargedPaymentPlanAmount = service.getAccountPaymentPlanUnchargedLoanRemainder(ctx,
                        account.getBAN(), account.getPaymentPlanStartDate(), billingDateWithNoTimeOfDay);

                if (unchargedPaymentPlanAmount < 0)
                {
                    final long chargeAmount = Math.min(account.getPaymentPlanMonthlyAmount(), Math
                        .abs(unchargedPaymentPlanAmount));
                    if (chargeAmount != account.getPaymentPlanMonthlyAmount())
                    {
                        account.setPaymentPlanMonthlyAmount(chargeAmount);
                    }

                    final PMLogMsg pm = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Account Payment Plan Charge Attempt",
                        account.getBAN());
                    try
                    {
                        /*
                         * Avoid creating multiple charges if recurring charge is run
                         * multiple times.
                         */
                        final CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
                        if (spid == null)
                        {
                            throw new HomeException("Service provider " + account.getSpid() + " cannot be found");
                        }
                        if (isPaymentPlanChargeable(ctx, account, chargeAmount, billingDateWithNoTimeOfDay, spid))
                        {
                            try
                            {
                                boolean isPrebilled = spid.isPrebilledRecurringChargeEnabled() && !account.isPrepaid();

                                if (isPrebilled)
                                {
                                    billingDateWithNoTimeOfDay = CalendarSupportHelper.get(ctx).getDayBefore(billingDateWithNoTimeOfDay);
                                }

                                trans = PaymentPlanSupportHelper.get(ctx).createPaymentPlanLoanAdjustment(ctx, chargeAmount, account,
                                    billingDateWithNoTimeOfDay, AdjustmentTypeActionEnum.DEBIT);
                            }
                            catch (final OcgTransactionException e)
                            {
                                ocgResult = e.getErrorCode();
                                result = handleOCGError(ctx, ocgResult);
                                LogSupport.major(ctx, this, "Couldn't charge Account " + account.getBAN()
                                    + " for paymentPlan", e);
                                handlePaymantPlanFailure(ctx, account.getBAN(), account.getResponsibleBAN(), account.getSpid(), trans, result,
                                    ocgResult, "OCG Failure", -1, ChargedItemTypeEnum.PAYMENTPLAN);
                            }
                        }
                    }
                    finally
                    {
                        pm.log(ctx);
                    }
                }
            }
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Couldn't charge Account " + account.getBAN() + " for paymentPlan", e);
            result = RECHARGE_FAIL_XHOME;
            handlePaymantPlanFailure(ctx, account.getBAN(), account.getResponsibleBAN(), account.getSpid(), trans, result, ocgResult,
                "Xhome Exception:" + e.getMessage(), -1, ChargedItemTypeEnum.PAYMENTPLAN);

        }
        catch (final Throwable t)
        {

            LogSupport.major(ctx, this, "Couldn't charge Account " + account.getBAN() + " for paymentPlan", t);
            result = RECHARGE_FAIL_UNKNOWN;
            handlePaymantPlanFailure(ctx, account.getBAN(), account.getResponsibleBAN(), account.getSpid(), trans, result, ocgResult,
                "Failure Unknown:" + t.getMessage(), -1, ChargedItemTypeEnum.PAYMENTPLAN);

        }

        // Should we generate ER 770 for payment plan.
    }


    /**
     * Determines whether payment plan is chargeable in the bill cycle.
     *
     * @param context
     *            The operating context.
     * @param account
     *            Account in question.
     * @param billingDate
     *            Billing date.
     * @return Whether payment plan is chargeable in the bill cycle.
     * @throws HomeException
     *             Thrown if there are problems determining whether the payment plan has
     *             already been charged to this account in this bill cycle.
     */
    private boolean isPaymentPlanChargeable(final Context context, final Account account, long amount,  final Date billingDate, CRMSpid spid)
        throws HomeException
    {
        final int billingCycleDay = account.getBillCycleDay(context);

        ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(ChargingCycleEnum.MONTHLY);
        Date startDate = handler.calculateCycleStartDate(context, billingDate, billingCycleDay, spid.getId());
        final Date endDate = handler.calculateCycleEndDate(context, billingDate, billingCycleDay, spid.getId());
        
        boolean isPrebilled = spid.isPrebilledRecurringChargeEnabled() && !account.isPrepaid();

        if (isPrebilled)
        {
            startDate = CalendarSupportHelper.get(context).getDayBefore(startDate);
        }

        /*
         * check payment plan start date, in case the account is on payment plan more than
         * once in the bill cycle.
         */
        if (startDate.before(account.getPaymentPlanStartDate()))
        {
            startDate = account.getPaymentPlanStartDate();
        }
        final AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(context).getAdjustmentType(context,
            AdjustmentTypeEnum.PaymentPlanLoanAdjustment);

        return TransactionSupport.isChargeable(context, account, adjustmentType.getCode(), amount , startDate, endDate,
            billingDate, isPrebilled);
    }


    /**
     * Get applicable subscriber type from configuration/context.
     *
     * @param ctx
     *            Context
     * @return The subscriber type which is chargeable
     */
    public SubscriberTypeEnum getApplicableSubscriberType(final Context ctx)
    {
        return ((ProcessAccountInfo) ctx.get(ProcessAccountInfo.class)).getApplicableSubscriberType();
    }


    /**
     * Create recharge error report.
     *
     * @param ctx
     *            Context
     * @param reason
     *            The root cause of error
     */
    private void handleException(final Context ctx, final String reason)
    {

        try
        {
            RechargeErrorReportSupport.createReport(ctx, getAgentName(), null, RECHARGE_FAIL_UNKNOWN,
                OCG_RESULT_UNKNOWN, reason, ACCOUNT_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, "", null, -1, "", "", this
                    .getBillingDate(), ChargedItemTypeEnum.UNKNOWN);
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction ", e);
        }
    }


    /**
     * Update the recharge summary data.
     *
     * @param visitor
     *            the visitor used in charging
     */
    @Override
    public void accumulate(final RechargeVisitorCountable visitor)
    {
        super.accumulate(visitor);
        accumulateForOne(visitor.getChargeAmountFailed() == 0);
    }


    /**
     * Accumulate counter.
     *
     * @param success
     *            Whether the charge was successful.
     */
    private synchronized void accumulateForOne(final boolean success)
    {
        if (success)
        {
            this.incrementAccountSuccessCount();
        }
        else
        {
            this.incrementAccountFailCount();
        }
        this.incrementAccountCount();
    }


    /**
     * Mapping OCG error code to a recharge result code.
     *
     * @param ctx
     *            The context
     * @param code
     *            OCG result code
     * @return Recurring recharge result code.
     */
    public int handleOCGError(final Context ctx, final int code)
    {

        int ret = RECHARGE_FAIL_OCG_UNKNOWN;

        switch (code)
        {
            case com.redknee.product.s2100.ErrorCode.BAL_EXPIRED:
                ret = RECHARGE_FAIL_ABM_EXPIRED;
                break;
            case com.redknee.product.s2100.ErrorCode.BALANCE_INSUFFICIENT:
            case com.redknee.product.s2100.ErrorCode.NOT_ENOUGH_BAL:
                ret = RECHARGE_FAIL_ABM_LOWBALANCE;
                break;
            case com.redknee.product.s2100.ErrorCode.RECORD_NOT_FOUND:
                ret = RECHARGE_FAIL_ABM_INVALIDPROFILE;
                break;
            case com.redknee.product.s2100.ErrorCode.ILLEGAL_PROFILE_STATE:
                ret = RECHARGE_FAIL_ABM_INVALID_STATE;
                break;
            default:

        }

        return ret;
    }


    /**
     * Create recharge error report for payment plan.
     *
     * @param ctx
     *            the Context
     * @param ban
     *            the ban of account
     * @param spid
     *            the SPID of account
     * @param trans
     *            the transaction, could be null
     * @param resultCode
     *            the recharge result code
     * @param ocgResultCode
     *            the result code from OCG
     * @param reason
     *            the error message of charge
     * @param chargedItemId
     *            the charging item id, it is -1 for payment plan
     * @param chargedItemType
     *            the charging item type, it should chargedItemType.PAYMENTPLAN
     */
    public void handlePaymantPlanFailure(final Context ctx, final String ban, final String responsibleBAN, final int spid, final Transaction trans,
        final int resultCode, final int ocgResultCode, final String reason, final long chargedItemId,
        final ChargedItemTypeEnum chargedItemType)
    {
        try
        {
            this.incrementChargeAmountFailed(trans.getAmount());
            RechargeErrorReportSupport.createReport(ctx, getAgentName(), trans, resultCode, ocgResultCode,
                reason, chargedItemId, ban, responsibleBAN, spid, "", "", this.getBillingDate(), chargedItemType);
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction sub = " + trans.getSubscriberID()
                + " adjustment type = " + trans.getAdjustmentType(), t);
        }
    }
}
