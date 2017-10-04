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
package com.trilogy.app.crm.poller;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.util.snippet.junit.XDBJUnit;

import com.trilogy.app.crm.TestSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.bean.CallTypeTransientHome;
import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.IPCGDataHome;
import com.trilogy.app.crm.bean.IPCGDataXDBHome;
import com.trilogy.app.crm.bean.IPCGDataXInfo;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.home.IPCGBufferFlushingHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnChangeAppendHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 * @author victor.stratan@redknee.com
 */
public class TestIPCGBufferFlushingHome extends XDBJUnit
{
    public void setUp() throws Exception
    {
        System.setProperty("rk.home", "/opt/redknee/app/crm/current");
        setXdbInfo("jubjub", tableSpace_, port_, "appcrm_victor_crm72", "appcrm_victor_crm72");
        super.setUp();
        
        final Context ctx = getContext();

        // Home xdbHome = bindHome(ctx, IPCGData.class);
        // this test will work only with XDBHome 
        xdbhome_ = new IPCGDataXDBHome(ctx, "JUnit_" + IPCGDataXInfo.DEFAULT_TABLE_NAME);
        ctx.put(IPCGDataXDBHome.class, xdbhome_);

        storageTracer_ = new TraceTestHome(ctx, xdbhome_);

        tracer_ = new TracingTestIPCGBufferFlushingHome(storageTracer_, ctx);
        ctx.put(IPCGDataHome.class, tracer_);

        now_ = new Date();
        Calendar cal = CalendarSupportHelper.get().dateToCalendar(now_);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        oneHourInFuture_ = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -2);
        oneHourInPast_ = cal.getTime();
        todayStart_ = CalendarSupportHelper.get().clearTimeOfDay(cal).getTime();
        cal = CalendarSupportHelper.get().dateToCalendar(now_);
        yesterdayTime_ = CalendarSupportHelper.get().findDateDaysBefore(1, cal).getTime();
        yesterdayStart_ = CalendarSupportHelper.get().clearTimeOfDay(cal).getTime();

        initMsisdnMgmtHistory(ctx);
        initMsisdn(ctx);
        initAccount(ctx);
        initSubscriber(ctx);

        initCallDetail(ctx);
        initCallType(ctx);

        TestSupport.setupTransientSubscriptionType(ctx);
    }

    public void tearDown() throws Exception
    {
        xdbhome_.drop(getContext());
        super.tearDown();
    }


    /**
     * Initialize MsisdnMgmtHistory.
     */
    private void initMsisdnMgmtHistory(final Context ctx)
    {
        final Home home = new MsisdnMgmtHistoryTransientHome(ctx);
        ctx.put(MsisdnMgmtHistoryHome.class, home);
        msisdnHistoryID = 0;
    }


    /**
     * Initialize Msisdn.
     */
    private void initMsisdn(final Context ctx)
    {
        final Home home = new MsisdnChangeAppendHistoryHome(new MsisdnTransientHome(ctx));
        ctx.put(MsisdnHome.class, home);
    }


    /**
     * Initialize Subscriber.
     */
    private void initSubscriber(final Context ctx)
    {
        final Home home = new SubscriberTransientHome(ctx);
        ctx.put(SubscriberHome.class, home);
    }


    /**
     * Initialize Account.
     */
    private void initAccount(final Context ctx)
    {
        final Home home = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, home);
    }

    /**
     * Initialize CallDetail.
     */
    private void initCallDetail(final Context ctx)
    {
        final Home home = new HomeProxy(ctx, new CallDetailTransientHome(ctx))
        {
            private long nextId = 1;

            public Object create(final Context context, final Object object) throws HomeException
            {
                final CallDetail callDetail = (CallDetail) object;
                callDetail.setId(nextId++);
                return super.create(context, callDetail);
            }
        };
        ctx.put(CallDetailHome.class, home);
    }


    /**
     * Initialize CallType.
     */
    private void initCallType(final Context ctx)
    {
        final Home home = new CallTypeTransientHome(ctx);
        for (final Iterator it = CallTypeEnum.COLLECTION.iterator(); it.hasNext();)
        {
            final CallTypeEnum callType = (CallTypeEnum) it.next();
            CallType bean = new CallType();
            bean.setId(callType.getIndex());
            bean.setGLCode("GL Code");
            bean.setInvoiceDesc(callType.getDescription());
            bean.setSpid(SPID);

            try
            {
                bean = (CallType) home.create(ctx, bean);
            }
            catch (final HomeException exception)
            {
                fail("Exception caught when initializing CallType");
            }
        }
        ctx.put(CallTypeHome.class, home);
    }


    /**
     * Generates a Subscriber with provided MSISDN for use.
     *
     * @return A valid MSIDN.
     * @throws HomeException
     *             Thrown if there are problems creating the MSISDN or subscriber.
     */
    private String generateSubscriber(final Context ctx, final String msisdn) throws HomeException
    {
        final Msisdn msisdnObj = new Msisdn();
        msisdnObj.setMsisdn(msisdn);
        final Date date = CalendarSupportHelper.get(ctx).findDateYearsAfter(-1, new Date());

        msisdnObj.setLastModified(date);
        msisdnObj.setStartTimestamp(date);
        msisdnObj.setState(MsisdnStateEnum.IN_USE);
        msisdnObj.setBAN(msisdn);
        msisdnObj.setSubscriberType(SubscriberTypeEnum.PREPAID);
        msisdnObj.setSpid(SPID);
        ((Home) ctx.get(MsisdnHome.class)).create(ctx, msisdnObj);

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setIdentifier(msisdnHistoryID++);
        msisdnHistory.setTerminalId(msisdn);
        msisdnHistory.setBAN(msisdn);
        msisdnHistory.setSubscriberId(msisdn + "-1");
        msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(ctx).getId());
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(date);
        ((Home) ctx.get(MsisdnMgmtHistoryHome.class)).create(ctx, msisdnHistory);

        final Subscriber subscriber = new Subscriber();
        subscriber.setId(msisdn + "-1");
        subscriber.setMSISDN(msisdn);
        subscriber.setBAN(msisdn);
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setStartDate(date);
        subscriber.setSpid(SPID);
        ((Home) ctx.get(SubscriberHome.class)).create(ctx, subscriber);

        final Account account = new Account();
        account.setBAN(msisdn);
        account.setSystemType(SubscriberTypeEnum.PREPAID);
        account.setState(AccountStateEnum.ACTIVE);
        account.setSpid(SPID);
        ((Home) ctx.get(AccountHome.class)).create(ctx, account);
        return msisdn;
    }

    public void testEmptyPreDrop() throws HomeException
    {
        // this empty test will cause a tearDown before next test clearing potential leftover data
        assertNotNull(now_);
    }

    /**
     * @throws HomeException thrown by tested methods
     */
    public void testCreateOneIPCGDataRecord() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        final IPCGData record = new IPCGData();
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not created ", 1, resultCol.size());

        assertEquals("create() should be called ", 1, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 1, storageTracer_.createCallCounter_);
        assertEquals("no flushBuffer() ", 0, tracer_.flushBufferCallCounter_);
        assertEquals("no createCDR() ", 0, tracer_.createCDRCallCounter_);
        assertEquals("no removeRecords() ", 0, tracer_.removeRecordsCallCounter_);
    }

    /**
     * @throws HomeException thrown by tested methods
     */
    public void testNoFlushBufferForRecordsInOneDay() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        IPCGData record;
        record = new IPCGData();
        record.setTranDate(now_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(oneHourInFuture_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_TWO);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(oneHourInPast_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_THREE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not created ", 3, resultCol.size());

        assertEquals("create() should be called ", 3, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 3, storageTracer_.createCallCounter_);
        assertEquals("no flushBuffer() ", 0, tracer_.flushBufferCallCounter_);
        assertEquals("no createCDR() ", 0, tracer_.createCDRCallCounter_);
        assertEquals("no removeRecords() ", 0, tracer_.removeRecordsCallCounter_);
    }

    /**
     * First record is dated yesterday.
     * Second record is dated today.
     * Second record should trigger flushing.
     *
     * @throws HomeException thrown by tested methods
     */
    public void testFlushBufferForRecordNewDay() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        IPCGData record;
        record = new IPCGData();
        record.setTranDate(yesterdayTime_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(now_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_TWO);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not flushed or not created ", 1, resultCol.size());

        assertEquals("create() should be called ", 2, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 2, storageTracer_.createCallCounter_);
        assertEquals("flushBuffer() once only", 1, tracer_.flushBufferCallCounter_);
        assertEquals("createCDR() once only", 1, tracer_.createCDRCallCounter_);
        assertEquals("removeRecords() once only", 1, tracer_.removeRecordsCallCounter_);
    }

    /**
     * First record is dated yesterday.
     * Second record is dated today.
     * Second record should trigger flushing.
     *
     * @throws HomeException thrown by tested methods
     */
    public void testFlushBufferForPreexistentRecordNewDay() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        IPCGData record;
        record = new IPCGData();
        record.setTranDate(yesterdayTime_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        xdbhome_.create(ctx, record);

        record = new IPCGData();
        record.setTranDate(now_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_TWO);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not flushed or not created ", 1, resultCol.size());

        assertEquals("create() should be called ", 1, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 1, storageTracer_.createCallCounter_);
        assertEquals("flushBuffer() once only", 1, tracer_.flushBufferCallCounter_);
        assertEquals("createCDR() once only", 1, tracer_.createCDRCallCounter_);
        assertEquals("removeRecords() once only", 1, tracer_.removeRecordsCallCounter_);
    }

    /**
     * First record is dated yesterday.
     * Second record is dated today.
     * Second record should trigger flushing.
     * Third record is dated one hour later today, should not trigger flushing.
     *
     * @throws HomeException thrown by tested methods
     */
    public void testNoSecondFlushBufferForRecordOnSameNewDay() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        IPCGData record;
        record = new IPCGData();
        record.setTranDate(yesterdayTime_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(now_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_TWO);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(oneHourInFuture_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_THREE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not flushed or not created ", 2, resultCol.size());

        assertEquals("create() should be called ", 3, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 3, storageTracer_.createCallCounter_);
        assertEquals("flushBuffer() once only", 1, tracer_.flushBufferCallCounter_);
        assertEquals("createCDR() once only", 1, tracer_.createCDRCallCounter_);
        assertEquals("removeRecords() once only", 1, tracer_.removeRecordsCallCounter_);
    }

    /**
     * First record is dated today.
     * Second record is dated yesterday.
     * Second record should go directly to CallDetails.
     *
     * @throws HomeException thrown by tested methods
     */
    public void testStaightToCallDetailsForLateRecord() throws HomeException
    {
        final Context ctx = getContext();

        generateSubscriber(ctx, MSIDN_ONE);

        IPCGData record;
        record = new IPCGData();
        record.setTranDate(now_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_ONE);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        record = new IPCGData();
        record.setTranDate(yesterdayTime_);
        record.setChargedMSISDN(MSIDN_ONE);
        record.setCharge(CHARGE_TWO);
        record.setCallType(CallTypeEnum.WEB);
        HomeSupportHelper.get(ctx).createBean(ctx, record);

        final Collection<IPCGData> resultCol = HomeSupportHelper.get(ctx).getBeans(ctx, IPCGData.class);
        assertEquals("Record not flushed or not created ", 1, resultCol.size());

        assertEquals("create() should be called ", 2, tracer_.createCallCounter_);
        assertEquals("storage create() should be called ", 1, storageTracer_.createCallCounter_);
        assertEquals("no flushBuffer() ", 0, tracer_.flushBufferCallCounter_);
        assertEquals("createCDR() directly", 1, tracer_.createCDRCallCounter_);
        assertEquals("no removeRecords() ", 0, tracer_.removeRecordsCallCounter_);
    }

    class TracingTestIPCGBufferFlushingHome extends IPCGBufferFlushingHome 
    {
        public int createCallCounter_;
        public int flushBufferCallCounter_;
        public int createCDRCallCounter_;
        public int removeRecordsCallCounter_;
        
        public TracingTestIPCGBufferFlushingHome(Home delegate, Context ctx)
        {
            super(delegate, ctx);
        }

        public Object create(Context ctx, Object obj) throws HomeException
        {
            createCallCounter_++;
            return super.create(ctx, obj);
        }

        public void flushBuffer(Context ctx, Date cutoffDate) throws HomeException
        {
            flushBufferCallCounter_++;
            super.flushBuffer(ctx, cutoffDate);
        }

        protected void createCDRWithAggregationLogging(Context ctx, IPCGData data, int numEr) throws HomeException
        {
            createCDRCallCounter_++;
            super.createCDRWithAggregationLogging(ctx, data, numEr);
        }

        protected void removeRecords(Context ctx, Home home, Set<Long> bufferIds) throws HomeException
        {
            removeRecordsCallCounter_++;
            super.removeRecords(ctx, home, bufferIds);
        }
    }
    
    class TraceTestHome extends HomeProxy
    {
        public int createCallCounter_;

        public TraceTestHome(Context ctx, Home delegate)
        {
            super(ctx, delegate);
        }

        public Object create(Context ctx, Object obj) throws HomeException
        {
            createCallCounter_++;
            return super.create(ctx, obj);
        }
        
    }

    Date now_;
    Date oneHourInFuture_;
    Date oneHourInPast_;
    Date todayStart_;
    Date yesterdayTime_;
    Date yesterdayStart_;

    private int msisdnHistoryID;

    private static final int SPID = 1;

    private static final String MSIDN_ONE = "9056252911";
    private static final String MSIDN_TWO = "9056252912";
    private static final String MSIDN_THREE = "9056252913";

    private static final int CHARGE_ONE = 12345;
    private static final int CHARGE_TWO = 23456;
    private static final int CHARGE_THREE = 34567;

    TracingTestIPCGBufferFlushingHome tracer_;
    TraceTestHome storageTracer_;
    IPCGDataXDBHome xdbhome_;
}
