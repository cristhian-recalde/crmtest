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
 */

package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.LastModifiedAware;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * Updates Subscriber SuspensionReason, reset to blank if there is state change
 * from Suspended to Active.
 * 
 * @author vikash.kumar@redknee.com
 *
 */
public class SubscriberSuspensionReasonUpdateHome extends HomeProxy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SubscriberSuspensionReasonUpdateHome(Home delegate) {
		setDelegate(delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException {
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
		}
		return getDelegate().create(ctx, obj);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException {
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
		}
		setSuspensionReason(ctx, obj);
		return getDelegate().store(ctx, obj);
	}

	private void setSuspensionReason(Context ctx, Object obj) {
		final Subscriber newSub = (Subscriber) obj;
		final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

		if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, newSub, SubscriberStateEnum.ACTIVE)) {
			// Removing SuspensionReason which was set during subscriber
			// suspension flow.
			newSub.setSuspensionReason("");
		}
	}
}
