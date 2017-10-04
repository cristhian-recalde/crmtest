package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.dunning.config.DunningConfig;
import com.trilogy.app.crm.dunning.config.DunningConfigHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;


public class DunningConfigHomePipelineFactory implements PipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home = CoreSupport.bindHome(ctx, DunningConfig.class);
        home = new SpidAwareHome(ctx, home);
        home = new RMIClusteredHome(ctx, DunningConfigHome.class.getName(), home);
        home = new SortingHome(ctx, home);
        return home;
    }
}
