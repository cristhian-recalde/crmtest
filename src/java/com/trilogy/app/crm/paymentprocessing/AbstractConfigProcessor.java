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
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;

/**
 * Abstract base class for Config Processor.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public abstract class AbstractConfigProcessor implements ConfigProcessor
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	protected long getPayments(Context context, Account account,
	    Invoice invoice, Date date)
	{
		CalculationService service =
		    (CalculationService) context.get(CalculationService.class);
		long payments = 0;
		try
		{
			payments =
			    service.getAccountPaymentsReceived(context, account.getBAN(),
			        invoice.getInvoiceDate(), date);
		}
		catch (CalculationServiceException exception)
		{
			LogSupport.minor(context, this,
			    "Exception caught while calculating effective payments",
			    exception);
		}
		return payments;
	}

	@Override
	public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config,
	    long additionalPayment)
	{
		return processConfig(context, account, extension, invoice, date,
		    deadline, config);
	}

	protected abstract long calculateAmount(Context context,
	    CreditCategoryExtension extension, Invoice invoice,
	    LateFeeEarlyRewardConfiguration config, long payment);
}
