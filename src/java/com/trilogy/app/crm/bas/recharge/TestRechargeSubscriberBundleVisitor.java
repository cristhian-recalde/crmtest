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

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.exception.BundleAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.app.crm.unit_test.TestFakeCRMBundleProfile;
import com.trilogy.product.s2100.ErrorCode;


/**
 * JUnit/XTest test case for {@link RechargeSubscriberBundleVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 2-May-08
 */
public class TestRechargeSubscriberBundleVisitor extends RechargeItemVisitorTestCase
{

    /**
     * Bundle ID.
     */
    public static final long BUNDLE_ID = 4;

    /**
     * Auxiliary bundle adjustment type.
     */
    public static final int AUXILIARY_ADJUSTMENT_TYPE = 53212;


    /**
     * Create a new instance of <code>TestRechargeSubscriberBundleVisitor</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestRechargeSubscriberBundleVisitor(final String name)
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

        final TestSuite suite = new TestSuite(TestRechargeSubscriberBundleVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        initBundleFee();
        initBundleProfile();
        initBundleAdjustmentType();
        try
        {
            visitor_ = new RechargeSubscriberBundleVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
            getSubscriber(), false, 1, true, true, false, false);
        } catch (HomeException e)
        {
            visitor_ = new RechargeSubscriberBundleVisitor(BILLING_DATE, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                    getSubscriber(), false, BILLING_DATE, BillCycleSupport.getDateOfBillCycleLastDay(CalendarSupportHelper.get(getContext()).findBillingDayOfMonth(BILLING_DATE), 
                            BILLING_DATE), 1, true, true, false, false);
        }
    }


    /**
     * Initialize bundle adjustment type.
     */
    private void initBundleAdjustmentType()
    {
        final Home home = (Home) getContext().get(AdjustmentTypeHome.class);
        AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setCode(AUXILIARY_ADJUSTMENT_TYPE);
        adjustmentType.setName("AuxBundle");
        adjustmentType.setDesc("Test adjustment type for auxiliary bundle");
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
    }


    /**
     * Initialize BundleFee.
     */
    private void initBundleFee()
    {
        bundleFee_ = new BundleFee();
        bundleFee_.setEndDate(CalendarSupportHelper.get(getContext()).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, BILLING_DATE));
        bundleFee_.setFee(3500);
        bundleFee_.setId(BUNDLE_ID);
        bundleFee_.setServicePeriod(ServicePeriodEnum.MONTHLY);
        bundleFee_.setSource("Bundle");
        bundleFee_.setStartDate(SUBSCRIBER_START_DATE);
    }


    /**
     * Initialize BundleProfile.
     */
    private void initBundleProfile()
    {
    	CRMBundleProfile testService = new TestFakeCRMBundleProfile(getContext());
    	getContext().put(CRMBundleProfile.class, testService);
    	
        BundleProfile api = new BundleProfile();
        api.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        api.setActivationFeeCalculation(ActivationFeeCalculationEnum.PRORATE);
        api.setActivationScheme(ActivationTypeEnum.ACTIVATE_ON_PROVISION);
        api.setAdjustmentTypeDescription("Adjustment type");
        api.setAuxiliary(false);
        api.setAuxiliaryAdjustmentType(AUXILIARY_ADJUSTMENT_TYPE);
        api.setAuxiliaryServiceCharge(4500);
        api.setBundleId(BUNDLE_ID);
        api.setContext(getContext());
        api.setEnabled(true);
        api.setGLCode(GL_CODE);
        api.setGroupChargingScheme(GroupChargingTypeEnum.MEMBER_BUNDLE);
        api.setName("Test Bundle");
        api.setSpid(SPID);
        api.setStartDate(SUBSCRIBER_START_DATE);
        api.setTaxAuthority(TAX_AUTHORITY_ID);

        try
        {
            testService.createBundle(getContext(), api);
        }
        catch (final BundleManagerException exception)
        {
            fail("BundleManagerException caught when initializing BundleProfile");
        }
		catch (BundleAlreadyExistsException e)
		{
			fail("BundleAlreadyExistsException caught when initializing BundleProfile");
		}
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is not part of a service package.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeable() throws HomeException
    {
        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged weekly and is not part of a service package, and weekly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testWeeklyIsChargeableWeekly() throws HomeException
    {
        bundleFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        visitor_ = new RechargeSubscriberBundleVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false, false);
        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged monthly and is not part of a service package, and weekly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testWeeklyIsChargeableMonthly() throws HomeException
    {
        visitor_ = new RechargeSubscriberBundleVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false, false);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged one-time and is not part of a service package, and weekly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testWeeklyIsChargeableOneTime() throws HomeException
    {
        bundleFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        visitor_ = new RechargeSubscriberBundleVisitor(getContext(), BILLING_DATE, AGENT_NAME, ChargingCycleEnum.WEEKLY,
            getSubscriber(), false, 1, true, true, false, false);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged weekly and is not part of a service package, and monthly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableWeekly() throws HomeException
    {
        bundleFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged one-time and is not part of a service package, and monthly
     * recurring charge is being generated.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableOneTime() throws HomeException
    {
        bundleFee_.setServicePeriod(ServicePeriodEnum.ONE_TIME);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is part of a service package.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeablePackage() throws HomeException
    {
        bundleFee_.setSource("Package");
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspended() throws HomeException
    {
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, SubscriberAuxiliaryServiceSupport.SECONDARY_ID_NOT_USED,  BundleFee.class);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * any charge items should be suspended for the subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspending() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * any charge items should be suspended, but it is a postpaid subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingPostpaid() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSuspendingEntities(true);
        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * any charge items should be suspended for the subscriber, but suspendOnFailure is
     * disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSuspendOnFailureDisabled() throws HomeException
    {
        getSubscriber().setSuspendingEntities(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * no charge items should be suspended for the subscriber.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableSuspendingSubscriberNotSuspending() throws HomeException
    {
        visitor_.setSuspendOnFailure(true);
        getSubscriber().setSubscriberType(SubscriberTypeEnum.PREPAID);
        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged already and prebilling is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedNoPrebilling() throws HomeException
    {
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is auxiliary and is charged already and prebilling is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     * @throws InvalidBundleApiException
     *             Thrown if the API is invalid.
     */
    public void testIsChargeableAuxiliaryChargedAuxiliaryNoPrebilling() throws HomeException, InvalidBundleApiException
    {
        final BundleProfile api = BundleSupportHelper.get(getContext()).getBundleProfile(getContext(), BUNDLE_ID);
        assertNotNull("Expected BundleProfile with id=" + BUNDLE_ID + " to have been created", api);
        api.setAuxiliary(true);
        bundleFee_.setSource("Auxiliary");
        
        updateBundle(api);
		
        final Transaction transaction = visitor_.createTransaction(getContext(), AUXILIARY_ADJUSTMENT_TYPE,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


	private void updateBundle(final BundleProfile api)
	{
		try
		{
			((CRMBundleProfile) getContext().get(CRMBundleProfile.class)).updateBundle(getContext(), api);
		}
		catch (BundleDoesNotExistsException e)
		{
			fail("Encountered BundleDoesNotExistException whey trying to updateBundle");
		}
		catch (BundleManagerException e)
		{
			fail("Encountered BundleManagerException whey trying to updateBundle");
		}
	}


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged already as an auxiliary bundle and prebilling is disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableAuxiliaryChargedNoPrebilling() throws HomeException
    {
        final Transaction transaction = visitor_.createTransaction(getContext(), AUXILIARY_ADJUSTMENT_TYPE,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is auxiliary and is charged already as a normal bundle and prebilling is
     * disabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     * @throws InvalidBundleApiException
     *             Thrown if the API is invalid.
     */
    public void testIsChargeableChargedAuxiliaryNoPrebilling() throws HomeException, InvalidBundleApiException
    {
        final BundleProfile api = BundleSupportHelper.get(getContext()).getBundleProfile(getContext(), BUNDLE_ID);
        assertNotNull("Expected BundleProfile with id=" + BUNDLE_ID + " to have been created", api);
        api.setAuxiliary(true);
        bundleFee_.setSource("Auxiliary");
        updateBundle(api);
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged already and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableChargedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID, bundleFee_
            .getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is auxiliary and is charged already and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     * @throws InvalidBundleApiException
     *             Thrown if the API is invalid.
     */
    public void testIsChargeableAuxiliaryChargedAuxiliaryPrebilling() throws HomeException, InvalidBundleApiException
    {
        enablePrebilling();

        final BundleProfile api = BundleSupportHelper.get(getContext()).getBundleProfile(getContext(), BUNDLE_ID);
        assertNotNull("Expected BundleProfile with id=" + BUNDLE_ID + " to have been created", api);
        api.setAuxiliary(true);
        bundleFee_.setSource("Auxiliary");
        updateBundle(api);
        final Transaction transaction = visitor_.createTransaction(getContext(), AUXILIARY_ADJUSTMENT_TYPE,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertFalse(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is charged already as an auxiliary bundle and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     */
    public void testIsChargeableAuxiliaryChargedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Transaction transaction = visitor_.createTransaction(getContext(), AUXILIARY_ADJUSTMENT_TYPE,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#isChargeable(Context, BundleFee)} when
     * the bundle is auxiliary and is charged already as a normal bundle and prebilling is
     * enabled.
     *
     * @throws HomeException
     *             Thrown if there are problems looking up any data.
     * @throws InvalidBundleApiException
     *             Thrown if the API is invalid.
     */
    public void testIsChargeableChargedAuxiliaryPrebilling() throws HomeException, InvalidBundleApiException
    {
        enablePrebilling();
        final BundleProfile api = BundleSupportHelper.get(getContext()).getBundleProfile(getContext(), BUNDLE_ID);
        assertNotNull("Expected BundleProfile with id=" + BUNDLE_ID + " to have been created", api);
        api.setAuxiliary(true);
        bundleFee_.setSource("Auxiliary");
        updateBundle(api);
        final Transaction transaction = visitor_.createTransaction(getContext(), ADJUSTMENT_TYPE_ID,
                bundleFee_.getFee());
        CoreTransactionSupportHelper.get(getContext()).createTransaction(getContext(), transaction, true);

        assertTrue(visitor_.isChargeable(getContext(), bundleFee_));
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when there is
     * no error.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransaction() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        visitor_.handleBundleTransaction(getContext(), bundleFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.size() == 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), bundleFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when there is
     * no error and prebilling is enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransactionPrebilling() throws HomeException
    {
        enablePrebilling();
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        visitor_.handleBundleTransaction(getContext(), bundleFee_);

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.size() == 1);
        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(transaction.getAmount(), bundleFee_.getFee());
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when there is
     * no error and the charge is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransactionProrated() throws HomeException
    {
        final Date monthBefore = CalendarSupportHelper.get(getContext()).getDayAfter(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, BILLING_DATE));
        final Date monthAfter = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, BILLING_DATE);
        Date currentDate = monthBefore;
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberBundleVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleBundleTransaction(getContext(), bundleFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), bundleFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + bundleFee_.getFee(), transaction.getAmount() != bundleFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when there is
     * no error and the charge is prorated and prebilling enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransactionProratedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Date monthBefore = CalendarSupportHelper.get(getContext()).getDayAfter(CalendarSupportHelper.get(getContext()).findDateMonthsAfter(-1, BILLING_DATE));
        final Date monthAfter = CalendarSupportHelper.get(getContext()).findDateMonthsAfter(1, BILLING_DATE);
        Date currentDate = monthBefore;
        while (currentDate.before(monthAfter))
        {
            visitor_ = new RechargeSubscriberBundleVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.MONTHLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleBundleTransaction(getContext(), bundleFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), bundleFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + bundleFee_.getFee(), transaction.getAmount() != bundleFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when the
     * charge is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransactionWeeklyProrated() throws HomeException
    {
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        bundleFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberBundleVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleBundleTransaction(getContext(), bundleFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), bundleFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + bundleFee_.getFee(), transaction.getAmount() != bundleFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when the
     * charge is weekly and prorated and prebilling enabled.
     *
     * @throws HomeException
     *             Thrown if there are problem running the method.
     */
    public void testhandleBundleTransactionWeeklyProratedPrebilling() throws HomeException
    {
        enablePrebilling();
        final Date weekBefore = CalendarSupportHelper.get(getContext()).findDateDaysBefore(6, BILLING_DATE);
        final Date weekAfter = CalendarSupportHelper.get(getContext()).findDateDaysAfter(6, BILLING_DATE);
        bundleFee_.setServicePeriod(ServicePeriodEnum.WEEKLY);
        Date currentDate = weekBefore;
        while (currentDate.before(weekAfter))
        {
            visitor_ = new RechargeSubscriberBundleVisitor(getContext(), currentDate, AGENT_NAME, ChargingCycleEnum.WEEKLY,
                getSubscriber(), false, 1, true, true, false, false);
            final StringBuilder sb = new StringBuilder();
            sb.append("Current date = ");
            sb.append(currentDate);
            sb.append(", billing Date = ");
            sb.append(BILLING_DATE);

            Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.isEmpty());

            visitor_.handleBundleTransaction(getContext(), bundleFee_);

            transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
            assertNotNull(sb.toString(), transactions);
            assertTrue(sb.toString(), transactions.size() == 1);
            final Transaction transaction = (Transaction) transactions.iterator().next();
            if (currentDate.equals(BILLING_DATE))
            {
                assertEquals(sb.toString(), transaction.getAmount(), bundleFee_.getFee());
            }
            else
            {
                assertTrue(sb.toString() + ", transaction amount = " + transaction.getAmount() + ", service fee = "
                    + bundleFee_.getFee(), transaction.getAmount() != bundleFee_.getFee());
            }
            initTransaction();
            currentDate = CalendarSupportHelper.get(getContext()).getDayAfter(currentDate);
        }
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when OCG
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
    public void testHandleBundleTransactionOcgError() throws HomeException, IllegalAccessException,
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

        final Method handleMethod = RechargeSubscriberBundleVisitor.class.getMethod("handleBundleTransaction",
            Context.class, BundleFee.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedBundles", new Class[0]);
        for (final Map.Entry<Integer, Integer> entry : errorCodeMap.entrySet())
        {
            handleTransactionOcgFailureTests(handleMethod, visitor_, bundleFee_, BUNDLE_ID, ChargedItemTypeEnum.BUNDLE,
                suspendedEntitiesMethod, entry.getKey().intValue(), entry.getValue().intValue(), true);
        }

        for (int i = 0; i < 1005; i++)
        {
            if (!errorCodeMap.containsKey(Integer.valueOf(i)))
            {
                handleTransactionOcgFailureTests(handleMethod, visitor_, bundleFee_, BUNDLE_ID,
                    ChargedItemTypeEnum.BUNDLE, suspendedEntitiesMethod, i,
                    RechargeConstants.RECHARGE_FAIL_OCG_UNKNOWN, false);
            }
        }
    }


    /**
     * Test {@link RechargeSubscriberBundleVisitor#handleBundleTransaction} when a home
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
    public void testHandleBundleTransactionHomeException() throws HomeException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException
    {
        final Method handleMethod = RechargeSubscriberBundleVisitor.class.getMethod("handleBundleTransaction",
            Context.class, BundleFee.class);
        final Method suspendedEntitiesMethod = Subscriber.class.getMethod("getSuspendedBundles", new Class[0]);
        final HomeException exception = new HomeException("generated home exception");
        handleTransactionHomeException(handleMethod, visitor_, bundleFee_, BUNDLE_ID, ChargedItemTypeEnum.BUNDLE,
            suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME, true, false);
        initTransaction();
        initRecurringChargeErrorReport();
        initSuspendedEntities();
        getSubscriber().setSuspendingEntities(false);
        handleTransactionHomeException(handleMethod, visitor_, bundleFee_, BUNDLE_ID, ChargedItemTypeEnum.BUNDLE,
            suspendedEntitiesMethod, exception, RechargeConstants.RECHARGE_FAIL_XHOME, false, false);
    }

    /**
     * Visitor to be tested.
     */
    private RechargeSubscriberBundleVisitor visitor_;

    /**
     * Bundle fee to be tested.
     */
    private BundleFee bundleFee_;
}
