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
package com.trilogy.app.crm.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ExternalAppMapping;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit tests for Service.
 *
 * @author gary.anderson@redknee.com
 */
public class TestService extends ContextAwareTestCase
{
    /**
     * Creates a new TestService.
     *
     * @param name The name of the set of tests.
     */
    public TestService(final String name)
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

        final TestSuite suite = new TestSuite(TestService.class);

        return suite;
    }


    /**
     * Tests that setting the ServiceType property updates the handler.
     * @throws HomeException thrown by called methods
     */
    public void testHandlerSetFromServiceType() throws HomeException
    {
        Collection<ExternalAppMapping> serviceHandles = HomeSupportHelper.get(getContext()).getBeans(getContext(), ExternalAppMapping.class);
        final Map<ServiceTypeEnum, String> expectedHandlers = new HashMap<ServiceTypeEnum, String>();
        for (ExternalAppMapping mapping : serviceHandles)
        {
            expectedHandlers.put(mapping.getServiceType(), mapping.getHandler());
        }

        final Iterator<ServiceTypeEnum> serviceTypeIterator = ServiceTypeEnum.COLLECTION.iterator();

        while (serviceTypeIterator.hasNext())
        {
            final ServiceTypeEnum serviceType = serviceTypeIterator.next();

            final String expectedHandler = expectedHandlers.get(serviceType);
            assertNotNull(
                "The ServiceType " + serviceType + " has not been accounted for in the test.",
                expectedHandler);

            final Service service = new Service();
            service.setContext(getContext());
            service.setType(serviceType);

            assertEquals(
                "The ServiceType to Handler mapping is incorrect.",
                expectedHandler, service.getHandler());
        }
    }


} // class
