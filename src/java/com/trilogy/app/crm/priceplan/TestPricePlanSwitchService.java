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
package com.trilogy.app.crm.priceplan;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionClass;
import com.trilogy.app.crm.bean.account.SubscriptionClassHome;
import com.trilogy.app.crm.bean.account.SubscriptionClassTransientHome;
import com.trilogy.app.crm.bean.account.SubscriptionType;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeHome;
import com.trilogy.app.crm.bean.account.SubscriptionTypeTransientHome;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author victor.stratan@redknee.com
 */
public class TestPricePlanSwitchService extends ContextAwareTestCase
{
    private static final String BAN = "12345";
    private static final String SUB_ID = "12345-1";
    private static final int SPID = 1;
    private static final long SUBSCRIPTION_CLASS_ID = 2;
    private static final long IN_SUBSCRIPTION_CLASS_ID = 1;
    private static final String MSISDN = "407220000123";
    private static final long SUBSCRIPTION_TYPE_ID = 5;
    private static final long IN_SUBSCRIPTION_TYPE_ID = 3;
    private static final long SERVICE_COMMON_ID = 10;
    private static final Long SERVICE_COMMON_ID_OBJ = Long.valueOf(SERVICE_COMMON_ID);
    private static final long SERVICE_ONE_ID = 11;
    private static final Long SERVICE_ONE_ID_OBJ = Long.valueOf(SERVICE_ONE_ID);
    private static final long SERVICE_TWO_ID = 12;
    private static final long SERVICE_TWO_ID_OBJ = Long.valueOf(SERVICE_TWO_ID);
    private static final long PRICE_PLAN_ID = 123;
    private static final int PRICE_PLAN_VER_ID = 1;
    private static final int PRICE_PLAN_VER_UPGRADE_ID = 2;
    private static final long SECOND_PRICE_PLAN_ID = 125;
    private static final int SECOND_PRICE_PLAN_VER_ID = 1;

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPricePlanSwitchService(final String name)
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

        final TestSuite suite = new TestSuite(TestPricePlanSwitchService.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        sub_ = new Subscriber();
        sub_.setContext(ctx);
        sub_.setSpid(SPID);
        sub_.setBAN(BAN);
        sub_.setId(SUB_ID);
        sub_.setMSISDN(MSISDN);
        sub_.setPricePlan(PRICE_PLAN_ID);
        sub_.setPricePlanVersion(PRICE_PLAN_VER_ID);

        SubscriptionClass subscriptionClass = new SubscriptionClass();
        subscriptionClass.setId(SUBSCRIPTION_CLASS_ID);
        subscriptionClass.setName("some name");
        subscriptionClass.setSubscriptionType(SUBSCRIPTION_TYPE_ID);
        subscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);
        subscriptionClass.setTechnologyType(TechnologyEnum.ANY_INDEX);

        SubscriptionClass inSubscriptionClass = new SubscriptionClass();
        inSubscriptionClass.setId(IN_SUBSCRIPTION_CLASS_ID);
        inSubscriptionClass.setName("some name");
        inSubscriptionClass.setSubscriptionType(IN_SUBSCRIPTION_TYPE_ID);
        inSubscriptionClass.setSegmentType(SubscriberTypeEnum.HYBRID_INDEX);
        inSubscriptionClass.setTechnologyType(TechnologyEnum.ANY_INDEX);

        Home subClasshome = new SubscriptionClassTransientHome(ctx);
        ctx.put(SubscriptionClassHome.class, subClasshome);

        SubscriptionType subscriptionType = new SubscriptionType();
        subscriptionType.setId(SUBSCRIPTION_TYPE_ID);
        subscriptionType.setName("type name");
        subscriptionType.setType(SubscriptionTypeEnum.MOBILE_WALLET_INDEX);

        SubscriptionType inSubscriptionType = new SubscriptionType();
        inSubscriptionType.setId(IN_SUBSCRIPTION_TYPE_ID);
        inSubscriptionType.setName("other type name");
        inSubscriptionType.setType(SubscriptionTypeEnum.AIRTIME_INDEX);

        Home subTypehome = new SubscriptionTypeTransientHome(ctx);
        ctx.put(SubscriptionTypeHome.class, subTypehome);

        Service serviceCommon = new Service();
        serviceCommon.setID(SERVICE_COMMON_ID);
        serviceCommon.setTechnology(TechnologyEnum.ANY);
        serviceCommon.setType(ServiceTypeEnum.GENERIC);

        Service serviceOne = new Service();
        serviceOne.setID(SERVICE_ONE_ID);
        serviceOne.setTechnology(TechnologyEnum.ANY);
        serviceOne.setType(ServiceTypeEnum.GENERIC);

        Service serviceTwo = new Service();
        serviceTwo.setID(SERVICE_TWO_ID);
        serviceTwo.setTechnology(TechnologyEnum.ANY);
        serviceTwo.setType(ServiceTypeEnum.GENERIC);

        Home serviceHome = new ServiceTransientHome(ctx);
        ctx.put(ServiceHome.class, serviceHome);

        PricePlan pricePlan = new PricePlan();
        pricePlan.setId(PRICE_PLAN_ID);
        pricePlan.setCurrentVersion(PRICE_PLAN_VER_ID);

        PricePlan secondPricePlan = new PricePlan();
        secondPricePlan.setId(SECOND_PRICE_PLAN_ID);
        secondPricePlan.setCurrentVersion(PRICE_PLAN_VER_ID);

        Home ppHome = new PricePlanTransientHome(ctx);
        ctx.put(PricePlanHome.class, ppHome);

        PricePlanVersion ppVersion = new PricePlanVersion();
        ppVersion.setId(PRICE_PLAN_ID);
        ppVersion.setVersion(PRICE_PLAN_VER_ID);

        ServiceFee2 feeCommon = new ServiceFee2();
        feeCommon.setServiceId(SERVICE_COMMON_ID);
        ServiceFee2 feeOne = new ServiceFee2();
        feeOne.setServiceId(SERVICE_ONE_ID);
        ServiceFee2 feeTwo = new ServiceFee2();
        feeTwo.setServiceId(SERVICE_TWO_ID);
        ppVersion.getServicePackageVersion().getServiceFees().put(SERVICE_COMMON_ID_OBJ, feeCommon);
        ppVersion.getServicePackageVersion().getServiceFees().put(SERVICE_ONE_ID_OBJ, feeOne);

        PricePlanVersion ppVersionUpgrade = new PricePlanVersion();
        ppVersionUpgrade.setId(PRICE_PLAN_ID);
        ppVersionUpgrade.setVersion(PRICE_PLAN_VER_UPGRADE_ID);

        secondPpVersion_ = new PricePlanVersion();
        secondPpVersion_.setId(SECOND_PRICE_PLAN_ID);
        secondPpVersion_.setVersion(SECOND_PRICE_PLAN_VER_ID);

        secondPpVersion_.getServicePackageVersion().getServiceFees().put(SERVICE_COMMON_ID_OBJ, feeCommon);
        secondPpVersion_.getServicePackageVersion().getServiceFees().put(SERVICE_TWO_ID_OBJ, feeTwo);

        ppvHome_ = new PricePlanVersionTransientHome(ctx);
        ctx.put(PricePlanVersionHome.class, ppvHome_);

        Home packHome = new ServicePackageTransientHome(ctx);
        ctx.put(ServicePackageHome.class, packHome);

        Home ssHome = new SubscriberServicesTransientHome(ctx);
        ctx.put(SubscriberServicesHome.class, ssHome);

        try
        {
            subClasshome.create(ctx, subscriptionClass);
            subClasshome.create(ctx, inSubscriptionClass);
            subTypehome.create(ctx, subscriptionType);
            subTypehome.create(ctx, inSubscriptionType);

            serviceHome.create(ctx, serviceCommon);
            serviceHome.create(ctx, serviceOne);
            serviceHome.create(ctx, serviceTwo);

            ppHome.create(ctx, pricePlan);
            ppHome.create(ctx, secondPricePlan);

            ppvHome_.create(ctx, ppVersion);
//            ppvHome.create(ctx, ppVersionUpgrade);
            ppvHome_.create(ctx, secondPpVersion_);
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
        super.tearDown();
    }

    private void markServiceSubscribed(final Context ctx, final String subId, final long serviceID) throws HomeException
    {
        SubscriberServices ssRecord =  new SubscriberServices();
        ssRecord.setSubscriberId(subId);
        ssRecord.setServiceId(serviceID);
        ssRecord.setProvisionedState(ServiceStateEnum.PROVISIONED);

        final Home h = (Home) ctx.get(SubscriberServicesHome.class);
        h.create(ctx, ssRecord);
    }

    public void testNoServicePPSwitch()
    {
        final Context ctx = getContext();
        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testOneCommonServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();
        markServiceSubscribed(ctx, SUB_ID, SERVICE_COMMON_ID);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Default service should be subscribed", true, it.hasNext());

        final SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Default service should be subscribed", SERVICE_COMMON_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testOneUncommonServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();
        markServiceSubscribed(ctx, SUB_ID, SERVICE_ONE_ID);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testTwoServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();
        markServiceSubscribed(ctx, SUB_ID, SERVICE_COMMON_ID);
        markServiceSubscribed(ctx, SUB_ID, SERVICE_ONE_ID);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Default service should be subscribed", true, it.hasNext());

        final SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Default service should be subscribed", SERVICE_COMMON_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testOneDefaultServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();

        final ServiceFee2 fee;
        fee = (ServiceFee2) secondPpVersion_.getServicePackageVersion().getServiceFees().get(SERVICE_TWO_ID_OBJ);
        fee.setServicePreference(ServicePreferenceEnum.DEFAULT);
        secondPpVersion_.setVersion(secondPpVersion_.getVersion() + 1);

        ppvHome_.create(ctx, secondPpVersion_);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Default service should be subscribed", true, it.hasNext());

        final SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Default service should be subscribed", SERVICE_TWO_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testOneMandatoryServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();

        final ServiceFee2 fee;
        fee = (ServiceFee2) secondPpVersion_.getServicePackageVersion().getServiceFees().get(SERVICE_TWO_ID_OBJ);
        fee.setServicePreference(ServicePreferenceEnum.MANDATORY);
        secondPpVersion_.setVersion(secondPpVersion_.getVersion() + 1);

        ppvHome_.create(ctx, secondPpVersion_);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Default service should be subscribed", true, it.hasNext());

        final SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Default service should be subscribed", SERVICE_TWO_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Only one service should be subscribed", false, it.hasNext());
    }

    public void testTwoDefaultServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();
        markServiceSubscribed(ctx, SUB_ID, SERVICE_COMMON_ID);

        final ServiceFee2 fee;
        fee = (ServiceFee2) secondPpVersion_.getServicePackageVersion().getServiceFees().get(SERVICE_TWO_ID_OBJ);
        fee.setServicePreference(ServicePreferenceEnum.DEFAULT);
        secondPpVersion_.setVersion(secondPpVersion_.getVersion() + 1);

        ppvHome_.create(ctx, secondPpVersion_);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Two service should be subscribed", true, it.hasNext());

        SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Two service should be subscribed", true, it.hasNext());
        SubscriberServices bean2 = (SubscriberServices) it.next();

        if (SERVICE_TWO_ID != bean.getServiceId())
        {
            SubscriberServices temp = bean;
            bean = bean2;
            bean2 = temp;
        }
        assertEquals("Default service should be subscribed", SERVICE_TWO_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Default service should be subscribed", SERVICE_COMMON_ID, bean2.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean2.getSubscriberId());
        assertEquals("Only two service should be subscribed", false, it.hasNext());
    }

    public void testTwoMandatoryServicePPSwitch() throws HomeException
    {
        final Context ctx = getContext();
        markServiceSubscribed(ctx, SUB_ID, SERVICE_COMMON_ID);

        final ServiceFee2 fee;
        fee = (ServiceFee2) secondPpVersion_.getServicePackageVersion().getServiceFees().get(SERVICE_TWO_ID_OBJ);
        fee.setServicePreference(ServicePreferenceEnum.MANDATORY);
        secondPpVersion_.setVersion(secondPpVersion_.getVersion() + 1);

        ppvHome_.create(ctx, secondPpVersion_);

        sub_.switchPricePlan(ctx, SECOND_PRICE_PLAN_ID, SECOND_PRICE_PLAN_VER_ID);

        final Iterator it = sub_.getAllNonUnprovisionedStateServices().iterator();
        assertEquals("Two service should be subscribed", true, it.hasNext());

        SubscriberServices bean = (SubscriberServices) it.next();
        assertEquals("Two service should be subscribed", true, it.hasNext());
        SubscriberServices bean2 = (SubscriberServices) it.next();

        if (SERVICE_TWO_ID != bean.getServiceId())
        {
            SubscriberServices temp = bean;
            bean = bean2;
            bean2 = temp;
        }
        assertEquals("Default service should be subscribed", SERVICE_TWO_ID, bean.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean.getSubscriberId());
        assertEquals("Default service should be subscribed", SERVICE_COMMON_ID, bean2.getServiceId());
        assertEquals("Default service should be subscribed", SUB_ID, bean2.getSubscriberId());
        assertEquals("Only two service should be subscribed", false, it.hasNext());
    }

    private Subscriber sub_;
    private PricePlanVersion secondPpVersion_;
    private Home ppvHome_;
}
