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
package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingHome;
import com.trilogy.app.crm.bean.GLCodeMappingID;

/**
 * @author amedina
 */
public class GLCodeUtil 
{
	/**
	 * @param ctx
	 * @param adjustmentGLCode
	 * @return
	 */
	public static GLCodeMapping getGLCodeMapping(Context ctx, String adjustmentGLCode, Object source, int spid) 
	{
		GLCodeMapping map = null;
		Home home = (Home) ctx.get(GLCodeMappingHome.class);
		
		if (home != null)
		{
			try 
			{
				if (adjustmentGLCode != null)
				{
					map = (GLCodeMapping)home.find(ctx, new GLCodeMappingID(spid, adjustmentGLCode));
				}
			}
			catch (HomeException e)
			{
				LogSupport.crit(ctx,source,"Home Exception: ", e);
			}
		}
		return map;
	}

}
