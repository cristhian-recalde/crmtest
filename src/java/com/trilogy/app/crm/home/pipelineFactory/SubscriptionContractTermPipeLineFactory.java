package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.billing.message.BillingMessageAwareHomeDecorator;
import com.trilogy.app.crm.contract.ContractTermAdjustmentTypeCreationHome;
import com.trilogy.app.crm.contract.ContractTermValidator;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermHome;
import com.trilogy.app.crm.contract.SubscriptionContractTermIdentifierSettingHome;
import com.trilogy.app.crm.contract.SubscriptionContractTermTransientHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.xhome.home.ConfigShareTotalCachingHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;


public class SubscriptionContractTermPipeLineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>SubscriptionContractTermPipeLineFactory</code>.
     */
    protected SubscriptionContractTermPipeLineFactory()
    {
        // TODO
    }


    /**
     * Returns an instance of <code>SubscriptionContractTermPipeLineFactory</code>.
     * 
     * @return An instance of <code>SubscriptionContractTermPipeLineFactory</code>.
     */
    public static SubscriptionContractTermPipeLineFactory instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionContractTermPipeLineFactory();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
            IOException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriptionContractTerm.class, "CONTRACTTERM");
        
        // Note: TotalCachingHome automatically registers the home for config sharing, so no need to do it explicitly
        home = new ConfigShareTotalCachingHome(ctx, new SubscriptionContractTermTransientHome(ctx), home);
        
        home = new ContractTermAdjustmentTypeCreationHome(ctx,home);
        home = new SubscriptionContractTermIdentifierSettingHome(home); 
        home = new BillingMessageAwareHomeDecorator().decorateHome(ctx, home);
        home = new ValidatingHome(new ContractTermValidator(), home);
        
        ctx.put(SubscriptionContractTermHome.class, home);
        
        return home;
    }

    /**
     * Singleton instance.
     */
    private static SubscriptionContractTermPipeLineFactory instance;
}
