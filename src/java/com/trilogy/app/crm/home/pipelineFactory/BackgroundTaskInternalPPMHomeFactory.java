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
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.om.PPMInfo;
import com.trilogy.framework.xlog.om.PPMInfoHome;
import com.trilogy.framework.xlog.om.PPMInfoXMLHome;


/**
 * Creates the homes to persist internal PPM and Agent control and decorates pipeline for
 * locating them.
 * 
 * @author simar.singh@redknee.com
 */
public class BackgroundTaskInternalPPMHomeFactory implements PipelineFactory
{

    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException, AgentException
    {
        Home home = new PPMInfoXMLHome(ctx, "BulkTaskPPMInfo.xml");
        home = new BackgroundTaskPPMLocatorHome(ctx, home);
        home = new PMHome(ctx, getClass().getSimpleName(), home);
        ctx.put(PPM_BACKGROUND_TASK_HOME_KEY, home);
        return home;
    }

    public static final String PPM_BACKGROUND_TASK_HOME_KEY = "PPM_BACKGROUND_TASK_HOME_KEY";

    static class BackgroundTaskPPMLocatorHome extends HomeProxy
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;


        public BackgroundTaskPPMLocatorHome(Context ctx, Home home)
        {
            super(ctx, home);
        }


        @Override
        public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
        {
            PPMInfo ppmInfo = HomeSupportHelper.get(ctx).findBean(ctx, PPMInfo.class, obj);
            if (null == ppmInfo)
            {
                ppmInfo = (PPMInfo) getDelegate().find(ctx, obj);
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
            final Set<PPMInfo> set;
            {
                set = new HashSet<PPMInfo>(super.select(ctx, obj));
                set.addAll(((Home) ctx.get(PPMInfoHome.class)).select(ctx, obj));
            }
            return set;
        }
    }
}
