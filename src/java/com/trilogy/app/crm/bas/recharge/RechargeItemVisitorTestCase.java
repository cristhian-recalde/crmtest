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

package com.trilogy.app.crm.bas.recharge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.CRMSpidTransientHome;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingHome;
import com.trilogy.app.crm.bean.GLCodeMappingTransientHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.app.crm.bean.IdentifierSequenceHome;
import com.trilogy.app.crm.bean.IdentifierSequenceTransientHome;
import com.trilogy.app.crm.bean.IdentifierSequenceXInfo;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.RecurringChargeErrorReport;
import com.trilogy.app.crm.bean.RecurringChargeErrorReportHome;
import com.trilogy.app.crm.bean.RecurringChargeErrorReportTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.app.crm.bean.SuspendedEntityHome;
import com.trilogy.app.crm.bean.SuspendedEntityTransientHome;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TaxAuthorityHome;
import com.trilogy.app.crm.bean.TaxAuthorityTransientHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.bean.WeekDayEnum;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryTransientHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryTransientHome;
import com.trilogy.app.crm.sequenceId.IncrementIdentifierCmd;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

/**
 * Superclass for recurring recharge item unit test cases.
 *
 * @author cindy.wong@redknee.com
 * @since 2-May-08
 */
public abstract class RechargeItemVisitorTestCase extends ContextAwareTestCase
{

    /**
     * SPID to use.
     */
    public static final int SPID = 2;
    /**
     * Adjustment type for the auxiliary service.
     */
    public static final int ADJUSTMENT_TYPE_ID = 50100;
    /**
     * Tax authority to use.
     */
    public static final int TAX_AUTHORITY_ID = 5;
    /**
     * GL code to use.
     */
    public static final String GL_CODE = "Test GL Code";
    /**
     * Account BAN.
     */
    public static final String BAN = "32123";
    /**
     * Subscriber ID.
     */
    public static final String SUBSCRIBER_ID = "45678-1";
    /**
     * Bill cycle ID.
     */
    public static final int BILL_CYCLE_ID = 4;
    /**
     * MSISDN to use.
     */
    public static final String MSISDN = "34567893123";

    /**
     * Billing date.
     */
    public static final Date BILLING_DATE;

    static
    {
        final Calendar cal = Calendar.getInstance();
        CalendarSupportHelper.get().clearTimeOfDay(cal);
        cal.set(Calendar.DAY_OF_MONTH, CalendarSupportHelper.get().findBillingDayOfMonth(cal.getTime()));
        BILLING_DATE = cal.getTime();
    }

    /**
     * Subscriber start date.
     */
    public static final Date SUBSCRIBER_START_DATE = CalendarSupportHelper.get().findDateMonthsAfter(-6, BILLING_DATE);
    /**
     * Agent name.
     */
    public static final String AGENT_NAME = "AuxServiceTest";
    /**
     * Available MSISDN.
     */
    public static final String AVAILABLE_MSISDN = "3948683143";
    /**
     * Price Plan ID.
     */
    public static final long PRICE_PLAN_ID = 43;
    /**
     * Price plan version.
     */
    public static final int PRICE_PLAN_VERSION = 4;


    /**
     * Create a new instance of <code>RechargeItemVisitorTestCase</code>.
     */
    public RechargeItemVisitorTestCase()
    {
        super();
    }


    /**
     * Create a new instance of <code>RechargeItemVisitorTestCase</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public RechargeItemVisitorTestCase(final String name)
    {
        super(name);
    }


    /**
     * {@inheritDoc}
     */
    protected void setUp()
    {
        super.setUp();

        // sets up homes
        initSpid();
        initGLCode();
        initTaxAuthority();
        initAdjustmentType();
        initBillCycle();
        initAccount();
        initSubscriber();
        initTransaction();
        initChargeHistory();
        initSuspendedEntities();
        initRecurringChargeErrorReport();
        initIdentifierSequence();
        initMsisdn();
        initPricePlanVersion();
        initPricePlan();

        try
        {
            subscriber_ = SubscriberSupport.getSubscriber(getContext(), SUBSCRIBER_ID);
            subscriber_.setContext(getContext());
        }
        catch (final HomeException exception)
        {
            fail("Cannot retrieve subscriber");
        }
    }


    /**
     * Initialize PricePlanVersion.
     */
    private void initPricePlanVersion()
    {
        final Home home = new AdapterHome(
                getContext(), 
                new PricePlanVersionTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlanVersion, com.redknee.app.crm.bean.core.PricePlanVersion>(
                        com.redknee.app.crm.bean.PricePlanVersion.class, 
                        com.redknee.app.crm.bean.core.PricePlanVersion.class));
        PricePlanVersion version = new PricePlanVersion();
        version.setActivateDate(SUBSCRIBER_START_DATE);
        version.setActivation(SUBSCRIBER_START_DATE);
        version.setCreatedDate(SUBSCRIBER_START_DATE);
        version.setId(PRICE_PLAN_ID);
        version.setVersion(PRICE_PLAN_VERSION);

        try
        {
            version = (PricePlanVersion) home.create(getContext(), version);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing PricePlanVersion");
        }
        getContext().put(PricePlanVersionHome.class, home);
    }


    /**
     * Initialize PricePlan.
     */
    private void initPricePlan()
    {
        final Home home = new AdapterHome(
                getContext(), 
                new PricePlanTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.PricePlan, com.redknee.app.crm.bean.core.PricePlan>(
                        com.redknee.app.crm.bean.PricePlan.class, 
                        com.redknee.app.crm.bean.core.PricePlan.class));
        getContext().put(PricePlanHome.class, home);
        PricePlan pricePlan = new PricePlan();
        pricePlan.setId(PRICE_PLAN_ID);
        pricePlan.setCurrentVersion(PRICE_PLAN_VERSION);
        pricePlan.setSpid(SPID);
        pricePlan.setEnabled(true);
        pricePlan.setName("Price Plan");
        pricePlan.setPricePlanType(SubscriberTypeEnum.HYBRID);

        try
        {
           // pricePlan.setVersions(PricePlanSupport.getVersion(getContext(), PRICE_PLAN_ID, PRICE_PLAN_VERSION));
            pricePlan = (PricePlan) home.create(getContext(), pricePlan);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing PricePlan");
        }
        getContext().put(PricePlanHome.class, home);
    }


    /**
     * Initialize Msisdn.
     */
    protected void initMsisdn()
    {
        final Home home = new AdapterHome(
                getContext(), 
                new MsisdnTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Msisdn, com.redknee.app.crm.bean.core.Msisdn>(
                        com.redknee.app.crm.bean.Msisdn.class, 
                        com.redknee.app.crm.bean.core.Msisdn.class));
        final Home msisdnHistoryHome = new MsisdnMgmtHistoryTransientHome(getContext());

        Msisdn msisdn = new Msisdn();
        msisdn.setMsisdn(MSISDN);
        msisdn.setSpid(SPID);
        msisdn.setStartTimestamp(SUBSCRIBER_START_DATE);
        msisdn.setState(MsisdnStateEnum.IN_USE);
        msisdn.setBAN(BAN);
        msisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        Msisdn freeMsisdn = new Msisdn();
        freeMsisdn.setMsisdn(AVAILABLE_MSISDN);
        freeMsisdn.setSpid(SPID);
        freeMsisdn.setState(MsisdnStateEnum.AVAILABLE);
        freeMsisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        final MsisdnMgmtHistory msisdnHistory = new MsisdnMgmtHistory();
        msisdnHistory.setTerminalId(MSISDN);
        msisdnHistory.setBAN(BAN);
        msisdnHistory.setSubscriberId(SUBSCRIBER_ID);
        try
        {
            msisdnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(getContext()).getId());
        }
        catch (HomeException e)
        {
            fail(e.getMessage());
        }
        msisdnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        msisdnHistory.setTimestamp(SUBSCRIBER_START_DATE);

        try
        {
            msisdn = (Msisdn) home.create(getContext(), msisdn);
            freeMsisdn = (Msisdn) home.create(getContext(), freeMsisdn);
            msisdnHistoryHome.create(getContext(), msisdnHistory);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Msisdn");
        }
        getContext().put(MsisdnHome.class, home);
        getContext().put(MsisdnMgmtHistoryHome.class, msisdnHistoryHome);
    }


    /**
     * Initialize IdentifierSequence.
     */
    protected void initIdentifierSequence()
    {
        final Home home = new IdentifierSequenceTransientHome(getContext());
        IdentifierSequence identifierSequence = new IdentifierSequence();
        identifierSequence.setIdentifier(IdentifierEnum.TRANSACTION_ID.getDescription());
        identifierSequence.setStartNum(1);
        identifierSequence.setNextNum(1000);
        identifierSequence.setEndNum(Long.MAX_VALUE);
        try
        {
            identifierSequence = (IdentifierSequence) home.create(getContext(), identifierSequence);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing IdentifierSequence");
        }
        final Home wrapper = new HomeProxy(home)
        {
            /**
             * Serialization plug for java version.
             */
            private static final long serialVersionUID = 1L;

            public Object cmd(final Context ctx, final Object obj) throws HomeException
            {
                final IncrementIdentifierCmd cmd = (IncrementIdentifierCmd) obj;
                final IdentifierSequence seq = (IdentifierSequence) find(ctx,
                        new EQ(IdentifierSequenceXInfo.IDENTIFIER, cmd.identifierSequenceName));
                final Long result = Long.valueOf(seq.getNextNum());
                seq.setNextNum(result.longValue() + 1);
                store(ctx, seq);
                return result;
            }
        };
        getContext().put(IdentifierSequenceHome.class, wrapper);
    }


    /**
     * Initialize SPID.
     */
    protected void initSpid()
    {
        final Home home = new CRMSpidTransientHome(getContext());
        CRMSpid spid = new CRMSpid();
        spid.setId(SPID);
        spid.setName("Test SPID");
        spid.setPrebilledRecurringChargeEnabled(false);
        spid.setRecurChargeSubscriberType(SubscriberTypeRecurringChargeEnum.OPTIONAL);
        spid.setRecurringChargeSubType(SubscriberTypeRecurringChargeEnum.OPTIONAL);
        spid.setSuspendServiceOnFailedCharge(false);
        spid.setTaxAuthority(TAX_AUTHORITY_ID);
        spid.setWeeklyRecurChargingDay(WeekDayEnum.get((short) BillCycleSupport.computeBillingDayOfWeek(BILLING_DATE)));
        try
        {
            spid = (CRMSpid) home.create(getContext(), spid);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing SPID");
        }
        getContext().put(CRMSpidHome.class, home);
    }


    /**
     * Enable prebilling in SPID.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up or updating the SPID.
     */
    protected void enablePrebilling() throws HomeException
    {
        final CRMSpid spid = SpidSupport.getCRMSpid(getContext(), SPID);
        spid.setPrebilledRecurringChargeEnabled(true);
        final Home home = (Home) getContext().get(CRMSpidHome.class);
        home.store(getContext(), spid);
    }


    /**
     * Initialize GL Code.
     */
    protected void initGLCode()
    {
        final Home home = new GLCodeMappingTransientHome(getContext());
        final GLCodeMapping glCode = new GLCodeMapping();
        glCode.setGlCode(GL_CODE);
        glCode.setSpid(SPID);
        try
        {
            home.create(getContext(), glCode);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing GL Code");
        }
        getContext().put(GLCodeMappingHome.class, home);
    }


    /**
     * Initialize TaxAuthority.
     */
    protected void initTaxAuthority()
    {
        final Home home = new TaxAuthorityTransientHome(getContext());
        final TaxAuthority bean = new TaxAuthority();
        bean.setIdentifier(TAX_AUTHORITY_ID);
        bean.setSpid(SPID);
        bean.setTaxAuthName("Tax Authority");
        bean.setTaxRate(0);
        try
        {
            home.create(getContext(), bean);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing TaxAuthority");
        }
        getContext().put(TaxAuthorityHome.class, home);
    }


    /**
     * Initialize adjustment type.
     */
    protected void initAdjustmentType()
    {
        final Home home = new AdapterHome(
                getContext(), 
                new AdjustmentTypeTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.AdjustmentType, com.redknee.app.crm.bean.core.AdjustmentType>(
                        com.redknee.app.crm.bean.AdjustmentType.class, 
                        com.redknee.app.crm.bean.core.AdjustmentType.class));
        AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setCode(ADJUSTMENT_TYPE_ID);
        adjustmentType.setName("MonthlyAdj");
        adjustmentType.setDesc("Test adjustment type");
        adjustmentType.setCategory(false);
        adjustmentType.setState(AdjustmentTypeStateEnum.ACTIVE);
        adjustmentType.setSystem(false);
        final Map<Integer, AdjustmentInfo> adjustmentSpidInfo = new HashMap<Integer, AdjustmentInfo>();
        final AdjustmentInfo info = new AdjustmentInfo();
        info.setSpid(SPID);
        info.setGLCode(GL_CODE);
        info.setTaxAuthority(TAX_AUTHORITY_ID);
        adjustmentSpidInfo.put(Integer.valueOf(SPID), info);
        adjustmentType.setAdjustmentSpidInfo(adjustmentSpidInfo);
        try
        {
            adjustmentType = (AdjustmentType) home.create(getContext(), adjustmentType);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Adjustment Type");
        }
        getContext().put(AdjustmentTypeHome.class, home);
    }


    /**
     * Initialize BillCycle.
     */
    protected void initBillCycle()
    {
        final Home home =  new AdapterHome(
                getContext(), 
                new BillCycleTransientHome(getContext()), 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.BillCycle, com.redknee.app.crm.bean.core.BillCycle>(
                        com.redknee.app.crm.bean.BillCycle.class, 
                        com.redknee.app.crm.bean.core.BillCycle.class));
        BillCycle billCycle = new BillCycle();
        billCycle.setIdentifier(BILL_CYCLE_ID);
        billCycle.setSpid(SPID);
        billCycle.setDayOfMonth(CalendarSupportHelper.get().findBillingDayOfMonth(BILLING_DATE));
        billCycle.setDescription("Bill Cycle");

        try
        {
            billCycle = (BillCycle) home.create(getContext(), billCycle);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing BillCycle");
        }
        getContext().put(BillCycleHome.class, home);
    }


    /**
     * Initialize Account.
     */
    protected void initAccount()
    {
        final Home home = new AccountTransientHome(getContext());
        Account account = new Account();
        account.setAccountName("Account");
        account.setBAN(BAN);
        account.setBillCycleID(BILL_CYCLE_ID);
        account.setSpid(SPID);
        account.setState(AccountStateEnum.ACTIVE);
        account.setSystemType(SubscriberTypeEnum.POSTPAID);
        account.setTaxAuthority(TAX_AUTHORITY_ID);

        try
        {
            account = (Account) home.create(getContext(), account);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Account");
        }
        getContext().put(AccountHome.class, home);
    }


    /**
     * Initialize Subscriber.
     */
    protected void initSubscriber()
    {
        final Home home = new SubscriberTransientHome(getContext());
        Subscriber subscriber = new Subscriber();
        subscriber.setBAN(BAN);
        subscriber.setId(SUBSCRIBER_ID);
        subscriber.setMSISDN(MSISDN);
        subscriber.setSpid(SPID);
        subscriber.setState(SubscriberStateEnum.ACTIVE);
        subscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        subscriber.setWeeklyRecurringCharge(false);
        subscriber.setStartDate(SUBSCRIBER_START_DATE);
        subscriber.setPricePlan(PRICE_PLAN_ID);
        subscriber.setPricePlanVersion(PRICE_PLAN_VERSION);

        try
        {
            subscriber = (Subscriber) home.create(getContext(), subscriber);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Subscriber");
        }

        getContext().put(SubscriberHome.class, home);
    }


    /**
     * Initialize Transaction home.
     */
    protected void initTransaction()
    {
        final Home home = new AdapterHome(
                getContext(), 
                new TransactionTransientHome(getContext()), 
                new ExtendedBeanAdapter<Transaction, com.redknee.app.crm.bean.core.Transaction>(
                        com.redknee.app.crm.bean.Transaction.class, 
                        com.redknee.app.crm.bean.core.Transaction.class));
        getContext().put(TransactionHome.class, home);
    }

    /**
     * Initialize SubscriberSubscriptionHistory home.
     */
    protected void initChargeHistory()
    {
        final Home home = new SubscriberSubscriptionHistoryTransientHome(getContext());
        getContext().put(SubscriberSubscriptionHistoryHome.class, home);
    }

    /**
     * Initialize SuspendedEntities.
     */
    protected void initSuspendedEntities()
    {
        final Home home = new SuspendedEntityTransientHome(getContext());
        getContext().put(SuspendedEntityHome.class, home);
    }


    /**
     * Initialize RecurringChargeErrorReport.
     */
    protected void initRecurringChargeErrorReport()
    {
        final Home home = new RecurringChargeErrorReportTransientHome(getContext());
        getContext().put(RecurringChargeErrorReportHome.class, home);
    }


    /**
     * Retrieves the subscriber being tested.
     *
     * @return The subscriber being tested.
     */
    public final Subscriber getSubscriber()
    {
        return this.subscriber_;
    }


    /**
     * Creates and returns a context with a transaction home wrapped to throw a specific
     * home exception.
     *
     * @param context
     *            The operating context.
     * @param homeException
     *            Exception to be thrown.
     * @return The wrapped context.
     */
    protected Context addExceptionWrapper(final Context context, final HomeException homeException)
    {
        final Context subContext = context.createSubContext();
        final Home transactionHome = (Home) context.get(TransactionHome.class);
        final Home wrapperHome = new HomeProxy(transactionHome)
        {
            /**
             * Serialization plug for java version.
             */
            private static final long serialVersionUID = 1L;

            public Object create(final Context ctx, final Object obj) throws HomeException
            {
                throw homeException;
            }
        };
        subContext.put(TransactionHome.class, wrapperHome);
        return subContext;

    }


    /**
     * Test the method for handling transaction when home exception is generated.
     *
     * @param handleMethod
     *            The method which handles the transaction.
     * @param visitor
     *            The visitor being called.
     * @param rechargeItem
     *            The item being recharged.
     * @param rechargeItemId
     *            Identifier of the recharge item.
     * @param rechargeItemType
     *            Type of the recharge item.
     * @param suspensionEntitiesMethod
     *            The method of Subscriber which returns the suspended entities.
     * @param exception
     *            Home exception generated.
     * @param expectedResult
     *            Expected recharge result code.
     * @param suspend
     *            Whether suspension is enabled.
     * @param expectSuspend
     *            Whether suspension is expected
     * @throws HomeException
     *             Thrown if there are problems verifying the results.
     * @throws InvocationTargetException
     *             Thrown by reflection.
     * @throws IllegalAccessException
     *             Thrown by reflection.
     * @throws NoSuchMethodException
     *             Thrown by reflection.
     */
    public void handleTransactionHomeException(final Method handleMethod, final AbstractRechargeItemVisitor visitor,
        final Object rechargeItem, final long rechargeItemId, final ChargedItemTypeEnum rechargeItemType,
        final Method suspensionEntitiesMethod, final HomeException exception, final int expectedResult,
        final boolean suspend, final boolean expectSuspend) throws HomeException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(exception.getMessage(), transactions);
        assertTrue(exception.getMessage(), transactions.isEmpty());

        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        visitor.setSuspendOnFailure(suspend);
        Map suspendedEntities = (Map) suspensionEntitiesMethod.invoke(getSubscriber(), new Object[]{});
        assertTrue(exception.getMessage(), suspendedEntities == null || suspendedEntities.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), exception);

        handleMethod.invoke(visitor, subContext, rechargeItem);
        // ease up on the system running this test
        Thread.yield();

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(exception.getMessage(), transactions);
        assertTrue(exception.getMessage(), transactions.isEmpty());

        final Collection errors = ((Home) getContext().get(RecurringChargeErrorReportHome.class))
            .selectAll(getContext());
        assertNotNull(exception.getMessage(), errors);
        assertTrue(exception.getMessage(), errors.size() == 1);
        final RecurringChargeErrorReport report = (RecurringChargeErrorReport) errors.iterator().next();
        assertEquals(exception.getMessage(), AGENT_NAME, report.getAgent());
        assertEquals(exception.getMessage(), expectedResult, report.getResultCode());
        assertEquals(exception.getMessage(), rechargeItemId, report.getChargedItemId());
        assertEquals(exception.getMessage(), rechargeItemType, report.getChargedItemType());

        suspendedEntities = (Map) suspensionEntitiesMethod.invoke(getSubscriber(), new Object[]{});
        assertNotNull(exception.getMessage(), suspendedEntities);
        assertEquals(exception.getMessage(), expectSuspend, getSubscriber().isSuspendingEntities());
        assertEquals(exception.getMessage(), !expectSuspend, suspendedEntities.isEmpty());
    }


    /**
     * Test the method for handling the transaction when OCG replies failure.
     *
     * @param handleMethod
     *            The method which handles the transaction.
     * @param visitor
     *            The visitor being called.
     * @param rechargeItem
     *            The item being recharged.
     * @param rechargeItemId
     *            Identifier of the recharge item.
     * @param rechargeItemType
     *            Type of the recharge item.
     * @param suspendedEntitiesMethod
     *            The method of Subscriber which returns the suspended entities.
     * @param ocgCode
     *            OCG result code.
     * @param expectedResult
     *            Expected recharge result code.
     * @param suspend
     *            Whether suspension is enabled.
     * @param expectSuspend
     *            Whether suspension is expected
     * @throws HomeException
     *             Thrown if there are problems verifying the results.
     * @throws NoSuchMethodException
     *             Thrown by reflection.
     * @throws InvocationTargetException
     *             Thrown by reflection.
     * @throws IllegalAccessException
     *             Thrown by reflection.
     */
    public void handleTransactionOcgFailureTest(final Method handleMethod, final AbstractRechargeItemVisitor visitor,
        final Object rechargeItem, final long rechargeItemId, final ChargedItemTypeEnum rechargeItemType,
        final Method suspendedEntitiesMethod, final int ocgCode, final int expectedResult, final boolean suspend,
        final boolean expectSuspend) throws HomeException, IllegalAccessException, InvocationTargetException,
        NoSuchMethodException
    {
        final String msg = "OCG = " + ocgCode + ", expectedResult = " + expectedResult + ", suspend = " + suspend
            + ", expectSuspend = " + expectSuspend;
        handleTransactionHomeException(handleMethod, visitor, rechargeItem, rechargeItemId, rechargeItemType,
            suspendedEntitiesMethod, new OcgTransactionException(msg, ocgCode), expectedResult, suspend, expectSuspend);

        final Collection errors = ((Home) getContext().get(RecurringChargeErrorReportHome.class))
            .selectAll(getContext());
        assertNotNull(msg, errors);
        assertTrue(msg, errors.size() == 1);
        final RecurringChargeErrorReport report = (RecurringChargeErrorReport) errors.iterator().next();
        assertEquals(msg, ocgCode, report.getOcgResultCode());
    }


    /**
     * Test the method for handling transaction when OCG replies failure.
     *
     * @param handleMethod
     *            The method which handles the transaction.
     * @param visitor
     *            The visitor being called.
     * @param rechargeItem
     *            The item being recharged.
     * @param rechargeItemId
     *            Identifier of the recharge item.
     * @param rechargeItemType
     *            Type of the recharge item.
     * @param suspendedEntitiesMethod
     *            The method of Subscriber which returns the suspended entities.
     * @param ocgCode
     *            OCG result code.
     * @param expectedResult
     *            Expected recharge result code.
     * @param expectSuspend
     *            Whether suspension is expected.
     * @throws HomeException
     *             Thrown if there are problems verifying the results.
     * @throws NoSuchMethodException
     *             Thrown by reflection.
     * @throws InvocationTargetException
     *             Thrown by reflection.
     * @throws IllegalAccessException
     *             Thrown by reflection.
     */
    public void handleTransactionOcgFailureTests(final Method handleMethod, final AbstractRechargeItemVisitor visitor,
        final Object rechargeItem, final long rechargeItemId, final ChargedItemTypeEnum rechargeItemType,
        final Method suspendedEntitiesMethod, final int ocgCode, final int expectedResult, final boolean expectSuspend)
        throws HomeException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        handleTransactionOcgFailureTest(handleMethod, visitor, rechargeItem, rechargeItemId, rechargeItemType,
            suspendedEntitiesMethod, ocgCode, expectedResult, false, false);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        getSubscriber().setSuspendedAuxServices(new HashMap());
        getSubscriber().setSuspendedBundles(new HashMap());
        getSubscriber().setSuspendedPackages(new HashMap());
        getSubscriber().setSuspendedServices(new HashMap());
        handleTransactionOcgFailureTest(handleMethod, visitor, rechargeItem, rechargeItemId, rechargeItemType,
            suspendedEntitiesMethod, ocgCode, expectedResult, true, expectSuspend);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        getSubscriber().setSuspendedAuxServices(new HashMap());
        getSubscriber().setSuspendedBundles(new HashMap());
        getSubscriber().setSuspendedPackages(new HashMap());
        getSubscriber().setSuspendedServices(new HashMap());
    }

    /**
     * Subscriber to use.
     */
    protected Subscriber subscriber_;

}
