package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.NoSelectAllHome;

import com.trilogy.app.crm.extension.ExtensionInstallationHome;
import com.trilogy.app.crm.extension.service.AbstractExternalServiceTypeExtension;
import com.trilogy.app.crm.extension.service.ExternalServiceTypeExtension;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


public class ExternalServiceTypeExtensionHomePipelineFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, ExternalServiceTypeExtension.class,
                "SVCEXTEXTERNALSERVICETYPE");

        home = new AdapterHome(
                ctx,
                new ExtendedBeanAdapter<AbstractExternalServiceTypeExtension, ExternalServiceTypeExtension>(
                        AbstractExternalServiceTypeExtension.class,
                        ExternalServiceTypeExtension.class), home);

        home = new ExtensionInstallationHome(ctx, home);
        home = new NoSelectAllHome(home);

        return home;
    }

}