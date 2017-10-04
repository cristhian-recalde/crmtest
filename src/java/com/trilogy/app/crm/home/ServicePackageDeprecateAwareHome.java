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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.UsageTypeStateEnum;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.WhereHome;

/**
 * Filters the home to return only non-deprecated Service Packages
 * @author psperneac
 *
 */
public class ServicePackageDeprecateAwareHome extends WhereHome 
{
	/**
	 * Only constructor
	 * @param ctx
	 * @param delegate
	 */
	public ServicePackageDeprecateAwareHome(Context ctx, Home delegate)
	{
		super(ctx,delegate);
	}

	
   
	public Object getWhere(Context ctx)
	{
		return new Predicate()
		{
			public boolean f(Context ctx, Object obj)
	        {
	        	ServicePackage type = (ServicePackage) obj;
	        	return type.getState() == ServicePackageStateEnum.ACTIVE_INDEX;
	        }

	      };
	   }

}
