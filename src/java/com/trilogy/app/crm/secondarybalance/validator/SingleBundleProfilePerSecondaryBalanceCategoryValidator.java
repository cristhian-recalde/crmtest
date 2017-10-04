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
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BMBundleCategoryAssociationXInfo;
import com.trilogy.app.crm.secondarybalance.license.PTUBLicenseSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Validate/check that a single bundle profile should exist for every
 * BundleCategory of unit type Secondary Balance.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class SingleBundleProfilePerSecondaryBalanceCategoryValidator implements
		Validator 
{
	
	public static final String MESSAGE = "Only ONE Bundle allowed in a Category ID of Unit Type=Secondary Balance";

	@Override
	public void validate(Context ctx, Object object)
			throws IllegalStateException 
	{
		HomeOperationEnum homeOperation = (HomeOperationEnum)ctx.get(HomeOperationEnum.class);
		if(HomeOperationEnum.CREATE.equals(homeOperation))
		{
			CompoundIllegalStateException validationException = new CompoundIllegalStateException();
			
			BundleProfile bundle = (BundleProfile)object;
			
			try
			{
				int categoryId = bundle.getBundleCategoryId();
				long numberOfSecondaryBalanceBundles = HomeSupportHelper.get(ctx).getBeanCount(ctx, BMBundleCategoryAssociation.class, 
																			new EQ(BMBundleCategoryAssociationXInfo.CATEGORY_ID, categoryId));
				
				if(PTUBLicenseSupport.getNumberOfSecondaryBalances(ctx) <= numberOfSecondaryBalanceBundles)
				{
					validationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.BUNDLE_CATEGORY_ID, MESSAGE));
				}
			}
			catch(HomeException he)
			{
				 validationException.thrown(he);
			}
			
			validationException.throwAll();
		}
	}

}
