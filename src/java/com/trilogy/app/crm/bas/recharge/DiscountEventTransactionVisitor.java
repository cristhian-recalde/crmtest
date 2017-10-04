package com.trilogy.app.crm.bas.recharge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.SystemFeatureThreadpoolConfig;
import com.trilogy.app.crm.discount.DiscountEventSqlGenerator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.core.cron.ExitStatusEnum;
import com.trilogy.framework.core.cron.TaskEntry;
import com.trilogy.framework.core.cron.TaskHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.service.task.executor.support.TaskProcessingSupport;
import com.trilogy.service.task.executor.bean.CrmTaskStatusEnum;
import com.trilogy.service.task.executor.bean.ScheduleTask;

/**
 * @author Abhishek Sathe
 * @since 10.5
 * 
 *        Visitor responsible to identify entries from DiscountEvent and check
 *        whether records are eligible for Discounting or not.
 */
public class DiscountEventTransactionVisitor extends
AbstractDiscountEventTransaction {

	private static final long serialVersionUID = 1L;

	public DiscountEventTransactionVisitor(
			final LifecycleAgentSupport lifecycleAgent) {
		super();
		lifecycleAgent_ = lifecycleAgent;
	}

	/**
	 * Return the DiscountEventTransactionVisitor.
	 */
	public DiscountEventTransactionVisitor getVisitor() {
		return visitor_;
	}

	@Override
	public void visit(Context ctx, Object obj) throws AgentException,
	AbortVisitException {
		PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(),
				"DiscountEventTransaction");
	
		ScheduleTask taskDetails = new ScheduleTask();

		final SystemFeatureThreadpoolConfig config = (SystemFeatureThreadpoolConfig) ctx
				.get(SystemFeatureThreadpoolConfig.class);
		int threadCount = 5;
		int queueSize = 5;
		if (config != null) {
			threadCount = config.getDiscountEventThreadCount();
			queueSize = config.getDiscountEventQueueSize();
		}
		DiscountEventTransactionThreadPoolVisitor threadPoolVisitor = new DiscountEventTransactionThreadPoolVisitor(
				ctx, threadCount, queueSize, this, lifecycleAgent_);
		try {

			LogSupport.debug(ctx, this,
					"inside DiscountEventTransactionVisitor visit method");
			String banParameter = (String) ctx
					.get(AccountConstants.BAN_FOR_DISCOUNT);
			ArrayList<Object> banList = new ArrayList<Object>();
			
			if (banParameter != null)
				banList.add(banParameter);
			else
				banList = getAccountFromDiscount(ctx);
			LogSupport.info(ctx, this,
					"List of Ban to be processed for the Discount event transactions [" + banList  + " ]");

			int totalAcctSize = banList.size();
			int initSize = 0;
			int size = getConfiguredFetchSize(ctx);

			if (totalAcctSize < getConfiguredFetchSize(ctx)) {
				size = totalAcctSize;
			}
			while (size <= totalAcctSize) {
				if (totalAcctSize - size < getConfiguredFetchSize(ctx)) {
					size = totalAcctSize;
				}
				List<Object> acctChunkList = banList.subList(initSize, size);
				initSize = size;
				size = size + getConfiguredFetchSize(ctx);

				try {
					for (Object singleBan : acctChunkList) {
						LogSupport.info(ctx, this,
								"Discount event transactions are generated for ="
										+ singleBan);
						threadPoolVisitor.visit(ctx, singleBan);
					}
				} catch (final Exception e) {
					LogSupport.minor(ctx, this,
							"Error getting while process account. ", e);
				}
			}
			
			taskDetails.setTaskRunId(TaskProcessingSupport.getScheduledTaskRunId());
		    taskDetails.setNoOfRecordsProcessed(totalAcctSize - 1); 

		} catch (final HomeException e) {
			String cause = "Unable to retrieve accounts";
			StringBuilder sb = new StringBuilder();
			sb.append(cause);
			sb.append(": ");
			sb.append(e.getMessage());
			LogSupport.major(ctx, this, sb.toString(), e);
			throw new IllegalStateException("Discount Event Transaction Process"
					+ " failed: " + cause, e);
		} catch (final Throwable e) {
			String cause = "General error";
			StringBuilder sb = new StringBuilder();
			sb.append(cause);
			sb.append(": ");
			sb.append(e.getMessage());
			LogSupport.major(ctx, this, sb.toString(), e);
			throw new IllegalStateException("Discount Event Transaction Process"
					+ " failed: " + cause, e);
		} finally {
			try {
				threadPoolVisitor.getPool().shutdown();
				threadPoolVisitor.getPool().awaitTerminationAfterShutdown(
						TIME_OUT_FOR_SHUTTING_DOWN);
				
				
				taskDetails.setCompletionTime(new Date());
				taskDetails.setNoOfFailed(threadPoolVisitor.getFailedBANs().size());
				taskDetails.setNoOfSuccessful(taskDetails.getNoOfRecordsProcessed() - threadPoolVisitor.getFailedBANs().size());
				
				TaskEntry taskEntry = TaskHelper.retrieve(ctx, getProcessName());
				if (taskEntry != null) {
					taskDetails.setTaskName(taskEntry.getDescription());
					taskDetails.setTaskParam1(taskEntry.getParam0());
					taskDetails.setTaskParam2(taskEntry.getParam1());
					if (taskEntry.getExitStatus().equals(ExitStatusEnum.FAILED)) {
						taskDetails.setStatus(CrmTaskStatusEnum.FAILED);
					} else {
						taskDetails.setStatus(CrmTaskStatusEnum.COMPLETED);
					}
				}
				
				LogSupport.info(ctx, this, "Logging History for : " + getProcessName());
				TaskProcessingSupport.logHistory(ctx, taskDetails);
				
			} catch (final Exception e) {
				LogSupport
				.minor(ctx,
						this,
						"Exception catched during wait for completion of all discounteventtransaction threads",
						e);
			}
		}
		pm.log(ctx);
	}

	private ArrayList<Object> getAccountFromDiscount(Context ctx) throws HomeException {
		String mainSql = DiscountEventSqlGenerator.getDiscountingSqlGenerator().getDISCOUNTING_ACCOUNT_FOR_TRANSACTION_FILTER();
		Collection<Object> list = AccountSupport.getQueryDataList(ctx, mainSql);
		return null != list ? new ArrayList<Object>(list) : null;
	}

	/**
	 * Get discounting fetch size from configuration
	 * 
	 * @param ctx
	 * @return
	 */
	private int getConfiguredFetchSize(Context ctx) {
		GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
		return gc.getDiscountingProcessFetchSize();
	}

	protected LifecycleAgentSupport getLifecycleAgent() {
		return lifecycleAgent_;
	}
	
	private LifecycleAgentSupport lifecycleAgent_;
	private DiscountEventTransactionVisitor visitor_;
	public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
}
