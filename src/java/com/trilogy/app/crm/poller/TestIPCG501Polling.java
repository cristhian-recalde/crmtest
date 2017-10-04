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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.xdb.Max;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.CallType;
import com.trilogy.app.crm.bean.CallTypeHome;
import com.trilogy.app.crm.bean.CallTypeTransientHome;
import com.trilogy.app.crm.bean.ErPollerConfig;
import com.trilogy.app.crm.bean.IPCGData;
import com.trilogy.app.crm.bean.IPCGDataHome;
import com.trilogy.app.crm.bean.IPCGDataTransientHome;
import com.trilogy.app.crm.bean.IPCGDataXDBHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.PollerProcessorPackage;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.bean.calldetail.CallTypeEnum;
import com.trilogy.app.crm.bean.calldetail.RateUnitEnum;
import com.trilogy.app.crm.config.IPCGPollerConfig;
import com.trilogy.app.crm.home.IPCGBufferFlushingHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnChangeAppendHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.poller.event.IPCGWProcessor;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.DefaultER;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * @author cindy.wong@redknee.com
 * @since 2008-07-14
 */
public class TestIPCG501Polling extends ContextAwareTestCase
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

        final TestSuite suite = new TestSuite(TestIPCG501Polling.class);

        return suite;
    }


    /**
     * Create a new instance of <code>TestIPCG501Polling</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestIPCG501Polling(final String name)
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

        (new com.redknee.app.crm.core.agent.BeanFactoryInstall()).execute(getContext());

        final Calendar cal = Calendar.getInstance();
        today_ = CalendarSupportHelper.get(getContext()).clearTimeOfDay(cal).getTime();
        msisdnHistoryID = 1;

        LogSupport.setSeverityThreshold(getContext(), SeverityEnum.DEBUG);
        if (ContextLocator.locate() == null)
        {
            ContextLocator.set(null, getContext());
        }
        ContextLocator.setThreadContext(getContext());
        initIPCGData();
        initCallDetail();
        initPollerProcessorPackage();
        initIPCGPollerConfig();
        initErPollerConfig();
        initParser();
        initMsisdn();
        initSubscriber();
        initMsisdnMgmtHistory();
        initAccount();
        initCallType();
    }


    /**
     * Initialize CallType.
     */
    private void initCallType()
    {
        final Home home = new CallTypeTransientHome(getContext());
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
                bean = (CallType) home.create(getContext(), bean);
            }
            catch (final HomeException exception)
            {
                fail("Exception caught when initializing CallType");
            }
        }
        getContext().put(CallTypeHome.class, home);
    }


    /**
     * Initialize MsisdnMgmtHistory.
     */
    private void initMsisdnMgmtHistory()
    {
        final Home home = new MsisdnMgmtHistoryTransientHome(getContext());
        getContext().put(MsisdnMgmtHistoryHome.class, home);
    }


    /**
     * Initialize Msisdn.
     */
    private void initMsisdn()
    {
        final Home home = new MsisdnChangeAppendHistoryHome(new MsisdnTransientHome(getContext()));
        getContext().put(MsisdnHome.class, home);
    }


    /**
     * Initialize Subscriber.
     */
    private void initSubscriber()
    {
        final Home home = new SubscriberTransientHome(getContext());
        getContext().put(SubscriberHome.class, home);
    }


    /**
     * Initialize Account.
     */
    private void initAccount()
    {
        final Home home = new AccountTransientHome(getContext());
        getContext().put(AccountHome.class, home);
    }


    /**
     * Initializes the parser.
     */
    private void initParser()
    {
        IPCGWUnifiedBillingParser.instance().setProcessor(new IPCGWProcessor()
        {

            /**
             * {@inheritDoc}
             */
            @Override
            public int isChargedMsisdnOkay(final Context ctx, final String chargedMsisdn, final Date transDate)
            {
                return 0;
            }
        });
    }


    /**
     * Initialize IPCGData.
     */
    private void initIPCGData()
    {
        final Home home = new IPCGDataTransientHome(getContext());
        getContext().put(IPCGDataHome.class, home);
        final Home xdbHome = new HomeProxy(home)
        {

            public Object cmd(final Context context, final Object cmd) throws UnsupportedOperationException,
                HomeException
            {
                if (cmd instanceof Max)
                {
                    final Max max = (Max) cmd;
                    Object maxValue = null;
                    final PropertyInfo property = max.getArg();
                    for (final Object object : getDelegate(context).selectAll(context))
                    {
                        final Object value = property.get(object);
                        if (maxValue == null || value != null && ((Comparable) value).compareTo(maxValue) > 0)
                        {
                            maxValue = value;
                        }
                    }
                    if (maxValue instanceof Date)
                    {
                        return new BigDecimal(((Date) maxValue).getTime());
                    }
                    return maxValue;
                }
                return cmd;
            }
        };
        getContext().put(IPCGDataXDBHome.class, xdbHome);
    }


    /**
     * Initialize CallDetail.
     */
    private void initCallDetail()
    {
        final Home home = new HomeProxy(new CallDetailTransientHome(getContext()))
        {

            private long nextId = 1;


            public Object create(final Context context, final Object object) throws HomeException
            {
                final CallDetail callDetail = (CallDetail) object;
                callDetail.setId(nextId++);
                return super.create(context, callDetail);
            }
        };
        getContext().put(CallDetailHome.class, home);
    }


    /**
     * Initialize PollerProcessorPackage configuration.
     */
    private void initPollerProcessorPackage()
    {
        final PollerProcessorPackage config = new PollerProcessorPackage();
        getContext().put(PollerProcessorPackage.class, config);
    }

    /**
     * Initialize IPCG poller configuration.
     */
    private void initIPCGPollerConfig()
    {
        final IPCGPollerConfig config = new IPCGPollerConfig();
        config.setEnableFilter(false);
        config.setErrorLog(ERROR_LOG_FILE_NAME);
        config.setPosMsisdn(POS_MSISDN);
        config.setPosGeneralCharge(POS_GENERAL_CHARGE);
        config.setPosSCPID(POS_SCP_ID);
        config.setPosTransDate(POS_TRANS_DATE);
        config.setPosTransTime(POS_TRANS_TIME);
        config.setPosSdrUsage(POS_SDR_USAGE);
        config.setPosHttp(POS_PROTOCOL);
        config.setPosCharge(POS_PROTOCOL_TOTAL_CHARGE);
        config.setPosBundleCharge(POS_BUNDLE_CHARGE);
        config.setPosDurationCharge(POS_PROTOCOL_DURATION_CHARGE);
        config.setPosDurationRated(POS_PROTOCOL_DURATION_RATED);
        config.setPosEventCharge(POS_PROTOCOL_EVENT_CHARGE);
        config.setPosEventCounter(POS_PROTOCOL_EVENT_RATED);
        config.setPosUrlInfo(POS_PROTOCOL_URL_INFO);
        config.setPosVolDown(POS_PROTOCOL_VOLUME_RATED);
        config.setPosVolUp(POS_PROTOCOL_VOLUME_UP);
        config.setPosVolumeCharge(POS_PROTOCOL_VOLUME_CHARGE);
        getContext().put(IPCGPollerConfig.class, config);
    }


    /**
     * Initialize ER poller configuration.
     */
    private void initErPollerConfig()
    {
        final ErPollerConfig config = new ErPollerConfig();
        getContext().put(ErPollerConfig.class, config);
    }


    /**
     * Test ER with SDR usage.
     *
     * @throws FilterOutException
     *             Thrown by ER processor.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws AgentException
     *             Thrown if there are problems processing the ER.
     * @throws HomeException
     *             Thrown if there are problems creating the required subscriber or
     *             MSISDN.
     */
    public void testIPCGDataGenerationWithSDR() throws FilterOutException, AgentException, ParseException,
        HomeException
    {
        final int numProtocols = (int) Math.round(Math.random() * 10) + 1;

        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> usageMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();

        final String[] protocols = generateProtocols(numProtocols, false, false, false, false, chargeMap, usageMap);
        for (final Map.Entry<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeEntry : chargeMap.entrySet())
        {
            final CallTypeEnum callType = chargeEntry.getKey();
            final Map<RateUnitEnum, Integer> rateUnitMap = chargeEntry.getValue();
            for (final Map.Entry<RateUnitEnum, Integer> rateUnitEntry : rateUnitMap.entrySet())
            {
                final RateUnitEnum rateUnit = rateUnitEntry.getKey();
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "Call type = " + callType + ", unit type = " + rateUnit
                        + ", charge = " + rateUnitEntry.getValue() + ", usage = "
                        + usageMap.get(callType).get(rateUnit));
                }
            }
        }

        final Date erDate = new Date();
        final String msisdn = generateMsisdn();
        final int scpId = (int) Math.round(Math.random() * 100);
        final int sdrUsage = (int) Math.round(Math.random() * 500);
        final int bundleCharge = (int) Math.round(Math.random() * 5000);
        final int generalCharge = (int) Math.round(Math.random() * 5000);
        final Calendar transactionDate = Calendar.getInstance();
        transactionDate.set(Calendar.MILLISECOND, 0);
        final String er = generateEr(erDate, scpId, msisdn, transactionDate.getTime(), generalCharge, bundleCharge,
            sdrUsage, protocols);
        LogSupport.info(getContext(), this, "ER generated: " + er);

        final List<String> params = parseEr(er);

        verifyErFields(msisdn, scpId, sdrUsage, generalCharge, bundleCharge, protocols, params);

        final Collection<IPCGData> collection = IPCGWUnifiedBillingParser.instance().processEr501(getContext(), params);

        assertNotNull(collection);
        for (final IPCGData data : collection)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "IPCGData generated: " + data.toString());
            }
        }

        assertEquals("Only 1 IPCGData should be generated", 1, collection.size());
        final IPCGData data = collection.iterator().next();
        assertEquals("Call type does not match", CallTypeEnum.SDR, data.getCallType());
        assertEquals("General charge does not match", generalCharge, data.getCharge());
        assertEquals("SDR usage does not match", sdrUsage, data.getUsage());
        assertEquals("Rating unit type does not match", RateUnitEnum.SEC, data.getUnitType());
        assertEquals("MSISDN does not match", msisdn, data.getChargedMSISDN());
        assertEquals("Transaction date does not match", transactionDate.getTime(), data.getTranDate());
    }


    /**
     * Test ER with no SDR usage.
     *
     * @throws FilterOutException
     *             Thrown by ER processor.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws AgentException
     *             Thrown if there are problems processing the ER.
     * @throws HomeException
     *             Thrown if there are problems creating the required subscriber or
     *             MSISDN.
     */
    public void testIPCGDataGenerationWithoutSDR() throws FilterOutException, AgentException, ParseException,
        HomeException
    {
        final int numProtocols = (int) Math.round(Math.random() * 10) + 1;

        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> usageMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();

        final String[] protocols = generateProtocols(numProtocols, true, true, false, false, chargeMap, usageMap);

        int itemCount = 0;
        for (final Map.Entry<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeEntry : chargeMap.entrySet())
        {
            final CallTypeEnum callType = chargeEntry.getKey();
            final Map<RateUnitEnum, Integer> rateUnitMap = chargeEntry.getValue();
            for (final Map.Entry<RateUnitEnum, Integer> rateUnitEntry : rateUnitMap.entrySet())
            {
                final RateUnitEnum rateUnit = rateUnitEntry.getKey();
                itemCount++;
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "Call type = " + callType + ", unit type = " + rateUnit
                        + ", charge = " + rateUnitEntry.getValue() + ", usage = "
                        + usageMap.get(callType).get(rateUnit));
                }
            }
        }

        final Date erDate = new Date();
        final String msisdn = generateMsisdn();
        final int scpId = (int) Math.round(Math.random() * 100);
        final int sdrUsage = 0;
        final int bundleCharge = (int) Math.round(Math.random() * 5000);
        final int generalCharge = 0;
        final Calendar transactionDate = Calendar.getInstance();
        transactionDate.set(Calendar.MILLISECOND, 0);
        final String er = generateEr(erDate, scpId, msisdn, transactionDate.getTime(), generalCharge, bundleCharge,
            sdrUsage, protocols);
        LogSupport.info(getContext(), this, "ER generated: " + er);

        final List<String> params = parseEr(er);

        verifyErFields(msisdn, scpId, sdrUsage, generalCharge, bundleCharge, protocols, params);

        final Collection<IPCGData> collection = IPCGWUnifiedBillingParser.instance().processEr501(getContext(), params);

        assertNotNull(collection);
        for (final IPCGData data : collection)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "IPCGData generated: " + data.toString());
            }
        }

        assertEquals("Number of IPCGData generated don't match", itemCount, collection.size());
        for (final IPCGData data : collection)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "IPCGData generated: " + data.toString());
            }
        }

        for (final IPCGData data : collection)
        {
            assertEquals("MSISDN does not match", msisdn, data.getChargedMSISDN());
            assertEquals("Transaction date does not match", transactionDate.getTime(), data.getTranDate());
            assertNotNull("Call Type does not match", chargeMap.get(data.getCallType()));
            assertNotNull("Rating unit type does not match for call type " + data.getCallType(), chargeMap.get(
                data.getCallType()).get(data.getUnitType()));
            assertEquals("Charge does not match for call type " + data.getCallType() + " and unit type "
                + data.getUnitType(), chargeMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                .getCharge());
            assertEquals("Usage does not match for call type " + data.getCallType() + " and unit type "
                + data.getUnitType(), usageMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                .getUsage());
        }
    }


    /**
     * Test ER with multiple types of charges in a single protocol section.
     *
     * @throws FilterOutException
     *             Thrown by ER processor.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws AgentException
     *             Thrown if there are problems processing the ER.
     * @throws HomeException
     *             Thrown if there are problems creating the required subscriber or
     *             MSISDN.
     */
    public void testIPCGDataGenerationWithMultipleTypesOfCharges() throws FilterOutException, AgentException,
        ParseException, HomeException
    {
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> usageMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();

        final int numProtocols = (int) Math.round(Math.random() * 10) + 1;
        final String[] protocols = generateProtocols(numProtocols, true, true, true, true, chargeMap, usageMap);

        int itemCount = 0;
        for (final Map.Entry<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeEntry : chargeMap.entrySet())
        {
            final CallTypeEnum callType = chargeEntry.getKey();
            final Map<RateUnitEnum, Integer> rateUnitMap = chargeEntry.getValue();
            for (final Map.Entry<RateUnitEnum, Integer> rateUnitEntry : rateUnitMap.entrySet())
            {
                final RateUnitEnum rateUnit = rateUnitEntry.getKey();
                itemCount++;
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "Call type = " + callType + ", unit type = " + rateUnit
                        + ", charge = " + rateUnitEntry.getValue() + ", usage = "
                        + usageMap.get(callType).get(rateUnit));
                }
            }
        }

        final Date erDate = new Date();
        final String msisdn = generateMsisdn();
        final int scpId = (int) Math.round(Math.random() * 100);
        final int sdrUsage = 0;
        final int bundleCharge = (int) Math.round(Math.random() * 5000);
        final int generalCharge = 0;
        final Calendar transactionDate = Calendar.getInstance();
        transactionDate.set(Calendar.MILLISECOND, 0);
        final String er = generateEr(erDate, scpId, msisdn, transactionDate.getTime(), generalCharge, bundleCharge,
            sdrUsage, protocols);
        LogSupport.info(getContext(), this, "ER generated: " + er);

        final List<String> params = parseEr(er);

        verifyErFields(msisdn, scpId, sdrUsage, generalCharge, bundleCharge, protocols, params);

        final Collection<IPCGData> collection = IPCGWUnifiedBillingParser.instance().processEr501(getContext(), params);

        assertNotNull(collection);
        for (final IPCGData data : collection)
        {
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "IPCGData generated: " + data.toString());
            }
        }

        assertEquals("Number of IPCGData generated don't match", itemCount, collection.size());
        for (final IPCGData data : collection)
        {
            assertEquals("MSISDN does not match", msisdn, data.getChargedMSISDN());
            assertEquals("Transaction date does not match", transactionDate.getTime(), data.getTranDate());
            assertNotNull("Call Type does not match", chargeMap.get(data.getCallType()));
            assertNotNull("Rating unit type does not match for call type " + data.getCallType(), chargeMap.get(
                data.getCallType()).get(data.getUnitType()));
            assertEquals("Charge does not match for call type " + data.getCallType() + " and unit type "
                + data.getUnitType(), chargeMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                .getCharge());
            assertEquals("Usage does not match for call type " + data.getCallType() + " and unit type "
                + data.getUnitType(), usageMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                .getUsage());
        }
    }


    /**
     * Test IPCGData flushing.
     *
     * @throws FilterOutException
     *             Thrown by ER processor.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws AgentException
     *             Thrown if there are problems processing the ER.
     * @throws HomeException
     *             Thrown if there are problems storing the IPCG data.
     */
    public void testIPCGDataFlushing() throws AgentException, ParseException, FilterOutException, HomeException
    {
        final Map<String, Map<Date, List<IPCGData>>> dataMap = new HashMap<String, Map<Date, List<IPCGData>>>();
        final Home ipcgDataHome = (Home) getContext().get(IPCGDataHome.class);
        final IPCGBufferFlushingHome bufferHome = new IPCGBufferFlushingHome(ipcgDataHome, getContext());

        final int numMsisdn = (int) Math.round(Math.random() * 10) + 1;
        for (int i = 0; i < numMsisdn; i++)
        {
            final String msisdn = generateMsisdn();
            dataMap.put(msisdn, new HashMap<Date, List<IPCGData>>());
        }
        final List<String> msisdns = new ArrayList<String>(dataMap.keySet());

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -((int) Math.round(Math.random() * 3) + 1));
        final Date now = new Date();
        long id = 1;
        while (calendar.getTime().before(now))
        {
            // pick a random MSISDN
            final String msisdn = msisdns.get((int) Math.floor(Math.random() * msisdns.size()));
            final List<IPCGData> dataCollection = generateIPCGDataCollection(1, calendar.getTime(), msisdn);
            for (final IPCGData data : dataCollection)
            {
                data.setIPCGDataID(id++);
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "IPCGData to be stored: " + data.getIPCGDataID() + ": "
                        + data.getChargedMSISDN() + " " + new SimpleDateFormat("yyyy-MM-dd").format(data.getTranDate())
                        + " " + data.getCallType() + " " + data.getUnitType() + " " + data.getCharge() + " "
                        + data.getUsage());
                }
            }

            assertNotNull("IPCGData collection must not be null", dataCollection);

            for (final IPCGData data : dataCollection)
            {
                bufferHome.create(getContext(), data);
            }

            final Date dateOnly = CalendarSupportHelper.get(getContext()).getDateWithNoTimeOfDay(calendar.getTime());

            if (dataMap.get(msisdn).get(dateOnly) != null)
            {
                dataCollection.addAll(dataMap.get(msisdn).get(dateOnly));
            }
            dataMap.get(msisdn).put(dateOnly, dataCollection);
            calendar.add(Calendar.HOUR_OF_DAY, (int) Math.round(Math.random() * 6) + 1);
        }

        bufferHome.flushBuffer(getContext(), now);

        int itemCount = 0;
        for (final Map.Entry<String, Map<Date, List<IPCGData>>> entry : dataMap.entrySet())
        {
            final String msisdn = entry.getKey();
            final Map<Date, List<IPCGData>> innerMap = entry.getValue();
            for (final Map.Entry<Date, List<IPCGData>> innerEntry : innerMap.entrySet())
            {
                final Date date = innerEntry.getKey();
                final List<IPCGData> collection = innerEntry.getValue();
                Collections.sort(collection, new IPCGWUnifiedBillingParser.CallTypeUsageTypeComparator());
                final List<IPCGData> results = IPCGWUnifiedBillingParser.instance().consolidateResults(collection);
                for (final IPCGData data : results)
                {
                    itemCount++;
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        LogSupport.debug(getContext(), this, "Expected call detail: " + data.getIPCGDataID() + ": "
                            + data.getChargedMSISDN() + " "
                            + new SimpleDateFormat("yyyy-MM-dd").format(data.getTranDate()) + " " + data.getCallType()
                            + " " + data.getUnitType() + " " + data.getCharge() + " " + data.getUsage());
                    }
                }
            }
        }

        final Home callDetailHome = (Home) getContext().get(CallDetailHome.class);
        for (final Object object : callDetailHome.selectAll(getContext()))
        {
            final CallDetail callDetail = (CallDetail) object;
            if (LogSupport.isDebugEnabled(getContext()))
            {
                LogSupport.debug(getContext(), this, "Created call detail: "
                    + callDetail.getId()
                    + ": "
                    + callDetail.getChargedMSISDN()
                    + " "
                    + new SimpleDateFormat("yyyy-MM-dd").format(callDetail.getTranDate())
                    + " "
                    + callDetail.getCallType()
                    + " "
                    + callDetail.getVariableRateUnit()
                    + " "
                    + callDetail.getCharge()
                    + " "
                    + (callDetail.getVariableRateUnit() == RateUnitEnum.SEC ? callDetail.getDuration() : callDetail
                        .getDataUsage()));
            }
        }
        assertEquals("Number of call details don't match", itemCount, callDetailHome.selectAll(getContext()).size());
    }


    /**
     * Generates a collection of IPCGData based on randomly generated ERs for a single
     * MSISDN.
     *
     * @param numEr
     *            Number of ERs to be generated.
     * @param erDate
     *            Date of the transaction.
     * @param msisdn
     *            MSISDN to be charged.
     * @return A collection of IPCGData generated.
     * @throws FilterOutException
     *             Thrown by ER processor.
     * @throws ParseException
     *             Thrown if there are problems parsing the ER.
     * @throws AgentException
     *             Thrown if there are problems processing the ER.
     */
    private List<IPCGData> generateIPCGDataCollection(final int numEr, final Date erDate, final String msisdn)
        throws AgentException, ParseException, FilterOutException
    {
        final List<IPCGData> result = new ArrayList<IPCGData>();
        if (LogSupport.isDebugEnabled(getContext()))
        {
            LogSupport.debug(getContext(), this, "MSISDN = " + msisdn + ", date = " + erDate);
        }
        for (int i = 0; i < numEr; i++)
        {
            final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();
            final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> usageMap = new HashMap<CallTypeEnum, Map<RateUnitEnum, Integer>>();

            int itemCount = 0;
            final Calendar transactionDate = Calendar.getInstance();
            transactionDate.setTime(erDate);
            transactionDate.set(Calendar.MILLISECOND, 0);
            final int scpId = (int) Math.round(Math.random() * 100);

            final int sdrUsage;
            final int bundleCharge = (int) Math.round(Math.random() * 5000);
            final int generalCharge;
            final boolean sdr = Math.random() > 0.7;
            if (sdr)
            {
                sdrUsage = (int) Math.round(Math.random() * 1000);
                generalCharge = (int) Math.round(Math.random() * 5000);

                if (chargeMap.get(CallTypeEnum.SDR) == null)
                {
                    chargeMap.put(CallTypeEnum.SDR, new HashMap<RateUnitEnum, Integer>());
                }
                int totalCharge = generalCharge;
                if (chargeMap.get(CallTypeEnum.SDR).get(RateUnitEnum.SEC) != null)
                {
                    totalCharge += chargeMap.get(CallTypeEnum.SDR).get(RateUnitEnum.SEC).intValue();
                }
                chargeMap.get(CallTypeEnum.SDR).put(RateUnitEnum.SEC, Integer.valueOf(totalCharge));

                if (usageMap.get(CallTypeEnum.SDR) == null)
                {
                    usageMap.put(CallTypeEnum.SDR, new HashMap<RateUnitEnum, Integer>());
                }
                int totalUsage = sdrUsage;
                if (usageMap.get(CallTypeEnum.SDR).get(RateUnitEnum.SEC) != null)
                {
                    totalUsage += usageMap.get(CallTypeEnum.SDR).get(RateUnitEnum.SEC).intValue();
                }
                usageMap.get(CallTypeEnum.SDR).put(RateUnitEnum.SEC, Integer.valueOf(totalUsage));
            }
            else
            {
                sdrUsage = 0;
                generalCharge = 0;
            }

            final int numProtocols = (int) Math.round(Math.random() * 10) + 1;
            final String[] protocols = generateProtocols(numProtocols, !sdr, !sdr, false, false, chargeMap, usageMap);
            for (final Map.Entry<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeEntry : chargeMap.entrySet())
            {
                final CallTypeEnum callType = chargeEntry.getKey();
                final Map<RateUnitEnum, Integer> rateUnitMap = chargeEntry.getValue();
                for (final Map.Entry<RateUnitEnum, Integer> rateUnitEntry : rateUnitMap.entrySet())
                {
                    final RateUnitEnum rateUnit = rateUnitEntry.getKey();
                    itemCount++;
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        LogSupport.debug(getContext(), this, "Call type = " + callType + ", unit type = " + rateUnit
                            + ", charge = " + rateUnitEntry.getValue() + ", usage = "
                            + usageMap.get(callType).get(rateUnit));
                    }
                }
            }

            final String er = generateEr(erDate, scpId, msisdn, transactionDate.getTime(), generalCharge, bundleCharge,
                sdrUsage, protocols);
            LogSupport.info(getContext(), this, "ER generated: " + er);

            final List<String> params = parseEr(er);

            verifyErFields(msisdn, scpId, sdrUsage, generalCharge, bundleCharge, protocols, params);

            final Collection<IPCGData> collection = IPCGWUnifiedBillingParser.instance().processEr501(getContext(),
                params);

            assertNotNull(collection);
            for (final IPCGData data : collection)
            {
                if (LogSupport.isDebugEnabled(getContext()))
                {
                    LogSupport.debug(getContext(), this, "IPCGData generated: " + data.getIPCGDataID() + ": "
                        + data.getChargedMSISDN() + " " + new SimpleDateFormat("yyyy-MM-dd").format(data.getTranDate())
                        + " " + data.getCallType() + " " + data.getUnitType() + " " + data.getCharge() + " "
                        + data.getUsage());
                }
            }

            assertEquals("Number of IPCGData generated don't match", itemCount, collection.size());
            for (final IPCGData data : collection)
            {
                assertEquals("MSISDN does not match", msisdn, data.getChargedMSISDN());
                assertEquals("Transaction date does not match", transactionDate.getTime(), data.getTranDate());
                assertNotNull("Call Type does not match", chargeMap.get(data.getCallType()));
                assertNotNull("Rating unit type does not match for call type " + data.getCallType(), chargeMap.get(
                    data.getCallType()).get(data.getUnitType()));
                assertEquals("Charge does not match for call type " + data.getCallType() + " and unit type "
                    + data.getUnitType(), chargeMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                    .getCharge());
                assertEquals("Usage does not match for call type " + data.getCallType() + " and unit type "
                    + data.getUnitType(), usageMap.get(data.getCallType()).get(data.getUnitType()).intValue(), data
                    .getUsage());
            }
            result.addAll(collection);
        }
        return result;
    }


    /**
     * Generate all of the protocol sections.
     *
     * @param numProtocols
     *            The number of protocol sections to generate.
     * @param generateCharge
     *            Whether charges should be generated.
     * @param generateUsage
     *            Whether usages should be generated.
     * @param generateMultipleCharges
     *            Whether multiple types of charges should be generated.
     * @param generateMultipleUsage
     *            Whether multiple types of usage should be generated.
     * @param chargeMap
     *            This map stores all charges generated.
     * @param usageMap
     *            This map stores all usages generated.
     * @return The array of protocol sections generated.
     */
    private String[] generateProtocols(final int numProtocols, final boolean generateCharge,
        final boolean generateUsage, final boolean generateMultipleCharges, final boolean generateMultipleUsage,
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> chargeMap,
        final Map<CallTypeEnum, Map<RateUnitEnum, Integer>> usageMap)
    {
        final String[] protocols = new String[numProtocols];
        final List<String> protocolTypes = new ArrayList<String>(IPCGWUnifiedBillingParser.PROTOCOL_CALL_TYPE_MAP
            .keySet());
        protocolTypes.add("HTTP");
        protocolTypes.add("FTP");

        final List<RateUnitEnum> rateUnits = new ArrayList<RateUnitEnum>();
        rateUnits.add(RateUnitEnum.EVENT);
        rateUnits.add(RateUnitEnum.KBYTES);
        rateUnits.add(RateUnitEnum.SEC);

        for (int i = 0; i < protocols.length; i++)
        {
            long volumeCharged = 0;
            long volumeRated = 0;
            long eventCharged = 0;
            long eventRated = 0;
            long durationCharged = 0;
            long durationRated = 0;
            final String protocol = protocolTypes.get((int) Math.floor(Math.random() * protocolTypes.size()));
            final CallTypeEnum callType = IPCGWUnifiedBillingParser.PROTOCOL_CALL_TYPE_MAP.get(protocol);

            int numRateUnits = 1;
            if (generateMultipleUsage)
            {
                numRateUnits = (int) Math.ceil(Math.random() * rateUnits.size());
            }
            final RateUnitEnum[] rateTypes = new RateUnitEnum[numRateUnits];
            final List<RateUnitEnum> rateUnitsAvailable = new ArrayList<RateUnitEnum>(rateUnits);
            for (int j = 0; j < rateTypes.length; j++)
            {
                final int index = (int) Math.floor(Math.random() * rateUnitsAvailable.size());
                rateTypes[j] = rateUnitsAvailable.get(index);
                rateUnitsAvailable.remove(index);
                CallTypeEnum currentCallType = callType;

                int totalCharge = 0;
                int totalUsage = 0;
                if (rateTypes[j] == RateUnitEnum.EVENT)
                {
                    if (generateCharge && (j == 0 || generateMultipleCharges))
                    {
                        eventCharged = (long) Math.round(Math.random() * 100);
                    }

                    if (generateUsage && (j == 0 || generateMultipleUsage))
                    {
                        eventRated = (long) Math.round(Math.random() * 1000);
                    }

                    totalCharge += eventCharged;
                    totalUsage += eventRated;
                    currentCallType = CallTypeEnum.DOWNLOAD;
                }
                else if (rateTypes[j] == RateUnitEnum.SEC)
                {
                    if (generateCharge && (j == 0 || generateMultipleCharges))
                    {
                        durationCharged = (long) Math.round(Math.random() * 1000);
                    }

                    if (generateUsage && (j == 0 || generateMultipleUsage))
                    {
                        durationRated = (long) Math.round(Math.random() * 1000);
                    }

                    totalCharge += durationCharged;
                    totalUsage += durationRated;
                }
                else
                {
                    if (generateCharge && (j == 0 || generateMultipleCharges))
                    {
                        volumeCharged = (long) Math.round(Math.random() * 5000);
                    }

                    if (generateUsage && (j == 0 || generateMultipleUsage))
                    {
                        volumeRated = (long) Math.round(Math.random() * 1000);
                    }

                    totalCharge += volumeCharged;
                    totalUsage += volumeRated;
                }
                if (currentCallType == null)
                {
                    currentCallType = CallTypeEnum.WEB;
                }

                if (chargeMap.get(currentCallType) == null)
                {
                    chargeMap.put(currentCallType, new HashMap<RateUnitEnum, Integer>());
                }

                final Integer charge = chargeMap.get(currentCallType).get(rateTypes[j]);
                if (charge != null)
                {
                    totalCharge += charge.intValue();
                }

                if (usageMap.get(currentCallType) == null)
                {
                    usageMap.put(currentCallType, new HashMap<RateUnitEnum, Integer>());
                }
                final Integer usage = usageMap.get(currentCallType).get(rateTypes[j]);
                if (usage != null)
                {
                    totalUsage += usage.intValue();
                }

                if (totalCharge > 0 || totalUsage > 0)
                {
                    chargeMap.get(currentCallType).put(rateTypes[j], Integer.valueOf(totalCharge));
                    usageMap.get(currentCallType).put(rateTypes[j], Integer.valueOf(totalUsage));
                    if (LogSupport.isDebugEnabled(getContext()))
                    {
                        LogSupport.debug(getContext(), this, "Current call type = " + currentCallType + ", unit type = "
                                + rateTypes[j] + ", charge = " + totalCharge + ", usage = " + totalUsage);
                    }
                }
            }
            protocols[i] = generateProtocolSection(protocol, volumeCharged, durationCharged, eventCharged, volumeRated,
                durationRated, eventRated);
        }
        return protocols;
    }


    /**
     * Generates a MSISDN for use.
     *
     * @return A valid MSIDN.
     * @throws HomeException
     *             Thrown if there are problems creating the MSISDN or subscriber.
     */
    private String generateMsisdn() throws HomeException
    {
        String msisdn;
        do
        {
            msisdn = String.format("%03d", Long.valueOf(Math.round(Math.random() * 1000L)));
        }
        while (!Msisdn.MSISDN_PATTERN.matcher(String.valueOf(msisdn)).matches());

        final Msisdn msisdnObj = new Msisdn();
        msisdnObj.setMsisdn(msisdn);
        final Date date = CalendarSupportHelper.get(getContext()).findDateYearsAfter(-1, new Date());

        msisdnObj.setLastModified(date);
        msisdnObj.setStartTimestamp(date);
        msisdnObj.setState(MsisdnStateEnum.IN_USE);
        msisdnObj.setBAN(msisdn);
        msisdnObj.setSubscriberType(SubscriberTypeEnum.PREPAID);
        msisdnObj.setSpid(SPID);
        ((Home) getContext().get(MsisdnHome.class)).create(getContext(), msisdnObj);

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setIdentifier(msisdnHistoryID++);
        msisdnHistory.setTerminalId(msisdn);
        msisdnHistory.setBAN(msisdn);
        msisdnHistory.setSubscriberId(msisdn + "-1");
        msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(getContext()).getId());
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(today_);
        ((Home) getContext().get(MsisdnMgmtHistoryHome.class)).create(getContext(), msisdnHistory);

        final Subscriber subscriber = new Subscriber();
        subscriber.setId(msisdn + "-1");
        subscriber.setMSISDN(msisdn);
        subscriber.setBAN(msisdn);
        subscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
        subscriber.setStartDate(date);
        subscriber.setSpid(SPID);
        ((Home) getContext().get(SubscriberHome.class)).create(getContext(), subscriber);

        final Account account = new Account();
        account.setBAN(msisdn);
        account.setSystemType(SubscriberTypeEnum.PREPAID);
        account.setState(AccountStateEnum.ACTIVE);
        account.setSpid(SPID);
        ((Home) getContext().get(AccountHome.class)).create(getContext(), account);
        return msisdn;
    }


    /**
     * Verify all the fields of the ER are valid.
     *
     * @param msisdn
     *            MSISDN.
     * @param scpId
     *            SCP ID.
     * @param sdrUsage
     *            SDR usage.
     * @param sdrCharge
     *            SDR charge.
     * @param bundleCharge
     *            Bundle charge.
     * @param protocols
     *            Protocol section.
     * @param parsedEr
     *            Parsed ER.
     */
    private void verifyErFields(final String msisdn, final int scpId, final int sdrUsage, final int sdrCharge,
        final int bundleCharge, final String[] protocols, final List<String> parsedEr)
    {
        final IPCGPollerConfig config = (IPCGPollerConfig) getContext().get(IPCGPollerConfig.class);

        LogSupport.info(getContext(), this, "Parsed ER: " + parsedEr.toString());

        assertEquals("MSISDN does not match", msisdn, CRMProcessorSupport.getField(parsedEr, config.getPosMsisdn()));
        assertEquals("SCP ID does not match", scpId, CRMProcessorSupport.getInt(getContext(), CRMProcessorSupport
            .getField(parsedEr, config.getPosSCPID()), -1));
        assertEquals("SDR usage does not match", sdrUsage, CRMProcessorSupport.getInt(getContext(), CRMProcessorSupport
            .getField(parsedEr, config.getPosSdrUsage()), -1));
        assertEquals("Bundle charge does not match", bundleCharge, CRMProcessorSupport.getInt(getContext(),
            CRMProcessorSupport.getField(parsedEr, config.getPosBundleCharge()), -1));

        int index = config.getPosHttp();
        int generalCharge = 0;
        for (int i = 0; i < protocols.length; i++)
        {
            final String[] fields = protocols[i].split(",");
            assertEquals("Protocol Section " + i + ": Protocol does not match", fields[0], CRMProcessorSupport
                .getField(parsedEr, index));

            index++;
            final int totalCharge = Integer.parseInt(fields[config.getPosCharge() + 1]);
            assertEquals("Protocol Section " + i + ": Total Charge does not match", totalCharge, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosCharge()), -1));
            generalCharge += totalCharge;

            final int volumeCharge = Integer.parseInt(fields[config.getPosVolumeCharge() + 1]);
            assertEquals("Protocol Section " + i + ": Volume Charge does not match", volumeCharge, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosVolumeCharge()), -1));

            final int durationCharge = Integer.parseInt(fields[config.getPosDurationCharge() + 1]);
            assertEquals("Protocol Section " + i + ": Duration Charge does not match", durationCharge,
                CRMProcessorSupport.getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index
                    + config.getPosDurationCharge()), -1));

            final int eventCharge = Integer.parseInt(fields[config.getPosEventCharge() + 1]);
            assertEquals("Protocol Section " + i + ": Event Charge does not match", eventCharge, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosEventCharge()), -1));

            final int volumeRated = Integer.parseInt(fields[config.getPosVolDown() + 1]);
            assertEquals("Protocol Section " + i + ": Volume Rated does not match", volumeRated, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosVolDown()), -1));

            final int durationRated = Integer.parseInt(fields[config.getPosDurationRated() + 1]);
            assertEquals("Protocol Section " + i + ": Volume Charge does not match", durationRated, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosDurationRated()), -1));

            final int eventRated = Integer.parseInt(fields[config.getPosEventCounter() + 1]);
            assertEquals("Protocol Section " + i + ": Volume Charge does not match", eventRated, CRMProcessorSupport
                .getInt(getContext(), CRMProcessorSupport.getField(parsedEr, index + config.getPosEventCounter()), -1));

            assertEquals("Protocol Section " + i + ": Volume + Duration + Event Charge do not add up to Total Charge",
                volumeCharge + durationCharge + eventCharge, totalCharge);

            index += config.getPosUrlInfo() + 1;
        }

        if (sdrUsage > 0)
        {
            generalCharge = sdrCharge;
        }
        assertEquals("General Charge does not match", generalCharge, CRMProcessorSupport.getInt(getContext(),
            CRMProcessorSupport.getField(parsedEr, config.getPosGeneralCharge()), -1));
    }


    /**
     * Converts a string ER into a list of fields as returned by
     * {@link CRMProcessorSupport#makeArray}.
     *
     * @param er
     *            ER string.
     * @return A list of fields representing the ER.
     * @throws FilterOutException
     *             Thrown when there were problems parsing the ER.
     */
    private List<String> parseEr(final String er) throws FilterOutException
    {
        final DefaultER defaultEr = new DefaultER();
        defaultEr.parse(getContext(), er);
        final ProcessorInfo info = new ProcessorInfo(defaultEr.getTimestamp(), String.valueOf(defaultEr.getId()),
            defaultEr.getCharFields(), 20);
        final List<String> params = new ArrayList<String>();
        CRMProcessorSupport.makeArray(getContext(), params, info.getRecord(), info.getStartIndex(), ',',
            info.getErid(), this);
        return params;
    }


    /**
     * Generates an IPCG ER 501.
     *
     *@param erDate
     *            ER date.
     * @param scpId
     *            SCP ID.
     * @param msisdn
     *            MSISDN to be charged.
     * @param transactionDate
     *            Transaction date.
     * @param sdrCharge
     *            SDR charge.
     * @param bundlCharge
     *            Bundle charge.
     * @param sdrUsage
     *            SDR usage.
     * @param protocols
     *            Protocol section.
     * @return The full IPCG ER 501.
     */
    private String generateEr(final Date erDate, final int scpId, final String msisdn, final Date transactionDate,
        final int sdrCharge, final int bundlCharge, final int sdrUsage, final String... protocols)
    {
        final IPCGPollerConfig config = (IPCGPollerConfig) getContext().get(IPCGPollerConfig.class);
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        // generate the main section of the ER.
        final String[] fields = new String[config.getPosHttp() + 1];
        Arrays.fill(fields, "");
        fields[0] = "501";
        fields[1] = "500";
        fields[2] = "Synaxis-5600 ICG Blade Charging Event Record";
        fields[3] = "1";

        if (config.getPosMsisdn() > 0)
        {
            fields[config.getPosMsisdn() + 1] = msisdn;
        }

        // determines the general charge
        int generalCharge = 0;
        for (final String protocol : protocols)
        {
            final String[] params = protocol.split(",");
            generalCharge += Math.max(0, Integer.parseInt(params[config.getPosCharge() + 1]));
        }

        if (sdrUsage > 0)
        {
            generalCharge = sdrCharge;
        }

        if (config.getPosGeneralCharge() > 0)
        {
            fields[config.getPosGeneralCharge() + 1] = String.valueOf(generalCharge);
        }
        if (config.getPosBundleCharge() > 0)
        {
            fields[config.getPosBundleCharge() + 1] = String.valueOf(bundlCharge);
        }
        if (config.getPosSCPID() > 0)
        {
            fields[config.getPosSCPID() + 1] = String.valueOf(scpId);
        }
        if (config.getPosTransDate() > 0)
        {
            fields[config.getPosTransDate() + 1] = dateFormat.format(transactionDate);
        }
        if (config.getPosTransTime() > 0)
        {
            fields[config.getPosTransTime() + 1] = timeFormat.format(transactionDate);
        }
        if (config.getPosSdrUsage() > 0)
        {
            fields[config.getPosSdrUsage() + 1] = String.valueOf(sdrUsage);
        }

        // build the string
        final StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss,").format(new Date()));
        for (final String field : fields)
        {
            sb.append(field);
            sb.append(',');
        }
        for (int i = 0; i < protocols.length - 1; i++)
        {
            sb.append(protocols[i]);
            sb.append(",#,");
        }
        sb.append(protocols[protocols.length - 1]);
        return sb.toString();
    }


    /**
     * Generates a protocol section of the ER.
     *
     * @param protocolType
     *            Protocol type.
     * @param volumeCharge
     *            Volume charge.
     * @param durationCharge
     *            Duration charge.
     * @param eventCharge
     *            Event charge.
     * @param volumeRated
     *            Volume rated (in kilobytes).
     * @param durationRated
     *            Duration rated (in seconds).
     * @param eventRated
     *            Events rated (in number of events).
     * @return The formatted protocol section of an IPCG ER.
     */
    private String generateProtocolSection(final String protocolType, final long volumeCharge, final long durationCharge,
        final long eventCharge, final long volumeRated, final long durationRated, final long eventRated)
    {
        final IPCGPollerConfig config = (IPCGPollerConfig) getContext().get(IPCGPollerConfig.class);
        final String[] fields = new String[config.getPosUrlInfo() + 1];
        Arrays.fill(fields, "");
        fields[0] = protocolType;
        final long totalCharge = Math.max(0, volumeCharge) + Math.max(0, eventCharge) + Math.max(0, durationCharge);
        if (config.getPosCharge() > 0)
        {
            fields[config.getPosCharge() + 1] = String.valueOf(totalCharge);
        }
        if (config.getPosVolumeCharge() > 0)
        {
            fields[config.getPosVolumeCharge() + 1] = String.valueOf(Math.max(0, volumeCharge));
        }
        if (config.getPosDurationCharge() > 0)
        {
            fields[config.getPosDurationCharge() + 1] = String.valueOf(Math.max(0, durationCharge));
        }
        if (config.getPosEventCharge() > 0)
        {
            fields[config.getPosEventCharge() + 1] = String.valueOf(Math.max(0, eventCharge));
        }
        if (config.getPosVolDown() > 0)
        {
            fields[config.getPosVolDown() + 1] = String.valueOf(Math.max(0, volumeRated));
        }
        if (config.getPosDurationRated() > 0)
        {
            fields[config.getPosDurationRated() + 1] = String.valueOf(Math.max(0, durationRated));
        }
        if (config.getPosEventCounter() > 0)
        {
            fields[config.getPosEventCounter() + 1] = String.valueOf(Math.max(0, eventRated));
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length - 1; i++)
        {
            sb.append(fields[i]);
            sb.append(',');
        }
        sb.append(fields[fields.length - 1]);

        final StringBuilder msg = new StringBuilder();
        msg.append("Protocol section generated: protocol = ");
        msg.append(protocolType);
        msg.append(", volume charge = ");
        msg.append(volumeCharge);
        msg.append(", duration charge = ");
        msg.append(durationCharge);
        msg.append(", event charge = ");
        msg.append(eventCharge);
        msg.append(", volume rated = ");
        msg.append(volumeRated);
        msg.append(", duration rated = ");
        msg.append(durationRated);
        msg.append(", event rated = ");
        msg.append(eventRated);
        if (LogSupport.isDebugEnabled(getContext()))
        {
            LogSupport.debug(getContext(), this, msg.toString());
        }
        return sb.toString();
    }

    private Date today_ = null;
    private long msisdnHistoryID = 1;

    /**
     * SPID to be tested.
     */
    private static final int SPID = 12;
    /**
     * Error log file name.
     */
    private static final String ERROR_LOG_FILE_NAME = "/tmp/ipcgpoller.err";

    /**
     * MSISDN position.
     */
    private static final int POS_MSISDN = 3;

    /**
     * General Charge position.
     */
    private static final int POS_GENERAL_CHARGE = 9;

    /**
     * Bundle Charge position.
     */
    private static final int POS_BUNDLE_CHARGE = 11;

    /**
     * SCP ID position.
     */
    private static final int POS_SCP_ID = 17;

    /**
     * Transaction Date position.
     */
    private static final int POS_TRANS_DATE = 21;

    /**
     * Transaction Time position.
     */
    private static final int POS_TRANS_TIME = 22;

    /**
     * SDR Usage position.
     */
    private static final int POS_SDR_USAGE = 23;

    /**
     * Protocol Type position.
     */
    private static final int POS_PROTOCOL = 24;

    /**
     * Total Charge position in protocol section.
     */
    private static final int POS_PROTOCOL_TOTAL_CHARGE = 3;

    /**
     * Volume Charge position in protocol section.
     */
    private static final int POS_PROTOCOL_VOLUME_CHARGE = 4;

    /**
     * Duration Charge position in protocol section.
     */
    private static final int POS_PROTOCOL_DURATION_CHARGE = 5;

    /**
     * Event Charge position in protocol section.
     */
    private static final int POS_PROTOCOL_EVENT_CHARGE = 6;

    /**
     * Volume Rated position in protocol section.
     */
    private static final int POS_PROTOCOL_VOLUME_RATED = 8;

    /**
     * Volume Up (now Reserved) position in protocol section.
     */
    private static final int POS_PROTOCOL_VOLUME_UP = 9;

    /**
     * Duration Rated position in protocol section.
     */
    private static final int POS_PROTOCOL_DURATION_RATED = 10;

    /**
     * Event Rated position in protocol section.
     */
    private static final int POS_PROTOCOL_EVENT_RATED = 11;

    /**
     * URL Info (protocol section end marker) position in protocol section.
     */
    private static final int POS_PROTOCOL_URL_INFO = 12;
}
