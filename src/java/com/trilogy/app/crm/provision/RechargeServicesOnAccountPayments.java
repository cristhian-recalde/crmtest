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

import java.sql.SQLException;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubscriberRechargeRequest;
import com.trilogy.app.crm.subscriber.GenericProcessSubscriberThreadPoolVisitor;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Multi-threaded task that processes requests for re-charging subscribers
 * under a pooled account on payments done.
 * This request entity is populated on account payment done for group pooled postpaid account only.
 * 
 * @author bdhavalshankh
 * @since 9.9
 */
public class RechargeServicesOnAccountPayments extends ContextAwareSupport {

	private final LifecycleAgentSupport agent_;
	

	public RechargeServicesOnAccountPayments(Context ctx, String agentName, LifecycleAgentSupport agent) 
	{
		super(ctx);
		this.agent_ = agent;
	}
	
	public void execute()
	{
		Context ctx = this.context_;
		LogSupport.info(ctx, this, "Recharge subscriber services upon pool account payments Agent - BEGINS ");
		
		processSubscriberRechargeRequests(ctx);
		
		LogSupport.info(ctx, this, "Recharge subscriber services upon pool account payments Agent - END ");
	}
	
	
    private void processSubscriberRechargeRequests(final Context ctx)
    {
    	final XDB xdb = (XDB) ctx.get(XDB.class);
    	
    	final String requestTableName = "SUBRRECHARGEREQ";
    	
    	XStatement sql = new XStatement(){
             public String createStatement(Context ctx)
             {
                 return " select "
                     + requestTableName + "." + SubscriberRechargeRequest.SUBSCRIBERID_PROPERTY
                     + " from "
                     + requestTableName 
                     + " where "
                     + requestTableName + "." + SubscriberRechargeRequest.STATE_PROPERTY + " = 0";
             }

             public void set(Context ctx, XPreparedStatement ps) throws SQLException
             {
             }
         };
    	
    	
    	ProcessSubscriberRechargeRequestVisitor delegate = new  ProcessSubscriberRechargeRequestVisitor();

    	final GenericProcessSubscriberThreadPoolVisitor threadPoolVisitor = new GenericProcessSubscriberThreadPoolVisitor(ctx,
    			getThreadPoolSize(ctx), getThreadPoolQueueSize(ctx), delegate, agent_);

    	try
    	{
    		xdb.forEach(ctx, threadPoolVisitor, sql);
    	}
    	catch (final Throwable t)
    	{
    		LogSupport.major(ctx, this, "Exception caught while running Recharge subscriber services upon pool account payments Task : " + t.getMessage(), t);
    	}
    	finally
    	{
    		try
    		{
    			threadPoolVisitor.getPool().shutdown();
    			threadPoolVisitor.getPool().awaitTerminationAfterShutdown(getTimeOut(ctx)); 
    		}
    		catch (final Exception e)
    		{
    			LogSupport.major(ctx, this,
    					"Exception caught during wait for completion of Recharge subscriber services upon pool account payments Task"
    							+ e.getMessage(), e);
    		}
    	} 
    }

    
    private int getThreadPoolSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getProcessSubscriberRechargeRequestThreads();
    }
    
    private int getThreadPoolQueueSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getProcessSubscriberRechargeRequestQueueSize();
    }
    
    private long getTimeOut(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getProcessSubscriberRechargeRequestTimeOut();
    }

}
