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
package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author jchen
 */
public class SubscriberStateUpdateAgentTest extends ContextAwareTestCase 
{

	    /**
	     * Constructs a test case with the given name.
	     *
	     * @param name The name of the test.
	     */
	    public SubscriberStateUpdateAgentTest(final String name)
	    {
	        super(name);
	    }


	    /**
	     * Creates a new suite of Tests for execution.  This method is intended to
	     * be invoked by standard JUnit tools (i.e., those that do not provide a
	     * context).
	     *
	     * @return A new suite of Tests for execution.
	     */
	    public static Test suite()
	    {
	        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
	    }


	    /**
	     * Creates a new suite of Tests for execution.  This method is intended to
	     * be invoked by the Redknee Xtest code, which provides the application's
	     * operating context.
	     *
	     * @param context The operating context.
	     * @return A new suite of Tests for execution.
	     */
	    public static Test suite(final Context context)
	    {
	        setParentContext(context);

	        final TestSuite suite = new TestSuite(SubscriberStateUpdateAgentTest.class);

	        return suite;
	    }
	    
	    /*
	     * @see TestCase#setUp()
	     */
	    protected void setUp()
	    {
	        super.setUp();
	    }

	    /*
	     * @see TestCase#tearDown()
	     */
	    protected void tearDown(){
	        super.tearDown();
	    }
	    
	    public Context prepareSubscriberTestHome() throws HomeException
	    {
	    	Context subCtx = getContext().createSubContext();
            // TODO 2009-03-05 rewrite to use Transient Home
            Home home = new SubscriberXDBHome(subCtx, "Subscriber_test");
	    	subCtx.put(SubscriberHome.class, home);
	    	home.removeAll(subCtx);
	    	return subCtx;
	    	
	    }
	    
	    public void testGetPreExpiredSubscribers() throws Exception
	    {
	    	Context ctx = prepareSubscriberTestHome();
	    	Home home = (Home)ctx.get(SubscriberHome.class);
	    	ArrayList preExpiredSubIdList = new ArrayList();
	    	
	    	//int subId = 1000;
	    	int preExpiryDays = 3;
	    	
	    	{
	    		//subscerber not expired  NO
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 1); 
	    		sub.setId(sub_id);
	    		//preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(5, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.ACTIVE);
	    		sub.setExpiryDate(expired);
	    		home.create(ctx,sub);
	    	}
	    	
	    	{
	    		//subscerber active pre- expired
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 2); 
	    		sub.setId(sub_id);
	    		preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(1, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.ACTIVE);
	    		sub.setExpiryDate(expired);
	    	}
	    	
	    	{
	    		//subscerber active pre- expired
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 3); 
	    		sub.setId(sub_id);
	    		preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(2, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.ACTIVE);
	    		sub.setExpiryDate(expired);
	    	}
	    	
	    	{
	    		//subscerber active pre- expired   YES
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 4); 
	    		sub.setId(sub_id);
	    		preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(2, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.ACTIVE);
	    		sub.setExpiryDate(expired);
	    	}
	    	
	    	{
	    		//subscerber suspend pre- expired  YES
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 5); 
	    		sub.setId(sub_id);
	    		preExpiredSubIdList.add(sub_id);
	    		preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(1, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.SUSPENDED);
	    		sub.setExpiryDate(expired);
	    	}
	    	
	    	{
	    		//subscerber Pending  pre- expired NO
	    		Subscriber sub = new Subscriber();
	    		String sub_id = "" + (1000 + 6); 
	    		sub.setId(sub_id);
	    		//preExpiredSubIdList.add(sub_id);
	    		
	    		Date expired = CalendarSupportHelper.get(ctx).findDateDaysAfter(1, new Date());
	    		
	    		sub.setState(SubscriberStateEnum.AVAILABLE);
	    		sub.setExpiryDate(expired);
	    	}
	    }
}
