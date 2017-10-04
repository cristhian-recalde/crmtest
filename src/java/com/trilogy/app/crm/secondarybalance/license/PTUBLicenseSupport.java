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

package com.trilogy.app.crm.secondarybalance.license;

import com.trilogy.app.crm.defaultvalue.BooleanValue;
import com.trilogy.app.crm.defaultvalue.IntValue;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Support class for PTUB Licensing.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class PTUBLicenseSupport 
{

	public static final String PREPAID_LICENSE = "Prepaid Airtime Secondary Balance";
	public static final String POSTPAID_LICENSE = "Postpaid Airtime Secondary Balance";
	public static final String QUANTITY_LICENSE = "Number Of Secondary Balance Profiles";
	
	/**
	 * 
	 * Check if prepaid subscriptions are licensed to use Secondary Balance profiles.
	 * 
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isPrepaidEnabled(Context ctx)
	{
		LicenseMgr licenseManager = (LicenseMgr) ctx.get(LicenseMgr.class);
		
		if(licenseManager == null || !licenseManager.isLicensed(ctx, PREPAID_LICENSE))
		{
			return BooleanValue.FALSE;
		}
		else
		{
			return BooleanValue.TRUE;
		}
	}
	
	/**
	 * 
	 * Check if postpaid subscriptions are licensed to use Secondary Balance profiles.
	 * 
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isPostpaidEnabled(Context ctx)
	{
		LicenseMgr licenseManager = (LicenseMgr) ctx.get(LicenseMgr.class);
		
		if(licenseManager == null || !licenseManager.isLicensed(ctx, POSTPAID_LICENSE))
		{
			return BooleanValue.FALSE;
		}
		else
		{
			return BooleanValue.TRUE;
		}
	}
	
	/**
	 * 
	 * Returns the number of Secondary Balance profiles a subscription may have.
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getNumberOfSecondaryBalances(Context ctx)
	{
		LicenseMgr licenseManager = (LicenseMgr) ctx.get(LicenseMgr.class);
		
		if(licenseManager == null || !licenseManager.isLicensed(ctx, QUANTITY_LICENSE))
		{
			return IntValue.ZERO;
		}
		else
		{
			return (int)licenseManager.quantityLimit(ctx, QUANTITY_LICENSE);
		}
	} 
}
