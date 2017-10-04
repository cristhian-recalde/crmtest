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

package com.trilogy.app.crm.home.sub;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * JUnit test for subscriber state validation.
 *
 * @author cindy.wong@redknee.com
 * @since 13-Mar-08
 */
public class SubscriberStateTypeValidatorTest extends ContextAwareTestCase
{

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution
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
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(SubscriberStateTypeValidatorTest.class);

        return suite;
    }


    /**
     * Create a new instance of <code>SubscriberStateTypeValidatorTest</code>.
     *
     * @param name
     *            Unit test name.
     */
    public SubscriberStateTypeValidatorTest(final String name)
    {
        super(name);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        // set up system feature configuration bean
        final SysFeatureCfg systemFeatureConfig = new SysFeatureCfg();
        getContext().put(SysFeatureCfg.class, systemFeatureConfig);
    }


    /**
     * Validate a passing (state, subscriber type) combination.
     *
     * @param subscriber
     *            Subscriber.
     * @param state
     *            State.
     */
    protected void passState(final Subscriber subscriber, final SubscriberStateEnum state)
    {
        subscriber.setState(state);
        SubscriberStateTypeValidator.instance().validate(getContext(), subscriber);
    }


    /**
     * Validate a failing (state, subscriber type) combination.
     *
     * @param subscriber
     *            Subscriber.
     * @param state
     *            State.
     */
    protected void failState(final Subscriber subscriber, final SubscriberStateEnum state)
    {
        subscriber.setState(state);
        try
        {
            SubscriberStateTypeValidator.instance().validate(getContext(), subscriber);
            final StringBuilder sb = new StringBuilder();
            sb.append(state.getDescription());
            sb.append(" is not a valid state for ");
            sb.append(subscriber.getSubscriberType());
            sb.append(" subscribers");
            fail(sb.toString());
        }
        catch (final IllegalPropertyArgumentException exception)
        {
            // pass
        }
    }


    /**
     * Test postpaid subscriber state validation in
     * {@link SubscriberStateTypeValidator#validate(Context, Object)}.
     */
    public void testValidatePostpaid()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setId("postpaid-id");

        passState(subscriber, SubscriberStateEnum.ACTIVE);
        failState(subscriber, SubscriberStateEnum.AVAILABLE);
        failState(subscriber, SubscriberStateEnum.EXPIRED);
        passState(subscriber, SubscriberStateEnum.IN_ARREARS);
        passState(subscriber, SubscriberStateEnum.IN_COLLECTION);
        passState(subscriber, SubscriberStateEnum.INACTIVE);
        failState(subscriber, SubscriberStateEnum.LOCKED);
        passState(subscriber, SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        passState(subscriber, SubscriberStateEnum.NON_PAYMENT_WARN);
        passState(subscriber, SubscriberStateEnum.PENDING);
        passState(subscriber, SubscriberStateEnum.PROMISE_TO_PAY);
        passState(subscriber, SubscriberStateEnum.SUSPENDED);
    }


    /**
     * Test hybrid prepaid subscriber state validation in
     * {@link SubscriberStateTypeValidator#validate(Context, Object)}.
     */
    public void testValidateHybridPrepaid()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("hybrid-prepaid");
        final SysFeatureCfg cfg = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        cfg.setCreatePrepaidInActiveState(true);
        cfg.setAllowPrepaidToBeCreatedInPending(true);
        cfg.setUnExpirablePrepaid(true);
        getContext().put(SysFeatureCfg.class, cfg);

        passState(subscriber, SubscriberStateEnum.ACTIVE);
        failState(subscriber, SubscriberStateEnum.AVAILABLE);
        failState(subscriber, SubscriberStateEnum.EXPIRED);
        failState(subscriber, SubscriberStateEnum.IN_ARREARS);
        failState(subscriber, SubscriberStateEnum.IN_COLLECTION);
        passState(subscriber, SubscriberStateEnum.INACTIVE);
        failState(subscriber, SubscriberStateEnum.LOCKED);
        failState(subscriber, SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        failState(subscriber, SubscriberStateEnum.NON_PAYMENT_WARN);
        passState(subscriber, SubscriberStateEnum.PENDING);
        failState(subscriber, SubscriberStateEnum.PROMISE_TO_PAY);
        passState(subscriber, SubscriberStateEnum.SUSPENDED);
    }


    /**
     * Test true prepaid subscriber state validation in
     * {@link SubscriberStateTypeValidator#validate(Context, Object)}.
     */
    public void testValidateTruePrepaid()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setId("true-prepaid");
        final SysFeatureCfg cfg = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        getContext().put(SysFeatureCfg.class, cfg);

        passState(subscriber, SubscriberStateEnum.ACTIVE);
        passState(subscriber, SubscriberStateEnum.AVAILABLE);
        passState(subscriber, SubscriberStateEnum.EXPIRED);
        failState(subscriber, SubscriberStateEnum.IN_ARREARS);
        failState(subscriber, SubscriberStateEnum.IN_COLLECTION);
        passState(subscriber, SubscriberStateEnum.INACTIVE);
        passState(subscriber, SubscriberStateEnum.LOCKED);
        failState(subscriber, SubscriberStateEnum.NON_PAYMENT_SUSPENDED);
        failState(subscriber, SubscriberStateEnum.NON_PAYMENT_WARN);
        passState(subscriber, SubscriberStateEnum.PENDING);
        failState(subscriber, SubscriberStateEnum.PROMISE_TO_PAY);
        passState(subscriber, SubscriberStateEnum.SUSPENDED);
    }

}
