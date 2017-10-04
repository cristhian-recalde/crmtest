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
package com.trilogy.app.crm.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportHome;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordHome;
import com.trilogy.app.crm.dunning.DunningReportRecordMatureStateEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportStatusEnum;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.home.BeanNotFoundHomeException;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Home responsible for updating dunning report records on dunning report modification.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportRecordsUpdateHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 3325025261923013565L;


    /**
     * Create a new DunningReportRecordsUpdateHome object.
     * 
     * @param ctx
     * @param delegate
     */
    public DunningReportRecordsUpdateHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    public Object store(final Context ctx, final Object obj) throws HomeException
    {

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Store : DRRUH");
        }

        final DunningReport dunningReport = (DunningReport) obj;
        boolean movingToProcessed = false, storeReport = false, createReport = false;
        DunningReport oldDunningReport = null;

        // DUNNING REPORT status update PENDING --> APPROVED and reverse APPROVED --> PENDING

        switch (dunningReport.getStatus())
        {
            case DunningReportStatusEnum.PENDING_INDEX:

                // The approval on DunningReportRecord(DRR) can only be done when DR is in PendingState
                int approvalCount = storeDunningReportRecordsPendingToApprove(ctx, dunningReport, true);

                // In this case we need to reduce count from current DR and update or create new DR for same date and
                // other property fields

                if (approvalCount > 0)
                {
                    dunningReport.setNumberOfRecords(dunningReport.getNumberOfRecords() - approvalCount);
                    // create APPROVED or update approved cnt

                    oldDunningReport = findOldDunningReport(ctx, dunningReport.getSpid(),
                            dunningReport.getReportDate(), dunningReport.getNextLevel(),
                            DunningReportStatusEnum.APPROVED_INDEX);

                    if (oldDunningReport != null)
                    {
                        oldDunningReport.setNumberOfRecords(oldDunningReport.getNumberOfRecords() + approvalCount);
                        storeReport = true;
                    }
                    else
                    {
                        oldDunningReport = new DunningReport();
                        oldDunningReport.setStatus(DunningReportStatusEnum.APPROVED_INDEX);
                        oldDunningReport.setFailedToProcessRecords(0);
                        oldDunningReport.setNextLevel(dunningReport.getNextLevel());
                        oldDunningReport.setNumberOfRecords(approvalCount);
                        oldDunningReport.setReportDate(dunningReport.getReportDate());
                        oldDunningReport.setSpid(dunningReport.getSpid());
                        oldDunningReport.setSuccessfullyProcessedRecords(0);
                        oldDunningReport.setUpToDate(dunningReport.getUpToDate());
                        createReport = true;
                    }
                }
                break;

            case DunningReportStatusEnum.APPROVED_INDEX:

                // The approval on DunningReportRecord(DRR) can only be done when DR is in PendingState
                approvalCount = storeDunningReportRecordsPendingToApprove(ctx, dunningReport, false);

                // In this case we need to reduce count from current DR and update or create new DR for same date and
                // other property fields

                if (approvalCount > 0)
                {

                    dunningReport.setNumberOfRecords(dunningReport.getNumberOfRecords() - approvalCount);

                    // create PENDING or add PENDING cnt

                    oldDunningReport = findOldDunningReport(ctx, dunningReport.getSpid(),
                            dunningReport.getReportDate(), dunningReport.getNextLevel(),
                            DunningReportStatusEnum.PENDING_INDEX);
                    if (oldDunningReport != null)
                    {
                        oldDunningReport.setNumberOfRecords(oldDunningReport.getNumberOfRecords() + approvalCount);
                        storeReport = true;
                    }
                    else
                    {
                        oldDunningReport = new DunningReport();
                        oldDunningReport.setStatus(DunningReportStatusEnum.PENDING_INDEX);
                        oldDunningReport.setFailedToProcessRecords(0);
                        oldDunningReport.setNextLevel(dunningReport.getNextLevel());
                        oldDunningReport.setNumberOfRecords(approvalCount);
                        oldDunningReport.setReportDate(dunningReport.getReportDate());
                        oldDunningReport.setSpid(dunningReport.getSpid());
                        oldDunningReport.setSuccessfullyProcessedRecords(0);
                        oldDunningReport.setUpToDate(dunningReport.getUpToDate());
                        createReport = true;
                    }
                }

                break;

            case DunningReportStatusEnum.PROCESSED_INDEX:

                DunningReport oldReport = HomeSupportHelper.get(ctx).findBean(
                        ctx,
                        DunningReport.class,
                        new And()
                                .add(new EQDay(DunningReportXInfo.REPORT_DATE, dunningReport.getReportDate()))
                                .add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(dunningReport.getSpid())))
                                .add(new EQ(DunningReportXInfo.NEXT_LEVEL,
                                        Integer.valueOf(dunningReport.getNextLevel())))
                                .add(new EQ(DunningReportXInfo.STATUS, DunningReportStatusEnum.PROCESSED_INDEX)));

                if (oldReport != null)
                {
                    movingToProcessed = true;
                }
                break;
        }

        Object result = super.store(ctx, dunningReport);
        
        DunningReport storedDunningReport = (DunningReport) result;
        if(storedDunningReport.getNumberOfRecords() == 0)
        {
            Home home = (Home) ctx
                    .get(DunningReportHome.class);
            home.remove(storedDunningReport);
        }

        if (oldDunningReport != null)
        {
            Home home = (Home) ctx.get(DunningReportHome.class);
            if (storeReport)
            {
                home.store(ctx, oldDunningReport);
            }
            else if (createReport)
            {
                home.create(ctx, oldDunningReport);
            }
        }

        if (movingToProcessed)
        {
            moveFailedRecords(ctx, dunningReport);
        }

        return result;
    }


    private DunningReport findOldDunningReport(Context ctx, int spid, Date reportDate, int nextLevel, int status)
            throws HomeException
    {

        DunningReport oldReport = HomeSupportHelper.get(ctx).findBean(
                ctx,
                DunningReport.class,
                new And().add(new EQDay(DunningReportXInfo.REPORT_DATE, reportDate))
                        .add(new EQ(DunningReportXInfo.SPID, spid))
                        .add(new EQ(DunningReportXInfo.NEXT_LEVEL, nextLevel))
                        .add(new EQ(DunningReportXInfo.STATUS, status)));
        return oldReport;

    }


    /***
     * @param ctx
     * @param dunningReport
     * @param forward
     * @return
     * @throws HomeException
     */

    private int storeDunningReportRecordsPendingToApprove(final Context ctx, final DunningReport dunningReport,
            boolean forward) throws HomeException
    {
        Home home = (Home) ctx.get(DunningReportRecordHome.class);
        int approvalCount = 0;

        for (DunningReportRecord dunningReportRecord : dunningReport.getRecords(null))
        {
            try
            {
                LogSupport.debug(ctx, this,
                        "--------------------- Storing records : " + dunningReportRecord.isMoveToPTP() + " approve : "
                                + dunningReportRecord.getRecordMaturity());

                And where = new And();
                where.add(new EQ(DunningReportRecordXInfo.SPID, dunningReport.getSpid()));
                where.add(new EQ(DunningReportRecordXInfo.REPORT_DATE, dunningReport.getReportDate()));
                where.add(new EQ(DunningReportRecordXInfo.BAN, dunningReportRecord.getBAN()));

                DunningReportRecord oldDunningReportRecord = HomeSupportHelper.get(ctx).findBean(ctx,
                        DunningReportRecord.class, where);

                if (oldDunningReportRecord != null)
                {
                    // going from approve to unapprove
                    if (!forward
                            && (oldDunningReportRecord.getRecordMaturity() == DunningReportRecordMatureStateEnum.APPROVED_INDEX && dunningReportRecord
                                    .getRecordMaturity() == DunningReportRecordMatureStateEnum.PENDING_INDEX))
                    {
                        approvalCount += 1;
                        // dunningReport.increaseNumberOfRecords();
                    }
                    // goint from unapprove to approve
                    else if (forward
                            && (oldDunningReportRecord.getRecordMaturity() == DunningReportRecordMatureStateEnum.PENDING_INDEX && dunningReportRecord
                                    .getRecordMaturity() == DunningReportRecordMatureStateEnum.APPROVED_INDEX))
                    {
                        approvalCount = approvalCount + 1;
                    }
                }

                dunningReportRecord.setReportDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
                        dunningReport.getReportDate()));
                home.store(ctx, dunningReportRecord);
            }
            catch (BeanNotFoundHomeException exception)
            {
                LogSupport.major(ctx, this, "Exception occured and can be ignored : ", exception);
                // Record has been removed. Ignored.
            }
        }

        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Storing DRR : FORWARD DIRECTION : " + forward + " : count : " + approvalCount);
        }
        return approvalCount;
    }


    /**
     * Move failed records to the next report.
     * 
     * @param ctx
     * @param report
     * @throws HomeException
     */
    private void moveFailedRecords(final Context ctx, final DunningReport report) throws HomeException
    {
        DunningReport nextReport = findNextReport(ctx, report);

        Home home = (Home) ctx.get(DunningReportRecordHome.class);

        if (nextReport != null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Moving failed records from Dunning Report on date '");
                sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                sb.append("' to Dunning Report on date '");
                sb.append(CoreERLogger.formatERDateDayOnly(nextReport.getReportDate()));
                sb.append("'");
                LogSupport.debug(ctx, this, sb.toString());
            }

            boolean needsUpdate = false;

            for (DunningReportRecord record : report.getRecords(ctx))
            {
                if (record.getStatus() == DunningReportRecordStatusEnum.FAILED_INDEX)
                {
                    record.setReportDate(nextReport.getReportDate());
                    record.setStatus(DunningReportRecordStatusEnum.PENDING_INDEX);
                    record.setAdditionalInfo(null);
                    try
                    {
                        home.create(ctx, record);
                        nextReport.increaseNumberOfRecords();
                        needsUpdate = true;
                    }
                    catch (HomeException e)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Unable to copy failed record for account '");
                        sb.append(record.getBAN());
                        sb.append("' from Dunning Report processed on date '");
                        sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                        sb.append("' to the next Dunning Report on date '");
                        sb.append(CoreERLogger.formatERDateDayOnly(nextReport.getReportDate()));
                        sb.append("'");
                        LogSupport.minor(ctx, this, sb.toString(), e);
                    }
                }
            }

            if (needsUpdate)
            {
                try
                {
                    if (nextReport.getStatus() != DunningReportStatusEnum.PROCESSING_INDEX
                            && nextReport.getStatus() != DunningReportStatusEnum.ACCEPTED_INDEX)
                    {
                        nextReport.setUpToDate(false);
                    }
                    this.store(ctx, nextReport);
                }
                catch (HomeException e)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unable to update number of records for Dunning Report for date '");
                    sb.append(CoreERLogger.formatERDateDayOnly(nextReport.getReportDate()));
                    sb.append("'");
                    LogSupport.minor(ctx, this, sb.toString(), e);
                }
            }
        }

        // TODO
        // if next report is null we might have to create one and assign failed DRR to it
    }


    /**
     * Find next report.
     * 
     * @param ctx
     * @param report
     * @return
     * @throws HomeException
     */
    private DunningReport findNextReport(final Context ctx, final DunningReport report) throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Finding next report after date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("'");
            LogSupport.debug(ctx, this, sb.toString());
        }

        DunningReport result = null;
        Collection<DunningReport> nextReports = HomeSupportHelper.get(ctx).getBeans(
                ctx,
                DunningReport.class,
                new And()
                        .add(new GT(DunningReportXInfo.REPORT_DATE, CalendarSupportHelper.get(ctx)
                                .getDateWithLastSecondofDay(report.getReportDate())))
                        .add(new EQ(DunningReportXInfo.SPID, Integer.valueOf(report.getSpid())))
                        .add(new EQ(DunningReportXInfo.NEXT_LEVEL, Integer.valueOf(report.getNextLevel())))
                        .add(new EQ(DunningReportXInfo.STATUS, report.getStatus())), 1, true,
                DunningReportXInfo.REPORT_DATE);

        if (nextReports != null && nextReports.size() > 0)
        {
            result = nextReports.iterator().next();
        }
        return result;
    }


    /**
     * {@inheritDoc}
     */
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final DunningReport report = (DunningReport) obj;

        Home recordsHome = (Home) ctx.get(DunningReportRecordHome.class);
        List<DunningReportRecord> newRecords = new ArrayList<DunningReportRecord>();
        DunningReport nextReport = null;

        // Moving records in the old record to an existing record in another date.
        if (DunningReportStatusEnum.PROCESSED_INDEX != report.getStatus() && report.getRecords(ctx).size() > 0)
        {
            nextReport = findNextReport(ctx, report);

            if (nextReport != null)
            {
                for (DunningReportRecord record : report.getRecords(ctx))
                {
                    record.setReportDate(nextReport.getReportDate());
                    newRecords.add(record);
                }
            }
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Removing all records from Dunning Report on date '");
            sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
            sb.append("' and SPID '");
            sb.append(report.getSpid());
            sb.append("'");
            LogSupport.debug(ctx, this, sb.toString());
        }
        
//        if(report.getStatus() == DunningReportStatusEnum.PENDING_INDEX || report.getStatus() == DunningReportStatusEnum.ACCEPTED_INDEX || report.getStatus() == DunningReportStatusEnum.APPROVED_INDEX)
//        {
//            recordsHome.where(
//             
//                ctx,
//                new And().add(new EQDay(DunningReportRecordXInfo.REPORT_DATE, report.getReportDate()))
//                         .add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(report.getSpid())))
//                         .add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, report.getNextLevel()))
//                         .add(new EQ(DunningReportRecordXInfo.RECORD_MATURITY, DunningReportRecordMatureStateEnum.getByName(DunningReportStatusEnum.get((short)report.getStatus()).getName()).getIndex()))
//                             ).removeAll(ctx);
//        }
//        else if(report.getStatus() == DunningReportStatusEnum.PROCESSED_INDEX)
//        {
//            recordsHome.where(
//                    
//                    ctx,
//                    new And().add(new EQDay(DunningReportRecordXInfo.REPORT_DATE, report.getReportDate()))
//                             .add(new EQ(DunningReportRecordXInfo.SPID, Integer.valueOf(report.getSpid())))
//                             .add(new EQ(DunningReportRecordXInfo.FORECASTED_LEVEL, report.getNextLevel()))
//                             .add(new EQ(DunningReportRecordXInfo.STATUS, DunningReportRecordStatusEnum.PROCESSED_INDEX))
//                                 ).removeAll(ctx);
//        }
//        
        if(nextReport!=null)
        {
            for (DunningReportRecord record : newRecords)
            {
                try
                {
                    // Recreating records.
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Copying record for account '");
                        sb.append(record.getBAN());
                        sb.append("' from removed Dunning Report on date '");
                        sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                        sb.append("' to dunning report on date'");
                        sb.append(CoreERLogger.formatERDateDayOnly(record.getReportDate()));
                        sb.append("'");
                        LogSupport.debug(ctx, this, sb.toString());
                    }
                    recordsHome.create(ctx, record);
                    nextReport.increaseNumberOfRecords();
                }
                catch (HomeException e)
                {
                    if (!HomeSupportHelper.get(ctx).hasBeans(
                            ctx,
                            DunningReportRecord.class,
                            new And().add(new EQDay(DunningReportRecordXInfo.REPORT_DATE, record.getReportDate())).add(
                                    new EQ(DunningReportRecordXInfo.BAN, record.getBAN()))))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Unable to copy record for account '");
                        sb.append(record.getBAN());
                        sb.append("' from removed Dunning Report on date '");
                        sb.append(CoreERLogger.formatERDateDayOnly(report.getReportDate()));
                        sb.append("' to dunning report on date'");
                        sb.append(CoreERLogger.formatERDateDayOnly(record.getReportDate()));
                        sb.append("'");
                        LogSupport.minor(ctx, this, sb.toString(), e);
                    }
                }
            }
        }
        
        if (nextReport !=null && newRecords.size() > 0)
        {
            try
            {
                if (nextReport.getStatus() != DunningReportStatusEnum.PROCESSING_INDEX
                        && nextReport.getStatus() != DunningReportStatusEnum.ACCEPTED_INDEX)
                {
                    nextReport.setUpToDate(false);
                }

                this.store(ctx, nextReport);
            }
            catch (HomeException e)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to update number of records for Dunning Report for date '");
                sb.append(CoreERLogger.formatERDateDayOnly(nextReport.getReportDate()));
                sb.append("'");
                LogSupport.minor(ctx, this, sb.toString(), e);
            }
        }

        super.remove(ctx, report);
    }

    // /**
    // * Store records
    // *
    // * @param ctx
    // * @param dunningReport
    // * @throws HomeException
    // */
    // private int[] storeDunningReportRecords(final Context ctx, final DunningReport dunningReport) throws
    // HomeException
    // {
    // Home home = (Home) ctx.get(DunningReportRecordHome.class);
    // int approvalCount = 0, approvalCountReversal = 0;
    //
    // for (DunningReportRecord dunningReportRecord : dunningReport.getRecords(null))
    // {
    // try
    // {
    // LogSupport.debug(ctx, this,
    // "--------------------- Storing records : " + dunningReportRecord.isMoveToPTP() + " approve : "
    // + (dunningReportRecord.getRecordMaturity() == DunningReportRecordMatureStateEnum.APPROVED_INDEX));
    //
    // And where = new And();
    // where.add(new EQ(DunningReportRecordXInfo.STATUS, dunningReportRecord.getStatus()));
    // where.add(new EQ(DunningReportRecordXInfo.REPORT_DATE, dunningReportRecord.getReportDate()));
    // where.add(new EQ(DunningReportRecordXInfo.BAN, dunningReportRecord.getBAN()));
    //
    // DunningReportRecord oldDunningReportRecord = HomeSupportHelper.get(ctx).findBean(ctx,
    // DunningReportRecord.class, where);
    //
    // if (oldDunningReportRecord != null)
    // {
    // // going from approve to unapprove
    // if (oldDunningReportRecord.isApprove() && !dunningReportRecord.isApprove())
    // {
    // approvalCountReversal += 1;
    // // dunningReport.increaseNumberOfRecords();
    // }
    // // goint from unapprove to approve
    // else if (!oldDunningReportRecord.isApprove() && dunningReportRecord.isApprove())
    // {
    // approvalCount += 1;
    // }
    // }
    //
    // dunningReportRecord.setReportDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(
    // dunningReport.getReportDate()));
    // home.store(ctx, dunningReportRecord);
    // }
    // catch (BeanNotFoundHomeException exception)
    // {
    // LogSupport.major(ctx, this, "Exception occured and can be ignored : ", exception);
    // // Record has been removed. Ignored.
    // }
    // }
    //
    // return new int[] { approvalCount, approvalCountReversal };
    // }

}