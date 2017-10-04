/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.dunning.LevelInfoXMLHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.validator.LevelInfoIdValidator;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;

public class LevelInfoHomePipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>SubscriptionContractPipeLineFactory</code>.
     */
    protected LevelInfoHomePipelineFactory()
    {
        // TODO
    }


    /**
     * Returns an instance of <code>SubscriptionContractPipeLineFactory</code>.
     * 
     * @return An instance of <code>SubscriptionContractPipeLineFactory</code>.
     */
    public static LevelInfoHomePipelineFactory instance()
    {
        if (instance == null)
        {
            instance = new LevelInfoHomePipelineFactory();
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
        Home home = new LevelInfoXMLHome(ctx, CoreSupport.getFile(ctx,"LevelInfoConfig.xml"));
        home = new ValidatingHome(new LevelInfoIdValidator(), home);
        home = new LevelInfoRemoveHome(ctx,home);
        return home;
    }

    /**
     * Singleton instance.
     */
    private static LevelInfoHomePipelineFactory instance;
}
