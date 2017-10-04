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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.RecurringChargeErrorReport;
import com.trilogy.app.crm.bean.RecurringChargeErrorReportHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;


/**
 * Support class for RechargeErrorReport.
 *
 * @author larry.xia@redknee.com
 */
public final class RechargeErrorReportSupport
{

    /**
     * Creates a new <code>RechargeErrorReportSupport</code> instance. This method is
     * made private to prevent instantiation of utility class.
     */
    private RechargeErrorReportSupport()
    {
        // empty
    }


    /**
     * Create recurring charge report item.
     *
     * @param ctx
     *            The operating context.
     * @param resultCode
     *            Recurring charge result code.
     * @param ocgResultCode
     *            OCG result code.
     * @param reason
     *            Detailed report message.
     * @param chargedItemId
     *            Charged item ID.
     * @param ban
     *            Account BAN.
     * @param spid
     *            Service provider ID.
     * @param msisdn
     *            MSISDN.
     * @param subscriberID
     *            Subscriber ID.
     * @param billingDate
     *            Billing date.
     * @param chargedItemType
     *            Type of charge item.
     * @return Created report item.
     * @throws HomeException
     *             Thrown if there are problems assigning a unique ID to the recurring
     *             charge item.
     */
    public static RecurringChargeErrorReport generateReport(final Context ctx, final int resultCode,
        final int ocgResultCode, final String reason, final long chargedItemId, final String ban, final String responsibleBAN, final int spid,
        final String msisdn, final String subscriberID, final Date billingDate,
        final ChargedItemTypeEnum chargedItemType) throws HomeException
    {

        final RecurringChargeErrorReport report = new RecurringChargeErrorReport();

        report.setReceiptNum(CoreTransactionSupportHelper.get(ctx).getNextIdentifier(ctx));
        report.setReceiveDate(new Date());
        report.setTransDate(billingDate);
        report.setSpid(spid);
        report.setBAN(ban);
        report.setSubscriberID(subscriberID);
        report.setMSISDN(msisdn);
        report.setChargedItemType(chargedItemType);
        report.setResponsibleBAN(responsibleBAN);

        return report;
    }


    /**
     * Create recurring charge report item.
     *
     * @param ctx
     *            The operating context.
     * @param agentName
     *            The name of the agent creating this recurring charge report item.
     * @param trans
     *            Transaction.
     * @param resultCode
     *            Recurring charge result code.
     * @param ocgResultCode
     *            OCG result code.
     * @param reason
     *            Detailed report message.
     * @param chargedItemId
     *            Charged item ID.
     * @param ban
     *            Account BAN.
     * @param spid
     *            Service provider ID.
     * @param msisdn
     *            MSISDN.
     * @param subscriberID
     *            Subscriber ID.
     * @param billingDate
     *            Billing date.
     * @param chargedItemType
     *            Type of charge item.
     * @throws HomeException
     *             Thrown if there are problems creating the report.
     */
    public static void createReport(final Context ctx, final String agentName, final Transaction trans,
        final int resultCode, final int ocgResultCode, final String reason, final long chargedItemId, final String ban,
        final String responsibleBAN, final int spid, final String msisdn, final String subscriberID, final Date billingDate,
        final ChargedItemTypeEnum chargedItemType) throws HomeException
    {

        final RecurringChargeErrorReport report;

        if (trans != null)
        {
            report = RechargeReportTransactionAdapter.toReport(trans);
            report.setReceiptNum(CoreTransactionSupportHelper.get(ctx).getNextIdentifier(ctx));
        }
        else
        {
            report = generateReport(ctx, resultCode, ocgResultCode, reason, chargedItemId, ban, responsibleBAN, spid, msisdn,
                subscriberID, billingDate, chargedItemType);
        }

        report.setAgent(agentName);
        report.setResultCode(resultCode);
        report.setOcgResultCode(ocgResultCode);
        report.setReason(reason);
        report.setChargedItemId(chargedItemId);
        report.setChargedItemType(chargedItemType);

        final Home home = (Home) ctx.get(RecurringChargeErrorReportHome.class);
        home.create(report);
    }
    

    public static void createReport(final Context ctx, final String agentName, final int resultCode,
            final int ocgResultCode, final String reason, final long chargedItemId, final String ban,
            final String responsibleBAN, final int spid, final String msisdn, final String subscriberID,
            final Date billingDate, final ChargedItemTypeEnum chargedItemType, final long amount) throws HomeException
    {
        final RecurringChargeErrorReport report;
        report = generateReport(ctx, resultCode, ocgResultCode, reason, chargedItemId, ban, responsibleBAN, spid,
                msisdn, subscriberID, billingDate, chargedItemType);
        report.setAmount(amount);
        report.setAgent(agentName);
        report.setResultCode(resultCode);
        report.setOcgResultCode(ocgResultCode);
        report.setReason(reason);
        report.setChargedItemId(chargedItemId);
        report.setChargedItemType(chargedItemType);
        final Home home = (Home) ctx.get(RecurringChargeErrorReportHome.class);
        home.create(report);
    }
}
