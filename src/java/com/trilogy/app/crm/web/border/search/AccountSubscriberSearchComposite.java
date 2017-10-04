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
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.ContactTypeEnum;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberXInfo;
import com.trilogy.app.crm.bean.account.ContactHome;
import com.trilogy.app.crm.bean.account.ContactXDBHome;
import com.trilogy.app.crm.bean.account.ContactXInfo;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.web.border.ConvergedAccountFirstLastNameSearchAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Composite that collects all the SQL statement to get the required filter
 * 
 * @author amedina
 *
 */

public class AccountSubscriberSearchComposite implements SQLJoinCreator
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9220217202311048489L;

	public AccountSubscriberSearchComposite(boolean searchSubAccounts) 
	{
		super();
		intializeArray(searchSubAccounts);
	}

	/**
	 * Initializes the array of SQLJoinCreators
	 *
	 */
	@SuppressWarnings("unchecked")
    private void intializeArray(boolean searchSubAccounts) 
	{
		agents_= new ArrayList();
		
		agents_.add(new AccountSubscriberSearchByBANAgent());
		if(searchSubAccounts)
		{
	        agents_.add(new AccountSubscriberSearchByParentBANAgent());
		}
		agents_.add(new AccountSubscriberSearchByMSISDNAgent());
		agents_.add(new AccountSubscriberSearchByIMSI());
		agents_.add(new AccountSubscriberSearchByPackage());

		agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.FIRST_NAME.getName(), true)
	            {
		            @Override
                    public String getCriteria(Context ctx, Object criteria)
		            {
		               return ((ConvergedAccountSubscriberSearch) criteria).getFirstName().trim();
		            }
		           
		            @Override
                    public String getField(Object bean)
		            {
		               return ((ConvergedAccountSubscriber) bean).getFirstName();
		            }

		         });
		agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.LAST_NAME.getName(), true)
	            {
		            @Override
                    public String getCriteria(Context ctx, Object criteria)
		            {
		               return ((ConvergedAccountSubscriberSearch) criteria).getLastName().trim();
		            }
		           
		            @Override
                    public String getField(Object bean)
		            {
		               return ((ConvergedAccountSubscriber) bean).getLastName();
		            }
	            });
        agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.ACCOUNT_MGR.getName(), true)
        {
            @Override
            public String getCriteria(Context ctx, Object criteria)
            {
               return ((ConvergedAccountSubscriberSearch) criteria).getAccountMgr().trim();
            }
           
            @Override
            public String getField(Object bean)
            {
               return ((ConvergedAccountSubscriber) bean).getAccountMgr();
            }
        });
		agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.COMPANY_NAME.getName(), true)
	            {
		            @Override
                    public String getCriteria(Context ctx, Object criteria)
		            {
		               return ((ConvergedAccountSubscriberSearch) criteria).getCompanyName().trim();
		            }
		           
		            @Override
                    public String getField(Object bean)
		            {
		               return ((ConvergedAccountSubscriber) bean).getCompanyName();
		            }
		         });
		agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.ACCOUNT_NAME.getName(), true)
	            {
		            @Override
                    public String getCriteria(Context ctx, Object criteria)
		            {
		               return ((ConvergedAccountSubscriberSearch) criteria).getAccountName().trim();
		            }
		           
		            @Override
                    public String getField(Object bean)
		            {
		               return ((ConvergedAccountSubscriber) bean).getAccountName();
		            }
		         });
		agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ConvergedAccountSubscriberXInfo.BILLING_ADDRESS1.getName(), true)
	            {
		            @Override
                    public String getCriteria(Context ctx, Object criteria)
		            {
		               return ((ConvergedAccountSubscriberSearch) criteria).getBillingAddress1().trim();
		            }
		           
		            @Override
                    public String getField(Object bean)
		            {
		               return ((ConvergedAccountSubscriber) bean).getBillingAddress1();
		            }
		         });
		
        agents_.add(new ConvergedAccountFirstLastNameSearchAgent(ContactXInfo.EMAIL.getSQLName(), true)
        {
            @Override
            public String getCriteria(Context ctx, Object criteria)
            {
               return ((ConvergedAccountSubscriberSearch) criteria).getEmail().trim();
            }
           
            @Override
            public String getSqlJoinClause(Context ctx, boolean bCheckAccountTable, String value,  boolean isWildCardSearch, boolean isEscapeNeeded, int searchTypeIndex)
            {
                StringBuilder sqlClause = new StringBuilder(); 

                sqlClause.append(" BAN in ( select ACCOUNT from ");
                sqlClause.append(MultiDbSupportHelper.get(ctx).getTableName(ctx,
                        ContactHome.class,
                        ContactXInfo.DEFAULT_TABLE_NAME));
                sqlClause.append(" where ");
                sqlClause.append(getFieldName());
                if (isWildCardSearch)
                {
                    sqlClause.append(" like '");
                }
                else
                {
                    sqlClause.append(" = '");
                }
                sqlClause.append(((XQL) ctx.get(XQL.class)).escapeSQL(value.toUpperCase()));
                if (isEscapeNeeded)
                {
                    sqlClause.append("' escape '!");
                }
                sqlClause.append("' and ");
                sqlClause.append(ContactXInfo.TYPE.getSQLName());
                sqlClause.append(" = ");
                sqlClause.append(ContactTypeEnum.PERSON_INDEX);
                sqlClause.append(" )");
                
                

                if(LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this,"Converged Search SQL = " + sqlClause.toString());
                }
                
                return sqlClause.toString();
            }

            @Override
            public String getField(Object bean)
            {
                return null;
            }
         });
	}

	/**
	 * Collects all the SQL where clauses depending on the parameter required
	 */
	public String getSqlJoinClause(Context ctx, int searchTypeIndex) 
	{
		Iterator iter = agents_.iterator();
		StringBuilder sql = new StringBuilder();
		boolean firstTime = true;
		
		while (iter.hasNext())
		{
			SQLJoinCreator join = (SQLJoinCreator) iter.next();
			String newJoin = join.getSqlJoinClause(ctx, searchTypeIndex);
            if (!newJoin.equals(""))
            {
                if (firstTime)
                {
                    firstTime = false;
                }
                else
                {
                    sql.append(" and ");
                }
			    sql.append(newJoin);
			}
		}
		
		
		
		return sql.toString();
	}

	protected List agents_ ;

}
