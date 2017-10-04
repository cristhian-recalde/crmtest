/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.unit_test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionTransientHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageTransientHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceTransientHome;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.account.SubscriptionTypeEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.home.ServiceAdjustmentTypeCreationHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.unit_test.utils.TransientSequenceIdentifiedSettingHome;
import com.trilogy.app.crm.xhome.adapter.ExtendedBeanAdapter;
import com.trilogy.app.crm.xhome.home.TransientFieldResettingHome;

/**
 * Unit test setup for default Price Plan and Price Plan Version.
 * @author Angie Li 
 *
 */
public class TestSetupPricePlanAndServices extends ContextAwareTestCase 
{
    public TestSetupPricePlanAndServices(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
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
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestSetupPricePlanAndServices.class);
        return suite;
    }

    //  INHERIT
    @Override
    public void setUp()
    {
        super.setUp();
        
        setup(getContext());
    }

    //  INHERIT
    @Override
    public void tearDown()
    {
        //tear down here
        completelyTearDown(getContext());
        
        super.tearDown();
    }

    public static void completelyTearDown(Context context) 
    {
        deletePricePlan(context);
        deleteServices(context);

        // Allow the Setup to run again
        setAllowSetupInstallation(context, true);
    }

    /**
     * Return TRUE if the installation of the configuration setup is permitted.
     * @param ctx
     * @return
     */
    public static boolean getAllowSetupInstallation(Context ctx) 
    {
        return ctx.getBoolean(TestSetupPricePlanAndServices.class, true);
    }

    /**
     * Sets the flag to allow configuration overwriting. 
     * @param ctx
     * @param b  If TRUE, allows configuration setup to run again (potentially overwriting old setup).
     */
    public static void setAllowSetupInstallation(Context ctx, boolean b) 
    {
        ctx.put(TestSetupPricePlanAndServices.class, b);
    }

    /**
     * Creates account in an account hierarchy
     * @param context
     */
    public static void setup(Context ctx)
    {
        if (getAllowSetupInstallation(ctx))
        {
            //Install homes if this is a unit test run off-line
            if (!UnitTestSupport.isTestRunningInXTest(ctx))
            {
                //Setup homes if running offline
                TestSetupAdjustmentTypes.setup(ctx);
                try
                {
                    TestSetupAccountHierarchy.setupSpid(ctx);
                }
                catch(Exception e)
                {
                    logDebugMsg(ctx, "Failed setup for SPID. " + e.getMessage(), e);
                }

                setupServiceHome(ctx);
                setupPricePlanHome(ctx);
                setupAuxServiceHome(ctx);
                setupServicePackageHome(ctx);
            }
            else
            {
                //Sometimes the Price Plan was scheduled for Version update.  Clear this list.
                clearPricePlanVersionUpdate(ctx);
            }
            
            setupServices(ctx);
            setupPricePlan(ctx);
            setupAuxServices(ctx);

            // Prevent this setup from running again.
            setAllowSetupInstallation(ctx, false);
        }
        else
        {
            logDebugMsg(ctx, "Skipping setup for TestSetupPricePlanAndServices again.", null);
        }
    }

    private static void clearPricePlanVersionUpdate(Context ctx) 
    {
        try
        {
            final Home home = (Home) ctx.get(PricePlanVersionUpdateRequestHome.class);
            Predicate predicate = new In(PricePlanVersionUpdateRequestXInfo.PRICE_PLAN_IDENTIFIER,
                    PRICE_PLAN_COLLECTION);
            home.removeAll(ctx, predicate);
        }
        catch (Exception e)
        {
            logDebugMsg(ctx, "Error during clearling of PricePlanVersionUpdateRequest.", e);
        }
    }

    public void testSetup()
    {
        testSetup(getContext());
    }

    public static void testSetup(Context ctx)
    {
        try
        {
            Home home = (Home) ctx.get(ServiceHome.class);
            assertNotNull("ServiceHome is null in the context.", home);
            Collection<Service> col = home.selectAll();
            assertTrue("No services found in the context.", col.size() > 0);

            for (Long serviceID: SERVICES_COLLECTION)
            {
                Or predicate  = new Or();
                predicate.add(new EQ(ServiceXInfo.ID, Long.valueOf(serviceID)));

                Collection<Service> result = CollectionSupportHelper.get(ctx).findAll(ctx, col, predicate);

                assertTrue("No such service exists in the system id=" + serviceID, result.size() == 1 );

                for (Service service: result)
                {
                    //TODO: add verification for created Services.
                    assertNotNull(AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, service.getAdjustmentType()));
                }
            }
        }
        catch (Exception e)
        {
            fail("Failed Services Setup. " + e.getMessage());
        }

        try
        {
            Home home = (Home) ctx.get(PricePlanHome.class);
            assertNotNull("PricePlanHome is null in the context.", home);
            Collection col = home.selectAll(ctx);
            assertTrue("No priceplans found were in the system.", col.size() > 0);
            Home ppvHome = (Home) ctx.get(PricePlanVersionHome.class);
            assertNotNull("PricePlanVersion is null in the context.", ppvHome);
            assertTrue("No price plan versions were in the system.", ppvHome.selectAll().size() > 0);
            
            assertNotNull("No ServicePackageHome in the context", ctx.get(ServicePackageHome.class));
        }
        catch (Exception e)
        {
            fail("Failed Price Plan Setup. " + e.getMessage());
        }
    }

    /**
     * Install the transient Service home in the context.
     * @param ctx
     */
    private static void setupServiceHome(Context ctx)
    {
        Home home = new TransientFieldResettingHome(ctx, new ServiceTransientHome(ctx));

        // Install a home to adapt between business logic bean and data bean
        home = new AdapterHome(
                ctx, 
                home, 
                new ExtendedBeanAdapter<com.redknee.app.crm.bean.Service, com.redknee.app.crm.bean.core.Service>(
                        com.redknee.app.crm.bean.Service.class, 
                        com.redknee.app.crm.bean.core.Service.class));
        
        try
        {
            home = new TransientSequenceIdentifiedSettingHome(ctx, home, IdentifierEnum.SERVICE_ID);
        }
        catch(Exception e)
        {
            fail("Failed setup. " + e.getMessage());
        }
        home = new ServiceAdjustmentTypeCreationHome(home);

        ctx.put(ServiceHome.class, home);
    }

    /**
     * Install the transient Price Plan home in the context.
     * @param ctx
     */
    private static void setupPricePlanHome(Context ctx)
    {
        Home pphome = new TransientFieldResettingHome(ctx, new PricePlanTransientHome(ctx));
        ctx.put(PricePlanHome.class, pphome);

        Home ppvHome = new TransientFieldResettingHome(ctx, new PricePlanVersionTransientHome(ctx));
        ctx.put(PricePlanVersionHome.class, ppvHome);
    }

    /**
     * Install the transient Aux service home in the context.
     * @param ctx
     */
    private static void setupAuxServiceHome(Context ctx)
    {
        Home home = new TransientFieldResettingHome(ctx, new SubscriberAuxiliaryServiceTransientHome(ctx));
        ctx.put(SubscriberAuxiliaryServiceHome.class, home);
    }

    /**
     * Create a default service
     */
    public static void setupServices(Context ctx)
    {
        /*
         * In a live system,
         * Creating the Services with id's specified will update the identifiersequence for ServiceIDs. 
         * So we have to keep track of the old identifier and reset it after we are done generating 
         * Services. 
         */
        long originalNextIdentifier = -1;
        try
        {
            originalNextIdentifier = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx, IdentifierEnum.SERVICE_ID, null);
        }
        catch (Exception e)
        {
            originalNextIdentifier = -1;
        }
        
        createServices(ctx);


        //Reset the old Next Identifier value.
        try
        {
            if (originalNextIdentifier != -1)
            {
                IdentifierSequenceSupportHelper.get(ctx).updateIdentifierSequence(ctx, IdentifierEnum.SERVICE_ID,
                        originalNextIdentifier);
            }
        }
        catch (Exception e)
        {
            logDebugMsg(ctx, "Failed to reset the old Service Next Identifier to " + originalNextIdentifier, e);
        }
    }

    private static void createServices(Context ctx) 
    {
        final Home home = (Home) ctx.get(ServiceHome.class);

        for(Long subscriptionType: TestSetupAccountHierarchy.TEST_SUBSCRIPTION_TYPES)
        {
            Iterator i = ServiceTypeEnum.COLLECTION.iterator();
            while (i.hasNext())
            {
                ServiceTypeEnum serviceType = (ServiceTypeEnum) i.next();

                final Service service = new Service();
                service.setContext(ctx);
                long serviceId = getUnitTestServiceId(subscriptionType.longValue(), serviceType);
                service.setID(serviceId);
                service.setIdentifier(serviceId);
                service.setExecutionOrder(1);
                service.setSpid(TestSetupAccountHierarchy.SPID_ID);
                service.setType(serviceType);
                service.setSubscriptionType(subscriptionType.longValue());
                service.setName("Unit Test Service " + serviceType.getDescription());
                service.setAdjustmentGLCode("GL00TEST001");
                service.setTaxAuthority(12);
                try 
                {
                    home.create(ctx, service);
                } 
                catch (Exception e) 
                {
                    logDebugMsg(ctx, "Failed to create Service=" + serviceId + " due to "+ e.getMessage(), e);
                }
                addToServicesCollection(serviceId);
            }
            /*TODO:
             * We need to setup a few services, of different ServiceTypes,
             * with different execution orders.  
             * In the off-line test scenario, these services should be 
             * associated with Adjustment types (leave the addition of these
             * AdjustmentTypes to TestSetupAdjustmentTypes). 
             */
        }
    }

    /**
     * Create default Price Plans for all supported Paid Types, Subscription Types. 
     * By default these Price Plan have all supported Services.
     * @param ctx
     */
    public static void setupPricePlan(Context ctx)
    {
        for(Long subscriptionType: TestSetupAccountHierarchy.TEST_SUBSCRIPTION_TYPES)
        {
            for(SubscriberTypeEnum paidType: PAID_TYPES)
            {
                PricePlan plan = new PricePlan();
                long pricePlanId = getUnitTestPricePlanId(subscriptionType.longValue(), paidType);
                plan.setId(pricePlanId);
                plan.setName(pricePlanName_ + pricePlanId);
                plan.setSpid(TestSetupAccountHierarchy.SPID_ID);
                plan.setPricePlanType(paidType);
                plan.setSubscriptionType(subscriptionType.longValue());
                plan.setSMSRatePlan(SMS_RATE_PLAN_ID);
                plan.setVoiceRatePlan(VOICE_RATE_PLAN_ID);
                plan.setDataRatePlan(DATA_RATE_PLAN_ID);

                PricePlanVersion version = new PricePlanVersion();
                version.setVersion(pricePlanVersionID_);
                version.setId(plan.getId());
                //Add all the Services to this Price Plan. //Map<String ServiceId, ServiceFee2>
                Map<Long, ServiceFee2> fees = new HashMap<Long, ServiceFee2>();
                Iterator i = ServiceTypeEnum.COLLECTION.iterator();
                while(i.hasNext())
                {
                    ServiceTypeEnum serviceType = (ServiceTypeEnum) i.next();
                    
                    ServiceFee2 fee = new ServiceFee2();
                    long serviceId = getUnitTestServiceId(subscriptionType, serviceType);
                    if (serviceExists(ctx, serviceId))
                    {
                        fee.setServiceId(serviceId);
                        fee.setFee(1000);
                        fee.setServicePeriod(ServicePeriodEnum.MONTHLY);
                        fee.setEnabled(true);
                        fees.put(serviceId, fee);
                    }
                }
                version.setServiceFees(fees);
                version.setDescription(pricePlanName_);
                if (paidType.equals(SubscriberTypeEnum.POSTPAID))
                {
                    version.setCreditLimit(100000);
                }
                version.setActivateDate(new Date());

          //      plan.setVersions(version);
                plan.setCurrentVersion(pricePlanVersionID_);

                try 
                {
                    Home ppHome = (Home) ctx.get(PricePlanHome.class);
                    ppHome.create(ctx, plan);
                }
                catch (Exception e) 
                {
                    logDebugMsg(ctx, "Failed to create Price Plan. " + e.getMessage(), e);
                }
                finally
                {
                    addToPricePlanCollection(pricePlanId);
                }

                try
                {
                    Home ppvHome = (Home) ctx.get(PricePlanVersionHome.class);
                    ppvHome.create(ctx, version);
                } 
                catch (Exception e)
                {
                    logDebugMsg(ctx, "Failed to create Price Plan Version. " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Returns TRUE if the service exists in the system.
     * @param ctx
     * @param serviceId
     * @return
     */
    private static boolean serviceExists(Context ctx, long serviceId) 
    {
        Service service = null;
        try
        {
            service  = ServiceSupport.getService(ctx, serviceId);
        }
        catch (Exception e)
        {
            logDebugMsg(ctx, "Service not found, id=" + serviceId, e);
        }
        return service != null;
    }

    /**
     * Create a default aux services
     */
    private static void setupAuxServices(Context ctx)
    {
    }

    private static void setupServicePackageHome(Context ctx) 
    {
        ctx.put(ServicePackageHome.class, new TransientFieldResettingHome(ctx, new ServicePackageTransientHome(ctx)));
    }

    private static void deletePricePlan(Context context)
    {
        try
        {
            Home pphome = (Home) context.get(PricePlanHome.class);
            Predicate predicate = new In(PricePlanXInfo.ID, PRICE_PLAN_COLLECTION);
            pphome.removeAll(context, predicate);
        }
        catch (Exception e)
        {
            logDebugMsg(context, "Failed to delete all test Price Plans.", e);
        }

        try
        {
            Home pphome = (Home) context.get(PricePlanHome.class);
            Predicate predicate = new In(PricePlanXInfo.ID, PRICE_PLAN_COLLECTION);
            pphome.removeAll(context, predicate);

            Home ppvhome = (Home) context.get(PricePlanVersionHome.class);
            predicate = new In(PricePlanVersionXInfo.ID, PRICE_PLAN_COLLECTION);
            ppvhome.removeAll(context, predicate);
        }
        catch (Exception e)
        {
            logDebugMsg(context, "Failed to delete all test Price Plans Versions", e);
        }
    }

    private static void deleteServices(Context context) 
    {
        try
        {
            Home home = (Home) context.get(ServiceHome.class);
            Predicate predicate = new In(ServiceXInfo.ID, SERVICES_COLLECTION);
            home.removeAll(context, predicate);
        }
        catch (Exception e)
        {
            logDebugMsg(context, "Failed to delete all test Services.", e);
        }
    }

    /**
     * Assuming there are no more than 9 Paid types, subscriptionTypes, Services Types then we'll be ok to do this.
     * @param paidType
     * @param subscriptionType
     * @param serviceType
     * @return
     */
    public static long getUnitTestServiceId(
            long subscriptionTypeId, 
            ServiceTypeEnum serviceType)
    {
        return (DEFAULT_OFFSET 
                + (OFFSET_SERVICE_TYPE * serviceType.getIndex())
                + (OFFSET_SUBSCRIPTION_TYPE * subscriptionTypeId));
    }

    public static long getUnitTestPricePlanId(
            long subscriptionTypeId,
            SubscriberTypeEnum paidType)
    {
        return (DEFAULT_OFFSET 
                + (OFFSET_PAID_TYPE * paidType.getIndex())
                + (OFFSET_SUBSCRIPTION_TYPE * subscriptionTypeId));
    }

    /**
     * Log Debug message about the exception that occurred.
     * @param ctx
     * @param msg
     * @param e
     */
    private static void logDebugMsg(Context ctx, String msg, Throwable e) 
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(TestSetupPricePlanAndServices.class, msg, e).log(ctx);
        }
    }

    /**
     * Add to list of services to clean up when unit test is torn down.
     * @param identifier
     */
    public static void addToServicesCollection(Long identifier)
    {
        SERVICES_COLLECTION.add(identifier);
    }

    /**
     * Add to list of price plans to clean up when unit test is torn down.
     * @param identifier
     */
    public static void addToPricePlanCollection(Long identifier)
    {
        PRICE_PLAN_COLLECTION.add(identifier);
    }

    /**
     * Service and Price Plan static variables.
     */
    
    /**
     * The following offsets are used to create the identifiers for Services and Price Plans.
     * All identifiers are in the 900,000's
     * They differ by Paid type, Subscription Type and sometimes Service Type.
     * By default the Subscription Type identifiers are in the 900's. See TestSetupAccountHierarchy. 
     */
    public static final long DEFAULT_OFFSET = 900000L;
    private static final long OFFSET_PAID_TYPE = 10000L;
    private static final long OFFSET_SERVICE_TYPE = 1000L;
    private static final long OFFSET_SUBSCRIPTION_TYPE = 1L;

    //List of Service Identifiers created for this unit test.
    private static HashSet<Long> SERVICES_COLLECTION = new HashSet<Long>();

    public static Collection<SubscriberTypeEnum> PAID_TYPES = new ArrayList<SubscriberTypeEnum>();
    static
    {
        PAID_TYPES.add(SubscriberTypeEnum.PREPAID);
        PAID_TYPES.add(SubscriberTypeEnum.POSTPAID);
    }
    private static HashSet<Long> PRICE_PLAN_COLLECTION = new HashSet<Long>();

    //DEFAULT_PRICE_PLAN_ID was kept to satisfy the unit test SubscribersImplTest.  Even that test will need to be modified eventually.
    public static final long DEFAULT_PRICE_PLAN_ID = 
        getUnitTestPricePlanId(TestSetupAccountHierarchy.getSubscriptionTypeId(SubscriptionTypeEnum.AIRTIME_INDEX), 
                SubscriberTypeEnum.POSTPAID);

    public static final int pricePlanVersionID_ = 1; 
    public static final String pricePlanName_ = "Test Priceplan ";
    /**
     * Used for Offline Subscription creation.  See TestSetupSubscriberServices.
     * Default services for Postpaid subscriptions.
     */
    public static final long SERVICE_VOICE = 902991L;
    public static final long SERVICE_GENERIC = 900991L;

    private static final String VOICE_RATE_PLAN_ID = "1";
    private static final String SMS_RATE_PLAN_ID = "1";
    private static final String DATA_RATE_PLAN_ID = "1";
}
