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
package com.trilogy.app.crm.bulkloader;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.MessageMgrSPI;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassTransientHome;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeTransientHome;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestFakeLicenseMgr;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;

/**
 * A suite of test cases for SubscriberCreateVisitor.
 *
 * @author victor.stratan@redknee.com
 */
public class TestSubscriberCreateVisitor extends ContextAwareTestCase
{
    private static final String BAN = "12345";
    private static final String SPID = "1";
    private static final long SUBSCRIPTION_CLASS_ID = 2;
    private static final long IN_SUBSCRIPTION_CLASS_ID = 1;
    private static final String SUBSCRIPTION_CLASS = String.valueOf(SUBSCRIPTION_CLASS_ID);
    private static final String IN_SUBSCRIPTION_CLASS = String.valueOf(IN_SUBSCRIPTION_CLASS_ID);
    private static final String MSISDN = "407220000123";
    private static final long SUBSCRIPTION_TYPE_ID = 5;
    private static final long IN_SUBSCRIPTION_TYPE_ID = 3;
    private static final long PRICE_PLAN_ID = 123;
    private static final String PRICE_PLAN = String.valueOf(PRICE_PLAN_ID);

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSubscriberCreateVisitor(final String name)
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

        final TestSuite suite = new TestSuite(TestSubscriberCreateVisitor.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();
        ctx.put(MessageMgrSPI.class, new FakeMessageMgrSPI());

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(ctx);

        final LicenseMgr lMgr = new TestFakeLicenseMgr();
        ctx.put(LicenseMgr.class, lMgr);

        final PrintWriter writer = new PrintWriter(new ByteArrayOutputStream());
        final PrintWriter errWriter = new PrintWriter(new ByteArrayOutputStream());
        visitor = new SubscriberCreateVisitor(getContext(), null, new Subscriber(), writer, errWriter);

        bulkSubscriber =  new BulkLoadSubscriber();
        bulkSubscriber.setSpid(SPID);
        bulkSubscriber.setSubscriptionClass(SUBSCRIPTION_CLASS);
        bulkSubscriber.setTechnology(TechnologyEnum.GSM);
        bulkSubscriber.setMSISDN(MSISDN);
        bulkSubscriber.setPackageId("78563412");
        bulkSubscriber.setPricePlan(PRICE_PLAN);

        SubscriptionClass subscriptionClass = new SubscriptionClass();
        subscriptionClass.setId(SUBSCRIPTION_CLASS_ID);
        subscriptionClass.setName("some name");
        subscriptionClass.setSubscriptionType(SUBSCRIPTION_TYPE_ID);
        subscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);
        subscriptionClass.setTechnologyType(TechnologyEnum.ANY_INDEX);

        SubscriptionClass inSubscriptionClass = new SubscriptionClass();
        inSubscriptionClass.setId(IN_SUBSCRIPTION_CLASS_ID);
        inSubscriptionClass.setName("some IN name");
        inSubscriptionClass.setSubscriptionType(IN_SUBSCRIPTION_TYPE_ID);
        inSubscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);
        inSubscriptionClass.setTechnologyType(TechnologyEnum.ANY_INDEX);

        Home subClasshome = new AdapterHome(
                ctx, 
                new SubscriptionClassTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.account.SubscriptionClass, com.redknee.app.crm.bean.core.SubscriptionClass>(
                        com.redknee.app.crm.bean.account.SubscriptionClass.class, 
                        com.redknee.app.crm.bean.core.SubscriptionClass.class));
        
        ctx.put(SubscriptionClassHome.class, subClasshome);

        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setId(SUBSCRIPTION_TYPE_ID);
        subscriptionType.setName("type name");
        subscriptionType.setType(SubscriptionTypeEnum.MOBILE_WALLET_INDEX);

        SubscriptionType inSubscriptionType = new SubscriptionType();
        inSubscriptionType.setId(IN_SUBSCRIPTION_TYPE_ID);
        inSubscriptionType.setName("IN type name");
        inSubscriptionType.setType(SubscriptionTypeEnum.AIRTIME_INDEX);

        Home subTypehome = new AdapterHome(
                ctx, 
                new SubscriptionTypeTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.account.SubscriptionType, com.redknee.app.crm.bean.core.SubscriptionType>(
                        com.redknee.app.crm.bean.account.SubscriptionType.class, 
                        com.redknee.app.crm.bean.core.SubscriptionType.class));
        ctx.put(SubscriptionTypeHome.class, subTypehome);

        PricePlan pricePlan = new PricePlan();
        pricePlan.setId(PRICE_PLAN_ID);
        pricePlan.setCurrentVersion(1);

        Home ppHome = new AdapterHome(
                ctx, 
                new PricePlanTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
        ctx.put(PricePlanHome.class, ppHome);

        PricePlanVersion ppVersion = new PricePlanVersion();
        ppVersion.setId(PRICE_PLAN_ID);
        ppVersion.setVersion(1);

        Home ppvHome = new AdapterHome(
                ctx, 
                new PricePlanVersionTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                        com.redknee.app.crm.bean.PricePlanVersion.class, 
                        com.redknee.app.crm.bean.core.PricePlanVersion.class));
        ctx.put(PricePlanVersionHome.class, ppvHome);

        Home packHome = new ServicePackageTransientHome(ctx);
        ctx.put(ServicePackageHome.class, packHome);

        try
        {
            subClasshome.create(ctx, subscriptionClass);
            subClasshome.create(ctx, inSubscriptionClass);
            subTypehome.create(ctx, subscriptionType);
            subTypehome.create(ctx, inSubscriptionType);
            ppHome.create(ctx, pricePlan);
            ppvHome.create(ctx, ppVersion);
        }
        catch (HomeException e)
        {
            fail("Error in setup:" + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void tearDown()
    {
        bulkSubscriber = null;
        visitor = null;
        super.tearDown();
    }

    private void configurePostpaid(BulkLoadSubscriber bulkSub)
    {
        bulkSub.setBillingType(SubscriberTypeEnum.POSTPAID);
        bulkSub.setDeposit("10000");
        bulkSub.setLastDepositDate("10/10/2008 05:05:05");
        bulkSub.setCreditLimit("10000");
        bulkSub.setDiscountClass("2");
    }

    private void configurePrepaid(BulkLoadSubscriber bulkSub)
    {
        bulkSub.setBillingType(SubscriberTypeEnum.PREPAID);
        bulkSub.setExpiryDate("10/10/2009 05:05:05");
    }

    public void testValidationOfMandatoryBAN()
    {
        final Context ctx = getContext();
        configurePostpaid(bulkSubscriber);

        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        // verify the successful case
        bulkSubscriber.setBAN(BAN);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }
    }

    public void testValidationOfMandatorySPID()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setSpid(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setSpid("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatorySubscriptionClass()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setSubscriptionClass(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setSubscriptionClass("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryTechnology()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setTechnology(AbstractBulkLoadSubscriber.DEFAULT_TECHNOLOGY);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryBillingType()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setMSISDN(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setMSISDN("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryDeposit()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setDeposit(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setDeposit("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryLastDepositDate()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setLastDepositDate(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setLastDepositDate("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryCreditLimit()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setCreditLimit(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setCreditLimit("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryPricePlan()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setPricePlan(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setPricePlan("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryPackageID()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setPackageId(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setPackageId("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    public void testValidationOfMandatoryDiscountClass()
    {
        final Context ctx = getContext();
        bulkSubscriber.setBAN(BAN);
        configurePostpaid(bulkSubscriber);

        // verify the successful case
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
        }
        catch (AgentException e)
        {
            fail("Should be successful:" + e.getMessage());
        }

        bulkSubscriber.setDiscountClass(null);
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }

        bulkSubscriber.setDiscountClass("");
        try
        {
            visitor.getSubscriber(ctx, bulkSubscriber);
            fail("No Exception thrown");
        }
        catch (AgentException e)
        {
        }
    }

    BulkLoadSubscriber bulkSubscriber;
    SubscriberCreateVisitor visitor;
}

class FakeMessageMgrSPI implements MessageMgrSPI
{
    public String get(final Context ctx, final String key, final Class module, final Lang lang,
            final String defaultValue, final Object[] args)
    {
        return defaultValue;
    }
}