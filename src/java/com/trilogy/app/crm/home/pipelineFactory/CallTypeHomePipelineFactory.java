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
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CallTypeHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home = CoreSupport.bindHome(ctx, CallType.class); 
        
        home = new RMIClusteredHome(ctx, CallTypeHome.class.getName(), home);

        home = new SpidAwareHome(ctx, home);
                
        home = new SortingHome(ctx, home);
        
        ConfigChangeRequestSupportHelper.get(ctx).registerHomeForConfigSharing(ctx, home, CallType.class);
        
        return home;
    }

}
