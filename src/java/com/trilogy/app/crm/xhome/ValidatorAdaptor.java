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
package com.trilogy.app.crm.xhome;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

/**
 * @author jchen
 *
 * Validator interface is a little bit in favor of bean validation.
 * Many times we have different validation logic for home methods, create/store/remove
 * 
 * 
 */
public class ValidatorAdaptor
		implements Validator
{
	
	public ValidatorAdaptor(HomeValidator homeValidator)
	{
		homeValidator_ = homeValidator; 
	}

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public void validate(Context ctx, Object obj) throws IllegalStateException 
	{
		if (homeValidator_ != null)
		{
			int homeOp = ctx.getInt("MODE", AbstractWebControl.DISPLAY_MODE);
			
			
			if (homeOp == AbstractWebControl.CREATE_MODE)
			{
				homeValidator_.validateCreate(ctx, obj);
			}
			else
			{
				//FIXME, how to tell a store method
				//homeValidator_.validateStore(ctx, obj);
			}
			//FIXME, how to tell it for remove method
			{
				//homeValidator_.validateRemove(ctx, obj);
			}
			
		}
			
	}
	
	private HomeValidator homeValidator_ = null;
}
