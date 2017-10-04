package com.trilogy.app.crm.transaction.task;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountOverPaymentHistory;
import com.trilogy.app.crm.bean.AccountOverPaymentHistoryHome;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.BillCycleHistory;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.InvoiceHome;
import com.trilogy.app.crm.bean.InvoiceXInfo;
import com.trilogy.app.crm.bean.OverPaymentRun;
import com.trilogy.app.crm.bean.SpidDirectDebitConfigXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.support.AccountSupport;
import com.trilogy.app.crm.support.AccountOverPaymentHistorySupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.lifecycle.LifecycleStateEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.util.snippet.log.Logger;

public class OverPaymentRunTransactionProcessingVisitor implements Visitor {

    protected final OverPaymentProcessingLifecycleAgent agent_;
    
    OverPaymentRunTransactionProcessingVisitor(OverPaymentProcessingLifecycleAgent agent)
    {
        agent_ = agent;
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
        if (obj instanceof OverPaymentRun)       {
            OverPaymentRun oprun = (OverPaymentRun) obj;
            agent_.addRecord(oprun);
        }
	}

}
