/*
 * Created on Oct 29, 2004
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.home.Home;


/**
 * @author jchen
 */
public class TestServiceSupport extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestServiceSupport(final String name)
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

        final TestSuite suite = new TestSuite(TestServiceSupport.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        arrServices = prepareServicesHome();
        
    }

    
    Service arrServices[] = null;
    public Service[] prepareServicesHome()
    {
        Service arr[] = null;
        arr = new Service[4];
        
        arr[0] = new Service();
        arr[0].setID(1000);
        arr[0].setName("Service 0");
        
        
        arr[1] = new Service();
        arr[1].setID(1001);
        arr[1].setName("Service 1");
        
        
        arr[2] = new Service();
        arr[2].setID(1002);
        arr[2].setName("Service 2");
        
        
        arr[3] = new Service();
        arr[3].setID(1003);
        arr[3].setName("Service 3");

        Home home = new ServiceTransientHome(getContext());
        
        
        for (int i = 0; i < arr.length ; i++)
        {
	        try{
	            home.create(getContext(),arr[i]);
	        }
	        catch(Throwable a)
	        {
	            a.printStackTrace();
	            throw new AssertionError("Test case setup error." + a);
	        }
        }
        getContext().put(ServiceHome.class, home);
        return arr;
    }

    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the getBillCycleDay() method works according to the intent.
     * 
     * @exception HomeException Thrown if failed to create any test object.
     */
    public void testGetServiceIdString()
    {
        
        Collection cl = new ArrayList();
        
        String str = ServiceSupport.getServiceIdString( cl);
        assertTrue("".equals(str));
                
        cl.add(arrServices[1]);
        str = ServiceSupport.getServiceIdString( cl);
        assertTrue("1001".equals(str));
                
        cl.add(arrServices[2]);
        str = ServiceSupport.getServiceIdString( cl);
        assertTrue("1001,1002".equals(str));
    }
    
    
    public void testTransformServiceIdToObjects() throws HomeException
    {
        Collection c1 = new ArrayList();
        c1.add(Integer.valueOf(1001));
        c1.add(Integer.valueOf(1002));


        Collection c2 = new ArrayList(); 
        c2.addAll(ServiceSupport.transformServiceIdToObjects(getContext(), c1));
        
        assertTrue(c2.size() == 2);
        assertTrue(c2.contains(arrServices[1]));
        assertTrue(c2.contains(arrServices[2]));
    }
    
    
    
    public void testTransformServiceObjectToIds()
    {
        Collection c1 = new ArrayList();
        c1.add(arrServices[1]);
        c1.add(arrServices[2]);


        Collection c2 = new ArrayList(); 
        c2.addAll(ServiceSupport.transformServiceObjectToIds(c1));
        
        assertTrue(c2.size() == 2);
        assertTrue(c2.contains(Integer.valueOf(1001)));
        assertTrue(c2.contains(Integer.valueOf(1002)));
    }
    
}
