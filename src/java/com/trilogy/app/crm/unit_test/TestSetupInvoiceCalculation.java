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
package com.trilogy.app.crm.unit_test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SubscriberCycleUsageHome;
import com.trilogy.app.crm.bean.SubscriberCycleUsageTransientHome;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessServer;
//import com.trilogy.app.crm.factory.AccountHierarchyInvoiceCalculationFactory;
//import com.trilogy.app.crm.factory.InvoiceCalculationFactory;
import com.trilogy.app.crm.invoice.factory.SubscriberCalculationFactoryImpl;
import com.trilogy.app.crm.invoice.factory.SubscriberCalculatorFactory;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Used to setup CRM Invoice Calculation Factories and all basic necessary entities for unit testing 
 * Invoice Calculations.
 * @author angie.li@redknee.com
 *
 */
public class TestSetupInvoiceCalculation extends ContextAwareTestCase 
{
	/**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSetupInvoiceCalculation(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestSetupInvoiceCalculation.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();
        
        setup(getContext());
    }
    
    @Override
    public void tearDown()
	{
    	completelyTearDown(getContext());
	    super.tearDown();
	}

    /**
     * Setup the Calculation Factories: plus anything else needed to do invoice calculations.
     * @param context
     */
	public static void setup(Context context) 
	{
		if (context.getBoolean(TestSetupInvoiceCalculation.class, true))
		{
			setupCalculationFactories(context);
			TestSetupAdjustmentTypes.setup(context);
			TestSetupPaymentPlan.setup(context);
			TestSetupCallDetails.setup(context, false, true);
			TestSetupTransactions.setup(context, false);
			TestSetupInvoiceHistory.setup(context);
			
			// Install dunning process for testing
	        context.put(DunningProcess.class, new DunningProcessServer(context));
	        
	        // Install Empty Subscriber Cycle Usage
	        //TODO: Break the installation of SubscriberCycleUsageHome into another Test harness setup class.
	        context.put(SubscriberCycleUsageHome.class, new TransientFieldResettingHome(context, new SubscriberCycleUsageTransientHome(context)));
	        
			
			//Avoid overwriting this setup.
			context.put(TestSetupTransactions.class, false);
		}
		else
		{
			LogSupport.debug(context, TestSetupInvoiceCalculation.class.getName(), 
					"Skipping TestSetupInvoiceCalculationFactories setup again.");
		}
	}

	public static void completelyTearDown(Context ctx) 
	{
		TestSetupAdjustmentTypes.completelyTeardown(ctx);
		TestSetupPaymentPlan.completelyTearDown(ctx);
		TestSetupCallDetails.completelyTearDown(ctx);
		TestSetupTransactions.deleteTransactions(ctx);
		TestSetupInvoiceHistory.tearDown(ctx);
	}

	/**
     * Install the Calculation Factories: from ServiceInstall.installInvoiceServices()
     * @param context
     */
	private static void setupCalculationFactories(Context context) 
	{
		//context.put(InvoiceCalculationFactory.class, new AccountHierarchyInvoiceCalculationFactory());
		context.put(SubscriberCalculatorFactory.class, new SubscriberCalculationFactoryImpl());
	}
	
	public void testSetup()
	{
		Context ctx = getContext().createSubContext();
		//assertNotNull("InvoiceCalculationFactory was null in context.", ctx.get(InvoiceCalculationFactory.class));
		assertNotNull("SubscriberCalculatorFactory was null in context", ctx.get(SubscriberCalculatorFactory.class));
		
		TestSetupAdjustmentTypes.testSetup(ctx);
		TestSetupPaymentPlan.testSetup(ctx);
		TestSetupCallDetails.testSetup(ctx);
		TestSetupTransactions.testSetup(ctx);
		TestSetupInvoiceHistory.testSetup(ctx);
	}

}
