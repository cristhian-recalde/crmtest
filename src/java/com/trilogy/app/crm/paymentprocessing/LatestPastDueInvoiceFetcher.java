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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;

/**
 * Fetchest the latest past due invoice as of the provided date.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LatestPastDueInvoiceFetcher implements InvoiceFetcher
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LatestPastDueInvoiceFetcher instance =
	    new LatestPastDueInvoiceFetcher();

	public static LatestPastDueInvoiceFetcher instance()
	{
		return instance;
	}

	/**
	 * Returns the latest past due invoice as of the provided date.
	 * 
	 * @param context
	 *            Operating context.
	 * @param account
	 *            Account to be processed.
	 * @param billingDate
	 *            Date to be processed.
	 * @return
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
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(billingDate);
		do
		{
			try
			{
				invoice =
				    service.getMostRecentInvoice(context, account.getBAN(),
				        calendar.getTime());
				if (invoice != null)
				{
					calendar.setTime(invoice.getInvoiceDate());
					calendar.add(Calendar.MONTH, -1);
				}
			}
			catch (CalculationServiceException exception)
			{
				LogSupport
				    .minor(context, this,
				        "Exception caught while looking up latest invoice for account "
				            + account.getBAN() + " as of " + billingDate,
				        exception);
			}
		}
		while (invoice != null && invoice.getDueDate().after(billingDate));
		return invoice;
	}

}
