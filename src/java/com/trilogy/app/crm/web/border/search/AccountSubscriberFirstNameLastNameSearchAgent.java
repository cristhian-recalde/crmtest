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
package com.trilogy.app.crm.web.border.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ConvergedAccountSubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAwareHome;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Context agent that controls the first name last name functionality 
 * 
 * @author amedina
 *
 */
public class AccountSubscriberFirstNameLastNameSearchAgent extends
		ContextAgentProxy implements SQLJoinCreator 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8357836156864032276L;


	public AccountSubscriberFirstNameLastNameSearchAgent(ContextAgent arg0, SQLJoinCreator delegate) 
	{
		super(arg0);
		join_ = delegate;
	}

	public AccountSubscriberFirstNameLastNameSearchAgent(SQLJoinCreator delegate) 
	{
		super();
		join_ = delegate;
	}

	
	public void execute(Context ctx)
	      throws AgentException
	{
		  ConvergedAccountSubscriberSearch criteria = (ConvergedAccountSubscriberSearch)SearchBorder.getCriteria(ctx);
		  searchTypeIndex_ = criteria.getType().getIndex();


		  Home acctHome_ = (Home)ctx.get(AccountHome.class);
		  Home subHome_ = (Home)ctx.get(SubscriberHome.class);
		  Home conAcctSub=null;

		  try
		  {
			  //Map Account
			  if (getSearchTypeIndex() == 0 || getSearchTypeIndex() == 2)
			  {
				  
				  Collection acctBeanCollection = executeCommand(ctx, acctHome_, getSqlJoinClauseXStatment(ctx, 0));
				  if ( acctBeanCollection != null && !acctBeanCollection.isEmpty())
				  {
					  Subscriber sub=null;
					  for(Iterator it = acctBeanCollection.iterator(); it.hasNext();)
					  {
						  Account acct = (Account)it.next();
						  conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,acct,sub,conAcctSub,(short)getSearchTypeIndex());
					  }
							
				  }
			  }
			  //Map Subscribers
			  if(getSearchTypeIndex() == 1)
			  {
	    			  
				  Collection subBeanCollection = executeCommand(ctx, subHome_, getSqlJoinClauseXStatment(ctx, 1));
	    		      
				  if ( subBeanCollection != null && !subBeanCollection.isEmpty())
				  {
					  conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,null,subBeanCollection,conAcctSub,(short)getSearchTypeIndex());
				  }
			  }
			  //For Account/Subscriber search eventhough account does not satisfy the criteria but its subscriber do,it should be displayed into result set.
			  if(getSearchTypeIndex()==2)
			  {
				  try
				  {
					  Collection subBeanCollection = executeCommand(ctx, subHome_, getSqlJoinClauseXStatment(ctx, 2));
					  if ( subBeanCollection != null && !subBeanCollection.isEmpty())
					  {
						  for(Iterator it = subBeanCollection.iterator(); it.hasNext();)
						  {
							  Subscriber sub = (Subscriber)it.next();
									
							  Account acct= AccountSupport.getAccount(ctx,sub.getBAN());
							  conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,acct,sub,conAcctSub,(short)getSearchTypeIndex());
						  }
					  }
				  }
				  catch (HomeException e)
				  {
					  if(LogSupport.isDebugEnabled(ctx))
					  {
						  new DebugLogMsg(ConvergedAccountSubscriberSupport.class.getName(),e.getMessage(),e).log(ctx);
					  }
				  }  
			  }

		  }
		  catch (HomeException e)
		  {
			  //as of now eat it, decide strategy later on 
		  }
	    	 
		  if( conAcctSub != null)
		  {
			  ctx.put(ConvergedAccountSubscriberHome.class, new SpidAwareHome(ctx, conAcctSub));
		  }
		  else
		  {
			  ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
		  }
	      delegate(ctx);

	}


	private Collection executeCommand(Context ctx, Home home, XStatement sqlJoinClause) throws HomeException
	{
		Collection coll = null;
	
		
		if (sqlJoinClause != null && home != null)
		{
			coll = home.where(ctx, sqlJoinClause).selectAll(ctx);
		}
		else
		{
			coll = new ArrayList();
		}
		return coll;
	}

    public String getSqlJoinClause(Context ctx, int searchTypeIndex)
    {
        String sql = join_.getSqlJoinClause(ctx, searchTypeIndex);
        LogSupport.debug(ctx, this, "Complete sql clause is : " + sql);

        return sql;
    }

    public com.redknee.app.crm.web.border.search.SimpleXStatementTruePredicate getSqlJoinClauseXStatment(Context ctx, int searchTypeIndex)
    {
        String sqlClause = getSqlJoinClause(ctx, searchTypeIndex);
  
        if (sqlClause != null && !sqlClause.isEmpty())
            return new com.redknee.app.crm.web.border.search.SimpleXStatementTruePredicate(sqlClause);
        return null;
    }

	   
	public int getSearchTypeIndex()
	{
		return searchTypeIndex_;
	}

	/**
	 * @param fieldName
	 * @param checkAccountTable
	 */
	protected int searchTypeIndex_;
	

	protected SQLJoinCreator join_;
	
}
