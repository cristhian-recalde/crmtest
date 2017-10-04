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


public class TestProvisionSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculatorContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestProvisionSubscriberServicesProvisioningCalculator(final String name)
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

        final TestSuite suite = new TestSuite(TestProvisionSubscriberServicesProvisioningCalculator.class);

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
    
    public void testCalculateServicesToProvisionOnCreation()
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
            
            // Services to unprovision
            Set<SubscriberServices> servicesToUnprovision = new HashSet<SubscriberServices>();
            
            // Services to retry
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            
            // Elegible services
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 1, "Service 1", false, false));
            elegibleServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 2, "Service 2", false, false));
            elegibleServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 3, "Service 3", false, false));
            
            // Currently provisioned services
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            
            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.UNPROVISION, servicesToUnprovision);
            
            // Currently suspended services
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            
            final ProvisionSubscriberServicesProvisioningCalculator calculator = new ProvisionSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.PROVISION), 1L, 2L, 3L);
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    public void testCalculateServicesToProvisionOnStore()
    {
        Context ctx = setUpContext(getContext());
        
        try
        {
            // New subscriber
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setState(SubscriberStateEnum.ACTIVE);
            newSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            newSubscriber.setId("100-1");
        
            // Old subscriber
            Subscriber oldSubscriber = new Subscriber();
            oldSubscriber.setState(SubscriberStateEnum.SUSPENDED);
            oldSubscriber.setSubscriberType(SubscriberTypeEnum.POSTPAID);
            oldSubscriber.setId("100-1");
            
            // Services to unprovision (2)
            Set<SubscriberServices> servicesToUnprovision = new HashSet<SubscriberServices>();
            servicesToUnprovision.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 2, "Service 2", false, false));
            
            // Services to retry (7)
            Set<SubscriberServices> servicesToRetry = new HashSet<SubscriberServices>();
            servicesToRetry.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 7, "Service 7", false, false));
            
            // Currently suspended services (6)
            Set<SubscriberServices> currentlySuspendedServices = new HashSet<SubscriberServices>();
            currentlySuspendedServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 6, "Service 6", false, false));

            // Currently provisioned services (1, 2, 3)
            Set<SubscriberServices> currentlyProvisionedServices = new HashSet<SubscriberServices>();
            currentlyProvisionedServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 1, "Service 1", true, false));
            currentlyProvisionedServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 3, "Service 3", false, false));
            currentlyProvisionedServices.addAll(servicesToUnprovision);

            // Elegible services (1 2 3 4 5 6 7)
            Set<SubscriberServices> elegibleServices = new HashSet<SubscriberServices>();
            elegibleServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 4, "Service 4", false, false));
            elegibleServices.add(createSubscriberServices(ctx, newSubscriber, 
                    ServiceStateEnum.PROVISIONED, true, 5, "Service 5", false, false));
            elegibleServices.addAll(currentlyProvisionedServices);
            elegibleServices.addAll(currentlySuspendedServices);
            elegibleServices.addAll(servicesToRetry);
            
            final ProvisionSubscriberServicesProvisioningCalculator calculator = new ProvisionSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber);
    
            Map<Short, Set<SubscriberServices>> currentResult = new HashMap<Short, Set<SubscriberServices>>();
            currentResult.put(AbstractSubscriberServicesProvisioningCalculator.UNPROVISION, servicesToUnprovision);

            Map<Short, Set<SubscriberServices>> result = calculator.calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, currentResult);
            
            compare(result.get(ProvisionSubscriberServicesProvisioningCalculator.PROVISION), 1L, 2L, 4L, 5L, 7L);
        }
        catch (HomeException e)
        {
            fail("Exception: " + e.getMessage());
        }
    }
    
    

}
