/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.state.StateChangeException;
import com.trilogy.app.crm.support.Lookup;

/**
 * Ensures that a subscription state transition to another state is valid
 * @since 14th October 2013
 * @author nitin.agrawal@redknee.com
 */
public enum SubscriberStateValidator implements Validator
{

	/**
	 * Singleton instance.
	 */ 
	instance;

	/**
	 * {@inheritDoc}
	 */
	public void validate(final Context ctx, final Object obj)
	{
		if (!HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
		{
			// validating only store() operations
			return;
		}
		final Subscriber sub = (Subscriber) obj;
		final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		final RethrowExceptionListener exceptions = new RethrowExceptionListener();

//		Fix for TT#13102148036 & TT#12030619014. Below condition was modified to allow the subscriber state change from 'Pending to 'Available' & not to allow state change from not Available
//		to Available & not to allow from Inactive to any other state.
		if(((!SubscriberStateEnum.AVAILABLE.equals(oldSub.getState()) && !SubscriberStateEnum.PENDING.equals(oldSub.getState())) && SubscriberStateEnum.AVAILABLE.equals(sub.getState())) || 
				(SubscriberStateEnum.INACTIVE.equals(oldSub.getState()) && !SubscriberStateEnum.INACTIVE.equals(sub.getState()))){
			StateChangeException sce = new StateChangeException("State of subscriber cannot be changed from " + oldSub.getState().toString() + " to " + sub.getState().toString());
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "State of subscriber cannot be changed from " + oldSub.getState().toString() + " to " + sub.getState().toString(), null).log(ctx);
			}
			exceptions.thrown(sce);
		}
		exceptions.throwAllAsCompoundException();
	}
}