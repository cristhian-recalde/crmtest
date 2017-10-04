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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.product.s2100.ErrorCode;


/**
 * JUnit/XTest test case for {@link RechargeSubscriberServiceVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 5-May-08
 */
public class TestRechargeSubscriberServiceVisitor extends RechargeItemVisitorTestCase
{

    /**
     * Service ID.
     */
    public static final int SERVICE_ID = 5;


    /**
     * Create a new instance of <code>TestRechargeSubscriberServiceVisitor</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestRechargeSubscriberServiceVisitor(final String name)
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

        final TestSuite suite = new TestSuite(TestRechargeSubscriberServiceVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     * @throws HomeException 
     * @throws IllegalStateException 
     */
    @Override
    protected void setUp() 
    {
        super.setUp();
        initService();
        initServiceFee2();
        initSubscriberServices();
        initServicesPackages();
        getSubscriber().setSuspendedServices(new HashMap());
        try
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
            getSubscriber(), false, 1, true, true, false);
        } catch (HomeException e)
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(),BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                    getSubscriber(), false, BILLING_DATE, BillCycleSupport.getDateOfBillCycleLastDay(CalendarSupportHelper.get(getContext()).findBillingDayOfMonth(BILLING_DATE), BILLING_DATE), 1, true, true, false, false, false);
        }
    }


    /**
     * Initialize SubscriberServices.
     */
    private void initSubscriberServices()
    {
        final Home home = new SubscriberServicesTransientHome(getContext());
        SubscriberServices service = new SubscriberServices();
        service.setMandatory(true);
        service.setServiceId(SERVICE_ID);
        service.setProvisionedState(ServiceStateEnum.PROVISIONED);
        service.setSubscriberId(SUBSCRIBER_ID);

        try
        {
            service = (SubscriberServices) home.create(getContext(), service);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing SubscriberService");
        }
        getContext().put(SubscriberServicesHome.class, home);
    }


    /**
     * Initialize SubscriberServices.
     */
    private void initServicesPackages()
    {
        final Home home = new ServicePackageTransientHome(getContext());
        getContext().put(ServicePackageHome.class, home);
    }

    /**
     * Initialize service package fee.
     */
    private void initServiceFee2()
    {
        serviceFee_ = new ServiceFee2();
        serviceFee_.setServiceId(SERVICE_ID);
        serviceFee_.setServicePreference(ServicePreferenceEnum.MANDATORY);
        serviceFee_.setServicePeriod(ServicePeriodEnum.MONTHLY);
        serviceFee_.setFee(6500);
        serviceFee_.setChecked(true);
        serviceFee_.setDispCLTC(false);
    }


    /**
     * Initialize Service.
     */
    private void initService()
    {
        final Home home = new ServiceTransientHome(getContext());
        Service service = new Service();
        service.setIdentifier(SERVICE_ID);
        service.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        service.setAdjustmentGLCode(GL_CODE);
        service.setChargeScheme(ServicePeriodEnum.MONTHLY);
        service.setName("Test Service");
        service.setSpid(SPID);
        service.setTaxAuthority(TAX_AUTHORITY_ID);
        service.setType(ServiceTypeEnum.GENERIC);

        try
        {
            service = (Service) home.create(getContext(), service);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Service");
        }
        getContext().put(ServiceHome.class, home);
    }


    /**
     * Add the service to the subscriber.
     */
    private void subscribeService()
    {
        final Set<Long> services = new HashSet<Long>();
        services.add(Long.valueOf(SERVICE_ID));
        getSubscriber().setServices(services);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeable() throws HomeException
    {
        subscribeService();
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), true);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is charged weekly and monthly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableWeekly() throws HomeException
    {
        subscribeService();
        serviceFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is charged one-time and monthly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableOneTime() throws HomeException
    {
        subscribeService();
        serviceFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is charged weekly and weekly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableWeekly() throws HomeException
    {
        subscribeService();
        serviceFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        visitor_ = new RechargeSubscriberServiceVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), true);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is charged monthly and weekly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableMonthly() throws HomeException
    {
        subscribeService();
        visitor_ = new RechargeSubscriberServiceVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is charged one-time and weekly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableOneTime() throws HomeException
    {
        subscribeService();
        serviceFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        visitor_ = new RechargeSubscriberServiceVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the service is part of a package.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableInPackage() throws HomeException
    {
        subscribeService();
        serviceFee_.setSource("Package");
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the subscriber does not subscribe to the service.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableNotSubscribed() throws HomeException {

		// set a service ID not provisioned to the subscriber
		serviceFee_.setServiceId(SERVICE_ID + 1);
		// execute the isChargeable test case.
		assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
	}


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * the package is suspended.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableSuspended() throws HomeException
    {
        subscribeService();
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
                ServiceFee2.class);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)} when
     * all recurring charges of the subscriber should be suspended.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableSuspending() throws HomeException
    {
        subscribeService();
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}
     * when any charge items should be suspended, but it is a postpaid subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingPostpaid() throws HomeException
    {
        subscribeService();
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), true);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}
     * when any charge items should be suspended for the subscriber, but suspendOnFailure
     * is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSuspendOnFailureDisabled() throws HomeException
    {
        subscribeService();
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), true);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}
     * when no charge items should be suspended for the subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSubscriberNotSuspending() throws HomeException
    {
        subscribeService();
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), true);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}
     * when the service package is charged already and prebilling is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedNoPrebilling() throws HomeException
    {
        subscribeService();
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                serviceFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#isChargeable(Context, ServiceFee2)}
     * when the service package is charged already and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedPrebilling() throws HomeException
    {
        subscribeService();
        enablePrebilling();
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                serviceFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertEquals(visitor_.isChargeable(getContext(), serviceFee_), false);
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when there
     * is no error.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransaction() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        visitor_.handleServiceTransaction(getContext(), serviceFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(transactions.size(), 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), serviceFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when there
     * is no error and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionPrebilling() throws HomeException
    {
        enablePrebilling();
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        visitor_.handleServiceTransaction(getContext(), serviceFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(transactions.size(), 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), serviceFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when there
     * is no error and the charge is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionProrated() throws HomeException
    {
        final Date monthBefore = CalendarSupportHelper.get(getContext()).getDayAfter(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, BILLING_DATE));
        final Date monthAfter = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, BILLING_DATE);
        Date currentDate = monthBefore;
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), serviceFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), serviceFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + serviceFee_.getFee(), transaction.getAmount() != serviceFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when there
     * is no error and the charge is prorated and prebilling enabled.
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
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), serviceFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), serviceFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + serviceFee_.getFee(), transaction.getAmount() != serviceFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when the
     * charge is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionWeeklyProrated() throws HomeException
    {
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        serviceFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), serviceFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), serviceFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + serviceFee_.getFee(), transaction.getAmount() != serviceFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when the
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
        serviceFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberServiceVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), serviceFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), serviceFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + serviceFee_.getFee(), transaction.getAmount() != serviceFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when OCG
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

        final Method handleMethod = RechargeSubscriberServiceVisitor.class.getMethod("handleServiceTransaction",
            Context.class, ServiceFee2.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedServices", new Class[0]);
        for (final Map.Entry<Integer, Integer> entry : errorCodeMap.entrySet())
        {
            handleTransactionOcgFailureTests(handleMethod, visitor_, serviceFee_, SERVICE_ID,
                ChargedItemTypeEnum.SERVICE, suspendedEntitiesMethod, entry.getKey().intValue(),
                    entry.getValue().intValue(), true);
        }

        for (int i = 0; i < 1005; i++)
        {
            if (!errorCodeMap.containsKey(Integer.valueOf(i)))
            {
                handleTransactionOcgFailureTests(handleMethod, visitor_, serviceFee_, SERVICE_ID,
                    ChargedItemTypeEnum.SERVICE, suspendedEntitiesMethod, i,
                    RechargeConstants.RECHARGE_FAIL_OCG_UNKNOWN, false);
            }
        }
    }


    /**
     * Test {@link RechargeSubscriberServiceVisitor#handleServiceTransaction} when a home
     * exception (not related to OCG) is generated.
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
        final Method handleMethod = RechargeSubscriberServiceVisitor.class.getMethod("handleServiceTransaction",
            Context.class, ServiceFee2.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedServices", new Class[0]);
        final HomeException exception = new HomeException("generated home exception");
        handleTransactionHomeException(handleMethod, visitor_, serviceFee_, SERVICE_ID, ChargedItemTypeEnum.SERVICE,
            suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME, true, false);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        getSubscriber().setSuspendedAuxServices(new HashMap());
        handleTransactionHomeException(handleMethod, visitor_, serviceFee_, SERVICE_ID, ChargedItemTypeEnum.SERVICE,
            suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME, false, false);
    }

    /**
     * Visitor to be tested.
     */
    private RechargeSubscriberServiceVisitor visitor_;

    /**
     * Service.
     */
    private ServiceFee2 serviceFee_;
}
