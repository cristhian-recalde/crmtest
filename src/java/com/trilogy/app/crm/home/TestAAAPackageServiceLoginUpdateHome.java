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
package com.trilogy.app.crm.home;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageTransientHome;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackageTransientHome;
import com.trilogy.app.crm.client.aaa.AAAClientException;
import com.trilogy.app.crm.client.aaa.AAAClientFactory;
import com.trilogy.app.crm.client.aaa.AAATestClient;
import com.trilogy.app.crm.client.aaa.AAATestClientSwitch;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;


/**
 * Unit tests for AAAPackageServiceLoginUpdateHome.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestAAAPackageServiceLoginUpdateHome
    extends ContextAwareTestCase
{

    /**
     * Creates a new TestAAAPackageServiceLoginUpdateHome.
     *
     * @param name The name of the set of tests.
     */
    public TestAAAPackageServiceLoginUpdateHome(final String name)
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

        final TestSuite suite = new TestSuite(TestAAAPackageServiceLoginUpdateHome.class);

        return suite;
    }


    /**
     * Adds package homes to the default context.  Installs the AAA client into
     * the default context.  Installs the License Manager into the default
     * context.  Enables the AAA debugging license.  Installs the
     * SystemStatusRequestServicer key set.
     */
    public void setUp()
    {
        super.setUp();

        UnitTestSupport.installLicenseManager(getContext());

        try
        {
            UnitTestSupport.createLicense(getContext(), AAATestClientSwitch.LICENSE_KEY);
        }
        catch (final HomeException exception)
        {
            final RuntimeException newException =
                new IllegalStateException("Failed to create license for test.");

            newException.initCause(exception);
            throw newException;
        }

        AAAClientFactory.installClient(getContext());

        // Add the GSMPackageHome
        {
            Home home = new GSMPackageTransientHome(getContext());
            home = new AAAPackageServiceLoginUpdateHome(home);
            home = new OldGSMPackageLookupHome(getContext(), home);

            getContext().put(GSMPackageHome.class, home);
        }

        // Add the TDMAPackageHome
        {
            Home home = new TDMAPackageTransientHome(getContext());
            home = new AAAPackageServiceLoginUpdateHome(home);
            home = new OldTDMAPackageLookupHome(getContext(), home);

            getContext().put(TDMAPackageHome.class, home);
        }
    }


    /**
     * Tests that the AAAClient.updateProfile(GSMPackage) method does not get
     * called when a GSMPackage is updated.
     */
    public void testUpdateProfileNotCalledForGSMPackageUpdate()
        throws HomeException
    {
        GSMPackage card = new GSMPackage();
        card.setPackId("Check");
        card.setState(PackageStateEnum.IN_USE);

        final AAATestCheckClient client = new AAATestCheckClient();
        getContext().put(AAATestClient.class, client);

        final Home home = (Home)getContext().get(GSMPackageHome.class);
        card = (GSMPackage)home.create(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not yet have been called.",
            client.wasUpdateProfileCalled());

        card.setServiceLogin1("check");
        card = (GSMPackage)home.store(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not have been called.",
            client.wasUpdateProfileCalled());
    }


    /**
     * Tests that the AAAClient.updateProfile(TDMAPackage) method does not get
     * called when a TDMAPackage is updated.
     */
    public void testUpdateProfileNotCalledForTDMAPackageUpdate()
        throws HomeException
    {
        TDMAPackage card = new TDMAPackage();
        card.setPackId("Check");
        card.setState(PackageStateEnum.IN_USE);
        card.setTechnology(TechnologyEnum.TDMA);

        final AAATestCheckClient client = new AAATestCheckClient();
        getContext().put(AAATestClient.class, client);

        final Home home = (Home)getContext().get(TDMAPackageHome.class);
        card = (TDMAPackage)home.create(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not yet have been called.",
            client.wasUpdateProfileCalled());

        card.setServiceLogin1("check");
        card = (TDMAPackage)home.store(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not have been called.",
            client.wasUpdateProfileCalled());
    }


    /**
     * Tests that the AAAClient.updateProfile(CDMAPackage) method gets called
     * when a CDMAPackage is updated.
     */
    public void testUpdateProfileCalledForCDMAPackageUpdate()
        throws HomeException
    {
        // CDMA cards are represented by a TDMAPackage with its technology set
        // to CDMA.
        TDMAPackage card = new TDMAPackage();
        card.setPackId("Check");
        card.setState(PackageStateEnum.IN_USE);
        card.setTechnology(TechnologyEnum.CDMA);
        card.setESN("234");

        final AAATestCheckClient client = new AAATestCheckClient();
        getContext().put(AAATestClient.class, client);

        final Home home = (Home)getContext().get(TDMAPackageHome.class);
        card = (TDMAPackage)home.create(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not yet have been called.",
            client.wasUpdateProfileCalled());

        card.setESN("789");
        card.setServiceLogin1("check");
        card = (TDMAPackage)home.store(getContext(), card);

        assertTrue(
            "The AAAClient.updateProfile(TDMAPackage) method should have been called.",
            client.wasUpdateProfileCalled());
    }


    /**
     * Tests that the AAAClient.updateProfile(CDMAPackage) method does not get called
     * when a CDMA Package is updated without a change in any of the service
     * login or service password fields.
     */
    public void testUpdateProfileNotCalledForCDMAPackageUpdateWithoutServiceLoginChange()
        throws HomeException
    {
        // CDMA cards are represented by a TDMAPackage with its technology set
        // to CDMA.
        TDMAPackage card = new TDMAPackage();
        card.setPackId("Check");
        card.setState(PackageStateEnum.IN_USE);
        card.setTechnology(TechnologyEnum.CDMA);
        card.setESN("234");

        final AAATestCheckClient client = new AAATestCheckClient();
        getContext().put(AAATestClient.class, client);

        final Home home = (Home)getContext().get(TDMAPackageHome.class);
        card = (TDMAPackage)home.create(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not yet have been called.",
            client.wasUpdateProfileCalled());

        // Note that although the ESN has changed, none of the service logins or
        // service passwords have.

        card.setESN("789");
        card = (TDMAPackage)home.store(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not have been called.",
            client.wasUpdateProfileCalled());
    }


    /**
     * Tests that the AAAClient.updateProfile(CDMAPackage) method does not get
     * called when a CDMAPackage is updated if it is not IN_USE.
     */
    public void testUpdateProfileNotCalledForCDMAPackageUpdateWhenNotInUse()
        throws HomeException
    {
        // CDMA cards are represented by a TDMAPackage with its technology set
        // to CDMA.
        TDMAPackage card = new TDMAPackage();
        card.setPackId("Check");
        card.setState(PackageStateEnum.AVAILABLE);
        card.setTechnology(TechnologyEnum.CDMA);
        card.setESN("234");

        final AAATestCheckClient client = new AAATestCheckClient();
        getContext().put(AAATestClient.class, client);

        final Home home = (Home)getContext().get(TDMAPackageHome.class);
        card = (TDMAPackage)home.create(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not yet have been called.",
            client.wasUpdateProfileCalled());

        card.setESN("789");
        card.setServiceLogin1("check");
        card = (TDMAPackage)home.store(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not have been called.",
            client.wasUpdateProfileCalled());

        card.setServiceLogin1("checkagain");
        card.setState(PackageStateEnum.HELD);
        card = (TDMAPackage)home.store(getContext(), card);

        assertFalse(
            "The AAAClient.updateProfile(TDMAPackage) method should not have been called.",
            client.wasUpdateProfileCalled());

        card.setServiceLogin1("checkoncemore");
        card.setState(PackageStateEnum.IN_USE);
        card = (TDMAPackage)home.store(getContext(), card);

        assertTrue(
            "The AAAClient.updateProfile(TDMAPackage) method should have been called.",
            client.wasUpdateProfileCalled());
    }


    /**
     * Tests that the hasServiceLoginDataChanged() properly returns true when
     * any of the service login or service password fields have changed.
     */
    public void testHasServiceLoginDataChanged()
    {
        final AAAPackageServiceLoginUpdateHome home =
            new AAAPackageServiceLoginUpdateHome(null);

        // No Changes
        {
            final TDMAPackage card1 = new TDMAPackage();
            card1.setServiceLogin1("ServiceLogin1");
            card1.setServicePassword1("ServicePassword1");
            card1.setServiceLogin2("ServiceLogin2");
            card1.setServicePassword2("ServicePassword2");

            final TDMAPackage card2 = new TDMAPackage();
            card2.setServiceLogin1("ServiceLogin1");
            card2.setServicePassword1("ServicePassword1");
            card2.setServiceLogin2("ServiceLogin2");
            card2.setServicePassword2("ServicePassword2");

            assertFalse(
                "The cards should not appear to have any service login differences.",
                home.hasServiceDataChanged(getContext(), card1, card2));
        }

        // Service Login 1 Update
        {
            final TDMAPackage card1 = new TDMAPackage();
            card1.setServiceLogin1("ServiceLogin1");
            card1.setServicePassword1("ServicePassword1");
            card1.setServiceLogin2("ServiceLogin2");
            card1.setServicePassword2("ServicePassword2");

            final TDMAPackage card2 = new TDMAPackage();
            card2.setServiceLogin1("changed");
            card2.setServicePassword1("ServicePassword1");
            card2.setServiceLogin2("ServiceLogin2");
            card2.setServicePassword2("ServicePassword2");

            assertTrue(
                "The cards should appear to have service login differences.",
                home.hasServiceDataChanged(getContext(), card1, card2));
        }

        // Service Password 1 Update
        {
            final TDMAPackage card1 = new TDMAPackage();
            card1.setServiceLogin1("ServiceLogin1");
            card1.setServicePassword1("ServicePassword1");
            card1.setServiceLogin2("ServiceLogin2");
            card1.setServicePassword2("ServicePassword2");

            final TDMAPackage card2 = new TDMAPackage();
            card2.setServiceLogin1("ServiceLogin1");
            card2.setServicePassword1("changed");
            card2.setServiceLogin2("ServiceLogin2");
            card2.setServicePassword2("ServicePassword2");

            assertTrue(
                "The cards should appear to have service login differences.",
                home.hasServiceDataChanged(getContext(), card1, card2));
        }

        // Service Login 2 Update
        {
            final TDMAPackage card1 = new TDMAPackage();
            card1.setServiceLogin1("ServiceLogin1");
            card1.setServicePassword1("ServicePassword1");
            card1.setServiceLogin2("ServiceLogin2");
            card1.setServicePassword2("ServicePassword2");

            final TDMAPackage card2 = new TDMAPackage();
            card2.setServiceLogin1("ServiceLogin1");
            card2.setServicePassword1("ServicePassword1");
            card2.setServiceLogin2("changed");
            card2.setServicePassword2("ServicePassword2");

            assertTrue(
                "The cards should appear to have service login differences.",
                home.hasServiceDataChanged(getContext(), card1, card2));
        }

        // Service Password 2 Update
        {
            final TDMAPackage card1 = new TDMAPackage();
            card1.setServiceLogin1("ServiceLogin1");
            card1.setServicePassword1("ServicePassword1");
            card1.setServiceLogin2("ServiceLogin2");
            card1.setServicePassword2("ServicePassword2");

            final TDMAPackage card2 = new TDMAPackage();
            card2.setServiceLogin1("ServiceLogin1");
            card2.setServicePassword1("ServicePassword1");
            card2.setServiceLogin2("ServiceLogin2");
            card2.setServicePassword2("changed");

            assertTrue(
                "The cards should appear to have service login differences.",
                home.hasServiceDataChanged(getContext(), card1, card2));
        }

    }


    /**
     * Provides a testing client that we can use to check whether or not AAA is
     * used.
     */
    private static final
    class AAATestCheckClient
        extends AAATestClient
    {
        public void updateProfile(
            final Context context,
            final TDMAPackage oldPackage,
            final TDMAPackage newPackage)
            throws AAAClientException
        {
            super.updateProfile(context, oldPackage, newPackage);

            updateProfileCalled_ = true;

            assertEquals(
                "The package identifier should be the same.",
                oldPackage.getPackId(),
                newPackage.getPackId());
        }

        public boolean wasUpdateProfileCalled()
        {
            return updateProfileCalled_;
        }

        private boolean updateProfileCalled_ = false;

    } // inner-class

} // class
