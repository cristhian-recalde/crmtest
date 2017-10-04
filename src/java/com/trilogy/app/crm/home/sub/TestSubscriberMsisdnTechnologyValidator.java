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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit test for <code>SubscriberMsisdnTechnologyValidator</code>.
 *
 * @author cindy.wong@redknee.com
 */
public class TestSubscriberMsisdnTechnologyValidator extends ContextAwareTestCase
{

    /**
     * Prefix of subscriber ID to use.
     */
    private final String subscriberIdPrefix_ = "333";

    /**
     * Next ID of the subscriber.
     */
    private int nextSubscriberId_ = 1;

    /**
     * A map of technology and MSISDN.
     */
    private Map<TechnologyEnum, Msisdn> msisdnMap_ = new TreeMap<TechnologyEnum, Msisdn>();

    /**
     * Create a new <code>TestSubscriberMsisdnTechnologyValidator</code>.
     *
     * @param name Name of the test suite.
     */
    public TestSubscriberMsisdnTechnologyValidator(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by standard JUnit tools (i.e.,
     * those that do not provide a context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked by the Redknee Xtest code,
     * which provides the application's operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestSubscriberMsisdnTechnologyValidator.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        // set up msisdn home
        final Home home = new MsisdnTransientHome(getContext());

        // create msisdns
        {
            final Msisdn msisdn = new Msisdn();
            msisdn.setMsisdn("1234567");
            msisdn.setTechnology(TechnologyEnum.CDMA);
            try
            {
                home.create(getContext(), msisdn);
                msisdnMap_.put(msisdn.getTechnology(), msisdn);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
        }
        {
            final Msisdn msisdn = new Msisdn();
            msisdn.setMsisdn("7654321");
            msisdn.setTechnology(TechnologyEnum.TDMA);
            try
            {
                home.create(getContext(), msisdn);
                msisdnMap_.put(msisdn.getTechnology(), msisdn);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
        }
        {
            final Msisdn msisdn = new Msisdn();
            msisdn.setMsisdn("9999999");
            msisdn.setTechnology(TechnologyEnum.GSM);
            try
            {
                home.create(getContext(), msisdn);
                msisdnMap_.put(msisdn.getTechnology(), msisdn);
            }
            catch (HomeException exception)
            {
                fail(exception.getMessage());
            }
        }
        getContext().put(MsisdnHome.class, home);
    }

    /**
     * Test method for {@link com.redknee.app.crm.home.sub.SubscriberMsisdnTechnologyValidator#validate}.
     */
    public void testValidate()
    {
        for (final Iterator iter = TechnologyEnum.COLLECTION.iterator(); iter.hasNext();)
        {
            final TechnologyEnum technology = (TechnologyEnum) iter.next();
            if (!technology.equals(TechnologyEnum.ANY))
            {
                technologyTest(technology);
            }
        }
    }

    /**
     * Test the validation of a subscriber against MSISDN's in all technologies.
     *
     * @param technology The technology of the subscriber
     */
    private void technologyTest(final TechnologyEnum technology)
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setTechnology(technology);
        subscriber.setId(subscriberIdPrefix_ + nextSubscriberId_);
        nextSubscriberId_++;

        for (TechnologyEnum msisdnTechnology : msisdnMap_.keySet())
        {
            final Msisdn msisdn = msisdnMap_.get(msisdnTechnology);
            subscriber.setMSISDN(msisdn.getMsisdn());
            msisdn.setBAN(subscriberIdPrefix_);
            if (subscriber.getTechnology().equals(msisdnTechnology) || msisdnTechnology.equals(TechnologyEnum.ANY))
            {
                try
                {
                    SubscriberMsisdnTechnologyValidator.getInstance().validate(getContext(), subscriber);
                }
                catch (IllegalStateException exception)
                {
                    fail("Exception caught: " + exception.getMessage());
                }
            }
            else
            {
                try
                {
                    SubscriberMsisdnTechnologyValidator.getInstance().validate(getContext(), subscriber);
                    fail("Exception should be thrown");
                }
                catch (IllegalStateException exception)
                {
                    // OK
                }
            }
        }
    }
}
