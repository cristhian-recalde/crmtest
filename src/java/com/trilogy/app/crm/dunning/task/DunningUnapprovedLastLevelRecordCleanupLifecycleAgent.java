/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.task;

import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author abhijit.mokashi
 * @since 10.2.1
 * 
 * This class starts the DunningUnapprovedLastLevelRecordCleanupProcess
 * 
 */
public class DunningUnapprovedLastLevelRecordCleanupLifecycleAgent extends LifecycleAgentScheduledTask {

	public DunningUnapprovedLastLevelRecordCleanupLifecycleAgent(Context ctx, String agentId)
			throws AgentException {
		super(ctx, agentId);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void start(Context ctx) throws LifecycleException, HomeException {
		LogSupport.debug(ctx, this,
				"inside DunningUnapprovedLastLevelRecordCleanupLifecycleAgent start method");
		try {
			
			DunningUnapprovedLastLevelRecordCleanupVisitor visitor = new DunningUnapprovedLastLevelRecordCleanupVisitor();
			visitor.visit(ctx, null);
		} catch (AbortVisitException e) {
			LogSupport.minor(ctx,this,"Failed to run the unapproved low level Dunning Record cleanup task.",e);
		} catch (AgentException e) {
			LogSupport.minor(ctx,this,"Failed to run the unapproved low level Dunning Record cleanup task.",e);
		}
	}
}
