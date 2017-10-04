package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bulkprovisioning.PRBTBulkProvisioningConfig;
import com.trilogy.app.crm.home.PipelineFactory;


public class PRBTBulkProvisioningConfigPipeLineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>PRBTBulkProvisioningConfigPipeLineFactory</code>.
     */
    protected PRBTBulkProvisioningConfigPipeLineFactory()
    {
        // TODO
    }


    /**
     * Returns an instance of <code>PRBTBulkProvisioningConfigPipeLineFactory</code>.
     * 
     * @return An instance of <code>PRBTBulkProvisioningConfigPipeLineFactory</code>.
     */
    public static PRBTBulkProvisioningConfigPipeLineFactory instance()
    {
        if (instance == null)
        {
            instance = new PRBTBulkProvisioningConfigPipeLineFactory();
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
        Home home = CoreSupport.bindHome(ctx, PRBTBulkProvisioningConfig.class);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static PRBTBulkProvisioningConfigPipeLineFactory instance;
}
