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
package com.trilogy.app.crm.home.pipelineFactory.ui;

import java.io.IOException;

import com.trilogy.app.crm.bean.ui.AuxiliaryServiceUIAdapter;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.xhome.adapter.BeanAdapter;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Adapts the auxiliary service home into UI version of home
 * 
 * @author aaron.gourley@redknee.com
 * 
 */
public class AuxiliaryServiceUIHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the auxiliary service UI home ");

        Home home = new ContextRedirectingHome(ctx, com.redknee.app.crm.bean.AuxiliaryServiceHome.class);

        home = new AdapterHome(ctx, home, new AuxiliaryServiceUIAdapter(ctx));
        return home;
    }
}
