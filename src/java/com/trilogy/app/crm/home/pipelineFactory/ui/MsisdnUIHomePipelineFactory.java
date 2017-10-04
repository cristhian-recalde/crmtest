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

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.SharedBeanGenericSelectionHome;
import com.trilogy.app.crm.home.SharedBeanSpidOverridingHome;
import com.trilogy.app.crm.home.SubscriberTypeOverridingHome;
import com.trilogy.app.crm.xhome.adapter.BeanAdapter;
import com.trilogy.app.crm.xhome.home.ContextRedirectingHome;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Adapts the serviceHome inot ServiceUIHome
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 * 
 */
public class MsisdnUIHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the msisdn UI home ");
        
        Home home = new ContextRedirectingHome(ctx, com.redknee.app.crm.bean.MsisdnHome.class);
        home = new AdapterHome(
                ctx, home, 
                new BeanAdapter<com.redknee.app.crm.bean.core.Msisdn, com.redknee.app.crm.bean.ui.Msisdn>(
                        com.redknee.app.crm.bean.core.Msisdn.class, 
                        com.redknee.app.crm.bean.ui.Msisdn.class));
        home = new SubscriberTypeOverridingHome(ctx, home);
        home = new SharedBeanSpidOverridingHome<com.redknee.app.crm.bean.ui.Msisdn>(ctx, home);
        home = new NoSelectAllHome(home);
        
        return home;
    }
}
