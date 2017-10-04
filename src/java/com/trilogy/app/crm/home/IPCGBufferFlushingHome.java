/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.Max;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.IPCGDataXDBHome;
import com.trilogy.app.crm.bean.IPCGDataXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.poller.IPCGCallDetailCreator;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;


/**
 * This class flushes all the "last-day" records that are stored in the buffer to the
 * CallDetail table.
 *
 * @author jimmy.ng@redknee.com
 * @author cindy.wong@redknee.com
 */
public class IPCGBufferFlushingHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Success code for ER log.
     */
    private static final int SUCCESSFUL = 0;
    /**
     * General error code for ER log.
     */
    private static final int GENERAL_ERROR = 9999;
    /**
     * IPCG aggregation ER ID.
     */
    private static final int IPCG_AGGREGATION_ER_ID = 786;
    /**
     * IPCG aggregation ER class.
     */
    private static final int IPCG_AGGREGATION_ER_CLASS = 700;
    /**
     * IPCG aggregation ER name.
     */
    private static final String IPCG_AGGREGATION_ER_NAME = "IPCG ER Aggregation Event";

    /**
     * Should not be bigger than Oracle IN size restriction which is currently 1000.
     */
    public static int REMOVE_DB_TRANSACTION_SIZE = 1000;

    /**
     * Visitor for IPCGData aggregation.
     *
     * @author cindy.wong@redknee.com
     * @since 2008-07-04
     */
    public static class IPCGDataAggregationVisitor implements Visitor
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Map of IPCGData for aggregation. The key of this map is a string in the format
         * of "[subscriber MSISDN] [date] [call type] [rate unit]".
         */
        private final Map<String, IPCGData> aggregatedMap_ = new HashMap<String, IPCGData>();

        /**
         * Map of counters for aggregation. The key of this map is in the format of
         * "[subscriber MSISDN] [date] [call type] [rate unit]", and the value is the set
         * of ID of IPCGData aggregated.
         */
        private final Map<String, Set<Long>> bufferMap_ = new HashMap<String, Set<Long>>();


        /**
         * {@inheritDoc}
         */
        public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
        {
            final IPCGData data = (IPCGData) obj;

            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            final StringBuilder sb = new StringBuilder();
            sb.append(data.getChargedMSISDN());
            sb.append(' ');
            sb.append(dateFormat.format(data.getTranDate()));
            sb.append(' ');
            sb.append(data.getCallType());
            sb.append(' ');
            sb.append(data.getUnitType());
            final String key = sb.toString();

            IPCGData aggregated = this.aggregatedMap_.get(key);
            if (aggregated == null)
            {
                aggregated = (IPCGData) FrameworkSupportHelper.get(ctx).doClone(data);
            }
            else
            {
                if (data.getTranDate().before(aggregated.getTranDate()))
                {
                    aggregated.setTranDate(data.getTranDate());
                }
                aggregated.setUsage(aggregated.getUsage() + data.getUsage());
                aggregated.setCharge(aggregated.getCharge() + data.getCharge());
            }
            this.aggregatedMap_.put(key, aggregated);

            Set<Long> idSet = this.bufferMap_.get(key);
            if (idSet == null)
            {
                idSet = new HashSet<Long>();
            }
            idSet.add(Long.valueOf(data.getIPCGDataID()));
            this.bufferMap_.put(key, idSet);
        }


        /**
         * Retrieves the value of <code>aggregatedMap</code>.
         *
         * @return The value of <code>aggregatedMap</code>.
         */
        public final Map<String, IPCGData> getAggregatedMap()
        {
            return this.aggregatedMap_;
        }


        /**
         * Retrieves the value of <code>bufferMap</code>.
         *
         * @return The value of <code>bufferMap</code>.
         */
        public final Map<String, Set<Long>> getBufferMap()
        {
            return this.bufferMap_;
        }
    }


    /**
     * Creates a new IPCGBufferFlushingHome.
     *
     * @param delegate
     *            The home to delegate to.
     * @param ctx
     *            The operating context.
     */
    public IPCGBufferFlushingHome(final Home delegate, final Context ctx)
    {
        super(delegate);
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof IPCGData)
        {
            final IPCGData data = (IPCGData) obj;
            final Date date = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(data.getTranDate());
            Object ret = null;
            boolean directToCallDetails = false;
            boolean flushBuffer = false;

            synchronized (this)
            {
                /*
                 * Need to make sure that in the case that we run the aggregation logic
                 * for the first time that we insert all out of order records into the
                 * CallDetails table directly. We have to synchronize here as we have to
                 * make sure that the XIPCGDATA record for the next date is put into the
                 * table before subsequent threads try and find the max transaction date
                 * from the table. The amount of time in this synchronized block is
                 * generally just the time to query for the maxDate and do the comparisons
                 * but in the worse case it also includes the time for the creation of the
                 * record into the XIPCGDATA table.
                 */
                Date maxDate = getMaxTransactionDate(ctx);
                if (maxDate != null)
                {
                    if (date.before(maxDate))
                    {
                        // go to Call Details directly after flushing is done, or while it is being done.
                        directToCallDetails = true;
                    }
                    else if (date.after(maxDate))
                    {
                        // Encountered the first ER for a new day, so we will need to flush the buffer.
                        ret = super.create(ctx, obj);
                        setMaxTransactionDate(date);
                        flushBuffer = true;
                    }
                    // else the record is for the current day so it will simply go into the XIPCGDATA table.

                }
                else
                {
                    // There are no records, so the maxDate is null. Remember the date of this record as the maxDate.
                    setMaxTransactionDate(date);
                }
            }

            if (directToCallDetails)
            {
                try
                {
                    createCDRWithAggregationLogging(ctx, data, 1);
                }
                catch (final HomeException exception)
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Encountered HomeException while trying to call createCDRWithAggregationLogging() ");
                    sb.append("during a direct create for the CallDetail [chargedMsisdn=");
                    sb.append(data.getChargedMSISDN());
                    sb.append(",transDate=");
                    sb.append(data.getTranDate());
                    sb.append(",callType=");
                    sb.append(data.getCallType());
                    sb.append("]");

                    LogSupport.minor(ctx, this, sb.toString(), exception);
                }

                // No need to create the IPCGData object
                return null;
            }
            else if (flushBuffer)
            {
                // Flush the "old" records in the buffer.
                flushBuffer(ctx, date);
                return ret;
            }
        }

        return super.create(ctx, obj);
    }


    /**
     * This method flushes those records (older than the given cut-off date) in the
     * buffer.
     *
     * @param ctx
     *            The operating context.
     * @param cutoffDate
     *            The given cut-off date.
     * @throws HomeException
     *             Thrown if there are problems creating one or more call details.
     */
    public void flushBuffer(final Context ctx, final Date cutoffDate) throws HomeException
    {
        final PMLogMsg flushPm = new PMLogMsg("IPCGBufferFlushingHome", "flushBuffer", "cutoffDate="
            + cutoffDate.toString());
        try
        {
            // Retrieve info from all records that are going to be flushed.
            final Home home = (Home) ctx.get(IPCGDataXDBHome.class);
            final IPCGDataAggregationVisitor visitor = new IPCGDataAggregationVisitor();

            home.forEach(ctx, visitor, new LT(IPCGDataXInfo.TRAN_DATE, cutoffDate));

            for (final String key : visitor.getAggregatedMap().keySet())
            {
                final IPCGData data = visitor.getAggregatedMap().get(key);
                final Set<Long> bufferIds = visitor.getBufferMap().get(key);

                try
                {
                    createCDRWithAggregationLogging(ctx, data, bufferIds.size());
                }
                catch (final Exception exception)
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Encountered ");
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" while trying to call createCDRWithAggregationLogging() ");
                    sb.append("for flushing the buffer [");
                    sb.append(key);
                    sb.append("]");
                    LogSupport.minor(ctx, this, sb.toString(), exception);
                    continue;
                }

                try
                {
                    removeRecords(ctx, home, bufferIds);
                }
                catch (final Exception exception)
                {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Encountered ");
                    sb.append(exception.getClass().getSimpleName());
                    sb.append(" during the removal of records for [");
                    sb.append(key);
                    sb.append("]");
                    LogSupport.minor(ctx, this, sb.toString(), exception);
                    continue;
                }
            }
        }
        finally
        {
            flushPm.log(ctx);
        }
    }


    /**
     * Removal of records in a staged manner to avoid following issues:
     * - DB transaction size too big and does not fit in the rollback log
     * - in an event of a DB error the error returned by the DB may not fit in the XDBError.lastError field
     *   which will case the loss of the root cause error. See TT#9120803034
     * - Other problems in Oracle JDBC driver and Framework made it impossible to use OracleIn ELang while removing
     *   lots of records. See TT#9121625042
     *
     * @param ctx
     * @param home
     * @param bufferIds
     * @throws HomeException 
     */
    protected void removeRecords(final Context ctx, final Home home, final Set<Long> bufferIds) throws HomeException
    {
        Set<Long> leftIDs = new HashSet<Long>(bufferIds);
        final Iterator<Long> leftIterator = leftIDs.iterator();
        while (leftIDs.size() > 0)
        {
            final Set<Long> worksetIDs;
            if (leftIDs.size() < REMOVE_DB_TRANSACTION_SIZE)
            {
                worksetIDs = leftIDs;
                leftIDs = new HashSet<Long>();
            }
            else
            {
                worksetIDs = new HashSet<Long>(REMOVE_DB_TRANSACTION_SIZE);
                for (int count = 0; count < REMOVE_DB_TRANSACTION_SIZE; count++)
                {
                    worksetIDs.add(leftIterator.next());
                    leftIterator.remove();
                }
            }
            home.removeAll(ctx, new In(IPCGDataXInfo.IPCGDATA_ID, worksetIDs));
        }
    }
    


    /**
     * This method acts as a wrapper to the createCallDetailRecord() method. Its task is
     * to generate proper aggregation OMs and ERs.
     *
     * @param ctx
     *            The operating context.
     * @param data
     *            The given IPCGData object.
     * @param numEr
     *            Number of ERs aggregated.
     * @throws HomeException
     *             Thrown if there are problems creating the call detail.
     */
    protected void createCDRWithAggregationLogging(final Context ctx, final IPCGData data, final int numEr)
        throws HomeException
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_IPCG_AGGREGATION_ATTEMPT).log(ctx);

            CallDetail cd = IPCGCallDetailCreator.instance().createCallDetail(ctx, data);

            final Home home = (Home) ctx.get(CallDetailHome.class);
            if (home == null)
            {
                throw new HomeException("CallDetailHome not found in context");
            }
            cd = (CallDetail) home.create(ctx, cd);

            new OMLogMsg(Common.OM_MODULE, Common.OM_IPCG_AGGREGATION_SUCCESS).log(ctx);

            new ERLogMsg(IPCG_AGGREGATION_ER_ID, IPCG_AGGREGATION_ER_CLASS, IPCG_AGGREGATION_ER_NAME, cd.getSpid(),
                new String[]
                {
                    cd.getChargedMSISDN(), cd.getBAN(), String.valueOf(numEr), String.valueOf(cd.getCharge()),
                    String.valueOf(cd.getDataUsage()), cd.getCallType().getDescription(),
                    dateFormat.format(cd.getTranDate()), String.valueOf(SUCCESSFUL),
                }).log(ctx);
        }
        catch (final HomeException e)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_IPCG_AGGREGATION_FAIL).log(ctx);

            new ERLogMsg(IPCG_AGGREGATION_ER_ID, IPCG_AGGREGATION_ER_CLASS, IPCG_AGGREGATION_ER_NAME, 0, new String[]
            {
                data.getChargedMSISDN(), "", String.valueOf(numEr), String.valueOf(data.getCharge()),
                String.valueOf(data.getUsage()), data.getCallType().getDescription(),
                dateFormat.format(data.getTranDate()), String.valueOf(GENERAL_ERROR),
            }).log(ctx);

            throw e;
        }
    }


    /**
     * This method finds and returns the maximum transaction date among all the records in the IPCGData table.
     *
     * @param ctx The operating context.
     * @return Date The maximum transaction date found in the IPCGData table.
     * @throws HomeException
     *             Thrown if there are problems finding the date.
     */
    private Date getDBMaxTransactionDate(final Context ctx) throws HomeException
    {
        final Home home = (Home) ctx.get(IPCGDataXDBHome.class);
        final Max max = new Max(IPCGDataXInfo.TRAN_DATE, True.instance());
        final Object value = home.cmd(ctx, max);
        if (value != null)
        {
            return new Date(((BigDecimal) value).longValue());
        }
        return null;
    }

    /**
     * This method returns the current maximum transaction date that is maintained in this class to avoid DQ queries in
     * the synchronized block. The value is maintained with NoTimeOfDay for optimization.
     * 
     * @param ctx The operating context.
     * @return Date The maximum transaction date found in the IPCGData table.
     * @throws HomeException
     *             Thrown if there are problems finding the date.
     */
    private synchronized Date getMaxTransactionDate(final Context ctx) throws HomeException
    {
        if (currentMaxTranDate_ == null)
        {
            final Date dbMaxDate = getDBMaxTransactionDate(ctx);
            if (dbMaxDate != null)
            {
                setMaxTransactionDate(dbMaxDate);
            }
        }

        return currentMaxTranDate_;
    }

    /**
     * This method sets the current maximum transaction date that is maintained in this class to avoid DQ queries in
     * the synchronized block. The value is maintained with NoTimeOfDay for optimization.
     * 
     * @param newDate The maximum transaction date found in the IPCGData table.
     * @throws HomeException
     *             Thrown if there are problems finding the date.
     */
    private synchronized void setMaxTransactionDate(final Date newDate) throws HomeException
    {
        if (newDate != null)
        {
            currentMaxTranDate_ = CalendarSupportHelper.get().getDateWithNoTimeOfDay(newDate);
        }
        else
        {
            // this is useful to do debugging and problem recovery :)
            currentMaxTranDate_ = newDate;
        }
    }

    /**
     * Holds the current max for tran date.
     * This field is not static because there should be only one instance of this home decorator, and the
     * synchronization is on the instance object. Also having it non static make it easier to test.
     */
    private Date currentMaxTranDate_ = null;
}
