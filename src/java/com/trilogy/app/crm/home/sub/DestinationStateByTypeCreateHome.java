/*
 * Created on May 16, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.*;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Depending on the subscriber type it sets the destination state on create. For postpaid that state is 
 * ACTIVATED, for prepaid it is AVAILABLE. 
 * 
 * This class also implements the logic for future activation.
 * 
 * @author psperneac
 */
public class DestinationStateByTypeCreateHome extends HomeProxy
{
	public DestinationStateByTypeCreateHome(Home delegate)
	{
		super(delegate);
	}

	/**
	 * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
	{
		Subscriber sub=(Subscriber) obj;
		
		if(sub.isPostpaid() || SystemSupport.supportsPrepaidCreationInActiveState(ctx))
		{
			if(sub.getStartDate()!=null && sub.getStartDate().getTime()>System.currentTimeMillis())
			{
				// future activation
				sub.setState(SubscriberStateEnum.PENDING);
			}
			else
			{
				sub.setState(SubscriberStateEnum.ACTIVE);
			}
		}
		//else if(sub.getSubscriberType().getIndex()==SubscriberTypeEnum.PREPAID_INDEX)
		else
		{
			sub.setState(SubscriberStateEnum.AVAILABLE);
		} 
		
		return super.create(ctx, obj);
	}
	
	
}
