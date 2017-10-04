/*
 * TestLicenseHome.java
 * 
 * Author : danny.ng@redknee.com
 * Date : Apr 21, 2006
 * 
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.license.DefaultLicenseMgr;
import com.trilogy.framework.license.License;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.license.LicenseXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Unit tests the LicenseHome.
 * 
 * @author danny.ng@redknee.com
 * @since  Apr 21, 2006
 */
/**
 * @author dannyng
 *
 */
public class TestLicenseHome extends ContextAwareTestCase
{

    public TestLicenseHome(String name)
    {
        super(name);
    }
    
    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }
    

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestLicenseHome.class);
        return suite;
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp()
    {
        super.setUp();
        final Context ctx = getContext();
     
        /**
         * Install my license into the license manager
         */
        Home licenses = new com.redknee.framework.license.LicenseTransientHome(ctx);
        License myLicense = new License();
        myLicense.setName(LICENSE_KEY);
        myLicense.setKey(LICENSE_KEY);
        myLicense.setEnabled(true);
        try
        {
            licenses.create(ctx, myLicense);
        }
        catch (Exception e)
        {
            fail("Unexpected exception installing license into license manager " + e.getMessage());
        }
        
        /**
         * Put my license home into the context
         */
        ctx.put(com.redknee.framework.license.LicenseHome.class, licenses);
        
        /**
         * Put my license manager into the context, this is used by the 
         * app crm LicenseHome
         */
        ctx.put(LicenseMgr.class,
                new DefaultLicenseMgr(ctx));
        
        /**
         * Our test home
         */
        ctx.put(TEST_HOME, new LicenseHome(ctx, LICENSE_KEY,
                new LicenseeHome(
                        new EndHome())));
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    public void tearDown()
    {
        LicenseeHome.resetCounters();
        EndHome.resetCounters();
        
        super.tearDown();
        
    }
    
    
    /**
     * Tests that the licensing home is not skipped when the license is enabled
     *
     */
    public void testLicenseHome()
    {
        final Context ctx = getContext();
        Home home = (Home) ctx.get(TEST_HOME);
        
        /**
         * Call an op on our test home
         */
        try
        {
            home.create(ctx, "blah");
            assertEquals("Home under license did not get called expected number of times.", 1, LicenseeHome.getTimesCalled());
            assertEquals("End home did not get called expected number of times.", 1, EndHome.getTimesCalled());
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    
    /**
     * Tests that the licensing home is skipped when the license is disabled
     *
     */
    public void testLicenseHomeDisabledLicense()
    {
        final Context ctx = getContext();
        Home home = (Home) ctx.get(TEST_HOME);
        
        try
        {
            /**
             * Disable our license
             */
            Home licenses = (Home) ctx.get(com.redknee.framework.license.LicenseHome.class);
            License license = (License) licenses.find(ctx, new EQ(LicenseXInfo.NAME, LICENSE_KEY));
            license.setEnabled(false);
            licenses.store(ctx, license);
        
            /**
             * Call an op on our test home
             */
            home.create(ctx, "blah");
            assertEquals("Home under license was called an unexpected number of times.", 0, LicenseeHome.getTimesCalled());
            assertEquals("End home did not get called an expected number of times.", 1, EndHome.getTimesCalled());
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    
    /**
     * Tests that the licensing home is skipped when the license is not in the license manager
     *
     */
    public void testLicenseHomeWithMissingLicense()
    {
        final Context ctx = getContext();
        Home home = (Home) ctx.get(TEST_HOME);
        
        try
        {
            /**
             * Disable our license
             */
            Home licenses = (Home) ctx.get(com.redknee.framework.license.LicenseHome.class);
            License license = (License) licenses.find(ctx, new EQ(LicenseXInfo.NAME, LICENSE_KEY));
            licenses.remove(ctx, license);
        
            /**
             * Call an op on our test home
             */
            home.create(ctx, "blah");
            assertEquals("Home under license was called an unexpected number of times.", 0, LicenseeHome.getTimesCalled());
            assertEquals("End home did not get called an expected number of times.", 1, EndHome.getTimesCalled());
        }
        catch (Exception e)
        {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    /**
     * Test Home name
     */
    public static final String TEST_HOME = "Test Home";
    
    /**
     * License key for the LicenseHome
     */
    public static final String LICENSE_KEY = "License Key";
    
    /**
     * Custom test home to determine if an operation on this home was called
     * @author dannyng
     *
     */
    static class LicenseeHome extends HomeProxy
    {
        private static final long serialVersionUID = 1L;

        public LicenseeHome(final Home delegate)
        {
            super(delegate);
        }
        
        public Object create(Context ctx, Object obj)
        {   
            timesCreateCalled++;
            try
            {
                obj = super.create(ctx, obj);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return obj;
        }
        
        public static int getTimesCalled()
        {
            return timesCreateCalled;
        }
        
        public static void resetCounters()
        {
            timesCreateCalled = 0;
        }
        
        private static int timesCreateCalled = 0;
    }
    
    
    /**
     * Custom test home to determine if an operation on this home was called
     * @author dannyng
     *
     */
    static class EndHome extends HomeProxy
    {
        private static final long serialVersionUID = 1L;
        
        public EndHome()
        {
            super();
        }
        
        public Object create(Context ctx, Object obj)
        {   
            timesCreateCalled++;
            return obj;
        }
        
        public static int getTimesCalled()
        {
            return timesCreateCalled;
        }
        
        public static void resetCounters()
        {
            timesCreateCalled = 0;
        }
        
        private static int timesCreateCalled = 0;
    }
    
}
