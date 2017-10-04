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

import java.util.Comparator;

import com.trilogy.app.crm.api.queryexecutor.ApiInterface;
import com.trilogy.app.crm.api.queryexecutor.ApiInterfaceTransientHome;
import com.trilogy.app.crm.api.queryexecutor.ApiMethod;
import com.trilogy.app.crm.home.ApiInterfaceHandlingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.xhome.home.TotalCachingHome;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.SortingHome;

/**
 * Creates the service home decorators and put is in the context.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class ApiInterfaceHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx)
    {
        Home home = CoreSupport.bindHome(ctx, ApiInterface.class);
        home = new TotalCachingHome( ctx, new ApiInterfaceTransientHome(ctx), home); 
        home = new ApiInterfaceHandlingHome(ctx, home);
        home = new SortingHome(ctx, home, new Comparator() {

            @Override
            public int compare(Object o1, Object o2)
            {
                ApiInterface interface1 = (ApiInterface) o1;
                ApiInterface interface2 = (ApiInterface) o2;
                return interface1.getName().compareTo(interface2.getName());
            }
        });

        return home;
    }

}
