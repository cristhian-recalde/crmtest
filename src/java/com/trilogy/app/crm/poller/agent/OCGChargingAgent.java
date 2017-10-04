/*
 * OCGChargingAgent.java
 * 
 * Author : kenneth.so@redknee.com Date : Mar 03, 2006
 * 
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller.agent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AbstractTransaction;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.OCGChargingProcessor;
import com.trilogy.app.crm.poller.event.VRAERProcessor;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.OcgAdj2CrmAdjSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.support.VRASupport;

/**
 * Agent that acutally performs operations with the ER.
 * 
 * @author kenneth.so@redknee.com
 * @since Mar 03, 2006
 */
public class OCGChargingAgent implements ContextAgent, Constants
{

	public OCGChargingAgent(CRMProcessor processor)
	{
		super();
		processor_ = processor;
	}

	@Override
	public void execute(Context ctx) throws AgentException
	{
		final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

		List params = new ArrayList();
		final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
		Common.OM_OCG375.attempt(ctx);
		try
		{
			try
			{
				CRMProcessorSupport.makeArray(ctx, params, info.getRecord(),
				    info.getStartIndex(), ',', info.getErid(), this);
			}
			catch (FilterOutException e)
			{
				return;
			}

			createTransaction(ctx, params);

			Common.OM_OCG375.success(ctx);

		}
		catch (final Throwable t)
		{
			new MinorLogMsg(this,
			    "Failed to process ER 375 because of Exception "
			        + t.getMessage(), t).log(ctx);
			processor_.saveErrorRecord(ctx, info.getRecord());
			Common.OM_OCG375.failure(ctx);
		}
		finally
		{
			pmLogMsg.log(ctx);
		}
	}

	public String extractMsisdn(final Context ctx, final List params1)
	    throws Exception
	{
		final String msisdn =
		    ((String) params1.get(OCG375_MSISDN_INDEX)).trim();
		if (msisdn.length() == 0)
		{
			throw new Exception("MSISDN is empty");
		}

		return msisdn;
	}

	private String
	    getOptionalParam(List params, int index, String defaultValue)
	{
		String result = defaultValue;
		if (params.size() > index + 1)
		{
			String parameter = ((String) params.get(index)).trim();
			if (parameter != null && !parameter.isEmpty())
			{
				result = parameter;
			}
		}
		return result;
	}

	/**
	 * Update subscriber profile and create Transaction Entry per OCG ER.
	 * 
	 * @param transDate
	 *            Transaction date.
	 * @param params1
	 *            The parsed ER fields value list.
	 */
	public void createTransaction(final Context ctx, final List params1)
	    throws Exception
	{

		final String msisdn =
		    ((String) params1.get(OCG375_MSISDN_INDEX)).trim();
		/*
		 * TT 8021100026: CRM will read the 7 field (transdate) as timestamp.
		 * The format for the transaction
		 * date should be: "yyyy/MM/dd HH:mm:ss" and if that fails, then it will
		 * use ER Polled
		 * timestamp as transaction timestamp (to have backward compatibility).
		 */
		Date transDate = null;
		try
		{
			transDate =
			    CRMProcessorSupport.getDate((String) params1
			        .get(OCG375_TRANS_DATE_INDEX));
		}
		catch (ParseException e)
		{
			final ProcessorInfo info =
			    (ProcessorInfo) ctx.get(ProcessorInfo.class);
			transDate = new java.util.Date(info.getDate());
		}
		final String ocgAdjId =
		    ((String) params1.get(OCG375_ADJ_TYPE_INDEX)).trim();
		final String csrInput =
		    ((String) params1.get(OCG375_ADJ_DESC_INDEX)).trim();
		final String amtStr =
		    ((String) params1.get(OCG375_CHARGING_AMT_INDEX)).trim();
		final String externalID =
		    ((String) params1.get(OCG375_EXTERNAL_TRANS_ID_INDEX)).trim();

		/* TT 8021100026: Also, it expects 10 the field for "New balance" */

		// Senthooran: Make the New Balance be optional so it is backward
		// compatible with older ER 375
		// adding 1 to OCG375_NEWBALANCE_AMT_INDEX since that index value that
		// begins counting at 0
		String newBalanceStr =
		    getOptionalParam(params1, OCG375_NEWBALANCE_AMT_INDEX, "0");

		/*
		 * [Cindy Wong] TT#10050708048: New fields in OCG375.
		 */
		String subTypeStr =
		    getOptionalParam(params1, OCG375_SUBSCRIPTION_TYPE_INDEX, "0");

		if (amtStr.length() == 0)
		{
			throw new IllegalArgumentException(
			    "Failed to process OCG ER for MSISDN [" + msisdn
			        + "]: Amount is empty");
		}
		if (newBalanceStr.length() == 0)
		{
			throw new IllegalArgumentException(
			    "Failed to process OCG ER for MSISDN [" + msisdn
			        + "]: New Balance Amount is empty");
		}

		final long amt = Long.parseLong(amtStr);
		final long newBalanceamt = Long.parseLong(newBalanceStr);
		int extensionDays = getExtensionDays(params1);

		Subscriber subscriber = null;

		/*
		 * [Cindy Wong] TT#10050708048: Attempt to look up by subscription type,
		 * if provided.
		 */
		if (subTypeStr != null && !subTypeStr.isEmpty()
		    && !SafetyUtil.safeEquals(subTypeStr, "0"))
		{
			try
			{
				long subType = Long.parseLong(subTypeStr);
				subscriber =
				    SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn,
				        subType, transDate);
			}
			catch (NumberFormatException exception)
			{
				throw new IllegalArgumentException(
				    "Failed to process OCG ER: Subscription Type " + subTypeStr
				        + " is not properly formatted");
			}
		}

		/*
		 * [Cindy Wong] TT#10050708048: Fall back to look up by MSISDN only, the
		 * old fashioned way.
		 */
		if (subscriber == null)
		{
			subscriber =
			    SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn,
			        transDate);
		}

		if (subscriber == null)
		{
			throw new IllegalArgumentException(
			    "Failed to process OCG ER: Cannot find account/subscriber to process -- MSISDN="
			        + msisdn);
		}

		// Need to add the following into the context so that CRM will skip the
		// OCG charge.
		ctx.put(VRAERProcessor.TRANSACTION_FROM_VAR_ER_POLLER, true);

		AdjustmentType adjType =
		    OcgAdj2CrmAdjSupport.mapOcgAdj2CrmAdjType(ctx,
		        subscriber.getSpid(), ocgAdjId);

		// create Transaction Entry in Ecare DB. It should not check the limit.

		/*
		 * Check whether the transaction is a VRA normal, VRA ETU, or normal 375
		 * transaction
		 */
		if (VRASupport.isVRAEvent(ctx, adjType.getCode(),subscriber.getSpid()))
		{
			// TODO: OCG must pass the voucher credit value, to be shown to the
			// subscriber in the recharge SMS notification.
		    
			long voucherCreditValue = amt;
			int eventType = VRASupport.getVRAEventTypeFromAdjustmentType(ctx, adjType.getCode(),subscriber.getSpid());
			VRASupport.createVRATransaction(ctx, transDate, amt, newBalanceamt,
			    voucherCreditValue, extensionDays, subscriber,
			     csrInput, eventType, externalID);
		}
		else
		{
			TransactionSupport.createTransaction(ctx, subscriber, amt,
			    newBalanceamt, adjType, DEFAULT_PRORATE,
			    DEFAULT_LIMIT_EXCEPTION, CoreTransactionSupportHelper.get(ctx)
			        .getCsrIdentifier(ctx), transDate, new Date(), csrInput,
			    DEFAULT_EXPIRYDAY_EXT, externalID,
			    AbstractTransaction.DEFAULT_TRANSACTIONMETHOD);
		}

	}

	private int getExtensionDays(final List params)
	{
		int extensionDays;
		final String extensionDaysStr =
		    ((String) params.get(OCG375_EXPIRY_EXTENSION_INDEX)).trim();
		try
		{
			if (extensionDaysStr != null && extensionDaysStr.length() > 0)
			{
				extensionDays = Integer.parseInt(extensionDaysStr);
			}
			else
			{
				extensionDays = 0;
			}
		}
		catch (NumberFormatException e)
		{
			new MinorLogMsg(this, "Could not parse Extension Days ["
			    + extensionDaysStr + "]", e);
			extensionDays = 0;
		}
		return extensionDays;
	}


	static boolean DEFAULT_PRORATE = false;
	static boolean DEFAULT_LIMIT_EXCEPTION = true;
	static int DEFAULT_EXPIRYDAY_EXT = 0;

	private CRMProcessor processor_ = null;

	private static final String PM_MODULE = OCGChargingProcessor.class
	    .getName();
}
