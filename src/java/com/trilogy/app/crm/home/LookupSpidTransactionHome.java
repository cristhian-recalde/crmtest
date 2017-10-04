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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.*;

import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Account;

public class LookupSpidTransactionHome
	extends HomeProxy
{
	public LookupSpidTransactionHome(Home delegate)
	{
		super(delegate);
	}

	public Object create(Context ctx, Object obj)
		throws HomeException, HomeInternalException
	{
		Transaction transaction = (Transaction)obj;
        Account account = (Account) ctx.get(Account.class);
        
        if (account == null || account.getBAN() == null
            || !SafetyUtil.safeEquals(account.getBAN().trim(), transaction.getBAN().trim()))
        {
            Home accountHome = (Home)ctx.get(AccountHome.class);
            account = (Account)accountHome.find(ctx,transaction.getAcctNum());
        }

		transaction.setSpid(account.getSpid());
		return super.create(ctx,transaction);
	}
}
