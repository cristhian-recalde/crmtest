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
import com.trilogy.app.crm.invoice.process.ProducerAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

/**
 * The Producer Agent for Account Accumulator updating.
 * 
 * @author Angie Li
 */
public class AccountAccumulatorProducer extends ProducerAgent 
{
	public AccountAccumulatorProducer(Context ctx, 
            ContextAgent agent,
            String threadName,  
            int threadSize, 
            int queueSize)
    {
        super (ctx, agent, threadName, threadSize, queueSize);
    }
	
	public void updateAccountAccumulator(Context ctx, Account account) throws AgentException
	{
		Context subCtx = ctx.createSubContext();
        subCtx.put(Account.class, account);
        
		execute(subCtx);
	}
}
