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


package com.trilogy.app.crm.dunning.visitor.accountprocessing;

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

//import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.trilogy.app.crm.dunning.AccountDunningSqlGenerator;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.visitor.AbstractAccountDunningPolicyAssignement;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningAccountProcessor;
import com.trilogy.app.crm.dunning.visitor.AccountDunningPolicyThreadPoolVisitor;
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
 * Abstract class responsible to process accounts during DunningPolicy Assignment.
 *
 * @author Sapan Modi
 * 
 */
public class AccountDunningPolicyAssignmentVisitor extends AbstractAccountDunningPolicyAssignement{
	
	
	
	  /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int MAXIMUM_ACCOUNTS_CHUNK = 500;
    
	/**
     * Creates a new AccountDunningPolicyAssignmentVisitor visitor.
     * 
     * @param lifecycleAgent
     */
	public AccountDunningPolicyAssignmentVisitor(final LifecycleAgentSupport lifecycleAgent)
	{
	   super();
	   lifecycleAgent_=lifecycleAgent;
	}

	   /**
     * Return the AccountDunningPolicyAssignmentVisitor which will be used by this
     * dunningPolicyPredicateVisitor_.
     */
    public AccountDunningPolicyAssignmentVisitor getAccountVisitor()
    {
        return accountVisitor_;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Context ctx,Object obj) throws AgentException, AbortVisitException
    {
    	
    	
    	PMLogMsg pm = new PMLogMsg(this.getClass().getSimpleName(), "AssiginingDunningPolicy");
    	PrintWriter fileWriter=null ;
        final Home accountHome = (Home) ctx.get(AccountHome.class);
        String filePath = CoreSupport.getFileBase(ctx);
    	AccountDunningPolicyThreadPoolVisitor threadPoolVisitor = new AccountDunningPolicyThreadPoolVisitor(ctx,
                 getThreadPoolSize(ctx), getThreadPoolQueueSize(ctx), this, lifecycleAgent_);
    	 
 		
 		  try
 	        {
 	          String mainSql = AccountDunningSqlGenerator.getDunningSqlGenerator().get(DunningConstants.DUNNINGPOLICY_DEFAULT_KEY);
 	          
 	         List<Object> banList = (List<Object>)AccountSupport.getQueryDataList(ctx, mainSql);
         	
         	int totalAcctSize =  banList.size();
         	int initSize = 0;
         	int size = MAXIMUM_ACCOUNTS_CHUNK;

         	if(totalAcctSize < MAXIMUM_ACCOUNTS_CHUNK)
         	{
         		size = totalAcctSize;
         	}
         	while(size <= totalAcctSize)
         	{	
         		if(totalAcctSize - size < MAXIMUM_ACCOUNTS_CHUNK)
         		{
         			size = totalAcctSize;
         		}
         		List acctChunkList = banList.subList(initSize, size);
         		
         		initSize = size;
         		size = size + MAXIMUM_ACCOUNTS_CHUNK;
         		
         		String banString = StringUtils.join(acctChunkList, "','");
         		
         		SimpleXStatement predicate = new SimpleXStatement(" BAN IN ( '"+ banString +"' ) ");
         		try
                 {
         			Collection<Account> accounts = accountHome.where(ctx, predicate).selectAll(ctx);
         			Iterator<Account> itrator = accounts.iterator();
         			while(itrator.hasNext())
         			{
         				Account account = itrator.next();
         				threadPoolVisitor.visit(ctx, account);
         			}
                 }
                 catch (final Exception e)
                 {
                     LogSupport.minor(ctx, this, "Error getting while process account. ", e);
                 }
         	}
         

 	        		
 	        	
 	        }
 	        catch (final HomeException e)
 	        {
 	            String cause = "Unable to retrieve accounts";
 	            StringBuilder sb = new StringBuilder();
 	            sb.append(cause);
 	            sb.append(": ");
 	            sb.append(e.getMessage());
 	            LogSupport.major(ctx, this, sb.toString(), e);
 	            throw new IllegalStateException("Account Dunning Policy Process" + " failed: " + cause, e);
 	        }
 	        catch (final Throwable e)
 	        {
 	            String cause = "General error";
 	            StringBuilder sb = new StringBuilder();
 	            sb.append(cause);
 	            sb.append(": ");
 	            sb.append(e.getMessage());
 	            LogSupport.major(ctx, this, sb.toString(), e);
 	            throw new IllegalStateException("Account Dunning Policy Process" + " failed: " + cause, e);
 	        }
 	        finally
 	        {
 	        	
 	            try
 	            {
 	                threadPoolVisitor.getPool().shutdown();
 	                threadPoolVisitor.getPool().awaitTerminationAfterShutdown(TIME_OUT_FOR_SHUTTING_DOWN);
 	            }
 	            catch (final Exception e)
 	            {
 	                LogSupport.minor(ctx, this, "Exception catched during wait for completion of all dunningPolicy threads",
 	                        e);
 	            }
 	            ERLogger.generateDunningPolicyAssignmentER(ctx,threadPoolVisitor.getFailedBANs().size());
 	        }
 		    pm.log(ctx);
 	        if (threadPoolVisitor.getFailedBANs() != null && threadPoolVisitor.getFailedBANs().size() > 0)
 	        {
 	            StringBuilder sb = new StringBuilder();
 	            sb.append("The ");
 	            sb.append("Account Dunning Policy Process");
 	            sb.append(" has finished executing but a set of accounts could not be processed: ");
 	            sb.append(threadPoolVisitor.getFailedBANs());
 	            LogSupport.info(ctx, this, sb.toString());
 	           try {
 	        	   	File accountFile = new File(fetchFileNameByDateFormat(ctx, filePath, File.separator));
 	        	   	
					fileWriter = new PrintWriter(accountFile);
					LogSupport.info(ctx, this, "Started printing BANs in");
					fileWriter.write(sb.toString());
 	           	} catch (FileNotFoundException e) {
				
 	           		LogSupport.info(ctx, this, "Unable to find the File to write the account BANS",e);
 	           	} catch (IOException e) {
 	           		LogSupport.info(ctx, this, "Unable to create file",e);
				}finally
 	           	{
 	           		if(fileWriter !=null)
 	           		{
 	           		fileWriter.flush();
 	                fileWriter.close();  
 	           		}
 	           	}
 	            throw new AgentException(sb.toString());
 	        }

 	
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
	protected LifecycleAgentSupport getLifecycleAgent()
	    {
	        return lifecycleAgent_;
	    }
	
	/**
     * @param outBoundFileName
     * @param formatCharacter 
     */
    private String fetchFileNameByDateFormat(Context ctx, String outBoundFileName, String formatCharacter)
    {
        int index = outBoundFileName.indexOf(File.separator);
        Date currentDate = new Date();
        StringBuffer sb = new StringBuffer(File.separator);
        String pattern = Pattern.quote(File.separator);
        if(index != -1)
          {
            String[] arrStr = outBoundFileName.split(pattern);
            for (int i = 1; i < arrStr.length-1; i++)
            {
                sb.append(arrStr[i]);
                sb.append(File.separator);
              	
             }
          }
        
         
         sb.append("log");
         sb.append(File.separator);
         String dateFormat ="yyyymmdd";
  	   	 SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
  	   	 String dateString = sdf.format(new Date());
  	   	
  	     sb.append("Account");
  	     sb.append(dateString);
  	     sb.append(".csv");
  	    
         
         return sb.toString();
    }

	private LifecycleAgentSupport lifecycleAgent_;
	private AccountDunningPolicyAssignmentVisitor accountVisitor_;
	public static final long TIME_OUT_FOR_SHUTTING_DOWN = 60 * 1000;
	

	

}
