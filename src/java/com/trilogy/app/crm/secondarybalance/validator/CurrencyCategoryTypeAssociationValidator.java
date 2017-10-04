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
import com.trilogy.app.crm.bundle.CategoryAssociationTypeEnum;
import com.trilogy.app.crm.defaultvalue.IntValue;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Validate if this Bundle Profile has a category association of type Currency. 
 * 
 * Since currency category association has multiple bundle categories, we need to ensure that 
 * only one category association exists.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CurrencyCategoryTypeAssociationValidator implements Validator 
{
	
	public static final String MESSAGE = "Bundle(s) of Bundle Category having unit type Secondary Balance can only have " +
			" category association of type Currency.";
	
	public static final String SINGLE_ASSOCIATION_MESSAGE = "Bundle(s) of Bundle Category having unit type Secondary Balance " +
			"can only have a single Category Association.";
	
	public CurrencyCategoryTypeAssociationValidator(String message)
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
			if(!CategoryAssociationTypeEnum.CURRENCY.equals(bundleProfile.getAssociationType()))
			{
				validationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.ASSOCIATION_TYPE, message_));
			}
			
			if(bundleProfile.getBundleCategories(ctx).size() > 1)
			{
				validationException.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.ASSOCIATION_TYPE, SINGLE_ASSOCIATION_MESSAGE));
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
