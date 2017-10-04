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

import java.io.Serializable;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public interface ExtensionFetcher extends Serializable
{
	final String CREDIT_CATEGORY_KEY = "ExtensionFetcher.CreditCategory";

	CreditCategoryExtension getExtension(Context context, Account account);

	boolean isValidExtension(Context context, Extension extension);

	void cacheExtension(Context context, Extension extension);

	Extension retrieveCachedExtension(Context context);
}
