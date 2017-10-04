package com.trilogy.app.crm.dunning;

import java.util.Collection;
import java.util.Date;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.DunningProcessingAccountXStatementVisitor;
import com.trilogy.app.crm.dunning.visitor.reportgeneration.DunningReportRecordGenerationAccountVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

public class DunningProcessImpl implements DunningProcess
{
	public DunningProcessImpl(final Context ctx)
    {
        setContext(ctx);
    }
	
	@Override
	public void generateReport(Date date, LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
        generateReport(getContext(), date, lifecycleAgent);
        }
	
	@Override
	public void generateReport(Context ctx, Date date,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		//TODO Report Gui logic should use this to fetch and display data from  DunningReport Table
	}

	@Override
	public void processReport(Date date, LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSpidReport(Date date, int spid,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processSpidReport(Date date, int spid,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAccount(Date date, String accountIdentifier)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAccount(Date date, Account account)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccounts(Date date) throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccounts(Date date,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccountsWithServiceProviderID(Date date, int spId)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccountsWithBillCycleID(Date date, int billCycleId)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInDunningProcess() throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logEventRecord(Subscriber subscriber, AccountStateEnum state,
			int result, Account account) throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSubscriberDunningResultOm(AccountStateEnum state,
			Subscriber subscriber, boolean overallSuccess, Object caller)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void omLoggerAttempt(AccountStateEnum state, Object caller)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void processReport(Context ctx, Date date,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSpidReport(Context ctx, Date date, int spid,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processSpidReport(Context ctx, Date date, int spid,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAccount(Context ctx, Date date, String accountIdentifier)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAccount(Context ctx, Date date, Account account)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccounts(Context ctx, Date date)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccounts(Context ctx, Date date,
			LifecycleAgentSupport lifecycleAgent)
			throws DunningProcessException, DunningProcessInternalException {

        DunningProcessingAccountXStatementVisitor visitor = new DunningProcessingAccountXStatementVisitor(date, lifecycleAgent);
        visitor.setJoinAgedDebt(true);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Start ");
            sb.append(visitor.getProcessName());
            sb.append(" for all accounts");
            LogSupport.info(ctx, this, sb.toString());
        }
        
        
        try
        {
            visitor.visit(ctx, AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNING_DEFAULT_KEY));
        }
        catch (AgentException e)
        {
            throw new DunningProcessException(e);
        }
        catch (IllegalStateException e)
        {
            throw new DunningProcessException(e);
        }
        catch (AbortVisitException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append(visitor.getProcessName());
            cause.append(" was interrupted");
            
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            throw new DunningProcessException(cause.toString(), e);
        }

        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append(visitor.getProcessName());
            sb.append(" finished for all accounts");
            LogSupport.info(ctx, this, sb.toString());
        }
    
		
	}

	@Override
	public void processAllAccountsWithServiceProviderID(Context ctx, Date date,
			int spId) throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAllAccountsWithBillCycleID(Context ctx, Date date,
			int billCycleId) throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInDunningProcess(Context ctx)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logEventRecord(Context ctx, Subscriber subscriber,
			AccountStateEnum state, int result, Account account)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSubscriberDunningResultOm(Context ctx,
			AccountStateEnum state, Subscriber subscriber,
			boolean overallSuccess, Object caller)
			throws DunningProcessException, DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void omLoggerAttempt(Context ctx, AccountStateEnum state,
			Object caller) throws DunningProcessException,
			DunningProcessInternalException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContext(Context arg0) {
		// TODO Auto-generated method stub
		
	}

}
