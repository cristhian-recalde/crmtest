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
package com.trilogy.app.crm.home.sub;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


/**
 * A suite of tests for the AAAProfileUpdateHome.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestAAAProfileUpdateHome
    extends ContextAwareTestCase
{
    /**
     * Creates a new TestAAAProfileUpdateHome.
     *
     * @param name The name of the set of tests.
     */
    public TestAAAProfileUpdateHome(final String name)
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

        final TestSuite suite = new TestSuite(TestAAAProfileUpdateHome.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
        UnitTestSupport.installLicenseManager(getContext());

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());

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

        // Install PricePlanVersion Home and one plan.
        {
            final Home home = new AdapterHome(
                    getContext(), 
                    new PricePlanVersionTransientHome(getContext()), 
                    new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                            com.redknee.app.crm.bean.PricePlanVersion.class, 
                            com.redknee.app.crm.bean.core.PricePlanVersion.class));
            getContext().put(PricePlanVersionHome.class, home);

            // Create a plan with no EVDO service.
            {
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

                try
                {
                    home.create(plan);
                }
                catch (final HomeException exception)
                {
                    final IllegalStateException newException = new IllegalStateException();
                    newException.initCause(exception);
                    throw newException;
                }
            }

            // Create a plan with EVDO service.
            {
                final PricePlanVersion plan = new PricePlanVersion();
                plan.setId(6);
                plan.setVersion(7);

                final ServicePackageVersion localPackage = plan.getServicePackageVersion();
                localPackage.setId(13);
                localPackage.setVersion(14);

                for (int n = 0; n < 6; ++n)
                {
                    final ServiceFee2 service = new ServiceFee2();
                    service.setServiceId(n);
                    service.setServicePreference(ServicePreferenceEnum.MANDATORY);
                    localPackage.getServiceFees().put(Long.valueOf(service.getServiceId()), service);
                }

                try
                {
                    home.create(plan);
                }
                catch (final HomeException exception)
                {
                    final IllegalStateException newException = new IllegalStateException();
                    newException.initCause(exception);
                    throw newException;
                }
            }
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

            for (int n = 0; n < 6; ++n)
            {
                final Service service = new Service();
                service.setID(n);
                service.setName("Test Service #" + n);
                service.setTechnology(TechnologyEnum.CDMA);

                if (n == 5)
                {
                    service.setType(ServiceTypeEnum.EVDO);
                }
                else
                {
                    service.setType(ServiceTypeEnum.GENERIC);
                }

                try
                {
                    home.create(getContext(), service);
                }
                catch (final HomeException exception)
                {
                    final IllegalStateException newException = new IllegalStateException();
                    newException.initCause(exception);
                    throw newException;
                }
            }
        }
    }


    /**
     * Tests that a subscriber with no EVDO service does not have its profile
     * updated.
     */
    public void testSubscriberWithoutEVDONotUpdated()
        throws HomeException
    {
        final AAAProfileUpdateTestingHome home = new AAAProfileUpdateTestingHome();

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("1232343485-2");
        subscriber.setBAN("1232343485");
        subscriber.setPricePlan(5);
        subscriber.setPricePlanVersion(7);
        subscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Lookup.OLDSUBSCRIBER, subscriber);

        assertFalse(
            "The hasAAAProfile() method should not yet have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());

        home.store(getContext(), subscriber);

        assertTrue(
            "The hasAAAProfile() method should have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());
    }


    /**
     * Tests that a subscriber being given an EVDO service does not have its
     * profile updated, since the initial provisioning will do that.
     */
    public void testOldSubscriberWithoutEVDONotUpdated()
        throws HomeException
    {
        final AAAProfileUpdateTestingHome home = new AAAProfileUpdateTestingHome();

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setId("1232343485-2");
        newSubscriber.setBAN("1232343485");
        newSubscriber.setPricePlan(6);
        newSubscriber.setPricePlanVersion(7);
        newSubscriber.setTechnology(TechnologyEnum.CDMA);

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setId("1232343485-2");
        oldSubscriber.setBAN("1232343485");
        oldSubscriber.setPricePlan(5);
        oldSubscriber.setPricePlanVersion(7);
        oldSubscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        assertFalse(
            "The hasAAAProfile() method should not yet have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());

        assertTrue(
            "The new subscriber should have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), newSubscriber, ServiceTypeEnum.EVDO));

        assertFalse(
            "The old subscriber should not have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), oldSubscriber, ServiceTypeEnum.EVDO));

        home.store(getContext(), newSubscriber);

        assertTrue(
            "The hasAAAProfile() method should have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());
    }


    /**
     * Tests that a subscriber having EVDO service taken away does not have its
     * profile updated, since the unprovisioning will remove the profile.
     */
    public void testNewSubscriberWithoutEVDONotUpdated()
        throws HomeException
    {
        final AAAProfileUpdateTestingHome home = new AAAProfileUpdateTestingHome();

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setId("1232343485-2");
        newSubscriber.setBAN("1232343485");
        newSubscriber.setPricePlan(5);
        newSubscriber.setPricePlanVersion(7);
        newSubscriber.setTechnology(TechnologyEnum.CDMA);

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setId("1232343485-2");
        oldSubscriber.setBAN("1232343485");
        oldSubscriber.setPricePlan(6);
        oldSubscriber.setPricePlanVersion(7);
        oldSubscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        assertFalse(
            "The hasAAAProfile() method should not yet have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());

        assertFalse(
            "The new subscriber should not have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), newSubscriber, ServiceTypeEnum.EVDO));

        assertTrue(
            "The old subscriber should have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), oldSubscriber, ServiceTypeEnum.EVDO));

        home.store(getContext(), newSubscriber);

        assertTrue(
            "The hasAAAProfile() method should have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());
    }


    /**
     * Tests that a subscriber with EVDO service has its profile updated.
     */
    public void testSubscriberWithEVDOUpdated()
        throws HomeException
    {
        final AAAProfileUpdateTestingHome home = new AAAProfileUpdateTestingHome();

        final Subscriber newSubscriber = new Subscriber();
        newSubscriber.setId("1232343485-2");
        newSubscriber.setBAN("1232343485");
        newSubscriber.setPricePlan(6);
        newSubscriber.setPricePlanVersion(7);
        newSubscriber.setTechnology(TechnologyEnum.CDMA);

        final Subscriber oldSubscriber = new Subscriber();
        oldSubscriber.setId("1232343485-2");
        oldSubscriber.setBAN("1232343485");
        oldSubscriber.setPricePlan(6);
        oldSubscriber.setPricePlanVersion(7);
        oldSubscriber.setTechnology(TechnologyEnum.CDMA);
        getContext().put(Lookup.OLDSUBSCRIBER, oldSubscriber);

        assertFalse(
            "The hasAAAProfile() method should not yet have been called.",
            home.getHasAAAProfileCalled());

        assertFalse(
            "The updateAAAProfile() method should not have been called.",
            home.getUpdateAAAProfileCalled());

        assertTrue(
            "The new subscriber should have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), newSubscriber, ServiceTypeEnum.EVDO));

        assertTrue(
            "The old subscriber should have the EVDO service.",
            ServiceSupport.isServiceSelected(getContext(), oldSubscriber, ServiceTypeEnum.EVDO));

        home.store(getContext(), newSubscriber);

        assertTrue(
            "The hasAAAProfile() method should have been called.",
            home.getHasAAAProfileCalled());

        assertTrue(
            "The updateAAAProfile() method should have been called.",
            home.getUpdateAAAProfileCalled());
    }


    /**
     * Tests that a HomeException is thrown if the old subscriber is not
     * provided in the context and not available in the Home.
     */
    public void testNoSubscriberThrowsHomeException()
        throws HomeException
    {
        final AAAProfileUpdateTestingHome home = new AAAProfileUpdateTestingHome();

        final Subscriber subscriber = new Subscriber();
        subscriber.setId("123123123-2");

        getContext().put(SubscriberHome.class, new SubscriberTransientHome(getContext()));

        try
        {
            home.store(getContext(), subscriber);
            fail("The store() method should have thrown a HomeException.");
        }
        catch (final HomeException exception)
        {
            // Empty
        }
    }


    /**
     * This class makes testing easier by promoting protected methods to public,
     * and by providing indications of whether or not methods were called.
     */
    private static final
    class AAAProfileUpdateTestingHome
        extends AAAProfileUpdateHome
    {
        /**
         * Creates a new AAAProfileUpdateTestingHome that delegates to a simple
         * null home.
         */
        public AAAProfileUpdateTestingHome()
        {
            super(
                new HomeProxy()
                {
                    public Object store(final Context context, final Object object)
                    {
                        return object;
                    }
               });
        }

        public boolean hasAAAProfile(
            final Context context,
            final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
            throws HomeException
        {
            hasAAAProfileCalled_ = true;
            return super.hasAAAProfile(context, oldSubscriber, newSubscriber);
        }

        public boolean getHasAAAProfileCalled()
        {
            return hasAAAProfileCalled_;
        }

        public void updateAAAProfile(
            final Context context,
            final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
            throws AAAClientException
        {
            updateAAAProfileCalled_ = true;
            super.updateAAAProfile(context, oldSubscriber, newSubscriber);
        }

        public boolean getUpdateAAAProfileCalled()
        {
            return updateAAAProfileCalled_;
        }

        private boolean hasAAAProfileCalled_ = false;
        private boolean updateAAAProfileCalled_ = false;
    }


} // class
