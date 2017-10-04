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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;

/**
 * Abstract base class of config fetcher.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public abstract class AbstractConfigFetcher implements ConfigFetcher
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	protected LateFeeEarlyRewardConfiguration getConfiguration(Context context,
	    Object homeKey, long configId)
	{
		Home home = (Home) context.get(homeKey);
		if (home == null)
		{
			LogSupport.minor(context, this, "Cannot find home " + homeKey
			    + " in context");
			return null;
		}

		LateFeeEarlyRewardConfiguration result = null;
		try
		{
			result =
			    (LateFeeEarlyRewardConfiguration) home.find(context,
			        Long.valueOf(configId));
		}
		catch (HomeException exception)
		{
			LogSupport.minor(context, this,
			    "Exception caught while looking up configuration " + configId,
			    exception);
		}
		return result;
	}
}
