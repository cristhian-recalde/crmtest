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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.dunning.DunningProcess;

/**
 * @author jchen
 */
public class AccountStateDunningAgent  extends AccountStateAgentHome 
{

	/**
	 * @param ctx
	 * @param delegate
	 */
	public AccountStateDunningAgent(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
    public void onStateChange(Context ctx, Account oldAccount, Account newAccount) throws HomeException
	{
		if (needsDunning(ctx))
		{
			goDunning(ctx, newAccount);
		}
		

	}
	
	//
	public static String ACCOUNT_STATE_DUNNING_REQUEST = "AccountStateChangeDunningRequest";
	public static void requestDunning(Context ctx, Object caller)
	{
		new DebugLogMsg(caller, "requesting Dunning", null).log(ctx);
		ctx.put(ACCOUNT_STATE_DUNNING_REQUEST, true);
	}
	public static boolean needsDunning(Context ctx)
	{
		return ctx.getBoolean(ACCOUNT_STATE_DUNNING_REQUEST, false);
	}
	
	
	private void goDunning(Context ctx,Account newAccount)
	{
		try
		{
         DunningProcess dunningProcess = (DunningProcess)ctx.get(DunningProcess.class);
         dunningProcess.processAccount(ctx, new Date(), newAccount);
		}
		catch (Exception e)
		{
			new MinorLogMsg(this, "Dunning Process has failed for Account [BAN" + newAccount.getBAN() + "]", e)
				.log(ctx);
		}
	}
}
