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
package com.trilogy.app.crm.provision.soap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

/** 
 * @author shailesh.kushwaha@redknee.com
 * Code ported by amit.baid@redknee.com
 */
public class TestSubscriberInterfaceImpl extends ContextAwareTestCase {

	/**
	 * @param name
	 */
	public TestSubscriberInterfaceImpl(String name) {
		super(name);
	}

	/**
	 * Creates a new suite of Tests for execution.  This method is intended to
	 * be invoked by standard JUnit tools (i.e., those that do not provide a
	 * context).
	 *
	 * @return A new suite of Tests for execution.
	 */
	public static Test suite() {
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
	public static Test suite(final Context context) {
		setParentContext(context);

		final TestSuite suite = new TestSuite(TestSubscriberInterfaceImpl.class);

		return suite;
	}

	public void setUp() {
		super.setUp();
	}

	public void tearDown() {
		super.tearDown();
	}

}
