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
package com.trilogy.app.crm.support.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMapping;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingHome;
import com.trilogy.app.crm.bean.SystemAdjustTypeMappingTransientHome;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;


/**
 * A suite of test cases for AdjustmentValidator. Due to changes in Adjustment Type
 * storage (now in DB), this is to be run with an Application context.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAdjustmentTypeSupport extends ContextAwareTestCase
{

    /**
     * System adjustment type used for testing.
     */
    private static final AdjustmentTypeEnum SYSTEM_ADJUSTMENT_TYPE_1 = AdjustmentTypeEnum.StandardPayments;

    /**
     * System adjustment type used for testing.
     */
    private static final AdjustmentTypeEnum SYSTEM_ADJUSTMENT_TYPE_2 = AdjustmentTypeEnum.BalanceTransfer;


    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            The name of the test.
     */
    public TestAdjustmentTypeSupport(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     *
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAdjustmentTypeSupport.class);

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

        final Context subContext = getContext().createSubContext();

        final Home adjHome = new AdapterHome(subContext,
                new AdjustmentTypeTransientHome(subContext), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.AdjustmentType, com.redknee.app.crm.bean.core.AdjustmentType>(
                com.redknee.app.crm.bean.AdjustmentType.class, 
                com.redknee.app.crm.bean.core.AdjustmentType.class));
        sysAdjHome = new SystemAdjustTypeMappingTransientHome(subContext);
        subContext.put(SystemAdjustTypeMappingHome.class, sysAdjHome);
        try
        {
            final AdjustmentType adj1 = new AdjustmentType();
            adj1.setCode(TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1.getIndex());
            adj1.setCategory(true);
            adj1.setParentCode(0);
            adjHome.create(adj1);
            final AdjustmentType adj2 = new AdjustmentType();
            adj2.setCode(TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_2.getIndex());
            adj2.setCategory(false);
            adj2.setParentCode(0);
            adjHome.create(adj2);
        }
        catch (final HomeException e)
        {
            // empty
        }

        subContext.put(AdjustmentTypeHome.class, adjHome);
        subContext.put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME, adjHome);
        subContext.put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME, adjHome);
        setContext(subContext);
    }


    /**
     * Unit test for
     * {@link AdjustmentTypeSupportHelper#getAdjustmentTypeCodeByAdjustmentTypeEnum(Context, AdjustmentTypeEnum)}.
     */
    public void testGetAdjustmentTypeCodeByAdjustmentTypeEnum()
    {
        final SystemAdjustTypeMapping mapping1 = new SystemAdjustTypeMapping();
        mapping1.setSysAdjustmeType(TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1.getIndex());
        mapping1.setAdjType(TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_2.getIndex());
        try
        {
            sysAdjHome.create(mapping1);
        }
        catch (final HomeException e)
        {
            fail("Exception caught: " + e.toString());
        }

        final int adjId = AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(getContext(),
                TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1);
        assertEquals(adjId, TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_2.getIndex());
    }


    /**
     * Unit test for {@link AdjustmentTypeSupportHelper#getAdjustmentType(Context, int)}.
     */
    public void testGetAdjustmentType()
    {
        try
        {
            final AdjustmentType adj = AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentType(getContext(),
                TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1.getIndex());
            assertEquals(adj.getCode(), TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1.getIndex());
        }
        catch (final HomeException e)
        {
            fail("Exception caught: " + e.toString());
        }
    }


    /**
     * Unit test for
     * {@link AdjustmentTypeSupportHelper#isInCategory(Context, int, AdjustmentTypeEnum)}.
     */
    public void testIsInCategoryFalse()
    {
        assertEquals(AdjustmentTypeSupportHelper.get(getContext()).isInCategory(getContext(),
            TestAdjustmentTypeSupport.SYSTEM_ADJUSTMENT_TYPE_1.getIndex(), AdjustmentTypeEnum.Payments), false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        super.tearDown();
    }


    private Home sysAdjHome;
}
