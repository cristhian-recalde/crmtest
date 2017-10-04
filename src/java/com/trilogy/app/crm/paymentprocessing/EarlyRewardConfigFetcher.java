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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.EarlyRewardConfigurationHome;
import com.trilogy.app.crm.bean.EarlyRewardExtensionProperty;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.EarlyRewardCreditCategoryExtension;

/**
 * Fetches the correct set of early reward configurations.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class EarlyRewardConfigFetcher extends AbstractConfigFetcher
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final EarlyRewardConfigFetcher instance =
	    new EarlyRewardConfigFetcher();

	public static EarlyRewardConfigFetcher instance()
	{
		return instance;
	}

	/**
	 * Retrieves all the applicable early reward configuration. Unlike late fee,
	 * only one early reward configuration may be applicable, if any.
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
	    populateEarlyRewardConfigurations(Context context,
	        EarlyRewardCreditCategoryExtension extension, Invoice invoice,
	        Date date,
	        SortedMap<Integer, LateFeeEarlyRewardConfiguration> results)
	{
		int maxDaysEarly = -1;
		long configId = -1;
		Calendar calendar = Calendar.getInstance();
		for (final Object obj : extension.getConfigurations().keySet())
		{
			final int deadline = ((Number) obj).intValue();
			if (deadline > maxDaysEarly)
			{
				calendar.setTime(invoice.getDueDate());
				calendar.add(Calendar.DAY_OF_MONTH, -deadline);
				if (!calendar.getTime().before(date))
				{
					configId =
					    ((EarlyRewardExtensionProperty) extension
					        .getConfigurations().get(obj)).getConfiguration();
					maxDaysEarly = deadline;
				}
			}
		}

		LateFeeEarlyRewardConfiguration config =
		    getConfiguration(context, EarlyRewardConfigurationHome.class,
		        configId);

		if (maxDaysEarly >= 0 && config != null)
		{
			results.put(Integer.valueOf(maxDaysEarly), config);
		}

		return results;
	}

	/**
	 * Retrieves all the applicable early reward configurations. Unlike late
	 * fee, only one early reward configuration is applicable at any one time,
	 * if any.
	 * 
	 * @param context
	 *            Operating context.
	 * @param extension
	 *            Early reward credit category extension.
	 * @param invoice
	 *            Invoice to be processed.
	 * @param date
	 *            Date of processing.
	 * @return Map of all applicable early reward configurations.
	 * @see com.redknee.app.crm.paymentprocessing.AbstractConfigFetcher#getConfigurations(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.bean.core.CreditCategory,
	 *      com.redknee.app.crm.bean.Invoice, java.util.Date)
	 */
	@Override
	public SortedMap<Integer, LateFeeEarlyRewardConfiguration>
	    getConfigurations(Context context, Account account,
	        CreditCategoryExtension extension, Invoice invoice, Date date)
	{
		SortedMap<Integer, LateFeeEarlyRewardConfiguration> results =
		    new TreeMap<Integer, LateFeeEarlyRewardConfiguration>();

		populateEarlyRewardConfigurations(context,
		    (EarlyRewardCreditCategoryExtension) extension, invoice, date,
		    results);
		return results;
	}

}
