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
package com.trilogy.app.crm.integration.pc;

import java.io.IOException;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.bean.ui.TechnicalServiceTemplate;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.service.TechnicalServiceTemplateServiceExtension;
import com.trilogy.app.crm.extension.service.TechnicalServiceTemplateServiceExtensionXInfo;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.SortingHome;
import com.trilogy.framework.xlog.log.LogSupport;

public class TechnicalServiceTemplateHomePipelineFactory implements PipelineFactory
{

	public Home createPipeline(final Context ctx, final Context serverCtx)
        throws HomeException, IOException, AgentException {
		
        LogSupport.debug(ctx, this, "[createPipeline] Installing the Technical Service Template");
        
        Home technicalServiceTemplateHome = StorageSupportHelper.get(ctx).createHome(ctx, TechnicalServiceTemplate.class, "TECHNICALSERVICETEMPLATE");
        technicalServiceTemplateHome = new TechnicalServiceNameCheckingHome(ctx, technicalServiceTemplateHome);
        technicalServiceTemplateHome  = new TechnicalServiceTemplateSettingHome(ctx, technicalServiceTemplateHome);
        
        technicalServiceTemplateHome  = new TechnicalServiceCompSpecCheckingHome(ctx, technicalServiceTemplateHome);
		technicalServiceTemplateHome = new ExtensionHandlingHome<TechnicalServiceTemplateServiceExtension>(
                ctx, TechnicalServiceTemplateServiceExtension.class, TechnicalServiceTemplateServiceExtensionXInfo.ID,technicalServiceTemplateHome);
        
        technicalServiceTemplateHome = new AdapterHome(technicalServiceTemplateHome,
        		new ExtensionForeignKeyAdapter(TechnicalServiceTemplateServiceExtensionXInfo.ID));
        
        technicalServiceTemplateHome = new SortingHome(technicalServiceTemplateHome);
				
        return technicalServiceTemplateHome ;
    }
}
