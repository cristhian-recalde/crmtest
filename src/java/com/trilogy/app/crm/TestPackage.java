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
package com.trilogy.app.crm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.core.factory.FacetInstall;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.DefaultFacetMgr;
import com.trilogy.framework.xhome.beans.FacetMgr;
import com.trilogy.framework.xhome.beans.Function;
import com.trilogy.framework.xhome.beans.ParentClassFacetMgr;
import com.trilogy.framework.xhome.beans.SimpleFacetMgr;
import com.trilogy.framework.xhome.beans.facets.java.lang.ObjectFunction;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.logger.FilterLogger;
import com.trilogy.framework.xlog.logger.LogMsgPredicate;
import com.trilogy.framework.xlog.logger.Logger;
import com.trilogy.framework.xlog.logger.PrintStreamLogger;
import com.trilogy.framework.xlog.logger.SystemFilterLoggerConfig;

/**
 * Provides a central point for running all AppCrm JUnit tests.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestPackage
    extends TestCase
{
    /**
     * This key can be used to query the context to see if it contains
     * application data.  Standard JUnit tools will not be able to provide
     * application data in the context.  It is best to assume that application
     * data is not proviced.
     */
    public static final String APPLICATION_CONTEXT =
        TestPackage.class.getName() + ".APPLICATION_CONTEXT";


    public static final String RUNNING_UNIT_TEST = 
        TestPackage.class.getName() + ".RUNNING_UNIT_TEST";
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPackage(final String name)
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
        return suite(createDefaultContext());
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
        // Ensure that the APPLICATION_CONTEXT is set.
        context.put(APPLICATION_CONTEXT, context.getBoolean(APPLICATION_CONTEXT, true));
        // Ensure that the RUNNING_UNIT_TEST is set to true.
        context.put(RUNNING_UNIT_TEST, true);

        final TestSuite suite = new TestSuite(TestPackage.class.getPackage().getName() + ".*");

        suite.addTest(new TestSuite(TestPackage.class));
        suite.addTest(com.redknee.app.crm.TestStringSeparator.suite(context));

        suite.addTest(com.redknee.app.crm.account.state.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.bas.recharge.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.bas.tps.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.bean.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.bundle.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.bulkloader.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.client.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.support.test.TestCollectionSupport.suite(context));
        suite.addTest(com.redknee.app.crm.filter.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.home.TestPackage.suite(context));
         //suite.addTest(com.redknee.app.crm.invoice.TestPackage.suite(context));
        //suite.addTest(com.redknee.app.crm.invoice.bucket.category.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.log.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.move.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.numbermgn.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.poller.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.priceplan.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.provision.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.sequenceId.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.support.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.support.test.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.transaction.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.unit_test.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.support.test.TestCalendarSupport.suite(context));
        suite.addTest(com.redknee.app.crm.urcs.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.web.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.web.control.TestPackage.suite(context));
        suite.addTest(com.redknee.app.crm.subscriber.provision.calculation.TestPackage.suite(context));
        return suite;
    }


    /**
     * Provides a default context for passing to the suite() method as
     * necessary.
     *
     * @return The Context.
     */
    public static Context createDefaultContext()
    {
        final Context context = new ContextSupport();
        context.setName("JUnit Context");
        context.put(APPLICATION_CONTEXT, false);
        context.put(RUNNING_UNIT_TEST, true);
        
        try
        {
            System.setProperty("rk.home", "/opt/redknee/app/crm/current");
            CoreSupport.initProjectHome(context);
        }
        catch (AgentException ae)
        {
            ae.printStackTrace();
        }

        final FacetMgr facetMgr = new ParentClassFacetMgr(new DefaultFacetMgr(SimpleFacetMgr.instance()));
        facetMgr.register(context, Object.class, Function.class, ObjectFunction.class);
        context.put(FacetMgr.class, facetMgr);
        final FacetInstall install = new FacetInstall();
        try
        {
            install.execute(context);
        }
        catch (AgentException e)
        {
        }

        try
        {
            // this predicate and FilterLogger is needed because in 5_7 FW FacetMgr is spamming the logs
            final Or predicate = new Or();
            predicate.add(new LogMsgPredicate(FacetMgr.class.getName(), SeverityEnum.INFO, false));
            predicate.add(new LogMsgPredicate("com.redknee.app.crm.*", SeverityEnum.DEBUG, true));
            final Logger logger = new FilterLogger(context, new PrintStreamLogger(context, System.out), predicate);
            context.put(Logger.class, logger);
            LogSupport.setSeverityThreshold(context, SeverityEnum.DEBUG);
            
            //Util Snippet Logging Instantiation
            SystemFilterLoggerConfig config = new SystemFilterLoggerConfig();
            config.setSeverityThreshold(SeverityEnum.DEBUG);
            context.put(SystemFilterLoggerConfig.class, config);
            com.redknee.util.snippet.log.Logger.install(context);
        }
        catch (final Exception e)
        {
            System.out.println("Exception caught: ");
            e.printStackTrace();
        }

        TestSupport.setupTransientSubscriptionType(context);
        TestSupport.setupTransientExternalAppMapping(context);

        return context;
    }


    /**
     * Tests that the default Context created indicates that it does not contain
     * application data.
     */
    public void testDefaultContextContainsNoApplicationData()
    {
        final Context context = createDefaultContext();

        assertFalse(context.getBoolean(APPLICATION_CONTEXT));
        assertFalse(context.getBoolean(APPLICATION_CONTEXT, true));
        assertFalse(context.getBoolean(APPLICATION_CONTEXT, false));
    }
    
    /**
     * Check if the flag indicating a Unit Test is enabled.
     * @param context
     * @return
     */
    public static boolean isRunningUnitTest(Context context)
    {
        return context.getBoolean(RUNNING_UNIT_TEST, false);
    }

} // class
