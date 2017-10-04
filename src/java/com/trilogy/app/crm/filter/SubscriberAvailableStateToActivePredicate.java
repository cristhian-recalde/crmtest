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
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import java.util.Date;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * @author amedina
 *
 * Filters the subscriber that has startDate and state to AVAILABLE
 */
public class SubscriberAvailableStateToActivePredicate implements Predicate
{

    /**
     * Creates a new predicate with the given criteria.
     *
     * @param serviceProvider The service provider to compare with.
     * @param expirationDate
     */ 
	public SubscriberAvailableStateToActivePredicate(final CRMSpid serviceProvider, final Date expirationDate)
	{
        serviceProvider_ = serviceProvider;
        date_ = expirationDate;

	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context ctx, Object obj) throws AbortVisitException 
	{
		final Subscriber subscriber = (Subscriber) obj;
        
        if (subscriber.getState() == SubscriberStateEnum.AVAILABLE &&
            subscriber.getSpid() == serviceProvider_.getId() && 
			(subscriber.getStartDate() != null && subscriber.getStartDate().getTime() >= date_.getTime()))
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
