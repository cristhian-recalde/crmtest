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

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassTransientHome;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

/**
 * Tests the functionality of the SubscriberTypeEnumPredicate Predicate
 * @author arturo.medina@redknee.com
 *
 */
public class TestSubscriptionTechnologyTypeEnumPredicate extends
        ContextAwareTestCase
{

    /**
     * 
     * @param name
     */
    public TestSubscriptionTechnologyTypeEnumPredicate(String name)
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

        final TestSuite suite = new TestSuite(TestServiceAdjustmentTypePredicate.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();
        setupContext();
    }


    private void setupContext()
    {
        Subscriber sub = createSubscriber();
        SubscriptionClass subClass = createSubbscriptionClass();
        sub.setSubscriptionClass(subClass.getId());
    }

    private SubscriptionClass createSubbscriptionClass()
    {
        SubscriptionClass bean = new SubscriptionClass();
        bean.setDescription("Desc");
        bean.setId(1l);
        bean.setName("Name");
        bean.setSegmentType(SubscriberTypeEnum.POSTPAID_INDEX);
        bean.setTechnologyType(TechnologyEnum.GSM_INDEX);
        bean.setSubscriptionType(2l);
        
        Home home = new SubscriptionClassTransientHome(getContext());
        
        try
        {
            home.create(bean);
        }
        catch (HomeException e)
        {
        }

        getContext().put(SubscriptionClassHome.class, home);
        return bean;
    }
    
    private Subscriber createSubscriber()
    {
        Subscriber sub = new Subscriber();
        sub.setId("123-1");
        sub.setMSISDN("1234567890");
        getContext().put(AbstractWebControl.BEAN, sub);
        
        return sub;
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        super.tearDown();
        getContext().put(AbstractWebControl.BEAN, null);
        getContext().put(SubscriptionClassHome.class, null);
        
    }

    /**
     * Tests that the f() method works according to the intent.
     */
    public void testF()
    {
        TechnologyTypeEnumPredicate predicate = new TechnologyTypeEnumPredicate();
        
        final Iterator iterator = TechnologyEnum.COLLECTION.iterator();
        try
        {
            UnitTestSupport.createLicense(getContext(), LicenseConstants.GSM_LICENSE_KEY);
        }
        catch (HomeException e)
        {
            fail();
        }

        while (iterator.hasNext())
        {
            final TechnologyEnum type = (TechnologyEnum)iterator.next();
            assertTrue(type + " Can be displayed.", predicate.f(getContext(), type));
            assertFalse(type + " Can NOT be displayed.", predicate.f(getContext(), type));
        }
        
    }

}
