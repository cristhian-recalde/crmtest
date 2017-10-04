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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author amedina
 *
 * Filters the Account Search by First and last name
 * including Subscriber searches 
 */
public abstract class AccountFirstLastNameSearchAgent
extends	AccountSubscriberJoinSearchAgent 
{
	
	public AccountFirstLastNameSearchAgent(String fieldName, boolean checkAccountTable)
	{
		super(fieldName, checkAccountTable);
	}

	/**
	 * TODO refactor to use XStatement
	*/
	public XStatement getSqlJoinClause(Context ctx, boolean bCheckAccountTable, String value,  boolean isWildCardSearch)
	{
	    StringBuilder sqlClause = new StringBuilder(); 

    	final String subscriberTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
    			SubscriberHome.class,
				SubscriberXInfo.DEFAULT_TABLE_NAME);
    	
    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
    			AccountHome.class,
				AccountXInfo.DEFAULT_TABLE_NAME);
    	

	    
        sqlClause.append("BAN in ( select BAN from ");
        sqlClause.append(accountTableName);
        sqlClause.append(" where ");
        sqlClause.append(getFieldName());
        sqlClause.append(" like '");
        sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(value));
        if(isWildCardSearch)
        	sqlClause.append("%");
        sqlClause.append("' UNION select BAN from ");
        sqlClause.append(subscriberTableName);
        sqlClause.append(" where ");
        sqlClause.append(getFieldName());
        sqlClause.append(" like '");
        sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(value));
        if(isWildCardSearch)
        	sqlClause.append("%");
        sqlClause.append("' )"); 
	    
	    return new SimpleXStatement(sqlClause.toString());
	}

	

}
