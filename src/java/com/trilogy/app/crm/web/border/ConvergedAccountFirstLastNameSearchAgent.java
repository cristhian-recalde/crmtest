/**
 * 
 */
package com.trilogy.app.crm.web.border;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberHome;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberSearch;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ConvergedAccountSubscriberSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.app.crm.web.border.search.SQLJoinCreator;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.xdb.XQL;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author sthakur
 *
 */
public abstract class ConvergedAccountFirstLastNameSearchAgent extends
		AccountFirstLastNameSearchAgent implements SQLJoinCreator
{

	/**
	 * @param fieldName
	 * @param checkAccountTable
	 */
	protected int searchTypeIndex_;
	
	public int getSearchTypeIndex()
	{
		return searchTypeIndex_;
	}
	
	
	public ConvergedAccountFirstLastNameSearchAgent(String fieldName,
			boolean checkAccountTable) 
	{
       super(fieldName, checkAccountTable);
	}
	   
	   public void execute(Context ctx)
	      throws AgentException
	   {
		  Home acctHome_ = (Home)ctx.get(AccountHome.class);
		  Home subHome_ = (Home)ctx.get(SubscriberHome.class);
		  
		  Home conAcctSub=null;
		  
		  if( acctHome_ == null)
			  throw new AgentException("Can not find accopunt home in context, can not continue");
		  
		  
		  boolean isWildCardEnabled = false;
		  boolean isEscapeEnabled = false;
		  
		  //Object criteria = SearchBorder.getCriteria(ctx);
		  ConvergedAccountSubscriberSearch criteria = (ConvergedAccountSubscriberSearch)SearchBorder.getCriteria(ctx);
		  searchTypeIndex_ = criteria.getType().getIndex();
	      Vector returnVect = getCleanCriteriaValue(ctx, criteria);
	      
	      String value = (String)returnVect.get(0);
	      Boolean wildCardObj = (Boolean)returnVect.get(1);      
	      if(wildCardObj != null)
	    	  isWildCardEnabled = wildCardObj.booleanValue();      
	      Boolean escapeObj = (Boolean)returnVect.get(2);      
	      if(escapeObj != null)
	          isEscapeEnabled = escapeObj.booleanValue();      
	      
	      if ( ! "".equals(value) )
	      {
	    	  try
	    	  {
	    		  //Map Account
	    		  if (getSearchTypeIndex() == 0 || getSearchTypeIndex() == 2)
	    		  {
	    			  
	    			  Collection acctBeanCollection = acctHome_.where(ctx, getSqlJoinClause(ctx, checkAccountTable_, value, isWildCardEnabled, isEscapeEnabled,0)).selectAll();
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
	    			  
	    		      Collection subBeanCollection = subHome_.where(ctx, getSqlJoinClause(ctx, checkAccountTable_, value, isWildCardEnabled,isEscapeEnabled, 1)).selectAll();
	    		      
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
		    			  Collection subBeanCollection = subHome_.where(ctx, getSqlJoinClause(ctx, checkAccountTable_, value, isWildCardEnabled, isEscapeEnabled, 1)).selectAll();
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
	    		 ctx.put(ConvergedAccountSubscriberHome.class, conAcctSub);
	    	 else
	    		 ctx.put(ConvergedAccountSubscriberHome.class, new ConvergedAccountSubscriberTransientHome(ctx));
	      }
	      delegate(ctx);
	   }
	   
	   

	   public String getSqlJoinClause(Context ctx, int searchTypeIndex)
	   {
			  boolean isWildCardEnabled = false;
			  boolean isEscapeEnabled = false;
			  
			  String sqlResult = "";
				
			  //Object criteria = SearchBorder.getCriteria(ctx);
			  ConvergedAccountSubscriberSearch criteria = (ConvergedAccountSubscriberSearch)SearchBorder.getCriteria(ctx);
			  searchTypeIndex_ = criteria.getType().getIndex();
		      Vector returnVect = getCleanCriteriaValue(ctx, criteria);
		      
		      String value = (String)returnVect.get(0);
		      Boolean wildCardObj = (Boolean)returnVect.get(1);      
		      if(wildCardObj != null)
		    	  isWildCardEnabled = wildCardObj.booleanValue();      
		      Boolean escapeObj = (Boolean)returnVect.get(2);      
		      if(escapeObj != null)
		          isEscapeEnabled = escapeObj.booleanValue();      
		      
		      if ( ! "".equals(value) )
		      {
		    	  sqlResult = getSqlJoinClause(ctx, checkAccountTable_, value, isWildCardEnabled, isEscapeEnabled, searchTypeIndex);
		      }
		      
		      LogSupport.debug(ctx, this, "sqlResult for " + this.getFieldName() + " is : " + sqlResult);
		      
		      return sqlResult;

	   }
	   
	public String getSqlJoinClause(Context ctx, boolean bCheckAccountTable, String value,  boolean isWildCardSearch, boolean isEscapeNeeded, int searchTypeIndex)
		{
		    StringBuilder sqlClause = new StringBuilder(); 

	    	final String subscriberTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
	    			SubscriberHome.class,
					SubscriberXInfo.DEFAULT_TABLE_NAME);
	    	
	    	final String accountTableName = MultiDbSupportHelper.get(ctx).getTableName(ctx,
	    			AccountHome.class,
					AccountXInfo.DEFAULT_TABLE_NAME);
	    	
	    		    	
	    	if(getFieldName().equalsIgnoreCase("UPPER(accountName)")||
                    getFieldName().equalsIgnoreCase("UPPER(accountMgr)") ||
                    getFieldName().equalsIgnoreCase("UPPER(billingAddress1)") ||
	    	        getFieldName().equalsIgnoreCase("UPPER(companyName)") || 
	    	        getFieldName().equalsIgnoreCase("UPPER(firstName)") ||
	    	        getFieldName().equalsIgnoreCase("UPPER(lastName)"))
	    	{
	    		searchTypeIndex=0;
	    	}

	    	if(searchTypeIndex==0 || searchTypeIndex==2)
	    	{
	    		sqlClause.append("BAN in ( select BAN from ");
	    		sqlClause.append(accountTableName);
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
	    	}
	    	
            if (searchTypeIndex==2)
	    	{
	    		sqlClause.append("' UNION select BAN from ");
	    	}
	    	
	    	else if(searchTypeIndex==1)
	    	{
	    		sqlClause.append("ID in ( select ID from ");
	    	}
	    	
	    	if(searchTypeIndex==2 || searchTypeIndex==1)
	    	{
	    		sqlClause.append(subscriberTableName);
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

	    		if (searchTypeIndex==2)
	    		{
	    	    	sqlClause.append("' )");
			    	sqlClause.append(" and (");
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
	    		}

	    	}
	        
	    	sqlClause.append("' )");
	    	
	    	

		    if(LogSupport.isDebugEnabled(ctx))
			{
		    	LogSupport.debug(ctx, this,"Converged Search SQL = " + sqlClause.toString());
			}
		    
		    return sqlClause.toString();
		}
	
}
