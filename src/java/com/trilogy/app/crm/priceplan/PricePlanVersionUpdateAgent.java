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
package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bas.directDebit.EnhancedParallVisitor;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXDBHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.home.sub.PricePlanChangeProhibitingHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.core.cron.ExitStatusEnum;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.task.executor.bean.ScheduleTask;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.service.task.executor.support.TaskProcessingSupport;
import com.trilogy.service.task.executor.bean.CrmTaskStatusEnum;

/**
 * Provides a lifecycle agent to control the thread that checks the
 * PricePlanUpdateHome for updates to perform.
 *
 * @author gary.anderson@redknee.com
 */
public class PricePlanVersionUpdateAgent implements ContextAgent
{
    public void execute(final Context context)
    {
        Context appCtx = CoreSupport.getApplicationContext(context);
        try
        {
            appCtx.put( PRICE_PLAN_VERSION_UPDATE_IN_PROGRESS, true);
            
            
            // Ensure that the updates to the subscriber ignore the check
            // for conflicts, otherwise, this agent would conflict with
            // itself.
            final Context subContext = context.createSubContext();
            subContext.setName(getClass().getName());
            subContext.put(PricePlanChangeProhibitingHome.ENABLED, false);
            subContext.put(PRICE_PLAN_VERSION_UPDATE, true);

            processAllRequests(subContext);
        }
        catch (final Throwable exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            }
        }
        finally
        {
            appCtx.put( PRICE_PLAN_VERSION_UPDATE_IN_PROGRESS, false);
        }
    }

    /**
     * Processes all requests.
     *
     * @param ctx The operating context.
     */
    private void processAllRequests(final Context ctx)
    {
    	ScheduleTask taskDetails = new ScheduleTask();
    	taskDetails.setTaskRunId(TaskProcessingSupport.getScheduledTaskRunId());
    	
        final Home home = (Home) ctx.get(PricePlanVersionUpdateRequestHome.class);
        final EQ condition = new EQ(PricePlanVersionUpdateRequestXInfo.READY, Boolean.TRUE);

        final GeneralConfig genConf = (GeneralConfig)ctx.get(GeneralConfig.class);
        final int threadsCount = genConf.getPpvUpdateThreads();

        Visitor reqVisitor = new PricePlanVersionUpdateVisitor();
        if (threadsCount > 1)
        {
            reqVisitor = new EnhancedParallVisitor(threadsCount, reqVisitor);
        }

        try
        {
            final String tableName = MultiDbSupportHelper.get(ctx).getTableName(ctx, PricePlanVersionUpdateRequestHome.class,
                    PricePlanVersionUpdateRequestXInfo.DEFAULT_TABLE_NAME);
        	
        	StringBuilder query = new StringBuilder();
        	query.append("select REQUESTID from ").append(tableName);
        	query.append(" where ");
        	query.append("Ready = 'y'");
        	
        	String sql = query.toString();
        	Collection <Object> requestIdColl = AccountSupport.getQueryDataList(ctx,sql);
        	LogSupport.info(ctx, this, "Total Number of entry retrived:" + requestIdColl.size());
        	Visitors.forEach(ctx, requestIdColl, reqVisitor);
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(ctx);
            }
        }
        catch (final Exception exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, exception.getMessage(), exception).log(ctx);
            }
        }
        finally
        {
    	        	 try
    	             {
    	        		 EnhancedParallVisitor pv = (EnhancedParallVisitor) reqVisitor;
    	        		 pv.shutdown(EnhancedParallVisitor.TIME_OUT_FOR_SHUTTING_DOWN);
    	        		 
    	        		 taskDetails.setCompletionTime(new Date());
    	        		 
    	        		 TaskEntry taskEntry = TaskHelper.retrieve(ctx, CronConstant.PRICE_PLAN_VERSION_UPDATE_NAME);
    	        		 if(taskEntry != null)
    	        		 {
        	        		 taskDetails.setTaskParam1(taskEntry.getParam0());
        	        		 taskDetails.setTaskParam2(taskEntry.getParam1());
        	        		 taskDetails.setTaskName(taskEntry.getDescription());
        	        		 if (taskEntry.getExitStatus().equals(ExitStatusEnum.FAILED)) {
        	        			 taskDetails.setStatus(CrmTaskStatusEnum.FAILED);
        	        		 } else {
        	        			 taskDetails.setStatus(CrmTaskStatusEnum.COMPLETED);
        	        		 }
    	        		 }
    	        		 
    	        		 LogSupport.info(ctx, PM_MODULE,"Logging History for : " + taskDetails.getTaskName());
    	        		 TaskProcessingSupport.logHistory(ctx, taskDetails);
    	             }
    	             catch (final Exception e)
    	             {
    	                 LogSupport.major(ctx, this, "Exception caught during wait for completion of all Price Plan Version Udate Threads", e);
    	             }
        }
    }

    /**
     * Used to identify this class's PMs.
     */
    private static final String PM_MODULE = PricePlanVersionUpdateAgent.class.getName();

    public static final String PRICE_PLAN_VERSION_UPDATE = "PRICE_PLAN_VERSION_UPDATE";
    
    public static final String PRICE_PLAN_VERSION_UPDATE_IN_PROGRESS = "PRICE_PLAN_VERSION_UPDATE_IN_PROGRESS";
    

} // class
