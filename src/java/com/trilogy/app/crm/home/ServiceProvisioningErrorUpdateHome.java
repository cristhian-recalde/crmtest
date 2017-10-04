package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesID;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.bean.service.ServiceProvisioningError;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorHome;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorID;
import com.trilogy.app.crm.bean.service.ServiceProvisioningErrorXInfo;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.support.ExternalAppMappingSupportHelper;
import com.trilogy.app.crm.support.ServiceProvisioningErrorSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

public class ServiceProvisioningErrorUpdateHome extends HomeProxy 
{
    
    ServiceProvisioningErrorUpdateHome(Home delegate)
    {
        super(delegate);
    }
    
    public Object create(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        updateServiceProvisioningErrorRecord(context, (SubscriberServices) obj);
        return super.create(context, obj);
    }
    
    public Object store(Context context, Object obj)
    throws HomeException, HomeInternalException
    {
        updateServiceProvisioningErrorRecord(context, (SubscriberServices) obj);
        return super.store(context, obj);
    }
    
    public void remove(Context context, Object obj)
    throws HomeException, HomeInternalException
    {
    	deleteServiceProvisioningErrorRecords(context, obj);
        getDelegate().remove(obj);
    }
    
    public void removeAll(Context context, Object obj)
    throws HomeException, HomeInternalException
    {
    	throw new HomeException("removeAll() is not available for this pipeline.  " +
    			" This method does not allow a straight forward way to get the filter" +
    			" information so that we can also remove the appropriate " +
    	        " Subscriber Provisioning Error Records");
    }

    
    /**
     * Update/Creates/Removes an Service Provisioning Error record with the given parameters.
     * @param ctx
     * @param subId
     * @param serviceId
     * @param state
     * @param serviceType
     * @param action
     * @throws HomeException
     */
    private void updateServiceProvisioningErrorRecord(
            final Context ctx, final SubscriberServices bean)
    throws HomeException
    {
        SubscriberServices oldBean = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, bean.getSubscriberId(), bean.getServiceId(), bean.getPath());
        if (isInErrorState(bean))
        {
            Home errorHome = (Home) ctx.get(ServiceProvisioningErrorHome.class);
            Service service = ServiceSupport.getService(ctx, bean.getServiceId());
            ServiceProvisioningError error = ServiceProvisioningErrorSupport.getErrorRecord(
                    ctx, bean.getServiceId(), bean.getSubscriberId());
            if (error != null)
            {   
                error.setAction(mapToAction(bean.getProvisionedState()));
                errorHome.store(ctx,error);
            }
            else
            {
                error = new ServiceProvisioningError();
                error.setServiceId(bean.getServiceId());
                error.setSubscriberId(bean.getSubscriberId());
                error.setExternalApplicationId(
                        ExternalAppMappingSupportHelper.get(ctx).getExternalApplicationId(ctx, service.getType()));
                error.setAction(mapToAction(bean.getProvisionedState()));
                errorHome.create(error);
            }
        }
        else if (oldBean != null && isInErrorState(oldBean) && !isInErrorState(bean))
        {
            //left an error state
            deleteServiceProvisioningErrorRecords(ctx, oldBean);
        }
    }
    
    /**
     * Returns TRUE if the given bean is in an error state.
     * @param bean
     * @return
     */
    private boolean isInErrorState(SubscriberServices bean)
    {
        boolean result = false;
        try 
        {
            if (SubscriberServicesSupport.ERROR_STATES.getByIndex(bean.getProvisionedState().getIndex()) != null)
            {
                result = true;
            }
        }
        catch (java.lang.ArrayIndexOutOfBoundsException e)
        {
            // The Provisioned State is not one of the Error States.
        }
        return result;
    }

    /**
     * Map the given Service State Enum to the Service Provision Action.
     * @param state
     * @return
     */
    private ServiceProvisionActionEnum mapToAction(ServiceStateEnum state)
    {
        ServiceProvisionActionEnum result = null;
        switch(state.getIndex())
        {
        case ServiceStateEnum.PROVISIONED_INDEX:
        case ServiceStateEnum.PROVISIONEDWITHERRORS_INDEX:
            result = ServiceProvisionActionEnum.PROVISION;
            break;
        case ServiceStateEnum.SUSPENDED_INDEX:
        case ServiceStateEnum.SUSPENDEDWITHERRORS_INDEX:
            result = ServiceProvisionActionEnum.SUSPEND;
            break;
        default:
            result = ServiceProvisionActionEnum.UNPROVISION;               
        }
        return result;
    }
    
    /**
     * Delete all the records identified by the service id and the subscriber id.
     * @param ctx
     * @param bean
     * @throws HomeException
     * @throws HomeInternalException
     */
    private void deleteServiceProvisioningErrorRecords(final Context ctx, final Object bean)
    throws HomeException, HomeInternalException
    {
    	ServiceProvisioningErrorID id = null;
    	And where = new And();
    	if (bean instanceof SubscriberServices)
    	{
    		SubscriberServices service = (SubscriberServices) bean;
    		where.add(new EQ(ServiceProvisioningErrorXInfo.SUBSCRIBER_ID, service.getSubscriberId()));
    		where.add(new EQ(ServiceProvisioningErrorXInfo.SERVICE_ID, Long.valueOf(service.getServiceId())));
    	}
    	else //bean instanceof SubscriberServicesID
    	{
    		SubscriberServicesID service = (SubscriberServicesID) bean;
    		where.add(new EQ(ServiceProvisioningErrorXInfo.SUBSCRIBER_ID, service.getSubscriberId()));
    		where.add(new EQ(ServiceProvisioningErrorXInfo.SERVICE_ID, Long.valueOf(service.getServiceId())));
    	}

    	deleteErrorRecord(ctx, where);
    }
    
    private void deleteErrorRecord(final Context ctx, final Object where)
    throws HomeException, HomeInternalException
    {
    	Home errorHome = (Home) ctx.get(ServiceProvisioningErrorHome.class);
    	errorHome.removeAll(ctx, where);
    }
    
}


