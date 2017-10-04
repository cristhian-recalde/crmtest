/*
 * Created on May 20, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.validator;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public class StartEndDateValidator implements Validator
{
	protected static StartEndDateValidator instance__;
	
	private StartEndDateValidator()
	{
		
	}
	
	public static StartEndDateValidator instance()
	{
		if(instance__==null)
		{
			instance__=new StartEndDateValidator();
		}
		
		return instance__;
	}

	public void validate(Context ctx, Object obj) throws IllegalStateException
	{
		Subscriber sub=(Subscriber) obj;
		
		if(sub==null || sub.getStartDate()==null || sub.getEndDate()==null)
		{
			throw new IllegalStateException("Activation and Deactivation dates cannot be empty.");
		}
	}

}
