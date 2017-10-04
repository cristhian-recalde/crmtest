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

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePackageVersionHome;
import com.trilogy.app.crm.bean.ServicePackageVersionTransientHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.product.s2100.ErrorCode;


/**
 * JUnit/XTest test case for {@link RechargeSubscriberServicePackageVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 5-May-08
 */
public class TestRechargeSubscriberServicePackageVisitor extends RechargeItemVisitorTestCase
{

    /**
     * Service package ID.
     */
    public static final int SERVICE_PACKAGE_ID = 7;

    /**
     * Service package version.
     */
    public static final int SERVICE_PACKAGE_VERSION = 2;


    /**
     * Create a new instance of <code>TestRechargeSubscriberServicePackageVisitor</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestRechargeSubscriberServicePackageVisitor(final String name)
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

        final TestSuite suite = new TestSuite(TestRechargeSubscriberServicePackageVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        initServicePackage();
        initServicePackageVersion();
        initServicePackageFee();
        try
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
            getSubscriber(), false, 1, true, true, false);
        } catch (HomeException e)
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                    getSubscriber(), false, BILLING_DATE, BillCycleSupport.getDateOfBillCycleLastDay(CalendarSupportHelper.get(getContext()).findBillingDayOfMonth(BILLING_DATE), 
                            BILLING_DATE), 1, true, true, false);
        }
    }


    /**
     * Initialize service package fee.
     */
    private void initServicePackageFee()
    {
        packageFee_ = new ServicePackageFee();
        packageFee_.setPackageId(SERVICE_PACKAGE_ID);
        packageFee_.setPackageVersionId(SERVICE_PACKAGE_VERSION);
        packageFee_.setMandatory(true);
        packageFee_.setServicePeriod(ServicePeriodEnum.MONTHLY);
        packageFee_.setFee(6500);
        packageFee_.setChecked(true);
        packageFee_.setDispCLTC(false);
    }


    /**
     * Initialize ServicePackage.
     */
    private void initServicePackage()
    {
        final Home home = new ServicePackageTransientHome(getContext());
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setId(SERVICE_PACKAGE_ID);
        servicePackage.setAdjustmentCode(ADJUSTMENT_TYPE_ID);
        servicePackage.setAdjustmentGLCode(GL_CODE);
        servicePackage.setChargingLevel(ChargingLevelEnum.PACKAGE);
        servicePackage.setChargingMode(ServicePeriodEnum.MONTHLY);
        servicePackage.setCurrentVersion(SERVICE_PACKAGE_VERSION);
        servicePackage.setEnabled(true);
        servicePackage.setName("Service Package");
        servicePackage.setSpid(SPID);
        servicePackage.setTaxAuthority(TAX_AUTHORITY_ID);
        servicePackage.setType(SubscriberTypeEnum.HYBRID);

        try
        {
            servicePackage = (ServicePackage) home.create(getContext(), servicePackage);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing ServicePackage");
        }
        getContext().put(ServicePackageHome.class, home);
    }


    /**
     * Initialize ServicePackageVersion.
     */
    private void initServicePackageVersion()
    {
        final Home home = new ServicePackageVersionTransientHome(getContext());
        ServicePackageVersion version = new ServicePackageVersion();
        version.setActivateDate(SUBSCRIBER_START_DATE);
        version.setActivation(SUBSCRIBER_START_DATE);
        version.setCreatedDate(SUBSCRIBER_START_DATE);
        version.setId(SERVICE_PACKAGE_ID);
        version.setVersion(SERVICE_PACKAGE_VERSION);

        try
        {
            version = (ServicePackageVersion) home.create(getContext(), version);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing ServicePackageVersion");
        }
        getContext().put(ServicePackageVersionHome.class, home);
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeable() throws HomeException
    {
        assertTrue(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is charged weekly and monthly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableWeekly() throws HomeException
    {
        packageFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is charged one-time and monthly recurring charge is being
     * generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableOneTime() throws HomeException
    {
        packageFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is charged weekly and weekly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableWeekly() throws HomeException
    {
        packageFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertTrue(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is charged monthly and weekly recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableMonthly() throws HomeException
    {
        packageFee_.setServicePeriod(ServicePeriodEnum.MONTHLY);
        visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is charged one-time and weekly recurring charge is being
     * generated.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testWeeklyIsChargeableOneTime() throws HomeException
    {
        packageFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the package is suspended.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableSuspended() throws HomeException
    {
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED, 
            ServicePackage.class);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test method for
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when all recurring charges of the subscriber should be suspended.
     *
     * @throws HomeException
     *             Thrown by XHome.
     */
    public void testIsChargeableSuspending() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when any charge items should be suspended, but it is a postpaid subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingPostpaid() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        assertTrue(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when any charge items should be suspended for the subscriber, but suspendOnFailure
     * is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSuspendOnFailureDisabled() throws HomeException
    {
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertTrue(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when no charge items should be suspended for the subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSubscriberNotSuspending() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertTrue(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the service package is charged already and prebilling is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedNoPrebilling() throws HomeException
    {
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID, packageFee_
            .getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test
     * {@link RechargeSubscriberServicePackageVisitor#isChargeable(Context, ServicePackageFee)}
     * when the service package is charged already and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID, packageFee_
            .getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), packageFee_));
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * there is no error.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransaction() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        visitor_.handleServiceTransaction(getContext(), packageFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.size() == 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), packageFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * there is no error and prebilling is enabled.
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

        visitor_.handleServiceTransaction(getContext(), packageFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.size() == 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), packageFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
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
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), packageFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), packageFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + packageFee_.getFee(), transaction.getAmount() != packageFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
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
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), packageFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), packageFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + packageFee_.getFee(), transaction.getAmount() != packageFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * the charge is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionWeeklyProrated() throws HomeException
    {
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        packageFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), packageFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), packageFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + packageFee_.getFee(), transaction.getAmount() != packageFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * the charge is weekly and prorated and prebilling enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleServiceTransactionWeeklyProratedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        packageFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberServicePackageVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleServiceTransaction(getContext(), packageFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), packageFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + packageFee_.getFee(), transaction.getAmount() != packageFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * OCG replied an error.
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

        errorCodeMap.put(Integer.valueOf(ErrorCode.BAL_EXPIRED), Integer
            .valueOf(RechargeConstants.RECHARGE_FAIL_ABM_EXPIRED));
        errorCodeMap.put(Integer.valueOf(ErrorCode.BALANCE_INSUFFICIENT), Integer
            .valueOf(RechargeConstants.RECHARGE_FAIL_ABM_LOWBALANCE));
        errorCodeMap.put(Integer.valueOf(ErrorCode.NOT_ENOUGH_BAL), Integer
            .valueOf(RechargeConstants.RECHARGE_FAIL_ABM_LOWBALANCE));
        errorCodeMap.put(Integer.valueOf(ErrorCode.INVALID_ACCT_NUM), Integer
            .valueOf(RechargeConstants.RECHARGE_FAIL_ABM_INVALIDPROFILE));

        final Method handleMethod = RechargeSubscriberServicePackageVisitor.class.getMethod("handleServiceTransaction",
            Context.class, ServicePackageFee.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedPackages", new Class[0]);
        for (final Map.Entry<Integer, Integer> entry : errorCodeMap.entrySet())
        {
            handleTransactionOcgFailureTests(handleMethod, visitor_, packageFee_, SERVICE_PACKAGE_ID,
                ChargedItemTypeEnum.SERVICEPACKAGE, suspendedEntitiesMethod, entry.getKey().intValue(), entry
                    .getValue().intValue(), true);
        }

        for (int i = 0; i < 1005; i++)
        {
            if (!errorCodeMap.containsKey(Integer.valueOf(i)))
            {
                handleTransactionOcgFailureTests(handleMethod, visitor_, packageFee_, SERVICE_PACKAGE_ID,
                    ChargedItemTypeEnum.SERVICEPACKAGE, suspendedEntitiesMethod, i,
                    RechargeConstants.RECHARGE_FAIL_OCG_UNKNOWN, false);
            }
        }
    }


    /**
     * Test {@link RechargeSubscriberServicePackageVisitor#handleServiceTransaction} when
     * a home exception (not related to OCG) is generated.
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
        final Method handleMethod = RechargeSubscriberServicePackageVisitor.class.getMethod("handleServiceTransaction",
            Context.class, ServicePackageFee.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedPackages", new Class[0]);
        final HomeException exception = new HomeException("generated home exception");
        handleTransactionHomeException(handleMethod, visitor_, packageFee_, SERVICE_PACKAGE_ID,
            ChargedItemTypeEnum.SERVICEPACKAGE, suspendedEntitiesMethod, exception,
            RechargeConstants.RECHARGE_FAIL_XHOME, true, false);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        handleTransactionHomeException(handleMethod, visitor_, packageFee_, SERVICE_PACKAGE_ID,
            ChargedItemTypeEnum.SERVICEPACKAGE, suspendedEntitiesMethod, exception,
            RechargeConstants.RECHARGE_FAIL_XHOME, false, false);
    }

    /**
     * Visitor to be tested.
     */
    private RechargeSubscriberServicePackageVisitor visitor_;
    /**
     * Service package fee.
     */
    private ServicePackageFee packageFee_;
}
