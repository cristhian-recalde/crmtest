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
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionHolder;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public abstract class AbstractExtensionFetcher implements ExtensionFetcher
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	protected abstract String getCacheKey();

	/**
	 * @param context
	 * @param account
	 * @return
	 * @see com.redknee.app.crm.paymentprocessing.ExtensionFetcher#getExtension(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.bean.Account)
	 */
	@Override
	public CreditCategoryExtension
	    getExtension(Context context, Account account)
	{
		Home ccHome = (Home) context.get(CreditCategoryHome.class);
		CreditCategory creditCategory =
		    (CreditCategory) context.get(CREDIT_CATEGORY_KEY);

		if (creditCategory == null)
		{
			try
			{
				creditCategory =
				    (CreditCategory) ccHome.find(context, new EQ(
				        CreditCategoryXInfo.CODE, account.getCreditCategory()));
				if (creditCategory == null)
				{
					LogSupport.minor(context, this,
					    "Credit category of account " + account.getBAN()
					        + " cannot be found");
				}
			}
			catch (HomeException exception)
			{
				LogSupport.minor(context, this,
				    "Exception caught while looking up credit category for account "
				        + account.getBAN(), exception);
			}
		}
		if (creditCategory == null)
		{
			return null;
		}

		Extension ext = retrieveCachedExtension(context);
		if (ext == null)
		{
			for (final Object o : creditCategory.getCreditCategoryExtensions())
			{
				CreditCategoryExtensionHolder holder =
				    (CreditCategoryExtensionHolder) o;
				ext = holder.getExtension();
				if (isValidExtension(context, ext))
				{
					return (CreditCategoryExtension) ext;
				}
			}
			return null;
		}
		return (CreditCategoryExtension) ext;
	}

	@Override
	public void cacheExtension(Context context, Extension extension)
	{
		context.put(getCacheKey(), extension);
	}

	@Override
    public Extension retrieveCachedExtension(Context context)
	{
		return (Extension) context.get(getCacheKey());
	}
}
