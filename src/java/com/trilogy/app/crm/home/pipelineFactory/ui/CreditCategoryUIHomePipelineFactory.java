package com.trilogy.app.crm.home.pipelineFactory.ui;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.ui.CreditCategoryUIAdapter;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


public class CreditCategoryUIHomePipelineFactory implements PipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the credit category UI home ");

        Home home = new ContextRedirectingHome(ctx, com.redknee.app.crm.bean.CreditCategoryHome.class);
        
        // Add adapter to switch between GUI bean and core beans.
        home = new AdapterHome(ctx, home, new CreditCategoryUIAdapter(ctx));
        
        return home;
    }
}
