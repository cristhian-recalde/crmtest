package com.trilogy.app.crm.home.sub;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.blackberry.BlackberrySupport;
import com.trilogy.app.crm.support.ServiceSupport;


public class SubscriberSelectedServicesValidator implements Validator
{
    
    private SubscriberSelectedServicesValidator()
    {
    }

    public static Validator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberSelectedServicesValidator();
        }

        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object)
    {
        final Subscriber newSub = (Subscriber)object;
        
        if (newSub!=null)
        {
            try
            {
                int countData = 0;
                int countBlackBerry = 0;
                Set<ServiceFee2ID> services = newSub.getServices(context);
                
                int count = 1;
                for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
                { 
					Object obj = iter.next();
					long serviceId = 0;
					if (obj instanceof ServiceFee2ID) {
						serviceId = ((ServiceFee2ID) obj).getServiceId();
					} else if (obj instanceof Long) {
						serviceId = (Long) obj;
					}
					
                    //Long serviceId = iter.next();
                    Service service = ServiceSupport.getService(context, serviceId);
                    if (service != null)
                    {
                        if (service.getType().equals(ServiceTypeEnum.DATA))
                        {
                            countData++;
                        }
                        if (service.getType().equals(ServiceTypeEnum.BLACKBERRY))
                        {
                            countBlackBerry++;
                        }
                    }
                }
                
                final CompoundIllegalStateException compound = new CompoundIllegalStateException();
                
                if (countData>0 && countBlackBerry>0 && BlackberrySupport.areBlackberryServicesProvisionedToIPC(context))
                {
                    compound.thrown(
                            new IllegalPropertyArgumentException(
                                SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Only either one " + ServiceTypeEnum.DATA + " service or one " + ServiceTypeEnum.BLACKBERRY + " service can be selected."));
                }
                else
                {
                    if (countData>1)
                    {
                        compound.thrown(
                                new IllegalPropertyArgumentException(
                                    SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Only one " + ServiceTypeEnum.DATA + " service can be selected."));
                    }
                    
                    if (countBlackBerry>1)
                    {
                        compound.thrown(
                                new IllegalPropertyArgumentException(
                                    SubscriberXInfo.INTENT_TO_PROVISION_SERVICES, "Only one " + ServiceTypeEnum.BLACKBERRY + " service can be selected."));
                    }
                }
                

                compound.throwAll();

            } 
            catch (HomeException e)
            {
                new MajorLogMsg(this,"Can not get the services from serviceIDs", e).log(context);
                final CompoundIllegalStateException compound = new CompoundIllegalStateException();
                compound.thrown(
                        new IllegalPropertyArgumentException(
                            SubscriberXInfo.PRICE_PLAN, "Services not found."));
                compound.throwAll();
            }
        }
    }
    
    private boolean isServiceProvisionExpected(Context ctx, Subscriber newSub, Subscriber oldSub, ServiceTypeEnum serviceType) throws HomeException
    {
        List<ServiceFee2ID> services = new ArrayList<ServiceFee2ID>();

        services.addAll(newSub.getServices());
        services.removeAll(oldSub.getServices());
        
        for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if (service.getType().equals(serviceType))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isServiceUnprovisionExpected(Context ctx, Subscriber newSubscriber, Subscriber oldSubscriber, ServiceTypeEnum serviceType) throws HomeException
    {
        List<ServiceFee2ID> services = new ArrayList<ServiceFee2ID>();

        //try to calculate service needs provision

        services.addAll(oldSubscriber.getServices());
        services.removeAll(newSubscriber.getServices());
        
        for (Iterator<ServiceFee2ID> iter = services.iterator(); iter.hasNext();)
        { 
        	ServiceFee2ID serviceFee2ID = iter.next();
            Service service = ServiceSupport.getService(ctx, serviceFee2ID.getServiceId());
            if (service.getType().equals(serviceType))
            {
                return true;
            }
        }
        
        return false;        
    }
    
    private static Validator instance;    
}
