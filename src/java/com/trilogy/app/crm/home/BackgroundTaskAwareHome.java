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

import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleAgentControlHome;
import com.trilogy.framework.lifecycle.LifecycleAgentControlXInfo;
import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.Identifiable;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.om.PPMInfo;
import com.trilogy.framework.xlog.om.PPMInfoHome;
import com.trilogy.framework.xlog.om.PPMInfoXInfo;

import com.trilogy.app.crm.bean.BackgroundTaskAware;
import com.trilogy.app.crm.home.pipelineFactory.BackgroundTaskInternalLifeCycleAgentHomeFactory;
import com.trilogy.app.crm.home.pipelineFactory.BackgroundTaskInternalPPMHomeFactory;


/**
 * Manages (Installs/Unistalls) PPMInfo and LifeCycleAgent control in the Encapsulated
 * BackgroundTask of the BackgroundTaskAwareBean
 * 
 * @author simar.singh@redknee.com
 */
public class BackgroundTaskAwareHome<BEAN extends AbstractBean & Identifiable & BackgroundTaskAware> extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a a new BackgroundTaskAwareHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public BackgroundTaskAwareHome(Class<BEAN> classObject, final Home delegate)
    {
        super(delegate);
    }


    /**
     * Forwards creation of bean and if successful, executes the BuckgroundTask
     * <RunnhableLifeCycleAgent> encapsulated in it.
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object bean) throws HomeException
    {
        final BEAN taskAwarebean = (BEAN) super.create(ctx, bean);
        try
        {
        	taskAwarebean.getTask().setTaskOwner(taskAwarebean);
            taskAwarebean.getTask().execute(ctx);
        }
        catch (AgentException e)
        {
            throw new HomeException(e.getMessage(), e);
        }
        return taskAwarebean;
    }


    /**
     * Removed the task and its associated LifeCycleAgentControl and PPMInfo entries
     * {@inheritDoc}
     */
    @Override
    public void remove(Context ctx, Object bean) throws HomeException
    {
        // remove the associatied PPMInfo and LifecycleAgentControlEnty
        super.remove(ctx, bean);
        @SuppressWarnings("unchecked")
        final BEAN taskAwarebean = (BEAN) bean;
        final String key = taskAwarebean.getTask().getKey();
        try
        {
            Home ppmHome = (Home) ctx.get(BackgroundTaskInternalPPMHomeFactory.PPM_BACKGROUND_TASK_HOME_KEY);
            PPMInfo ppmInfo = (PPMInfo) ppmHome.find(ctx, key);
            if (null != ppmHome)
            {
                ppmHome.remove(ctx, ppmInfo);
            }
            Home agentCtlHome = (Home) ctx.get(BackgroundTaskInternalLifeCycleAgentHomeFactory.AGENT_BACKGROUND_TASK_HOME_KEY);
            LifecycleAgentControl agentCtl = (LifecycleAgentControl) (agentCtlHome).find(ctx, key);
            if (null != agentCtl)
            {
                agentCtlHome.remove(ctx, agentCtl);
            }
        }
        catch (Throwable t)
        {
            final String message = "KEY [" + key
                    + "]Could not remove Porgress-Monitor (PPMInfo0 na Control (LifeCycleAgentControl). Reson["
                    + t.getMessage() + "] ";
            new MinorLogMsg(this, message, t).log(ctx);
            ExceptionListener exceptionListener = (ExceptionListener) ctx.get(ExceptionListener.class);
            exceptionListener.thrown(new IllegalStateException(message, t));
        }
    }
}
