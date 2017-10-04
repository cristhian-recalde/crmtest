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

import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.app.crm.home.ChargingTemplateRemovalValidator;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.ScreeningTemplateServiceHome;
import com.trilogy.app.crm.home.validator.RemovalValidatingHome;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AuditJournalHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.ReadOnlyHome;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author Marcio Marques
 * @since 8.5
 *
 */
public class ScreeningTemplateHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the Screening template home ");
        Home home = new ScreeningTemplateServiceHome(ctx);
        home = new ReadOnlyHome(ctx, home);
        home = new SortingHome(ctx, home);
        LogSupport.info(ctx, this, "Screening Template Home installed successfully");
        return home;
    }
        
}
