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
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTransientHome;
import com.trilogy.app.crm.bean.SearchTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
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
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Context agent that searches for an account or subscriber by MSISDN
 * 
 * @author amedina
 *
 */

public class AccountSubscriberSearchByMSISDNAgent extends AbstractAccountSubscriberSearch 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3013354042910231986L;

	public AccountSubscriberSearchByMSISDNAgent() 
	{
		super();
	}

	public AccountSubscriberSearchByMSISDNAgent(ContextAgent arg0) 
	{
		super(arg0);
	}

	public void execute(final Context ctx)
	throws AgentException
	{
		ConvergedAccountSubscriberSearch criteria  = (ConvergedAccountSubscriberSearch) SearchBorder.getCriteria(ctx);
		String msisdn= criteria.getMSISDN();
		SearchTypeEnum searchType=criteria.getType();
		
		
		if ( ! "".equals(msisdn) )
		{
			Home       subHome = (Home)       ctx.get(SubscriberHome.class);
			Home  conAcctSub=null;
		
			criteria.setDoSearch(true);
			try
			{
				Collection  subCol     = subHome.select(new EQ(SubscriberXInfo.MSISDN, msisdn));
				
				
				if ( subCol != null && !subCol.isEmpty())
				{
					for(Iterator it = subCol.iterator(); it.hasNext();)
					{
						Subscriber sub = (Subscriber)it.next();
						
						Account acct= AccountSupport.getAccount(ctx,sub.getBAN());
						if(searchType.getIndex() == SearchTypeEnum.Both_INDEX)
						{
							conAcctSub = ConvergedAccountSubscriberSupport.mergeAcctSubHome(ctx,acct,sub,conAcctSub,searchType.getIndex());
						}	
					}
					
				}
			}
			catch (HomeException e)
			{
				new MajorLogMsg(
						this,
						"Problem when searching account by msisdn, msisdn = " + msisdn +".",
						e).log(ctx);
			}
			catch (NullPointerException e)
			{
				new MajorLogMsg(
						this,
						"Problem when searching account by msisdn, msisdn = " + msisdn +".",
						e).log(ctx);
			}
			
	    	 if( conAcctSub != null)
	    		 ctx.put(ConvergedAccountSubscriberHome.class, conAcctSub);
	    	 else
	    		 ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
		}
		delegate(ctx);
	}

	@Override
	protected String getField()
	{
		return SubscriberXInfo.MSISDN.getSQLName();
	}

	@Override
	protected String getCriteria(ConvergedAccountSubscriberSearch criteria) 
	{
		return criteria.getMSISDN();
	}

}
