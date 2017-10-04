/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 * 
 * @Filename : AccountGroupMsisdnCopyMoveStrategy.java
 * @Author   : Unmesh Sonawane
 * @Date     : Jan 29, 2014
 * 
 */

package com.trilogy.app.crm.move.processor.account.strategy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class AccountGroupMsisdnCopyMoveStrategy
{
	
		/*
		 * This method is use to update the Group Id in CPS when Prepaid group Account is move to group pooled account
		 */
		public static void updateGroupIDInBMGT(Context ctx, Account parentAccount, Account originalAccount)
		{
			
			Subscriber originSub=null;
			SubscriberProfileProvisionClient bmClient_ = null;
			
			try {
				
			originSub = originalAccount.getIndividualSubscriber(ctx);
				 
			if(originSub==null)
			{
				LogSupport.info(ctx, AccountGroupMsisdnCopyMoveStrategy.class.getName(), "Account is null");
				return;
			}
			
			String newGroupID = parentAccount.getBAN();
			
			if(newGroupID==null)
			{
				LogSupport.info(ctx, AccountGroupMsisdnCopyMoveStrategy.class.getName(), "Parent account BAN is null");
				return;
			}
			
			bmClient_  = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
						
			bmClient_.updatePooledGroupID(ctx, originalAccount.getSubscriber() , newGroupID, false);

			}catch(SubscriberProfileProvisionException e)
			{
				new MinorLogMsg(AccountGroupMsisdnCopyMoveStrategy.class.getName(), e);
			}catch(HomeException e)
			{
				new MinorLogMsg(AccountGroupMsisdnCopyMoveStrategy.class.getName(), e);
			}
			
		}
	    

}
