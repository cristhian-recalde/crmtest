/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.pos.support;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.pos.AccumulatorDataCache;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.xdb.Sum;

/**
 * Support methods for account (and subscriber) accumulator
 * 
 * @author amedina
 *
 */
public class AccumulatorSupport 
{

	public AccumulatorSupport() 
	{
		super();
	}

	public static AccumulatorDataCache getAccumulation(Context ctx, 
			Account account, 
			Date startDate, 
			Date endDate) throws HomeException
	{
		AccumulatorDataCache data = new AccumulatorDataCache();
		
		data.setPaymentsReceived(getPaymentsReceivedFromTransactions(ctx, account, startDate, endDate));
		data.setDiscountAmount(getDiscountFromTransaction(ctx, account, startDate, endDate));
		
		return data;
	}

	private static long getPaymentsReceivedFromTransactions(Context ctx, 
			Account account, 
			Date startDate, 
			Date endDate) throws HomeException 
	{

		And filter = new And();
		filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
		filter.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeSupportHelper.get(ctx).getPaymentsCodes(ctx)));
		filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
		filter.add(new LT(TransactionXInfo.RECEIVE_DATE, endDate));
		
		return getTransactionValue(ctx, filter);
	}

	private static long getTransactionValue(Context ctx, Object filter) throws HomeInternalException, HomeException
	{
		Home txnHome = (Home) ctx.get(TransactionHome.class);
		long result = 0;
		Object sum = txnHome.cmd(ctx, new Sum(TransactionXInfo.AMOUNT, filter));
		
		if (sum != null)
		{
			result = ((BigDecimal)sum).longValue();
		}
		
		return result;
	}

	private static long getDiscountFromTransaction(Context ctx, 
			Account account, 
			Date startDate, 
			Date endDate) throws HomeException 
	{
		And filter = new And();
		filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
		filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Integer.valueOf(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,AdjustmentTypeEnum.Discount).getCode())));
		filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
		filter.add(new LT(TransactionXInfo.RECEIVE_DATE, endDate));

		return getTransactionValue(ctx, filter);
	}

	public static Collection getPaymentTransactions(Context ctx, 
			Account account, 
			Date startDate, 
			Date midnightToday) throws HomeException 
	{
		Home txnHome = (Home) ctx.get(TransactionHome.class);
		Collection txns = null;
		if (txnHome != null)
		{
			And filter = new And();
			filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
			filter.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeSupportHelper.get(ctx).getPaymentsCodes(ctx)));
			filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
			filter.add(new LT(TransactionXInfo.RECEIVE_DATE, midnightToday));
			
			txns = ((Home)txnHome.where(ctx, filter)).selectAll();
		}
		return txns;
	}

    public static Collection getPaymentTransactions(Context ctx, 
            Msisdn msisdn, 
            Date startDate, 
            Date midnightToday) throws HomeException 
    {
        Home txnHome = (Home) ctx.get(TransactionHome.class);
        Collection txns = null;
        if (txnHome != null)
        {
            And filter = new And();
            filter.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, msisdn.getSubscriberID()));
            filter.add(new In(TransactionXInfo.ADJUSTMENT_TYPE, AdjustmentTypeSupportHelper.get(ctx).getPaymentsCodes(ctx)));
            filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
            filter.add(new LT(TransactionXInfo.RECEIVE_DATE, midnightToday));
            
            txns = ((Home)txnHome.where(ctx, filter)).selectAll();
        }
        return txns;
    }

    public static AccumulatorDataCache getMSISDNAccumulation(Context ctx, Msisdn msisdn, Date startDate, Date endDate) throws HomeException
	{
		AccumulatorDataCache data = new AccumulatorDataCache();
		
		data.setAdjustments(getAdjustmentsFromTransactions(ctx, msisdn, startDate, endDate));
		return data;
	}

	private static long getAdjustmentsFromTransactions(Context ctx, Msisdn msisdn, Date startDate, Date endDate) throws HomeException 
	{
		Set deposits = new HashSet();
		
		deposits.add(Integer.valueOf(AdjustmentTypeEnum.DepositMade.getIndex()));
		
		And filter = new And();
		filter.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, msisdn.getSubscriberID()));
		filter.add(new Not(new In(TransactionXInfo.ADJUSTMENT_TYPE, deposits)));
		filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, startDate));
		filter.add(new LT(TransactionXInfo.RECEIVE_DATE, endDate));

		return getTransactionValue(ctx, filter);
	}
	
	

}
