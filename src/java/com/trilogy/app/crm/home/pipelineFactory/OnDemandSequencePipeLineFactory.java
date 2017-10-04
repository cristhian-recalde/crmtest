package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.OnDemandSequence;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.sequenceId.OnDemandSequenceResettingHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


public class OnDemandSequencePipeLineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>OnDemandSequencePipeLineFactory</code>.
     */
    protected OnDemandSequencePipeLineFactory()
    {
        // TODO
    }


    /**
     * Returns an instance of <code>OnDemandSequencePipeLineFactory</code>.
     * 
     * @return An instance of <code>OnDemandSequencePipeLineFactory</code>.
     */
    public static OnDemandSequencePipeLineFactory instance()
    {
        if (instance == null)
        {
            instance = new OnDemandSequencePipeLineFactory();
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
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, OnDemandSequence.class, "ONDEMANDSEQUENCE");
        home = new OnDemandSequenceResettingHome(home);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static OnDemandSequencePipeLineFactory instance;
}
