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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistory;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryHome;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.product.s2100.ErrorCode;


/**
 * JUnit/XTest test case for {@link RechargeSubscriberAuxServiceVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 30-Apr-08
 */
public class TestRechargeSubscriberAuxServiceVisitor extends RechargeItemVisitorTestCase
{

    /**
     * Auxiliary service ID to use.
     */
    public static final long AUXILIARY_SERVICE_ID = 10;
    /**
     * VPN MSISDN.
     */
    public static final String VPN_MSISDN = "3904839213";

    /**
     * VPN group leader subscriber ID.
     */
    public static final String VPN_LEADER_ID = "46821-1";

    /**
     * VPN auxiliary service ID.
     */
    public static final long VPN_AUXILIARY_SERVICE_ID = 68;


    /**
     * Create a new instance of <code>TestRechargeSubscriberAuxServiceVisitor</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestRechargeSubscriberAuxServiceVisitor(final String name)
    {
        super(name);
    }


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

        final TestSuite suite = new TestSuite(TestRechargeSubscriberAuxServiceVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        initVpn();
        initAuxiliaryService();
        try
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
            getSubscriber(), false, 1, true, true, false, false);
        } catch (HomeException e)
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                    getSubscriber(), false, BILLING_DATE, BillCycleSupport.getDateOfBillCycleLastDay(CalendarSupportHelper.get(getContext()).findBillingDayOfMonth(BILLING_DATE), 
                            BILLING_DATE), 1, true, true, false);
        }

    }


    /**
     * Initialize VPN-related entities.
     */
    private void initVpn()
    {
        Msisdn vpnMsisdn = new Msisdn();
        vpnMsisdn.setMsisdn(VPN_MSISDN);
        vpnMsisdn.setSpid(SPID);
        vpnMsisdn.setStartTimestamp(SUBSCRIBER_START_DATE);
        vpnMsisdn.setState(MsisdnStateEnum.IN_USE);
        vpnMsisdn.setBAN(BAN);
        vpnMsisdn.setSubscriberType(SubscriberTypeEnum.POSTPAID);

        final MsisdnMgmtHistory vpnHistory = new MsisdnMgmtHistory();
        vpnHistory.setIdentifier(2L);
        vpnHistory.setTerminalId(VPN_MSISDN);
        vpnHistory.setBAN(BAN);
        vpnHistory.setSubscriberId(VPN_LEADER_ID);
        try
        {
            vpnHistory.setSubscriptionType(SubscriptionType.getINSubscriptionType(getContext()).getId());
        }
        catch (HomeException e)
        {
            fail(e.getMessage());
        }
        vpnHistory.setEvent(HistoryEventSupport.SUBID_MOD);
        vpnHistory.setTimestamp(SUBSCRIBER_START_DATE);

        try
        {
            vpnMsisdn = (Msisdn) ((Home) getContext().get(MsisdnHome.class)).create(getContext(), vpnMsisdn);
            ((Home) getContext().get(MsisdnMgmtHistoryHome.class)).create(getContext(), vpnHistory);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing VPN MSISDN");
        }

        try
        {
            final Account account = AccountSupport.getAccount(getContext(), BAN);
            account.setVpn(true);
            account.setVpnMSISDN(VPN_MSISDN);
            ((Home) getContext().get(AccountHome.class)).store(getContext(), account);
        }
        catch (final HomeException exception)
        {
            fail("Cannot update the account");
        }

        Subscriber vpnLeader = new Subscriber();
        vpnLeader.setBAN(BAN);
        vpnLeader.setId(VPN_LEADER_ID);
        vpnLeader.setMSISDN(VPN_MSISDN);
        vpnLeader.setSpid(SPID);
        vpnLeader.setStartDate(SUBSCRIBER_START_DATE);
        vpnLeader.setState(SubscriberStateEnum.ACTIVE);
        vpnLeader.setSubscriberType(SubscriberTypeEnum.POSTPAID);
        vpnLeader.setWeeklyRecurringCharge(false);

        try
        {
            vpnLeader = (Subscriber) ((Home) getContext().get(SubscriberHome.class)).create(getContext(), vpnLeader);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing VPN leader");
        }
    }


    /**
     * Initialize AuxiliaryService.
     */
    private void initAuxiliaryService()
    {
        final Home home = new AuxiliaryServiceTransientHome(getContext());

        AuxiliaryService vpnAuxiliaryService = new AuxiliaryService();
        vpnAuxiliaryService.setIdentifier(VPN_AUXILIARY_SERVICE_ID);
        vpnAuxiliaryService.setActivationFee(ActivationFeeModeEnum.PRORATE);
        vpnAuxiliaryService.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        vpnAuxiliaryService.setAdjustmentTypeDescription("VPN auxiliary service adjustment type");
        vpnAuxiliaryService.setCharge(1500);
        vpnAuxiliaryService.setChargingModeType(ServicePeriodEnum.MONTHLY);
        vpnAuxiliaryService.setGLCode(GL_CODE);
        vpnAuxiliaryService.setName("VPN auxiliary service");
        vpnAuxiliaryService.setSpid(SPID);
        vpnAuxiliaryService.setState(AuxiliaryServiceStateEnum.ACTIVE);
        vpnAuxiliaryService.setSubscriberType(SubscriberTypeEnum.HYBRID);
        vpnAuxiliaryService.setTaxAuthority(TAX_AUTHORITY_ID);
        vpnAuxiliaryService.setTechnology(TechnologyEnum.ANY);
        vpnAuxiliaryService.setType(AuxiliaryServiceTypeEnum.Vpn);

        AuxiliaryService auxiliaryService = new AuxiliaryService();
        auxiliaryService.setIdentifier(AUXILIARY_SERVICE_ID);
        auxiliaryService.setActivationFee(ActivationFeeModeEnum.PRORATE);
        auxiliaryService.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        auxiliaryService.setAdjustmentTypeDescription("Monthly auxiliary service adjustment type");
        auxiliaryService.setCharge(1000);
        auxiliaryService.setChargingModeType(ServicePeriodEnum.MONTHLY);
        auxiliaryService.setGLCode(GL_CODE);
        auxiliaryService.setName("Monthly auxiliary service");
        auxiliaryService.setSpid(SPID);
        auxiliaryService.setState(AuxiliaryServiceStateEnum.ACTIVE);
        auxiliaryService.setSubscriberType(SubscriberTypeEnum.HYBRID);
        auxiliaryService.setTaxAuthority(TAX_AUTHORITY_ID);
        auxiliaryService.setTechnology(TechnologyEnum.ANY);
        auxiliaryService.setType(AuxiliaryServiceTypeEnum.Basic);

        try
        {
            auxiliaryService = (AuxiliaryService) home.create(getContext(), auxiliaryService);
            vpnAuxiliaryService = (AuxiliaryService) home.create(getContext(), vpnAuxiliaryService);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing AuxiliaryService");
        }
        getContext().put(AuxiliaryServiceHome.class, home);
    }

    private SubscriberAuxiliaryService createAssociation()
    {
        final SubscriberAuxiliaryService association = new SubscriberAuxiliaryService();
        association.setAuxiliaryServiceIdentifier(AUXILIARY_SERVICE_ID);
        association.setContext(getContext());
        association.setCreated(SUBSCRIBER_START_DATE);
        association.setEndDate(CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE));
        association.setIdentifier(AUXILIARY_SERVICE_ID);
        association.setProvisioned(true);
        association.setStartDate(SUBSCRIBER_START_DATE);
        association.setSubscriberIdentifier(SUBSCRIBER_ID);
        association.setType(AuxiliaryServiceTypeEnum.Basic);
        return association;
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, SubscriberAuxiliaryService)}
     * when the subscriber has the provided auxiliary service with start date in the
     * future, and it has not yet been charged.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForFutureStartDate() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();
        association.setStartDate(CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE));

        assertFalse(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service with end date which has
     * passed and it has not yet been charged.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSubscriptionEnded() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();
        association.setEndDate(CalendarSupportHelper.get(getContext()).getDayBefore(BILLING_DATE));

        assertFalse(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has been suspended.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSuspended() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        assertFalse(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and all recurring charges of
     * the subscriber should be suspended.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSuspending() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);

        assertFalse(visitor_.isChargeable(getContext(), association));
    }

    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the postpaid subscriber has the provided auxiliary service and all recurring
     * charges of the subscriber should be suspended.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSuspendingPostpaid() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);

        assertTrue(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and all recurring charges of
     * the subscriber should be suspended and suspendOnFailure is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSuspendingNoSuspendOnFailure() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);

        assertTrue(visitor_.isChargeable(getContext(), association));
    }

    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and all recurring charges of
     * the subscriber should not be suspended.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForSuspendingSubscriberNotSuspending() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(false);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);

        assertTrue(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has already been
     * charged.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForChargedNoPrebilling() throws HomeException
    {
        final SubscriberAuxiliaryService association = createAssociation();

        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);

        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                service.getCharge());
        //only charge history is used to determine charge already exists
        //TransactionSupport.createTransaction(getContext(), transaction, true);

        SubscriberSubscriptionHistorySupport.addChargingHistory(getContext(), association, getSubscriber(), 
                HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.AUXSERVICE, transaction.getAmount(), 
                service.getCharge(), transaction, BILLING_DATE);

        assertFalse(visitor_.isChargeable(getContext(), association));
    }

    /**
     * Test
     * {@link RechargeSubscriberAuxServiceVisitor#isChargeable(Context, AuxiliaryService)}
     * when the subscriber has the provided auxiliary service and it has already been
     * charged.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testIsChargeableForChargedPrebilling() throws HomeException
    {
        enablePrebilling();

        final SubscriberAuxiliaryService association = createAssociation();

        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);

        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                service.getCharge());
        //only charge history is used to determine charge already exists
        //TransactionSupport.createTransaction(getContext(), transaction, true);

        SubscriberSubscriptionHistorySupport.addChargingHistory(getContext(), association, getSubscriber(), 
                HistoryEventTypeEnum.CHARGE, ChargedItemTypeEnum.AUXSERVICE, transaction.getAmount(), 
                service.getCharge(), transaction, BILLING_DATE);

        assertFalse(visitor_.isChargeable(getContext(), association));
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when
     * there is no error.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testHandleServiceTransaction() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
        final SubscriberAuxiliaryService association = createAssociation();

        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        visitor_.handleServiceTransaction(getContext(), association);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(service.getCharge(), transaction.getAmount());
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when
     * there is no error and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testHandleServiceTransactionPrebilling() throws HomeException
    {
        enablePrebilling();
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
        final SubscriberAuxiliaryService association = createAssociation();

        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        visitor_.handleServiceTransaction(getContext(), association);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(service.getCharge(), transaction.getAmount());
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when
     * there is no error and the charge is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionProrated() throws HomeException
    {
        final Date monthBefore = CalendarSupportHelper.get(getContext()).getDayAfter(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, BILLING_DATE));
        final Date monthAfter = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, BILLING_DATE);
        Date currentDate = monthBefore;
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.WEEKLY);
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());
            final SubscriberAuxiliaryService association = createAssociation();

            visitor_.handleServiceTransaction(getContext(), association);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertEquals(sb.toString(), 1, transactions.size());
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), service.getCharge());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + service.getCharge(), transaction.getAmount() != service.getCharge());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when
     * there is no error and the charge is prorated and prebilling enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionProratedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Date monthBefore = CalendarSupportHelper.get(getContext()).getDayAfter(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, BILLING_DATE));
        final Date monthAfter = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, BILLING_DATE);
        Date currentDate = monthBefore;
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());
            final SubscriberAuxiliaryService association = createAssociation();

            visitor_.handleServiceTransaction(getContext(), association);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertEquals(sb.toString(), 1, transactions.size());
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), service.getCharge());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + service.getCharge(), transaction.getAmount() != service.getCharge());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when the
     * charge is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionWeeklyProrated() throws HomeException
    {
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            final SubscriberAuxiliaryService association = createAssociation();
            visitor_.handleServiceTransaction(getContext(), association);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertEquals(sb.toString(), 1, transactions.size());
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), service.getCharge());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + service.getCharge(), transaction.getAmount() != service.getCharge());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when the
     * charge is weekly and prorated and prebilling enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionWeeklyProratedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        service.setChargingModeType(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberAuxServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            final SubscriberAuxiliaryService association = createAssociation();
            visitor_.handleServiceTransaction(getContext(), association);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertEquals(sb.toString(), 1, transactions.size());
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), service.getCharge());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + service.getCharge(), transaction.getAmount() != service.getCharge());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when OCG
     * replied an error.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     * @throws NoSuchMethodException
     *             Thrown by reflection.
     * @throws InvocationTargetException
     *             Thrown by reflection.
     * @throws IllegalAccessException
     *             Thrown by reflection.
     */
    public void testHandleServiceTransactionOcgError() throws HomeException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        final Map<Integer, Integer> errorCodeMap = new HashMap<Integer, Integer>();

        errorCodeMap.put(Integer.valueOf(ErrorCode.BAL_EXPIRED),
                Integer.valueOf(RechargeConstants.RECHARGE_FAIL_ABM_EXPIRED));
        errorCodeMap.put(Integer.valueOf(ErrorCode.BALANCE_INSUFFICIENT),
                Integer.valueOf(RechargeConstants.RECHARGE_FAIL_ABM_LOWBALANCE));
        errorCodeMap.put(Integer.valueOf(ErrorCode.NOT_ENOUGH_BAL),
                Integer.valueOf(RechargeConstants.RECHARGE_FAIL_ABM_LOWBALANCE));
        errorCodeMap.put(Integer.valueOf(ErrorCode.INVALID_ACCT_NUM),
                Integer.valueOf(RechargeConstants.RECHARGE_FAIL_ABM_INVALIDPROFILE));

        final Method handleMethod = RechargeSubscriberAuxServiceVisitor.class.getMethod("handleServiceTransaction",
            Context.class, AuxiliaryService.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedAuxServices", new Class[0]);
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        SubscriberAuxiliaryService subAuxService = new SubscriberAuxiliaryService();
        subAuxService.setAuxiliaryService(service);
        subAuxService.setAuxiliaryServiceIdentifier(service.getID());
        Subscriber sub = new Subscriber();
        sub.setId("1234-4");
        subAuxService.setSubscriber(sub);
        for (final Map.Entry<Integer, Integer> entry : errorCodeMap.entrySet())
        {
            handleTransactionOcgFailureTests(handleMethod, visitor_, subAuxService, service.getIdentifier(),
                    ChargedItemTypeEnum.AUXSERVICE, suspendedEntitiesMethod, entry.getKey().intValue(),
                    entry.getValue().intValue(), true);
        }

        for (int i = 0; i < 1005; i++)
        {
            if (!errorCodeMap.containsKey(Integer.valueOf(i)))
            {
                handleTransactionOcgFailureTests(handleMethod, visitor_, subAuxService, service.getIdentifier(),
                    ChargedItemTypeEnum.AUXSERVICE, suspendedEntitiesMethod, i,
                    RechargeConstants.RECHARGE_FAIL_OCG_UNKNOWN, false);
            }
        }
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleServiceTransaction} when a
     * home exception (not related to OCG) is generated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     * @throws NoSuchMethodException
     *             Thrown by reflection.
     * @throws InvocationTargetException
     *             Thrown by reflection.
     * @throws IllegalAccessException
     *             Thrown by reflection.
     */
    public void testHandleServiceTransactionHomeException() throws HomeException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        final Method handleMethod = RechargeSubscriberAuxServiceVisitor.class.getMethod("handleServiceTransaction",
            Context.class, AuxiliaryService.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedAuxServices", new Class[0]);
        final AuxiliaryService service = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
                AUXILIARY_SERVICE_ID);
        SubscriberAuxiliaryService subAuxService = new SubscriberAuxiliaryService();
        subAuxService.setAuxiliaryService(service);
        subAuxService.setAuxiliaryServiceIdentifier(service.getID());
        Subscriber sub = new Subscriber();
        sub.setId("1234-4");
        subAuxService.setSubscriber(sub);
        final HomeException exception = new HomeException("generated home exception");
        handleTransactionHomeException(handleMethod, visitor_, subAuxService, service.getIdentifier(),
            ChargedItemTypeEnum.AUXSERVICE, suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME,
            true, false);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        getSubscriber().setSuspendedAuxServices(new HashMap());
        handleTransactionHomeException(handleMethod, visitor_, subAuxService, service.getIdentifier(),
            ChargedItemTypeEnum.AUXSERVICE, suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME,
            false, false);
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleVpnTransaction} in the normal
     * case.
     *
     * @throws InvalidVPNGroupLeaderException
     *             Thrown if the VPN group leader is invalid or found.
     * @throws HomeException
     *             Thrown if there are problems looking up any info.
     */
    public void testHandleVpnTransaction() throws HomeException, InvalidVPNGroupLeaderException
    {
        final AuxiliaryService vpnAuxiliaryService = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
            VPN_AUXILIARY_SERVICE_ID);
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
            vpnAuxiliaryService.getCharge());
        assertNotNull(transaction);
        assertEquals(transaction.getBAN(), BAN);
        assertEquals(transaction.getSubscriberID(), SUBSCRIBER_ID);
        assertEquals(transaction.getMSISDN(), MSISDN);
        RechargeSubscriberAuxServiceVisitor.handleVpnAndGroupTransaction(getContext(), subscriber_, vpnAuxiliaryService, transaction);
        assertEquals(transaction.getSubscriberID(), VPN_LEADER_ID);
        assertEquals(transaction.getMSISDN(), VPN_MSISDN);
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleVpnTransaction} when the
     * auxiliary service is not VPN.
     *
     * @throws InvalidVPNGroupLeaderException
     *             Thrown if the VPN group leader is invalid or found.
     * @throws HomeException
     *             Thrown if there are problems looking up any info.
     */
    public void testHandleVpnTransactionNotVpn() throws HomeException, InvalidVPNGroupLeaderException
    {
        final AuxiliaryService vpnAuxiliaryService = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
            AUXILIARY_SERVICE_ID);
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
            vpnAuxiliaryService.getCharge());
        assertNotNull(transaction);
        assertEquals(transaction.getBAN(), BAN);
        assertEquals(transaction.getSubscriberID(), SUBSCRIBER_ID);
        assertEquals(transaction.getMSISDN(), MSISDN);
        RechargeSubscriberAuxServiceVisitor.handleVpnAndGroupTransaction(getContext(), subscriber_, vpnAuxiliaryService, transaction);
        assertEquals(transaction.getSubscriberID(), SUBSCRIBER_ID);
        assertEquals(transaction.getMSISDN(), MSISDN);
    }


    /**
     * Test {@link RechargeSubscriberAuxServiceVisitor#handleVpnTransaction} when the VPN
     * leader is invalid.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any info.
     */
    public void testHandleVpnTransactionInvalidLeader() throws HomeException
    {
        final Account account = AccountSupport.getAccount(getContext(), BAN);
        account.setVpnMSISDN(AVAILABLE_MSISDN);
        ((Home) getContext().get(AccountHome.class)).store(getContext(), account);
        final AuxiliaryService vpnAuxiliaryService = AuxiliaryServiceSupport.getAuxiliaryService(getContext(),
            VPN_AUXILIARY_SERVICE_ID);
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
            vpnAuxiliaryService.getCharge());
        assertNotNull(transaction);
        assertEquals(transaction.getBAN(), BAN);
        assertEquals(transaction.getSubscriberID(), SUBSCRIBER_ID);
        assertEquals(transaction.getMSISDN(), MSISDN);
        try
        {
            RechargeSubscriberAuxServiceVisitor.handleVpnAndGroupTransaction(getContext(), subscriber_, vpnAuxiliaryService, transaction);
            fail("Exception should be thrown");
        }
        catch (final InvalidVPNGroupLeaderException exception)
        {
            // expected
        }
    }

    /**
     * Visitor to use.
     */
    private RechargeSubscriberAuxServiceVisitor visitor_;

}
