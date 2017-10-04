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
package com.trilogy.app.crm.bundle.web;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bundle.AddBundleRequest;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestFakeCRMBundleCategory;
import com.trilogy.app.crm.unit_test.TestFakeCRMBundleProfile;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;

/**
 * Tests BundleUploadRequestServicer.process() logic.
 *
 * @author victor.stratan@redknee.com
 */
public class TestBundleUploadRequestServicer extends ContextAwareTestCase
{
    public static final String BAN = "123";
    public static final String SUB_ID = BAN + "-45";
    public static final String MSISDN = "6470000123";
    public static final int SPID = 1;
    public static final long BUNDLE_ID = 15;

    public TestBundleUploadRequestServicer(String name)
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

        final TestSuite suite = new TestSuite(TestBundleUploadRequestServicer.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        calendar_ = Calendar.getInstance();
        
        CalendarSupportHelper.get(ctx).clearTimeOfDay(calendar_);
        today_ = calendar_.getTime();

        calendar_.add(Calendar.DAY_OF_MONTH, -5);
        past_ = calendar_.getTime();

        calendar_.add(Calendar.YEAR, 1);
        future_ = calendar_.getTime();

        Home home;
//        home = new AccountTransientHome(ctx);
//        ctx.put(AccountHome.class, home);

//        home = new BillCycleTransientHome(ctx);
//        ctx.put(BillCycleHome.class, home);

//        home = new CRMSpidTransientHome(ctx);
//        ctx.put(CRMSpidHome.class, home);

        home = new SubscriberTransientHome(ctx);
        subHome_ = new TestFakeSubHomeRememberSub(ctx, home);
        ctx.put(SubscriberHome.class, subHome_);

        profileService_ = new TestFakeCRMBundleProfile(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMBundleProfile.class, profileService_);

        categoryService_ = new TestFakeCRMBundleCategory(ctx);
        ctx.put(com.redknee.app.crm.bundle.service.CRMBundleCategory.class, categoryService_);

        final BundleProfile bundle = new BundleProfile();
        bundle.setBundleId(BUNDLE_ID);
        bundle.setSpid(SPID);
        bundle.setAuxiliary(true);
        bundle.setSegment(BundleSegmentEnum.POSTPAID);
        HashMap bundleCategoryIds = new HashMap();
        BundleCategoryAssociation assoc = new BundleCategoryAssociation();
        assoc.setCategoryId(10 + UnitTypeEnum.VOLUME_SECONDS_INDEX);
        assoc.setContext(getContext());
        bundleCategoryIds.put(Integer.valueOf(1), assoc);
        bundle.setBundleCategoryIds(bundleCategoryIds);

        final BundleCategory category = new BundleCategory();
        category.setCategoryId(10 + UnitTypeEnum.VOLUME_SECONDS_INDEX);
        category.setName(UnitTypeEnum.VOLUME_SECONDS.getDescription());
        category.setUnitType(UnitTypeEnum.VOLUME_SECONDS);

        final Home msisdnHome = new AdapterHome(
                getContext(), 
                new MsisdnTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Msisdn, com.redknee.app.crm.bean.core.Msisdn>(
                        com.redknee.app.crm.bean.Msisdn.class, 
                        com.redknee.app.crm.bean.core.Msisdn.class));
        ctx.put(MsisdnHome.class, msisdnHome);

        final Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(MSISDN);
        msisdn.setBAN(BAN);
        msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        final Home msisdnHistoryHome = new AdapterHome(
                ctx, 
                new MsisdnMgmtHistoryTransientHome(ctx), 
                new ExtendedBeanAdapter<com.redknee.app.crm.numbermgn.MsisdnMgmtHistory, com.redknee.app.crm.bean.core.MsisdnMgmtHistory>(
                        com.redknee.app.crm.numbermgn.MsisdnMgmtHistory.class, 
                        com.redknee.app.crm.bean.core.MsisdnMgmtHistory.class));
        ctx.put(MsisdnMgmtHistoryHome.class, msisdnHistoryHome);

        msisdnHistoryID = 1;

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setIdentifier(msisdnHistoryID++);
        msisdnHistory.setTerminalId(MSISDN);
        msisdnHistory.setBAN(BAN);
        msisdnHistory.setSubscriberId(SUB_ID);
        try
        {
            msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(ctx).getId());
        }
        catch (HomeException e)
        {
            fail(e.getMessage());
        }
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(past_);

        try
        {
            profileService_.createBundle(ctx, bundle);
            categoryService_.createCategory(ctx, category);
            msisdnHome.create(ctx, msisdn);
            msisdnHistoryHome.create(ctx, msisdnHistory);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot continue with the test!", e);
        }

        request_ = new AddBundleRequest();
        request_.setId(BUNDLE_ID);
        request_.setMSISDN(MSISDN);

        request_.setStartDate(today_);
        request_.setEndDate(future_);

        servicer_ = new BundleUploadRequestServicer();
        
        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());

    }

    protected void tearDown()
    {
        servicer_ = null;
        subHome_ = null;
        request_ = null;
        profileService_ = null;
        calendar_ = null;
        today_ = null; future_ = null;

        super.tearDown();
    }

    private Subscriber createTestSub(final Context ctx, final String ban, final String id,
            final SubscriberTypeEnum type, final SubscriberStateEnum state)
            throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberHome.class);

        final Subscriber sub = new Subscriber();
        sub.setSubscriberType(type);
        sub.setState(state);
        sub.setBAN(ban);
        sub.setId(id);
        sub.setMSISDN(MSISDN);
        sub.setSpid(SPID);

        home.create(ctx, sub);
        return sub;
    }

    private void createMsisdnRecords(final Context ctx, final String msisdn, final String ban, final String subId,
            final SubscriberTypeEnum billingType, final long subType, final Date start, final Date end)
        throws HomeException
    {
        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setIdentifier(msisdnHistoryID++);
        msisdnHistory.setTerminalId(msisdn);
        msisdnHistory.setBAN(ban);
        msisdnHistory.setSubscriberId(subId);
        msisdnHistory.setSubscriptionType(subType);
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(start);
        if (end != null)
        {
            msisdnHistory.setEndTimestamp(end);
            msisdnHistory.setLatest(false);
        }

        final Home homeHistory = (Home) ctx.get(MsisdnMgmtHistoryHome.class);

        homeHistory.create(ctx, msisdnHistory);
    }

    public void testSubscriberSearch() throws HomeException
    {
        final Context ctx = getContext();
        final String BAN_2 = "456";

        final long inID = SubscriptionType.getINSubscriptionType(ctx).getId();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        Date start, end;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(past_);
        cal.add(Calendar.SECOND, -1);
        end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        start = cal.getTime();

        createTestSub(ctx, BAN, BAN + "-20", SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        createMsisdnRecords(ctx, MSISDN, BAN, BAN + "-20", SubscriberTypeEnum.POSTPAID, inID, start, end);

        cal.add(Calendar.SECOND, -1);
        end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        start = cal.getTime();

        createTestSub(ctx, BAN, BAN + "-75", SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        createMsisdnRecords(ctx, MSISDN, BAN, BAN + "-75", SubscriberTypeEnum.POSTPAID, inID, start, end);

        cal.add(Calendar.SECOND, -1);
        end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -9);
        start = cal.getTime();

        createTestSub(ctx, BAN_2, BAN_2 + "-5", SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        createMsisdnRecords(ctx, MSISDN, BAN_2, BAN_2 + "-2", SubscriberTypeEnum.POSTPAID, inID, start, end);

        cal.add(Calendar.SECOND, -1);
        end = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        start = cal.getTime();

        createTestSub(ctx, BAN_2, BAN_2 + "-29", SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.INACTIVE);
        createMsisdnRecords(ctx, MSISDN, BAN_2, BAN_2 + "-29", SubscriberTypeEnum.POSTPAID, inID, start, end);

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);
        assertEquals("Update on the wrong subscriber", SUB_ID, sub.getId());
    }

    public void testBundleFeeDateSetting() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", future_, fee.getEndDate());
    }

    public void testBundleFeeDateSettingNoStartDate() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setStartDate(null);
        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", future_, fee.getEndDate());
    }

    public void testBundleFeeDateSettingEndDateWithPayments() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setPaymentNum(9);
        calendar_.setTime(today_);
        calendar_.add(Calendar.MONTH, 9);
        final Date endDate = calendar_.getTime();

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", endDate, fee.getEndDate());
    }

    public void testBundleFeeDateSettingNoEndDateWithPayments() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setEndDate(null);
        request_.setPaymentNum(7);
        calendar_.setTime(today_);
        calendar_.add(Calendar.MONTH, 7);
        final Date endDate = calendar_.getTime();

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", endDate, fee.getEndDate());
    }

    public void testBundleFeeDateSettingNoEndDateNoPayments() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setEndDate(null);
        request_.setPaymentNum(0);
        calendar_.setTime(today_);
        calendar_.add(Calendar.YEAR, 20);
        final Date endDate = calendar_.getTime();

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", endDate, fee.getEndDate());
    }

    public void testBundleFeeDateSettingNoStartNoEndDateWithPayments() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setStartDate(null);
        request_.setEndDate(null);
        request_.setPaymentNum(8);
        calendar_.setTime(today_);
        calendar_.add(Calendar.MONTH, 8);
        final Date endDate = calendar_.getTime();

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", endDate, fee.getEndDate());
    }

    public void testBundleFeeDateSettingNoStartNoEndDateNoPayments() throws HomeException
    {
        final Context ctx = getContext();

        createTestSub(ctx, BAN, SUB_ID, SubscriberTypeEnum.POSTPAID, SubscriberStateEnum.ACTIVE);

        request_.setStartDate(null);
        request_.setEndDate(null);
        request_.setPaymentNum(0);
        calendar_.setTime(today_);
        calendar_.add(Calendar.YEAR, 20);
        final Date endDate = calendar_.getTime();

        servicer_.process(ctx, request_);

        final Subscriber sub = (Subscriber) subHome_.storeList_.get(0);

        assertEquals("Wrong no of bundles", 1, sub.getBundles().size());

        final BundleFee fee = (BundleFee) sub.getBundles().values().iterator().next();

        assertEquals("Wrong bundle ID", BUNDLE_ID, fee.getId());
        assertNotNull("Start date in NULL", fee.getStartDate());
        assertEquals("Wrong start date", today_, fee.getStartDate());
        assertNotNull("End date in NULL", fee.getEndDate());
        assertEquals("Wrong end date", endDate, fee.getEndDate());
    }

    private BundleUploadRequestServicer servicer_;
    
    private long msisdnHistoryID = 1;

    private TestFakeSubHomeRememberSub subHome_;
    private AddBundleRequest request_;
    private com.redknee.app.crm.bundle.service.CRMBundleProfile profileService_;
    private com.redknee.app.crm.bundle.service.CRMBundleCategory categoryService_;
    private Calendar calendar_;
    private Date future_;
    private Date today_;
    private Date past_;
}

class TestFakeSubHomeRememberSub extends HomeProxy
{
    public LinkedList storeList_ = new LinkedList();

    public TestFakeSubHomeRememberSub(final Context ctx, final Home home)
    {
        super(ctx, home);
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) obj;
        storeList_.addLast(sub);
        return super.store(ctx, obj);
    }
}