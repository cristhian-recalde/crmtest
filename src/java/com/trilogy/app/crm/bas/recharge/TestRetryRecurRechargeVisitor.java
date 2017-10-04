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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceStateEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargingLevelEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackage;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePackageVersionHome;
import com.trilogy.app.crm.bean.ServicePackageVersionTransientHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePackageSupportHelper;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Unit test for {@link RetryRecurRechargeVisitor}.
 *
 * @author cindy.wong@redknee.com
 * @since 9-May-08
 */
public class TestRetryRecurRechargeVisitor extends RechargeItemVisitorTestCase
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
     * Service package fee.
     */
    public static final long SERVICE_PACKAGE_FEE = 3500;

    /**
     * Bundle ID.
     */
    public static final long BUNDLE_ID = 4;

    /**
     * Auxiliary bundle adjustment type.
     */
    public static final int AUXILIARY_ADJUSTMENT_TYPE = 53212;

    /**
     * Bundle fee.
     */
    public static final long BUNDLE_FEE = 4000;

    /**
     * Auxiliary bundle fee.
     */
    public static final long AUXILIARY_BUNDLE_FEE = 4200;

    /**
     * Service ID.
     */
    public static final long SERVICE_ID = 5;

    /**
     * Service fee.
     */
    public static final long SERVICE_FEE = 3000;

    /**
     * Auxiliary service ID.
     */
    public static final long AUXILIARY_SERVICE_ID = 3;

    /**
     * AUxiliary service fee.
     */
    public static final long AUXILIARY_SERVICE_FEE = 5000;


    /**
     * Create a new instance of <code>TestRetryRecurRechargeVisitor</code>.
     *
     * @param name
     *            Name of the test.
     */
    public TestRetryRecurRechargeVisitor(final String name)
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

        final TestSuite suite = new TestSuite(TestRetryRecurRechargeVisitor.class);

        return suite;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();
        initBundleAdjustmentType();
        initServicePackageServicePeriod(ServicePeriodEnum.MONTHLY, true, false);
        initServiceServicePeriod(ServicePeriodEnum.MONTHLY, false);
        initBundleServicePeriod(ServicePeriodEnum.MONTHLY, false);
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.MONTHLY);
    }


    /**
     * Initialize service period for auxiliary services.
     *
     * @param chargingModeType
     *            Charging mode type.
     */
    private void initAuxiliaryServiceServicePeriod(final ServicePeriodEnum chargingModeType)
    {
        initAuxiliaryService(chargingModeType);
        initSubscriberAuxiliaryService();
    }


    /**
     * Initialize AuxiliaryService.
     *
     * @param chargingModeType
     *            Charging mode type.
     */
    private void initAuxiliaryService(final ServicePeriodEnum chargingModeType)
    {
        final Home home = new AuxiliaryServiceTransientHome(getContext());
        AuxiliaryService service = new AuxiliaryService();
        service.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        service.setAdjustmentTypeDescription("Auxiliary service adjustment");
        service.setCharge(AUXILIARY_SERVICE_FEE);
        service.setChargingModeType(chargingModeType);
        service.setEndDate(CalendarSupportHelper.get(getContext()).findDateYearsAfter(1, BILLING_DATE));
        service.setGLCode(GL_CODE);
        service.setIdentifier(AUXILIARY_SERVICE_ID);
        service.setName("Auxiliary service");
        service.setSpid(SPID);
        service.setStartDate(SUBSCRIBER_START_DATE);
        service.setState(AuxiliaryServiceStateEnum.ACTIVE);
        service.setSubscriberType(SubscriberTypeEnum.HYBRID);
        service.setTaxAuthority(TAX_AUTHORITY_ID);
        service.setType(AuxiliaryServiceTypeEnum.Basic);

        try
        {
            service = (AuxiliaryService) home.create(getContext(), service);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing AuxiliaryService");
        }
        getContext().put(AuxiliaryServiceHome.class, home);
    }


    /**
     * Initialize SubscriberAuxiliaryService.
     */
    private void initSubscriberAuxiliaryService()
    {
        final Home home = new SubscriberAuxiliaryServiceTransientHome(getContext());
        association_ = new SubscriberAuxiliaryService();
        association_.setAuxiliaryServiceIdentifier(AUXILIARY_SERVICE_ID);
        association_.setContext(getContext());
        association_.setCreated(SUBSCRIBER_START_DATE);
        association_.setEndDate(CalendarSupportHelper.get(getContext()).findDateYearsAfter(1, BILLING_DATE));
        association_.setIdentifier(AUXILIARY_SERVICE_ID);
        association_.setProvisioned(true);
        association_.setStartDate(SUBSCRIBER_START_DATE);
        association_.setSubscriberIdentifier(SUBSCRIBER_ID);
        association_.setType(AuxiliaryServiceTypeEnum.Basic);

        try
        {
            association_ = (SubscriberAuxiliaryService) home.create(getContext(), association_);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing SubscriberAuxiliaryService");
        }
        getContext().put(SubscriberAuxiliaryServiceHome.class, home);
    }


    /**
     * Initialize service to service period.
     *
     * @param servicePeriod
     *            Service period.
     * @param addBundles
     *            Whether bundles are added to the service period.
     */
    private void initServiceServicePeriod(final ServicePeriodEnum servicePeriod, final boolean addBundles)
    {
        initSubscriberServices();
        initService();
        initServiceFee2(servicePeriod);
        subscribeService();
        initServicePackageServicePeriod(servicePeriod, addBundles, true);
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
     * Initialize service package fee.
     *
     * @param servicePeriod
     *            Service period.
     */
    private void initServiceFee2(final ServicePeriodEnum servicePeriod)
    {
        serviceFee_ = new ServiceFee2();
        serviceFee_.setServiceId(SERVICE_ID);
        serviceFee_.setServicePreference(ServicePreferenceEnum.MANDATORY);
        serviceFee_.setServicePeriod(servicePeriod);
        serviceFee_.setFee(SERVICE_FEE);
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
     * Initialize bundle to the provided service period.
     *
     * @param servicePeriod
     *            Service period.
     * @param isAuxiliary
     *            Whether the bundle should be auxiliary or normal.
     */
    private void initBundleServicePeriod(final ServicePeriodEnum servicePeriod, final boolean isAuxiliary)
    {
        initBundleFee(servicePeriod, isAuxiliary);
        initBundleProfile(isAuxiliary);
        initServicePackageServicePeriod(servicePeriod, !isAuxiliary, false);

        final Map<Long, BundleFee> bundles = new HashMap<Long, BundleFee>();
        bundles.put(Long.valueOf(BUNDLE_ID), bundleFee_);
        getSubscriber().setBundles(bundles);
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
     *
     * @param servicePeriod
     *            Service period to charge.
     * @param isAuxiliary
     *            Whether the bundle is auxiliary.
     */
    private void initBundleFee(final ServicePeriodEnum servicePeriod, final boolean isAuxiliary)
    {
        bundleFee_ = new BundleFee();
        bundleFee_.setEndDate(CalendarSupportHelper.get(getContext()).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, BILLING_DATE));
        bundleFee_.setId(BUNDLE_ID);
        bundleFee_.setServicePeriod(servicePeriod);
        if (isAuxiliary)
        {
            bundleFee_.setSource(BundleFee.AUXILIARY);
            bundleFee_.setFee(0);
        }
        else
        {
            bundleFee_.setSource("Bundle");
            bundleFee_.setFee(BUNDLE_FEE);
        }
        bundleFee_.setStartDate(SUBSCRIBER_START_DATE);
    }


    /**
     * Initialize BundleProfileApi.
     *
     * @param isAuxiliary
     *            Whether the bundle is auxiliary.
     */
    private void initBundleProfile(final boolean isAuxiliary)
    {
        final Home home = new BundleProfileTransientHome(getContext());
        BundleProfile api = new BundleProfile();
        api.setAdjustmentType(ADJUSTMENT_TYPE_ID);
        api.setActivationFeeCalculation(ActivationFeeCalculationEnum.PRORATE);
        api.setActivationScheme(ActivationTypeEnum.ACTIVATE_ON_PROVISION);
        api.setAdjustmentTypeDescription("Adjustment type");
        api.setAuxiliary(isAuxiliary);
        api.setAuxiliaryAdjustmentType(AUXILIARY_ADJUSTMENT_TYPE);
        api.setAuxiliaryServiceCharge(AUXILIARY_BUNDLE_FEE);
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
            api = (BundleProfile) home.create(getContext(), api);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing BundleProfile");
        }
        getContext().put(BundleProfileHome.class, home);
    }


    /**
     * Initialize service package fee.
     *
     * @param servicePeriod
     *            Service period.
     */
    private void initServicePackageFee(final ServicePeriodEnum servicePeriod)
    {
        packageFee_ = new ServicePackageFee();
        packageFee_.setPackageId(SERVICE_PACKAGE_ID);
        packageFee_.setPackageVersionId(SERVICE_PACKAGE_VERSION);
        packageFee_.setMandatory(true);
        packageFee_.setServicePeriod(servicePeriod);
        packageFee_.setFee(SERVICE_PACKAGE_FEE);
        packageFee_.setChecked(true);
        packageFee_.setDispCLTC(false);
    }


    /**
     * Initialize ServicePackage.
     *
     * @param servicePeriod
     *            Service period to charge.
     */
    private void initServicePackage(final ServicePeriodEnum servicePeriod)
    {
        final Home home = new ServicePackageTransientHome(getContext());
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setId(SERVICE_PACKAGE_ID);
        servicePackage.setAdjustmentCode(ADJUSTMENT_TYPE_ID);
        servicePackage.setAdjustmentGLCode(GL_CODE);
        servicePackage.setChargingLevel(ChargingLevelEnum.PACKAGE);
        servicePackage.setChargingMode(servicePeriod);
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
     *
     * @param addBundle
     *            Whether to add bundle to the service package version.
     * @param addService
     *            Whether to add service to the service package version.
     */
    private void initServicePackageVersion(final boolean addBundle, final boolean addService)
    {
        final Home home = new ServicePackageVersionTransientHome(getContext());
        ServicePackageVersion version = new ServicePackageVersion();
        version.setActivateDate(SUBSCRIBER_START_DATE);
        version.setActivation(SUBSCRIBER_START_DATE);
        version.setCreatedDate(SUBSCRIBER_START_DATE);
        version.setId(SERVICE_PACKAGE_ID);
        version.setVersion(SERVICE_PACKAGE_VERSION);

        if (addBundle)
        {
            final Map<Long, BundleFee> bundleFees = new HashMap<Long, BundleFee>();
            bundleFees.put(Long.valueOf(BUNDLE_ID), bundleFee_);
            version.setBundleFees(bundleFees);
        }
        else
        {
            version.setBundleFees(new HashMap());
        }

        if (addService)
        {
            final Map<Long, ServiceFee2> serviceFees = new HashMap<Long, ServiceFee2>();
            serviceFees.put(Long.valueOf(SERVICE_ID), serviceFee_);
            version.setServiceFees(serviceFees);

            version.setPackageFees(new HashMap());
        }
        else
        {
            final Map<Integer, ServicePackageFee> packageFees = new HashMap<Integer, ServicePackageFee>();
            packageFees.put(Integer.valueOf(SERVICE_PACKAGE_ID), packageFee_);
            version.setPackageFees(packageFees);

            version.setServiceFees(new HashMap());
        }

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
     * Initialize service package to the provided service period.
     *
     * @param servicePeriod
     *            Service period.
     */
    private void initServicePackageServicePeriod(final ServicePeriodEnum servicePeriod)
    {
        initServicePackage(servicePeriod);
        initServicePackageFee(servicePeriod);
        initServicePackageVersion(false, false);
        try
        {
            final PricePlanVersion version = PricePlanSupport.getVersion(getContext(), PRICE_PLAN_ID,
                PRICE_PLAN_VERSION);
            version.setServicePackageVersion(ServicePackageSupportHelper.get(getContext()).getServicePackageVersion(getContext(),
                SERVICE_PACKAGE_ID, SERVICE_PACKAGE_VERSION));
            ((Home) getContext().get(PricePlanVersionHome.class)).store(getContext(), version);
        }
        catch (final HomeException exception)
        {
            fail("Cannot init service package for price plan");
        }
    }


    /**
     * Initialize service package to the provided service period.
     *
     * @param servicePeriod
     *            Service period.
     * @param addBundle
     *            Whether to add the bundle to the service package version.
     * @param addService
     *            Whether to add the service to the service package version.
     */
    private void initServicePackageServicePeriod(final ServicePeriodEnum servicePeriod, final boolean addBundle,
        final boolean addService)
    {
        initServicePackage(servicePeriod);
        initServicePackageFee(servicePeriod);
        initServicePackageVersion(addBundle, addService);
        try
        {
            final PricePlanVersion version = PricePlanSupport.getVersion(getContext(), PRICE_PLAN_ID,
                PRICE_PLAN_VERSION);
            version.setServicePackageVersion(ServicePackageSupportHelper.get(getContext()).getServicePackageVersion(getContext(),
                SERVICE_PACKAGE_ID, SERVICE_PACKAGE_VERSION));
            ((Home) getContext().get(PricePlanVersionHome.class)).store(getContext(), version);
        }
        catch (final HomeException exception)
        {
            fail("Cannot init service package for price plan");
        }
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages}.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackages() throws HomeException
    {
        // suspend a package
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, -1,
            ServicePackage.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedPackages = visitor_.rechargePackages(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertEquals(1, suspendedPackages.size());
        assertEquals(SERVICE_PACKAGE_ID, ((Number) suspendedPackages.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(SERVICE_PACKAGE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages} when the charge is
     * weekly.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackagesWeekly() throws HomeException
    {
        initServicePackageServicePeriod(ServicePeriodEnum.WEEKLY);

        // suspend a package
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, -1,
            ServicePackage.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedPackages = visitor_.rechargePackages(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertEquals(1, suspendedPackages.size());
        assertEquals(SERVICE_PACKAGE_ID, ((Number) suspendedPackages.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(SERVICE_PACKAGE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages} when there are no
     * packages suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackagesNoSuspendedPackages() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedPackages = visitor_.rechargePackages(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertTrue(suspendedPackages.isEmpty());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages} when the charge
     * fails.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackagesNoCharge() throws HomeException
    {
        // suspend a package
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, -1,
            ServicePackage.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), new HomeException("Exception thrown"));

        final Map suspendedPackages = visitor_.rechargePackages(subContext, BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertTrue(suspendedPackages.isEmpty());

        transactions = ((Home) subContext.get(TransactionHome.class)).selectAll(subContext);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages} when the charge is
     * prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackagesProrated() throws HomeException
    {
        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);
        // suspend a package
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, -1,
            ServicePackage.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedPackages = visitor_.rechargePackages(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertEquals(1, suspendedPackages.size());
        assertEquals(SERVICE_PACKAGE_ID, ((Number) suspendedPackages.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertTrue("Charged fee = " + transaction.getAmount(), SERVICE_PACKAGE_FEE > transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargePackages} when the charge is
     * weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargePackagesWeeklyProrated() throws HomeException
    {
        initServicePackageServicePeriod(ServicePeriodEnum.WEEKLY);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);

        // suspend a package
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_PACKAGE_ID, -1,
            ServicePackage.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedPackages = visitor_.rechargePackages(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedPackages);
        assertEquals(1, suspendedPackages.size());
        assertEquals(SERVICE_PACKAGE_ID, ((Number) suspendedPackages.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals((long) (SERVICE_PACKAGE_FEE / 7.0 * 6.0), transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles}.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundles() throws HomeException
    {
        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(BUNDLE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the charge is
     * weekly.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesWeekly() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.WEEKLY, false);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(BUNDLE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when there are no
     * packages suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesNoSuspendedBundles() throws HomeException
    {
        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargePackages(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedPackages().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE);
        assertNotNull(suspendedBundles);
        assertTrue(suspendedBundles.isEmpty());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the charge
     * fails.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesNoCharge() throws HomeException
    {
        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), new HomeException("Exception thrown"));

        final Map suspendedBundles = visitor_.rechargeBundles(subContext, BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertTrue(suspendedBundles.isEmpty());

        transactions = ((Home) subContext.get(TransactionHome.class)).selectAll(subContext);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the charge is
     * prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesProrated() throws HomeException
    {
        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);
        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertTrue("Charged fee = " + transaction.getAmount(), BUNDLE_FEE > transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the charge is
     * weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesWeeklyProrated() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.WEEKLY, false);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1,false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(Math.round(BUNDLE_FEE / 7.0 * 6.0), transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles}.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliary() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.MONTHLY, true);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(AUXILIARY_BUNDLE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the bundle is
     * auxiliary and the charge is weekly.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliaryWeekly() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.WEEKLY, true);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(AUXILIARY_BUNDLE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the bundle is
     * auxiliary and there are no bundles suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliaryNoSuspendedBundles() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.MONTHLY, true);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1,false);
        assertNotNull(suspendedBundles);
        assertTrue(suspendedBundles.isEmpty());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the bundle is
     * auxiliary and the charge fails.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliaryNoCharge() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.MONTHLY, true);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), new HomeException("Exception thrown"));

        final Map suspendedBundles = visitor_.rechargeBundles(subContext, BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1,false);
        assertNotNull(suspendedBundles);
        assertTrue(suspendedBundles.isEmpty());

        transactions = ((Home) subContext.get(TransactionHome.class)).selectAll(subContext);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the bundle is
     * auxiliary and the charge is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliaryProrated() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.MONTHLY, true);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);
        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1,false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertTrue("Charged fee = " + transaction.getAmount(), AUXILIARY_BUNDLE_FEE > transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeBundles} when the bundle is
     * auxiliary and the charge is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeBundlesAuxiliaryWeeklyProrated() throws HomeException
    {
        initBundleServicePeriod(ServicePeriodEnum.WEEKLY, true);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);

        // suspend a bundle
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, BUNDLE_ID, -1, BundleFee.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedBundles = visitor_.rechargeBundles(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedBundles().values(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1,false);
        assertNotNull(suspendedBundles);
        assertEquals(1, suspendedBundles.size());
        assertEquals(BUNDLE_ID, ((Number) suspendedBundles.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(Math.round(AUXILIARY_BUNDLE_FEE / 7.0 * 6.0), transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices}.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServices() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.MONTHLY, true);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, -1, ServiceFee2.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(SERVICE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices} when the charge is
     * weekly.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServicesWeekly() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.WEEKLY, true);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, -1, ServiceFee2.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(SERVICE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices} when there are no
     * services suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServicesNoSuspendedServices() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.MONTHLY, true);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertTrue(suspendedServices.isEmpty());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices} when the charge
     * fails.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServicesNoCharge() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.MONTHLY, true);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, -1, ServiceFee2.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), new HomeException("Exception thrown"));

        final Map suspendedServices = visitor_.rechargeServices(subContext, BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertTrue(suspendedServices.isEmpty());

        transactions = ((Home) subContext.get(TransactionHome.class)).selectAll(subContext);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices} when the charge is
     * prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServicesProrated() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.MONTHLY, true);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);
        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, -1, ServiceFee2.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeServices(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedServices(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertTrue("Charged fee = " + transaction.getAmount(), SERVICE_FEE > transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeServices} when the charge is
     * weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeServicesWeeklyProrated() throws HomeException
    {
        initServiceServicePeriod(ServicePeriodEnum.WEEKLY, true);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, SERVICE_ID, -1, ServiceFee2.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeServices(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedServices(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1, false, false);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(Math.round(SERVICE_FEE / 7.0 * 6.0), transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices}.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServices() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.MONTHLY);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeAuxServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(AUXILIARY_SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(AUXILIARY_SERVICE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices} when charge is
     * weekly.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServicesWeekly() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.WEEKLY);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeAuxServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(AUXILIARY_SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(AUXILIARY_SERVICE_FEE, transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices} when there are
     * no services suspended.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServicesNoSuspendedServices() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.MONTHLY);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeAuxServices(getContext(), BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertTrue(suspendedServices.isEmpty());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices} when the charge
     * fails.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServicesNoCharge() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.MONTHLY);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Context subContext = addExceptionWrapper(getContext(), new HomeException("Exception thrown"));

        final Map suspendedServices = visitor_.rechargeAuxServices(subContext, BILLING_DATE, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertTrue(suspendedServices.isEmpty());

        transactions = ((Home) subContext.get(TransactionHome.class)).selectAll(subContext);
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices} when the charge
     * is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServicesProrated() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.MONTHLY);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);
        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeAuxServices(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(AUXILIARY_SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertTrue("Charged fee = " + transaction.getAmount(), AUXILIARY_SERVICE_FEE > transaction.getAmount());
    }


    /**
     * Test case for {@link RetryRecurRechargeVisitor#rechargeAuxServices} when the charge
     * is weekly and is prorated.
     *
     * @throws HomeException
     *             Thrown if there are problems executing the test.
     */
    public void testRechargeAuxiliaryServicesWeeklyProrated() throws HomeException
    {
        initAuxiliaryServiceServicePeriod(ServicePeriodEnum.WEEKLY);

        final Date billingDate = CalendarSupportHelper.get(getContext()).getDayAfter(BILLING_DATE);

        // suspend a service
        SuspendedEntitySupport.createSuspendedEntity(getContext(), SUBSCRIBER_ID, AUXILIARY_SERVICE_ID, -1,
            AuxiliaryService.class);

        Collection transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());

        final Map suspendedServices = visitor_.rechargeAuxServices(getContext(), billingDate, getSubscriber(),
            getSubscriber().getSuspendedAuxServices(), 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 0.5, BILLING_DATE, BILLING_DATE, 1, 0.0, null, null, 1);
        assertNotNull(suspendedServices);
        assertEquals(1, suspendedServices.size());
        assertEquals(AUXILIARY_SERVICE_ID, ((Number) suspendedServices.keySet().iterator().next()).intValue());

        transactions = ((Home) getContext().get(TransactionHome.class)).selectAll(getContext());
        assertNotNull(transactions);
        assertEquals(1, transactions.size());

        final Transaction transaction = (Transaction) transactions.iterator().next();
        assertEquals(WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, transaction.getAgent());
        assertEquals(Math.round(AUXILIARY_SERVICE_FEE / 7.0 * 6.0), transaction.getAmount());
    }

    /**
     * Visitor to use.
     */
    private final RetryRecurRechargeVisitor visitor_ = new PrepaidRetryRecurRechargeVisitor();
    /**
     * Service package fee.
     */
    private ServicePackageFee packageFee_;
    /**
     * Bundle fee.
     */
    private BundleFee bundleFee_;

    /**
     * Service fee.
     */
    private ServiceFee2 serviceFee_;

    /**
     * Subscriber-auxiliary service association.
     */
    private SubscriberAuxiliaryService association_;
}
