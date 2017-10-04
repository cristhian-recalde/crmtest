/*
 * Created on Mar 30, 2005
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
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Abstract class for all range managers. Implements the methods that are shared between all range managers.
 * 
 * @author psperneac
 */
public abstract class AbstractRangeManager implements RangeManager
{
	public static final String DATE_FORMAT_STRING = "yyyyMMddHH";
	public static Date parseStartOfInterval(Context ctx, String value)
	{
		try
		{
			DateFormat localDF=new SimpleDateFormat(DATE_FORMAT_STRING);
			return localDF.parse(value);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(AbstractRangeManager.class.getName(),e.getMessage(),e).log(ctx);
			}
		}
		return null;
	}
	
	public static Date parseEndOFInterval(Context ctx, String value)
	{
		try
		{
			DateFormat localDF=new SimpleDateFormat(DATE_FORMAT_STRING);
			return localDF.parse(value);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(AbstractRangeManager.class.getName(),e.getMessage(),e).log(ctx);
			}
		}
		return null;
	}
	
	public String getName(Context ctx,Date start, Date end)
	{
		final DateFormat df=new SimpleDateFormat(DATE_FORMAT_STRING);

		return df.format(start)+"_"+df.format(new Date(end.getTime()+1));
	}
}
