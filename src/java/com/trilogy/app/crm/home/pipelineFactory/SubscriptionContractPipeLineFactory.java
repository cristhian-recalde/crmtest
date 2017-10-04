package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.contract.EarlySubscriptionContractTerminationHome;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractChargingHome;
import com.trilogy.app.crm.contract.SubscriptionContractHistory;
import com.trilogy.app.crm.contract.SubscriptionContractHistoryHome;
import com.trilogy.app.crm.contract.SubscriptionContractHome;
import com.trilogy.app.crm.contract.SubscriptionContractHomeNoteHome;
import com.trilogy.app.crm.contract.SubscriptionContractPipeLinePrepareHome;
import com.trilogy.app.crm.contract.SubscriptionContractTerm;
import com.trilogy.app.crm.contract.SubscriptionContractTermXInfo;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SubscriptionContractHistoryIDSettingHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;


public class SubscriptionContractPipeLineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>SubscriptionContractPipeLineFactory</code>.
     */
    protected SubscriptionContractPipeLineFactory()
    {
        // TODO
    }


    /**
     * Returns an instance of <code>SubscriptionContractPipeLineFactory</code>.
     * 
     * @return An instance of <code>SubscriptionContractPipeLineFactory</code>.
     */
    public static SubscriptionContractPipeLineFactory instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionContractPipeLineFactory();
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
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, SubscriptionContract.class, "SubscriptionContract");
        home = new SubscriptionContractHomeNoteHome(home);
        home = new SubscriptionContractChargingHome(ctx, home);
        home = new EarlySubscriptionContractTerminationHome(ctx, home);
        home = new ValidatingHome(home, new Validator()
        {

            @Override
            public void validate(Context subCtx, Object obj) throws IllegalStateException
            {
                SubscriptionContract contract = (SubscriptionContract) obj;
                try
                {
                    SubscriptionContractTerm term = HomeSupportHelper.get(subCtx).findBean(subCtx,
                            SubscriptionContractTerm.class,
                            new EQ(SubscriptionContractTermXInfo.ID, contract.getContractId()));
                    if (term.isDisable())
                    {
                        throw new IllegalStateException("Contract Term is depreicated.  Please choose another contract");
                    }
                }
                catch (HomeException homeEx)
                {
                    throw new IllegalStateException("Unable to load the contract selected.");
                }
            }
        });
        home = new SubscriptionContractPipeLinePrepareHome(home);
        ctx.put(SubscriptionContractHome.class, home);
        Home historyHome = StorageSupportHelper.get(ctx).createHome(ctx, SubscriptionContractHistory.class, "SubscriptionContractHistory");
        historyHome = new SubscriptionContractHistoryIDSettingHome(ctx, historyHome);
        ctx.put(SubscriptionContractHistoryHome.class, historyHome);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static SubscriptionContractPipeLineFactory instance;
}
