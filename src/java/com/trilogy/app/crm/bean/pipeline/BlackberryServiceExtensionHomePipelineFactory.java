package com.trilogy.app.crm.bean.pipeline;

import java.io.IOException;
import java.rmi.RemoteException;
import com.trilogy.app.crm.extension.service.BlackberryTServiceExtension;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

public class BlackberryServiceExtensionHomePipelineFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx,BlackberryTServiceExtension.class,"BLACKTECHSEREXT");
        return home;
    }

}