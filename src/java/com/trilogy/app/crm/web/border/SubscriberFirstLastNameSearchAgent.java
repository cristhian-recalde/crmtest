package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author pkulkarni
 *
 * On the similar lines of AccountFirstLastNameSearchAgent
 * 	
 * Filters the Subscriber Search by First and last name 
 */

public abstract class SubscriberFirstLastNameSearchAgent
extends	AccountSubscriberJoinSearchAgent
{

	public SubscriberFirstLastNameSearchAgent(String fieldName, boolean checkAccountTable)
	{
		super(fieldName, checkAccountTable);
	}

	public XStatement getSqlJoinClause(Context ctx, boolean bCheckAccountTable, String value,  boolean isWildCardSearch)
	{
	    StringBuilder sqlClause = new StringBuilder(); 

    	final String subscriberTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
    			SubscriberHome.class,
				SubscriberXInfo.DEFAULT_TABLE_NAME);
    	
    	sqlClause.append("id in ( select id from ");
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
