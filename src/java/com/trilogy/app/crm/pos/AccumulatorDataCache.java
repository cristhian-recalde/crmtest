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
package com.trilogy.app.crm.pos;

/**
 * Class that holds locally the data chached for the accumulator
 * 
 * @author amedina
 *
 */
public class AccumulatorDataCache 
{
	public AccumulatorDataCache() 
	{
		super();
	}

	public AccumulatorDataCache(long payments, long discount) 
	{
		super();
		setPaymentsReceived(payments);
		setDiscountAmount(discount);
	}

	
	public long getDiscountAmount()
	{
		return discountAmount_;
	}

	public long getPaymentsReceived() 
	{
		return paymentsReceived_;
	}

	/**
	 * @param discountAmount The discountAmount to set.
	 */
	public void setDiscountAmount(long discountAmount) 
	{
		discountAmount_ = discountAmount;
	}

	/**
	 * @param paymentsReceived The paymentsReceived to set.
	 */
	public void setPaymentsReceived(long paymentsReceived) 
	{
		paymentsReceived_ = paymentsReceived;
	}

	public long getAdjustments() 
	{
		return adjustments_;
	}

	/**
	 * @param discountAmount The discountAmount to set.
	 */
	public void setAdjustments(long adjustments) 
	{
		adjustments_ = adjustments;
	}

	private long paymentsReceived_;
	private long discountAmount_;
	private long adjustments_;
}
