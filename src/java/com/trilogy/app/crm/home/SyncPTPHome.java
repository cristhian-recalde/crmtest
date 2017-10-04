/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXDBHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryHome;
import com.trilogy.app.crm.dunning.DunningConfigTypeEnum;
import com.trilogy.app.crm.dunning.DunningPolicy;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.DunningPolicySupport;
import com.trilogy.app.crm.support.SpidSupport;
/*
 * Purpose: When u reduce the current no of MAX PTP transaction. We should update all the
 * accounts which uses this category. We should set the ptptermstightened of the account
 * to true where the current ptp transaction is more than the new max PTP transactions
 */
public class SyncPTPHome extends HomeProxy
{

    public SyncPTPHome(Context ctx, Home home)
    {
        super(home);
        setContext(ctx);
    }


    @Override
    public Object store(Context ctx, Object obj) throws HomeInternalException, HomeException
    {
    	DunningPolicy dunningPolicy = (DunningPolicy) obj; 
    	DunningPolicy oldDunningPolicy = (DunningPolicy)this.find(dunningPolicy.getDunningPolicyId());
    			
    	dunningPolicy = (DunningPolicy) super.store(ctx, obj);
        
    	// if the store was success
        DunningConfigTypeEnum dunningConfigType = dunningPolicy.getDunningConfig();
        int maxNumberPTP = dunningPolicy.getMaxNumOfPaymentPlans();
        int maxPTPInterval =  dunningPolicy.getPaymentPlanInterval();
        DunningConfigTypeEnum oldDunningConfigType = oldDunningPolicy.getDunningConfig();
    	if(oldDunningConfigType.getIndex() ==  DunningConfigTypeEnum.SPID_INDEX &&  dunningConfigType.getIndex() == DunningConfigTypeEnum.CUSTOM_INDEX)
    	{
    		Collection<CRMSpid> allSpids = SpidSupport.getAllSpid(ctx);
    		for(CRMSpid crmSpid: allSpids)
    		{
    			if(crmSpid.getMaxNumOfPaymentPlans() > maxNumberPTP)
    			{
    				processAccountsPerSpid(ctx, dunningPolicy.getDunningPolicyId(), crmSpid.getSpid() , maxNumberPTP, maxPTPInterval);
    			}
    		}
    	}
    	else if(dunningConfigType.getIndex() == DunningConfigTypeEnum.CUSTOM_INDEX)
    	{
    		if(oldDunningPolicy.getMaxNumOfPaymentPlans() > maxNumberPTP)
    		{
    			processAccounts(ctx, dunningPolicy.getDunningPolicyId(), maxNumberPTP, maxPTPInterval);
    			// update(ctx, cc);
    		}
    	}
        
        return dunningPolicy;
    }


    public String getTableName()
    {
        return AccountXDBHome.TABLE_NAME;
    }
    
    private void processAccounts(Context ctx, long dunningPolicyId, int maxNumberPTP, int maxPTPInterval) throws HomeException
    {
        // Get account BANs that needs to be processed.
    	java.util.Collection col = DunningPolicySupport.getBanForPtpTightening(ctx, dunningPolicyId, maxNumberPTP);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "ProcessAccounts() - Accounts: " + col, null).log(ctx);
        }
        
        Home home = (Home)ctx.get(AccountHome.class);
        Home ccHome = (Home) ctx.get(CreditCategoryHome.class);
        Account acc = null;
        for(Iterator i=col.iterator();i.hasNext();)
		{
            acc = (Account)home.find(ctx, i.next().toString());
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Processing Account: " + acc.getBAN(), null).log(ctx);
            }
            
            CreditCategory cc = (CreditCategory) ccHome.find(ctx, Integer.valueOf(acc.getCreditCategory()));
            
            if (cc == null)
            {
                new DebugLogMsg(this, "Credit category " + acc.getCreditCategory() + " not found", null).log(ctx);
                throw new HomeException("Could not find the credit category for credit category number "
                        + acc.getCreditCategory());
            }
            
            acc.setPtpTermsTightened(true);
            acc.setCurrentNumPTPTransitions(cc.getMaxNumberPTP());
            Account newAcc = (Account)home.store(ctx, acc);
	        if(newAcc != null && newAcc.getCurrentNumPTPTransitions() == cc.getMaxNumberPTP())
	        {
	            try
	            {
	                if (LogSupport.isDebugEnabled(ctx))
	                {
		                new DebugLogMsg(this, "ProcessAccounts() - generating ER 1107 - BAN: " + acc.getBAN(), null).log(ctx);
	                }

	                //generate ER 1107
	                ERLogger.genPTPResetER(ctx, acc, cc, acc.getState(), maxNumberPTP, maxPTPInterval);
	            }
	            catch(Exception e)
	            {
	                new MinorLogMsg(this, "Error generating ER 1107 - BAN: " + acc.getBAN(), e).log(ctx);
	            }                
	        }
		}
   
    }
    
    
    private void processAccountsPerSpid(Context ctx, long dunningPolicyId, int spid ,int maxNumberPTP, int maxPTPInterval) throws HomeException
    {
    	// Get account BANs that needs to be processed.
        java.util.Collection col = DunningPolicySupport.getBanPerSpidForPtpTightening(ctx,spid , dunningPolicyId, maxNumberPTP);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "ProcessAccounts() - Accounts: " + col, null).log(ctx);
        }
        
        Home home = (Home)ctx.get(AccountHome.class);
        Home ccHome = (Home) ctx.get(CreditCategoryHome.class);
        Account acc = null;
        for(Iterator i=col.iterator();i.hasNext();)
		{
            acc = (Account)home.find(ctx, i.next().toString());
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Processing Account: " + acc.getBAN(), null).log(ctx);
            }
            
            CreditCategory cc = (CreditCategory) ccHome.find(ctx, Integer.valueOf(acc.getCreditCategory()));
            
            if (cc == null)
            {
                new DebugLogMsg(this, "Credit category " + acc.getCreditCategory() + " not found", null).log(ctx);
                throw new HomeException("Could not find the credit category for credit category number "
                        + acc.getCreditCategory());
            }
            
            acc.setPtpTermsTightened(true);
            acc.setCurrentNumPTPTransitions(maxNumberPTP);
            Account newAcc = (Account)home.store(ctx, acc);
	        if(newAcc != null && newAcc.getCurrentNumPTPTransitions() == maxNumberPTP)
	        {
	            try
	            {
	                if (LogSupport.isDebugEnabled(ctx))
	                {
		                new DebugLogMsg(this, "ProcessAccounts() - generating ER 1107 - BAN: " + acc.getBAN(), null).log(ctx);
	                }

	                //generate ER 1107
	                ERLogger.genPTPResetER(ctx, acc, cc, acc.getState(), maxNumberPTP, maxPTPInterval);
	            }
	            catch(Exception e)
	            {
	                new MinorLogMsg(this, "Error generating ER 1107 - BAN: " + acc.getBAN(), e).log(ctx);
	            }                
	        }
		}
    }
    
    
    /*
     
     private void update(Context ctx, CreditCategory cc) throws HomeException
     {
       
        final String update_statement = "UPDATE Account" + " SET " + "ptptermstightened='y', currentnumptptransitions="
                + cc.getMaxNumberPTP() + " where currentnumptptransitions >" + cc.getMaxNumberPTP() + " AND creditcategory=" + cc.getCode();

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Credit Category: " + cc, null).log(ctx);
            new DebugLogMsg(this, "update_statement: " + update_statement, null).log(ctx);
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, update_statement, null).log(ctx);
        }
        XDB xdb = (XDB) ctx.get(XDB.class);
        xdb.execute(ctx, update_statement);
                
     } 
     */
}