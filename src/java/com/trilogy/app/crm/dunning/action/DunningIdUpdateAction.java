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
package com.trilogy.app.crm.dunning.action;

import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.core.bean.ifc.AccountIfc;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.actions.ActionOutputImpl;
import com.trilogy.app.crm.core.ruleengine.actions.ifc.ActionOutputIfc;
import com.trilogy.app.crm.core.ruleengine.actions.param.ActionParameter;
import com.trilogy.app.crm.core.ruleengine.counters.CounterData;
import com.trilogy.app.crm.core.ruleengine.oam.ActionEventRecord;


public class DunningIdUpdateAction extends AbstractDunningIdUpdateAction  
{
	private static final long serialVersionUID = 1L;

	public String getName()
	{
		return NAME;
	}
    
	public ActionOutputIfc execute(Context ctx,
								   AccountIfc accountifc,
								   SubscriberIfc subscriber,
								   List<SubscriberIfc> subscriberList,
								   CounterData counterData,
								   ActionParameter[] actionParameters)
	{
		ActionOutputImpl actionOutput = new ActionOutputImpl();
		String ban = "EMPTY";
		int spid = -1;
		try
		{
			Account account = (Account)accountifc;
			ban = account.getBAN();
			spid = account.getSpid();
			
			if(account.getDunningPolicyId() != getDunningPolicy())
			{
				actionOutput.setExecuted();
				Home accountHome = (Home)ctx.get(AccountHome.class);
				account.setDunningPolicyId(getDunningPolicy());
				accountHome.store(account);
				actionOutput.setResultCode(ActionOutputIfc.RESULT_CODE_PASS);
			}
		}
		catch(Exception e)
		{
			actionOutput.setResultCode(ActionOutputIfc.RESULT_CODE_FAIL);
			LogSupport.info(ctx, this, "Failed to execute Dunning Id for Account:" + ban);
		}
		finally
		{
			ActionEventRecord.logActionEvent(ctx, spid, ban, getName(), actionOutput.isExecuted(), getCommonConfig().getDescription());
		}
		
		return actionOutput;
	}
	
	public static String NAME = "Account DunningId Update";
}
