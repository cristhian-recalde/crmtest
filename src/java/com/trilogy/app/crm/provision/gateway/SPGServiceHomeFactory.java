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
package com.trilogy.app.crm.provision.gateway;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.home.PipelineFactory;


/**
 * 
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class SPGServiceHomeFactory implements PipelineFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Home createPipeline(final Context ctx, final Context serverCtx)
    {
        final Home home = CoreSupport.bindHome(ctx, SPGService.class, true);

        return home;
    }

}
