package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.lifecycle.LifecycleException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.app.crm.CoreCrmLicenseConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.lifecycle.LifecycleAgentScheduledTask;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * 
 * This lifecycle agent will perform the discount transactions for discount
 * events.
 * 
 * @author Abhishek Sathe
 *
 */
public class DiscountEventLifecycleAgent extends LifecycleAgentScheduledTask {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a DiscountEventLifecycleAgent object.
	 * 
	 * @param ctx
	 * @param agentId
	 * @throws AgentException
	 */
	public DiscountEventLifecycleAgent(Context ctx, String agentId)
			throws AgentException {
		super(ctx, agentId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled(Context ctx) {
		return (LicensingSupportHelper.get(ctx).isLicensed(ctx,
				CoreCrmLicenseConstants.POSTPAID_LICENSE_KEY) || LicensingSupportHelper
				.get(ctx).isLicensed(ctx,
						CoreCrmLicenseConstants.HYBRID_LICENSE_KEY))
						&& DunningReport.isDunningReportSupportEnabled(ctx);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void start(Context ctx) throws LifecycleException {
		try {
			DiscountEventTransactionVisitor visitor = new DiscountEventTransactionVisitor(
					this);
			LogSupport.debug(ctx,
					"In Scheduler of Generate Discount Event Transaction",
					"Starting Discount Transaction Generation Process");
			String banParameter = getParameter2(ctx, String.class);
			ctx.put(AccountConstants.BAN_FOR_DISCOUNT, banParameter);
			visitor.visit(ctx, null);

		} catch (AbortVisitException e) {
			// TODO Auto-generated catch block
			final String message = e.getMessage();
			LogSupport.minor(ctx, getClass().getName(), message, e);
		} catch (AgentException e) {
			final String message = e.getMessage();
			LogSupport.minor(ctx, getClass().getName(), message, e);
		}

	}

}
