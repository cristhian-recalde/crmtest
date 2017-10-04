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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Apply recurring charge to a service provider.
 *
 * @author cindy.wong@redknee.com
 * @since 6-May-08
 */
public class RechargeSpidVisitor extends AbstractRechargeVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeSpidVisitor</code>.
     *
     * @param billingDate
     *            Billing Date.
     * @param agentName
     *            Agent name.
     * @param servicePeriod
     *            Service period to charge.
     * @param accountVisitor
     *            Visitor to use for account recurring charges.
     */
    public RechargeSpidVisitor(final Date billingDate, final String agentName, final ChargingCycleEnum chargingCycle,
        final Visitor accountVisitor, boolean recurringRecharge, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        super(billingDate, agentName, chargingCycle, recurringRecharge, proRated, preWarnNotificationOnly);
        this.accountVisitor_ = accountVisitor;
    }


    /**
     * Create a new instance of <code>RechargeSpidVisitor</code>.
     *
     * @param billingDate
     *            Billing Date.
     * @param agentName
     *            Agent name.
     * @param servicePeriod
     *            Service period to charge.
     */
    public RechargeSpidVisitor(final Date billingDate, final String agentName, final ChargingCycleEnum chargingCycle,
            final boolean recurringRecharge, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        this(billingDate, agentName, chargingCycle,
                new RechargeAccountVisitor(billingDate, agentName, chargingCycle, recurringRecharge, proRated, preWarnNotificationOnly), recurringRecharge, proRated, preWarnNotificationOnly);
    }


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
    {
        final CRMSpid spid = (CRMSpid) obj;
        Collection billCycles = null;
        try
        {
            billCycles = getBillCyclesToCharge(ctx, spid);
        }
        catch (final Exception exception)
        {
            LogSupport.info(ctx, this, "Exception caught when looking up bill cycles for service provider "
                + spid.getSpid(), exception);
            throw new AgentException(exception);
        }

        if (billCycles == null)
        {
            LogSupport.info(ctx, this, "No bill cycles of service provider " + spid.getSpid()
                + " suitable for recurring recharge, exiting");
        }
        else
        {
            try
            {
                Visitors.forEach(ctx, billCycles, new RechargeBillCycleVisitor(getBillingDate(), getAgentName(),
                        getChargingCycle(), this.accountVisitor_, isRecurringRecharge(), isProRated(), isPreWarnNotificationOnly()));
            }
            catch (final Exception exception)
            {
                LogSupport.minor(ctx, this,
                    "Exception caught when attempting to generate recurring recharge for service provider "
                        + spid.getSpid(), exception);
                handleException(ctx, "Error when processing bill cycle of service provider " + spid.getSpid());
            }
        }
    }


    /**
     * Return the bill cycles to charge.
     *
     * @param context
     *            The operating context.
     * @param spid
     *            Service provider.
     * @return A collection of bill cycles to charge.
     * @throws HomeException
     *             Thrown if there are problems determining which bill cycles to charge.
     */
    private Collection getBillCyclesToCharge(final Context context, final CRMSpid spid) throws HomeException
    {
        final Collection billCycles;
        if (SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.MONTHLY))
        {
            billCycles = BillCycleSupport.getAllBillCyclesForSpid(context, spid.getSpid(),
                    CalendarSupportHelper.get(context).getDayOfMonth(getBillingDate()));
        }
        else if (SafetyUtil.safeEquals(getChargingCycle(), ChargingCycleEnum.WEEKLY))
        {
            final int dayOfWeek = BillCycleSupport.computeBillingDayOfWeek(getBillingDate());
            final WeekDayEnum chargeableDayOfWeek = spid.getWeeklyRecurChargingDay();
            if (chargeableDayOfWeek != null && chargeableDayOfWeek.getIndex() == dayOfWeek)
            {

                billCycles = BillCycleSupport.getAllBillCyclesForSpid(context, spid.getSpid());
            }
            else
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, this, ("Weekly recurring recharge of service provider " + spid.getSpid()
                        + " does not fall on " + chargeableDayOfWeek.getDescription()));
                }
                billCycles = Collections.EMPTY_SET;
            }
        }
        else
        {
            throw new IllegalArgumentException("Only monthly and weekly recurring recharges are supported");
        }

        return billCycles;
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
