/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.paymentprocessing;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.support.LicensingSupportHelper;

/**
 * Agent to process late fee.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.4
 */
public class LateFeeAgent extends InvoicePaymentProcessingAgent
{

	/**
	 * Returns whether the license is enabled.
	 * 
	 * @param ctx
	 *            Operating context.
	 * @return Whether the on-time payment license is enabled.
	 */
	@Override
	protected boolean isLicenseEnabled(final Context ctx)
	{
		return LicensingSupportHelper.get(ctx).isLicensed(ctx,
		    LicenseConstants.LATE_FEE_LICENSE);
	}

	/**
	 * @param ctx
	 * @return
	 * @see com.redknee.app.crm.paymentprocessing.InvoicePaymentProcessingAgent#getAccountProcessor(com.redknee.framework.xhome.context.Context)
	 */
	@Override
	protected LateFeeEarlyRewardAccountProcessor
	    getAccountProcessor(Context ctx)
	{
		return LateFeeEarlyRewardAccountProcessor.getGenerateLateFeeInstance();
	}
}
