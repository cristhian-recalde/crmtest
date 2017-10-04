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

import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Creates the SQL for exact fields so far:
 * IMSI
 * Package
 * MSISDN
 * @author amedina
 *
 */
public abstract class AbstractAccountSubscriberSearch extends ContextAgentProxy
		implements SQLJoinCreator 
{

	/**
	 * Default constructor
	 *
	 */
	public AbstractAccountSubscriberSearch() 
	{
		super();
	}

	/**
	 * Constructor that accepts a delegate
	 * @param delegate
	 */
	public AbstractAccountSubscriberSearch(ContextAgent delegate) 
	{
		super(delegate);
	}
	
	/**
	 * Build the SQL Join statement depending on the criteria and field the sub classes contins
	 */
	public String getSqlJoinClause(Context ctx, int searchTypeIndex) 
	{
		ConvergedAccountSubscriberSearch criteria  = (ConvergedAccountSubscriberSearch) SearchBorder.getCriteria(ctx);
		
		StringBuilder sqlClause = new StringBuilder();

		if (getCriteria(criteria) != null && getCriteria(criteria).trim().length() > 0)
		{
			if (searchTypeIndex == 1)
			{
				sqlClause.append(getField()).append(" = '").append(getCriteria(criteria)).append("'");
			}
			else
			{
				if (searchTypeIndex == 0 || searchTypeIndex == 2)
				{
			    	final String subscriberTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
			    			SubscriberHome.class,
							SubscriberXInfo.DEFAULT_TABLE_NAME);
	
			    	sqlClause.append("BAN in ( select BAN from ");
		    		sqlClause.append(subscriberTableName);
		    		sqlClause.append(" where ");
		    		sqlClause.append(getField());
		    		sqlClause.append(" = '");
		    		sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(getCriteria(criteria)));
			    	sqlClause.append("' )");
			    	
		    		if (searchTypeIndex==2)
		    		{
				    	sqlClause.append(" and (");
			    		sqlClause.append(getField());
			    		sqlClause.append(" = '");
			    		sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(getCriteria(criteria)));
		    	    	sqlClause.append("' )");
		    		}

				}
			}
		}
		
	    if(LogSupport.isDebugEnabled(ctx))
		{
	    	LogSupport.debug(ctx, this,"Converged Search SQL = " + sqlClause.toString());
		}
	    
	    return sqlClause.toString();
	}

	protected abstract String getField();
	protected abstract String getCriteria(ConvergedAccountSubscriberSearch criteria);


}
