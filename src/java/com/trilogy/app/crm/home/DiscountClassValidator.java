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

import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Validator for discount class
 * 
 * @author ankit.nagpal@redknee.com
 * since 9_7_2
 */

public class DiscountClassValidator implements Validator
{
	protected static DiscountClassValidator instance__=null;
	
	public static DiscountClassValidator instance()
	{
		if(instance__==null)
		{
			instance__=new DiscountClassValidator();
		}
		
		return instance__;
	}
	
   /**
    * check to see if the service provider selected has any Bill Cycle
    */
	public void validate(Context ctx, Object obj) throws IllegalStateException {
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		DiscountClass discountClass = (DiscountClass) obj;
		if (discountClass != null
				&& discountClass.getServiceLevelDiscount().size() > 1) {
			el.thrown(new IllegalPropertyArgumentException(
					DiscountClassXInfo.SERVICE_LEVEL_DISCOUNT,
					"Cannot add more than one template"));
		}
		el.throwAll();
	}
}
