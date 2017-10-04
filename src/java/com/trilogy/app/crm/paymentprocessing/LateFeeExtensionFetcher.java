/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.paymentprocessing;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtension;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LateFeeExtensionFetcher extends AbstractExtensionFetcher
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LateFeeExtensionFetcher instance =
	    new LateFeeExtensionFetcher();

	public static String CACHE_KEY = "LateFeeExtensionFetcher.extension";

	public static LateFeeExtensionFetcher instance()
	{
		return instance;
	}

	/**
	 * @param extension
	 * @return
	 * @see com.redknee.app.crm.paymentprocessing.ExtensionFetcher#isValidExtension
	 */
	@Override
	public boolean isValidExtension(Context context, Extension extension)
	{
		return extension instanceof LateFeeCreditCategoryExtension;
	}

	@Override
	protected String getCacheKey()
	{
		return CACHE_KEY;
	}

}
