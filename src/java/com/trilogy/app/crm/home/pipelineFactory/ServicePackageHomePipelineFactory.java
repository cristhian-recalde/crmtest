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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.home.ServicePackageAdjustmentTypeCreationHome;
import com.trilogy.app.crm.home.ServicePackagePricePlanRemoveHome;
import com.trilogy.app.crm.home.core.CoreServicePackageHomePipelineFactory;
import com.trilogy.app.crm.sequenceId.IdentifierSettingHome;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class ServicePackageHomePipelineFactory extends CoreServicePackageHomePipelineFactory
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        Home home = super.createPipeline(ctx, serverCtx);
        
        home = new ServicePackageAdjustmentTypeCreationHome(home);
        
        home = new IdentifierSettingHome(ctx, home, IdentifierEnum.SERVICEPACKAGE_ID, null);
        
        home = new ServicePackagePricePlanRemoveHome(home);
        
        return home;
    }

}
