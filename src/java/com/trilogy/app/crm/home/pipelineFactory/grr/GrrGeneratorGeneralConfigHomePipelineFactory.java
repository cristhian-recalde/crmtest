/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright  Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.pipelineFactory.grr;

import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.XSDVersionConfig;
import com.trilogy.app.crm.home.grr.XSDVersionConfigCheckHome;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;

/**
 * 
 * @author ishan.batra
 *
 */
public class GrrGeneratorGeneralConfigHomePipelineFactory implements PipelineFactory
{
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.home.PipelineFactory#createPipeline(com.redknee.framework.xhome.context.Context, com.redknee.framework.xhome.context.Context)
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException
            
    {

        Home home = CoreSupport.bindHome(ctx, GrrGeneratorGeneralConfig.class);
        
        home = new SortingHome(ctx,home);

        return home;
    }
}
