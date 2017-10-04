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
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.secondarybalance.license.PTUBLicenseSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 * Validates the following licenses - 
 * 
 * <ul>
 * 	<li>Prepaid License
 * 	<li>Postpaid License  
 * </ul>
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */

public class PTUBLicenseValidator implements Validator 
{

	@Override
	public void validate(Context ctx, Object obj)
			throws IllegalStateException 
	{

		BundleProfile bundleProfile = (BundleProfile)obj;
		
		CompoundIllegalStateException licenseValidationException = new CompoundIllegalStateException();
		
		if( !PTUBLicenseSupport.isPrepaidEnabled(ctx) && BundleSegmentEnum.PREPAID.equals( bundleProfile.getSegment()) ) 
		{
			licenseValidationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.SEGMENT, "Please enable " +
					"license to create prepaid secondary balance bundles."));
		}
		
		if( !PTUBLicenseSupport.isPostpaidEnabled(ctx) && BundleSegmentEnum.POSTPAID.equals( bundleProfile.getSegment()) )  
		{
			licenseValidationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.SEGMENT, "Please enable " +
			"license to create postpaid secondary balance bundles."));			
		}
		
		licenseValidationException.throwAll();
	}

}
