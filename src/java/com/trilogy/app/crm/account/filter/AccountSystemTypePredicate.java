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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * @author jke
 *
 */
public class AccountSystemTypePredicate implements Predicate 
{

	/**
	 * If responsible equals null, we ignore the checking
	 * @param type
	 */
	public AccountSystemTypePredicate(SubscriberTypeEnum type)
	{
	    type_ = type;
	}

	public boolean f(Context arg0, Object obj)
	{
	    Account account = (Account)obj;
	    return type_ == account.getSystemType();
	}
	
	protected SubscriberTypeEnum type_;
}
