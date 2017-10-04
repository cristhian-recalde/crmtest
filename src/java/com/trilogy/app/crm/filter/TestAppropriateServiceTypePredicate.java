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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.xenum.EnumCollection;


/**
 * Test the AppropriateServiceTypePredicate class.
 *
 * @author gary.anderson@redknee.com
 */
public class TestAppropriateServiceTypePredicate
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAppropriateServiceTypePredicate(final String name)
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

        final TestSuite suite = new TestSuite(TestAppropriateServiceTypePredicate.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());
    }

    /**
     * Tests that the AppropriateServiceTypePredicate is likely covering all
     * known ServiceTypeEnum values. IMPORTANT -- when this test was last
     * updated, the AppropriateServiceTypePredicate class was explicitly
     * designed to handle only the types checked in this test. If this test
     * fails, there is a good chance that AppropriateServiceTypePredicate needs
     * to be updated to explicitly handle one or more types.
     */
    public void testExpectedServiceTypes()
    {
        final Set<ServiceTypeEnum> expectedTypes = new HashSet<ServiceTypeEnum>();
        expectedTypes.add(ServiceTypeEnum.GENERIC);
        expectedTypes.add(ServiceTypeEnum.VOICEMAIL);
        expectedTypes.add(ServiceTypeEnum.VOICE);
        expectedTypes.add(ServiceTypeEnum.SMS);
        expectedTypes.add(ServiceTypeEnum.DATA);
        expectedTypes.add(ServiceTypeEnum.TRANSFER);
        expectedTypes.add(ServiceTypeEnum.EVDO);
        expectedTypes.add(ServiceTypeEnum.BLACKBERRY);
        expectedTypes.add(ServiceTypeEnum.ALCATEL_SSC);
        expectedTypes.add(ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY);

        for (int n = 0; n < ServiceTypeEnum.COLLECTION.getSize(); ++n)
        {
            final ServiceTypeEnum value = (ServiceTypeEnum)ServiceTypeEnum.COLLECTION.get((short)n);
            assertTrue("Unexpected ServiceTypeEnum: " + value, expectedTypes.contains(value));
        }
    }


    /**
     * Test that all expected Airtime, GSM services are appropriate.
     *
     * @throws HomeException Thrown if there is problems accessing home data in the context.
     */
    public void testAirtimeGSMServiceTypes()
        throws HomeException
    {
        final Context context = getContext();
        UnitTestSupport.createLicense(context, LicenseConstants.AIRTIME_LICENSE_KEY);

        final Set<ServiceTypeEnum> expectedTypes = new HashSet<ServiceTypeEnum>();
        expectedTypes.add(ServiceTypeEnum.GENERIC);
        expectedTypes.add(ServiceTypeEnum.VOICEMAIL);
        expectedTypes.add(ServiceTypeEnum.VOICE);
        expectedTypes.add(ServiceTypeEnum.SMS);
        expectedTypes.add(ServiceTypeEnum.DATA);
        expectedTypes.add(ServiceTypeEnum.TRANSFER);

        final Service service = new Service();
        service.setTechnology(TechnologyEnum.GSM);
        context.put(AbstractWebControl.BEAN, service);

        final EnumCollection actualTypes =
            ServiceTypeEnum.COLLECTION.where(context, new AppropriateServiceTypePredicate());

        assertEquals(
            "The count of expected types matches the count of found types.",
            expectedTypes.size(),
            actualTypes.size());

        for (int n = 0; n < actualTypes.getSize(); ++n)
        {
            final ServiceTypeEnum value = (ServiceTypeEnum)actualTypes.getElementAt(n);
            assertTrue("Unexpected ServiceTypeEnum: " + value, expectedTypes.contains(value));
            expectedTypes.remove(value);
        }
    }

    /**
     * Test that all expected non-Airtime, GSM services are appropriate.
     *
     * @throws HomeException Thrown if there is problems accessing home data in the context.
     */
    public void testNonAirtimeGSMServiceTypes()
        throws HomeException
    {
        final Context context = getContext();

        final Set<ServiceTypeEnum> expectedTypes = new HashSet<ServiceTypeEnum>();
        expectedTypes.add(ServiceTypeEnum.GENERIC);
        expectedTypes.add(ServiceTypeEnum.TRANSFER);

        final Service service = new Service();
        service.setTechnology(TechnologyEnum.GSM);
        context.put(AbstractWebControl.BEAN, service);

        final EnumCollection actualTypes =
            ServiceTypeEnum.COLLECTION.where(context, new AppropriateServiceTypePredicate());

        assertEquals(
            "The count of expected types matches the count of found types.",
            expectedTypes.size(),
            actualTypes.size());

        for (int n = 0; n < actualTypes.getSize(); ++n)
        {
            final ServiceTypeEnum value = (ServiceTypeEnum)actualTypes.get((short)n);
            assertTrue("Unexpected ServiceTypeEnum: " + value, expectedTypes.contains(value));
            expectedTypes.remove(value);
        }
    }

    /**
     * Test that all expected Airtime, GSM services are appropriate.
     *
     * @throws HomeException Thrown if there is problems accessing home data in the context.
     */
    public void testAirtimeCDMAServiceTypes()
        throws HomeException
    {
        final Context context = getContext();
        UnitTestSupport.createLicense(context, LicenseConstants.AIRTIME_LICENSE_KEY);
        UnitTestSupport.createLicense(context, LicenseConstants.EVDO_LICENSE);

        final Set<ServiceTypeEnum> expectedTypes = new HashSet<ServiceTypeEnum>();
        expectedTypes.add(ServiceTypeEnum.GENERIC);
        expectedTypes.add(ServiceTypeEnum.VOICEMAIL);
        expectedTypes.add(ServiceTypeEnum.VOICE);
        expectedTypes.add(ServiceTypeEnum.SMS);
        expectedTypes.add(ServiceTypeEnum.DATA);
        expectedTypes.add(ServiceTypeEnum.TRANSFER);
        expectedTypes.add(ServiceTypeEnum.EVDO);

        final Service service = new Service();
        service.setTechnology(TechnologyEnum.CDMA);
        context.put(AbstractWebControl.BEAN, service);

        final EnumCollection actualTypes =
            ServiceTypeEnum.COLLECTION.where(context, new AppropriateServiceTypePredicate());

        assertEquals(
            "The count of expected types matches the count of found types.",
            expectedTypes.size(),
            actualTypes.size());

        for (int n = 0; n < actualTypes.getSize(); ++n)
        {
            final ServiceTypeEnum value = (ServiceTypeEnum)actualTypes.getElementAt(n);
            assertTrue("Unexpected ServiceTypeEnum: " + value, expectedTypes.contains(value));
            expectedTypes.remove(value);
        }
    }


}
