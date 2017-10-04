/*
_ * This code is a protected work and subject to domestic and international
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
package com.trilogy.app.crm.agent;

import com.trilogy.framework.core.cron.AgentEntry;
import com.trilogy.framework.core.cron.AgentEntryHome;
import com.trilogy.framework.core.cron.AgentHelper;
import com.trilogy.framework.core.cron.SchedulerConfigException;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskEntryHome;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.framework.core.cron.XCronLifecycleAgentControlConfig;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.lifecycle.RunnableLifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.menu.XMenuSqlShellConfig;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.core.cron.TaskStatusEnum;
import com.trilogy.service.task.executor.support.TaskProcessingSupport;
import com.trilogy.app.crm.bas.recharge.DiscountEventLifecycleAgent;

import com.trilogy.app.crm.account.BillCycleChangeLifecycleAgent;
import com.trilogy.app.crm.bas.directDebit.CreateDirectDebitRequestService;
import com.trilogy.app.crm.bas.directDebit.DirectDebitOvertimeUpdateAgent;
import com.trilogy.app.crm.bas.promotion.ReportCronAgent;
import com.trilogy.app.crm.bas.recharge.DailyRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.MonthlyRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.MultiDayRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.RecurringRechargeInsufficientBalancePreWarnNotificationLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.RecurringRechargePreWarnNotificationLifecycleAgent;
import com.trilogy.app.crm.bas.recharge.WeeklyRecurringRechargesLifecycleAgent;
import com.trilogy.app.crm.bas.roamingcharges.ApplyRoamingChargesCronAgent;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitHome;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitXDBHome;
import com.trilogy.app.crm.bean.UserDailyAdjustmentLimitXInfo;
import com.trilogy.app.crm.bean.deposit.DepositReleaseProcessingAgent;
import com.trilogy.app.crm.bean.paymentgatewayintegration.DirectDebitOutboundFileProcessorLifecycleAgent;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PostpaidRecurringCreditCardTopUpAgent;
import com.trilogy.app.crm.bean.paymentgatewayintegration.RecurringCreditCardTopUpLifecycleAgent;
import com.trilogy.app.crm.bean.refund.GenerateAutomaticRefundAgent;
import com.trilogy.app.crm.bulkloader.generic.request.BulkLoadLifeCycleAgent;
import com.trilogy.app.crm.clean.AccountCleanUpCronAgent;
import com.trilogy.app.crm.clean.AuxiliaryServicesProvisioningLifecycleAgent;
import com.trilogy.app.crm.clean.AuxiliaryServicesUnprovisioningLifecycleAgent;
import com.trilogy.app.crm.clean.AvailablePendingSubCronAgent;
import com.trilogy.app.crm.clean.BalanceBundleUsageSummaryCleanUpAgent;
import com.trilogy.app.crm.clean.BalanceBundleUsageSummaryLifeCycleAgent;
import com.trilogy.app.crm.clean.BundleAuxiliaryServiceCronAgent;
import com.trilogy.app.crm.clean.ClosedAuxiliaryServiceCleanUpCronAgent;
import com.trilogy.app.crm.clean.DeactivateInCollectionAccountCronAgent;
import com.trilogy.app.crm.clean.MSISDNDeletionCronAgent;
import com.trilogy.app.crm.clean.MSISDNStateModifyCronAgent;
import com.trilogy.app.crm.clean.PackageStateModifyCronAgent;
import com.trilogy.app.crm.clean.SubscriberCleanUpCronAgent;
import com.trilogy.app.crm.clean.SubscriberFiniteBalanceDormantToDeactivationCronAgent;
import com.trilogy.app.crm.clean.SubscriberZeroBalanceDormantToDeactivationCronAgent;
import com.trilogy.app.crm.clean.TransactionCleanUpCronAgent;
import com.trilogy.app.crm.clean.TransferExceptionCleanUpCronAgent;
import com.trilogy.app.crm.cltc.SubscriberCltcCleanUpCronAgent;
import com.trilogy.app.crm.contract.SubscriptionContractEndUpdateCronAgent;
import com.trilogy.app.crm.contract.SubscriptionContractHistoryCleanupCronAgent;
import com.trilogy.app.crm.creditcategoryupdate.task.CreditCategoryUpdateLifecycleAgent;
import com.trilogy.app.crm.datamart.cron.UserDumpAgent;
import com.trilogy.app.crm.deposit.AutoDepositReleaseCronAgent;
import com.trilogy.app.crm.dunning.task.DunningPolicyAssignementLifecycleAgent;
import com.trilogy.app.crm.dunning.task.DunningProcessLifecycleAgent;
import com.trilogy.app.crm.dunning.task.DunningReportGenerationLifecycleAgent;
import com.trilogy.app.crm.dunning.task.DunningReportProcessingLifecycleAgent;
import com.trilogy.app.crm.migration.PricePlanMigrationCronAgent;
import com.trilogy.app.crm.notification.DunningNoticeLifecycleAgent;
import com.trilogy.app.crm.numbermgn.MobileNumGrpMonitorAgent;
import com.trilogy.app.crm.paymentprocessing.InvoicePaymentProcessingCronAgent;
import com.trilogy.app.crm.paymentprocessing.LateFeeAgent;
import com.trilogy.app.crm.paymentprocessing.OnTimePaymentCronAgent;
import com.trilogy.app.crm.pos.AccountPointOfSaleLifecycleAgent;
import com.trilogy.app.crm.pos.SubscriberPointOfSaleLifecycleAgent;
import com.trilogy.app.crm.priceplan.AutoActivationCronAgent;
import com.trilogy.app.crm.priceplan.PricePlanServiceMonitoringAgent;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.app.crm.priceplan.ScheduledSubscriberModificationExecutorCronAgent;
import com.trilogy.app.crm.priceplan.SecondaryPricePlanActivationAgent;
import com.trilogy.app.crm.priceplan.task.PricePlanVersionModificationLifecycleAgent;
import com.trilogy.app.crm.provision.RechargeSubscriberServicesOnPaymentsLifeCycleAgent;
import com.trilogy.app.crm.provision.SubscriberServicesRetryLifeCycleAgent;
import com.trilogy.app.crm.subscriber.agent.FixedStopPricePlanSubscriberExtensionLifecycleAgent;
import com.trilogy.app.crm.subscriber.cron.SubscriberExpiredToDormantAgent;
import com.trilogy.app.crm.subscriber.cron.SubscriberFutureActiveOrDeactiveAgent;
import com.trilogy.app.crm.subscriber.cron.SubscriberInAvailableOrExpiredAgent;
import com.trilogy.app.crm.subscriber.cron.SubsriberPreExpiryAgent;
import com.trilogy.app.crm.subscriber.service.cron.PackageNotificationCronAgent;
import com.trilogy.app.crm.support.DeploymentTypeSupportHelper;
import com.trilogy.app.crm.support.MultiDbSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.transaction.task.OverPaymentProcessingLifecycleAgent;
import com.trilogy.app.crm.transaction.task.UnappliedTransactionProcessingLifecycleAgent;
import com.trilogy.app.crm.writeoff.WriteOffCronAgent;
import com.trilogy.app.crm.subscriber.agent.SubscriptionSegmentUpdateLifecycleAgent;
import com.trilogy.app.crm.discount.DiscountingClassAssignmentLifecycleAgent;

/**
 * This agent installs the cron tasks used by the app.
 *
 * @author ltse@redknee.com
 */
public class CronInstall extends CoreSupport implements ContextAgent, CronConstant
{
    /**
     * this agent adds to the scheduler all the custom agents that crm uses.
     *
     * @param ctx the context used to locate the scheduler.
     * @throws AgentException
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        Context subCtx = ctx.createSubContext();
        
        
        
        // Using the CRON_INSTALL_TASK_ENTRY_HOME TaskEntryHome, as the default TaskEntryHome filters off disabled cron
        // agents, but we still want to find those during installation.
        Home home = (Home) ctx.get(CRON_INSTALL_TASK_ENTRY_HOME);
        if (home!=null)
        {
            subCtx.put(TaskEntryHome.class, home);
        }
        
        try
        {
        	 installGenericBeanBulkLoadCronAgent(subCtx);
        	 
            installMobileNumGrpMonitorCronAgent(subCtx);
            installSecondaryPricePlanActivationCronAgent(subCtx);
            installPricePlanServiceMonitoringCronAgent(subCtx);

            installRecurringChargesAgents(subCtx);

            installDunningCronAgents(subCtx); 
            installDiscountingClassAssignmentRuleEngineCronAgent(subCtx);
            installDiscountEventAgent(subCtx);
            
            installDunninPolicyRuleEngineCronAgent(subCtx);
            
            installCleanUpCronAgents(subCtx);

            //installSubscriberScheduledActivationDeactivationCronAgent(subCtx);


            installApplyRoamingChargesCronAgent(subCtx);
            installCleanUpCronAgent(subCtx);
            installReportCronAgent(subCtx);
            AutoActivationCronAgent.install(subCtx);
            installAuxiliaryServiceCronAgent(subCtx);
            installRecurringTopUpCronAgent(subCtx);
            installPostpaidRecurringCreditCardTopUpAgent(subCtx);
            installDirectDebitOubboundFileProcessorCronAgent(subCtx);
            installSubscriberStateUpdateCronAgent(subCtx);
            if (SystemSupport.supportsInCollection(subCtx))
            {
                installDeactivateInCollectionAccountCronAgent(subCtx);
            }
            installLNPCronAgent(subCtx);
            installReratedCallDetailAlarmCronAgent(subCtx);
            installBundleAuxiliaryServiceCronAgent(subCtx);

            // Install the cron agent that propagates price plan version changes
            // to affected subscribers.
            if (!DeploymentTypeSupportHelper.get(subCtx).isEcare(subCtx))
            {
                installPricePlanVersionUpdateCronAgent(subCtx);
            }

            //Install the cron for DataMart mining
            installDataMartCronAgent(subCtx);
            installPointOfSaleExtractors(subCtx);

            /*
             * This wraps all cron tasks in the system with Cron Stat collection agent
             *
             */
            installCronStatCollection(subCtx);

            PricePlanMigrationCronAgent.install(subCtx);

            installAutoDepositReleaseCronAgent(subCtx);

            installOnTimePaymentCronAgent(subCtx);
            
            installLateFeeEarlyRewardCronAgent(subCtx);
            
            installDirectDebitOutboundCronAgents(subCtx);

            installDirectDebitCleanupCronAgents(subCtx);
            installContractEndUpdateCrontAgent(subCtx);
            installContractHistoryCleanupCronAgent(subCtx);
            installScheduledSubscriberModificationExecutionCronAgent(subCtx);
            installRechargeServicesUponPaymentsServicesAgent(subCtx);
            installBillCycleChangeAgent(subCtx);
            
            installFixedStopPricePlanSubscriberAgent(subCtx);
            
            installPricePlanModificationAgent(subCtx);
            
            installSubscriberServicesRetryLifeCycleAgent(subCtx);
            
            installOverPaymentProcessingCronAgent(subCtx);

            installLifeCycleCronAgent(subCtx, UNAPPLIED_TRANSACTION_CRON_NAME, UNAPPLIED_TRANSACTION_CRON_DESCRIPTION,
                    UNAPPLIED_TRANSACTION_CRON_DESCRIPTION, "every10minutes", TaskStatusEnum.SCHEDULED, 
                    TaskStatusEnum.SCHEDULED, UnappliedTransactionProcessingLifecycleAgent.class,
                    new Class[]{Context.class, String.class}, 
                    ctx, UNAPPLIED_TRANSACTION_CRON_NAME);

            
            installApplyWriteOffCronAgent(subCtx);
            installServiceExpiryNotificationCronAgent(subCtx);
            installBalanceBundleUsageSummaryAgent(subCtx);
            installBalanceBundleUsageSummaryCleanUpAgent(subCtx);
			installSubscriptionSegmentUpdateLifecycleAgent(subCtx);
			installGenerateAutomaticRefundSchedulerAgent(subCtx);
			//BSS-5196 Auto deposit release
			installDepositReleaseProcessingAgent(subCtx);
			installCreditCategoryUpdateProcessingAgent(subCtx);
        }
        catch (final Exception e)
        {
            new MajorLogMsg(this, "fail to install Cron Tasks " + e.getMessage(), e).log(ctx);
        }
    }

    private void installDepositReleaseProcessingAgent(Context subCtx) {
		installLifeCycleCronAgent(subCtx,DEPOSIT_RELEASE_PROCESSING_AGENT_NAME,DEPOSIT_RELEASE_PROCESSING_AGENT_DESCRIPTION,
				DEPOSIT_RELEASE_PROCESSING_AGENT_DESCRIPTION,CRM_DAILY_5AM_CRON_ENTRY,
				TaskStatusEnum.AVAILABLE,TaskStatusEnum.AVAILABLE,
				DepositReleaseProcessingAgent.class,new Class[]{Context.class, String.class},
				subCtx,DEPOSIT_RELEASE_PROCESSING_AGENT_NAME);
	}

    private void installCreditCategoryUpdateProcessingAgent(Context ctx) {
    	installLifeCycleCronAgent(ctx,CREDIT_CATEGORY_UPDATE_ASSIGNMENT_NAME,CREDIT_CATEGORY_UPDATE_ASSIGNMENT_DESCRIPTION,
				CREDIT_CATEGORY_UPDATE_ASSIGNMENT_DESCRIPTION,"never",
				TaskStatusEnum.DISABLED,TaskStatusEnum.DISABLED,
				CreditCategoryUpdateLifecycleAgent.class,
				new Class[]{Context.class, String.class},
				ctx,CREDIT_CATEGORY_UPDATE_ASSIGNMENT_NAME);		
	}


	/**
     * @param subCtx
     */
    private void installBalanceBundleUsageSummaryCleanUpAgent(Context subCtx)
    {
        installLifeCycleCronAgent(subCtx, 
                BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_NAME, 
                BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_NAME,
                BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_DESCRIPTION, 
                "CRM_Daily_2AM", 
                TaskStatusEnum.AVAILABLE, 
                TaskStatusEnum.AVAILABLE, 
                BalanceBundleUsageSummaryCleanUpAgent.class,
                new Class[]{Context.class, String.class}, 
                subCtx, 
                BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_NAME);
    }

    /**
     * @param subCtx
     */
    private void installBalanceBundleUsageSummaryAgent(Context subCtx)
    {
        installLifeCycleCronAgent(subCtx, 
                BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_NAME, 
                BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_DESCRIPTION,
                BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_DESCRIPTION, 
                "CRM_Daily_2AM", 
                TaskStatusEnum.DISABLED, 
                TaskStatusEnum.AVAILABLE, 
                BalanceBundleUsageSummaryLifeCycleAgent.class,
                new Class[]{Context.class, String.class}, 
                subCtx, 
                BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_NAME);
    }

    /**
	 * @param subCtx
	 */
	private void installSubscriberServicesRetryLifeCycleAgent(Context subCtx) {
		installLifeCycleCronAgent(subCtx, FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_NAME, FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_DESCRIPTION,
				FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_DESCRIPTION, "never", TaskStatusEnum.DISABLED, 
                TaskStatusEnum.DISABLED, SubscriberServicesRetryLifeCycleAgent.class,
                new Class[]{Context.class, String.class}, 
                subCtx, FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_NAME);
	}


	private void installPricePlanModificationAgent(Context ctx)
    {
        installLifeCycleCronAgent(ctx, PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_NAME, PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_DESCRIPTION,
        		PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_DESCRIPTION, "midnight", TaskStatusEnum.SCHEDULED, 
                TaskStatusEnum.SCHEDULED, PricePlanVersionModificationLifecycleAgent.class,
                new Class[]{Context.class, String.class}, 
                ctx, PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_NAME);
    }

    private void installBillCycleChangeAgent(Context ctx)
    {
        installLifeCycleCronAgent(ctx, BILL_CYCLE_CHANGE_AGENT_NAME, BILL_CYCLE_CHANGE_AGENT_DESCRIPTION,
                BILL_CYCLE_CHANGE_AGENT_DESCRIPTION, "CRM_Daily_2AM",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE,
                BillCycleChangeLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, BILL_CYCLE_CHANGE_AGENT_NAME);
    }

    private void installFixedStopPricePlanSubscriberAgent(Context ctx)
    {
        installLifeCycleCronAgent(ctx, SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_NAME, SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_DESCRIPTION,
                SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_HELP, "CRM_Daily_2AM",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE,
                FixedStopPricePlanSubscriberExtensionLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_NAME);
    }
	/**
	 * @param subCtx
	 */
    private void installSubscriptionSegmentUpdateLifecycleAgent(Context ctx)
    {
    	installLifeCycleCronAgent(ctx, 
    			SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_NAME, 
    			SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_NAME,
    			SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_DESCRIPTION, 
                "CRM_Daily_2AM", 
                TaskStatusEnum.AVAILABLE, 
                TaskStatusEnum.AVAILABLE, 
                SubscriptionSegmentUpdateLifecycleAgent.class,
                new Class[]{Context.class, String.class}, 
                ctx, 
                SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_NAME);
    }
    private void installCronStatCollection(final Context ctx)
    {
        final Home home = (Home) ctx.get(AgentEntryHome.class);
        try
        {
            home.forEach(ctx, new CloneingVisitor(new Visitor()
            {
                @Override
                public void visit(final Context ctx, final Object obj) throws AgentException, AbortVisitException
                {
                    final AgentEntry entry = (AgentEntry) obj;

                    try
                    {
                        entry.setAgent(new CronStatCollectorAgent(entry.getAgent()));
                        home.store(ctx, entry);
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Installs an agent that when run create direct debit out bound files.
     *
     * @param ctx context where the agent runs.
     */
    public void installDirectDebitOutboundCronAgents(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(CreateDirectDebitRequestService.AGENT_NAME);
            entry.setAgent(new CreateDirectDebitRequestService());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding usage direct debit out bound  agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, CreateDirectDebitRequestService.AGENT_NAME);
            task.setName(CreateDirectDebitRequestService.AGENT_NAME);
            task.setCronEntry("CRM_Daily_1AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(CreateDirectDebitRequestService.AGENT_DISCRIPTION);
            task.setHelp(CreateDirectDebitRequestService.AGENT_DISCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding direct debit out bound  agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }

    
    /* Installs an agent that when run applies roaming charges to accounts.
    *
    * @param ctx context where the agent runs.
    */
   public void installDirectDebitCleanupCronAgents(final Context ctx)
   {
       try
       {
           final AgentEntry entry = new AgentEntry();
           entry.setName(DirectDebitOvertimeUpdateAgent.AGENT_NAME);
           entry.setAgent(new DirectDebitOvertimeUpdateAgent());
           entry.setContext(ctx);

           AgentHelper.add(ctx, entry);
           if (LogSupport.isDebugEnabled(ctx))
           {
               new DebugLogMsg(this, "Adding usage direct debit time out  agent!", null).log(ctx);
           }

           final TaskEntry task = new TaskEntry();
           AgentHelper.makeAgentEntryConfig(task, DirectDebitOvertimeUpdateAgent.AGENT_NAME);
           task.setName(DirectDebitOvertimeUpdateAgent.AGENT_NAME);
           task.setCronEntry("CRM_Daily_1AM");
           task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
           task.setStatus(TaskStatusEnum.AVAILABLE);
           task.setDescription(DirectDebitOvertimeUpdateAgent.AGENT_DISCRIPTION);
           task.setHelp(DirectDebitOvertimeUpdateAgent.AGENT_DISCRIPTION);

           if (TaskHelper.retrieve(ctx, task.getName()) == null)
           {
               TaskHelper.add(ctx, task);
           }

           if (LogSupport.isDebugEnabled(ctx))
           {
               new DebugLogMsg(this, "Done adding direct debit time out  agent!", null).log(ctx);
           }
       }
       catch (final SchedulerConfigException e)
       {
           new MajorLogMsg(this, e.getMessage(), e).log(ctx);
       }
   }
   
   
   private void installDiscountEventAgent(final Context ctx) {
		installLifeCycleCronAgent(ctx, FINAL_DISCOUNT_EVENT_AGENT_NAME, FINAL_DISCOUNT_EVENT_AGENT_DESCRIPTION,
				FINAL_DISCOUNT_EVENT_AGENT_DESCRIPTION, "never", TaskStatusEnum.DISABLED,
				TaskStatusEnum.DISABLED, DiscountEventLifecycleAgent.class, 
				new Class[]{Context.class , String.class}, ctx , FINAL_DISCOUNT_EVENT_AGENT_NAME);
		
	}
    
    /**
     * Installs an agent that when run applies roaming charges to accounts.
     *
     * @param ctx context where the agent runs.
     */
    public void installReportCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(REPORT_AGENT_NAME);
            entry.setAgent(new ReportCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding usage report agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, REPORT_AGENT_NAME);
            task.setName(REPORT_AGENT_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(REPORT_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(REPORT_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding usage report agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }
    
    public void installApplyWriteOffCronAgent(Context ctx)
    {
        try
        {
            AgentEntry entry = new AgentEntry();
            entry.setName(WRITE_OFF_AGENT_NAME);
            entry.setAgent(new WriteOffCronAgent());
            entry.setContext(ctx);
            
            AgentHelper.add(ctx, entry);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Installing Write-off Cron Agent");
            }
            
            TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, WRITE_OFF_AGENT_NAME);
            task.setName(WRITE_OFF_AGENT_NAME);
            task.setCronEntry("never");
            task.setDefaultStatus(TaskStatusEnum.DISABLED);
            task.setDescription(WRITE_OFF_AGENT_DESC);
            
            if (TaskHelper.retrieve(ctx, task.getName())==null)
            {
                TaskHelper.add(ctx, task);
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Write-off Cron Agent has been installed.");
            }
        }
        catch (SchedulerConfigException e)
        {
            LogSupport.major(ctx, this, "Exception encountered when installing Write-off Cron Agent.", e);
        }
    }

    public void installServiceExpiryNotificationCronAgent(Context ctx)
    {
        try
        {
            AgentEntry entry = new AgentEntry();
            entry.setName(SERVICE_EXPIRY_NOTIFICATION_AGENT_NAME);
            entry.setAgent(new PackageNotificationCronAgent());
            entry.setContext(ctx);
            
            AgentHelper.add(ctx, entry);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Installing Service Recurrence and Expiry Notification Cron Agent");
            }
            
            TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SERVICE_EXPIRY_NOTIFICATION_AGENT_NAME);
            task.setName(SERVICE_EXPIRY_NOTIFICATION_AGENT_NAME);
            task.setCronEntry("never");
            task.setDefaultStatus(TaskStatusEnum.DISABLED);
            task.setDescription(SERVICE_EXPIRY_NOTIFICATION_AGENT_DESC);
            
            if (TaskHelper.retrieve(ctx, task.getName())==null)
            {
                TaskHelper.add(ctx, task);
            }
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Service Recurrence and Expiry Notification Cron Agent has been installed.");
            }
        }
        catch (SchedulerConfigException e)
        {
            LogSupport.major(ctx, this, "Exception encountered when installing Service Recurrence and Expiry Notification Cron Agent.", e);
        }
    }
    /**
     * Installs an agent that when run applies roaming charges to accounts.
     *
     * @param ctx context where the agent runs.
     */
    public void installApplyRoamingChargesCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(APPLY_ROAMING_CHARGES_AGENT_NAME);
            entry.setAgent(new ApplyRoamingChargesCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, APPLY_ROAMING_CHARGES_AGENT_NAME);
            task.setName(APPLY_ROAMING_CHARGES_AGENT_NAME);
            task.setCronEntry("everyDay");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(APPLY_ROAMING_CHARGES_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(APPLY_ROAMING_CHARGES_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }


    /**
     * Installs all agents that cleanup various tables/homes.
     *
     * @param ctx context where the agent runs.
     */
    public void installCleanUpCronAgent(final Context ctx)
    {

        // install clean-up cron agent
        try
        {

            // 1.  install MSISDNStateModifyCronAgent
            AgentEntry entry = new AgentEntry();
            entry.setName(MSISDN_STATE_MODIFY_AGENT_NAME);
            entry.setAgent(new MSISDNStateModifyCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, MSISDN_STATE_MODIFY_AGENT_NAME);
            task.setName(MSISDN_STATE_MODIFY_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(MSISDN_STATE_MODIFY_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(MSISDN_STATE_MODIFY_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
            
            // 1.b  install MSISDNStateModifyCronAgent
            entry = new AgentEntry();
            entry.setName(MSISDN_DELETION_AGENT_NAME);
            entry.setAgent(new MSISDNDeletionCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, MSISDN_DELETION_AGENT_NAME);
            task.setName(MSISDN_DELETION_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.DISABLED);
            task.setStatus(TaskStatusEnum.DISABLED);
            task.setDescription(MSISDN_DELETION_AGENT_DESCRIPTION);
            task.setHelp(MSISDN_DELETION_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // 2.  install PackageStateModifyCronAgent
            entry = new AgentEntry();
            entry.setName(PACKAGE_STATE_MODIFY_AGENT_NAME);
            entry.setAgent(new PackageStateModifyCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, PACKAGE_STATE_MODIFY_AGENT_NAME);
            task.setName(PACKAGE_STATE_MODIFY_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(PACKAGE_STATE_MODIFY_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(PACKAGE_STATE_MODIFY_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // 4. Subscriber Cleanup
            entry = new AgentEntry();
            entry.setName(SUSCRIBER_CLEANUP_AGENT_NAME);
            entry.setAgent(new SubscriberCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUSCRIBER_CLEANUP_AGENT_NAME);
            task.setName(SUSCRIBER_CLEANUP_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUSCRIBER_CLEANUP_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(SUSCRIBER_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }


            // 4.b Subscriber Deactivation
            entry = new AgentEntry();
            entry.setName(SUBSCRIBER_ZERO_DEACTIVATION_AGENT_NAME);
            entry.setAgent(new SubscriberZeroBalanceDormantToDeactivationCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBER_ZERO_DEACTIVATION_AGENT_NAME);
            task.setName(SUBSCRIBER_ZERO_DEACTIVATION_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBER_ZERO_DEACTIVATION_AGENT_DESCRIPTION);

            task.setHelp(SUBSCRIBER_ZERO_DEACTIVATION_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // 4.b Subscriber Deactivation with remainong balance write-off.
            entry = new AgentEntry();
            entry.setName(SUBSCRIBER_FINITE_DEACTIVATION_AGENT_NAME);
            entry.setAgent(new SubscriberFiniteBalanceDormantToDeactivationCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBER_FINITE_DEACTIVATION_AGENT_NAME);
            task.setName(SUBSCRIBER_FINITE_DEACTIVATION_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBER_FINITE_DEACTIVATION_AGENT_DESCRIPTION);

            task.setHelp(SUBSCRIBER_FINITE_DEACTIVATION_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }


            // 5. Account Cleanup
            entry = new AgentEntry();
            entry.setName(ACCOUNT_CLEANUP_AGENT_NAME);
            entry.setAgent(new AccountCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, ACCOUNT_CLEANUP_AGENT_NAME);
            task.setName(ACCOUNT_CLEANUP_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(ACCOUNT_CLEANUP_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(ACCOUNT_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // 7. Transaction History
            entry = new AgentEntry();
            entry.setName(TRANSACTION_HISTORY_CLEANUP_AGENT_NAME);
            entry.setAgent(new TransactionCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, TRANSACTION_HISTORY_CLEANUP_AGENT_NAME);
            task.setName(TRANSACTION_HISTORY_CLEANUP_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_One");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(TRANSACTION_HISTORY_CLEANUP_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(TRANSACTION_HISTORY_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }



            // 12.  install AvailablePendingSubCronAgent
            // if the subscribers are created in active, then we shouldn't move to Available from Pending
            // We also have to support subscribers being created in Pending state, otherwise we don't need this crontask
            if (!SystemSupport.supportsPrepaidCreationInActiveState(ctx) && SystemSupport.supportsPrepaidPendingState(ctx))
            {
                installAvailablePendingPrepaidSubscriberAgent(ctx);
            }

            //subscriber cltc clean up
            entry = new AgentEntry();
            entry.setName(SUBSCRIBERCLTC_CLEANUP_AGENT_NAME);
            entry.setAgent(new SubscriberCltcCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBERCLTC_CLEANUP_AGENT_NAME);
            task.setName(SUBSCRIBERCLTC_CLEANUP_AGENT_NAME);
            task.setCronEntry("CRM_Weekly_Two");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBERCLTC_CLEANUP_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(SUBSCRIBERCLTC_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // Transfer Exception clean up
            entry = new AgentEntry();
            entry.setName(TRANSFER_EXCEPTION_CLEANUP_AGENT_NAME);
            entry.setAgent(new TransferExceptionCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, TRANSFER_EXCEPTION_CLEANUP_AGENT_NAME);
            task.setName(TRANSFER_EXCEPTION_CLEANUP_AGENT_NAME);
            task.setCronEntry("never");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(TRANSFER_EXCEPTION_CLEANUP_AGENT_DESCRIPTION);

            task.setHelp(TRANSFER_EXCEPTION_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            // Subscriber Auxiliary Service clean up
            entry = new AgentEntry();
            entry.setName(CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_NAME);
            entry.setAgent(new ClosedAuxiliaryServiceCleanUpCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_NAME);
            task.setName(CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_NAME);
            task.setCronEntry("everyDay");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_DESCRIPTION);

            task.setHelp(CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
        }
        catch (final SchedulerConfigException e)
        {
            e.printStackTrace();
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }


    private void installAvailablePendingPrepaidSubscriberAgent(final Context ctx) throws SchedulerConfigException
    {
        AgentEntry entry;
        TaskEntry task;
        entry = new AgentEntry();
        entry.setName(AVAILABLE_PENDING_SUB_AGENT_NAME);
        entry.setAgent(new AvailablePendingSubCronAgent());
        entry.setContext(ctx);

        AgentHelper.add(ctx, entry);

        task = new TaskEntry();
        AgentHelper.makeAgentEntryConfig(task, AVAILABLE_PENDING_SUB_AGENT_NAME);
        task.setName(AVAILABLE_PENDING_SUB_AGENT_NAME);
        task.setCronEntry("CRM_Daily_4AM");
        task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
        task.setStatus(TaskStatusEnum.AVAILABLE);
        task.setDescription(AVAILABLE_PENDING_SUB_AGENT_DESCRIPTION);

        /*
         * [Cindy] 2007-10-16: using description as help for now.
         */
        task.setHelp(AVAILABLE_PENDING_SUB_AGENT_DESCRIPTION);

        if (TaskHelper.retrieve(ctx, task.getName()) == null)
        {
            TaskHelper.add(ctx, task);
        }

    }

    public void installRecurringChargesAgents(final Context ctx)
    {
    	installLifeCycleCronAgent(ctx, MULTI_DAY_RECURRING_CHARGES_AGENT_NAME, MULTI_DAY_RECURRING_CHARGES_AGENT_DESCRIPTION,
    			MULTI_DAY_RECURRING_CHARGES_AGENT_DESCRIPTION, "daily",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                MultiDayRecurringRechargesLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, MULTI_DAY_RECURRING_CHARGES_AGENT_NAME);
    	
    	
        installLifeCycleCronAgent(ctx, MONTHLY_RECURRING_CHARGES_AGENT_NAME, MONTHLY_RECURRING_CHARGES_AGENT_DESCRIPTION,
                MONTHLY_RECURRING_CHARGES_AGENT_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                MonthlyRecurringRechargesLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, MONTHLY_RECURRING_CHARGES_AGENT_NAME);
        
        installLifeCycleCronAgent(ctx, WEEKLY_RECURRING_CHARGES_AGENT_NAME, WEEKLY_RECURRING_CHARGES_AGENT_DESCRIPTION,
                WEEKLY_RECURRING_CHARGES_AGENT_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                WeeklyRecurringRechargesLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, WEEKLY_RECURRING_CHARGES_AGENT_NAME);
        
        installLifeCycleCronAgent(ctx, DAILY_RECURRING_CHARGES_AGENT_NAME, DAILY_RECURRING_CHARGES_AGENT_DESCRIPTION,
                DAILY_RECURRING_CHARGES_AGENT_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                DailyRecurringRechargesLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, DAILY_RECURRING_CHARGES_AGENT_NAME);
        
        installLifeCycleCronAgent(ctx, RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_NAME, RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION,
                RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                RecurringRechargePreWarnNotificationLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_NAME);        
        
        installLifeCycleCronAgent(ctx, RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_NAME, RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION,
        		RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                RecurringRechargeInsufficientBalancePreWarnNotificationLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_NAME);
        
    }

    public void installCleanUpCronAgents(final Context ctx)
    {
       	
        StringBuilder script = new StringBuilder();
        script.append("DELETE FROM ");
        script.append(MultiDbSupportHelper.get(ctx).getTableName(ctx, UserDailyAdjustmentLimitHome.class, UserDailyAdjustmentLimitXInfo.DEFAULT_TABLE_NAME));
        script.append(" WHERE ");
        
        if(MultiDbSupportHelper.get(ctx).getDbsType(ctx) == MultiDbSupport.ORACLE || MultiDbSupportHelper.get(ctx).getDbsType(ctx) == MultiDbSupport.MYSQL){
        	
        	script.append(UserDailyAdjustmentLimitXInfo.LIMIT_DATE.getName());

        }else{
        	script.append("left(");
        	script.append(UserDailyAdjustmentLimitXInfo.LIMIT_DATE.getName());
        	script.append(",10)"); //For SQL Server TT#13032919006. Truncating to 10 digits to get the value till seconds instead of milliseconds.
        }
        script.append(" <= ");
        if (MultiDbSupportHelper.get(ctx).getDbsType(ctx) == MultiDbSupport.ORACLE)
        {
            script.append("(TRUNC(SYSDATE- %PARAM0%) - TO_DATE('01011970','DDMMYYYY')) *60*60*24*1000");
        }
        else if (MultiDbSupportHelper.get(ctx).getDbsType(ctx) == MultiDbSupport.MYSQL)
        {
            script.append("unix_timestamp(DATE_ADD(DATE(NOW()), INTERVAL -%PARAM0% DAY)) * 1000");
        }
        else
        {
            script.append("(SELECT DATEDIFF(second, '1970-01-01 00:00:00', DATEADD(day, -%PARAM0%, CAST(FLOOR(CAST(GETDATE() AS DECIMAL(12, 5))) AS DATETIME))))");
        }
        
        installSQLScriptCronAgent(ctx, USER_DAILY_ADJUSTMENT_LIMIT_CLEAN_UP_AGENT_NAME, USER_DAILY_ADJUSTMENT_LIMIT_CLEAN_UP_AGENT_DESCRIPTION,
                USER_DAILY_ADJUSTMENT_LIMIT_CLEAN_UP_AGENT_DESCRIPTION, "CRM_Daily_4AM",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE, script.toString(), "60");
    }
    
    public void installDunningCronAgents(final Context ctx)
    {
        installLifeCycleCronAgent(ctx, DUNNING_REPORT_PROCESSING_NAME, DUNNING_REPORT_PROCESSING_DESCRIPTION,
                DUNNING_REPORT_PROCESSING_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED, 
                DunningReportProcessingLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, DUNNING_REPORT_PROCESSING_NAME);

        installLifeCycleCronAgent(ctx, DUNNING_REPORT_GENERATION_NAME, DUNNING_REPORT_GENERATION_DESCRIPTION,
                DUNNING_REPORT_GENERATION_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED,
                DunningReportGenerationLifecycleAgent.class,
                new Class[]{Context.class, String.class}, 
                ctx, DUNNING_REPORT_GENERATION_NAME);

        installLifeCycleCronAgent(ctx, DUNNING_NOTICE_NAME, DUNNING_NOTICE_DESCRIPTION,
                DUNNING_NOTICE_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED,
                DunningNoticeLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, DUNNING_NOTICE_NAME);

        installLifeCycleCronAgent(ctx, DUNNING_PROCESS_NAME, DUNNING_PROCESS_DESCRIPTION,
                DUNNING_PROCESS_DESCRIPTION, "never",
                TaskStatusEnum.DISABLED, TaskStatusEnum.DISABLED,
                DunningProcessLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, DUNNING_PROCESS_NAME);
        
        
    }
    
    public void installDunninPolicyRuleEngineCronAgent(final Context ctx)
	{
		installLifeCycleCronAgent(ctx,DUNNING_POLICY_ASSIGNMENT_NAME,DUNNING_POLICY_ASSIGNMENT_DESCRIPTION,
				DUNNING_POLICY_ASSIGNMENT_DESCRIPTION,"never",
				TaskStatusEnum.DISABLED,TaskStatusEnum.DISABLED,
				DunningPolicyAssignementLifecycleAgent.class,
				new Class[]{Context.class, String.class},
				ctx,DUNNING_POLICY_ASSIGNMENT_NAME);
	}
    
    /**
	 * Installs agent which identifies applicable Discount Class and assigned to subscriptions.
	 * @param ctx
	 */
	public void installDiscountingClassAssignmentRuleEngineCronAgent(final Context ctx)
    {
        installLifeCycleCronAgent(ctx,DISCOUNT_ASSIGNMENT_TASK_NAME,DISCOUNT_ASSIGNMENT_TASK_DESCRIPTION,
                DISCOUNT_ASSIGNMENT_TASK_DESCRIPTION,"never",
                TaskStatusEnum.DISABLED,TaskStatusEnum.DISABLED,
                DiscountingClassAssignmentLifecycleAgent.class,
                new Class[]{Context.class, String.class},
                ctx,DISCOUNT_ASSIGNMENT_TASK_NAME);
    }
    
    public void installGenericBeanBulkLoadCronAgent(final Context ctx)
    {
    	installLifeCycleCronAgent(ctx, GENERIC_BEAN_BULK_LOAD_PROCESS_NAME, GENERIC_BEAN_BULK_LOAD_PROCESS_DESCRIPTION,
    			GENERIC_BEAN_BULK_LOAD_PROCESS_DESCRIPTION, "never", TaskStatusEnum.DISABLED,
    			TaskStatusEnum.DISABLED,"/opt/redknee/mnt/GenericBeanBulkLoder/",null,BulkLoadLifeCycleAgent.class, 
    			new Class[]{Context.class , String.class}, ctx , GENERIC_BEAN_BULK_LOAD_PROCESS_NAME);
    	
    }

   
    /**
     * 
     * Installs agent which performs recurring top up.
     * 
     * @param ctx lifecycle agent context
     */
    public void installRecurringTopUpCronAgent(final Context ctx)
    {
    	installLifeCycleCronAgent(ctx, RECURRING_TOP_UP_PROCESS_NAME, RECURRING_TOP_UP_PROCESS_DESCRIPTION,
    			RECURRING_TOP_UP_PROCESS_DESCRIPTION, "midnight", TaskStatusEnum.AVAILABLE,
    			TaskStatusEnum.AVAILABLE, RecurringCreditCardTopUpLifecycleAgent.class, 
    			new Class[]{Context.class , String.class}, ctx , RECURRING_TOP_UP_PROCESS_NAME);
    }
    
    /**
     * Installs the agent that provision/unprovision AuxiliaryService.
     *
     * @param ctx context where the agent runs.
     */
    public void installAuxiliaryServiceCronAgent(final Context ctx)
    {
        installLifeCycleCronAgent(ctx, PROVISION_AUXILIARYSERVICE_PROCESS_NAME, PROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION,
                PROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION, "midnight",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE,
                AuxiliaryServicesProvisioningLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, PROVISION_AUXILIARYSERVICE_PROCESS_NAME);
        
        installLifeCycleCronAgent(ctx, UNPROVISION_AUXILIARYSERVICE_PROCESS_NAME, UNPROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION,
                UNPROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION, "midnight",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE,
                AuxiliaryServicesUnprovisioningLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, UNPROVISION_AUXILIARYSERVICE_PROCESS_NAME);
    }

    public void installPointOfSaleExtractors(final Context ctx)
    {
        installLifeCycleCronAgent(ctx, POS_EXTRACT_NAME, POS_EXTRACT_DESCRIPTION,
                POS_EXTRACT_DESCRIPTION, "CRM_Daily_2AM",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE, 
                AccountPointOfSaleLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, POS_EXTRACT_NAME);

        installLifeCycleCronAgent(ctx, POS_SUBSCRIBER_EXTRACT_NAME, POS_EXTRACT_MSISDN_DESCRIPTION,
                POS_EXTRACT_MSISDN_DESCRIPTION, "CRM_Daily_2AM",
                TaskStatusEnum.AVAILABLE, TaskStatusEnum.AVAILABLE, 
                SubscriberPointOfSaleLifecycleAgent.class, 
                new Class[]{Context.class, String.class}, 
                ctx, POS_SUBSCRIBER_EXTRACT_NAME);
    }

    /**
     * Installs an agent that when run updates subscriber states properly.
     *
     * @param ctx Context where the agent runs.
     */
    public void installSubscriberStateUpdateCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_NAME);
            entry.setAgent(new SubscriberFutureActiveOrDeactiveAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding subscriber state update agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_NAME);
            task.setName(SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding subscriber state update agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
        installSubscriberExpiredToDormantAgent(ctx);
        if (!SystemSupport.supportsUnExpirablePrepaidSubscription(ctx))
        {
            installSubscriberPreExpiryAgent(ctx);

            //expiry no longer done on CRM. moved to URCS

            installSubscriberInExpiredOrAvailableOrBarredAgent(ctx);
        }
    }


    private void installSubscriberPreExpiryAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(SUBSCRIBER_PRE_EXPIRY_AGENT_NAME);
            entry.setAgent(new SubsriberPreExpiryAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding subscriber state update agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBER_PRE_EXPIRY_AGENT_NAME);
            task.setName(SUBSCRIBER_PRE_EXPIRY_AGENT_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBER_PRE_EXPIRY_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(SUBSCRIBER_PRE_EXPIRY_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding subscriber state update agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }


    private void installSubscriberExpiredToDormantAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_NAME);
            entry.setAgent(new SubscriberExpiredToDormantAgent());
            entry.setContext(ctx);
            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding subscriber Expired to Dormant state update agent!", null).log(ctx);
            }
            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_NAME);
            task.setName(SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_DESCRIPTION);
            task.setHelp(SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_DESCRIPTION);
            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding subscriber state update agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }

    private void installSubscriberInExpiredOrAvailableOrBarredAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_NAME);
            entry.setAgent(new SubscriberInAvailableOrExpiredAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding subscriber state update agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_NAME);
            task.setName(DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding subscriber state update agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }

    /**
     * Installs an agent that when run applies Recurring Credit Card Top Up for Postpaid.
     *
     * @param ctx Context where the agent runs.
     */
    public void installPostpaidRecurringCreditCardTopUpAgent(final Context ctx)
    {
    	try
        {

            //Path for Postpaid Recurring Credit Card TopUp Direct Debit Output file.
    		final String path = "/opt/redknee/mnt/dd/outbound/ADM";
    		
    		/*
    		 * BankCode for Postpaid Recurring Credit Card TopUp Direct Debit Outbound file.
    		 * The same will also be used as initials of Filenam of Direct Debit Outbound file. 
    		 */
    		final String bankCode = "ADM";
    		
            final AgentEntry entry = new AgentEntry();
            entry.setName(POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_NAME);
            entry.setAgent(new PostpaidRecurringCreditCardTopUpAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Adding Postpaid Recurring Credit Card Top Up Agent!", null).log(ctx);
            }

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_NAME);
            task.setName(POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_DESCRIPTION);
            task.setParam0(path);
            task.setParam1(bankCode);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Done adding Postpaid Recurring Credit Card Top Up Agent!", null).log(ctx);
            }
        }
        catch (final SchedulerConfigException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }
    
    /**
     * 
     * Installs agent which performs Direct Debit Outbound File Processing.
     * 
     * @param ctx lifecycle agent context
     */
    public void installDirectDebitOubboundFileProcessorCronAgent(final Context ctx)
    {
    	installLifeCycleCronAgent(ctx, DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_NAME, DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_DESCRIPTION,
    			DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_DESCRIPTION, "midnight", TaskStatusEnum.AVAILABLE,
    			TaskStatusEnum.AVAILABLE, "/opt/redknee/mnt/dd/inbound/ADM", null,  DirectDebitOutboundFileProcessorLifecycleAgent.class, 
    			new Class[]{Context.class , String.class}, ctx , DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_NAME);
    	
    }
    
    
    /**
     * 
     * Installs agent which performs Over Payment Processing.
     * 
     * @param ctx lifecycle agent context
     */
    private void installOverPaymentProcessingCronAgent(Context ctx)
    {
        
        installLifeCycleCronAgent(ctx, OVER_PAYMENT_PROCESSING_AGENT_NAME, OVER_PAYMENT_PROCESSING_AGENT_DESCRIPTION,
        		OVER_PAYMENT_PROCESSING_AGENT_DESCRIPTION, "CRM_Daily_2AM", TaskStatusEnum.AVAILABLE, 
        		TaskStatusEnum.AVAILABLE, "", null, OverPaymentProcessingLifecycleAgent.class, 
        		new Class[]{Context.class, String.class}, ctx, OVER_PAYMENT_PROCESSING_AGENT_NAME);
    }
    
    
    /**
     * Installs the agent that deactivates in_collection accounts
     *
     * @param ctx context where the agent runs.
     */
    public void installDeactivateInCollectionAccountCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(DEACTIVATE_IN_COLLECTION_ACCOUNT_NAME);
            entry.setAgent(new DeactivateInCollectionAccountCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, DEACTIVATE_IN_COLLECTION_ACCOUNT_NAME);
            task.setName(DEACTIVATE_IN_COLLECTION_ACCOUNT_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(DEACTIVATE_IN_COLLECTION_ACCOUNT_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(DEACTIVATE_IN_COLLECTION_ACCOUNT_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }

    public void installSecondaryPricePlanActivationCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(SECONDARY_PRICE_PLAN_ACTIVATION_NAME);
            entry.setAgent(new SecondaryPricePlanActivationAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SECONDARY_PRICE_PLAN_ACTIVATION_NAME);
            task.setName(SECONDARY_PRICE_PLAN_ACTIVATION_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SECONDARY_PRICE_PLAN_ACTIVATION_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(SECONDARY_PRICE_PLAN_ACTIVATION_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }

    public void installPricePlanServiceMonitoringCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(PRICE_PLAN_SERVICE_MONITORING_NAME);
            entry.setAgent(new PricePlanServiceMonitoringAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, PRICE_PLAN_SERVICE_MONITORING_NAME);
            task.setName(PRICE_PLAN_SERVICE_MONITORING_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }

    public void installLNPCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(LNP_BULK_LOAD_NAME);
            // TODO
            entry.setAgent(new com.redknee.app.crm.lnp.LnpBulkLoadFileGenerationCronAgent());
            // End TODO
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, LNP_BULK_LOAD_NAME);
            task.setName(LNP_BULK_LOAD_NAME);
            task.setCronEntry("CRM_Daily_4AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(LNP_BULK_LOAD_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(LNP_BULK_LOAD_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }





    public void installPricePlanVersionUpdateCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(PRICE_PLAN_VERSION_UPDATE_NAME);
            entry.setAgent(new PricePlanVersionUpdateAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, PRICE_PLAN_VERSION_UPDATE_NAME);
            task.setName(PRICE_PLAN_VERSION_UPDATE_NAME);
            task.setCronEntry("never");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(PRICE_PLAN_VERSION_UPDATE_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(PRICE_PLAN_VERSION_UPDATE_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
            TaskProcessingSupport.storeTaskDataInDB(ctx, task);

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }

    /**
     * Installs an agent that when run dumps CRM User information for use by Datamart.
     *
     * @param ctx Context where the agent runs.
     */
    public void installDataMartCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(USER_DUMP_NAME);
            entry.setAgent(new UserDumpAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, USER_DUMP_NAME);
            task.setName(USER_DUMP_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(USER_DUMP_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(USER_DUMP_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }


    public void installReratedCallDetailAlarmCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(RERATED_CALL_DETAIL_ALARM_NAME);
            entry.setAgent(new com.redknee.app.crm.home.calldetail.RerateCallDetailAlarmAgent());
            entry.setContext(ctx);
            AgentHelper.add(ctx, entry);
            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, RERATED_CALL_DETAIL_ALARM_NAME);
            task.setName(RERATED_CALL_DETAIL_ALARM_NAME);
            task.setCronEntry("never");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(RERATED_CALL_DETAIL_ALARM_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(RERATED_CALL_DETAIL_ALARM_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }






    /**
     * Installs the agent that provision/unprovision Bundle Auxiliary Services.
     *
     * @param ctx context where the agent runs.
     */
    public void installBundleAuxiliaryServiceCronAgent(final Context ctx)
    {
        try
        {
            final TaskEntry task = new TaskEntry();
            final AgentEntry entry = new AgentEntry();
            entry.setName(BUNDLE_AUXILIARYSERVICE_PROCESS_NAME);
            entry.setAgent(new BundleAuxiliaryServiceCronAgent(task));
            entry.setContext(ctx);
            AgentHelper.add(ctx, entry);
            AgentHelper.makeAgentEntryConfig(task, BUNDLE_AUXILIARYSERVICE_PROCESS_NAME);
            task.setName(BUNDLE_AUXILIARYSERVICE_PROCESS_NAME);
            task.setCronEntry("midnight");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(BUNDLE_AUXILIARYSERVICE_PROCESS_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(BUNDLE_AUXILIARYSERVICE_PROCESS_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException exception)
        {
            exception.printStackTrace();
            new MajorLogMsg(this, exception.getMessage(), exception).log(ctx);
        }
    }

    /**
     * This method installs the Mobile number group Monitor cron agent
     * by checking the number of available Msisdns with the minimum number of msisdns allowed for that group.
     * @param ctx
     */
    public void installMobileNumGrpMonitorCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(MOBILE_NUMBER_GROUP_MONITOR_NAME);
            entry.setAgent(new MobileNumGrpMonitorAgent());
            entry.setContext(ctx);
            AgentHelper.add(ctx, entry);
            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, MOBILE_NUMBER_GROUP_MONITOR_NAME);
            task.setName(MOBILE_NUMBER_GROUP_MONITOR_NAME);
            task.setCronEntry("every5minutes");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(MOBILE_NUMBER_GROUP_MONITOR_NAME);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(MOBILE_NUMBER_GROUP_MONITOR_NAME);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
            ex.printStackTrace();
        }
    }

    /**
     * Installs the agent that automatically releases subscribers' deposits.
     *
     * @param ctx The operating context
     */
    public void installAutoDepositReleaseCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(AUTO_DEPOSIT_RELEASE_NAME);
            entry.setAgent(new AutoDepositReleaseCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, AUTO_DEPOSIT_RELEASE_NAME);
            task.setName(AUTO_DEPOSIT_RELEASE_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(AUTO_DEPOSIT_RELEASE_DESCRIPTION);

            /*
             * [Cindy] 2007-10-16: using description as help for now.
             */
            task.setHelp(AUTO_DEPOSIT_RELEASE_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }

    /**
     * Installs the agent that processes on-time payment promotion.
     * 
     * @param ctx The operating context
     */
    public void installOnTimePaymentCronAgent(final Context ctx) 
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(ON_TIME_PAYMENT_NAME);
            entry.setAgent(new OnTimePaymentCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, ON_TIME_PAYMENT_NAME);
            task.setName(ON_TIME_PAYMENT_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(ON_TIME_PAYMENT_DESCRIPTION);
            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }
            
        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }
    
    /**
     * Installs the agent that automatically releases subscribers' deposits.
     *
     * @param ctx The operating context
     */
    public void installLateFeeEarlyRewardCronAgent(final Context ctx)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(LATE_FEE_NAME);
            entry.setAgent(new InvoicePaymentProcessingCronAgent(new LateFeeAgent()));
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, LATE_FEE_NAME);
            task.setName(LATE_FEE_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
			task.setDescription(LATE_FEE_DESCRIPTION);

            task.setHelp(LATE_FEE_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }
    
    /**
     * Installs the agent that automatically change priceplan after contract expires
     *
     * @param ctx The operating context
     */
    public void installContractEndUpdateCrontAgent(final Context ctx)
    {
        try
        {

            final AgentEntry entry = new AgentEntry();
            entry.setName(CONTRACT_END_UPDATE_NAME);
            entry.setAgent(new SubscriptionContractEndUpdateCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, CONTRACT_END_UPDATE_NAME);
            task.setName(CONTRACT_END_UPDATE_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(CONTRACT_END_UPDATE_DESCRIPTION);

            task.setHelp(CONTRACT_END_UPDATE_DESCRIPTION);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }
    /**
     * Installs the agent that automatically cleans subscription contract history
     *
     * @param ctx The operating context
     */
    public void installContractHistoryCleanupCronAgent(final Context ctx)
    {
        try
        {

            final AgentEntry entry = new AgentEntry();
            entry.setName(CONTRACT_HISTORY_CLEANUP_NAME);
            entry.setAgent(new SubscriptionContractHistoryCleanupCronAgent());
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, CONTRACT_HISTORY_CLEANUP_NAME);
            task.setName(CONTRACT_HISTORY_CLEANUP_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(CONTRACT_HISTORY_CLEANUP_NAME);

            task.setHelp(CONTRACT_HISTORY_CLEANUP_NAME);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            LogSupport.major(ctx, this, ex.getMessage(), ex);
        }
    }
    /**
     * Installs the agent that automatically cleans subscription contract history
     *
     * @param ctx The operating context
     */
    public void installScheduledSubscriberModificationExecutionCronAgent(final Context ctx)
    {
        try
        {

            final AgentEntry entry = new AgentEntry();
            entry.setName(SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);
            entry.setAgent(new ScheduledSubscriberModificationExecutorCronAgent(ctx));
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);
            task.setName(SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);
            task.setCronEntry("CRM_Daily_2AM");
            task.setDefaultStatus(TaskStatusEnum.AVAILABLE);
            task.setStatus(TaskStatusEnum.AVAILABLE);
            task.setDescription(SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);

            task.setHelp(SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final SchedulerConfigException ex)
        {
            LogSupport.major(ctx, this, ex.getMessage(), ex);
        }
    }
    
    
    private void installRechargeServicesUponPaymentsServicesAgent(Context ctx)
    {
        installLifeCycleCronAgent(ctx, RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_NAME, RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_DESCRIPTION,
                RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_DESCRIPTION, "never", TaskStatusEnum.AVAILABLE, 
                TaskStatusEnum.AVAILABLE, RechargeSubscriberServicesOnPaymentsLifeCycleAgent.class,
                new Class[]{Context.class, String.class}, 
                ctx, RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_NAME);
    }
    
    
    /**
     * Given the name of an agent entry makes an agent entry config class. This version will also set the agent type to
     * be Agent Entry and will set the config in the task entry.
     * 
     * @param task
     *            the TaskEntry structure associated with the task
     * @param agent
     *            the name of the agent entry
     * @return the config object
     */
    private static XCronLifecycleAgentControlConfig makeLifecycleAgentEntryConfig(TaskEntry task, String agent)
    {
        XCronLifecycleAgentControlConfig config = new XCronLifecycleAgentControlConfig();
        config.setAgent(agent);

        if (task != null)
        {
            task.setAgentType("Lifecycle Agent Control");
            task.setAgentConfig(config);
        }

        return config;
    }

    private void installCronAgent(final Context ctx, final String name, final String description,
            final String help, final String cronEntry,
            final TaskStatusEnum defaultStatus, final TaskStatusEnum status, 
            final Class<? extends ContextAgent> agentClass)
    {
        installCronAgent(ctx, name, description, help, cronEntry, defaultStatus, status, agentClass, new Class[]{});
    }

    private void installCronAgent(final Context ctx, final String name, final String description,
            final String help, final String cronEntry,
            final TaskStatusEnum defaultStatus, final TaskStatusEnum status, 
            final Class<? extends ContextAgent> agentClass, Class[] constructorParams, Object... params)
    {
        try
        {

            final AgentEntry entry = new AgentEntry();
            entry.setName(name);
            entry.setAgent(agentClass.getConstructor(constructorParams).newInstance(params));
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            AgentHelper.makeAgentEntryConfig(task, name);
            
            task.setName(name);
            task.setDescription(description);
            task.setHelp(help);

            task.setCronEntry(cronEntry);
            task.setDefaultStatus(defaultStatus);
            task.setStatus(status);

            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
                TaskHelper.add(ctx, task);
            }

        }
        catch (final Exception ex)
        {
            new MajorLogMsg(this, ex.getMessage(), ex).log(ctx);
        }
    }
    
    /**
     * Installs the agent that does the dunning.
     * This task is never run by cron directly, only as a cascaded task.
     *
     * @param ctx context where the agent runs.
     */
    private void installLifeCycleCronAgent(final Context ctx, final String name, final String description,
            final String help, final String cronEntry,
            final TaskStatusEnum defaultStatus, final TaskStatusEnum status,
            final Class<? extends RunnableLifecycleAgentSupport> lifecycleAgentClass, Class[] constructorParams, Object... params)
    {
    	installLifeCycleCronAgent(ctx, name, description, help, cronEntry, defaultStatus, status,null, null, lifecycleAgentClass, constructorParams, params);
    }

    /**
     * Installs the agent that does the dunning.
     * This task is never run by cron directly, only as a cascaded task.
     *
     * @param ctx context where the agent runs.
     */
    private void installLifeCycleCronAgent(final Context ctx, final String name, final String description,
            final String help, final String cronEntry,
            final TaskStatusEnum defaultStatus, final TaskStatusEnum status,String param0, String param1,
            final Class<? extends RunnableLifecycleAgentSupport> lifecycleAgentClass, Class[] constructorParams, Object... params)
    {
        try
        {
            final AgentEntry entry = new AgentEntry();
            entry.setName(name);
            entry.setAgent(lifecycleAgentClass.getConstructor(constructorParams).newInstance(params));
            entry.setContext(ctx);

            AgentHelper.add(ctx, entry);

            final TaskEntry task = new TaskEntry();
            makeLifecycleAgentEntryConfig(task, name);
            
            task.setName(name);
            task.setDescription(description);
            task.setHelp(help);

            task.setCronEntry(cronEntry);
            task.setDefaultStatus(defaultStatus);
            task.setStatus(status);


            if (TaskHelper.retrieve(ctx, task.getName()) == null)
            {
            	if(param0 != null)
            	{
            		task.setParam0(param0);
            	}
            	if(param1 != null)
            	{
            		task.setParam1(param1);
            	}
                TaskHelper.add(ctx, task);
            }
            TaskProcessingSupport.storeTaskDataInDB(ctx, task);
            entry.setTask(task);
        }
        catch (final Exception exception)
        {
            new MajorLogMsg(this, exception.getMessage(), exception).log(ctx);
        }
    }
    
    private void installSQLScriptCronAgent(final Context ctx, final String name, final String description,
            final String help, final String cronEntry,
            final TaskStatusEnum defaultStatus, final TaskStatusEnum status,
            String sqlScript, final String defaultParam0)
    {
        try
        {
            final TaskEntry task = new TaskEntry();
            String param0 = defaultParam0;
            String param1 = null;

            TaskEntry oldTask = TaskHelper.retrieve(ctx, name);
            XMenuSqlShellConfig config = new XMenuSqlShellConfig();

            if (oldTask!=null && oldTask.getParam0()!=null && !oldTask.getParam0().trim().isEmpty())
            {
                try
                {
                    param0 = String.valueOf(Math.abs(Integer.parseInt(oldTask.getParam0())));
                }
                catch (Throwable t)
                {
                    // Ignored. Invalid config;
                }
                
                try
                {
                    param1 = String.valueOf(Math.abs(Integer.parseInt(oldTask.getParam1())));
                }
                catch (Throwable t)
                {
                    // Ignored. Invalid config;
                }

                oldTask.setAgentType("Sql script");
                oldTask.setAgentConfig(config);
                oldTask.setAgent(config);
            }
            else
            {
                task.setAgentConfig(config);
                task.setAgentType("Sql script");
                task.setAgent(config);

                task.setName(name);
                task.setDescription(description);
                task.setHelp(help);
                task.setParam0(param0);
                task.setParam1(param1);

                task.setCronEntry(cronEntry);
                task.setDefaultStatus(defaultStatus);
                task.setStatus(status);

            }
            
            if (param1!=null && param1.trim().length()>0)
            {
                sqlScript = param1;
            }
            
            config.setScript(sqlScript.replaceAll("%PARAM0%", param0));

            if (oldTask == null)
            {
                TaskHelper.add(ctx, task);
            }
            else
            {
                TaskHelper.store(ctx, oldTask);
            }
        }
        catch (final Exception exception)
        {
            new MajorLogMsg(this, exception.getMessage(), exception).log(ctx);
        }
    }
    public void installGenerateAutomaticRefundSchedulerAgent(final Context ctx)
	{
		installLifeCycleCronAgent(ctx, GENERATE_AUTOMATIC_REFUNDS, GENERATE_AUTOMATIC_REFUNDS_DESCRIPTION,
				GENERATE_AUTOMATIC_REFUNDS_DESCRIPTION, "everyMonth", TaskStatusEnum.AVAILABLE,
				TaskStatusEnum.AVAILABLE, "", null,GenerateAutomaticRefundAgent.class,
				new Class[]{Context.class , String.class}, ctx , GENERATE_AUTOMATIC_REFUNDS);
	}
    
    public static String CRON_INSTALL_TASK_ENTRY_HOME = "CronInstallTaskEntryHome";
}
