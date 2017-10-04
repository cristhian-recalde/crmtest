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
package com.trilogy.app.crm.client;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.EcpStateMap;
import com.trilogy.app.crm.bean.SubscriberStateConversion;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;



/**
 * @author jchen
 */
public class AppEcpClientSupportTest extends ContextAwareTestCase {

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public AppEcpClientSupportTest(final String name)
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

        final TestSuite suite = new TestSuite(AppEcpClientSupportTest.class);

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
    
    
    /**
     * In order to let prepared home to take effect, we need to install these homes
     * to install.java level, so all request services will still work.
     */
    public Context getContext()
    {
        return super.getContext();
       
    }
    
    public void testAll()
    {
    	AppEcpClientSupportTest myTest = new AppEcpClientSupportTest("EcpStateMapping");
    	AppEcpClientSupportTest.testEcpStateMapping(getContext());
    }
    
    
        

   
        
    /**
     * Call this funciton in install.java to do full test.
     * TODO, put it in xtest
     * @param ctx
     */
    public static void testEcpStateMapping(Context ctx) 
    {
    	EcpStateMap esm = new EcpStateMap();
    	ctx.put(EcpStateMap.class, esm);
    	
    	Map map = esm.getStates();
    	{
    		//the same number, 
    		SubscriberStateConversion ssc = new SubscriberStateConversion();
    		ssc.setSubscriberState(SubscriberStateEnum.ACTIVE);
    		ssc.setNewStateIndex(AppEcpClient.ACTIVE);
    		
    		map.put(ssc.ID(), ssc);    		
    	}
    	
    	
    	{
    		//different number
    		SubscriberStateConversion ssc = new SubscriberStateConversion();
    		ssc.setSubscriberState(SubscriberStateEnum.AVAILABLE);
    		ssc.setNewStateIndex(AppEcpClient.AVAILABLE);
    		
    		map.put(ssc.ID(), ssc);    		
    	}
    	
    	{
    		//not in defined value in ecp
    		SubscriberStateConversion ssc = new SubscriberStateConversion();
    		ssc.setSubscriberState(SubscriberStateEnum.IN_ARREARS);
    		ssc.setNewStateIndex(AppEcpClient.AVAILABLE);
    		
    		map.put(ssc.ID(), ssc);    		
    	}
    	
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.ACTIVE), (int)AppEcpClient.ACTIVE);
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.AVAILABLE), (int)AppEcpClient.AVAILABLE);
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.IN_ARREARS), (int)AppEcpClient.AVAILABLE);
    	
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.IN_COLLECTION), -1);
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.PENDING), -1);
    	
    	assertEquals(AppEcpClientSupport.mapPostpaidEcpState(ctx, SubscriberStateEnum.NON_PAYMENT_WARN), (int)AppEcpClient.DUNNED_WARNING );
    	
    }
}
