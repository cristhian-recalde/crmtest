/*
 *  CallDetailpostedDateAfterPredicate.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-10-21
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.filter;

import java.util.Date;

import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

/**
 * A Predicate used to find CallDetails with specific posdtedDate after the
 * given date.
 */
public class CallDetailPostedDateAfterPredicate
	implements Predicate
{
    /**
     * Creates a new predicate with the given criteria.
     *
     * @param date The postedDate to match.
     *
     * @exception IllegalArgumentException Thrown if the given date is
     * null. 
     */ 
	public CallDetailPostedDateAfterPredicate(final Date date)
	{
        if (date == null)
        {
            throw new IllegalArgumentException(
                "Could not initialize predicate.  "
                + "The given date parameter is null.");
        }
        
		date_ = date;
	}

    
    /**
     * @return True if the postedDate of the given callDetail comes after the
     * date criteria.
     */
	public boolean f(Context ctx,final Object obj)
	{
		final CallDetail detail = (CallDetail)obj;

		return detail.getPostedDate().after(date_);
	}


    /**
     * The postedDate to watch for.
     */
	protected final Date date_;
}
