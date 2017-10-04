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

/**
 * Fetches the latest late fee applicable invoice. This is required for
 * calculating the correct applicable late fee during the time period after an
 * older invoice has already been past due and not paid, but before the latest
 * invoice's due date. The older invoice's late fee(s) should already have been
 * captured in the invoice.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.6
 */
public class LatestLateFeeApplicableInvoiceFetcher implements InvoiceFetcher
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	private static final LatestLateFeeApplicableInvoiceFetcher instance =
	    new LatestLateFeeApplicableInvoiceFetcher();

	public static LatestLateFeeApplicableInvoiceFetcher instance()
	{
		return instance;
	}

	@Override
	public Invoice
	    getInvoice(Context context, Account account, Date billingDate)
	{
		Invoice latestPastDueInvoice =
		    LatestPastDueInvoiceFetcher.instance().getInvoice(context, account,
		        billingDate);
		Invoice latestInvoice =
		    LatestInvoiceFetcher.instance().getInvoice(context, account,
		        billingDate);

		if (latestPastDueInvoice == null)
		{
			return null;
		}

		/*
		 * If the latest psat due invoice's due date has already been covered by
		 * the latest invoice, it means the invoice's late fee should already
		 * have been generated.
		 */
		if (latestInvoice.getInvoiceDate().after(
		    latestPastDueInvoice.getDueDate()))
		{
			return null;
		}
		return latestPastDueInvoice;
	}

}
