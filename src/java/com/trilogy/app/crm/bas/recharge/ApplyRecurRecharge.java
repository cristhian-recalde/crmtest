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
package com.trilogy.app.crm.bas.recharge;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.core.cron.ExitStatusEnum;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.task.executor.bean.CrmTaskStatusEnum;
import com.trilogy.service.task.executor.bean.ScheduleTask;
import com.trilogy.service.task.executor.support.TaskProcessingSupport;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.log.ERLogger;

/**
 * ApplyRecurRecharge - apply monthly service/annual licenses fees to active subscribers on their billing cycle date.
 * 
 * @author lanny.tse@redknee.com
 * @author larry.xia@redknee.com
 * @author cindy.wong@redknee.com
 */
public class ApplyRecurRecharge implements ContextAware, ContextAgent, RechargeConstants
{

    /**
     * The maximum number of days in the future can the billing date be in.
     */
    public static final int NUMBER_OF_DAYS_IN_THE_FUTURE_FOR_BILLING_DATE = 2;

    /**
     * Time out to wait for the thread pool to shut down.
     */
    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;

    /**
     * Create a new instance of <code>ApplyRecurRecharge</code>.
     * 
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent executing this recurring recharge.
     * @param servicePeriod
     *            Service period of this recharge.
     */
    public ApplyRecurRecharge(final Context ctx, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingCycle, LifecycleAgentSupport lifecycleAgent)
    {
        this.billingDate_ = billingDate;
        this.agentName_ = agentName;
        this.chargingCycle_ = chargingCycle;
        this.lifecycleAgent_ = lifecycleAgent;

        try
        {
            setContext(ctx);
        }
        catch (final Exception e)
        {
            LogSupport.info(ctx, this, "Fail to init ApplyRecurRecharge", e);
        }
    }

    public ApplyRecurRecharge(final Context ctx, final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingCycle)
    {
        this(ctx, billingDate, agentName, chargingCycle, null);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx)
    {
        final Date recurringChargeBillingDate = getBillingDate();
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, NUMBER_OF_DAYS_IN_THE_FUTURE_FOR_BILLING_DATE);
        if (recurringChargeBillingDate.after(calendar.getTime()))
        {
            final String errMsg = " Requested Billing Date " + recurringChargeBillingDate
                    + " is beyond 2 days in the future.  " + " The billingDate has to be within 2 days in the future. ";
            LogSupport.major(ctx, this, errMsg);
            return;
        }

		ScheduleTask taskDetails = new ScheduleTask();
        final RechargeAccountVisitor delegate = new RechargeAccountVisitor(getBillingDate(), agentName_, chargingCycle_,
                true, isProrate(), false);
        final ProcessAccountThreadPoolVisitor threadPoolVisitor = new ProcessAccountThreadPoolVisitor(ctx,
                getAccountVisitorThreadPoolSize(ctx), getAccountVisitorThreadPoolQueueSize(ctx), delegate, lifecycleAgent_);

        final RechargeSpidVisitor spidVisitor = new RechargeSpidVisitor(getBillingDate(), agentName_, chargingCycle_,
                threadPoolVisitor, true, false, false);

        try
        {
            final Home home = (Home) ctx.get(CRMSpidHome.class);
            home.forEach(ctx, spidVisitor);
        }

        catch (final Throwable t)
        {
            LogSupport.crit(ctx, this, "Fail to apply recur recharge for billing date " + getBillingDate(), t);
            handleException(ctx, "Fail to apply recur recharge for billing date " + getBillingDate());
        }
        finally
        {
            try
            {
                threadPoolVisitor.getPool().shutdown();
                threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
            }
            catch (final Exception e)
            {
                LogSupport.minor(ctx, this, "exception catched during wait for completion of all recharge thread", e);
            }

            ERLogger.generateRechargeCountEr(ctx, delegate);
            
            fillScheduleTask(ctx, taskDetails, delegate);
            try {
            	TaskProcessingSupport.logHistory(ctx, taskDetails);
            } catch (final Exception e) {
            	if(LogSupport.isDebugEnabled(ctx)){
            		LogSupport.debug(ctx, this, "exception catched during logHistory", e);
            	}
            }
            
        }
    }

    /**
     * Creates recharge error report.
     * 
     * @param ctx
     *            the Context
     * @param reason
     *            error message
     */
    private void handleException(final Context ctx, final String reason)
    {
        try
        {
            RechargeErrorReportSupport.createReport(ctx, agentName_, null, RECHARGE_FAIL_XHOME, OCG_RESULT_UNKNOWN,
                    reason, SYSTEM_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, "", null, -1, "", "", this.getBillingDate(),
                    ChargedItemTypeEnum.UNKNOWN);
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction ", e);
        }
    }
    
    
     private void fillScheduleTask(Context ctx, ScheduleTask taskDetails,  RechargeVisitorCountable delegate) {
		LogSupport.info(ctx, this, "Starting Monthly Recurring Charges - fillScheduleTask");
		
		taskDetails.setTaskRunId(TaskProcessingSupport.getScheduledTaskRunId());
		taskDetails.setNoOfFailed(delegate.getAccountFailCount());
		taskDetails.setNoOfRecordsProcessed(delegate.getAccountCount());
		taskDetails.setNoOfSuccessful(delegate.getAccountSuccessCount());				
		taskDetails.setCompletionTime(new Date());
		
		TaskEntry taskEntry = TaskHelper.retrieve(ctx, CronConstant.MONTHLY_RECURRING_CHARGES_AGENT_NAME);
		if(taskEntry != null){
			taskDetails.setTaskParam1(taskEntry.getParam0());
			taskDetails.setTaskParam2(taskEntry.getParam1());
			taskDetails.setTaskName(taskEntry.getDescription());
			
			if(taskEntry.getExitStatus().equals(ExitStatusEnum.FAILED)){
				taskDetails.setStatus(CrmTaskStatusEnum.FAILED);
			}
			else{
				taskDetails.setStatus(CrmTaskStatusEnum.COMPLETED);
			}
		}
     }
     

    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return context_;
    }

    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        this.context_ = context;
    }

    /**
     * Returns the billingDate.
     * 
     * @return Billing date.
     */
    public Date getBillingDate()
    {
        return billingDate_;
    }

    protected boolean isProrate()
    {
        return false;
    }

    /**
     * Get Thread pool size from configuration.
     * 
     * @param ctx
     *            The operating context.
     * @return The recurring charge thread pool size.
     */
    int getAccountVisitorThreadPoolSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeThreads();
    }

    /**
     * Get queue size from configuration.
     * 
     * @param ctx
     *            The operating context.
     * @return The recurring charge thread pool queue size.
     */
    int getAccountVisitorThreadPoolQueueSize(final Context ctx)
    {
        final GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getRecurringChargeQueueSize();
    }

    /**
     * Operating context.
     */
    private Context context_;

    /**
     * Billing date.
     */
    private final Date billingDate_;

    /**
     * Service period.
     */
    private final ChargingCycleEnum chargingCycle_;

    /**
     * Name of the agent generating the charge.
     */
    private final String agentName_;
    
    private final LifecycleAgentSupport lifecycleAgent_;

}
