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
package com.trilogy.app.crm.account.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Account;

/**
 * TODO 2010-01-06 substitute with ELang
 *
 * @author jchen
 *
 * Predicate to determine account should be responsible or non-responsible
 * If we don't specify responsible attribute, we ignore this checking
 */
public class ResponsibleAccountPredicate implements Predicate 
{

	/**
	 * If responsible equals null, we ignore the checking
	 * @param responsible
	 */
	public ResponsibleAccountPredicate(boolean responsible)
	{
		responsible_ = responsible;
	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context arg0, Object obj)
	{
		Account account = (Account)obj;
      return responsible_ == account.isResponsible();
	}
	
	protected boolean responsible_;
}
