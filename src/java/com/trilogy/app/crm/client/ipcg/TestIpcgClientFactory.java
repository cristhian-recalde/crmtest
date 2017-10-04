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
package com.trilogy.app.crm.client.ipcg;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * Unit tests for IpcgClientFactory.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestIpcgClientFactory
    extends ContextAwareTestCase
{

    /**
     * Creates a new TestIpcgClientFactory.
     *
     * @param name The name of the set of tests.
     */
    public TestIpcgClientFactory(final String name)
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

        final TestSuite suite = new TestSuite(TestIpcgClientFactory.class);

        return suite;
    }


    /**
     * Tests that generateContextKey() returns the Class of the object when the
     * suffix is null.
     */
    public void testGenerateContextKeyWithNullSuffix()
    {
        assertEquals(
            "A null suffix should cause the key to be the class String.",
            String.class,
            IpcgClientFactory.generateContextKey(String.class, null));

        assertEquals(
            "A null suffix should cause the key to be the class Date.",
            Date.class,
            IpcgClientFactory.generateContextKey(Date.class, null));
    }


    /**
     * Tests that generateContextKey() returns the Class of the object when the
     * suffix is blank.
     */
    public void testGenerateContextKeyWithBlankSuffix()
    {
        assertEquals(
            "A blank suffix should cause the key to be the class String.",
            String.class,
            IpcgClientFactory.generateContextKey(String.class, ""));

        assertEquals(
            "A blank suffix should cause the key to be the class Date.",
            Date.class,
            IpcgClientFactory.generateContextKey(Date.class, "  "));
    }


    /**
     * Tests that generateContextKey() returns a String the Class of the object
     * and the suffix when the suffix is non-null.
     */
    public void testGenerateContextKeyWithNonNullSuffix()
    {
        assertEquals(
            "A \".foo\" suffix should cause the suffix to be appended.",
            "java.lang.String.foo",
            IpcgClientFactory.generateContextKey(String.class, ".foo"));

        assertEquals(
            "A \".bar\" suffix should cause the suffix to be appended.",
            "java.util.Date.bar",
            IpcgClientFactory.generateContextKey(Date.class, ".bar"));
    }


    /**
     * Tests that generatePropertiesKey() returns the unqualified Class name of
     * the object when the suffix is null.
     */
    public void testGeneratePropertiesKeyWithNullSuffix()
    {
        assertEquals(
            "A null suffix should cause the key to be the class String.",
            "String",
            IpcgClientFactory.generatePropertiesKey(String.class, null));

        assertEquals(
            "A null suffix should cause the key to be the class Date.",
            "Date",
            IpcgClientFactory.generatePropertiesKey(Date.class, null));
    }


    /**
     * Tests that generatePropertiesKey() returns the unqualified Class of the
     * object when the suffix is blank.
     */
    public void testGeneratePropertiesKeyWithBlankSuffix()
    {
        assertEquals(
            "A blank suffix should cause the key to be the class String.",
            "String",
            IpcgClientFactory.generatePropertiesKey(String.class, ""));

        assertEquals(
            "A blank suffix should cause the key to be the class Date.",
            "Date",
            IpcgClientFactory.generatePropertiesKey(Date.class, "  "));
    }


    /**
     * Tests that generatePropertiesKey() returns a String the Class of the object
     * and the suffix when the suffix is non-null.
     */
    public void testGeneratePropertiesKeyWithNonNullSuffix()
    {
        assertEquals(
            "A \".foo\" suffix should cause the suffix to be appended.",
            "String.foo",
            IpcgClientFactory.generatePropertiesKey(String.class, ".foo"));

        assertEquals(
            "A \".bar\" suffix should cause the suffix to be appended.",
            "Date.bar",
            IpcgClientFactory.generatePropertiesKey(Date.class, ".bar"));
    }


    /**
     * Tests that the locateClient() method returns the correct instance of IPCG
     * for the given technology type.
     */
    public void testLocateFunctionCorrectlyDistinguishesCDMATechnology()
    {
        final IpcgClient clientNonCDMA = new IpcgClientProxy(null);
        final IpcgClient clientCDMA = new IpcgClientProxy(null);

        assertNotSame(
            "The two clients should be distinct.",
            clientNonCDMA, clientCDMA);

        final Context context = getContext();

        // Add the dummy objects representing the two clients.
        {
            final Object keyNonCDMA =
                IpcgClientFactory.generateContextKey(IpcgClient.class, null);
            final Object keyCDMA =
                IpcgClientFactory.generateContextKey(
                    IpcgClient.class,
                    IpcgClientFactory.CDMA_SUFFIX);

            context.put(keyNonCDMA, clientNonCDMA);
            context.put(keyCDMA, clientCDMA);
        }

        assertSame(
            "The located GSM client should be the non-CDMA IPCG we added.",
            clientNonCDMA,
            IpcgClientFactory.locateClient(context, TechnologyEnum.GSM));

        assertSame(
            "The located TDMA client should be the non-CDMA IPCG we added.",
            clientNonCDMA,
            IpcgClientFactory.locateClient(context, TechnologyEnum.TDMA));

        assertSame(
            "The located CDMA client should be the CDMA IPCG we added.",
            clientCDMA,
            IpcgClientFactory.locateClient(context, TechnologyEnum.CDMA));
    }


} // class
