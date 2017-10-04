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


package com.trilogy.app.crm.dunning.action;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.3
 * @author shyamrag.charuvil@redknee.com
 */

public class DebtCollectionAgencyAccountAction extends AbstractDebtCollectionAgencyAccountAction
{
	private static final long serialVersionUID = 1L;
	public static String MODULE = DebtCollectionAgencyAccountAction.class.getName();
	
	public void execute(Context context) throws AgentException
	{
		LogSupport.info(context,MODULE, "In execute() method of Debt Collection Agency for Account Level action "
        		+ "of Dunning Policy Id= "+getAccount().getDunningPolicyId());
		
		Account account = getAccount();		
		if(getAccount().getDebtCollectionAgencyId() == getDebtCollectionAgencyAccAction())
		{
			if(LogSupport.isDebugEnabled(context))
			{
				LogSupport.debug(context, MODULE, "The new agency and old agency for the Account ="+account.getBAN()+" is same. Not updating the state.");
				return;
			}
		}
		else
			account.setDebtCollectionAgencyId(getDebtCollectionAgencyAccAction());
		
    	Home accountHome = (Home) context.get(AccountHome.class);
    	if (accountHome == null)
    	{
    		throw new AgentException("Account home not found in context.");
    	}
    	
    	// No need to invoke complete pipeline of Account as no business logic is associated with Debt Collection Agency.
		if(LogSupport.isDebugEnabled(context))
		{
			LogSupport.debug(context, MODULE, "Skipping Account Pipeline upto LRUCaching Home.");
		}
    	
    	try
    	{
    		accountHome = HomeSupportHelper.get(context).getHome(context, accountHome, LRUCachingHome.class);
        	if (accountHome == null)
        	{
        		throw new AgentException("LRUCachingHome for Account home pipeline not found in context.");
        	}
        	LogSupport.info(context, MODULE, "Updating Account Collection Agency to "+account.getDebtCollectionAgencyId());
        	accountHome.store(context, account);
		} 
		catch (HomeException e)
		{
			 LogSupport.major(context, MODULE, "Error in updating Account state in XDB",e); 	
			 throw new AgentException(e);
		}
	}
}
