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

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestTransientHome;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;

/**
 * A suite of test cases for PricePlanVersionHomeProxy.
 * 
 * @author gary.anderson@redknee.com
 */
public class TestPricePlanVersionHomeProxy extends ContextAwareTestCase
{
	/**
	 * Constructs a test case with the given name.
	 * 
	 * @param name
	 *            The name of the test.
	 */
	public TestPricePlanVersionHomeProxy(final String name)
	{
		super(name);
	}

	/**
	 * Creates a new suite of Tests for execution. This method is intended to
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
	 * Creates a new suite of Tests for execution. This method is intended to
	 * be invoked by the Redknee Xtest code, which provides the application's
	 * operating context.
	 * 
	 * @param context
	 *            The operating context.
	 * @return A new suite of Tests for execution.
	 */
	public static Test suite(final Context context)
	{
		setParentContext(context);

		final TestSuite suite =
		    new TestSuite(TestPricePlanVersionHomeProxy.class);

		return suite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUp()
	{
		super.setUp();
		
        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void tearDown()
	{
		super.tearDown();
	}

	/**
	 * Tests that a HomeException is thrown when no PricePlanHome is found in
	 * the context.
	 */
	public void testHomeExceptionForNoPricePlanHome()
	{
		final Home home =
		    new PricePlanVersionHomeProxy(getContext(),
		            new AdapterHome(
		                    getContext(), 
		                    new PricePlanVersionTransientHome(getContext()), 
		                    new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
		                            com.redknee.app.crm.bean.PricePlanVersion.class, 
		                            com.redknee.app.crm.bean.core.PricePlanVersion.class)));

		try
		{
			final PricePlanVersion version = new PricePlanVersion();
			version.setId(5);
			version.setVersion(14);
			version.setActivateDate(new Date());

			home.create(getContext(), version);
			fail("No PricePlanHome should have raised an exception.");
		}
		catch (final HomeException exception)
		{
			// Empty
		}
	}

	/**
	 * Tests that the proxy home explicitly disallows active versions from being
	 * stored.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated exception
	 *                raised while accessing Home data in the context.
	 */
	public void testVersionUpdate() throws HomeException
	{
		final Home planHome = new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setVoiceRatePlan("RP31");
			plan.setCurrentVersion(13);
			plan.setNextVersion(14);

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));

			final PricePlanVersion version = new PricePlanVersion();
			version.setId(5);
			version.setVersion(13);
			version.setActivation(new Date());
			version.setCreditLimit(1000L);

			baseHome.create(getContext(), version);
			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);

			getContext().put(PricePlanVersionHome.class, versionHome);
		}

		final PricePlanVersionID identifier = new PricePlanVersionID(5, 13);

		{
			final PricePlanVersion version =
			    (PricePlanVersion) versionHome.find(getContext(), identifier);

			assertNotNull("Version exists.", version);
			assertEquals("Version Credit Limit.", 1000L,
			    version.getCreditLimit());

			version.setCreditLimit(99);
			assertEquals("Version Credit Limit.", 99, version.getCreditLimit());

			try
			{
				versionHome.store(getContext(), version);
				fail("Attempt to store (modify) version should raise exception.");
			}
			catch (final HomeException exception)
			{
				// Empty
			}
		}

		{
			final PricePlanVersion version =
			    (PricePlanVersion) versionHome.find(getContext(), identifier);

			assertNotNull("Version exists.", version);
			assertEquals("Version Credit Limit.", 1000L,
			    version.getCreditLimit());
		}

		{
			final PricePlanVersion version =
			    (PricePlanVersion) versionHome.find(getContext(), identifier);

			assertNotNull("Version exists.", version);
			assertNotNull("Version activation date.", version.getActivation());

			version.setActivation(UnitTestSupport
			    .parseDate("2001-01-12 12:34:45.567"));

			try
			{
				versionHome.store(getContext(), version);
				fail("Attempt to store (modify) version should raise exception.");
			}
			catch (final HomeException exception)
			{
				// Empty
			}
		}
	}

	/**
	 * Tests that the proxy home allows versions to be activated.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated exception
	 *                raied while accessing Home data in the context.
	 */
	public void testActivation() throws HomeException
	{
		getContext().put(ServicePackageHome.class,
		    new ServicePackageTransientHome(getContext()));

		final Date now = new Date();
		final Home planHome =  new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setCurrentVersion(13);
			plan.setNextVersion(14);
			plan.setVoiceRatePlan("RP31");

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));

			Date yesterday =
			    CalendarSupportHelper.get(getContext()).getDayBefore(now);

			final PricePlanVersion version = new PricePlanVersion();
			version.setId(5);
			version.setVersion(13);
			version.setActivateDate(yesterday);

			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);

			getContext().put(PricePlanVersionHome.class, versionHome);

			baseHome.create(getContext(), version);
		}

		final PricePlanVersionID identifier = new PricePlanVersionID(5, 13);

		{
			final PricePlanVersion version =
			    (PricePlanVersion) versionHome.find(getContext(), identifier);

			assertNotNull("Version should exist.", version);
			assertNull("Version voice not activated.", version.getActivation());

			version.setActivation(now);

			versionHome.store(getContext(), version);
		}

		final PricePlanVersion version =
		    (PricePlanVersion) versionHome.find(getContext(), identifier);

		assertNotNull("Version exists.", version);
		assertEquals("Version activation date.", now, version.getActivation());
	}

	/**
	 * Tests that creation of a new version increments the nextVersion of the
	 * plan, but not the currentVersion.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated exception
	 *                raied while accessing Home data in the context.
	 */
	public void testNewVersionIncrementsNextVersionPropertyOnly()
	    throws HomeException
	{
		getContext().put(PricePlanVersionUpdateRequestHome.class,
		    new PricePlanVersionUpdateRequestTransientHome(getContext()));

		getContext().put(ServicePackageHome.class,
		    new ServicePackageTransientHome(getContext()));

		final Home planHome =  new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setCurrentVersion(13);
			plan.setNextVersion(17);

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));
			final PricePlanVersion firstVersion = new PricePlanVersion();
			firstVersion.setId(5);
			firstVersion.setVersion(16);
			firstVersion.setActivateDate(new Date());
			firstVersion.setActivation(new Date());
			baseHome.create(getContext(), firstVersion);

			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);
		}

		getContext().put(PricePlanVersionHome.class, versionHome);

		final PricePlanVersion proposedVersion = new PricePlanVersion();
		proposedVersion.setId(5);
		// This assigned version should be overridden by the home.
		proposedVersion.setVersion(3);
		proposedVersion.setActivateDate(UnitTestSupport
		    .parseDate("2010-01-02 01:12:35.678"));

		final PricePlanVersion createdVersion =
		    (PricePlanVersion) versionHome
		        .create(getContext(), proposedVersion);

		final PricePlan plan =
		    (PricePlan) planHome.find(getContext(), new EQ(PricePlanXInfo.ID,
		        Long.valueOf(5)));

		assertEquals("Version is correct.", 17, createdVersion.getVersion());
		assertEquals("Current version is unchanged.", 13,
		    plan.getCurrentVersion());
		assertEquals("Next version is updated.", 18, plan.getNextVersion());
	}

	/**
	 * Tests that the remove() methods throws an exception if an attempt is made
	 * to remove the most recent version, which happens to be active.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated problem
	 *                accessing data in the context.
	 */
	public void testRemoveActiveFalure() throws HomeException
	{
		final Home planHome =  new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setCurrentVersion(13);
			plan.setNextVersion(14);

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final PricePlanVersion version = new PricePlanVersion();
		version.setId(5);
		version.setVersion(13);

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));
			baseHome.create(getContext(), version);
			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);
		}

		assertNotNull("Version exists.",
		    versionHome.find(getContext(), version));

		try
		{
			versionHome.remove(getContext(), version);
			fail("Attempt to remove active, most recent version should raise exception.");
		}
		catch (final HomeException exception)
		{
			// Empty
		}

		assertNotNull("Version still exists.",
		    versionHome.find(getContext(), version));

		final PricePlan plan =
		    (PricePlan) planHome.find(getContext(), new EQ(PricePlanXInfo.ID,
		        Long.valueOf(5)));
		assertEquals("Plan next identifier not changed.", 14,
		    plan.getNextVersion());
	}

	/**
	 * Tests that the remove() methods allows removal of any version not yet
	 * activated.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated problem
	 *                accessing data in the context.
	 */
	public void testAllowRemoveUnactivatedVersions() throws HomeException
	{
		final Home planHome =  new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setCurrentVersion(13);
			plan.setNextVersion(17);

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final PricePlanVersion version = new PricePlanVersion();
		version.setId(5);
		version.setVersion(15);
		version.setActivateDate(CalendarSupportHelper.get(getContext())
		    .getDayAfter(new Date()));

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));
			baseHome.create(getContext(), version);
			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);
		}

		assertNotNull("Version exists.",
		    versionHome.find(getContext(), version));

		versionHome.remove(getContext(), version);

		assertNull("Version still exists.",
		    versionHome.find(getContext(), version));

		final PricePlan plan =
		    (PricePlan) planHome.find(getContext(), new EQ(PricePlanXInfo.ID,
		        Long.valueOf(5)));
		assertEquals("Plan next identifier not changed.", 17,
		    plan.getNextVersion());
	}

	/**
	 * Tests that the remove() methods successfully removes the most recently
	 * created, and inactive, version.
	 * 
	 * @exception HomeException
	 *                Thrown if there is an unanticipated problem
	 *                accessing data in the context.
	 */
	public void testRemoveSuccess() throws HomeException
	{
		final Home planHome =  new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
		{
			final PricePlan plan = new PricePlan();
			plan.setId(5);
			plan.setCurrentVersion(13);
			plan.setNextVersion(17);

			planHome.create(getContext(), plan);

			getContext().put(PricePlanHome.class, planHome);
		}

		final PricePlanVersion version = new PricePlanVersion();
		version.setId(5);
		version.setVersion(16);
		version.setActivateDate(new Date());

		final Home versionHome;
		{
			final Home baseHome =
			        new AdapterHome(
                            getContext(), 
                            new PricePlanVersionTransientHome(getContext()), 
                            new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                                    com.redknee.app.crm.bean.PricePlanVersion.class, 
                                    com.redknee.app.crm.bean.core.PricePlanVersion.class));
			baseHome.create(getContext(), version);
			versionHome = new PricePlanVersionHomeProxy(getContext(), baseHome);
			getContext().put(PricePlanVersionHome.class, versionHome);
		}

		assertNotNull("Version exists.",
		    versionHome.find(getContext(), version));

		versionHome.remove(getContext(), version);

		assertNull("Version removed.", versionHome.find(getContext(), version));

		final PricePlan plan =
		    (PricePlan) planHome.find(getContext(), new EQ(PricePlanXInfo.ID,
		        Long.valueOf(5)));

		assertEquals("Plan next identifier unchanged.", 17,
		    plan.getNextVersion());
	}

} // class
