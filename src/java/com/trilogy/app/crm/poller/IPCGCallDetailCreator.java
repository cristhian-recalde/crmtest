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
package com.trilogy.app.crm.poller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.util.time.Time;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.bean.CallTypeID;
import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.IPCGDataXDBHome;
import com.trilogy.app.crm.bean.IPCGDataXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Creates the call details for IPCG.
 *
 * @author arturo.medina@redknee.com
 * @author cindy.wong@redknee.com
 */
public class IPCGCallDetailCreator implements CallDetailCreator, Constants
{

    /**
     * Create a new instance of <code>IPCGCallDetailCreator</code>.
     */
    protected IPCGCallDetailCreator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>IPCGCallDetailCreator</code>.
     *
     * @return An instance of <code>IPCGCallDetailCreator</code>.
     */
    public static IPCGCallDetailCreator instance()
    {
        if (instance == null)
        {
            instance = new IPCGCallDetailCreator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public CallDetail createCallDetail(final Context ctx, final ProcessorInfo info, final List<String> params)
        throws ParseException, HomeException, AgentException
    {
        /*
         * IPCG always returns a collection of call details so this method doesn't make
         * any sense to be implemented in the IPCG call detail creator.
         */

        return null;
    }


    /**
     * {@inheritDoc}
     */
    public List<CallDetail> createCallDetails(final Context ctx, final ProcessorInfo info, final List<String> params)
        throws ParseException, HomeException, AgentException
    {
        final List<CallDetail> list = new ArrayList<CallDetail>();
        CallDetail cd = null;

        try
        {
            CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                this);
        }
        catch (final FilterOutException e)
        {
            throw new AgentException("The ER is filter out");
        }

        for (final IPCGData data : IPCGWUnifiedBillingParser.instance().processEr501(ctx, params))
        {
            cd = createCallDetail(ctx, data, false);
            list.add(cd);
        }

        return list;
    }


    /**
     * Creates a call detail based on an IPCGData buffer item.
     *
     * @param ctx
     *            The operating context.
     * @param data
     *            IPCGData buffer item.
     * @return Corresponding call detail.
     * @throws HomeException
     *             Thrown if there are problems creating the call detail.
     */
    public CallDetail createCallDetail(final Context ctx, final IPCGData data) throws HomeException
    {
        return createCallDetail(ctx, data, false);
    }


    /**
     * Creates a call detail based on an IPCGData buffer item.
     *
     * @param ctx
     *            The operating context.
     * @param data
     *            IPCGData buffer item.
     * @param logError
     *            Whether call detail generation errors are logged to the appropriate
     *            error file.
     * @return Corresponding call detail.
     * @throws HomeException
     *             Thrown if there are problems creating the call detail.
     */
    public CallDetail createCallDetail(final Context ctx, final IPCGData data, final boolean logError)
        throws HomeException
    {
        final String ban = AccountSupport.getBAN(ctx, data.getChargedMSISDN(), data.getTranDate());
        if (ban == null || ban.trim().length() == 0)
        {
            final String message = MessageFormat.format("Cannot find account for subscriber \"{0}\". Cannot continue.",
                data.getChargedMSISDN());
            LogSupport.major(ctx, this, message);

            throw new HomeException(message);
        }

        final Account acct = (Account) ReportUtilities.findByPrimaryKey(ctx, AccountHome.class, ban);

        if (acct == null)
        {
            if (logError)
            {
                writeError(ctx, ban, data.getChargedMSISDN());
            }
            throw new HomeException("Invalid account: " + ban);
        }

        final CallTypeID callTypeId = new CallTypeID(data.getCallType().getIndex(), acct.getSpid());
        final CallType callType = (CallType) ReportUtilities.findByPrimaryKey(ctx, CallTypeHome.class, callTypeId);
        if (callType == null)
        {
            final String message = MessageFormat.format(
                "Could not find CallType entry for type id \"{0}\" and service provider \"{1}\".",
                    String.valueOf(data.getCallType().getIndex()), String.valueOf(acct.getSpid()));
            LogSupport.major(ctx, this, message);

            throw new HomeException(message);
        }

        final CallDetail callDetail = new CallDetail();
        callDetail.setBAN(ban);
        callDetail.setTranDate(data.getTranDate());
        callDetail.setCallType(data.getCallType());
        callDetail.setPostedDate(new Date());
        callDetail.setChargedMSISDN(data.getChargedMSISDN());
        if (RateUnitEnum.SEC == data.getUnitType())
        {
            final Time duration = new Time();
            duration.set(0, 0, (int) data.getUsage(), 0);
            callDetail.setDuration(duration);
        }
        else
        {
            callDetail.setDataUsage(data.getUsage());
        }
        callDetail.setVariableRateUnit(data.getUnitType());

        /*
         * The unit of the Charge values provided by the IPCG ER 501s is tenths of the
         * lowest denomination of the given currency.
         */
        callDetail.setCharge(Math.round(data.getCharge() / 10.0));

        callDetail.setSpid(acct.getSpid());
        callDetail.setTaxAuthority1(acct.getTaxAuthority());
        callDetail.setGLCode(callType.getGLCode());
        if(data.getIPCGDataID()==0L)
        {
            XQL xql = (XQL)ctx.get(XQL.class);
            long ipcgDataId = Math.max(1000000000,xql.nextSequence(ctx,IPCGDataXInfo.DEFAULT_TABLE_NAME, "IPCGDataID"));
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,"Setting IPCGDataID value to " + ipcgDataId + " for IPCGDATA " + data,null).log(ctx);
            }
            data.setIPCGDataID(ipcgDataId);
        }
        callDetail.setCallID(Long.toString(data.getIPCGDataID()));        
        return callDetail;
    }


    /**
     * Logs a call detail error to the IPCG error log file.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account BAN associated with the error.
     * @param msisdn
     *            MSISDN associated with the error.
     */
    public synchronized void writeError(final Context context, final String ban, final String msisdn)
    {
        try
        {
            if (this.errorStream_ == null)
            {
                final IPCGPollerConfig config = (IPCGPollerConfig) context.get(IPCGPollerConfig.class);
                this.errorStream_ = new PrintStream(new FileOutputStream(new File(config.getErrorLog())));
            }
            this.errorStream_.printf("BAN = %s, MSISDN = %s", ban, msisdn);
        }
        catch (final Exception exception)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(exception.getClass().getSimpleName());
            sb.append(" caught in ");
            sb.append("IPCGCallDetailCreator.writeError(): ");
            if (exception.getMessage() != null)
            {
                sb.append(exception.getMessage());
            }
            LogSupport.minor(context, this, sb.toString(), exception);
        }
    }

    /**
     * Error print stream.
     */
    private PrintStream errorStream_;

    /**
     * Singleton instance.
     */
    private static IPCGCallDetailCreator instance;
}
