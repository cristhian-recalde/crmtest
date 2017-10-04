package com.trilogy.app.crm.bas.recharge;

import java.sql.SQLException;
import java.util.Map;


import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.context.PMContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.pipe.ThreadPool;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class ProcessSubscriberThreadPoolVisitor extends ContextAwareSupport implements Visitor, RechargeConstants
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    

    /**
     * Create a new instance of <code>ProcessAccountThreadPoolVisitor</code>.
     *
     * @param ctx
     *            The operating context.
     * @param threads
     *            Number of threads.
     * @param queueSize
     *            Queue size.
     * @param delegate
     *            Delegate of this visitor.
     */
    public ProcessSubscriberThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
        final ContextAgent delegate)
    {
        this(ctx, threads, queueSize, delegate, null);
    }
    
    public ProcessSubscriberThreadPoolVisitor(final Context ctx, final int threads, final int queueSize,
            final ContextAgent delegate, final LifecycleAgentSupport agent)
        {
            setContext(ctx);
            threadPool_ = new ThreadPool(POOL_NAME, queueSize, threads, new PMContextAgent(POOL_NAME, ProcessSubscriberThreadPoolVisitor.class.getSimpleName() , delegate));
            agent_ = agent;
        }
    /**
     * Creates recurring charge for each account.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The account to generate recurring charge for.
     */
    public void visit(final Context ctx, final Object obj)
    {
        final Context subContext = ctx.createSubContext();
        String subscriberId;
        
        if (agent_ != null && !LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining subscriptions will be processed next time.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }

        subscriberId = (String) obj;
		try
		{
		    Subscriber subscriber = SubscriberSupport.getSubscriber(subContext, subscriberId);
		    if (subscriber == null)
		    {
		        throw new HomeException("Developer error: Subscriber was not supposed to be null during recurring recharge");
		    }
		    
		    subContext.put(Subscriber.class, subscriber);
		    subContext.put(Account.class, subscriber.getAccount(ctx));
		    subContext.put(CRMSpid.class, SpidSupport.getCRMSpid(ctx, subscriber.getSpid()));
		    Map<Integer,Double> rates = (Map<Integer,Double>) ctx.get(RecurringRechargeSupport.PRORATED_RATE_MAP);
		    if (rates != null)
		    {
		        Double rate = rates.get(Integer.valueOf(subscriber.getSpid()));
		        subContext.put(RecurringRechargeSupport.PRORATED_RATE, Double.valueOf(rate));
		    }
		    
		    Integer billingCycleDay = (Integer) ctx.get(RecurringRechargeSupport.RECURRING_RECHARGE_PRE_WARNING_NOTIFICATION_BILLING_CYCLE_DAY);
		    if (billingCycleDay!=null)
		    {
		        int subscriberBillingCycleDay = SubscriberSupport.getBillCycleDay(ctx, subscriber);
		        if (subscriberBillingCycleDay != billingCycleDay)
		        {
		            return;
		        }
		    }

		    threadPool_.execute(subContext);
		}
		catch (final AgentException e)
		{
		    new MajorLogMsg(this, "Cannot recurring charge for subscriber: " + subscriberId + "," + e.getMessage(), e)
		        .log(getContext());
		    
		}
		catch (final Throwable t)
		{
		    new MajorLogMsg(this, "Unexpected error for recurring charge of subscriber: " + subscriberId + ","
		        + t.getMessage(), t).log(getContext());
		}
        
    }


    /**
     * Returns the thread pool.
     *
     * @return Thread pool.
     */
    protected ThreadPool getPool()
    {
        return threadPool_;
    }

    /**
     * Thread pool.
     */
    private final ThreadPool threadPool_;
    private final LifecycleAgentSupport agent_;
    private final static String POOL_NAME = "Recurring Charge (Subscriber)";
}
