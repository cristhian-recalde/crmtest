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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.lifecycle.LifecycleAgentControl;
import com.trilogy.framework.lifecycle.LifecycleAgentControlHome;
import com.trilogy.framework.lifecycle.LifecycleAgentControlXMLHome;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Creates the homes to persist internal PPM and Agent control and decorates pipeline for
 * locating them.
 * 
 * @author simar.singh@redknee.com
 */
public class BackgroundTaskInternalLifeCycleAgentHomeFactory implements PipelineFactory
{

    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException, AgentException
    {
        Home home = new LifecycleAgentControlXMLHome(ctx, "BulkTaskAgentControl.xml");
        home = new AdapterHome(home, new Adapter()
        {
            /**
             * Blocking Sate is persistent AgentControl model but we don't need it
             * Current state is transient in the model but we need it
             * Since our need is just to display this immutable records, using Blocking State to store last current Sate
             * Better way would have been to define a new / extended model but it will too much work for this little use
             */
            
            /**
             * 
             */
            private static final long serialVersionUID = 1L;


            @Override
            public Object unAdapt(Context ctx, Object obj) throws HomeException
            {
                if (obj instanceof LifecycleAgentControl)
                {
                    LifecycleAgentControl agentControl = (LifecycleAgentControl) obj;
                    LifecycleStateEnum state = agentControl.getState();
                    if (state != LifecycleStateEnum.UNKNOWN)
                    {
                        agentControl.setBlockingState(state);
                    }
                }
                return obj;
            }
            
        
            @Override
            public Object adapt(Context ctx, Object obj) throws HomeException
            {
                if (obj instanceof LifecycleAgentControl)
                {
                    LifecycleAgentControl agentControl = (LifecycleAgentControl) obj;
                    if (agentControl.getState() == LifecycleStateEnum.UNKNOWN)
                    {
                        LifecycleStateEnum state = agentControl.getBlockingState();
                        if (state != LifecycleStateEnum.UNKNOWN && state != agentControl.getInitialState())
                        {
                            agentControl.setCurrentState(state);
                        }
                    }
                    else
                    {
                        agentControl.setCurrentState(LifecycleStateEnum.RUN);
                    }
                }
                return obj;
            }
        });
        home = new BackgroundTaskLifeAgentLocatorHome(ctx, home);
        home = new PMHome(ctx, getClass().getSimpleName(), home);
        ctx.put(AGENT_BACKGROUND_TASK_HOME_KEY, home);
        return home;
    }

    public static final String AGENT_BACKGROUND_TASK_HOME_KEY = "AGENT_BACKGROUND_TASK_HOME_KEY";

    static class BackgroundTaskLifeAgentLocatorHome extends HomeProxy
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;


        public BackgroundTaskLifeAgentLocatorHome(Context ctx, Home home)
        {
            super(ctx, home);
        }


        @Override
        public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            LifecycleAgentControl ppmInfo = HomeSupportHelper.get(ctx).findBean(ctx, LifecycleAgentControl.class, obj);
            if (null == ppmInfo)
            {
                ppmInfo = (LifecycleAgentControl) getDelegate().find(ctx, obj);
            }
            return ppmInfo;
        }


        @Override
        public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            getDelegate(ctx).remove(ctx, obj);
            return getDelegate(ctx).create(ctx, obj);
        }


        @Override
        public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            getDelegate(ctx).remove(ctx, obj);
            return getDelegate(ctx).create(ctx, obj);
        }


        @Override
        public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            getDelegate(ctx).remove(ctx, obj);
        }


        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            final Set<LifecycleAgentControl> set;
            {
                set = new HashSet<LifecycleAgentControl>(super.select(ctx, obj));
                set.addAll(((Home) ctx.get(LifecycleAgentControlHome.class)).select(ctx, obj));
            }
            return set;
        }
    }
}
