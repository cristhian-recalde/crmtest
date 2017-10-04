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
package com.trilogy.app.crm.support;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityHome;
import com.trilogy.app.crm.bean.TaxAuthorityTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author jchen
 */
public class TestSpidSupport extends ContextAwareTestCase
{
	/**
	 * Constructs a test case with the given name.
	 *
	 * @param name The name of the test.
	 */
	public TestSpidSupport(final String name)
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

		final TestSuite suite = new TestSuite(TestSpidSupport.class);

		return suite;
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	{
		super.setUp();

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	{
		super.tearDown();
	}

	public void testCreateDefTaxAuthority() throws Exception
	{
		Home home = new TaxAuthorityTransientHome(getContext());
		getContext().put(TaxAuthorityHome.class, home);
		int SPID = 12;
		int id = 1;

		assertTrue(SpidSupport.getDefTaxAuthority(getContext(), SPID) == null);

		{
			TaxAuthority ta = new TaxAuthority();
			ta.setIdentifier(id++);
			ta.setTaxAuthName("Tax1");
			ta.setSpid(SPID);

			home.create(getContext(),ta);
		}

		assertTrue(SpidSupport.getDefTaxAuthority(getContext(), SPID - 1) == null);
		assertTrue(SpidSupport.getDefTaxAuthority(getContext(), SPID) != null);

		{
			TaxAuthority ta = new TaxAuthority();
			ta.setIdentifier(id++);
			ta.setTaxAuthName("Tax2");
			ta.setSpid(SPID);

			home.create(getContext(),ta);
		}

		assertTrue(SpidSupport.getDefTaxAuthority(getContext(), SPID - 1) == null);
		assertTrue(SpidSupport.getDefTaxAuthority(getContext(), SPID) != null);

	}
}
