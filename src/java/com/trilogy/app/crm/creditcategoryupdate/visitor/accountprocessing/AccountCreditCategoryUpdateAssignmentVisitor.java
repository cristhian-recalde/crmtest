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


package com.trilogy.app.crm.creditcategoryupdate.visitor.accountprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.trilogy.app.crm.creditcategoryupdate.AccountCreditCategoryUpdateSqlGenerator;
import com.trilogy.app.crm.creditcategoryupdate.CreditCategoryUpdateConstants;
import com.trilogy.app.crm.creditcategoryupdate.visitor.AbstractAccountCreditCategoryUpdateAssignment;
import com.trilogy.app.crm.creditcategoryupdate.visitor.AccountCreditCategoryUpdateThreadPoolVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.lifecycle.LifecycleAgentSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.AbstractJDBCXDB;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import org.apache.commons.lang.StringUtils;

//import sun.swing.FilePane;

/**
 * Abstract class responsible to process accounts during Credit Category Update Assignment.
 *
 * @author Himanshu Saxena
 * 
 */
public class AccountCreditCategoryUpdateAssignmentVisitor extends AbstractAccountCreditCategoryUpdateAssignment{
	
	
	
	  /**
     * 
     */
    private static final long serialVersionUID = 1L;
     
    private static final int MAXIMUM_ACCOUNTS_CHUNK = 500;
	/**
     * Creates a new AccountCreditCategoryUpdateAssignmentVisitor visitor.
     * 
     * @param lifecycleAgent
     */
	public AccountCreditCategoryUpdateAssignmentVisitor(final LifecycleAgentSupport lifecycleAgent)
	{
	   super();
	   lifecycleAgent_=lifecycleAgent;
	}

	   /**
     * Return the AccountCreditCategoryUpdateAssignmentVisitor which will be used by this
     * accountVisitor_.
     */
    public AccountCreditCategoryUpdateAssignmentVisitor getAccountVisitor()
    {
        return accountVisitor_;
    }
    /**
     * {@inheritDoc}
     */
    @Override
 public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException
 {
    	if (LogSupport.isDebugEnabled(ctx)){
    		LogSupport.debug(ctx, this, "AccountCreditCategoryUpdateAssignmentVisitor is Started!!!!!!!!");
    	}
		PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AssiginCreditCategoryUpdate");
		final Home accountHome = (Home) ctx.get(AccountHome.class);
		AccountCreditCategoryUpdateThreadPoolVisitor threadPoolVisitor = new AccountCreditCategoryUpdateThreadPoolVisitor(
				ctx, CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_THREAD_POOL_SIZE,CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_THREAD_QUEUE_SIZE, this, lifecycleAgent_);
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this, "CREDIT_CATEGORY_UPDATE Thread Size:: "+CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_THREAD_POOL_SIZE+" Queue Size:: "+CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_THREAD_QUEUE_SIZE);
		}
		try {
			String mainSql = AccountCreditCategoryUpdateSqlGenerator.getcreditCategorySqlGenerator()
					.get(CreditCategoryUpdateConstants.CREDIT_CATEGORY_UPDATE_DEFAULT_KEY);
			List<Object> banList = (List<Object>) AccountSupport.getQueryDataList(ctx, mainSql);
			int totalAcctSize = banList.size();
			int initSize = 0;
			int size = MAXIMUM_ACCOUNTS_CHUNK;
			if (totalAcctSize < MAXIMUM_ACCOUNTS_CHUNK) {
				size = totalAcctSize;
			}
			while (size <= totalAcctSize) {
				if (totalAcctSize - size < MAXIMUM_ACCOUNTS_CHUNK) {
					size = totalAcctSize;
				}
				List acctChunkList = banList.subList(initSize, size);
				initSize = size;
				size = size + MAXIMUM_ACCOUNTS_CHUNK;
				String banString = StringUtils.join(acctChunkList, "','");
				if (LogSupport.isDebugEnabled(ctx)){
					LogSupport.debug(ctx, this,
						"AccountCreditCategoryUpdateAssignmentVisitor StringUtil Usage:: " + banString);
				}
				SimpleXStatement predicate = new SimpleXStatement(" BAN IN ( '" + banString + "' ) ");
				try {
					Collection<Account> accounts = accountHome.where(ctx, predicate).selectAll(ctx);
					Iterator<Account> itrator = accounts.iterator();
					while (itrator.hasNext()) {
						Account account = itrator.next();
						threadPoolVisitor.visit(ctx, account);
					}
				} catch (final Exception e) {
					LogSupport.minor(ctx, this, "Error getting while process account. ", e);
				}
			}
		} catch (final HomeException e) {
			String cause = "Unable to retrieve accounts";
			StringBuilder sb = new StringBuilder();
			sb.append(cause);
			sb.append(": ");
			sb.append(e.getMessage());
			LogSupport.major(ctx, this, sb.toString(), e);
			throw new IllegalStateException("Account Credit Category Update Policy Process" + " failed: " + cause, e);
		} catch (final Throwable e) {
			String cause = "General error";
			StringBuilder sb = new StringBuilder();
			sb.append(cause);
			sb.append(": ");
			sb.append(e.getMessage());
			LogSupport.major(ctx, this, sb.toString(), e);
			throw new IllegalStateException("Account Credit Category Update Process" + " failed: " + cause, e);
		} finally {
			try {
				threadPoolVisitor.getPool().shutdown();
				threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
			} catch (final Exception e) {
				LogSupport.minor(ctx, this, "Exception catched during wait for completion of all Credit Category Update threads",
						e);
			}
		}
		pm.log(ctx);
		if (threadPoolVisitor.getFailedBANs() != null && threadPoolVisitor.getFailedBANs().size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("The ");
			sb.append("Account Credit Category Update Process");
			sb.append(" has finished executing but a set of accounts could not be processed: ");
			sb.append(threadPoolVisitor.getFailedBANs());
			LogSupport.info(ctx, this, sb.toString());
			throw new AgentException(sb.toString());
		}
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, this, "AccountCreditCategoryUpdateAssignmentVisitor is End!!!!!!!!");
		}
	}
	protected LifecycleAgentSupport getLifecycleAgent()
	{
	        return lifecycleAgent_;
	}
	
	private LifecycleAgentSupport lifecycleAgent_;
	private AccountCreditCategoryUpdateAssignmentVisitor accountVisitor_;
	public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
	

	

}
