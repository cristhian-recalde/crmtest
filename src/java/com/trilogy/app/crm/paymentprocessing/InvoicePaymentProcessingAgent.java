package com.trilogy.app.crm.paymentprocessing;

import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.core.cron.agent.CronContextAgentException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleXInfo;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.report.ReportUtilities;

public abstract class InvoicePaymentProcessingAgent extends ContextAwareSupport
{
	public static final String BILLING_DATE =
	    "InvoicePaymentProcessingAgent.BILLING_DATE";
	public static final String ACCOUNT =
	    "InvoicePaymentProcessingAgent.ACCOUNT";
	
	

	/**
	 * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
	 */
	public void execute(final Context ctx) throws AgentException
	{
		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this,
			    "Invoice payment processing agent initiated.", null).log(ctx);
		}

		final Date billingDate = getCurrentDate(ctx);
		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "Billing Date: " + billingDate);
		}

		Context sCtx = ctx.createSubContext();
		String sessionKey = CalculationServiceSupport.createNewSession(sCtx);

		try
		{
			And predicate = new And();

			LateFeeEarlyRewardAccountProcessor processor =
			    getAccountProcessor(sCtx);
			
			if(processor.getName().equals("GenerateLateFeeAccountProcessor")){
				
				ctx.put(InvoiceCalculationSupport.LATE_FEE_TASK, true);
			}

			Visitor visitor = null;
			Home home = null;

			Account account =
			    (Account) sCtx.get(InvoicePaymentProcessingAgent.ACCOUNT);
			/*
			 * [Cindy Wong] 2010-12-19: web agent processing a single account.
			 */
			if (account != null)
			{
				visitor =
				    new InvoicePaymentProcessorAccountVisitor(processor,
				        billingDate);
				home = (Home) sCtx.get(AccountHome.class);
				predicate.add(new EQ(AccountXInfo.BAN, account.getBAN()));
			}
			else
			{
				visitor =
			    new InvoicePaymentProcessingBillCycleVisitor(processor,
			        billingDate);
				home = (Home) sCtx.get(BillCycleHome.class);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(billingDate);
				predicate.add(new EQ(BillCycleXInfo.DAY_OF_MONTH, calendar
				    .get(Calendar.DAY_OF_MONTH)));
			}
			home.forEach(sCtx, visitor, predicate);
		}
		catch (final Exception exception)
		{
			ReportUtilities
			    .logMajor(
			        ctx,
			        getClass().getName(),
			        "Invoice payment processing cron task encounterred exception processing billing date {0}",
			        new String[]
			        {
				        billingDate.toString()
			        }, exception);

			throw new CronContextAgentException(
			    "Invoice payment processing cron task encountered exception.",
			    exception);
		}
		finally
		{
			ctx.put(InvoiceCalculationSupport.LATE_FEE_TASK, false);
			CalculationServiceSupport.endSession(sCtx, sessionKey);
		}

		LogSupport.debug(ctx, this,
		    "Invoice payment processing cron task complete.");
	}

	/**
	 * Gets the "current date" for the Invoice Generation. Returns the DAY
	 * BEFORE, either: - Param1 in the CRON task
	 * Configuration, or if Param1 is null - today
	 * 
	 * @param context
	 *            The operating context.
	 * @return The "current date" for the invoice run.
	 */
	private Date getCurrentDate(final Context context) throws AgentException
	{
		return (Date) context.get(BILLING_DATE, new Date());
	}

	/**
	 * Returns whether the license is enabled.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @return Whether the license is enabled.
	 */
	protected abstract boolean isLicenseEnabled(Context ctx);

	/**
	 * Returns the correct account processor.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param billingDate
	 *            Date to operate on. All bill cycles on this day are processed.
	 * @return The correct account processor.
	 */
	protected abstract LateFeeEarlyRewardAccountProcessor getAccountProcessor(
	    Context ctx);

}
