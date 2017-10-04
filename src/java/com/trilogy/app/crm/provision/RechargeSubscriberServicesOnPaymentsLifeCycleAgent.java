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
package com.trilogy.app.crm.provision;

import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;

/**
 * LifeCycleAgent for the task that processes 
 * Recharging of suspended services after account payments
 *
 * @author bdhavalshankh
 * @since 9.9
 */
public class RechargeSubscriberServicesOnPaymentsLifeCycleAgent extends
		LifecycleAgentScheduledTask {

	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param agentId
	 * @throws AgentException
	 */
	public RechargeSubscriberServicesOnPaymentsLifeCycleAgent(Context ctx,
			String agentId) throws AgentException 
	{
		super(ctx, agentId);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void start(Context ctx) throws LifecycleException, HomeException 
	{
		processSubscriberRechargeRequestRecord(ctx);
	}

	private void processSubscriberRechargeRequestRecord(Context ctx)
	{
		LogSupport.info(ctx, this, "Recharge subscriber services upon pool account payments Lifecycle Agent - BEGINS ");
		
		RechargeServicesOnAccountPayments agent = new RechargeServicesOnAccountPayments(ctx, "Recharge subscriber services upon pool account payments", this);
		agent.execute();
		
		LogSupport.info(ctx, this, "Recharge subscriber services upon pool account payments Lifecycle Agent - END ");
	}
}
