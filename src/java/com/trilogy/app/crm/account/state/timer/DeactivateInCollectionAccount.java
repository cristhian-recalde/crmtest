package com.trilogy.app.crm.account.state.timer;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.bean.CreditCategoryXInfo;
import com.trilogy.app.crm.bean.DunningConfigurationEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.home.account.AccountChildrenInactiveValidator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MultiDbSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.visitor.ListBuildingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.XDB;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XResultSet;
import com.trilogy.framework.xhome.xdb.XStatement;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**

 * @author jchen
 *
 * Query database, check and inactivate accounts in collection more than grace
 * days defined in spid 
 */
public class DeactivateInCollectionAccount
   implements ContextAgent
{

    final static long DAY_IN_MILLS = 24*60*60*1000L;

   public DeactivateInCollectionAccount()
   {
   	
   }
   public void execute(Context ctx)
      throws AgentException
   {
        if (LogSupport.isDebugEnabled(ctx))
        {
           new DebugLogMsg(this, "Deactivate IN_COLLECTION Accounts cron task initiated.",null).log(ctx);
        }
        Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
           throw new AgentException("System error: CRMSpidHome not found in context");
        }
        try
        {  
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
            	final CRMSpid spid = (CRMSpid)iter.next();
                if (LogSupport.isDebugEnabled(ctx))
                 {
                    new DebugLogMsg(this, "Expired in Collection, processing spid ["+spid.getSpid()+"]", null).log(ctx);
                 }

                 // search for all the accounts with a inCollectionDate older than
                 // date_ - in_collection_grace_days and still in the IN_COLLECTION state
                 Home accountHome = (Home)ctx.get(AccountHome.class);
                 
                 Home creditCategoryHome = (Home) ctx.get(CreditCategoryHome.class);                 

                 try
                 {
                    Collection creditCategories = creditCategoryHome.where(ctx,
                            new EQ(CreditCategoryXInfo.SPID, Integer.valueOf(spid.getId()))).selectAll();
                     
                     for(Object ccObj: creditCategories)
                    {
                        CreditCategory creditCategory = (CreditCategory) ccObj;
                        final int inCollectionGraceDays = creditCategory.getDunningConfiguration() == DunningConfigurationEnum.CUSTOM
                                ? creditCategory.getGraceDaysDeactivate()
                                : spid.getGraceDayDeactivate();
                        long expiryDate = new Date().getTime() - inCollectionGraceDays*DAY_IN_MILLS;
                        //fetch BANs to be deactivated for exceeding grace days in in_collection state.
                        List<String> banList = getApplicableBanList(ctx, spid.getId(), creditCategory.getCode(), expiryDate);
                        if(banList != null && banList.size() > 0)
                        {
                        	Visitor visitor = new CloneingVisitor(new DeactivateInCollectionAccountVisitor(accountHome, inCollectionGraceDays));
                        	for(String ban : banList)
                        	{
                        		Account account = AccountSupport.getAccount(ctx, ban);
                        		if(account != null)
                        		{
                        			visitor.visit(ctx, account);
                        		}
                        		else
                        		{
                        			new MajorLogMsg(this, "Data integrity issue because Account corressponding to the BAN["+ ban + "] does not exist").log(ctx);
                        		}
                        		account = null;
                        	}
                        }
                    }
                 }
                 catch (HomeException hEx)
                 {
                	 new MajorLogMsg(this, "fail to find accounts that have been in collection over, spid : " + spid.getId()
                             , hEx).log(ctx);
                 }
            }
            
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        } 
    }
   
   private List<String> getApplicableBanList(Context ctx, int spid, int creditCategoryCode, long inCollectionExpiryDate)
   throws HomeException
   {
	   final XDB xdb = (XDB) ctx.get(XDB.class);
	   
	   DeactivateInCollectionAccountXStatement xStatement = new DeactivateInCollectionAccountXStatement(spid, 
			   creditCategoryCode, inCollectionExpiryDate);
	   ListBuildingVisitor visitor = new ListBuildingVisitor()
		   {
			   @Override
			   public void visit(Context ctx, Object bean) throws AgentException,
					AbortVisitException 
				{
				   try
				   {
					   super.visit(ctx, ((XResultSet)bean).getString(AccountXInfo.BAN.getSQLName()));
				   }
				   catch(SQLException se)
					{
						new MajorLogMsg(this, "Exception while working on result set of DeactivateInCollectionAccountXStatement", se).log(ctx);
					}
				}
		   };
	   xdb.forEach(ctx, visitor, xStatement);
	   
	   return visitor;
   }
   
   private class DeactivateInCollectionAccountXStatement implements XStatement
   {
	   private int spid_;
	   private int creditCategoryCode_;
	   private long inCollectionExpiryDate_;
	   
	   private DeactivateInCollectionAccountXStatement(int spid, int creditCategoryCode, long inCollectionExpiryDate)
	   {
		   spid_ = spid;
		   creditCategoryCode_ = creditCategoryCode;
		   inCollectionExpiryDate_ = inCollectionExpiryDate;
	   }
	   
	   @Override
	   public String createStatement(Context context) 
	   {
		   final StringBuilder sqlStatement = new StringBuilder();

	        sqlStatement.append("SELECT ");
	        sqlStatement.append(AccountXInfo.BAN.getSQLName());
	        sqlStatement.append(" FROM ");
	        final String accountTableName = MultiDbSupportHelper.get(context).getTableName(context, AccountHome.class,
	                AccountXInfo.DEFAULT_TABLE_NAME);
	        sqlStatement.append(accountTableName);
	        sqlStatement.append(" WHERE " + AccountXInfo.SPID.getSQLName() + " = ? AND ");
	        sqlStatement.append(AccountXInfo.STATE.getSQLName() + " = ? AND ");
	        sqlStatement.append(AccountXInfo.CREDIT_CATEGORY.getSQLName() + " = ? AND ");
	        sqlStatement.append(AccountXInfo.IN_COLLECTION_DATE.getSQLName() + " <= ?");
			
	        return sqlStatement.toString();
	   }
	
		@Override
		public void set(Context context, XPreparedStatement xpreparedstatement)
				throws SQLException 
		{
			xpreparedstatement.setInt(spid_);
			xpreparedstatement.setShort(AccountStateEnum.IN_COLLECTION_INDEX);
			xpreparedstatement.setInt(creditCategoryCode_);
			xpreparedstatement.setLong(inCollectionExpiryDate_);
		}
	   
   }
   
   private class DeactivateInCollectionAccountVisitor extends HomeVisitor
   {
	   private DeactivateInCollectionAccountVisitor(Home accountHome, int inCollectionGraceDays)
	   {
		   super(accountHome);
		   inCollectionGraceDays_ = inCollectionGraceDays;
	   }
	   
	   public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
       {
          Context subCtx = ctx.createSubContext();
          subCtx.put(AccountChildrenInactiveValidator.BYPASS_DEACTIVE_ACCOUNT_SUBSCRIBER_STATE_VALIDATION, Boolean.TRUE);
          Account acct = (Account)obj;
          
          /**
           * TT#13011420022 . Putting boolean in Context which is used
           * in RecalculateTotalNoOfSubForAccountHome
           */
          if(!acct.isPrepaid())
          {
              if(acct.getGroupType().getIndex() ==  GroupTypeEnum.GROUP_INDEX || acct.getGroupType().getIndex() ==  GroupTypeEnum.GROUP_POOLED_INDEX)
              {
                  subCtx.put(AccountConstants.DEACTIVATE_ACCOUNT_CRON_AGENT+acct.getBAN(), Boolean.TRUE);
              }
          }
          
          acct.setInCollectionDate(null);
          acct.setState(AccountStateEnum.INACTIVE);
          if (LogSupport.isDebugEnabled(subCtx))
          {
             new DebugLogMsg(this, "Deactivating account["+acct.getBAN()+"] for being in collection for over "+ inCollectionGraceDays_ +" days", null).log(subCtx);
          }
          try
          {
              getHome().store(subCtx,acct);
          }
          catch (HomeException innerHEx)
          {
             new MinorLogMsg(this, "fail to deactivate expired in collection account "+acct.getBAN(), innerHEx).log(ctx);
          }
          
       }
	   
	   private int inCollectionGraceDays_ = 0;
   }
}
