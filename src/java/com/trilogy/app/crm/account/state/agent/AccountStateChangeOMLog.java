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

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.state.event.EnumStateChangeListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * @author jchen
 */
public class AccountStateChangeOMLog  extends AccountStateAgentHome
{
	/**
	 * @param ctx
	 * @param delegate
	 */
	public AccountStateChangeOMLog(Context ctx, Home delegate) {
		super(ctx, delegate);
	}
	
	public void onStateChange(Context ctx, Account oldAccount, Account newAccount) throws HomeException
	{
		generateOM(ctx,oldAccount, newAccount);
	}

	private void generateOM(Context ctx,Account oldAccount, Account newAccount)
	{ // generate corresponding OM when account got modified
		if (!AccountStateEnum.SUSPENDED.equals(oldAccount.getState())
			&& AccountStateEnum.SUSPENDED.equals(newAccount.getState()))
		{
			new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_SUSPEND_SUCCESS).log(ctx);
		}
		else if (!AccountStateEnum.INACTIVE.equals(oldAccount.getState())
			&& AccountStateEnum.INACTIVE.equals(newAccount.getState()))
		{
			new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_DEACTIVATE_SUCCESS);
		}
		else if (!AccountStateEnum.PROMISE_TO_PAY.equals(oldAccount.getState())
			&& AccountStateEnum.PROMISE_TO_PAY.equals(newAccount.getState()))
		{
			new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_PTP_SUCCESS);
		}
		else
		{
			//for other kinds of account modification
			new OMLogMsg(Common.OM_MODULE, Common.OM_ACCT_MODIFY_SUCCESS).log(ctx);
		}
	}
}
