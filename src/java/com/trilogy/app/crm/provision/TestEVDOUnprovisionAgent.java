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
package com.trilogy.app.crm.provision;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


/**
 * A suite of tests for the EVDOUnprovisionAgent.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestEVDOUnprovisionAgent
    extends ContextAwareTestCase
{
    /**
     * Creates a new TestEVDOUnprovisionAgent.
     *
     * @param name The name of the set of tests.
     */
    public TestEVDOUnprovisionAgent(final String name)
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

        final TestSuite suite = new TestSuite(TestEVDOUnprovisionAgent.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
        UnitTestSupport.installLicenseManager(getContext());
        
        IpcProvConfig config = new IpcProvConfig();
        getContext().put(IpcProvConfig.class, config);

        AAAClientFactory.installClient(getContext());
        IpcgClientFactory.installIpcgClient(getContext());

        try
        {
            UnitTestSupport.createLicense(getContext(), "DEV - Test AAA Client");
            UnitTestSupport.createLicense(getContext(), "DEV - Test IPCG Client");
        }
        catch (final HomeException exception)
        {
            final IllegalStateException newException = new IllegalStateException();
            newException.initCause(exception);
            throw newException;
        }
    }
    

    /**
     * Tests that IPCG is unprovisioned with EVDO.
     */
    public void testIPCGUnprovisioned()
        throws AgentException, HomeException
    {
        // Install PricePlanVersion Home and one plan.
        {
            final Home home = new AdapterHome(
                    getContext(), 
                    new PricePlanVersionTransientHome(getContext()), 
                    new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                            com.redknee.app.crm.bean.PricePlanVersion.class, 
                            com.redknee.app.crm.bean.core.PricePlanVersion.class));
            getContext().put(PricePlanVersionHome.class, home);

            final PricePlanVersion plan = new PricePlanVersion();
            plan.setId(5);
            plan.setVersion(7);
            
            final ServicePackageVersion localPackage = plan.getServicePackageVersion();
            localPackage.setId(13);
            localPackage.setVersion(14);

            for (int n = 0; n < 5; ++n)
            {
                final ServiceFee2 service = new ServiceFee2();
                service.setServiceId(n);
                service.setServicePreference(ServicePreferenceEnum.MANDATORY);
                localPackage.getServiceFees().put(Long.valueOf(service.getServiceId()), service);
            }
            
            home.create(plan);
        }

        // Install the ServicePackageHome.
        {
            final Home home = new ServicePackageTransientHome(getContext());
            getContext().put(ServicePackageHome.class, home);
        }

        // Install the Service Home.
        {
            final Home home = new ServiceTransientHome(getContext());
            getContext().put(ServiceHome.class, home);

            for (int n = 0; n < 5; ++n)
            {
                final Service service = new Service();
                service.setID(n);
                service.setName("Test Service #" + n);
                service.setTechnology(TechnologyEnum.CDMA);
                service.setType(ServiceTypeEnum.GENERIC);

                home.create(getContext(), service);
            }
        }

        final Service service = new Service();
        service.setID(123);
        service.setName("Test Service");
        service.setType(ServiceTypeEnum.EVDO);
        service.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Service.class, service);

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("1232343485-2");
        subscriber.setBAN("1232343485");
        subscriber.setPricePlan(5);
        subscriber.setPricePlanVersion(7);
        subscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Subscriber.class, subscriber);
        
        final EVDOUnprovisionAgent agent = new EVDOUnprovisionAgent();

        assertFalse(
            "The subscriber shouldn't appear to have a separate Data service.",
            agent.isSeparateDataServiceSelected(getContext()));

        // Just for fun, run the execution.
        agent.execute(getContext());
    }
    

    /**
     * Tests that IPCG is NOT unprovisioned from IPCG when EVDO is unprovisioned
     * if the subscriber has a separate Data service.
     */
    public void testIPCGNotUnprovisionedWithSeparateDataService()
        throws AgentException, HomeException
    {
        // Install PricePlanVersion Home and one plan.
        {
            final Home home = new AdapterHome(
                    getContext(), 
                    new PricePlanVersionTransientHome(getContext()), 
                    new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                            com.redknee.app.crm.bean.PricePlanVersion.class, 
                            com.redknee.app.crm.bean.core.PricePlanVersion.class));
            getContext().put(PricePlanVersionHome.class, home);

            final PricePlanVersion plan = new PricePlanVersion();
            plan.setId(5);
            plan.setVersion(7);
            
            final ServicePackageVersion localPackage = plan.getServicePackageVersion();
            localPackage.setId(13);
            localPackage.setVersion(14);

            for (int n = 0; n < 5; ++n)
            {
                final ServiceFee2 service = new ServiceFee2();
                service.setServiceId(n);
                service.setServicePreference(ServicePreferenceEnum.MANDATORY);
                localPackage.getServiceFees().put(Long.valueOf(service.getServiceId()), service);
            }
            
            home.create(plan);
        }

        // Install the ServicePackageHome.
        {
            final Home home = new ServicePackageTransientHome(getContext());
            getContext().put(ServicePackageHome.class, home);
        }

        // Install the Service Home.
        {
            final Home home = new ServiceTransientHome(getContext());
            getContext().put(ServiceHome.class, home);

            for (int n = 0; n < 5; ++n)
            {
                final Service service = new Service();
                service.setID(n);
                service.setName("Test Service #" + n);
                service.setTechnology(TechnologyEnum.CDMA);
                
                if (n == 3)
                {
                    service.setType(ServiceTypeEnum.DATA);
                }
                else
                {
                    service.setType(ServiceTypeEnum.GENERIC);
                }

                home.create(getContext(), service);
            }
        }

        final Service service = new Service();
        service.setID(123);
        service.setName("Test Service");
        service.setType(ServiceTypeEnum.EVDO);
        service.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Service.class, service);

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("1232343485-2");
        subscriber.setBAN("1232343485");
        subscriber.setPricePlan(5);
        subscriber.setPricePlanVersion(7);
        subscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Subscriber.class, subscriber);
        
        final EVDOUnprovisionAgent agent = new EVDOUnprovisionAgent();

        assertTrue(
            "The subscriber should appear to have a separate Data services.",
            agent.isSeparateDataServiceSelected(getContext()));

        // Just for fun, run the execution.
        agent.execute(getContext());
    }
    
} // class
