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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;


/**
 * Run recurring recharge for all accounts in a bill cycle.
 *
 * @author cindy.wong@redknee.com
 */
public class RechargeBillCycleVisitor extends AbstractRechargeVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeBillCycleVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     * @param accountVisitor
     *            The account visitor to use.
     */
    public RechargeBillCycleVisitor(final Date billingDate, final String agentName,
        final ChargingCycleEnum chargingCycle, final Visitor accountVisitor, final boolean recurringRecharge, final boolean proRated, final boolean smsNotificationOnly)
    {
        super(billingDate, agentName, chargingCycle, recurringRecharge, proRated, smsNotificationOnly);
        this.accountVisitor_ = accountVisitor;
    }


    /**
     * Create a new instance of <code>RechargeBillCycleVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     */
    public RechargeBillCycleVisitor(final Date billingDate, final String agentName,
        final ChargingCycleEnum chargingCycle, final boolean recurringRecharge, final boolean proRated, final boolean smsNotificationOnly)
    {
        this(billingDate, agentName, chargingCycle,
                new RechargeAccountVisitor(billingDate, agentName, chargingCycle, recurringRecharge, proRated, smsNotificationOnly), recurringRecharge, proRated, smsNotificationOnly);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException
    {
        final Context subContext = ctx.createSubContext();
        final BillCycle billCycle = (BillCycle) obj;

        try
        {
            if (LogSupport.isDebugEnabled(subContext))
            {
                LogSupport.debug(subContext, this, "Applying recur charges for billing cycle "
                    + billCycle.getBillCycleID());
            }

            final CRMSpid spid = SpidSupport.getCRMSpid(subContext, billCycle.getSpid());
            final SubscriberTypeEnum applicableSubscriberType;
            subContext.put(BillCycle.class, billCycle);
            subContext.put(CRMSpid.class, spid);
            
            double rate = 1.0;
            final Date startDate;
            final Date endDate;

            ChargingCycleHandler handler = ChargingCycleSupportHelper.get(ctx).getHandler(getChargingCycle());
            startDate = handler.calculateCycleStartDate(ctx, getBillingDate(), billCycle.getDayOfMonth(), spid.getId());
            endDate = handler.calculateCycleEndDate(ctx, getBillingDate(), billCycle.getDayOfMonth(), spid.getId());
            
            if (isProRated())
            {
                rate = handler.calculateRate(ctx, getBillingDate(), billCycle.getDayOfMonth(), spid.getId());
            }

            if (SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.WEEKLY))
            {
                applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurChargeSubscriberType());

            }
            else if (SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.MONTHLY))
            {
                applicableSubscriberType = SpidSupport.getSubscriberTypeEnum(spid.getRecurringChargeSubType());
            }
            else
            {
                throw new IllegalArgumentException("Only monthly and weekly recurring recharges are supported");
            }
            
            subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_START_DATE, startDate);
            subContext.put(RecurringRechargeSupport.RECURRING_RECHARGE_END_DATE, endDate);
            
            // Adding rate to the context.
            subContext.put(RecurringRechargeSupport.PRORATED_RATE, Double.valueOf(rate));

            MSP.setBeanSpid(subContext, billCycle.getSpid());
            
            final ProcessAccountInfo info = new ProcessAccountInfo(getBillingDate(), applicableSubscriberType);
            subContext.put(ProcessAccountInfo.class, info);
            
            List <String> eligibleBANs = new ArrayList <String>();
            eligibleBANs = (List<String>) AccountSupport.getMonthlyWeeklyRechargeAndNotificationEligibleBANsList(ctx, billCycle.getBillCycleID(), spid, applicableSubscriberType);
            
            if(!eligibleBANs.isEmpty())
            {
            	Iterator <String> i = eligibleBANs.listIterator();
            	while(i.hasNext())
            	{
            		String ban = i.next();
            		Account account = HomeSupportHelper.get(subContext).findBean(subContext, Account.class, new EQ(AccountXInfo.BAN, ban));
            		
            		accountVisitor_.visit(subContext, account);
            	}
            }
            else
            {
            	LogSupport.info(subContext, this, "No eligible accounts found for MultiDay Recurring Charges for Service Provider "+spid.getId());
            }
        }
        catch (final Exception e)
        {
            LogSupport.minor(subContext, this, "Error when select accounts with billingCycle Id"
                + billCycle.getBillCycleID(), e);
            handleException(subContext, "Error when select accounts with billingCycle Id" + billCycle.getBillCycleID());
        }
    }

    /**
     * Get account system types to filter on, based on applicable system type
     * 
     * @param type
     * @return
     */
    public static final Set<SubscriberTypeEnum> getSystemTypeToCharge(SubscriberTypeEnum type)
    {
        Set<SubscriberTypeEnum> systemTypes = new HashSet<SubscriberTypeEnum>();
        systemTypes.add(SubscriberTypeEnum.HYBRID);
        
        if (SubscriberTypeEnum.PREPAID.equals(type))
        {
            systemTypes.add(SubscriberTypeEnum.PREPAID);
        }
        else if (SubscriberTypeEnum.POSTPAID.equals(type))
        {
            systemTypes.add(SubscriberTypeEnum.POSTPAID);
        }
        else
        {
            systemTypes.add(SubscriberTypeEnum.PREPAID);
            systemTypes.add(SubscriberTypeEnum.POSTPAID);
        }
        return systemTypes;
    }


    /**
     * Returns a set of account state that is applicable to recharge.
     *
     * @return Set of account states which are applicable to recharge.
     */
    public static final Set<AccountStateEnum> getAccountRechargeStateSet(boolean applyChargesToSuspendedAccounts)
    {
        final Set<AccountStateEnum> rechargeSubStates = new HashSet<AccountStateEnum>();

        rechargeSubStates.add(AccountStateEnum.ACTIVE);
        rechargeSubStates.add(AccountStateEnum.NON_PAYMENT_WARN);
        rechargeSubStates.add(AccountStateEnum.NON_PAYMENT_SUSPENDED);
        rechargeSubStates.add(AccountStateEnum.PROMISE_TO_PAY);
        if (applyChargesToSuspendedAccounts)
        {
            rechargeSubStates.add(AccountStateEnum.IN_ARREARS);
            rechargeSubStates.add(AccountStateEnum.SUSPENDED);
        }
        

        return rechargeSubStates;
    }


    /**
     * Creates recharge error report.
     *
     * @param ctx
     *            the Context
     * @param reason
     *            error message
     */
    private void handleException(final Context ctx, final String reason)
    {
        try
        {
            RechargeErrorReportSupport.createReport(ctx, getAgentName(), null, RECHARGE_FAIL_XHOME,
                OCG_RESULT_UNKNOWN, reason, SYSTEM_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, "", null, -1, "", "", this
                    .getBillingDate(), ChargedItemTypeEnum.UNKNOWN);
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction ", e);
        }
    }

    /**
     * Account visitor.
     */
    private final Visitor accountVisitor_;
}
