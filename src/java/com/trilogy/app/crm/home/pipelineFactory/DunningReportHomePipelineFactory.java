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

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.home.DunningReportRecordsUpdateHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.app.crm.validator.DunningReportRecordsValidator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xhome.msp.SpidAwareHome;


/**
 * Pipeline factory for the DunningReportHome.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
    	 LogSupport.info(ctx, this, "Installing the Dunning Report home ");
         Home home = StorageSupportHelper.get(ctx).createHome(ctx, DunningReport.class, "DUNNINGREPORT");
         home = new SpidAwareHome(ctx, home);
         home = new DunningReportRecordsUpdateHome(ctx, home);
         home = new ValidatingHome(new DunningReportRecordsValidator(), home);
         LogSupport.info(ctx, this, "Dunning Report Home installed succesfully");
         return home;
    }
}