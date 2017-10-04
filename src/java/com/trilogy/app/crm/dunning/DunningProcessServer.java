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
package com.trilogy.app.crm.dunning;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.dunning.support.ActiveOMLogSupport;
import com.trilogy.app.crm.dunning.support.DunnedOMLogSupport;
import com.trilogy.app.crm.dunning.support.InArrearsOMLogSupport;
import com.trilogy.app.crm.dunning.support.NullStateOMLogSupport;
import com.trilogy.app.crm.dunning.support.OMLogSupport;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.DunningProcessingAccountVisitor;
import com.trilogy.app.crm.dunning.visitor.accountprocessing.DunningProcessingAccountXStatementVisitor;
import com.trilogy.app.crm.dunning.visitor.reportgeneration.DunningReportGenerationSpidVisitor;
import com.trilogy.app.crm.dunning.visitor.reportgeneration.DunningReportRecordGenerationAccountVisitor;
import com.trilogy.app.crm.dunning.visitor.reportprocessing.DunningReportProcessingSpidVisitor;
import com.trilogy.app.crm.dunning.visitor.reportprocessing.DunningReportRecordProcessingVisitor;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.log.DunningActionER;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CalendarSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.ERLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.util.snippet.log.Logger;

/**
 * Provides the dunning process for accounts.
 *
 * @author gary.anderson@redknee.com
 */
public class DunningProcessServer implements DunningProcess
{

    public DunningProcessServer(final Context ctx)
    {
        setContext(ctx);
    }

    /**
     * Processes the given account to update its dunned state.
     *
     * @param ctx The operating context.
     * @param date The current data used for processing.
     * @param accountIdentifier The identifier of the account to process.
     * @throws DunningProcessException Thrown if a problem is encountered during processing.
     */
    public void processAccount(final Context ctx, final Date date, final String accountIdentifier)
        throws DunningProcessException
    {
        setContext(ctx);
        final Account account;
        try
        {
            account = AccountSupport.getAccount(ctx, accountIdentifier);
            if (account == null)
            {
                String cause = DunningProcessingAccountVisitor.getVisitorProcessName() + " failed: Unable to find account";
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" '");
                sb.append(accountIdentifier);
                sb.append("'");

                LogSupport.major(ctx, this, sb.toString());

                throw new DunningProcessException(cause);
            }
            CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, account.getSpid());
            Context subContext = ctx.createSubContext();
            subContext.put(CRMSpid.class, crmSpid);
        }
        catch (final HomeException exception)
        {
            String cause = DunningProcessingAccountVisitor.getVisitorProcessName() + " failed: Unable to look for account";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" '");
            sb.append(accountIdentifier);
            sb.append("' -> ");
            sb.append(exception.getMessage());

            LogSupport.major(ctx, this, sb.toString(), exception);

            throw new DunningProcessException(exception);
     
        }

        

        processAccount(ctx, date, account);
    }

    /**
     * CI implementation.
     */
    public void processAccount(final Date date, final String accountIdentifier)
        throws DunningProcessException
    {
        processAccount(getContext(), date, accountIdentifier);
    }

    /**
     * Processes the given account to update its dunned state.
     *
     * @param ctx The operating context.
     * @param date The current data used for cprocessing.
     * @param account The account to process.
     * @throws DunningProcessException Thrown if a problem is encountered during processing.
     */
    public void processAccount(final Context ctx, final Date date, final Account account)
        throws DunningProcessException
    {
        //setContext(ctx);
        DunningProcessingAccountVisitor visitor = new DunningProcessingAccountVisitor(date);
        
        if ( account == null || account.getSystemType().equals(SubscriberTypeEnum.PREPAID))
        {
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(visitor.getProcessName());
                sb.append(" could not be executed: Account '");
                sb.append(account.getBAN());
                sb.append("' not found or prepaid.");
                LogSupport.info(ctx, this, sb.toString());
            }
            return;
        }

        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(visitor.getProcessName());
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }

        try
        {
            visitor.visit(ctx, account);
        }
        catch (AgentException e)
        {
            throw new DunningProcessException(e.getMessage(), e);
        }
        catch (IllegalStateException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(visitor.getProcessName());
            sb.append(" failed for account '");
            sb.append(account.getBAN());
            sb.append("'");
            throw new DunningProcessException(sb.toString(), e);
        }
    }

    /**
     * CI implementation.
     */
    public void processAccount(final Date date, final Account account)
        throws DunningProcessException
    {
        processAccount(getContext(), date, account);
    }
    

    public void processAllAccounts(final Context ctx, final Date date) throws DunningProcessException
    {
        processAllAccounts(ctx, date, null);
    }
    
    
    /**
     * Processes all of the active accounts to update their dunned state.
     *
     * @param ctx The operating context.
     * @param date The current data used for cprocessing.
     * @throws DunningProcessException Thrown if a problem is encountered during processing.
     */
    public void processAllAccounts(final Context ctx, final Date date, final LifecycleAgentSupport lifecycleAgent)
        throws DunningProcessException
    {

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

	private StringBuilder getCreditCategorySql(Context context) {
		StringBuilder creditCategory =null;
    	
    	Collection<CreditCategory> CreditCategoryCollection;
		try {
			CreditCategoryCollection = HomeSupportHelper.get(context).getBeans(context,CreditCategory.class);
			
			if(CreditCategoryCollection!=null && CreditCategoryCollection.size()>0)
				creditCategory = new StringBuilder(" and acc.creditcategory in(");
			
			for(CreditCategory cc : CreditCategoryCollection){
				if(!cc.isDunningExempt()){
					creditCategory.append(cc.getCode()+",");
				}
			}
			creditCategory.replace(creditCategory.length()-1, creditCategory.length(), "");
		} catch (HomeInternalException e) {
			new MinorLogMsg(this, "Could not find Credit Caterogy. Skipping Credit Category Filter" );
		} catch (HomeException e) {
			new MinorLogMsg(this, "Could not find Credit Caterogy. Skipping Credit Category Filter" );
		}
		
		return (StringBuilder) (creditCategory!=null?creditCategory.append(")"):"");
	}

    /**
     * CI implementation.
     */
    public void processAllAccounts(final Date date)
        throws DunningProcessException
    {
        processAllAccounts(getContext(), date);
    }

    /**
     * CI implementation.
     */
    public void processAllAccounts(final Date date, final LifecycleAgentSupport lifecycleAgent)
        throws DunningProcessException
    {
        processAllAccounts(getContext(), date, lifecycleAgent);
    }

    /**
     * Processes all of the active accounts with the given bill cycle identifier
     * to update their dunned state.
     *
     * @param ctx The operating context.
     * @param date The current data used for processing.
     * @param billCycleIdentifier The identifier of the billing cycle.
     * @throws DunningProcessException Thrown if a problem is encountered during processing.
     */
    public void processAllAccountsWithBillCycleID(final Context ctx, final Date date, final int billCycleIdentifier)
        throws DunningProcessException
    {
        DunningProcessingAccountXStatementVisitor visitor = 
        		new DunningProcessingAccountXStatementVisitor(date, null);
        
        visitor.setJoinAgedDebt(true);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Start ");
            sb.append(visitor.getProcessName());
            sb.append(" for all accounts in Bill Cycle '");
            sb.append(billCycleIdentifier);
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }

        try
        {
            visitor.visit(ctx, getBillCycleFilter(billCycleIdentifier).concat(getCreditCategorySql(ctx).toString()));
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
            sb.append(" finished for all accounts in Bill Cycle '");
            sb.append(billCycleIdentifier);
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
    }

    /**
     * CI implementation.
     */
    public void processAllAccountsWithBillCycleID(final Date date, final int billCycleIdentifier)
        throws DunningProcessException
    {
        processAllAccountsWithBillCycleID(getContext(), date, billCycleIdentifier);
    }

    /**
     * Processes all of the active accounts with the given service provider to
     * update their dunned state.
     *
     * @param ctx The operating context.
     * @param date The current data used for processing.
     * @param serviceProviderIdentifier The identifier of the service provider.
     * @throws DunningProcessException Thrown if a problem is encountered during processing.
     */
    public void processAllAccountsWithServiceProviderID(final Context ctx, final Date date,
            final int serviceProviderIdentifier)
        throws DunningProcessException
    {
        DunningProcessingAccountXStatementVisitor visitor = new DunningProcessingAccountXStatementVisitor(date, null);
        visitor.setJoinAgedDebt(true);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Start ");
            sb.append(visitor.getProcessName());
            sb.append(" for all accounts in SPID '");
            sb.append(serviceProviderIdentifier);
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }

        try
        {
            visitor.visit(ctx, getSpidSql(serviceProviderIdentifier).concat(getCreditCategorySql(ctx).toString()));
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
            sb.append(" finished for all accounts in SPID '");
            sb.append(serviceProviderIdentifier);
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
    }

    /**
     * CI implementation.
     */
    public void processAllAccountsWithServiceProviderID(final Date date, final int spid)
        throws DunningProcessException
    {
        processAllAccountsWithServiceProviderID(getContext(), date, spid);
    }

    //mark is in dunning process, since dunning state sometimes should not propage to sub accounts.
    //we need to distinuish the cause of state changes
    public boolean isInDunningProcess(final Context ctx)
    {
        return ctx.getBoolean(DunningConstants.CONTEXT_KEY_IS_IN_DUNNING, false);
    }

    /**
     * CI implementation.
     */
    public boolean isInDunningProcess()
    {
        return isInDunningProcess(getContext());
    }


    /**
     * 
     * @param ctx
     * @param account
     * @return Computed value of the InArrears date. Null in case of an error.
     */
    protected Date getToBeInArrearsDateOrNull(Context ctx, final Account account)
    {
        Date toBeInArrearsDate = null;
        try
        {
            toBeInArrearsDate = account.getToBeInArrearsDate(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Could not find to-be-in-arrears-date for Account: " + account.getBAN(), e).log(ctx);
        }
        return toBeInArrearsDate;
    }


   /**
    * 
    * @param ctx
    * @param account
     * @return Computed value of the InArrears date. Null in case of an error.
    */
    protected Date getToBeDunnedDateOrNull(Context ctx, final Account account)
    {
        Date nextInDunnedDate = null;
        try
        {
            nextInDunnedDate = account.getToBeDunnedDate(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Could not find to-be-dunned-date for Account: " + account.getBAN(), e).log(ctx);
        }

        return nextInDunnedDate;
    }
    
    /**
     * Gets the OMLogSuuport for the given state.
     *
     * @param ctx The current context.
     * @param state The new state of the account.
     * @param caller The new object logging the OM.
     * @return An object that generates dunning OM messages for the given state.
     */
    protected OMLogSupport getOMLogSupport(final Context ctx, final AccountStateEnum state, final Object caller)
    {
        OMLogSupport omLogger;

        if (state == null)
        {
            omLogger = new ActiveOMLogSupport();
        }

        switch (state.getIndex())
        {
        case AccountStateEnum.ACTIVE_INDEX:
            {
                omLogger = new ActiveOMLogSupport();
                break;
            }
        case AccountStateEnum.NON_PAYMENT_WARN_INDEX:
            {
                omLogger = new WarnedOMLogSupport();
                break;
            }

        case AccountStateEnum.NON_PAYMENT_SUSPENDED_INDEX:
            {
                omLogger = new DunnedOMLogSupport();
                break;
            }
        case AccountStateEnum.IN_ARREARS_INDEX:
            {
                omLogger = new InArrearsOMLogSupport();
                break;
            }
        default:
            {
                LogSupport.major(ctx, caller, "Could not create proper OMLogSupport.  The provided account state " + state + " is not supported.");
                return new NullStateOMLogSupport(state);
            }
        }

        return omLogger;
    }

    public void omLoggerAttempt(final Context ctx, final AccountStateEnum state, final Object caller)
        throws DunningProcessException
    {
        final OMLogSupport omLogger = getOMLogSupport(ctx, state, caller);
        omLogger.attempt(ctx);
    }

    public void omLoggerAttempt(final AccountStateEnum state, final Object caller)
        throws DunningProcessException
    {
        omLoggerAttempt(getContext(), state, caller);
    }

    /**
     * Logs a Dunning Action event record for the given subscriber, action, and result.
     *
     * @param subscriber The subscriber for which to log an event record.
     * @param state The state of the account.
     * @param result The result code of the event record.
     * @param localAccount Subscriber's Account
     */
    public void logEventRecord(
            final Context ctx,
            final Subscriber subscriber,
            final AccountStateEnum state,
            final int result,
            final Account localAccount)
        throws DunningProcessException
    {
        final DunningActionER er = new DunningActionER();
        er.setErDate(System.currentTimeMillis());
        er.setSpid(localAccount.getSpid());
        er.setVoiceMobileNumber(subscriber.getMSISDN());
        er.setBAN(localAccount.getBAN());
        er.setOldAccountState(localAccount.getState().getIndex());
        er.setNewAccountState(state.getIndex());
        er.setPromiseToPayDate(localAccount.getPromiseToPayDate());
        er.setResultCode(result);
        er.setToBeDunnedDate(getToBeDunnedDateOrNull(ctx, localAccount));
        er.setToBeInArrearsDate(getToBeInArrearsDateOrNull(ctx, localAccount));
        er.setDueDate(localAccount.getPaymentDueDate());

        new ERLogMsg(ctx, er).log(ctx);

        try
        {
            HomeSupportHelper.get(ctx).createBean(ctx, er);
        }
        catch (HomeException e)
        {
            Logger.minor(ctx, this, "Error while saving DunningActionER to the DB table: " + e.getMessage(), e);
        }
    }

    /**
     * CI implementation.
     */
    public void logEventRecord(
            final Subscriber subscriber,
            final AccountStateEnum state,
            final int result,
            final Account localAccount)
        throws DunningProcessException
    {
        logEventRecord(getContext(), subscriber, state, result, localAccount);
    }


    /**
     * @param state
     * @param subscriber
     * @param overallSuccess
     * @param caller
     */
    public void doSubscriberDunningResultOm(
            final Context ctx,
            final AccountStateEnum state,
            final Subscriber subscriber,
            final boolean overallSuccess,
            final Object caller)
        throws DunningProcessException
    {
        final OMLogSupport omLogger = getOMLogSupport(ctx, state, caller);

        if (overallSuccess)
        {
            omLogger.success(ctx);
        }
        else
        {
            omLogger.fail(ctx);

            new EntryLogMsg(10582, caller, "[None]", "", new String[]{subscriber.getId(), state.getDescription()},
                    null).log(ctx);
        }
    }

    public void doSubscriberDunningResultOm(
            final AccountStateEnum state,
            final Subscriber subscriber,
            final boolean overallSuccess,
            final Object caller)
        throws DunningProcessException
    {
        doSubscriberDunningResultOm(getContext(), state, subscriber, overallSuccess, caller);
    }


    public void generateReport(Date date, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        generateReport(getContext(), date, lifecycleAgent);
    }

    public void generateReport(Context ctx, Date date, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        setContext(ctx);
        
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            sb.append(" for reports including date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
        
        try
        {
            Collection<CRMSpid> spids = HomeSupportHelper.get(ctx).getBeans(ctx, CRMSpid.class);
            for (CRMSpid spid : spids)
            {
                try
                {
                	int advanceReportDays = spid.getAdvanceDunningReportDays();		            
		        	CalendarSupport calSupp = CalendarSupportHelper.get(ctx);
		        	Date advdate = calSupp.findDateDaysAfter(advanceReportDays, date);
                    generateSpidReport(ctx, advdate, spid, lifecycleAgent);
                }
                catch (AbortVisitException e)
                {
                    throw e;
                }
                catch (Throwable t)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
                    sb.append("failed for dates including '");
                    sb.append(CoreERLogger.formatERDateDayOnly(date));
                    sb.append("': ");
                    sb.append(t.getMessage());
                    LogSupport.major(ctx, this, sb.toString(), t);
                    throw new DunningProcessException(sb.toString(), t);
                }
            }
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            sb.append("failed for dates including '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("': Unable to retrieve spids");

            LogSupport.major(ctx, this, sb.toString(), e);
            throw new DunningProcessException(sb.toString(), e);
        }
       
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
                sb.append(" successfully finished for reports including date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'");
                LogSupport.info(ctx, this, sb.toString());
            }
        }
    }

    public void generateSpidReport(Date date, final int spid, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        generateSpidReport(getContext(), date, spid, lifecycleAgent);
    }

    private void generateSpidReport(Context ctx, Date date, final CRMSpid spid, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        setContext(ctx);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            sb.append(" for reports including date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' on SPID '");
            sb.append(spid.getSpid());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
        
        DunningReportGenerationSpidVisitor visitor = new DunningReportGenerationSpidVisitor(date, lifecycleAgent);
        try
        {
            visitor.visit(ctx, spid);

            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
                sb.append(" successfully executed for reports including date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("' and SPID '");
                sb.append(spid.getSpid());
                sb.append("'");
                LogSupport.info(ctx, this, sb.toString());
            }
        }
        catch (AbortVisitException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            cause.append(" was aborted");

            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" while executing for reports including date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' and SPID '");
            sb.append(spid.getSpid());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            throw e;
        }
        catch (Throwable e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            cause.append(" failed while executing for reports including date '");
            cause.append(CoreERLogger.formatERDateDayOnly(date));
            cause.append("' and SPID '");
            cause.append(spid.getSpid());
            cause.append("'");

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
            sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            sb.append(" successfully finished for reports including date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' on SPID '");
            sb.append(spid.getSpid());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
    }

    public void generateSpidReport(Context ctx, Date date, final int spid, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        try
        {
            CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            generateSpidReport(ctx, date, crmSpid, lifecycleAgent);
        }
        catch (HomeException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Unable to retrieve SPID '");
            cause.append(spid);
            cause.append("'");
            
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" during ");
            sb.append(DunningReportRecordGenerationAccountVisitor.getVisitorProcessName());
            sb.append(" for reports including date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' on SPID '");
            sb.append(spid);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            
            throw new DunningProcessException(cause.toString(), e);
        }
    }
    
    public void processReport(final Date date, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException,
        DunningProcessInternalException
    {
        processReport(getContext(), date, lifecycleAgent);
    }

    public void processReport(final Context ctx, final Date date, final LifecycleAgentSupport lifecycleAgent)
    throws DunningProcessException, DunningProcessInternalException
    {
        setContext(ctx);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            sb.append(" for reports up to date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
        
        try
        {
            Collection<CRMSpid> spids = HomeSupportHelper.get(ctx).getBeans(ctx, CRMSpid.class);
            for (CRMSpid spid : spids)
            {
                try
                {
                    processSpidReport(ctx, date, spid, lifecycleAgent);
                }
                catch (AbortVisitException e)
                {
                    throw e;
                }
                catch (Throwable t)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
                    sb.append("failed for dates up to '");
                    sb.append(CoreERLogger.formatERDateDayOnly(date));
                    sb.append("': ");
                    sb.append(t.getMessage());
                    LogSupport.major(ctx, this, sb.toString(), t);
                    throw new DunningProcessException(sb.toString(), t);
                }
            }
        }
        catch (HomeException e)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            sb.append("failed for dates up to '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("': Unable to retrieve spids");

            LogSupport.major(ctx, this, sb.toString(), e);
            throw new DunningProcessException(sb.toString(), e);
        }
       
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
                sb.append(" successfully finished for reports up to date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("'");
                LogSupport.info(ctx, this, sb.toString());
            }
        }
    }

    public void processSpidReport(final Date date, final int spid, final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException,
    DunningProcessInternalException
    {
        processSpidReport(getContext(), date, spid, lifecycleAgent);
    }


    public void processSpidReport(final Context ctx, final Date date, final CRMSpid spid,
            final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        setContext(ctx);
        if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Starting ");
            sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            sb.append(" for reports up to date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' on SPID '");
            sb.append(spid.getSpid());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }
        
        DunningReportProcessingSpidVisitor visitor = new DunningReportProcessingSpidVisitor(date, lifecycleAgent);
        try
        {
            visitor.visit(ctx, spid);

            if (LogSupport.isEnabled(ctx, SeverityEnum.INFO))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
                sb.append(" successfully executed for reports up to date '");
                sb.append(CoreERLogger.formatERDateDayOnly(date));
                sb.append("' and SPID '");
                sb.append(spid.getSpid());
                sb.append("'");
                LogSupport.info(ctx, this, sb.toString());
            }
        }
        catch (AbortVisitException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            cause.append(" was aborted");

            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" while executing for reports up to date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' and SPID '");
            sb.append(spid.getSpid());
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            throw e;
        }
        catch (Throwable e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            cause.append(" failed while executing for reports up to date '");
            cause.append(CoreERLogger.formatERDateDayOnly(date));
            cause.append("' and SPID '");
            cause.append(spid.getSpid());
            cause.append("'");

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
            sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            sb.append(" successfully finished for reports up to date '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' on SPID '");
            sb.append(spid.getSpid());
            sb.append("'");
            LogSupport.info(ctx, this, sb.toString());
        }

    }
    
    public void processSpidReport(final Context ctx, final Date date, final int spid,
            final LifecycleAgentSupport lifecycleAgent) throws DunningProcessException, DunningProcessInternalException
    {
        try
        {
            com.redknee.framework.xhome.context.Context appContext = (Context) ctx.get("AppCrm");
        	appContext.put(InvoiceCalculationSupport.DUNNING_TASK, true);
        	ctx.put(InvoiceCalculationSupport.DUNNING_TASK, true);
            CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            processSpidReport(ctx, date, crmSpid, lifecycleAgent);
        }
        catch (HomeException e)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("Unable to retrieve SPID '");
            cause.append(spid);
            cause.append("'");
            
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" during ");
            sb.append(DunningReportRecordProcessingVisitor.getVisitorProcessName());
            sb.append(" for reports with dates up to '");
            sb.append(CoreERLogger.formatERDateDayOnly(date));
            sb.append("' and SPID '");
            sb.append(spid);
            sb.append("': ");
            sb.append(e.getMessage());
            LogSupport.major(ctx, this, sb.toString(), e);
            
            throw new DunningProcessException(cause.toString(), e);
        }
    }

    
    
    /**
     * Gets a predicate for accounts to be processed by the dunning agent.
     * @param ctx 
     * @return
     */
    public static And getMainFilter(Context ctx, int spid)
    {
        final And condition = new And();
        final Or or = new Or();
        final And and = new And();
        final Set<AccountStateEnum> excludedStates = new HashSet<AccountStateEnum>();
        boolean suspendAccount = SpidSupport.isAllowDunningProcessSuspendedAccount(ctx, spid);
        if(!suspendAccount)
        {
        	excludedStates.add(AccountStateEnum.SUSPENDED);
        }
        excludedStates.add(AccountStateEnum.INACTIVE);
        and.add(new Not(new In(AccountXInfo.STATE, excludedStates)));
        or.add(and);
        or.add(new EQ(AccountXInfo.PTP_TERMS_TIGHTENED, Boolean.TRUE));
        condition.add(or);
        condition.add(new EQ(AccountXInfo.RESPONSIBLE, Boolean.TRUE));
        condition.add(new NEQ(AccountXInfo.SYSTEM_TYPE, SubscriberTypeEnum.PREPAID));
        
        return condition;

    }
    
    public static StringBuilder getMainSql(){
    	StringBuilder sql = new StringBuilder("select distinct(adbt.ban) from ageddebt adbt" +
			 	 " left outer join account acc on" +
			 	 " acc.ban = adbt.ban"+
			 	 " where (acc.state not in(1,2) or acc.ptptermstightened = 'y')"+
			 	 " and acc.responsible = 'y'"+
			 	 " and acc.systemtype = 0");
			 
    	return sql;
    }
    /**
     * Gets a predicate based on the Bill Cycle.
     * @param spid
     * @return
     */
    public static String getBillCycleFilter(final int billCycleId)
    {
		String condition = AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNING_DEFAULT_KEY)
				.concat(AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNING_BILLCYCLE_KEY))
				.concat("" + Integer.valueOf(billCycleId));
		// condition.add(new EQ(AccountXInfo.BILL_CYCLE_ID,
		// Integer.valueOf(billCycleId)));
        
        return condition;
    }
    
    
    /**
     * Gets a predicate based on the SPID.
     * @param spid
     * @return
     */
    public static String getSpidSql(final int spid)
    {
        final String condition = AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNING_DEFAULT_KEY)
        						.concat(AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNING_SPID_KEY)
        						.concat(""+Integer.valueOf(spid)));
        //condition.add(new EQ(AccountXInfo.SPID, Integer.valueOf(spid)));
        return condition;
    }
    
    public static And getSpidFilter(Context ctx,final int spid)
    {
        final And condition = getMainFilter(ctx,spid);
        condition.add(new EQ(AccountXInfo.SPID, Integer.valueOf(spid)));
        
        return condition;
    }
    
    public Context getContext()
    {
        return ctx_;
    }

    public void setContext(final Context ctx)
    {
        this.ctx_ = ctx;
    }

    /**
     * The operating context.
     */
    private Context ctx_;
    
    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;


}
