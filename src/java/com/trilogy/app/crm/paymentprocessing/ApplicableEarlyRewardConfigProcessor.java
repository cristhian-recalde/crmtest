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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;

/**
 * Config processor to calculate eligible early payment rebate had the invoice
 * been paid in full by today.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class ApplicableEarlyRewardConfigProcessor extends
    EarlyRewardConfigProcessor
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	private static final ApplicableEarlyRewardConfigProcessor instance =
	    new ApplicableEarlyRewardConfigProcessor();

	public static final ApplicableEarlyRewardConfigProcessor instance()
	{
		return instance;
	}

	@Override
	public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config,
	    long additionalPayment)
	{
		if (invoice.getTotalAmount() <= 0)
		{
			return null;
		}
		return super.processConfig(context, account, extension, invoice, date,
		    deadline, config, -invoice.getTotalAmount() + additionalPayment);
	}

	@Override
    public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config)
	{
		if (invoice.getTotalAmount() <= 0)
		{
			return null;
		}
		return super.processConfig(context, account, extension, invoice, date,
		    deadline, config, -invoice.getTotalAmount());
	}
}
