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
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bas.recharge.ProcessSubscriberThreadPoolVisitor;
import com.trilogy.app.crm.bas.recharge.RechargeSubscriberVisitor;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

/**
 * This task retries to provision/unprovision/suspend all the failed subscriber services 
 *
 * @author shailesh.makhijani
 * @since 9.7
 */
public class SubscriberServicesRetryLifeCycleAgent extends
		LifecycleAgentScheduledTask {

	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param agentId
	 * @throws AgentException
	 */
	public SubscriberServicesRetryLifeCycleAgent(Context ctx, String agentId)
			throws AgentException {
		super(ctx, agentId);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void start(Context ctx) throws LifecycleException, HomeException {
		Context subCtx = ctx.createSubContext();
		
		String subId = getParameter2(ctx, String.class);
		processSubscribersWithServiceChanges(subCtx,  subId);
	}

	/**
     * Find all the subscribers with failed service status and process each of them.
     * Service status could be PROVISIONEDWITHERRORS, SUSPENDEDWITHERRORS, UNPROVISIONEDWITHERRORS
     *
     * @param ctx The operating context.
     */
    private void processSubscribersWithServiceChanges(final Context ctx, final String subId)
    {
        LogSupport.info(ctx, this, "Subscriber Services Retry  Lifecycle Agent : BEGINS");
        
        RetryFailedSubscriberService retry = new RetryFailedSubscriberService(ctx, "Subscriber Services Retry Agent", this);
        retry.execute(subId);
        
        LogSupport.info(ctx, this, " Subscriber Services Retry  Lifecycle Agent : ENDS");
    }
}
