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
package com.trilogy.app.crm.account.state.agent;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.state.event.EnumStateChangeListener;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author jchen
 *
 * We assume that home methods only handles for Account state 
 * related actions
 */
public abstract class AccountStateAgentHome extends HomeProxy
{

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
//	public Object store(Context ctx, Object obj) throws HomeException,
//			HomeInternalException 
//	{
//		Account newAccount = (Account)obj;
//		Account oldAccount  = getOldAccount(ctx, newAccount);
//		if (oldAccount != null && !oldAccount.getState().equals(newAccount.getState()))
//			onStateChange(ctx, oldAccount, newAccount);
//		
//		return super.store(ctx, obj);
//	}
	
	public abstract void onStateChange(Context ctx, Account oldAccount, Account newAccount) throws HomeException;
	
	
	/**
	 * @param ctx
	 * @param delegate
	 */
	public AccountStateAgentHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	/**
	 * Gets old Account object from context by using the same account id
	 * @param ctx
	 * @param newAccount
	 * @return
	 */

//	public Account getOldAccount(Context ctx, Account newAccount) throws HomeException
//	{
//		Account oldAccount = (Account)ctx.get(CONTEXT_KEY_OLD_ACCOUNT + newAccount.getBAN());
//		if (oldAccount == null)
//		{
//			oldAccount = AccountSupport.getAccount(ctx, newAccount.getBAN());
//			ctx.put(CONTEXT_KEY_OLD_ACCOUNT  + newAccount.getBAN(), oldAccount);
//		}
//		//should we need to clone the old one
//		return oldAccount;
//	}
//	public final static String CONTEXT_KEY_OLD_ACCOUNT = "com.redknee.app.crm.account.state.agent.oldAccount";
}
