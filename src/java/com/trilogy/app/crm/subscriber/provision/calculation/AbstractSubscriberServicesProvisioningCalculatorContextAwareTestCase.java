package com.trilogy.app.crm.subscriber.provision.calculation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


public class AbstractSubscriberServicesProvisioningCalculatorContextAwareTestCase extends ContextAwareTestCase
{
    public AbstractSubscriberServicesProvisioningCalculatorContextAwareTestCase(final String name)
    {
        super(name);
    }

    protected void compare(Set<SubscriberServices> result, Long... expectedValues)
    {
        if (expectedValues.length>0)
        {
            assertNotNull("Provision set shouldn't be null", result);
            assertEquals("Number or services to provision should be " + expectedValues.length, expectedValues.length, result.size());
        }
        
        if (result != null)
        {
            assertEquals("Number or services to provision should be " + expectedValues.length, expectedValues.length, result.size());

            Set<Long> expected = new HashSet<Long>();
            Set<Long> actual = new HashSet<Long>();
            for (int i=0; i<expectedValues.length; i++)
            {
                expected.add(expectedValues[i]);
            }
            for (SubscriberServices service : result)
            {
                actual.add(service.getServiceId());
            }
            assertTrue("Expected services differs from current services", expected.equals(actual));        
        }

        
    }
    
    protected Context setUpContext(Context context)
    {
        Context result = context.createSubContext();
        result.put(ServiceHome.class, new ServiceTransientHome(result));
        result.put(SubscriberServicesHome.class, new SubscriberServicesTransientHome(result));
        return result;
       
    }
    
    
    protected SubscriberServices createSubscriberServices(Context ctx, Subscriber subscriber, ServiceStateEnum provisionedState, boolean mandatory, long id, boolean reprovisionOnActive, boolean enableCLTC) throws HomeException
    {
        Service svc = createService(ctx, id, "Service " + id, reprovisionOnActive, enableCLTC);
        SubscriberServices subService = createSubscriberServices(ctx, subscriber, provisionedState, mandatory, svc);
        return subService;
    }


    protected SubscriberServices createSubscriberServices(Context ctx, Subscriber subscriber, ServiceStateEnum provisionedState, boolean mandatory, long id, String name, boolean reprovisionOnActive, boolean enableCLTC) throws HomeException
    {
        Service svc = createService(ctx, id, name, reprovisionOnActive, enableCLTC);
        SubscriberServices subService = createSubscriberServices(ctx, subscriber, provisionedState, mandatory, svc);
        return subService;
    }
    
    protected Service createService(Context ctx, long id, String name, boolean reprovisionOnActive, boolean enableCLTC) throws HomeException
    {
        Service result = new Service();
        result.setEnableCLTC(enableCLTC);
        result.setIdentifier(id);
        result.setReprovisionOnActive(reprovisionOnActive);
        
        Home home = (Home) ctx.get(ServiceHome.class);
        home.create(result);
        return result;
        
        
    }
    
    protected SubscriberServices createSubscriberServices(Context ctx, Subscriber subscriber, ServiceStateEnum provisionedState, boolean mandatory, Service svc) throws HomeException
    {
        final SubscriberServices result = new SubscriberServices();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 30);
        final Date end = (calendar.getTime());

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        final Date start = (calendar.getTime());
        
        result.setContext(ctx);
        result.setMandatory(mandatory);
        result.setStartDate(start);
        result.setEndDate(end);
        result.setProvisionedState(provisionedState);
        result.setService(svc);
        result.setServiceId(svc.getID());
        result.setServicePeriod(svc.getChargeScheme());
        result.setSubscriberId(subscriber.getId());
        result.setSuspendReason(SuspendReasonEnum.NONE);
        
        Home home = (Home) ctx.get(SubscriberServicesHome.class);
        home.create(result);

        return result;
       
    }
}
