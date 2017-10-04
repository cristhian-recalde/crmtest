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
package com.trilogy.app.crm.filter;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;


/**
 * Tests that the SubscriptionTypeEnumLicensePredicate filters properly.
 *
 * @author gary.anderson@redknee.com
 */
public class TestSubscriptionTypeEnumLicensePredicate
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSubscriptionTypeEnumLicensePredicate(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be
     * invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestSubscriptionTypeEnumLicensePredicate.class);

        return suite;
    }


    /**
     * Tests that all expected SubscriptionTypeEnums have a corresponding
     * license key. If this test fails, then the predicate needs to be updated
     * to include a license for one or more SubscriptionTypeEnum entries.
     *
     * @throws HomeException Thrown if there are problems accessing license data
     * in the Context.
     */
    public void testAllExpectedTypesHaveLicenses()
        throws HomeException
    {
        final Iterator iterator = SubscriptionTypeEnum.COLLECTION.iterator();
        while (iterator.hasNext())
        {
            final SubscriptionTypeEnum type = (SubscriptionTypeEnum)iterator.next();
            final String licenseName = SubscriptionTypeEnumLicensePredicate.LICENSE_NAMES.get(type);

            assertTrue(type + " has a license name.", licenseName != null);
            assertTrue(type + " has a non-blank license name.", licenseName.trim().length() > 0);
        }
    }


    /**
     * Tests that types are available from the collection when licensed.
     *
     * @throws HomeException Thrown if there are problems accessing License data
     * in the Context.
     */
    public void testLicensingWorks()
        throws HomeException
    {
        // Note that I only test airtime here. No need to test all.

        // Want to temporarily override any existing LicenseMgr so that we start
        // with no licenses.
        final Context context = getContext();
        UnitTestSupport.installLicenseManager(context);

        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);
        assertFalse("Airtime not licensed.", manager.isLicensed(context, LicenseConstants.AIRTIME_LICENSE_KEY));

        final SubscriptionTypeEnumLicensePredicate predicate = new SubscriptionTypeEnumLicensePredicate();
        {
            final EnumCollection where = SubscriptionTypeEnum.COLLECTION.where(context, predicate);
            assertEquals("Set size is zero.", 0, where.getSize());
        }

        UnitTestSupport.createLicense(context, LicenseConstants.AIRTIME_LICENSE_KEY);
        {
            final EnumCollection where = SubscriptionTypeEnum.COLLECTION.where(context, predicate);
            assertEquals("Set size is one.", 1, where.getSize());
            assertEquals("Only allowed type is Airtime.", SubscriptionTypeEnum.AIRTIME, where.iterator().next());
        }
    }
}
