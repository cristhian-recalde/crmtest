/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor.reportgeneration;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportRecordAccountVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;


/**
 * Visitor responsible to process accounts during dunning report generation.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportRecordGenerationAccountVisitor extends AbstractDunningReportRecordAccountVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningReportRecordGenerationAccountVisitor visitor.
     * 
     * @param report
     */
    public DunningReportRecordGenerationAccountVisitor(final DunningReport report)
    {
        super(report);
    }


    /**
     * Retrieves the process name.
     * 
     * @return
     */
    public static String getVisitorProcessName()
    {
        return "Dunning Report Generation";
    }


    
    public String getProcessName()
    {
        return getVisitorProcessName();
    }


    
    protected void executeOnActionRequired(final Context context, final Account account,
             final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
        
        try
        {
            DunningReportRecord futureRecord = retrieveRecordFromReportInTheFuture(context, account.getBAN(),
                    dunningReportRecord.getForecastedLevel());
            DunningReportRecord acceptedRecord = retrieveRecordFromAcceptedReport(context, account.getBAN(),
                    dunningReportRecord.getForecastedLevel());
            if (acceptedRecord != null)
            {
                if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Dunning report record for '");
                    sb.append(account.getBAN());
                    sb.append("' already scheduled for '");
                    sb.append(CoreERLogger.formatERDateDayOnly(acceptedRecord.getReportDate()));
                    sb.append("'");
                    LogSupport.info(context, this, sb.toString());
                }
            }
            else if (futureRecord == null)
            {
                if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Creating dunning report record for '");
                    sb.append(account.getBAN());
                    sb.append("' on report for date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                    sb.append("'");
                    LogSupport.info(context, this, sb.toString());
                }
                HomeSupportHelper.get(context).createBean(context, dunningReportRecord);
            }
            else 
            {
                if (LogSupport.isEnabled(context, SeverityEnum.INFO))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Moving dunning report record for '");
                    sb.append(account.getBAN());
                    sb.append("' from report on date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(futureRecord.getReportDate()));
                    sb.append("' to report on date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                    sb.append("'");
                    LogSupport.info(context, this, sb.toString());
                }
                dunningReportRecord.setMoveToPTP(futureRecord.getMoveToPTP());
                dunningReportRecord.setPtpExpiryDate(futureRecord.getPtpExpiryDate());
                dunningReportRecord.setStatus(DunningReportRecordStatusEnum.PENDING_INDEX);
                HomeSupportHelper.get(context).removeBean(context, futureRecord);
                HomeSupportHelper.get(context).createBean(context, dunningReportRecord);
            }
            
        }
        catch (HomeException e)
        {
            String cause = "Unable to or update create dunning report record for account";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" '");
            sb.append(account.getBAN());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(context, this, sb.toString(), e);
            throw new DunningProcessException(cause, e);
        }
        
    }


    /**
     * Retrieves record from a report in the future with the same account and forecasted
     * state.
     * 
     * @param ctx
     * @param BAN
     * @param forecastedLevel
     * @return
     */
    private DunningReportRecord retrieveRecordFromReportInTheFuture(Context ctx, String BAN,
            int forecastedLevel)
    {
        DunningReportRecord result = null;
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Verifying if record for account '");
                sb.append(BAN);
                sb.append("' and forecastedLevel '");
                sb.append(forecastedLevel);
                sb.append("' exists in a report with date after '");
                sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                sb.append("'");
                LogSupport.debug(ctx, this, sb.toString());
            }
            And predicate = new And();
            predicate.add(new GT(DunningReportRecordXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                    .getDateWithLastSecondofDay(getRunningDate())));
            predicate.add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(getDunningReport().getSpid())));
            predicate.add(new EQ(DunningReportRecordXInfo.BAN, BAN));
            predicate.add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, forecastedLevel));
            result = HomeSupportHelper.get(ctx).findBean(ctx, DunningReportRecord.class, predicate);
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to retrieve record in a future dunning report for account '");
            sb.append(BAN);
            sb.append("' and forecasted state '");
            sb.append(forecastedLevel);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
        }
        return result;
    }


    /**
     * Retrieves a record from a scheduled report with the same forecasted state.
     * 
     * @param ctx
     * @param BAN
     * @param forecastedState
     * @return
     */
    private DunningReportRecord retrieveRecordFromAcceptedReport(Context ctx, String BAN,
            int forecastedState)
    {
        DunningReportRecord result = null;
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Verifying if record for account '");
                sb.append(BAN);
                sb.append("' and forecastedState '");
                sb.append(forecastedState);
                sb.append("' exists in an accepted report report with date before '");
                sb.append(CoreERLogger.formatERDateDayOnly(getRunningDate()));
                sb.append("'");
                LogSupport.debug(ctx, this, sb.toString());
            }
            And predicate = new And();
            predicate.add(new LT(DunningReportRecordXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                    .getDateWithNoTimeOfDay(getRunningDate())));
            predicate.add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(getDunningReport().getSpid())));
            predicate.add(new EQ(DunningReportRecordXInfo.BAN, BAN));
            predicate.add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, forecastedState));
            predicate.add(new Or().add(new EQ(DunningReportRecordXInfo.STATUS, Integer.valueOf(DunningReportRecordStatusEnum.PROCESSING_INDEX)))
                    .add(new EQ(DunningReportRecordXInfo.STATUS, Integer.valueOf(DunningReportRecordStatusEnum.NO_ACTION_INDEX)))
                    .add(new EQ(DunningReportRecordXInfo.STATUS, Integer.valueOf(DunningReportRecordStatusEnum.PENDING_INDEX))));
            Collection<DunningReportRecord> records = HomeSupportHelper.get(ctx).getBeans(ctx, DunningReportRecord.class, predicate, 1, false, DunningReportRecordXInfo.REPORT_DATE);
            if (records.size()>0)
            {
                result = records.iterator().next();
            }
            if (result != null)
            {
                And reportPredicate = new And();
                reportPredicate.add(new EQDay(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                        .getDateWithNoTimeOfDay(result.getReportDate())));
                reportPredicate.add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(getDunningReport().getSpid())));
                reportPredicate.add(new Or().add(
                        new EQ(DunningReportXInfo.STATUS, Integer.valueOf(DunningReportStatusEnum.ACCEPTED_INDEX)))
                        .add(
                                new EQ(DunningReportXInfo.STATUS, Integer
                                        .valueOf(DunningReportStatusEnum.PROCESSING_INDEX))));
                if (HomeSupportHelper.get(ctx).findBean(ctx, DunningReport.class, reportPredicate) == null)
                {
                    result = null;
                }
            }
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unable to retrieve record in a future dunning report for account '");
            sb.append(BAN);
            sb.append("' and forecasted state '");
            sb.append(forecastedState);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.minor(ctx, this, sb.toString(), e);
        }
        return result;
    }


    /**
     * Verifies if accounts forecasted to be activated should be automatically processed.
     * 
     * @param ctx
     * @return
     */
    private boolean automaticallyProcessForecastedToActive(Context ctx)
    {
        return (LicensingSupportHelper.get(ctx).isLicensed(ctx,
                LicenseConstants.DUNNING_REPORT_AUTOMATIC_EXEMPT_ACCOUNTS_PROCESSING));
    }
}
