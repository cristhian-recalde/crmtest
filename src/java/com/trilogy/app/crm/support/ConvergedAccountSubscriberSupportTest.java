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
package com.trilogy.app.crm.support;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriber;
import com.trilogy.app.crm.bean.ConvergedAccountSubscriberXInfo;
import com.trilogy.app.crm.bean.Subscriber;

public class ConvergedAccountSubscriberSupportTest extends TestCase {

	private static Context mainCtx_;

	public static TestSuite suite(Context ctx)
	{
	   TestSuite suite = new TestSuite(ConvergedAccountSubscriberSupportTest.class);
	   mainCtx_ = ctx;
	   return suite;
	}
	protected void setUp() throws Exception
    {
		super.setUp();
	}

	protected void tearDown() throws Exception
    {
		super.tearDown();
	}

	/*
	 * Test method for 'com.redknee.app.crm.support.ConvergedAccountSubscriberSupport.mergeAcctSubHome(Context, Account, Subscriber, Home, short)'
	 */
	public void testMergeHomeNull()
    {
		
		Home home =null;
		String ban="11142";
		Account acct=null;
		Home   acctHome = (Home) mainCtx_.get(AccountHome.class);
			
		try
		{
			acct=(Account)acctHome.find(new EQ(AccountXInfo.BAN,ban));
		}
		catch (HomeException e)
		{
		}
		catch (NullPointerException e)
		{
		}
		
		Subscriber sub =null;
		home=ConvergedAccountSubscriberSupport.mergeAcctSubHome(mainCtx_,acct,sub,home,(short)1);
		assertNotNull("ConvergedAccountSubscriber Home is Null!!",home);
	}

	
	public void testMergeHomeValue()
    {
		Home home =null;
		String ban="11142";
		Account acct=null;
		Home   acctHome = (Home) mainCtx_.get(AccountHome.class);
			
		try
		{
			acct=(Account)acctHome.find(new EQ(AccountXInfo.BAN,ban));
		}
		catch (HomeException e)
		{
		}
		catch (NullPointerException e)
		{
		}
		
		Subscriber sub =null;
		home=ConvergedAccountSubscriberSupport.mergeAcctSubHome(mainCtx_,acct,sub,home,(short)1);
			
		try {
			ConvergedAccountSubscriber conAcctSub=(ConvergedAccountSubscriber)home.find(new EQ(ConvergedAccountSubscriberXInfo.BAN,ban));
			
			assertEquals("Converged Bean is not merged correctly!!",ban,conAcctSub.getBAN());
		} catch (HomeException e) {
			e.printStackTrace();
		} 

	}

}
