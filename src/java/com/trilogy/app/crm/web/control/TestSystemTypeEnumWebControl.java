package com.trilogy.app.crm.web.control;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.unit_test.LicensedTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;

public class TestSystemTypeEnumWebControl 
extends LicensedTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSystemTypeEnumWebControl(final String name)
    {
        super(name, new String[]{LicenseConstants.HYBRID_LICENSE_KEY});
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

        final TestSuite suite = new TestSuite(TestSystemTypeEnumWebControl.class);

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
        final SystemTypeEnumWebControl webControl =
            new SystemTypeEnumWebControl();
        
        assertTrue(
            "The delegate class should be of EnumWebControl class",
            webControl instanceof EnumWebControl);
    }
    
    
    /**
     * Tests that the toWeb() method works according to the intent.
     */
    public void testGetEnumCollection()
    {
        final SystemTypeEnumWebControl webControl =
            new SystemTypeEnumWebControl();
 
        Account account = new Account(); 
        getContext().put(AbstractWebControl.BEAN, account);
        getContext().put(AccountCategoryHome.class, new AccountCategoryTransientHome(getContext())); 
        assertTrue(
               "if has hybrid license, " ,
                webControl.getEnumCollection(getContext()).size() == 3);
     }
    
 
}