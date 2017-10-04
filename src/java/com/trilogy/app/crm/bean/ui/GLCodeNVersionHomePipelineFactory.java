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

package com.trilogy.app.crm.bean.ui;
import com.trilogy.app.crm.home.core.GLCodeAdapterHome;
import com.trilogy.app.crm.bean.GLCodeVersionN;

import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.app.crm.home.GLCodeVersionNHomeProxy;
import com.trilogy.app.crm.support.StorageSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author bhushan.deshmukh@redknee.com
 * @since 10.3.6
 */


public class GLCodeNVersionHomePipelineFactory implements PipelineFactory {

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
	{
		Home glCodeNHome = StorageSupportHelper.get(ctx).createHome(ctx,GLCodeVersionN.class, "GLCodeVersionN");
		LogSupport.debug(ctx, this, "FINDME: Installing the GLCodeNVersion");
		//glCodeNHome = new GLCodeNSettingHome(ctx, glCodeNHome);
		LogSupport.debug(ctx, this, "FINDME: Installeds the GLCodeNVersion");
		LogSupport.debug(ctx, this, "FINDME: Installing the GLCodeAdapterHome");
		glCodeNHome = new GLCodeAdapterHome(ctx, glCodeNHome); 
		glCodeNHome = new GLCodeVersionNHomeProxy(ctx, glCodeNHome);
        return glCodeNHome;	
	
		
	}

}
