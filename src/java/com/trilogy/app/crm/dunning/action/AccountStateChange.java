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
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountSuspensionReasonEnum;
import com.trilogy.app.crm.home.TransactionRedirectionHome;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LRUCachingHome;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @since 10.2
 * @author shyamrag.charuvil@redknee.com
 */
public class AccountStateChange extends AbstractAccountStateChange{

	private static final long serialVersionUID = 1L;
	public static String MODULE = AccountStateChange.class.getName();

	public void execute(Context context) throws AgentException
	{
		LogSupport.info(context,MODULE, "In execute() method of Account State Change for Account Level action "
        		+ "of Dunning Policy Id= "+getAccount().getDunningPolicyId());
		
		Account account = getAccount();	
		
		if(getAccount().getState() == newState_)
		{
			LogSupport.info(context, MODULE, "The new state and current state for the Account ="+account.getBAN()+" is same. Not updating the state.");
			return;
		}
		else
		{
			account.setState(newState_);		
		}
		
        if(!(account.getState() == AccountStateEnum.IN_COLLECTION || account.getState() == AccountStateEnum.ACTIVE))
        {
        	LogSupport.info(context, MODULE, "Skipping Account Pipeline upto LRUCaching Home.");
        	Home accountHome = (Home) context.get(AccountHome.class);
        	if (accountHome == null)
        	{
        		throw new AgentException("Account home not found in context.");
        	}
        	
        	try
        	{
        		//accountHome = HomeSupportHelper.get(context).getHome(context, accountHome, LRUCachingHome.class);
            	if (accountHome == null)
            	{
            		throw new AgentException("LRUCachingHome for Account home pipeline not found in context.");
            	}
            	LogSupport.info(context, MODULE, "Updating Account State to "+account.getState().toString());
            	//if account is going to suspend state through dunning then set suspensionreason as unpaid
            	if(account.getState().equals(AccountStateEnum.SUSPENDED))
        		{
        			account.setSuspensionReason(AccountSuspensionReasonEnum.Unpaid_INDEX);
        		}
            	accountHome.store(context, account);
			} 
			catch (HomeException e)
			{
				 LogSupport.major(context, MODULE, "Error in updating Account state in XDB",e); 	
				 throw new AgentException(e);
			}

        }
        else
        {
        	LogSupport.info(context, MODULE, "Executing Account Pipeline for "+account.getState().toString());
        	
        	final Home accHome = (Home) context.get(AccountHome.class);		
        	if (accHome == null)
        	{
        		throw new AgentException("Account home not found in context.");
        	}

        	try
        	{
        		LogSupport.info(context, MODULE, "Updating Account State to "+account.getState().toString());
        		//if account is going to suspend state through dunning then set suspensionreason as unpaid
        		if(newState_.equals(AccountStateEnum.SUSPENDED))
        		{
        			account.setSuspensionReason(AccountSuspensionReasonEnum.Unpaid_INDEX);
        		}
        		//if account is suspended through dunning and after full payment it is going to active then set suspensionReason to default.
        		if(newState_.equals(AccountStateEnum.ACTIVE) && account.getSuspensionReason()== AccountSuspensionReasonEnum.Unpaid_INDEX)
        		{
        			short reason=-1;
        			account.setSuspensionReason(reason);
        		}
        		accHome.store(context, account);
        	} 
        	catch (HomeException e) 
        	{
        		LogSupport.major(context, MODULE, "Error in updating Account state.",e);
        		throw new AgentException(e);
        	} 
        	
        }	
        LogSupport.info(context, MODULE, "Successfully executed Account state change Action.");
	}
}