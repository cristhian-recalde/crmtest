/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries. Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All
 * Rights Reserved.
 */
package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.home.pipelineFactory.BackgroundTaskInternalLifeCycleAgentHomeFactory;
import com.trilogy.app.crm.home.pipelineFactory.BackgroundTaskInternalPPMHomeFactory;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleAgentControlXInfo;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.lifecycle.LifecycleSupport;
import com.trilogy.framework.lifecycle.RunnableLifecycleAgent;
import com.trilogy.framework.lifecycle.RunnableLifecycleAgentProxy;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.PPMLogMsg;
import com.trilogy.framework.xlog.om.PPMInfo;
import com.trilogy.framework.xlog.om.PPMInfoXInfo;


/**
 * 
 * @author simar.singh@redknee.com
 * 
 */
public class BackgroundTask extends AbstractBackgroundTask implements RunnableLifecycleAgent
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final RunnableLifecycleAgentProxy lifecycleProxy_;
    private volatile PPMLogMsg ppmLogMsg_;


    public BackgroundTask()
    {
        lifecycleProxy_ = new RunnableLifecycleAgentProxy(ContextLocator.locate(), this, key_);
    }


    private final void initilaize()
    {
        setKey(String.valueOf(this.getOwnerId()));
    }


    @Override
    public void setKey(String key) throws IllegalArgumentException
    {
        super.setKey(key);
        lifecycleProxy_.setAgentId(key);
    }


    @Override
    /**
     * A LifecycleAgent, can at its discretion choose to re-implement the various lifecycle event methods
     * such as 'doInit()', 'doStart()', and 'doRelease()'.  However, the simplest strategy for replacing a simple
     * java.lang.Runnable is to simply override the 'doRun()' method.  This method is analogous to the 'run()'
     * method in a thread.
     */
    public void doRun(Context ctx) throws LifecycleException
    {
        try
        {
            while (lifecycleProxy_.getState().equals(LifecycleStateEnum.RUNNING))
            {
                createPPM(ctx);
                {
                    final Context agentContext = ctx.createSubContext();
                    agentContext.put(PPMLogMsg.class, ppmLogMsg_);
                    agentContext.put(BackgroundTask.class, this);
                    getTaskExecutor(ctx).execute(agentContext);
                }
                pegPPM(ctx);
                break;
            }
        }
        catch (Throwable t)
        {
            /**
             * If this is a failure case (the most likely scenario!) then we _need_ to
             * generate a LifecycleException. This will ensure a message is logged, the
             * lifecycle monitor is notified that the thread has failed, and that the
             * lifecycle state is properly transitioned.
             */
            lifecycleProxy_.doFail(ctx, t);
            throw new LifecycleException(t.toString(), t);
        }
        finally
        {
            pegPPM(ctx);
            /**
             * Insert normal invariant thread/runnable teardown code here - this code
             * always gets executed even if an exception is thrown in the body of the
             * while()... statement.
             */
            clean(ctx);
        }
    }


    @Override
    public Object doCmd(Context ctx, Object cmd) throws LifecycleException
    {
        return lifecycleProxy_.doCmd(ctx, cmd);
    }


    @Override
    public void doInit(Context ctx) throws LifecycleException
    {
        new DebugLogMsg(this, "Nothing to inialize", null).log(ctx);
    }


    @Override
    public void doRelease(Context ctx) throws LifecycleException
    {
        new DebugLogMsg(this, "Nothing to release", null).log(ctx);
    }


    @Override
    public void doStart(Context ctx) throws LifecycleException
    {
        new DebugLogMsg(this, "Explicit doStart not supported", null).log(ctx);
    }


    @Override
    public void doStop(Context ctx) throws LifecycleException
    {
        new DebugLogMsg(this, "Explicit doStop is supported. Stoping the angent", null).log(ctx);
        lifecycleProxy_.setState(LifecycleStateEnum.STOP);
        lifecycleProxy_.interrupt();
    }


    @Override
    public String getAgentId()
    {
        return lifecycleProxy_.getAgentId();
    }


    @Override
    public void setAgentId(String agentId)
    {
        lifecycleProxy_.setAgentId(agentId);
    }


    @Override
    public Context getContext()
    {
        return lifecycleProxy_.getContext();
    }


    @Override
    public void setContext(Context context)
    {
        lifecycleProxy_.setContext(context);
    }


    @Override
    public void execute(Context ctx) throws AgentException
    {
        initilaize();
        lifecycleProxy_.execute(ctx);
    }


    @Override
    public String getOwnerId()
    {
        return String.valueOf(((Identifiable) this.getTaskOwner()).ID());
    }


    public ContextAgent getTaskExecutor(Context ctx)
    {
        return getTaskExecutor();
    }


    protected void pegPPM(Context ctx)
    {
        if (null != ppmLogMsg_)
        {
            ppmLogMsg_.log(ctx);
        }
    }


    protected PPMLogMsg createPPM(Context ctx)
    {
        return ppmLogMsg_ = new PPMLogMsg(ctx, getTaskOwner().getClass().getName(), getOwnerId());
    }

    private void clean(final Context ctx)
    {
        new Thread()
        {

            public void run()
            {
                lifecycleProxy_.waitRun(ctx, 10000L);
                PMLogMsg pm = new PMLogMsg("Bulk Task Cleanup", "clean(" + getKey() + ")");
                try
                {
                    HomeSupport homeSupport = HomeSupportHelper.get(ctx);
                    PPMInfo ppmInfo = persistPPM(ctx);
                    if (null != ppmInfo)
                    {
                        homeSupport.removeBean(ctx, ppmInfo);
                    }
                    LifecycleAgentControl agentCtl = persistAgent(ctx);
                    if (null != agentCtl)
                    {
                        homeSupport.removeBean(ctx, agentCtl);
                    }
                    LifecycleSupport.remove(ctx, getKey());
                    pm.log(ctx);
                }
                catch (Throwable t)
                {
                    new MajorLogMsg(this, "Error persisting and cleaning up agent and progress entires for task ["
                            + getKey() + "]", t).log(ctx);
                }
                finally
                {
                    pm.log(ctx);
                }
            }
        }.start();
    }

    
    private PPMInfo persistPPM(Context ctx) throws HomeException
    {
        PPMInfo ppmInfo = HomeSupportHelper.get(ctx).findBean(ctx, PPMInfo.class, new EQ (PPMInfoXInfo.MEASUREMENT, getKey()));
        if (null != ppmInfo)
        {
            PMLogMsg pm = new PMLogMsg(getClass().getSimpleName(), "persistPPM(" + ppmInfo.getKey() + ")");
            Home ppmInternalHome = (Home) ctx.get(BackgroundTaskInternalPPMHomeFactory.PPM_BACKGROUND_TASK_HOME_KEY);
            ppmInternalHome.remove(ctx, ppmInfo);
            ppmInternalHome.create(ctx, ppmInfo);
            pm.log(ctx);
        }
        return ppmInfo;
    }


    private LifecycleAgentControl persistAgent(Context ctx) throws HomeException
    {
        LifecycleAgentControl lifeAgent = HomeSupportHelper.get(ctx).findBean(ctx, LifecycleAgentControl.class,
        		new EQ (LifecycleAgentControlXInfo.AGENT_ID, getKey()));
        if (null != lifeAgent)
        {
            PMLogMsg pm = new PMLogMsg(getClass().getSimpleName(), "persistAgent(" + lifeAgent.getAgentId() + ")");
            Home home = (Home) ctx.get(BackgroundTaskInternalLifeCycleAgentHomeFactory.AGENT_BACKGROUND_TASK_HOME_KEY);
            home.remove(ctx, lifeAgent);
            home.create(ctx, lifeAgent);
            pm.log(ctx);
        }
        return lifeAgent;
    }

}
