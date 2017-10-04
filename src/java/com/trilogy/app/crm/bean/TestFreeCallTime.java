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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * Provides tests for the FreeCallTime bean.
 *
 * @author gary.anderson@redknee.com
 */
public
class TestFreeCallTime
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestFreeCallTime(final String name)
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

        final TestSuite suite = new TestSuite(TestFreeCallTime.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
    }


    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Test groupPooledSettings property set-up.
     */
    public void testGroupPooledSettingsInitialization()
    {
        final FreeCallTime bean = new FreeCallTime();

        final FCTGroupPooledSettings settings = bean.getGroupPooledSettings();
        assertNotNull("Initial GroupPooledSettings.", settings);
    }


    /**
     * Tests that the hidden property groupLimit is kept synchronized with the
     * transient property groupPooledSettings when changed through the
     * groupLimit property.
     */
    public void testBeanLevelGroupLimitSynchronization()
    {
        final FreeCallTime bean = new FreeCallTime();
        final FCTGroupPooledSettings settings = bean.getGroupPooledSettings();

        assertEquals("Initial groupPooledSettings.groupLimit.", settings.getGroupLimit(), 0);
        assertEquals("Initial groupLimit.", bean.getGroupLimit(), 0);

        bean.setGroupLimit(42);

        assertEquals("Updated (bean) groupPooledSettings.groupLimit.", settings.getGroupLimit(), 42);
        assertEquals("Updated (bean) groupLimit.", bean.getGroupLimit(), 42);
    }


    /**
     * Tests that the hidden property usagePrecedence is kept synchronized with
     * the transient property groupPooledSettings when chantged though the
     * usagePrecedence property.
     */
    public void testBeanLevelUsagePrecedenceSynchronization()
    {
        final FreeCallTime bean = new FreeCallTime();
        final FCTGroupPooledSettings settings = bean.getGroupPooledSettings();

        assertEquals(
            "Initial groupPooledSettings.usagePrecedence.",
            settings.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.GROUP);

        assertEquals(
            "Initial usagePrecedence.",
            bean.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.GROUP);

        bean.setUsagePrecedence(FCTUsagePrecedenceEnum.SUBSCRIBER);

        assertEquals(
            "Updated (bean) groupPooledSettings.usagePrecedence.",
            settings.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.SUBSCRIBER);

        assertEquals(
            "Updated (bean) usagePrecedence.",
            bean.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.SUBSCRIBER);
    }


    /**
     * Tests that the hidden property groupLimit is kept synchronized with the
     * transient property groupPooledSettings when changed through the
     * groupPooledSettings property.
     */
    public void testSettingsLevelGroupLimitSynchronization()
    {
        final FreeCallTime bean = new FreeCallTime();
        final FCTGroupPooledSettings settings = bean.getGroupPooledSettings();

        assertEquals("Initial groupPooledSettings.groupLimit.", settings.getGroupLimit(), 0);
        assertEquals("Initial groupLimit.", bean.getGroupLimit(), 0);

        settings.setGroupLimit(67);

        assertEquals("Updated (settings) groupPooledSettings.groupLimit.", settings.getGroupLimit(), 67);
        assertEquals("Updated (settings) groupLimit.", bean.getGroupLimit(), 67);
    }


    /**
     * Tests that the hidden property usagePrecedence is kept synchronized with
     * the transient property groupPooledSettings when chantged though the
     * groupPooledSettings property.
     */
    public void testSettingsLevelUsagePrecedenceSynchronization()
    {
        final FreeCallTime bean = new FreeCallTime();
        final FCTGroupPooledSettings settings = bean.getGroupPooledSettings();

        assertEquals(
            "Initial groupPooledSettings.usagePrecedence.",
            settings.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.GROUP);

        assertEquals(
            "Initial usagePrecedence.",
            bean.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.GROUP);

        settings.setUsagePrecedence(FCTUsagePrecedenceEnum.SUBSCRIBER);

        assertEquals(
            "Updated (settings) groupPooledSettings.usagePrecedence.",
            settings.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.SUBSCRIBER);

        assertEquals(
            "Updated (settings) usagePrecedence.",
            bean.getUsagePrecedence(),
            FCTUsagePrecedenceEnum.SUBSCRIBER);
    }

} // class
