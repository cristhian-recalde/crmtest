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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.xhome.adapter.TransientFieldResetAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * This Unit test Setup Class is used to add Subscriber Service records to existing 
 * Subscribers.
 * 
 * Also, it provisions the Subscribers/Subscriptions existing in the system with 
 * The default PricePlan Service: Voice and Generic services.  Starting as of the day of 
 * Subscription creation.
 * 
 * @author angie.li@redknee.com
 *
 */
public class TestSetupSubscriberServices extends ContextAwareTestCase 
{
    public TestSetupSubscriberServices(String name)
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
        final TestSuite suite = new TestSuite(TestSetupSubscriberServices.class);
        return suite;
    }
    
    @Override
    public void setUp()
    {
        super.setUp();
        setup(getContext(), true);
        
    }
    
    //  INHERIT
    @Override
    public void tearDown()  
    {
        //tear down here
        completelyTearDown(getContext());
        
        super.tearDown();
    }
    
    public static void setup(Context ctx, boolean provisionServices)
    {
        if (ctx.getBoolean(TestSetupSubscriberServices.class, true))
        {
            try
            {
                setupSubscriberServicesHome(ctx);
                decorateSubscriberPipeline(ctx);
                if (provisionServices)
                {
                	provisionSubscriberServices(ctx);
                }
            }
            catch (HomeException e)
            {
                fail("Failed setup due to " + e.getMessage());
            }
            //To prevent overwriting this setup when run multiple times.
            ctx.put(TestSetupSubscriberServices.class, false);
        }
        else
        {
            LogSupport.debug(ctx, TestSetupSubscriberServices.class.getName(), 
                    "Skipping TestSetupSubscriberServices.setup since it has already been run.");
        }
    }
    
    public static void completelyTearDown(Context ctx)
    {
        
    }
    
    private static void setupSubscriberServicesHome(Context ctx)
    {
        ctx.put(SubscriberServicesHome.class, new TransientFieldResettingHome(ctx, new SubscriberServicesTransientHome(ctx)));
    }
    
    private static void decorateSubscriberPipeline(Context ctx)
    {
        Home subHome = (Home) ctx.get(SubscriberHome.class);
        subHome = new HomeProxy(subHome)
        {
            /* This subscriber home decorator will create the 
             * SubscriberService records when Subscriber is stored with 
             * servicesForDisplay set.  This field is a transient field
             * and will not normally be saved in to to the "persistent"
             * datastore.
             */
            @Override
            public Object store(Context ctx, Object obj)
                throws HomeException
            {
                Subscriber sub = (Subscriber) obj;
                if (sub.getAllNonUnprovisionedStateServices() != null)
                {
                    Iterator i = sub.getAllNonUnprovisionedStateServices().iterator();
                    while (i.hasNext())
                    {
                        /* Strictly speaking, sub.servicesForDisplay is supposed to be 
                         * a Set of SubscriberServiceDisplay beans, but this is a unit 
                         * test and I can take shortcuts until I can't. */
                        Long serviceId = (Long) i.next();
                        // Persist the ACTIVE Service
                        /* Have to avoid using SubscriberServicesSupport.createOrModifySubcriberService
                         * since it requires Subscriber.servicesForDisplay to be used properly. */
                        SubscriberServices record = new SubscriberServices();
                        record.setSubscriberId(sub.getId());
                        record.setServiceId(serviceId);
                        record.setProvisionedState(ServiceStateEnum.PROVISIONED);
                        record.setStartDate(TestSetupAccountHierarchy.START_DATE);
                        Home home = (Home) ctx.get(SubscriberServicesHome.class);
                        home.create(record);
                        
                        LogSupport.debug(ctx, TestSetupSubscriberServices.class.getName(), 
                                "Created SubscriberService record " + record.toString());
                    }
                }
                return super.store(ctx, obj);
            }
        };
        
        ctx.put(SubscriberHome.class, subHome);
    }
    
    public static void provisionSubscriberServices(Context ctx)
        throws HomeException
    {
        //For all existing subscribers provision the Voice and Generic services.
        final Home subHome = (Home) ctx.get(SubscriberHome.class); 
        Collection<Subscriber> subscribers = subHome.selectAll();
        for (Subscriber sub : subscribers)
        {
            HashSet servicesForProvisioning = new HashSet();
            servicesForProvisioning.add(TestSetupPricePlanAndServices.SERVICE_VOICE);
            servicesForProvisioning.add(TestSetupPricePlanAndServices.SERVICE_GENERIC);
            sub.setIntentToProvisionServices(servicesForProvisioning);
            subHome.store(sub);
            COUNT_PROVISIONS += 2;
        }
    }
    
    public void testSetup()
    {
        testSetup(getContext());
    }
    
    public static void testSetup(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(SubscriberServicesHome.class);
            Collection<SubscriberServices> provisionedServices = home.selectAll();
            assertEquals(COUNT_PROVISIONS, provisionedServices.size());
            EQ predicate = new EQ(SubscriberServicesXInfo.SERVICE_ID, TestSetupPricePlanAndServices.SERVICE_VOICE);
            Collection<SubscriberServices> voiceOnly = CollectionSupportHelper.get(ctx).findAll(ctx, provisionedServices, predicate);
            assertEquals(COUNT_PROVISIONS/2, voiceOnly.size());
            predicate = new EQ(SubscriberServicesXInfo.SERVICE_ID, TestSetupPricePlanAndServices.SERVICE_GENERIC);
            Collection<SubscriberServices> genericOnly = CollectionSupportHelper.get(ctx).findAll(ctx, provisionedServices, predicate);
            assertEquals(COUNT_PROVISIONS/2, genericOnly.size());
        }
        catch(HomeException e)
        {
            fail("Failed test " + e.getMessage());
        }
    }
    
    private static int COUNT_PROVISIONS = 0;
}
