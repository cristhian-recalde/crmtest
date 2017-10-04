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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeBaseAmountTypeEnum;
import com.trilogy.app.crm.bean.LateFeeConfiguration;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.bean.LateFeeTypeEnum;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtension;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Process a late fee configuration for the potential late fee charge.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class LateFeeConfigProcessor extends AbstractConfigProcessor
{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final LateFeeConfigProcessor instance =
	    new LateFeeConfigProcessor();

	public static LateFeeConfigProcessor instance()
	{
		return instance;
	}

	/**
	 * Calculates the potential late fee applicable for the particular
	 * configuration.
	 * 
	 * @param context
	 *            Operating context.
	 * @param account
	 *            Account to be processed.
	 * @param extension
	 *            Credit category extension.
	 * @param invoice
	 *            Invoice to be processed.
	 * @param date
	 *            Date of processing.
	 * @param deadline
	 *            Deadline of the configuration.
	 * @param config
	 *            Configuration to be processed.
	 * @return The potential late fee applicable.
	 * @see com.redknee.app.crm.paymentprocessing.ConfigProcessor#processConfig(Context,
	 *      Account, CreditCategoryExtension, Invoice, Date, int,
	 *      LateFeeEarlyRewardConfiguration, long)
	 */
	@Override
	public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config)
	{
		LateFeeEarlyReward result = null;

		/* Calculate payment made with regard to this configuration. */
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(invoice.getDueDate());
		calendar.add(Calendar.DAY_OF_MONTH, deadline);
		long payment =
		    getPayments(context, account, invoice, calendar.getTime());

		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this,
			    "Total payment received since invoice date is " + payment);
		}

		long amount =
		    calculateAmount(context, extension, invoice, config, payment);

		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this, "Applicable late fee for invoice "
			    + invoice.getInvoiceId() + " is " + amount);
		}

		if (amount != 0)
		{
			int accountBillCycleDay = -1;

			try 
			{
				accountBillCycleDay = account.getBillCycleDay(context);
			}
			catch (HomeException e)
	        {
	            new MinorLogMsg(this, "Error retrieving bill cycle day for account " + account.getBAN() + " [BillCycleID=" + account.getBillCycleID() + "]", e).log(context);
	            
	        }
			calendar.setTime(date); 
			CalendarSupport calSupp = CalendarSupportHelper.get(context);
			calSupp.clearTimeOfDay(calendar);	
			
			if(calSupp.getDayOfMonth(date) == accountBillCycleDay)
			{
				calendar.add(Calendar.DAY_OF_MONTH, -1);
			}

			result =
				new LateFeeEarlyReward(calendar.getTime(),
						config.getAdjustmentType(), amount,
						ChargedItemTypeEnum.LATEFEE, deadline, config);
		}
		return result;
	}

	/**
	 * Calculates the applicable late fee.
	 * 
	 * @param context
	 *            Operating context.
	 * @param extension
	 *            Credit category extension (for retrieving threshold).
	 * @param invoice
	 *            Invoice.
	 * @param config
	 *            Configuration being processed.
	 * @param payment
	 *            Payments made.
	 * @return Late fee amount.
	 * @see com.redknee.app.crm.paymentprocessing.AbstractConfigProcessor#calculateAmount(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.extension.creditcategory.CreditCategoryExtension,
	 *      com.redknee.app.crm.bean.Invoice,
	 *      com.redknee.app.crm.paymentprocessing.LateFeeEarlyRewardConfiguration,
	 *      long)
	 */
	@Override
	protected long calculateAmount(Context context,
	    CreditCategoryExtension extension, Invoice invoice,
	    LateFeeEarlyRewardConfiguration config, long payment)
	{
		LateFeeCreditCategoryExtension ext =
		    (LateFeeCreditCategoryExtension) extension;
		long outstandingAmount = invoice.getTotalAmount() + payment;
		if (outstandingAmount <= ext.getThreshold())
		{
			return 0;
		}

		long result = 0;
		LateFeeConfiguration cfg = (LateFeeConfiguration) config;
		OUTER: switch (cfg.getAmountType())
		{
			case LateFeeTypeEnum.PERCENTAGE_INDEX:
				long baseAmount = 0;
				INNER: switch (cfg.getBaseAmountType())
				{
					case LateFeeBaseAmountTypeEnum.TOTAL_INDEX:
						baseAmount = invoice.getTotalAmount();
						break INNER;
					case LateFeeBaseAmountTypeEnum.UNPAID_INDEX:
						baseAmount = outstandingAmount;
						break INNER;
					default:
						LogSupport.info(
						    context,
						    this,
						    "Late Fee base amount type "
						        + cfg.getBaseAmountType()
						        + " of configuration " + config.getIdentifier()
						        + " is not understood by CRM. Skipping");
				}

				result = Math.round(baseAmount * cfg.getPercentage() / 100);
				break OUTER;
			case LateFeeTypeEnum.FLAT_FEE_INDEX:
				result = cfg.getFlatFee();
				break OUTER;
			default:
				LogSupport.info(context, this,
				    "Late Fee amount type " + cfg.getAmountType()
				        + " of configuration " + config.getIdentifier()
				        + " is not understood by CRM. Skipping");
		}

		return result;
	}
}
