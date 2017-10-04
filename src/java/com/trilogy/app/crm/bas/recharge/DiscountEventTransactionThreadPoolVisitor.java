package com.trilogy.app.crm.bas.recharge;

import java.sql.SQLException;
import java.util.List;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

/**
 * This visitor will use a threadpool to start the processing accounts for
 * Discount event transactions
 */

public class DiscountEventTransactionThreadPoolVisitor extends
ContextAwareSupport implements Visitor {
	private static final long serialVersionUID = 1L;

	private ThreadPool threadPool_;

	private DiscountEventContextAgent visitor_;

	/**
	 * Create a new DiscountEventTransactionThreadPoolVisitor visitor.
	 * 
	 * @param ctx
	 * @param threads
	 * @param queueSize
	 * @param discountClassAssignmentVisitor
	 * @param agent
	 */
	public DiscountEventTransactionThreadPoolVisitor(
			final Context ctx,
			final int threads,
			final int queueSize,
			final DiscountEventTransactionVisitor discountEventTransactionVisitor,
			final LifecycleAgentSupport agent) {
		setContext(ctx);
		visitor_ = new DiscountEventContextAgent(
				discountEventTransactionVisitor);
		threadPool_ = new ThreadPool(MODULE, queueSize, threads,
				new PMContextAgent(MODULE,
						DiscountEventTransactionVisitor.class.getSimpleName(),
						visitor_));
		agent_ = agent;
	}

	public void visit(Context ctx, Object obj) throws AgentException,
	AbortVisitException {
		PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(),
				"Generating discount event transaction.......");
		if (agent_ != null
				&& !LifecycleStateEnum.RUNNING.equals(agent_.getState())) {
			String msg = "Lifecycle agent "
					+ agent_.getAgentId()
					+ " no longer running.  Remaining accounts will be processed next time.";
			new InfoLogMsg(this, msg, null).log(ctx);
			throw new AbortVisitException(msg);
		}

		Context subContext = ctx.createSubContext();

		try {
			subContext
			.put(DiscountEventContextAgent.DISCOUNT_TRANSACTION_CHECK_ACCOUNT,
					obj);
			threadPool_.execute(subContext);
		} catch (AgentException e) {
			new MinorLogMsg(this,
					"Error while running Discount Event Transaction process for account '"
							+ obj + "': " + e.getMessage(), e)
			.log(getContext());
		} catch (Throwable t) {
			new MinorLogMsg(
					this,
					"Unexpected error while running Discount Event Transaction process for account '"
							+ obj + "': " + t.getMessage(), t)
			.log(getContext());
		}

		finally {
			pm.log(ctx);
		}
	}

	public ThreadPool getPool() {
		return threadPool_;
	}
	
	public List<String> getFailedBANs() {
        return visitor_.getFailedAssignedBANs();
    }

	private final LifecycleAgentSupport agent_;

	public static final String MODULE = "Discount Event Transaction";

}
