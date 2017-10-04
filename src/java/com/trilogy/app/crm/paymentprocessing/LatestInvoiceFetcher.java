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
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;

/**
 * Fetches the latest invoice for an account as of the provided date.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LatestInvoiceFetcher implements InvoiceFetcher
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LatestInvoiceFetcher instance =
	    new LatestInvoiceFetcher();

	public static LatestInvoiceFetcher instance()
	{
		return instance;
	}

	/**
	 * Returns the latest invoice for an account as of the provided date.
	 * 
	 * @param context
	 *            Operating context.
	 * @param account
	 *            Account to be processed.
	 * @param billingDate
	 *            Date to be processed.
	 * @return The latest invoice for an account as of the provided date.
	 * @see com.redknee.app.crm.paymentprocessing.InvoiceFetcher#getInvoice(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.bean.Account, java.util.Date)
	 */
	@Override
	public Invoice
	    getInvoice(Context context, Account account, Date billingDate)
	{
		CalculationService service =
		    (CalculationService) context.get(CalculationService.class);
		Invoice invoice = null;
		try
		{
			invoice =
			    service.getMostRecentInvoice(context, account.getBAN(),
			        billingDate);
		}
		catch (CalculationServiceException exception)
		{
			LogSupport.minor(context, this,
			    "Exception caught while looking up latest invoice for account "
			        + account.getBAN() + " as of " + billingDate, exception);
		}
		return invoice;
	}

}
