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

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.ui.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Validate that this bundle profile is a one time bundle
 * which will never expire.
 * 
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class NeverExpireOneTimeBundleValidator implements Validator 
{
	public static final String OT_MESSAGE = "Bundle(s) of Bundle Category having unit type Secondary Balance can only have " +
			"Charging Recurrence Scheme as One Time";
	public static final String NEVER_EXPIRE_MESSAGE = "Bundle(s) of Bundle Category having unit type Secondary Balance can only have " +
			"Expiry Scheme as Never Expire";

	public NeverExpireOneTimeBundleValidator(String otMessage, String neverExpireMessage)
	{
		otMessage_ = otMessage;
		neverExpireMessage_ = neverExpireMessage;
	}
	
	@Override
	public void validate(Context ctx, Object object)
			throws IllegalStateException 
	{
		CompoundIllegalStateException validationException = new CompoundIllegalStateException();
		
		BundleProfile bundleProfile = (BundleProfile)object;
		
		try
		{
			if(!ServicePeriodEnum.ONE_TIME.equals(bundleProfile.getChargingRecurrenceScheme()))
			{
				validationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.CHARGING_RECURRENCE_SCHEME, otMessage_));
			}
			
			if(!ExpiryTypeEnum.NEVER_EXPIRE.equals(bundleProfile.getExpiryScheme()))
			{
				validationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.EXPIRY_SCHEME, neverExpireMessage_));
			}
		}
		catch(Exception e)
		{
			validationException.thrown(e);
		}
		
		validationException.throwAll();
	}
	
	private String otMessage_ = null;
	private String neverExpireMessage_ = null;

}
