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
package com.trilogy.app.crm.poller.event;

import java.util.Date;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.service.poller.nbio.event.EventProcessor;

import com.trilogy.app.crm.poller.CRMPoller;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.error.ErrorFileOutputAgent;
import com.trilogy.app.crm.support.AccountSupport;

/**
 * Base class for all ER processors. This class implements a series of
 * conversion methods to and from the strings in the ER array.
 *
 * @author paul.sperneac@redknee.com
 */
public abstract class CRMProcessor extends ContextAwareSupport implements EventProcessor, Constants
{
    /**
     * Constructor. Initializes the list of parameters.
     */
    public CRMProcessor()
    {
    }

    public void init(final Context ctx, final String name, final String errPrefix,
            final int queueSize, final int poolSize, final ContextAgent agent)
    {
        setContext(ctx);
        errorRecordHandler_= new ErrorFileOutputAgent(ctx, name, errPrefix);
        // TODO 2009-12-14 it looks like all agents have PM logs in their implementation,
        // so do we need the PMAgent?
        threadPool_ = new ThreadPool(name, queueSize, poolSize, new PMContextAgent(SUCCESS_POOL_NAME, name, agent));
        errThreadPool_ = new ThreadPool(name, queueSize, 1, new PMContextAgent(FAIL_POOL_NAME, name, errorRecordHandler_));
    }


    /**
     * {@inheritDoc}
     */
    public void process(final long date, final String erid, final char[] record, final int startIndex)
        throws NumberFormatException, IndexOutOfBoundsException
    {
        final Context ctx = getContext().createSubContext();
        initializeLocalContext(ctx);

        ctx.put(ProcessorInfo.class, new ProcessorInfo(date, erid, record, startIndex));

        try
        {
            threadPool_.execute(ctx);
        }
        catch (AgentException e)
        {
            new MinorLogMsg(this, "Failed to process ER" + erid + " because of Exception " + e.getMessage(),
                    e).log(ctx);
            saveErrorRecord(ctx, record);
        }
    }

    /**
     * This can be implemented in subclasses should they want to specifically add things to the subcontext.
     * Nothing is done by default
     */
    protected void initializeLocalContext(final Context ctx)
    {
        // NOP
    }

    /**
     * Returns the account number corresponding to the MSISDN.
     *
     * @param msisdn
     *            the MSISDN that we look for
     * @param transDate
     *            transaction date
     * @return the account number associated with the MSISDN.
     */
    protected String getAccountNumber(final String msisdn, final Date transDate)
    {
        return AccountSupport.getBAN(getContext(), msisdn, transDate);
    }

    public void saveErrorRecord(final Context ctx, final char[] record)
    {
        ctx.put(ErrorFileOutputAgent.ERROR_RECORD, new String(record));
        try
        {
            errThreadPool_.execute(ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(this, "Failed to save error record [" + new String(record) + "], e=" + e.getMessage(),
                    e).log(ctx);
        }
    }

    public void stop()
    {
        errorRecordHandler_.stop();
    }


    public static void playNice(final Context context, final String license)
    {
        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);

        if (!manager.isLicensed(context, license))
        {
            return;
        }

        final int SLEEP_TIME = 500; // ms
        final int MAX_WAIT = (5 * 60 * 1000) / SLEEP_TIME;

        int waitCount = 0;
        while (!manager.attemptRate(context, license))
        {
            ++waitCount;

            if (waitCount > MAX_WAIT)
            {
                new MinorLogMsg(CRMProcessor.class.getName(), "Play Nice Maximum Check Count Reached: " + waitCount,
                        null).log(context);
                break;
            }

            try
            {
                Thread.sleep(SLEEP_TIME);
            }
            catch (final InterruptedException exception)
            {
                new MinorLogMsg(CRMProcessor.class.getName(), "Play Nice Check Interrupted at Count: " + waitCount,
                        exception).log(context);
                break;
            }
        }
    }

    /*Depeding on the severity of the poller, it will compete with another poller of the same severity*/

    /**High severity License Name**/
    public static final String HIGH_ER_THROTTLING = "ER_THROTTLING";
    /**Medium severity License Name**/
    public static final String MEDIUM_ER_THROTTLING = "MEDIUM_ER_THROTTLING";
    /**Low severity License Name**/
    public static final String LOW_ER_THROTTLING = "LOW_ER_THROTTLING";

    protected ThreadPool threadPool_;
    protected ThreadPool errThreadPool_;
    protected ErrorFileOutputAgent errorRecordHandler_;
    
    public static final String SUCCESS_POOL_NAME = CRMPoller.class.getSimpleName() + " : SUCESS";
    public static final String FAIL_POOL_NAME = CRMPoller.class.getSimpleName() + " : FAIL";
}
