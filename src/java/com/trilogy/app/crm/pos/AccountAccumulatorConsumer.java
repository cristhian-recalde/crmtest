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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * The Consumer Agent for updating Account Accumulators
 * 
 * @author Angie Li
 *
 */
public class AccountAccumulatorConsumer extends ContextAwareSupport implements ContextAgent 
{
	
	public AccountAccumulatorConsumer(Context ctx)
    {
        setContext(ctx);
    }

	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(Context ctx) throws AgentException 
	{
		Account account = (Account) ctx.get(Account.class);
		
		/*if (account != null)
		{
			try
			{
				//AccountAccumulatorVisitor.processAccount(ctx, account);
			}
			catch (HomeException e)
			{
				throw new AgentException(e.getMessage(), e);
			}
		}*/
	}

}
