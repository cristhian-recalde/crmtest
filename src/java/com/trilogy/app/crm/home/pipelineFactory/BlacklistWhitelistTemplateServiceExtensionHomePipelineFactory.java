package com.trilogy.app.crm.home.pipelineFactory;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.extension.ExtensionInstallationHome;
import com.trilogy.app.crm.extension.service.AbstractBlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


public class BlacklistWhitelistTemplateServiceExtensionHomePipelineFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(Context ctx, Context serverCtx) 
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, BlacklistWhitelistTemplateServiceExtension.class,
                "SVCEXTBLWLTEMPLATE");

        home = new AdapterHome(
                ctx,
                new ExtendedBeanAdapter<AbstractBlacklistWhitelistTemplateServiceExtension, BlacklistWhitelistTemplateServiceExtension>(
                        AbstractBlacklistWhitelistTemplateServiceExtension.class,
                        BlacklistWhitelistTemplateServiceExtension.class), home);

        home = new ExtensionInstallationHome(ctx, home);
        home = new ValidatingHome(home);
        home = new NoSelectAllHome(home);

        return home;
    }

}