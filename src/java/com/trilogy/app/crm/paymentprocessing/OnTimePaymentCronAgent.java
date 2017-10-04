/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.paymentprocessing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.paymentprocessing.OnTimeInvoicePaymentProcessingCronAgent;
import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Cron agent to process on-time payment.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
public class OnTimePaymentCronAgent extends OnTimeInvoicePaymentProcessingCronAgent
{

    /**
     * Gets the billing cycles which should be processed for on-time payment.
     * 
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            has the bill cycle date.
     * @return A list of billing cycles.
     */
    protected Collection<BillCycle> getBillingCyclesToProcess(
            final Context ctx, final Date billingDate)
    {
        OnTimePaymentSpidBillCycleVisitor visitor = new OnTimePaymentSpidBillCycleVisitor(
                billingDate);
        Home home = (Home) ctx.get(CRMSpidHome.class);
        try
        {
            visitor = (OnTimePaymentSpidBillCycleVisitor) home.forEach(ctx,
                    visitor);
        }
        catch (HomeException exception)
        {
            new MajorLogMsg(getClass(),
                    "Cannot get bill cycles to process for on-time payment",
                    exception).log(ctx);
            throw new CronContextAgentException(exception);
        }

        return visitor.getBillCycles();
    }
}

/**
 * Visitor to return bill cycles suitable for on-time payment processing.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
class OnTimePaymentSpidBillCycleVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Bill cycles to be processed.
     */
    List<BillCycle> billCycles_ = new ArrayList<BillCycle>();

    /**
     * Date to act upon.
     */
    Date billingDate_;

    OnTimePaymentSpidBillCycleVisitor(Date billingDate)
    {
        billingDate_ = billingDate;
    }

    /**
     * For a SPID, determines the bill cycles which belongs to this SPID which
     * should be processed for on-time payment.
     * 
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        CRMSpid spid = (CRMSpid) obj;
        if (spid.isOnTimePaymentEnabled())
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(billingDate_);
            calendar.add(Calendar.DAY_OF_MONTH, -spid
                    .getOnTimePaymentNumDaysPastDue());
            calendar.add(Calendar.DAY_OF_MONTH, -spid.getPaymentDuePeriod());

            Home billCycleHome = (Home) ctx.get(BillCycleHome.class);
            And and = new And();
            and.add(new EQ(BillCycleXInfo.SPID, spid.getSpid()));
            and.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, calendar
                    .get(Calendar.DAY_OF_MONTH)));
            try
            {
                billCycles_.addAll(billCycleHome.select(ctx, and));
            }
            catch (HomeException exception)
            {
                new MinorLogMsg(getClass(),
                        "Bill Cycle select failed for SPID " + spid.getSpid(),
                        exception).log(ctx);
            }
        }
    }

    /**
     * Returns bill cycles to be processed for on-time payment.
     * 
     * @return a collection of bill cycles to be processed for on-time payment.
     */
    public Collection<BillCycle> getBillCycles()
    {
        return billCycles_;
    }

}
