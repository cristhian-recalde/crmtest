/*
 * Created on Mar 31, 2005
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author psperneac
 */
public class TestDayRangeManager extends TestCase
{
	public static Context ctx_;
	
	public static DateFormat DF=new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
	
	public TestDayRangeManager(String name)
	{
		super(name);
	}

	public static Test suite(Context ctx)
	{
		ctx_=ctx.createSubContext();
		ctx_.setName("TestCallDetailDayRangeManager");
		TestSuite suite=new TestSuite(TestDayRangeManager.class);
		
		return suite;
	}

	public void testGetStartOfIntervalMorning()
	{
		DayRangeManager range=new DayRangeManager();
		
		try
		{
			Date aDate=DF.parse("19721016-101000-000");
			Date start=range.getStartOfInterval(getContext(),aDate);
			String strStart=DF.format(start);
			assertEquals("19721016-000000-000",strStart);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(getContext()))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(getContext());
			}
			fail(e.getMessage());
		}
	}
	
	public void testGetStartOfIntervalAfternoon()
	{
		DayRangeManager range=new DayRangeManager();
		
		try
		{
			Date aDate=DF.parse("19721016-222000-000");
			Date start=range.getStartOfInterval(getContext(),aDate);
			String strStart=DF.format(start);
			assertEquals("19721016-000000-000",strStart);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(getContext()))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(getContext());
			}
			fail(e.getMessage());
		}
	}

	public void testGetEndOfIntervalMorning()
	{
		DayRangeManager range=new DayRangeManager();
		
		try
		{
			Date aDate=DF.parse("19721016-101000-000");

			Date end=range.getEndOfInterval(getContext(),aDate);
			String strEnd=DF.format(end);
			assertEquals("19721016-235959-999",strEnd);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(getContext()))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(getContext());
			}
			fail(e.getMessage());
		}
	}
	
	
	public void testGetEndOfIntervalAfternoon()
	{
		DayRangeManager range=new DayRangeManager();
		
		try
		{
			Date aDate=DF.parse("19721016-222000-000");

			Date end=range.getEndOfInterval(getContext(),aDate);
			String strEnd=DF.format(end);
			assertEquals("19721016-235959-999",strEnd);
		}
		catch (ParseException e)
		{
			if(LogSupport.isDebugEnabled(getContext()))
			{
				new DebugLogMsg(this,e.getMessage(),e).log(getContext());
			}
			fail(e.getMessage());
		}
	}

	public static Context getContext()
	{
		if(ctx_==null)
		{
			ctx_=new ContextSupport();
		}
		
		return ctx_;
	}
}
