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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.poller.lifecycle;

import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.poller.CRMPoller;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.agent.PollerContinuousListener;
import com.trilogy.app.crm.poller.agent.PollerInstall;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.lifecycle.LifecycleAgent;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.txn.CommitRatioHome;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * 
 * @author Aaron Gourley
 * @since 7.5
 *
 */
public class PollerLifecycleAgent extends LifecycleAgentSupport
{
    public PollerLifecycleAgent(Context context)
    {
        this(context, null);
    }
    
    public PollerLifecycleAgent(Context context, String agentId)
    {
        super(context);
        setAgentId(agentId);
        if( agentId == null )
        {
            ErPollerConfig config = (ErPollerConfig) getContext().get(ErPollerConfig.class);
            if( config == null )
            {
                setAgentId(getClass().getName() + ": Unknown");
                setState(LifecycleStateEnum.UNKNOWN);
            }
            else
            {
                setAgentId(getClass().getName() + ": Poller ID " + config.getId());   
            }
        }
    }


    @Override
    public synchronized void doStart(final Context ctx)
        throws LifecycleException
    {
        super.doStart(ctx);
        
        if(getContext()==null)
        {
            new MajorLogMsg(this,"the context passed to the constructor is null. Cannot continue",null).log(ctx);
            throw new LifecycleException("Poller initialization failed.  " + getClass().getSimpleName() + " context is null.");
        }
        
        ErPollerConfig config=(ErPollerConfig) getContext().get(ErPollerConfig.class);
        if(config==null)
        {
            new MajorLogMsg(this,"Initializing poller failed. " + getClass().getSimpleName() + " requires instance of ErPollerConfig in context",null).log(getContext());
            throw new LifecycleException("Poller initialization failed.  ErPollerConfig not found in context");
        }
        
        // This calls a context factory (See package: com.redknee.app.crm.poller.factory)
        CRMPoller poller = (CRMPoller)getContext().get(CRMPoller.class);
        if( poller == null )
        {
            new MajorLogMsg(this,"Initializing poller failed.",null).log(getContext());
            throw new LifecycleException("Poller initialization failed (id=" + config.getId() + ")");
        }

        // Start the poller thread
        String pollerThreadName = getPollerThreadName(config);
        getContext().put(getPollerContextKey(config),poller);
        Thread th=new Thread(poller,pollerThreadName);
        getContext().put(pollerThreadName,th);
        th.start();

        startPollerHeartbeatTask(config);
        PollerContinuousListener pollerHeartbeater = (PollerContinuousListener)ctx.get(PollerContinuousListener.class);
        if( pollerHeartbeater != null )
        {
            pollerHeartbeater.setEnabled(true);
        }
    }


    @Override
    public synchronized void doStop(final Context ctx)
        throws LifecycleException
    {
        super.doStop(ctx);
        
        final ErPollerConfig pollerConfig = (ErPollerConfig) getContext().get(ErPollerConfig.class);
        if (pollerConfig == null)
        {
            new MajorLogMsg(this, "System error: no ErPollerConfig found in context.", null).log(getContext());
            throw new LifecycleException("Poller shutdown failed.  ErPollerConfig not found in context");
        }
        
        PollerContinuousListener pollerHeartbeater = (PollerContinuousListener)ctx.get(PollerContinuousListener.class);
        if( pollerHeartbeater != null )
        {
            // "Disable" the heartbeat mechanism because the poller was explicitly stopped by something.
            pollerHeartbeater.setEnabled(false);
        }

        // Stop heartbeat task to prevent it from restarting the poller.
        // This should be expected, since something specifically requested that it stop.
        stopPollerHeartbeatTask(pollerConfig);
        
        final Object poller =
            getContext().get(getPollerContextKey(pollerConfig));
        if( poller != null )
        {
            // Make sure the poller is stopped before doing the final commit.
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Start stopping Poller Id " + pollerConfig.getId(), null).log(getContext());
            }
            stopPoller(poller);
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(
                    this,
                    "Finished stopping Poller Id " + pollerConfig.getId() + " - wait for 2 seconds...",
                    null).log(getContext());
            }
        }
        
        // Wait for the poller to finish.
        try
        {
            wait(2000);
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
        
        final Home callDetailHome = (Home) getContext().get(CallDetailHome.class);
        if (callDetailHome == null)
        {
            new MajorLogMsg(this, "System error: no CallDetailHome found in context.", null);
            return;
        }
        
        // Do the final commit.
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(
                this,
                "Start doing final database commit for Poller Id " + pollerConfig.getId(),
                null).log(getContext());
        }
        try
        {
            Context subCtx = ctx.createSubContext();
            CommitRatioHome commitHome = new CommitRatioHome(subCtx, callDetailHome);
            callDetailHome.cmd(getContext(), com.redknee.framework.xhome.txn.CommitRatioHome.COMMIT_CMD);
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, "Unable to commit transactions: " + e.getMessage(), e);
        }
        if (LogSupport.isDebugEnabled(getContext()))
        {
            new DebugLogMsg(
                this,
                "Finished doing final database commit for Poller Id " + pollerConfig.getId(),
                null).log(getContext());
        }
    }

    public void restartPoller()
    {
        Context ctx = getContext();
        try
        {
            new MinorLogMsg(this,"Attempting to restart poller: "+getAgentId(),null).log(ctx);
            LifecycleAgent pollerLifecycle = LifecycleSupport.getLifecycleAgent(ctx, getAgentId());
            if( pollerLifecycle != null )
            {
                // Logic taken from FW's LifecycleWebAction
                try
                {
                    pollerLifecycle.doStop(ctx);
                }
                catch (LifecycleException e)
                {
                    new MajorLogMsg(this,"Got an exception while restarting a Poller: "+e.getMessage(),e).log(ctx);
                }   

                // Restart service
                LifecycleSupport.queue(ctx, pollerLifecycle.getAgentId(), LifecycleStateEnum.START);
            }
        }
        catch (HomeException e)
        {
            new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + PollerLifecycleAgent.class.getSimpleName() + ".restartPoller(): " + e.getMessage(), e).log(ctx);
        }
    }
        
    
    public static String getPollerContextKey(ErPollerConfig pollerConfig)
    {
        return Constants.POLLER + pollerConfig.getId();
    }
    
    
    public static String getPollerThreadName(ErPollerConfig pollerConfig)
    {
        return Constants.POLLER_THREAD + pollerConfig.getId();
    }


    private void startPollerHeartbeatTask(ErPollerConfig config)
    {
        TaskEntry task = TaskHelper.retrieve(getContext(), PollerInstall.getPollerTaskName(config));
        if( task != null && task.getStatus() != TaskStatusEnum.AVAILABLE )
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Starting poller heartbeat task for Poller Id " + config.getId(), null).log(getContext());
            }
            task.setStatus(TaskStatusEnum.AVAILABLE);
            TaskHelper.store(getContext(), task);
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(
                    this,
                    "Finished starting poller heartbeat task for Poller Id " + config.getId(),
                    null).log(getContext());
            }
        }
    }

    private void stopPollerHeartbeatTask(ErPollerConfig config)
    {
        TaskEntry task = TaskHelper.retrieve(getContext(), PollerInstall.getPollerTaskName(config));
        if( task != null && task.getStatus() != TaskStatusEnum.DISABLED )
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "Start stopping poller heartbeat task for Poller Id " + config.getId(), null).log(getContext());
            }
            task.setStatus(TaskStatusEnum.DISABLED);
            TaskHelper.store(getContext(), task);
            if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(
                    this,
                    "Finished stopping poller heartbeat task for Poller Id " + config.getId(),
                    null).log(getContext());
            }
        }
    }
    
    
    protected void stopPoller(final Object poller)
    {
        ((CRMPoller) poller).stop();
    }
    
    @Override
    public String toString()
    {
        // TODO: make more descriptive of which poller this is
        return super.toString();
    }
}
