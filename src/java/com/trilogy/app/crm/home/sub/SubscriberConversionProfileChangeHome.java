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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bas.tps.pipe.SubscriberCreditLimitUpdateAgent;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Handles aditional logic for sync other subscriber bean fields, 
 * due to conversion.
 * 
 * @author jchen
 */
public class SubscriberConversionProfileChangeHome extends HomeProxy
{
	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberConversionProfileChangeHome(Home delegate) 
	{
		super(delegate);	
	}
	
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object store(Context ctx, Object obj) throws HomeException,
			HomeInternalException 
	{
		Subscriber newSub = (Subscriber)obj;
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		
		//do conversion
		/*if (!oldSub.getSubscriberType().equals(newSub.getSubscriberType()))
		{
			onConversion(ctx, oldSub, newSub);
		}*/
			
		return super.store(ctx, obj);
	}

	/**
	 * @param oldSub
	 * @param newSub
	 */
	private void onConversion(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		//we need to reset CLCT flag, since prepaid does not check it.
		//TODO, where is the best place to put these two lines
		//since this flag affects services need to provisioned
		oldSub.setAboveCreditLimit(true);
		newSub.setAboveCreditLimit(true);
	}
}
