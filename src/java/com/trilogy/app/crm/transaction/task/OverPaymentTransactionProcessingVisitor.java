package com.trilogy.app.crm.transaction.task;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import com.trilogy.app.crm.bean.AccountOverPaymentHistory;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class OverPaymentTransactionProcessingVisitor implements Visitor {

    protected final OverPaymentProcessingLifecycleAgent agent_;
    
    OverPaymentTransactionProcessingVisitor(OverPaymentProcessingLifecycleAgent agent)
    {
        agent_ = agent;
        initializeTransactionCounters();
    }
	
	@Override
	public void visit(Context parentCtx, Object obj) throws AgentException,
			AbortVisitException {
        Context ctx = parentCtx.createSubContext();
        
        if (!LifecycleStateEnum.RUNNING.equals(agent_.getState()))
        {
            String msg = "Lifecycle agent " + agent_.getAgentId() + " no longer running.  Remaining bill cycle changes will be processed next time it is run.";
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new AbortVisitException(msg);
        }
        if (obj instanceof AccountOverPaymentHistory)
        {
        	AccountOverPaymentHistory hist = (AccountOverPaymentHistory) obj;
        	long overpaymentBalance = hist.getNewOverpaymentBalance();

        		try {
        			MessageMgr mmgr = new MessageMgr(ctx,"OverPaymentRun");
        			HTMLExceptionListener exceptionListener = new HTMLExceptionListener(mmgr);
        			ctx.put(HTMLExceptionListener.class, exceptionListener);
        			Transaction transaction =  TransactionSupport.createAccountPaymentTransaction(ctx, hist.getSpid(), overpaymentBalance, overpaymentBalance,
							AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.OverPaymentCredit), true, false, "SYSTEM", new Date(), new Date(), "Over Payment Transaction", 0,hist.getBan());
        			if(exceptionListener.hasErrors())
        			{
        				noOfFailedTransactions_.incrementAndGet();
        			}else{
        				noOfSuccessfulTransactions_.incrementAndGet();
        			}
        		} catch (HomeException e) {
					
        			noOfFailedTransactions_.incrementAndGet();
        			new MajorLogMsg(this, "Error occurred in visitor while processing Over Payment", e).log(ctx);
				}
        }
	}
	
	private void initializeTransactionCounters()
	{
		noOfSuccessfulTransactions_ = new AtomicLong(0);
		noOfFailedTransactions_ = new AtomicLong(0);
	}
	
	public void resetTransactionCounters()
	{
		noOfSuccessfulTransactions_.set(0);
		noOfFailedTransactions_.set(0);
	}
	
	public long getNoOfSuccessfulTransactions()
	{
		return noOfSuccessfulTransactions_.get();
	}
	
	public long getNoOfFailedTransactions()
	{
		return noOfFailedTransactions_.get();
	}
	
	private AtomicLong noOfSuccessfulTransactions_ = null;
	private AtomicLong noOfFailedTransactions_ = null;

}
