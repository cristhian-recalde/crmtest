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

import java.util.Calendar;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.EarlyRewardConfiguration;
import com.trilogy.app.crm.bean.EarlyRewardConfigurationHome;
import com.trilogy.app.crm.bean.EarlyRewardExtensionProperty;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeConfigurationHome;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.bean.LateFeeExtensionProperty;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtensionHolder;
import com.trilogy.app.crm.extension.creditcategory.EarlyRewardCreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtension;

/**
 * Fetches the correct set of late fee configurations.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LateFeeConfigFetcher extends AbstractConfigFetcher
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LateFeeConfigFetcher instance =
	    new LateFeeConfigFetcher();

	public static LateFeeConfigFetcher instance()
	{
		return instance;
	}

	/**
	 * Retrieves all the applicable late fee configurations.
	 * 
	 * @param context
	 *            Operating context.
	 * @param extension
	 *            Late Fee extension.
	 * @param invoice
	 *            Invoice being processed.
	 * @param date
	 *            Date being processed.
	 * @param results
	 *            Result map.
	 * @return Result map.
	 */
	protected SortedMap<Integer, LateFeeEarlyRewardConfiguration>
	    populateLateFeeConfigurations(Context context,
	        LateFeeCreditCategoryExtension extension, Invoice invoice,
	        Date date,
	        SortedMap<Integer, LateFeeEarlyRewardConfiguration> results)
	{
		Calendar calendar = Calendar.getInstance();
		for (final Object obj : extension.getConfigurations().keySet())
		{
			final int deadline = ((Number) obj).intValue();
			calendar.setTime(invoice.getDueDate());
			calendar.add(Calendar.DAY_OF_MONTH, deadline);
			if (!date.before(calendar.getTime()))
			{
				LateFeeEarlyRewardConfiguration config =
				    getConfiguration(context, LateFeeConfigurationHome.class,
				        ((LateFeeExtensionProperty) extension
				            .getConfigurations().get(obj)).getConfiguration());
				if (config != null)
				{
					results.put(deadline, config);
				}
			}
		}
		return results;
	}

	/**
	 * Returns the applicable late fee configurations.
	 * 
	 * @param context
	 *            Operating context.
	 * @param account
	 *            Account to be processed.
	 * @param extension
	 *            Late fee extension.
	 * @param invoice
	 *            Invoice to be processed.
	 * @param date
	 *            Date to be processed.
	 * @return All applicable late fee configurations.
	 * @see com.redknee.app.crm.paymentprocessing.ConfigFetcher#getConfigurations(Context,
	 *      Account, CreditCategoryExtension, Invoice, Date)
	 */
	public SortedMap<Integer, LateFeeEarlyRewardConfiguration>
	    getConfigurations(Context context, Account account,
	        CreditCategoryExtension extension, Invoice invoice, Date date)
	{
		SortedMap<Integer, LateFeeEarlyRewardConfiguration> results =
		    new TreeMap<Integer, LateFeeEarlyRewardConfiguration>();

		populateLateFeeConfigurations(context,
		    (LateFeeCreditCategoryExtension) extension, invoice, date, results);

		return results;
	}

}
