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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for AdjustmentTypeSupport.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAdjustmentTypeSupport extends ContextAwareTestCase
{

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

        // Set up an AdjustmentType for testing.
        this.adjustmentType_ = new TestWrapperAdjustmentType();
        this.adjustmentType_.setCode(123);
        this.adjustmentType_.setParentCode(0);

        // Set up an AdjustmentType home for testing.
        this.adjustmentTypeHome_ = new AdjustmentTypeTransientHome(getContext());
        try
        {
            this.adjustmentTypeHome_.create(getContext(), this.adjustmentType_);
        }
        catch (final HomeException e)
        {
            fail("Failed to create AdjustmentType(s) for testing");
        }
        getContext().put(AdjustmentTypeHome.class, this.adjustmentTypeHome_);
        getContext().put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME, this.adjustmentTypeHome_);
        getContext().put(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_READ_ONLY_HOME, this.adjustmentTypeHome_);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        this.adjustmentTypeHome_ = null;
        this.adjustmentType_ = null;

        super.tearDown();
    }


    /**
     * Tests that the isInCategory() method works according to the intent.
     */
    public void testIsInCategory()
    {
        // AdjustmentType identifier does not exist
        assertFalse("Should return false if the adjustment type could not be found", AdjustmentTypeSupportHelper.get(getContext())
            .isInCategory(getContext(), 999, AdjustmentTypeEnum.Payments));

        // AdjustmentType identifier exists
        assertTrue("Should return true if the input category is Payments", AdjustmentTypeSupportHelper.get(getContext()).isInCategory(
            getContext(), 123, AdjustmentTypeEnum.Payments));

        // AdjustmentType identifier exists
        assertFalse("Should return true if the input category is NOT Payments", AdjustmentTypeSupportHelper.get(getContext()).isInCategory(
            getContext(), 123, AdjustmentTypeEnum.StandardPayments));
    }


    /**
     * Tests that the getSelfAndDescendantCodes() method works according to the intent.
     */
    public void testGetSelfAndDescendantCodes()
    {
        {
            final Set codes = AdjustmentTypeSupportHelper.get(getContext()).getSelfAndDescendantCodes(getContext(), 999);

            assertEquals("Should return an empty Set because the given AdjustmentType " + "could not be found", 0,
                codes.size());
        }

        {
            final Set codes = AdjustmentTypeSupportHelper.get(getContext()).getSelfAndDescendantCodes(getContext(), 123);

            assertEquals("Should return Set with three elements", 3, codes.size());
        }
    }

    /**
     * Adjustment type.
     */
    private AdjustmentType adjustmentType_;

    /**
     * Adjustment type home.
     */
    private AdjustmentTypeHome adjustmentTypeHome_;

    /**
     * Provides a convenient test wrapper class for AdjustmentType.
     */
    private class TestWrapperAdjustmentType extends AdjustmentType
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;


        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isInCategory(final Context ctx, final AdjustmentTypeEnum category)
        {
            return category == AdjustmentTypeEnum.Payments;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Integer> getSelfAndDescendantCodes(final Context ctx)
        {
            final Set<Integer> codes = new HashSet<Integer>();
            codes.add(Integer.valueOf("111"));
            codes.add(Integer.valueOf("222"));
            codes.add(Integer.valueOf("333"));
            return codes;
        }
    } // inner-class
}
