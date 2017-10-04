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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.LateFeeEarlyRewardConfiguration;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.TransactionOwnerTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;

/**
 * Generates a transaction.
 * 
 * @author cindy.wong@redknee.com
 * @since 2010-11-04
 */
public class GenerateTransactionResultsProcessor implements ResultsProcessor
{

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	private static final GenerateTransactionResultsProcessor instance =
	    new GenerateTransactionResultsProcessor();

	public static final GenerateTransactionResultsProcessor instance()
	{
		return instance;
	}

	/**
	 * @param context
	 * @param account
	 * @param results
	 * @return
	 * @see com.redknee.app.crm.paymentprocessing.ResultsProcessor#processResults(com.redknee.framework.xhome.context.Context,
	 *      com.redknee.app.crm.bean.Account, com.redknee.app.crm.bean.Invoice,
	 *      java.util.Date, java.util.SortedSet)
	 */
	@Override
	public Object processResults(Context context, Account account,
	    SortedSet<LateFeeEarlyReward> results)
	{
		List<Transaction> transactions = new ArrayList<Transaction>();
		if (results != null)
		{
			for (LateFeeEarlyReward elem : results)
			{
				if (elem != null)
				{
					boolean chargeable = false;
					try
					{
						chargeable =
						    isChargeable(context, account,
						        elem.getChargedItemType(), elem.getDeadline(),
						        elem.getAdjustmentType(), elem.getAmount(),
						        elem.getDate());
					}
					catch (HomeException exception)
					{
						LogSupport
						    .minor(
						        context,
						        this,
						        "Fail to determine whether the late fee/early reward transaction has already been created.",
						        exception);
					}

					if (chargeable)
					{
					Transaction transaction =
					    handleTransaction(context, account,
						        elem.getChargedItemType(), elem.getDeadline(),
						        elem.getConfig(),
					        elem.getDate(), elem.getAmount());
					transactions.add(transaction);
					}
					else
					{
						if (LogSupport.isDebugEnabled(context))
						{
							LogSupport.debug(context, this,
							    elem.getChargedItemType()
							        + " is not chargeable on " + elem.getDate()
							        + " for account " + account.getBAN());
						}
					}

				}
			}
		}
		return transactions;
	}

	public boolean isChargeable(Context context, Account account,
	    ChargedItemTypeEnum itemType, int deadline,
	    int adjustmentType, long amount, Date date) throws HomeException
	{
		Context ctx = context.createSubContext();
		ctx.put(Account.class, account);
		ServicePeriodHandler handler =
		    ServicePeriodSupportHelper.get(context).getHandler(
		        ServicePeriodEnum.MONTHLY);
		int billCycleDay = account.getBillCycleDay(ctx);
		Date startDate =
		    handler.calculateCycleStartDate(context, date, billCycleDay,
		        account.getSpid(), account.getBAN(), deadline);
		Date endDate =
		    handler.calculateCycleEndDate(ctx, date, billCycleDay,
		        account.getSpid(), account.getBAN(), deadline);
		return SubscriberSubscriptionHistorySupport.isChargeable(ctx,
		    account.getBAN(), itemType, deadline, date, startDate, endDate);
	}


	protected Transaction handleTransaction(Context context, Account account,
	    ChargedItemTypeEnum chargedItemType,
 int deadline,
	    LateFeeEarlyRewardConfiguration config, Date date, long amount)
	{
		Context ctx = context.createSubContext();
		ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM,
		    deadline);
		Transaction trans = null;
		try
		{
			trans =
			    createTransaction(ctx, account, config.getAdjustmentType(),
			        amount, date);
			CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, trans,
			    true);

			SubscriberSubscriptionHistorySupport.addChargingHistory(ctx,
			    deadline, account, HistoryEventTypeEnum.CHARGE,
			    chargedItemType,
			    amount, trans, date);
			return trans;
		}
		catch (HomeException exception)
		{
			LogSupport.major(
			    ctx,
			    this,
			    "Could not charge account " + account.getBAN() + " for "
			        + chargedItemType.getDescription() + " (config = "
			        + config.getIdentifier() + ")", exception);
		}
		return null;
	}

	protected Transaction createTransaction(Context context, Account account,
	    int adjustmentTypeCode, long amount, Date date)
	{
		try
		{
			Transaction transaction =
			    (Transaction) XBeans.instantiate(Transaction.class, context);
			transaction.setBAN(account.getBAN());
			transaction.setResponsibleBAN(account.getResponsibleBAN());
			AdjustmentType adjustmentType =
			    AdjustmentTypeSupportHelper.get(context)
			        .getAdjustmentTypeForRead(context, adjustmentTypeCode);
			transaction.setAction(adjustmentType.getAction());

			if (AdjustmentTypeActionEnum.CREDIT.equals(adjustmentType
			    .getAction()) && amount < 0)
			{
				transaction.setAmount(-amount);
			}
			else
			{
				transaction.setAmount(amount);
			}
			transaction.setReceiveDate(date);
			transaction.setAdjustmentType(adjustmentTypeCode);
			transaction.setOwnerType(TransactionOwnerTypeEnum.ACCOUNT);
			transaction.setPayee(PayeeEnum.Account);

			return transaction;
		}
		catch (Exception exception)
		{
			LogSupport.minor(context, this,
			    "Exception caught while create transaction", exception);
		}
		return null;
	}

}
