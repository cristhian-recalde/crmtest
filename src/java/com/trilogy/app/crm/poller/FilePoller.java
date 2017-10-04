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

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.PollerProcessor;
import com.trilogy.app.crm.bean.PollerProcessorPackage;
import com.trilogy.app.crm.config.NotesPollerConfig;
import com.trilogy.app.crm.config.NotesPollerConfigHome;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.poller.nbio.PollerConfig;
import com.trilogy.service.poller.nbio.PollerLogger;
import com.trilogy.service.poller.nbio.event.EventProcessor;

import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Constructor;

public class FilePoller
    extends CRMPoller
{
    public FilePoller(Context ctx, ErPollerConfig config)
        throws Exception
    {
        this(ctx, config, getConfig(Constants.POLLER + config.getId(), new CRMPollerStatusNotifier(ctx, Constants.POLLER + config.getId()), config), new PollerLoggerImpl(ctx));
    }

    /**
     * Constructor
     * 
     * @param ctx
     *            the context that the poller runs into. It is passed to the
     *            processor
     * @param config
     *            the configuration bean
     * @param logger
     *            the logger
     * @throws java.lang.Exception
     */
    public FilePoller(Context ctx, ErPollerConfig erPollerConfig, PollerConfig config, PollerLogger logger)
        throws Exception
    {
        super(ctx, erPollerConfig, config, logger, 0, 0);
    }

    public void loadCRMERHandlers()
    {
        if(LogSupport.isDebugEnabled(getContext()))
        {
            LogSupport.debug(getContext(), this, "Loading ER Handlers for Poller " + config_.getId());
        }

        try
        {
            Context subCtx = getContext().createSubContext("Poller" + config_.getId());
            subCtx.put(ErPollerConfig.class, config_);

            Home home = config_.pollerProcessorPackages(getContext());
            Iterator i = home.selectAll(getContext()).iterator();
            while(i.hasNext())
            {
                PollerProcessorPackage pack = (PollerProcessorPackage)i.next();

                if(LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "Loading Processor Package " + pack.getPackageId());
                }

                Context packageSubCtx = subCtx.createSubContext("PollerProcessorPackage" + pack.getPackageId());
                packageSubCtx.put(PollerProcessorPackage.class, pack);
                List procList = pack.getProcessors();
                Iterator procListIt = procList.iterator();
                while(procListIt.hasNext())
                {
                    PollerProcessor proc = (PollerProcessor)procListIt.next();
                    Context procCtx = packageSubCtx.createSubContext("PollerProcessor " + proc.getProcessor());
                    procCtx.put(PollerProcessor.class, proc);
                    try
                    {
                        EventProcessor evProc = (EventProcessor)getConstructor(proc.getProcessor()).newInstance(new Object[]{procCtx, pack.getQueueSize(), pack.getThreads()});
                        if(!(evProc instanceof com.redknee.app.crm.poller.event.NotesUpdateProcessor))
                        {
                            addERHandler(proc.getEr(), evProc);
                        }
                        else
                        {
                            Home notehome = (Home) getContext().get(NotesPollerConfigHome.class);
                            NotesPollerConfig notesPollerConfig = (NotesPollerConfig)notehome.find(getContext(), config_.getId());
                            addERHandler(notesPollerConfig.getName(), evProc);
                        }
                    }
                    catch(Exception e)
                    {
                        LogSupport.minor(getContext(), this, "Exception caught creating processor " + proc, e);
                    }
                }
            }
        }
        catch(Exception e)
        {
            LogSupport.major(getContext(), this, "Exception trying to instantiate poller " + config_.getId(), e);
        }
    }

    private Constructor getConstructor(String className)
        throws Exception
    {
        return Class.forName(className).getConstructor(new Class[]{Context.class, int.class, int.class});
    }
}