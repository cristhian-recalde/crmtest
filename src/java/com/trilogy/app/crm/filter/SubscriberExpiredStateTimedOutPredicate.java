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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.util.Date;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;


/**
 * A Predicate used to find Subscribers (in Expired state) whose expiryTimers
 * match that of its service provider.
 *
 * @author jimmy.ng@redknee.com
 */
public class SubscriberExpiredStateTimedOutPredicate
	implements Predicate
{
    /**
     * Creates a new predicate with the given criteria.
     *
     * @param serviceProvider The service provider to compare with.
     */ 
	public SubscriberExpiredStateTimedOutPredicate(final CRMSpid serviceProvider, final Date expirationDate)
	{
        serviceProvider_ = serviceProvider;
        date_ = expirationDate;
	}

    
    /**
     * INHERIT
     */
	public boolean f(Context _ctx,final Object obj)
	{
		final Subscriber subscriber = (Subscriber) obj;
        
        if (subscriber.getState() == SubscriberStateEnum.EXPIRED &&
        	subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID &&
            subscriber.getSpid() == serviceProvider_.getId() &&
			subscriber.getDeactivationDate().getTime() <= date_.getTime() )
       {
            return true;
        }
        
		return false;
	}


    /**
     * The servie provider to compare with.
     */
    protected final CRMSpid serviceProvider_;
    protected final Date date_;
}
