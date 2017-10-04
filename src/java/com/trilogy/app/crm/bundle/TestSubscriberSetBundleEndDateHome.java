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
package com.trilogy.app.crm.bundle;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Exersize SubscriberSetBundleEndDateHome logic.
 *
 * @author victor.stratan@redknee.com
 */
public class TestSubscriberSetBundleEndDateHome extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSubscriberSetBundleEndDateHome(String name)
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
        final TestSuite suite = new TestSuite(TestSubscriberSetBundleEndDateHome.class);
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        obj_ = new SubscriberSetBundleEndDateHome(null);

        sub_ = new Subscriber();
        sub_.setId("123-1");

        calendar_ = Calendar.getInstance();

        calendar_.set(Calendar.HOUR_OF_DAY, 0);
        calendar_.set(Calendar.MINUTE, 0);
        calendar_.set(Calendar.SECOND, 0);
        calendar_.set(Calendar.MILLISECOND, 0);
        today_ = calendar_.getTime();
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        obj_ = null;
        super.tearDown();
    }

    public void testUpdateBundleFeeEndDateSetEndDateWithPaymentNum()
    {
        final Context ctx = getContext();
        calendar_.setTime(today_);
        calendar_.add(Calendar.MONTH, 2);
        final Date endDate = calendar_.getTime();

        final BundleFee fee = new BundleFee();
        fee.setId(1234);
        fee.setStartDate(today_);
        fee.setEndDate(today_);
        fee.setPaymentNum(2);
        final Long feeKey = Long.valueOf(fee.getId());

        sub_.getBundles().put(feeKey, fee);

        try 
        {
			obj_.updateBundleFeeEndDate(ctx, sub_);
		} 
        catch (HomeException e) 
		{
			fail(e.getMessage());
		}

        final BundleFee resultFee = (BundleFee) sub_.getBundles().get(feeKey);

        assertEquals("End date calculated incorrectly", endDate, resultFee.getEndDate());
        assertEquals("Payment Num not reset", 0, resultFee.getPaymentNum());
    }

    public void testUpdateBundleFeeEndDateNotSetEndDateWithNoPaymentNum()
    {
        final Context ctx = getContext();
        calendar_.setTime(today_);
        calendar_.add(Calendar.MONTH, 2);
        final Date endDate = calendar_.getTime();

        final BundleFee fee = new BundleFee();
        fee.setId(1234);
        fee.setStartDate(today_);
        fee.setEndDate(endDate);
        fee.setPaymentNum(0);
        final Long feeKey = Long.valueOf(fee.getId());

        sub_.getBundles().put(feeKey, fee);

        try 
        {
			obj_.updateBundleFeeEndDate(ctx, sub_);
		} 
        catch (HomeException e) 
		{
			fail(e.getMessage());
		}

        final BundleFee resultFee = (BundleFee) sub_.getBundles().get(feeKey);

        assertEquals("End date should not be modified", endDate, resultFee.getEndDate());
        assertEquals("Payment Num should not be modified", 0, resultFee.getPaymentNum());
    }

    private Date today_;
    private Calendar calendar_;
    private Subscriber sub_;
    private SubscriberSetBundleEndDateHome obj_;
}
