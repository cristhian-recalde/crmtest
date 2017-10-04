package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.cluster.RMIClusteredHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;

import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.account.SubscriptionClassNoRelationshipRemoveHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;

public class SubscriptionClassHomePipelineFactory implements PipelineFactory
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Singleton instance.
     */
    private static SubscriptionClassHomePipelineFactory instance_;
    
    /**
     * Create a new instance of <code>SubscriptionClassHomePipelineFactory</code>.
     */
    protected SubscriptionClassHomePipelineFactory()
    {
        // empty
    }

    /**
     * Returns an instance of <code>SubscriptionClassHomePipelineFactory</code>.
     *
     * @return An instance of <code>SubscriptionClassHomePipelineFactory</code>.
     */
    public static SubscriptionClassHomePipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriptionClassHomePipelineFactory();
        }
        return instance_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
    AgentException
    {
        // This bindHome call will return the home pipeline created by CoreSubscriptionClassHomePipelineFactory
        Home home = CoreSupport.bindHome(ctx, SubscriptionClass.class);

        home = new NotifyingHome(home);
        home = new RMIClusteredHome(ctx, SubscriptionClassHome.class.getName(), home);
        home = new SubscriptionClassNoRelationshipRemoveHome(ctx, home);
		home =
		    ConfigChangeRequestSupportHelper.get(ctx)
		        .registerHomeForConfigSharing(ctx, home,
		            SubscriptionClass.class);
        
        return home;
    }
}
