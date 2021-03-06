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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;

import com.trilogy.app.crm.bean.RecurringChargeErrorReport;
import com.trilogy.app.crm.bean.RecurringChargeErrorReportHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * Factory to create <code>RecurringChargeErrorReport</code> pipeline.
 *
 * @author larry.xia@redknee.com
 */
public class RecurringChargeErrorReportPipelineFactory implements PipelineFactory
{

    /**
     * Create a new instance of <code>RecurringChargeErrorReportPipelineFactory</code>.
     */
    public RecurringChargeErrorReportPipelineFactory()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws RemoteException, HomeException,
        IOException
    {
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, RecurringChargeErrorReport.class, "RecurringChargeErrorReport");
        home = new SpidAwareHome(ctx, home);
        home = new NoSelectAllHome(home);
        ctx.put(RecurringChargeErrorReportHome.class, home);
        return home;
    }
}
