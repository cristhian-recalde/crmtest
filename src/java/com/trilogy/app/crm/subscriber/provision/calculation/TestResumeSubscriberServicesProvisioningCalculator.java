package com.trilogy.app.crm.subscriber.provision.calculation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


public class TestResumeSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculatorContextAwareTestCase
{

    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestResumeSubscriberServicesProvisioningCalculator(final String name)
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

        final TestSuite suite = new TestSuite(TestResumeSubscriberServicesProvisioningCalculator.class);

        return suite;
    }
    
    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }
    
    public void testCalculateServicesToResumeOnCreation()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = null;
            
            SubscriberServices service1 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 1, false, false);
            SubscriberServices service2 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 2, false, false);
            SubscriberServices service3 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 3, false, false);
            SubscriberServices service4 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 4, false, false);

            // Services to retry
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            
            // Elegible services
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(service1);
            elegibleServices.add(service2);
            elegibleServices.add(service3);
            
            // Currently provisioned services
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(service2);
            
            // Currently suspended services
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(service3);
            
            // Currently suspended services
            Set<SubscriberServices> servicesToProvision = new HashSet<SubscriberServices>();
            servicesToProvision.add(service4);

            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.PROVISION, servicesToProvision);

            final ResumeSubscriberServicesProvisioningCalculator calculator = new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.RESUME));
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    public void testCalculateServicesToResumeOnStorePrepaid()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = new Subscriber();
            oldSubscriber.setState(SubscriberStateEnum.ACTIVE);
            oldSubscriber.setSubscriberType(SubscriberTypeEnum.PREPAID);
            oldSubscriber.setId("100-1");
            
            SubscriberServices service1 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 1, false, false);
            SubscriberServices service2 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 2, false, false);
            SubscriberServices service3 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 3, false, false);
            SubscriberServices service4 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 4, false, false);

            // Services to retry
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            
            // Elegible services
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(service1);
            elegibleServices.add(service2);
            elegibleServices.add(service3);
            
            // Currently provisioned services
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(service2);
            
            // Currently suspended services
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(service3);
            
            // Currently suspended services
            Set<SubscriberServices> servicesToProvision = new HashSet<SubscriberServices>();
            servicesToProvision.add(service4);

            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.PROVISION, servicesToProvision);

            final ResumeSubscriberServicesProvisioningCalculator calculator = new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.RESUME));
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    public void testCalculateServicesToResumeOnStorePostpaidNoCLCTOperation()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            newSubscriber.setAboveCreditLimit(true);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = new Subscriber();
            oldSubscriber.setState(SubscriberStateEnum.ACTIVE);
            oldSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            oldSubscriber.setAboveCreditLimit(true);
            oldSubscriber.setId("100-1");
            
            SubscriberServices service1 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 1, false, false);
            SubscriberServices service2 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 2, false, false);
            SubscriberServices service3 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 3, false, false);
            SubscriberServices service4 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 4, false, false);
            SubscriberServices service5 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 5,false, false);
            SubscriberServices service6 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 6,false, true);
            SubscriberServices service7 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.UNPROVISIONEDWITHERRORS, false, 7,false, false);
            SubscriberServices service8 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 8,false, true);
            
            // Services to retry (7)
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            servicesToRetry.add(service7);
            
            // Elegible services (1 2 5 6)
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(service1);
            elegibleServices.add(service2);
            elegibleServices.add(service5);
            elegibleServices.add(service6);

            // Currently suspended services (6 8)
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(service6);
            currentlySuspendedServices.add(service8);

            // Currently provisioned services (1, 2, 3, 4)
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(service1);
            currentlyProvisionedServices.add(service2);
            currentlyProvisionedServices.add(service3);
            currentlyProvisionedServices.add(service4);

            // Currently suspended services
            Set<SubscriberServices> servicesToProvision = new HashSet<SubscriberServices>();
            servicesToProvision.add(service5);

            final ResumeSubscriberServicesProvisioningCalculator calculator = new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.PROVISION, servicesToProvision);

            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.RESUME));
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    public void testCalculateServicesToResumeOnStorePostpaidCLCTOperation()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            newSubscriber.setAboveCreditLimit(true);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = new Subscriber();
            oldSubscriber.setState(SubscriberStateEnum.ACTIVE);
            oldSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            oldSubscriber.setAboveCreditLimit(false);
            oldSubscriber.setId("100-1");
            
            SubscriberServices service1 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 1, false, false);
            SubscriberServices service2 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 2, false, false);
            SubscriberServices service3 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 3, false, false);
            SubscriberServices service4 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 4, false, false);
            SubscriberServices service5 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 5,false, false);
            SubscriberServices service6 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 6,false, true);
            SubscriberServices service7 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.UNPROVISIONEDWITHERRORS, false, 7,false, false);
            SubscriberServices service8 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 8,false, true);
            
            // Services to retry (7)
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            servicesToRetry.add(service7);
            
            // Elegible services (1 2 5 6)
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(service1);
            elegibleServices.add(service2);
            elegibleServices.add(service5);
            elegibleServices.add(service6);

            // Currently suspended services (6 8)
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(service6);
            currentlySuspendedServices.add(service8);

            // Currently provisioned services (1, 2, 3, 4)
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(service1);
            currentlyProvisionedServices.add(service2);
            currentlyProvisionedServices.add(service3);
            currentlyProvisionedServices.add(service4);

            // Currently suspended services
            Set<SubscriberServices> servicesToProvision = new HashSet<SubscriberServices>();
            servicesToProvision.add(service5);

            final ResumeSubscriberServicesProvisioningCalculator calculator = new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.PROVISION, servicesToProvision);

            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.RESUME));
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    public void testCalculateServicesToResumeOnStorePostpaidCLCTSuspendedPricePlanChange()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            newSubscriber.setAboveCreditLimit(true);
            newSubscriber.setPricePlan(10);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = new Subscriber();
            oldSubscriber.setState(SubscriberStateEnum.ACTIVE);
            oldSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            oldSubscriber.setAboveCreditLimit(true);
            newSubscriber.setPricePlan(20);
            oldSubscriber.setId("100-1");
            
            SubscriberServices service1 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 1, false, false);
            SubscriberServices service2 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 2, false, false);
            SubscriberServices service3 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 3, false, false);
            SubscriberServices service4 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 4, false, false);
            SubscriberServices service5 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 5,false, false);
            SubscriberServices service6 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, true, 6, false, true);
            SubscriberServices service7 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.UNPROVISIONEDWITHERRORS, false, 7,false, false);
            SubscriberServices service8 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.SUSPENDED, false, 8,false, true);
            SubscriberServices service9 = createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, false, 9,false, true);
            
            // Services to retry (7)
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            servicesToRetry.add(service7);
            
            // Elegible services (1 2 5 6)
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(service1);
            elegibleServices.add(service2);
            elegibleServices.add(service5);
            elegibleServices.add(service6);
            elegibleServices.add(service8);
            elegibleServices.add(service9);
            newSubscriber.setIntentToProvisionServices(elegibleServices);

            // Currently suspended services (6 8)
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(service6);
            currentlySuspendedServices.add(service8);

            // Currently provisioned services (1, 2, 3, 4)
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(service1);
            currentlyProvisionedServices.add(service2);
            currentlyProvisionedServices.add(service3);
            currentlyProvisionedServices.add(service4);

            // Currently suspended services
            Set<SubscriberServices> servicesToProvision = new HashSet<SubscriberServices>();
            servicesToProvision.add(service5);
            servicesToProvision.add(service9);

            final ResumeSubscriberServicesProvisioningCalculator calculator = new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.PROVISION, servicesToProvision);

            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.RESUME), 6L);
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
}
