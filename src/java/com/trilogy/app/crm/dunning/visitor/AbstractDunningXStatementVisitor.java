/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor;

import java.util.Date;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.GeneralConfig;

import com.trilogy.app.crm.dunning.DunningAgent;
import com.trilogy.app.crm.invoice.InvoiceCalculationSupport;

import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.AbstractJDBCXDB;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to visit XStatements during dunning and delegating result to
 * DunningProcessThreadPoolVisitor to process.
 * 
 * @author Marcio Marques
 * @author odeshpande
 * @since 9.0 , modified 10.2
 */
public abstract class AbstractDunningXStatementVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

	/**
     * Creates a new AbstractDunningXStatementVisitor visitor.
     * 
     * @param runningDate
     * @param accountVisitor
     * @param lifecycleAgent
     */
    public AbstractDunningXStatementVisitor(final Date runningDate, final Visitor accountVisitor,
            final LifecycleAgentSupport lifecycleAgent)
    {
        runningDate_ = runningDate;
        accountVisitor_ = accountVisitor;
        lifecycleAgent_ = lifecycleAgent;
    }


    /**
     * Action to be performed when item is successfully visited by DunningAccountVisitor.
     */
    public abstract void onItemSuccess();


    /**
     * Action to be performed when there is a failure on a visited item by
     * DunningAccountVisitor.
     */
    public abstract void onItemFailure();


    /**
     * Return the process name.
     * 
     * @return
     */
    public String getProcessName()
    {
        return "";
    }


    /**
     * Return the DunningAccountVisitor which will be used by this
     * DunningPredicateVisitor.
     */
    public Visitor getAccountVisitor()
    {
        return accountVisitor_;
    }
    
    protected abstract DunningAgent getExecuter();


    /**
     * {@inheritDoc}
     */
    public void visit(final Context context, final Object obj) throws AgentException, AbortVisitException
    {
        String processName = getProcessName();
        DunningProcessThreadPoolVisitor threadPoolVisitor = new DunningProcessThreadPoolVisitor(context,
                getThreadPoolSize(context), getThreadPoolQueueSize(context),lifecycleAgent_,getExecuter());
        try
        {
        	
        	if(obj instanceof XStatement){//Flow of dunning report generation
        		XStatement xStatement = (XStatement) obj;
        		final Home home = getHomeToVisit(context);
                home.where(context, xStatement).forEach(context, threadPoolVisitor);
        	}else{        	
        		final XDB xdb= (XDB) context.get(XDB.class);
            	context.put(AbstractJDBCXDB.FETCH_SIZE_KEY, getConfiguredFetchSize(context));
        		String sqlQuery = ((String)obj);
        		xdb.forEach(context, threadPoolVisitor, isJoinAgedDebt()?sqlQuery.replace("{0}", getJoinSql(context)):sqlQuery);
        	}
        }
        catch (final HomeException e)
        {
            String cause = "Unable to retrieve accounts";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.major(context, this, sb.toString(), e);
            throw new IllegalStateException(processName + " failed: " + cause, e);
        }
        catch (final Throwable e)
        {
            String cause = "General error";
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(": ");
            sb.append(e.getMessage());
            LogSupport.major(context, this, sb.toString(), e);
            throw new IllegalStateException(processName + " failed: " + cause, e);
        }
        finally
        {
        	LogSupport.minor(context, this, "Unset Dunning flag in application context", null);
        	context.put(InvoiceCalculationSupport.DUNNING_TASK, false);
            try
            {
                threadPoolVisitor.getPool().shutdown();
                threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
            }
            catch (final Exception e)
            {
                LogSupport.minor(context, this, "Exception catched during wait for completion of all dunning threads",
                        e);
            }
        }
        if (threadPoolVisitor.getFailedBANs() != null && threadPoolVisitor.getFailedBANs().size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The ");
            sb.append(processName);
            sb.append(" has finished executing but a set of accounts could not be processed: ");
            sb.append(threadPoolVisitor.getFailedBANs());
            LogSupport.info(context, this, sb.toString());
            throw new AgentException(sb.toString());
        }
    }

	private Object getMaxDate(Context context) {
		Date oldestAgedDebt = new Date();
//		
//		try {
//			Collection<CreditCategory> CreditCategoryCollection =  HomeSupportHelper.get(context).getBeans(context,CreditCategory.class);
//			for(CreditCategory cc : CreditCategoryCollection){
//				if(!cc.isDunningExempt()){
//					CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);
//					
//					spid = HomeSupportHelper.get(context).findBean(context, CRMSpid.class,
//	                        new EQ(CRMSpidXInfo.ID, Integer.valueOf(cc.getSpid())));					
//					Date agedDebtDate = getAccountVisitor().getOldestAgedDebtToLook(context, spid, cc);
//					if(agedDebtDate.before(oldestAgedDebt))
//						oldestAgedDebt = agedDebtDate;
//				}
//			}
//			
//		} catch (HomeInternalException e) {
//			new MinorLogMsg(this, "Could not find Credit Caterogy. Skipping Credit Category Filter" );
//		} catch (HomeException e) {
//			new MinorLogMsg(this, "Could not find Credit Caterogy. Skipping Credit Category Filter" );
//		}
		return oldestAgedDebt.getTime();
	}


	/**
     * Get Thread pool size from configuration
     * 
     * @param ctx
     * @return
     */
    private int getThreadPoolSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDunningProcessThreads();
    }


    /**
     * Get queue size from configuration
     * 
     * @param ctx
     * @return
     */
    private int getThreadPoolQueueSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDunningProcessQueueSize();
    }
    
    private int getConfiguredFetchSize(Context ctx)
    {
        GeneralConfig gc = (GeneralConfig) ctx.get(GeneralConfig.class);
        return gc.getDunningProcessFetchSize();
    }
    
    protected String getJoinSql(Context context) throws HomeInternalException, HomeException{
    	String sql = new String(" adbt.duedate >="+ getMaxDate(context)+
    										  " and adbt.accumulateddebt > 0");
		return sql;
    }
    
    protected Date getRunningDate()
    {
        return runningDate_;
    }


    protected LifecycleAgentSupport getLifecycleAgent()
    {
        return lifecycleAgent_;
    }

	 public boolean isJoinAgedDebt() {
			return joinAgedDebt;
	}


	public void setJoinAgedDebt(boolean joinAgedDebt) {
			this.joinAgedDebt = joinAgedDebt;
	}
	
	protected Home getHomeToVisit(Context context)
	{
		Home accountHome = (Home) context.get(AccountHome.class);
		return accountHome;
	}
	
	private boolean joinAgedDebt;
	private Date runningDate_;
    private LifecycleAgentSupport lifecycleAgent_;
    private Visitor accountVisitor_;
    public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
}
