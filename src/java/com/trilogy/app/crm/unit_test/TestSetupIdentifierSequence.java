package com.trilogy.app.crm.unit_test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.IdentifierSequenceHome;
import com.trilogy.app.crm.bean.IdentifierSequenceTransientHome;
import com.trilogy.app.crm.sequenceId.IdentifierSequenceIncrementHome;
import com.trilogy.app.crm.xhome.adapter.TransientFieldResetAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Unit test setup to install IdentifierSequenceHome used by most of CRM.
 * @author ali
 *
 */
public class TestSetupIdentifierSequence extends ContextAwareTestCase 
{
    public TestSetupIdentifierSequence(String name)
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
        final TestSuite suite = new TestSuite(TestSetupIdentifierSequence.class);
        return suite;
    }

    /**
     * By default install only the Transient Homes.
     */
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext());
    }

    //  INHERIT
    @Override
    public void tearDown()
    {
        //tear down here

        super.tearDown();
    }

    /**
     * Creates account in an account hierarchy
     * @param context
     */
    public static void setup(Context ctx)
    {
        if (ctx.getBoolean(TestSetupIdentifierSequence.class, true))
        {
            Home home = (Home) ctx.get(IdentifierSequenceHome.class);
            if (home == null)
            {
                home = new TransientFieldResettingHome(ctx, new IdentifierSequenceTransientHome(ctx));
                home = new IdentifierSequenceIncrementHome(home);
                ctx.put(IdentifierSequenceHome.class, home);
            }

            //Sequences will be installed with the installation of the Transient Home Decorators.
            
            //Install TestSetupIdentifierSequence key to prevent setup from running multiple times during one test.
            ctx.put(TestSetupIdentifierSequence.class, false);
        }
        else 
        {
            LogSupport.debug(ctx, TestSetupIdentifierSequence.class.getName(),
                    "Skipping TestSetupIdentifierSequence setup again.");
            
        }
    }
    
    public void testSetup()
    {
        testSetup(getContext());
    }
    
    /**
     * Test the Transient Homes were installed properly
     * @param ctx
     */
    public static void testSetup(Context ctx)
    {
        Home home = (Home) ctx.get(IdentifierSequenceHome.class);
        assertNotNull("IdentifierSequenceHome is null.  Setup Failed.", home);
    }

}
