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
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.EarlyRewardConfiguration;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.bean.LateFeeTypeEnum;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.extension.creditcategory.CreditCategoryExtension;
import com.trilogy.app.crm.extension.creditcategory.EarlyRewardCreditCategoryExtension;

/**
 * Process an early reward configuration for the potential early payment rebate.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class EarlyRewardConfigProcessor extends AbstractConfigProcessor
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final EarlyRewardConfigProcessor instance =
	    new EarlyRewardConfigProcessor();

	public static EarlyRewardConfigProcessor instance()
	{
		return instance;
	}

	@Override
	public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config)
	{
		return processConfig(context, account, extension, invoice, date,
		    deadline, config, 0);
	}

	/**
	 * Calculates the potential early payment rebate applicable for the
	 * particular configuration.
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
	 * @param additionalPayment
	 *            Any additional payment which may be accounted for as of date.
	 * @return The potential late fee applicable.
	 * @see com.redknee.app.crm.paymentprocessing.ConfigProcessor#processConfig(Context,
	 *      Account, CreditCategoryExtension, Invoice, Date, int,
	 *      LateFeeEarlyRewardConfiguration, long)
	 */
	@Override
	public LateFeeEarlyReward processConfig(Context context, Account account,
	    CreditCategoryExtension extension, Invoice invoice, Date date,
	    int deadline, LateFeeEarlyRewardConfiguration config,
	    long additionalPayment)
	{
		LateFeeEarlyReward result = null;

		// make sure early reward is not generated twice.
		long appliedEarlyReward = getAppliedEarlyReward(context, account, date);
		if (LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, this,
			    "Early reward already applied since invoice date is "
			        + appliedEarlyReward);
		}

		if (appliedEarlyReward == 0)
		{
			long payment = getPayments(context, account, invoice, date);

			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "The total payment made since invoice date is " + payment);
			}

			payment += additionalPayment;
			long amount =
			    calculateAmount(context, extension, invoice, config, payment);

			if (LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, this,
				    "The total eligible early reward is " + amount);
			}

			if (amount != 0)
			{
				result =
				    new LateFeeEarlyReward(date, config.getAdjustmentType(),
				        amount, ChargedItemTypeEnum.EARLYREWARD, deadline,
				        config);
			}
		}
		return result;
	}

	/**
	 * Calculates the applicable early payment rebate.
	 * 
	 * @param context
	 *            Operating context.
	 * @param extension
	 *            Credit category extension.
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
		EarlyRewardCreditCategoryExtension ext =
		    (EarlyRewardCreditCategoryExtension) extension;
		if (invoice.getTotalAmount() + payment > 0)
		{
			return 0;
		}
		long result = 0;
		EarlyRewardConfiguration cfg = (EarlyRewardConfiguration) config;
		OUTER: switch (cfg.getAmountType())
		{
			case LateFeeTypeEnum.PERCENTAGE_INDEX:
				result =
				    -Math.round(invoice.getTotalAmount() * cfg.getPercentage()
				        / 100);
				break OUTER;
			case LateFeeTypeEnum.FLAT_FEE_INDEX:
				result = -cfg.getFlatFee();
				break OUTER;
			default:
				LogSupport.info(context, this,
				    "Early Reward amount type " + cfg.getAmountType()
				        + " of configuration " + config.getIdentifier()
				        + " is not understood by CRM. Skipping");
		}

		return result;
	}

	/**
	 * Returns the early reward applied to the account on or before a specific
	 * date.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param account
	 *            Account to be examined.
	 * @param date
	 *            Processing date.
	 * @return The early reward applied to the account on or before the provided
	 *         date.
	 */
	protected long
	    getAppliedEarlyReward(Context ctx, Account account, Date date)
	{
		CalculationService service =
		    (CalculationService) ctx.get(CalculationService.class);
		try
		{
			return service.getAppliedEarlyReward(ctx, account.getBAN(), date);
		}
		catch (CalculationServiceException exception)
		{
			LogSupport
			    .info(
			        ctx,
			        this,
			        "Exception caught while attempting to retrieve applied early reward",
			        exception);
		}
		return 0;
	}
}
