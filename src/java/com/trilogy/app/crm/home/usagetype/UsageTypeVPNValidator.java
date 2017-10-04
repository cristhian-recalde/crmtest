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
package com.trilogy.app.crm.home.usagetype;

import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.UsageTypeHome;
import com.trilogy.app.crm.bean.UsageTypeXInfo;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author amedina
 *
 * Validates is the particular UsageType is VPN and there is no 
 */
public class UsageTypeVPNValidator implements Validator 
{

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		UsageType current = (UsageType) obj;
		
		if (current.isIsVPN())
		{
			Home usageTypeHome = (Home) ctx.get(UsageTypeHome.class);
			
			if (usageTypeHome  != null)
			{
				UsageType usage;
				try 
				{
					usage = (UsageType)usageTypeHome.find(ctx,new EQ(UsageTypeXInfo.IS_VPN, Boolean.TRUE));
					if (usage != null)
					{
						throw new IllegalStateException("A VPN Usage Type is already registered in the system");
					}
				}
				catch (HomeException e) 
				{
					LogSupport.major(ctx,this,"Home Exception when trying to get the Usage Type" + e.getMessage(), e);
					IllegalStateException newException = new IllegalStateException("Home Exception when trying to get the Usage Type" + e.getMessage());
					
					newException.initCause(e);
					
					throw newException;
				}
			}
		}
	}

}
