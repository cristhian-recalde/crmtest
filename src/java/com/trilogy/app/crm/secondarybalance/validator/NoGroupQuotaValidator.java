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

package com.trilogy.app.crm.secondarybalance.validator;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Validate that a bundle profiles does not have group quota. In other words, it has Group Charging scheme as
 * 'Group Not Supported'.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class NoGroupQuotaValidator implements Validator 
{
	
	public static final String MESSAGE = "Bundle(s) of Bundle Category having unit type Secondary Balance can only have " + 
					" Group Charging Schema as 'Group Not Supported'";
	
	public NoGroupQuotaValidator(String message)
	{
		message_ = message;
	}

	@Override
	public void validate(Context ctx, Object object) throws IllegalStateException 
	{
		CompoundIllegalStateException validationException = new CompoundIllegalStateException();
		
		BundleProfile bundleProfile = (BundleProfile)object;
		
		try
		{
			if(!GroupChargingTypeEnum.GROUP_NOT_SUPPORTED.equals(bundleProfile.getGroupChargingScheme()))
			{
				throw new IllegalPropertyArgumentException(BundleProfileXInfo.GROUP_CHARGING_SCHEME, message_);
			}
		}
		catch(Exception e)
		{
			validationException.thrown(e);
		}
		
		validationException.throwAll();
	}
	
	private String message_ = null;

}
