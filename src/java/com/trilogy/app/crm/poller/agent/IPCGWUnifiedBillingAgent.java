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

package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.IPCGDataHome;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.IPCGWUnifiedBillingParser;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.IPCGWUnifiedBillingProcessor;


/**
 * IPCG ER 501 processing agent.
 *
 * @author vincci.cheng@redknee.com
 * @author cindy.wong@redknee.com
 */
public class IPCGWUnifiedBillingAgent implements ContextAgent
{

    /**
     * Create a new instance of <code>IPCGWUnifiedBillingAgent</code>.
     *
     * @param processor
     *            Processor for the ER.
     */
    public IPCGWUnifiedBillingAgent(final CRMProcessor processor)
    {
        super();
        this.processor_ = processor;
    }


    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        final List<String> params = new ArrayList<String>();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);

        final Home home = (Home) ctx.get(IPCGDataHome.class);
        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        final IPCGPollerConfig config = (IPCGPollerConfig) ctx.get(IPCGPollerConfig.class);
        if (config == null)
        {
            new MajorLogMsg(this, "ErPollerconfig is null for a IPCGWPoller", null).log(ctx);
            return;
        }

        try
        {
            try
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',', info.getErid(),
                    this);
            }
            catch (final FilterOutException e)
            {
                return;
            }

            for (final IPCGData data : IPCGWUnifiedBillingParser.instance().processEr501(ctx, params))
            {
                home.create(ctx, data);
            }
        }
        catch (final Throwable throwable)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(throwable.getClass().getSimpleName());
            sb.append(" caught when processing IPCG ER 501");
            if (throwable.getMessage() != null)
            {
                sb.append(": ");
                sb.append(throwable.getMessage());
            }
            LogSupport.minor(ctx, this, sb.toString(), throwable);
            this.processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }

    /**
     * ER processor to use.
     */
    private final CRMProcessor processor_;

    /**
     * Name of PM module.
     */
    private static final String PM_MODULE = IPCGWUnifiedBillingProcessor.class.getName();
}
