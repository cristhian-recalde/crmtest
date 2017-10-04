package com.trilogy.app.crm.home.account.extension;

import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtensionHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xhome.home.NoSelectAllHome;


public class FriendsAndFamilyExtensionHomePipelineFactory implements PipelineFactory
{
    /**
     * Create a new instance of <code>FriendsAndFamilyHomePipelineFactory</code>.
     */
    protected FriendsAndFamilyExtensionHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>FriendsAndFamilyHomePipelineFactory</code>.
     *
     * @return An instance of <code>FriendsAndFamilyHomePipelineFactory</code>.
     */
    public static FriendsAndFamilyExtensionHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new FriendsAndFamilyExtensionHomePipelineFactory();
        }
        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context context, final Context serverContext)
    {
        final Home home = StorageSupportHelper.get(context).createHome(context, FriendsAndFamilyExtension.class, "ACTEXTFNF");
        return decorateHome(home, context, serverContext);
    }
    
    /**
     * Decorates the home.
     *
     * @param originalHome
     *            Home being decorated.
     * @param context
     *            The operating context.
     * @param serverContext
     *            The server context.
     * @return Decorated home.
     */
    public Home decorateHome(final Home originalHome, final Context context, final Context serverContext)
    {
        Home home = originalHome;
        
        home = new LRUCachingHome(
        		context,
        		FriendsAndFamilyExtension.class,
                true,
                home);
        
        /**
         * Checking if it's not an invoice server install before installing the
         * F&F adapter home.
         */
        if (!com.redknee.app.crm.support.DeploymentTypeSupportHelper.get(context).isInvoiceServer(context))
        {
            home = new AdapterHome(home, new FriendsAndFamilyExtensionAdapter());
        }
        
        home = new AccountExtensionInstallationHome(context, home);
        home = new NoSelectAllHome(home);
        
        home =  ConfigChangeRequestSupportHelper.get(context).registerHomeForConfigSharing(context, home, FriendsAndFamilyExtension.class);
        
        context.put(FriendsAndFamilyExtensionHome.class, home);

        return home;
    }
   /**
     * Singleton instance.
     */
    private static FriendsAndFamilyExtensionHomePipelineFactory instance;
}
