package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.GenericTransientHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ReadOnlyHome;

import com.trilogy.app.crm.bean.SubscriberStateAction;
import com.trilogy.app.crm.bean.SubscriberStateActionHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.TotalCachingHome;

public class SubscriberStateActionHomePipelineFactory
{

    public SubscriberStateActionHomePipelineFactory() 
    {
        super();
    }

    public void createPipeline(Context ctx, Context serverCtx)
            throws RemoteException, HomeException, IOException 
    {
        Home  home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriberStateAction.class,"SubscriberStateAction");
        Home  cacheHome =  new GenericTransientHome(ctx, SubscriberStateAction.class);            
        home = new TotalCachingHome(ctx, cacheHome, home, true);           
        home = new ReadOnlyHome(home); 
        ctx.put(SubscriberStateActionHome.class, home);

    }
}