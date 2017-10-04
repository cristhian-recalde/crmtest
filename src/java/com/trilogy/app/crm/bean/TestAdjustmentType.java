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

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;


/**
 * A suite of test cases for AdjustmentType. Due to changes in Adjustment Type storage
 * (now in DB), this is to be run with an Application context.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestAdjustmentType extends TestCase
{

    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            The name of the test.
     */
    public TestAdjustmentType(final String name)
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
     * @param ctx
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context ctx)
    {
        setContext(ctx);

        final TestSuite suite = new TestSuite(TestAdjustmentType.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        // Set up several AdjustmentTypes for testing.
        this.payments_ = new AdjustmentType();
        this.payments_.setCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(getContext(),
            AdjustmentTypeEnum.Payments));
        this.payments_.setParentCode(0);
        this.standardPayments_ = new AdjustmentType();
        this.standardPayments_.setCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(getContext(),
            AdjustmentTypeEnum.StandardPayments));
        this.standardPayments_.setParentCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(
            getContext(), AdjustmentTypeEnum.Payments));
        this.paymentReversal_ = new AdjustmentType();
        this.paymentReversal_.setCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(getContext(),
            AdjustmentTypeEnum.PaymentReversal));
        this.paymentReversal_.setParentCode(AdjustmentTypeSupportHelper.get(getContext()).getAdjustmentTypeCodeByAdjustmentTypeEnum(
            getContext(), AdjustmentTypeEnum.StandardPayments));

        // Set up an AdjustmentType home for testing.
        this.adjustmentTypeHome_ = new AdjustmentTypeTransientHome(getContext());
        try
        {
            this.adjustmentTypeHome_.create(getContext(), this.payments_);
            this.adjustmentTypeHome_.create(getContext(), this.standardPayments_);
            this.adjustmentTypeHome_.create(getContext(), this.paymentReversal_);
        }
        catch (final HomeException e)
        {
            fail("Failed to create AdjustmentType(s) for testing");
        }
        getContext().put(AdjustmentTypeHome.class, this.adjustmentTypeHome_);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        this.adjustmentTypeHome_ = null;
        this.paymentReversal_ = null;
        this.standardPayments_ = null;
        this.payments_ = null;
    }


    /**
     * Tests that the getParent() method works according to the intent.
     */
    public void testGetParent()
    {
        assertTrue("Payments should be the parent of StandardPayments", this.payments_.equals(this.standardPayments_
            .getParent(getContext())));

        assertTrue("StandardPayments should be the parent of PaymentReversal", this.standardPayments_
            .equals(this.paymentReversal_.getParent(getContext())));

        assertFalse("StandardPayments should NOT be the parent of Payments", this.standardPayments_
            .equals(this.payments_.getParent(getContext())));

        assertTrue("Payments should have no parent", null == this.payments_.getParent(getContext()));
    }


    /**
     * Tests that the isInCategory() method works according to the intent.
     */
    public void testIsInCategory()
    {
        assertTrue("Payments should be in Payments category", this.payments_.isInCategory(getContext(),
            AdjustmentTypeEnum.Payments));

        assertTrue("StandardPayments should be in Payments category", this.standardPayments_.isInCategory(getContext(),
            AdjustmentTypeEnum.Payments));

        assertTrue("PaymentReversal should be in StandardPayments category", this.paymentReversal_.isInCategory(
            getContext(), AdjustmentTypeEnum.StandardPayments));

        assertTrue("PaymentReversal should also be in Payments category", this.paymentReversal_.isInCategory(
            getContext(), AdjustmentTypeEnum.Payments));

        assertFalse("Payments should NOT be in StandardPayments category", this.payments_.isInCategory(getContext(),
            AdjustmentTypeEnum.StandardPayments));
    }


    /**
     * Tests that the getSelfAndDescendantCodes() method works according to the intent.
     */
    public void testGetSelfAndDescendantCodes()
    {
        {
            final Set codes = this.payments_.getSelfAndDescendantCodes(getContext());
            assertEquals("Payments should have two descendants", 3, codes.size());
        }

        {
            final Set codes = this.standardPayments_.getSelfAndDescendantCodes(getContext());
            assertEquals("StandardPayments should have one descendant", 2, codes.size());
        }

        {
            final Set codes = this.paymentReversal_.getSelfAndDescendantCodes(getContext());
            assertEquals("PaymentReversal should have no descendants", 1, codes.size());
        }
    }


    /**
     * Returns the context.
     *
     * @return Returns the context.
     */
    public static Context getContext()
    {
        return context;
    }


    /**
     * Sets the context.
     *
     * @param newContext
     *            The context to set.
     */
    public static void setContext(final Context newContext)
    {
        context = newContext;
    }

    /**
     * Payments adjustment type.
     */
    private AdjustmentType payments_;
    /**
     * Standard payments adjustment type.
     */
    private AdjustmentType standardPayments_;

    /**
     * Payment reversal adjustment type.
     */
    private AdjustmentType paymentReversal_;

    /**
     * Adjustment type home.
     */
    private AdjustmentTypeHome adjustmentTypeHome_;

    /**
     * Context used.
     */
    private static transient Context context;
}
