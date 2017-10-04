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
package com.trilogy.app.crm.subscriber.filter;

import java.sql.SQLException;
import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;

/**
 * @author jchen
 *
 * Check if subscriber is in pending state and post scheduled starting date
 */
public class SubscriberPostScheduleStartDatePredicate implements Predicate, XStatement
{
	public SubscriberPostScheduleStartDatePredicate(Date checkTime)
    {
		checkTime_ = checkTime;
     }
     
     public boolean f(Context _ctx, Object obj)
     {
         Subscriber sub = (Subscriber) obj;
         if (getScheduleActivationDate(sub).getTime() >  checkTime_.getTime()
         		&& sub.getState().getIndex() == SubscriberStateEnum.PENDING_INDEX)
         	return true;
         return false;
     }

	/**
	 * @param sub
	 * @return
	 */
	private Date getScheduleActivationDate(Subscriber sub) 
	{
		//return sub.getScheduleActivationDate();
        return sub.getStartDate();
    }


    /**
     * Set a PreparedStatement with the supplied Object.
     */
    public void set(Context ctx, XPreparedStatement ps) throws SQLException
    {
    }


    public String createStatement(Context ctx)
    {
        // return "scheduleActivationDate > "
        return "startDate > " + checkTime_.getTime() + " AND state = " + SubscriberStateEnum.PENDING_INDEX;
    }

     private Date checkTime_; 
}
