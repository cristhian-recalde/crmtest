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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
public class AutoCreatePropertiesSpidHomeTest extends ContextAwareTestCase
{

	/**
	 * Constructs a test case with the given name.
	 *
	 * @param name The name of the test.
	 */
	public AutoCreatePropertiesSpidHomeTest(final String name)
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

		final TestSuite suite = new TestSuite(AutoCreatePropertiesSpidHomeTest.class);

		return suite;
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	{
		super.setUp();

		getContext().put(TaxAuthorityHome.class, new TaxAuthorityTransientHome(getContext()));
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	{
		super.tearDown();
	}

	//createObjectList
	public void testCreateObjectList() throws Exception
	{
		AutoCreatePropertiesSpidHome acpsh = new AutoCreatePropertiesSpidHome(getContext(), null);
		int SPID = 17;

		List list = new ArrayList();
		{
			TaxAuthority ta = new TaxAuthority();
			ta.setIdentifier(0); //id should be 0, since TaxAuthorityHome will not auto generate id
			ta.setTaxAuthName("Tax1");

			list.add(ta);
		}

		{
			TaxAuthority ta = new TaxAuthority();
			ta.setIdentifier(1); //id should be overrided
			ta.setTaxAuthName("Tax2");

			list.add(ta);
		}

		acpsh.createObjectList(getContext(), SPID, list);
		Home home = (Home) getContext().get(TaxAuthorityHome.class);
		Collection cl = home.selectAll(getContext());

		assertTrue(cl.size() == 2);

		for (Iterator it = cl.iterator(); it.hasNext();)
		{
			TaxAuthority ta = (TaxAuthority) it.next();
			if (ta.getTaxId() == 0)
			{
				assertEquals("Tax1", ta.getTaxAuthName());
			}
			else
			{
				assertTrue(ta.getTaxId() != 0);
				assertEquals("Tax2", ta.getTaxAuthName());
			}

			assertTrue(SPID == ta.getSpid());
		}
	}

}
