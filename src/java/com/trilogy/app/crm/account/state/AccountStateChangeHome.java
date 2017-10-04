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
package com.trilogy.app.crm.account.state;

import com.trilogy.app.crm.account.state.agent.AccountStateBlackListAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateChangeOMLog;
import com.trilogy.app.crm.account.state.agent.AccountStateDunningAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateSubAccountStateAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateSubscriberCreditLimitAgent;
import com.trilogy.app.crm.account.state.agent.AccountStateSubscriberStateAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * @author jchen
 *
 * The class sets up proper inner home chain for handling subscriber state changes.
 * 
 */
public class AccountStateChangeHome extends HomeProxy 
{
	/**
	 * @param ctx
	 * @param delegate
	 */
	public AccountStateChangeHome(Context ctx, Home delegate) 
	{
		Home home = getAccountStateAgentHome(ctx, delegate);
		setDelegate(home);		
	}
	
	
	protected Home getAccountStateAgentHome(Context ctx, Home delegate)
	{
		Home home = 
				new AccountStateSubscriberCreditLimitAgent(ctx, 
				new AccountStateSubscriberStateAgent(ctx,
				new AccountStateBlackListAgent(ctx,
				new AccountStateSubAccountStateAgent(ctx,
						
				//put dunning as late as possible
				new AccountStateDunningAgent(ctx,
				new AccountStateChangeOMLog(ctx, 
					delegate))))));
		return home;
	}

}
