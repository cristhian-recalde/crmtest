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
package com.trilogy.app.crm.home.pipelineFactory.ui;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.ui.ChargingTemplateUIAdapter;
import com.trilogy.app.crm.home.ChargingTemplateAdjustmentTypeMappingSavingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ChargingTemplateUIHomePipelineFactory implements PipelineFactory
{

    @Override
    public Home createPipeline(Context ctx, Context serverCtx) throws RemoteException, HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the charging template UI home ");

        Home home = new ContextRedirectingHome(ctx, com.redknee.app.crm.bean.ChargingTemplateHome.class);
        
        // Add adapter to switch between GUI bean and core beans.
        home = new AdapterHome(ctx, home, new ChargingTemplateUIAdapter(ctx));
        
        home = new ChargingTemplateAdjustmentTypeMappingSavingHome(ctx, home);
        
        return home;
    }
}