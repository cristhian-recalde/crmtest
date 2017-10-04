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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.home.GroupScreeningTemplateModifyHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.subscriber.GroupScreeningTemplateIdentifierSettingHome;
import com.trilogy.app.crm.support.ConfigChangeRequestSupportHelper;
import com.trilogy.app.crm.support.StorageSupportHelper;

/**
 * 
 * @author Ankit Nagpal
 * @since 9.0
 *
 */
public class GroupScreeningTemplateHomePipelineFactory implements PipelineFactory
{

    /**
     * {@inheritDoc}
     */
    public Home createPipeline(final Context ctx, final Context serverCtx) throws HomeException, IOException,
            AgentException
    {
        LogSupport.info(ctx, this, "Installing the Group Screening template home ");
        Home home = StorageSupportHelper.get(ctx).createHome(ctx, GroupScreeningTemplate.class, "GroupScreeningTemplate");
        home = new SpidAwareHome(ctx, home);
        home = new GroupScreeningTemplateModifyHome(ctx, home);
        home = new GroupScreeningTemplateIdentifierSettingHome(ctx, home);
        home =
    		    ConfigChangeRequestSupportHelper.get(ctx)
    		        .registerHomeForConfigSharing(ctx, home, GroupScreeningTemplate.class);
        
        
        return home;
    }
        
}
