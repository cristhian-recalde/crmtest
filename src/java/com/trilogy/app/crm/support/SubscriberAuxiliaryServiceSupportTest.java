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

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

public class SubscriberAuxiliaryServiceSupportTest extends ContextAwareTestCase
{
	public SubscriberAuxiliaryServiceSupportTest(final String name)
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

        final TestSuite suite = new TestSuite(SubscriberAuxiliaryServiceSupportTest.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        // Set up the Test Subscriber.
    }


    // INHERIT
    public void tearDown()
    {
        // Tear down the Test Subscriber.
        
        super.tearDown();
    }
    
    public void testGetSubscriberProvisionedAuxiliaryServiceIDs() throws HomeException
    {
    	Context ctx = getContext().createSubContext();
    	Home home = new SubscriberAuxiliaryServiceTransientHome(ctx);
    	ctx.put(SubscriberAuxiliaryServiceHome.class, home);
    	long id = 1;
    	
    	{
    		SubscriberAuxiliaryService sas = new SubscriberAuxiliaryService();
    		sas.setIdentifier(id++);
    		sas.setSubscriberIdentifier("111-1");
    		sas.setAuxiliaryServiceIdentifier(123L);
    		sas.setProvisioned(true);
    		home.create(sas);
    	}
    	
    	{
    		SubscriberAuxiliaryService sas = new SubscriberAuxiliaryService();
    		sas.setIdentifier(id++);
    		sas.setSubscriberIdentifier("111-1");
    		sas.setAuxiliaryServiceIdentifier(456L);
    		sas.setProvisioned(true);
    		home.create(sas);
    	}
    	
    	{
    		SubscriberAuxiliaryService sas = new SubscriberAuxiliaryService();
    		sas.setIdentifier(id++);
    		sas.setSubscriberIdentifier("111-1");
    		sas.setAuxiliaryServiceIdentifier(789L);
    		sas.setProvisioned(false);
    		home.create(sas);
    	}
    	
    	{
    		SubscriberAuxiliaryService sas = new SubscriberAuxiliaryService();
    		sas.setIdentifier(id++);
    		sas.setSubscriberIdentifier("111-2");
    		sas.setAuxiliaryServiceIdentifier(123L);
    		sas.setProvisioned(true);
    		home.create(sas);
    	}
    	
    	Subscriber sub = new Subscriber();
    	sub.setId("111-1");
    	Collection list = SubscriberAuxiliaryServiceSupport.getSubscriberProvisionedAuxiliaryServiceIDs(ctx, sub);
    	assertTrue(list.size() == 2);
    	assertTrue(list.contains(Long.valueOf(123L)));
    	assertTrue(list.contains(Long.valueOf(456L)));
    }


}
