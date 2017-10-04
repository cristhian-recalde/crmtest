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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author nitesh.jindal
 * @since 10.1.7
 */
public class SubscriberServiceCountUpdateHome extends HomeProxy
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SubscriberServiceCountUpdateHome(Context ctx, Home delegate)
	{
		super(ctx, delegate);
	}

	@Override
	public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		/* In memory subscriber services is retrieved from the subscriber. So it holds current changes to subscriber services fields. */
		final SubscriberServices subServInMemory = (SubscriberServices) obj;
		final SubscriberServices subServInDb = SubscriberServicesSupport.getSubscriberServiceRecord(
				ctx, subServInMemory.getSubscriberId(), subServInMemory.getServiceId(), subServInMemory.getPath());
		Subscriber sub=SubscriberSupport.getSubscriber(ctx, subServInMemory.getSubscriberId());
		if(sub == null)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, MODULE, "UNABLE TO FIND SUBSCRIBER WITH ID:"+subServInMemory.getSubscriberId()+" ....SKIPPING CHARGING DUE TO SERVICE QUANTITY CHANGE");
			}
			return super.store(ctx, obj);
		}
		
		BooleanHolder callFromApiHolder = (BooleanHolder) ctx.get(SubscribersApiSupport.IS_SERVICE_QUANTITY_CHANGED, new BooleanHolder(false));
        boolean callFromApi = callFromApiHolder.isBooleanValue();
        ctx.put(SubscribersApiSupport.Quantity_Support_For_Full_Charge, new BooleanHolder(true));
		//This is to check is the call is coming from the API
		if(callFromApi)
			return super.store(ctx, obj);
		
		if(subServInDb.getServiceQuantity() > subServInMemory.getServiceQuantity())
    	{
    		SubscribersApiSupport.handleRefundOnQuntityDecrease(ctx, sub, subServInMemory);            		
    	}
    	else if(subServInDb.getServiceQuantity() < subServInMemory.getServiceQuantity())
    	{
    		SubscribersApiSupport.handleChargeWithIncreasedQuantity(ctx, sub, subServInMemory);
    	}
		
		return super.store(ctx, obj);
	}
	/**
	 * 
	 */
	public final Object MODULE=SubscriberServiceCountUpdateHome.class.getName();

}