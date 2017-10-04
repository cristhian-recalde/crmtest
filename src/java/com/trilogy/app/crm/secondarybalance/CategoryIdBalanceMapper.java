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

package com.trilogy.app.crm.secondarybalance;

import com.trilogy.app.crm.xhome.home.OcgTransactionException;

/**
 * 
 * This class has 2 properties categoryId and balance, for a particular msisdn.
 * 
 * In case a particular categoryId does not return balance on a requestBalance call 
 * to OCG the exception property should be populated. Otherwise the exception property
 * should be null. 
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class CategoryIdBalanceMapper 
{

	private int categoryId = -1;
	private long balance = 0;
	private String msisdn = null;
	private OcgTransactionException exception = null;
	
	public CategoryIdBalanceMapper()
	{
		
	}
	
	public CategoryIdBalanceMapper(int categoryId_, long balance_, String msisdn_, OcgTransactionException exception_)
	{
		this.categoryId = categoryId_;
		this.balance = balance_;
		this.msisdn = msisdn_;
		this.exception = exception_;
	}
	
	public final int getCategoryId() 
	{
		return categoryId;
	}
	
	public final void setCategoryId(int categoryId) 
	{
		this.categoryId = categoryId;
	}
	
	public final long getBalance() 
	{
		return balance;
	}
	
	public final void setBalance(long balance) 
	{
		this.balance = balance;
	}
	
	public final String getMsisdn() 
	{
		return msisdn;
	}
	
	public final void setMsisdn(String msisdn) 
	{
		this.msisdn = msisdn;
	}

	public final OcgTransactionException getException() 
	{
		return exception;
	}

	public final void setException(OcgTransactionException exception) 
	{
		this.exception = exception;
	}	
	
	
}
