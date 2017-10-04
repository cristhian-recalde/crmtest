/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.DateTimeWebControl;

import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for AccountPromiseToPayDateWebControl.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAccountPromiseToPayDateWebControl
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAccountPromiseToPayDateWebControl(final String name)
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

        final TestSuite suite = new TestSuite(TestAccountPromiseToPayDateWebControl.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }
    
    
    /**
     * Tests that the constructor works according to the intent.
     */
    public void testConstructor()
    {
        final AccountPromiseToPayDateWebControl webControl =
            new AccountPromiseToPayDateWebControl();
        
        assertTrue(
            "The delegate class should be of DateTimeWebControl class",
            webControl.getDelegate() instanceof DateTimeWebControl);
    }
    
    
    /**
     * Tests that the toWeb() method works according to the intent.
     */
    public void testToWeb()
    {
        final AccountPromiseToPayDateWebControl webControl =
            new AccountPromiseToPayDateWebControl();
        
        final PrintWriter printWriter =
            new PrintWriter(new StringWriter());
        
        // Test case for non-null input date.
        {
            final Date todayDate =
                CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(Calendar.getInstance().getTime());
            
            final TestWrapperDateTimeWebControl delegate =
                new TestWrapperDateTimeWebControl();
            
            webControl.setDelegate(delegate);  // Use the custom DateTimeWebControl
            webControl.toWeb(getContext(), printWriter, "dummy", todayDate);
            
            assertTrue(
                "Use the input date if it is not null",
                todayDate.equals(delegate.getInputObject()));
        }
        
        // Test case for null input date.
        {
            final Date tomorrowDate =
                CalendarSupportHelper.get(getContext()).getDayAfter(Calendar.getInstance().getTime());
            
            final TestWrapperDateTimeWebControl delegate =
                new TestWrapperDateTimeWebControl();
            
            webControl.setDelegate(delegate);  // Use the custom DateTimeWebControl
            webControl.toWeb(getContext(), printWriter, "dummy", null);
            
            assertTrue(
                "Use tomorrow date if the input date is null",
                tomorrowDate.equals(delegate.getInputObject()));
        }
    }
    
    
    /**
     * Provides a convenient test wrapper class for DateTimeWebControl.
     */
    private class TestWrapperDateTimeWebControl extends DateTimeWebControl
    {
        public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
        {
            inputObject_ = obj;
        }
        
        public Object getInputObject()
        {
            return inputObject_;
        }
        
        private Object inputObject_ = null;
    } // inner-class
}
