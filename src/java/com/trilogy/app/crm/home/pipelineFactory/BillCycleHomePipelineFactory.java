/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.pipelineFactory;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.billcycle.BillCycleCheckHome;
import com.trilogy.app.crm.billcycle.BillCycleValidator;
import com.trilogy.app.crm.home.BillCycleERLogHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.xhome.beans.CompoundValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NotifyingHome;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class BillCycleHomePipelineFactory implements PipelineFactory
{

    /**
     * Singleton instance.
     */
    private static BillCycleHomePipelineFactory instance_;


    /**
     * Create a new instance of <code>CoreBillCyclePipelineFactory</code>.
     */
    protected BillCycleHomePipelineFactory()
    {
        // empty
    }


    /**
     * Returns an instance of <code>CoreBillCyclePipelineFactory</code>.
     * 
     * @return An instance of <code>CoreBillCyclePipelineFactory</code>.
     */
    public static PipelineFactory instance()
    {
        if (instance_ == null)
        {
            instance_ = new BillCycleHomePipelineFactory();
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
        // This bindHome call will return the home pipeline created by CoreBillCycleHomePipelineFactory
        Home home = (Home) ctx.get(BillCycleHome.class);
        
        home = new NotifyingHome(home);
        
        home = new BillCycleCheckHome(ctx, home);
        
        home = new SpidAwareHome(ctx, home);
        
        home = new BillCycleERLogHome( home);
       
        final CompoundValidator validators = new CompoundValidator();
        validators.add(new BillCycleValidator());
        home = new ValidatingHome(validators, home);
        return home;
    }

}
