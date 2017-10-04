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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transaction;

import java.io.Serializable;

/**
 * Container class to hold accumulations from Invoice Calculations.
 * 
 * @author Angie Li
 *
 */
public class InvoiceCalculationAccumulations implements Serializable 
{
	public InvoiceCalculationAccumulations()
	{
		amountOwing = 0;
		monthToDate = 0;
	}
	
	/**
	 * Sets the Amount Owing amount to the given value
	 * @param value - amount to set to amount owing
	 */
	public void setAmountOwing(int value)
	{
		amountOwing = value;
	}
	
	/**
	 * @return the amount owing balance 
	 */
	public int getAmountOwing()
	{
		return amountOwing;
	}
	
	/**
	 * Sets the Month To Date amount to the given value
	 * @param value - amount to set to month to date
	 */
	public void setMonthToDateBalance(int value)
	{
		monthToDate = value;
	}
	
	/**
	 * @return the month to date balance 
	 */
	public int setMonthToDateBalance()
	{
		return monthToDate;
	}
	
	private int amountOwing;
	private int monthToDate;

}
