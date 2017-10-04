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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.calldetail;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * @author psperneac
 */
public class TestCallDetailHome extends TestCase
{
	protected static Context ctx_;
	
	public TestCallDetailHome(String name)
	{
		super(name);
	}
	
	public static Test suite(Context ctx)
	{
		ctx_=ctx.createSubContext();
		ctx_.setName("TestCallDetailRangeHome");
		TestSuite suite=new TestSuite(TestCallDetailHome.class);
		
		return suite;
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testCreate()
	{
		assertTrue(true);
	}
	
	public Context getContext()
	{
		if(ctx_==null)
		{
			ctx_=new ContextSupport();
		}
		
		return ctx_;
	}
	
	
}
