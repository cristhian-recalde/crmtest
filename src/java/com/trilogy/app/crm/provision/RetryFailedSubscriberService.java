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

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.subscriber.GenericProcessSubscriberThreadPoolVisitor;

/**
 * 
 *
 * @author shailesh.makhijani
 * @since 9.7.0
 */
public class RetryFailedSubscriberService extends ContextAwareSupport {

	private final LifecycleAgentSupport agent_;	
	
	/**
	 * @param ctx
	 */
	public RetryFailedSubscriberService(Context ctx, String agentName, final LifecycleAgentSupport agent) {
		super(ctx);
		this.agent_= agent;
	}
	
	public void execute(String subId) {
		processSubscribersWithServiceChanges(this.context_, subId);
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

    	final XDB xdb=(XDB) ctx.get(XDB.class);
    	
    	String sqlQuery = getSqlQuery(ctx, subId);

    	FailedSubscriberServicesRetryVisitor delegate =  new FailedSubscriberServicesRetryVisitor();

    	final GenericProcessSubscriberThreadPoolVisitor threadPoolVisitor = new GenericProcessSubscriberThreadPoolVisitor(ctx,
    			getThreadPoolSize(ctx), getThreadPoolQueueSize(ctx), delegate, agent_);

    	try
    	{
    		xdb.forEach(ctx, threadPoolVisitor, sqlQuery);

    	}
    	catch (final Throwable t)
    	{
    		LogSupport.minor(ctx, this, "Exception caught while running Retry failed subscriber services task : " + t.getMessage(), t);
    	}
    	finally
    	{
    		try
    		{
    			threadPoolVisitor.getPool().shutdown();
    			threadPoolVisitor.getPool().awaitTerminationAfterShutdown(getTimeOut(ctx)); //default is 60*1000
    		}
    		catch (final Exception e)
    		{
    			LogSupport.minor(ctx, this,
    					"Exception caught during wait for completion of Retry failed subscriber services task"
    							+ e.getMessage(), e);
    		}
    	} 

    	LogSupport.info(ctx, this, " Subscriber Services Retry  Lifecycle Agent : ENDS");
    }

    
    private String getSqlQuery(Context ctx, String subId){
    	StringBuilder sql = new StringBuilder();
    	
    	sql.append("select id from subscriber where id in (select a.subscriberid from subscriberservices a where a.provisionedState IN (");
    	
    	sql.append(ServiceStateEnum.PROVISIONEDWITHERRORS_INDEX).append(",");
    	sql.append(ServiceStateEnum.UNPROVISIONEDWITHERRORS_INDEX).append(",");
    	sql.append(ServiceStateEnum.SUSPENDEDWITHERRORS_INDEX).append("))");
    	sql.append("AND state NOT IN (").
    		append(SubscriberStateEnum.INACTIVE_INDEX).append(",").
    		append(SubscriberStateEnum.PENDING_INDEX).append(",").
    		append(SubscriberStateEnum.AVAILABLE_INDEX);
    	sql.append(")");
    	
    	if(subId!=null && !subId.trim().equals("")){
    		sql.append(" AND Subscriber.id = ").append("'").append(subId).append("'");
    	}
    	
    	if (LogSupport.isDebugEnabled(ctx)){
    		LogSupport.debug(ctx, this, sql.toString());
    	}

    	return sql.toString();
    }
    
    private int getThreadPoolSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRetrySubscriberServicesThreads();
    }

    /**
     * Get queue size from configuration.
     * 
     * @param ctx
     *            The operating context.
     * @return The recurring charge thread pool queue size.
     */
    private int getThreadPoolQueueSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRetrySubscriberServicesQueueSize();
    }
    
    private long getTimeOut(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRetrySubscriberServicesTimeOut();
    }

}
