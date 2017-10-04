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

import java.util.SortedSet;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;

/**
 * This processor sums the amounts and returns the total.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class SumsResultsProcessor implements ResultsProcessor
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final SumsResultsProcessor instance =
	    new SumsResultsProcessor();

	public static SumsResultsProcessor instance()
	{
		return instance;
	}

	/**
	 * Sums all the amounts.
	 * 
	 * @param context
	 *            Operating context.
	 * @param account
	 *            Account to be processed.
	 * @param results
	 *            Results to be processed.
	 * @return Sum of all the amounts.
	 * @see com.redknee.app.crm.paymentprocessing.ResultsProcessor#processResults(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.bean.Account, com.redknee.app.crm.bean.Invoice,
	 *      java.util.Date, java.util.SortedSet)
	 */
	@Override
	public Object processResults(Context context, Account account,
	    SortedSet<LateFeeEarlyReward> results)
	{
		long result = 0;
		if (results != null)
		{
			for (LateFeeEarlyReward elem : results)
			{
				if (elem != null)
				{
					result += elem.getAmount();
				}
			}
		}
		return Long.valueOf(result);
	}

}
