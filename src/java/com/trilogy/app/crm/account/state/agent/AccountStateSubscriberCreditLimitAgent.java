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
package com.trilogy.app.crm.account.state.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningAccountProcessor;
import com.trilogy.app.crm.home.sub.UserAdjustmentLimitValidator;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author jchen
 */
public class AccountStateSubscriberCreditLimitAgent extends AccountStateAgentHome
{


	/**
	 * @param ctx
	 * @param delegate
	 */
	public AccountStateSubscriberCreditLimitAgent(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
    public void onStateChange(Context ctx, Account oldAccount, Account newAccount) throws HomeException
	{

		Collection allSub = getApplySubscribers(ctx, oldAccount);		
		
		if (EnumStateSupportHelper.get(ctx).isTransition(oldAccount, newAccount, 
		        AccountStateEnum.PROMISE_TO_PAY, 
		        Arrays.asList(
		                AccountStateEnum.ACTIVE,
		                AccountStateEnum.NON_PAYMENT_SUSPENDED,
		                AccountStateEnum.NON_PAYMENT_WARN,
		                AccountStateEnum.IN_ARREARS,
		                AccountStateEnum.IN_COLLECTION)))
		{
            // modify CL of all the subs ( current CL minus CLA from SP )
		    Context subCtx = ctx.createSubContext();
		    subCtx.put(UserAdjustmentLimitValidator.SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION, Boolean.TRUE);
			modifySubscriberCL(subCtx, newAccount, allSub , true);
			AccountStateDunningAgent.requestDunning(ctx, this);
		}
		else if (EnumStateSupportHelper.get(ctx).isEnteringState(oldAccount, newAccount, AccountStateEnum.PROMISE_TO_PAY))
		{
            Context subCtx = ctx.createSubContext();
            subCtx.put(UserAdjustmentLimitValidator.SKIP_USER_ADJUSTMENT_LIMIT_VALIDATION, Boolean.TRUE);
            modifySubscriberCL(subCtx,newAccount, allSub, false);
		}
	}

	protected Collection getApplySubscribers(Context ctx, Account oldAccount) throws HomeException
	{
		Collection subs = null;
		try
		{
			subs = AbstractDunningAccountProcessor.getDunnableSubscribers(ctx, oldAccount);
		}
		catch(Exception exp)
		{
			throw new HomeException("Failed to change credit for subscribers", exp);
		}
		return subs;
	}
	

	private void modifySubscriberCL(Context ctx,Account newAccount, Collection allSub, boolean minus)
	{
		Iterator i = null;
		try
		{
			for (i = allSub.iterator(); i.hasNext();)
			{
				Home spHome = (Home) ctx.get(CRMSpidHome.class);
				Home subHome = (Home) ctx.get(SubscriberHome.class);
				CRMSpid sp = (CRMSpid) spHome.find(ctx, Integer.valueOf(newAccount.getSpid()));
				long clAdjustment = sp.getCreditLimitAdjustmentAmount();
				
				Subscriber subscriber = (Subscriber) i.next();
				// TODO: check if less thant 0
				if (minus) // state from PTP --> Dunned/Warned/Active
				{
					subscriber.setCreditLimit(subscriber.getCreditLimit(ctx) - clAdjustment);
				}
				else
				// state from any state ---> PTP
				{
					if (LogSupport.isDebugEnabled(ctx))
					{
						new DebugLogMsg(this, " 1. Going to change subscriber [BAN=" + subscriber.getBAN()
							+ "] credit limit from " + subscriber.getCreditLimit(ctx) + " to "
							+ subscriber.getCreditLimit(ctx) + clAdjustment + " incrementing: "
							+ clAdjustment, null).log(ctx);
					}

					subscriber.setCreditLimit(subscriber.getCreditLimit(ctx) + clAdjustment);
				}

				try
				{
					subHome.store(ctx,subscriber);
				}
				catch (HomeException e)
				{
					new MinorLogMsg(this, "Fail to update the credit limit of subscriber [BAN=" + subscriber.getBAN()
						+ "]", e).log(ctx);
				}
			}
		}
		catch (HomeException e)
		{
			new MinorLogMsg(this, "Fail to update the credit limit of subscribers under account  [BAN="
				+ newAccount.getBAN() + "]", e).log(ctx);
		}
	}
	


}
