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

import java.util.Collection;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTransientHome;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ConvergedAccountSubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Context agent that searches for account or subscriber by BAN
 * 
 * @author amedina
 *
 */

public class AccountSubscriberSearchByBANAgent extends ContextAgentProxy implements SQLJoinCreator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8943105409049151595L;

	public AccountSubscriberSearchByBANAgent() 
	{
		super();
	}
	

	public AccountSubscriberSearchByBANAgent(ContextAgent arg0) 
	{
		super(arg0);
	}

	public void execute(final Context ctx)
	throws AgentException
	{
		ConvergedAccountSubscriberSearch criteria  = (ConvergedAccountSubscriberSearch) SearchBorder.getCriteria(ctx);
		String ban= criteria.getBAN();
		SearchTypeEnum searchType=criteria.getType();
		Account acct = null;
		Subscriber sub=null;
		Home       acctHome = (Home)       ctx.get(AccountHome.class);
		Home  conAcctSub=null;
		
		try
		{
			acct=(Account)acctHome.find(new EQ(AccountXInfo.BAN,ban));
		}
		catch (HomeException e)
		{
		}
		catch (NullPointerException e)
		{
		}
		
		if (acct!=null&&(! "".equals(ban)))
		{
			criteria.setDoSearch(true);
			if(searchType.getIndex() == SearchTypeEnum.Account_INDEX)
			{
				conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,acct,sub,conAcctSub,searchType.getIndex()); 
			}
			if(searchType.getIndex() == SearchTypeEnum.Both_INDEX)
			{
				try
				{
					Collection subs = AccountSupport.getImmediateChildrenSubscribers(ctx, acct);
				
					conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,acct,subs,searchType.getIndex());
					
				} catch (HomeException e) {
					e.printStackTrace();
				}
			}
	    	 if( conAcctSub != null)
	    		 ctx.put(ConvergedAccountSubscriberHome.class, conAcctSub);
	    	 else
	    		 ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
			
		}
		
		delegate(ctx);
	}

	public String getSqlJoinClause(Context ctx, int searchTypeIndex) 
	{
		ConvergedAccountSubscriberSearch criteria  = (ConvergedAccountSubscriberSearch) SearchBorder.getCriteria(ctx);
		
		StringBuilder sqlClause = new StringBuilder();

		if (criteria.getBAN() != null && criteria.getBAN().trim().length()>0)
		{
			sqlClause.append("BAN = '").append(criteria.getBAN()).append("'"); 
		}
		
	    if(LogSupport.isDebugEnabled(ctx))
		{
	    	LogSupport.debug(ctx, this,"Converged Search SQL = " + sqlClause.toString());
		}
	    
	    return sqlClause.toString();
	}
}
