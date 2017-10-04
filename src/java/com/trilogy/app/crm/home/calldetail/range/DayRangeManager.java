/*
 * Created on Mar 15, 2005
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
package com.trilogy.app.crm.home.calldetail.range;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Range manager for a day interval.
 * 
 * @author psperneac
 */
public class DayRangeManager extends AbstractRangeManager
{
    protected static RangeManager instance__=null;

	public static RangeManager instance()
	{
		if(instance__==null)
		{
			instance__=new DayRangeManager();
		}
		
		return instance__;
	}

	/**
	 * @see com.redknee.app.crm.home.calldetail.CallDetailRangeManager#getStartOfInterval(com.redknee.framework.xhome.context.Context, java.util.Date)
	 */
	public Date getStartOfInterval(Context ctx, Date currentDate)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * @see com.redknee.app.crm.home.calldetail.CallDetailRangeManager#getEndOfInterval(com.redknee.framework.xhome.context.Context, java.util.Date)
	 */
	public Date getEndOfInterval(Context ctx, Date currentDate)
	{
		Calendar cal=Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		cal.add(Calendar.DATE,1);
		cal.add(Calendar.MILLISECOND,-1);
		
		return new Date(cal.getTimeInMillis());
	}
}
