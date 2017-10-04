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
package com.trilogy.app.crm.provision;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryTransientHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriptionProvisioningHistoryTransientHome;
import com.trilogy.app.crm.client.ConnectionStatus;
import com.trilogy.app.crm.subscriber.provision.SubscriberServicesProvisioningHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;
import com.trilogy.app.crm.voicemail.VoiceMailServer;
import com.trilogy.app.crm.voicemail.VoiceMailService;
import com.trilogy.app.crm.voicemail.client.ExternalProvisionResult;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.service.voicemail.mpathix.MpathixService;
import com.trilogy.driver.voicemail.mpathix.MpathixTask;

/**
 * Test Voicemail service provisioning and unprovisioning done by VoicemailProvisionAgent and VoicemailUnprovisionAgent
 * together with SubscriberProvisionServicesHome.
 *
 * @author victor.stratan@redknee.com
 */
public class TestVoicemailHandlerAgents extends ContextAwareTestCase
{
    public static final String BAN = "123";
    public static final String SUB_ID = BAN + "-45";
    public static final String MSISDN = "6470000123";
    public static final long SRV_ID = 1234L;
    public static final Long SRV_OBJ_ID = Long.valueOf(1234L);
    public static final long PP_ID = 12;
    public static final int PPV_ID = 1;


    public TestVoicemailHandlerAgents(final String name)
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

        final TestSuite suite = new TestSuite(TestVoicemailHandlerAgents.class);

        return suite;
    }

    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();

        Context ctx = getContext();

        ctx.put(SubscriptionProvisioningHistoryHome.class, new SubscriptionProvisioningHistoryTransientHome(ctx));
        ctx.put(SubscriberSubscriptionHistoryHome.class, new SubscriberSubscriptionHistoryTransientHome(ctx));

        Calendar cal = Calendar.getInstance();
        now_ = cal.getTime();
        cal = CalendarSupportHelper.get(ctx).clearTimeOfDay(cal);
        today_ = cal.getTime();
        yesturday_ = CalendarSupportHelper.get(ctx).findDateDaysBefore(1, cal).getTime();

        home_ = new SubscriberTransientHome(ctx);
        home_ = new SubscriberServicesProvisioningHome(ctx, home_);
        home_ = new ContextualizingHome(ctx, home_);
        ctx.put(SubscriberHome.class, home_);

        service_ = new TestVoiceMailService();
        
        ctx.put(VoiceMailServer.class, new VoiceMailServer(ctx));
        ctx.put(VoiceMailService.class, service_);
  
        UnitTestSupport.installLicenseManager(getContext());

        try
        {
            UnitTestSupport.createLicense(getContext(), LicenseConstants.LICENSE_APP_CRM_VOICE_MAIL);
            UnitTestSupport.createLicense(getContext(), LicenseConstants.LICENSE_APP_CRM_VOICE_MAIL_MPATHIX);
        }
        catch (HomeException e)
        {
            final IllegalStateException newException = new IllegalStateException();
            newException.initCause(e);
            throw newException;
        }

        SysFeatureCfg config = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (config == null)
        {
            config = new SysFeatureCfg();
            ctx.put(SysFeatureCfg.class, config);
        }
        config.setForceReprovisionForPrepaid(true);

        Home serviceHome = new ServiceTransientHome(ctx);
        serviceHome = new AdapterHome(
                ctx, 
                serviceHome, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Service, com.redknee.app.crm.bean.core.Service>(
                        com.redknee.app.crm.bean.Service.class, 
                        com.redknee.app.crm.bean.core.Service.class));
        ctx.put(ServiceHome.class, serviceHome);

        Home subServicesHome = new SubscriberServicesTransientHome(ctx);
        ctx.put(SubscriberServicesHome.class, subServicesHome);

        Home accountHome = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, accountHome);

        Home pricePlanHome = new PricePlanTransientHome(ctx);
        ctx.put(PricePlanHome.class, pricePlanHome);

        Home pricePlanVersionHome = new PricePlanVersionTransientHome(ctx);
        ctx.put(PricePlanVersionHome.class, pricePlanVersionHome);

        Service service = new Service();
        service.setID(SRV_ID);
        service.setType(ServiceTypeEnum.VOICEMAIL);
        service.setVmPlanId("123");
        service.setContext(ctx);

        subService_ = new SubscriberServices();
        subService_.setServiceId(SRV_ID);
        subService_.setSubscriberId(SUB_ID);
        subService_.setProvisionedState(ServiceStateEnum.PENDING);
        subService_.setStartDate(yesturday_);

        Account account = new Account();
        account.setBAN(BAN);

        PricePlan plan = new PricePlan();
        plan.setId(PP_ID);
        plan.setName("Test plan");
        plan.setEnabled(true);

        PricePlanVersion version = new PricePlanVersion();
        version.setId(PP_ID);
        version.setVersion(PPV_ID);
        version.setCreditLimit(10000L);
        version.setDeposit(100L);
        version.getServicePackageVersion().getServiceFees().put(SRV_OBJ_ID, service);

        try
        {
            serviceHome.create(ctx, service);
            //subServicesHome.create(ctx, subService);
            accountHome.create(ctx, account);
            pricePlanHome.create(ctx, plan);
            pricePlanVersionHome.create(ctx, version);
        }
        catch (HomeException e)
        {
            throw new IllegalStateException("Cannot continue with tests.", e);
        }

        subscriber_ = new Subscriber();
        subscriber_.setContext(getContext());
        subscriber_.setId(SUB_ID);
        subscriber_.setBAN(BAN);
        subscriber_.setMSISDN(MSISDN);
        subscriber_.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber_.setState(SubscriberStateEnum.ACTIVE);
        //subscriber_.getServices().add(SRV_OBJ_ID);
        subscriber_.setPricePlan(PP_ID);
        subscriber_.setPricePlanVersion(PPV_ID);

        subscriber_.getAllNonUnprovisionedStateServices().add(subService_);
    }

    public void testProvisionVoicemailForPostpaid() throws HomeException
    {
        final Context ctx = getContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testProvisionVoicemailForPrepaid() throws HomeException
    {
        final Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber_.setState(SubscriberStateEnum.AVAILABLE);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testNotProvisionVoicemailAgainForPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();
        
        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testNotProvisionVoicemailAgainForPrepaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber_.setState(SubscriberStateEnum.AVAILABLE);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testUnProvisionVoicemailPostpaidOnInactive() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testUnProvisionVoicemailPrepaidOnInactive() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber_.setState(SubscriberStateEnum.AVAILABLE);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testSuspendUnsuspendVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.SUSPENDED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(1, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.activateAccountList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testSuspendDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.SUSPENDED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testWarnDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_WARN);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testWarnDunnedDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_WARN);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_SUSPENDED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testWarnPTPDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_WARN);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.PROMISE_TO_PAY);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testWarnInarrearsDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_WARN);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.IN_ARREARS);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testWarnIncollectionDeactivateVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.NON_PAYMENT_WARN);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.IN_COLLECTION);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testSuspendUnsuspendVoicemailPostpaidWithServicesForDisplay() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        final SubscriberServices bean = new SubscriberServices();
        bean.setSubscriberId(subscriber_.getId());
        bean.setServiceId(SRV_ID);
        bean.setMandatory(false);
        bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
        subscriber_.getAllNonUnprovisionedStateServices().add(bean);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.SUSPENDED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue("Service should be provisioned", sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(1, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.activateAccountList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testLockUnlockVoicemailPrepaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.LOCKED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(1, service_.activateAccountList.size());
        assertEquals(1, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.activateAccountList.get(0));
        assertEquals(MSISDN, service_.deactivateAccountList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testExpireActivateVoicemailPrepaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.EXPIRED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testExpireDeactivateVoicemailPrepaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.EXPIRED);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testDeactivateReactivateVoicemailPrepaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.setSubscriberType(SubscriberTypeEnum.PREPAID);

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.INACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.setState(SubscriberStateEnum.ACTIVE);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(2, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.addUserList.get(1));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testChangeMSISDNVoicemailPostpaid() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue("Service missing from provisioned set", sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        final String newMsisdn = "40" + MSISDN;
        sub.setMSISDN(newMsisdn);

        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(1, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.changeMsisdnList.get(0));
        assertTrue("Service missing from provisioned set", sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testUnprovisionVoicemailPostpaidWhenUnselected() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
//        Home subServicesHome = (Home) ctx.get(SubscriberServicesHome.class);
//        subServicesHome.removeAll(ctx);
        sub.getAllNonUnprovisionedStateServices().clear();
        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(1, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertEquals(MSISDN, service_.deleteUserList.get(0));
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    public void testProvisionVoicemailPostpaidWhenSelected() throws HomeException, CloneNotSupportedException
    {
        Context ctx = getContext().createSubContext();

        subscriber_.getAllNonUnprovisionedStateServices().clear();
        Subscriber sub = (Subscriber) home_.create(ctx, subscriber_);

        assertEquals(0, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertFalse(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));

        ctx = getContext().createSubContext();
        ctx.put(Lookup.OLDSUBSCRIBER, sub);

        sub = (Subscriber) sub.clone();
        sub.getAllNonUnprovisionedStateServices().add(subService_);
        sub = (Subscriber) home_.store(ctx, sub);

        assertEquals(1, service_.addUserList.size());
        assertEquals(0, service_.deleteUserList.size());
        assertEquals(0, service_.changePasswordList.size());
        assertEquals(0, service_.changeMsisdnList.size());
        assertEquals(0, service_.activateAccountList.size());
        assertEquals(0, service_.deactivateAccountList.size());
        assertEquals(MSISDN, service_.addUserList.get(0));
        assertTrue(sub.getProvisionedServices(ctx).contains(SRV_OBJ_ID));
    }

    private Home home_;
    private Subscriber subscriber_;
    private SubscriberServices subService_;
    private TestVoiceMailService service_;
    private Date now_;
    private Date today_;
    private Date yesturday_;
}

class TestVoiceMailService implements VoiceMailService
{
    
    private static final String SERVICE_NAME = "TestVoicemailHandlerAgents";
    private static final String SERVICE_DESCRIPTION = "TestVoicemailHandlerAgents";
    
    public ArrayList addUserList = new ArrayList();
    public ArrayList deleteUserList = new ArrayList();
    public ArrayList activateAccountList = new ArrayList();
    public ArrayList deactivateAccountList = new ArrayList();
    public ArrayList changePasswordList = new ArrayList();
    public ArrayList changeMsisdnList = new ArrayList();

    
    public TestVoiceMailService(){}
    
    
    public ExternalProvisionResult provision(Context ctx, Subscriber sub, ServiceBase service)
    {
        addUserList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 
    }
    public ExternalProvisionResult unprovision(Context ctx, Subscriber sub, ServiceBase service)
    {
        deleteUserList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 
        
    }
    public ExternalProvisionResult deactivate(Context ctx,Subscriber sub, ServiceBase service)
    {
        deactivateAccountList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 

    }
    public ExternalProvisionResult activate(Context ctx,Subscriber sub, ServiceBase service)
    {
        activateAccountList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 

    }
    public ExternalProvisionResult changeMsisdn(Context ctx, Subscriber sub, String newMsisdn)
    {
        changeMsisdnList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 

    }
    public ExternalProvisionResult resetPassword(Context ctx, Subscriber sub,  String password)
    {
        changePasswordList.add(sub.getMSISDN());
        return new ExternalProvisionResult(0,0); 

    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getRemoteInfo()
     */
    public String getRemoteInfo()
    {
        return "";
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceDescription()
     */
    public String getDescription()
    {
        return SERVICE_DESCRIPTION;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#getServiceName()
     */
    public String getName()
    {
        return SERVICE_NAME;
    }


    /* (non-Javadoc)
     * @see com.redknee.app.crm.client.ExternalService#isServiceAlive()
     */
    public boolean isAlive()
    {
        return true;
    }


    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus(getRemoteInfo(), isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }

}



class TestMpathixService extends MpathixService
{
    public ArrayList addUserList = new ArrayList();
    public ArrayList deleteUserList = new ArrayList();
    public ArrayList modifyUserList = new ArrayList();
    public ArrayList activateAccountList = new ArrayList();
    public ArrayList deactivateAccountList = new ArrayList();

    public TestMpathixService(Context ctx)
    {
        super(ctx, null);
    }

    @Override
    public boolean isConnected2VM()
    {
        return true;
    }

    @Override
    public MpathixTask addUser(String userID, String password, int planID, boolean waitOnTask, long timeout)
    {
        addUserList.add(userID);
        final MpathixTask task = new TestMpathixTask("aaa");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask addUser(String userID, String password, int planID, List params, boolean waitOnTask,
            long timeout)
    {
        addUserList.add(userID);
        final MpathixTask task = new TestMpathixTask("aaa");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask deleteUser(String userID, boolean waitOnTask, long timeout)
    {
        deleteUserList.add(userID);
        final MpathixTask task = new TestMpathixTask("bbb");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask deleteUser(String userID, List params, boolean waitOnTask, long timeout)
    {
        deleteUserList.add(userID);
        final MpathixTask task = new TestMpathixTask("bbb");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask modifyUser(String userID, List params, boolean waitOnTask, long timeout)
    {
        modifyUserList.add(userID);
        final MpathixTask task = new TestMpathixTask("ccc");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask activateAccount(String userID, boolean waitOnTask, long timeout)
    {
        activateAccountList.add(userID);
        final MpathixTask task = new TestMpathixTask("ddd");
        task.setProcessed();
        return task;
    }

    @Override
    public MpathixTask deactivateAccount(String userID, boolean waitOnTask, long timeout)
    {
        deactivateAccountList.add(userID);
        final MpathixTask task = new TestMpathixTask("eee");
        task.setProcessed();
        return task;
    }

    class TestMpathixTask extends MpathixTask
    {
        public TestMpathixTask(String request)
        {
            super(request);
        }

        @Override
        public int getErrorCode()
        {
            return MpathixTask.SUCCESS;
        }
    }
}
