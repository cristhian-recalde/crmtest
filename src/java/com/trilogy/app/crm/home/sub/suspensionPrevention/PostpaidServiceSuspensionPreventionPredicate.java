package com.trilogy.app.crm.home.sub.suspensionPrevention;

import java.util.Map;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * 
 * @author sbanerjee
 *
 */
public final class PostpaidServiceSuspensionPreventionPredicate implements
        TypedPredicate<ServiceFee2ID>
{
    private final Map<ServiceFee2ID, ServiceFee2> suspendedServices;

    public PostpaidServiceSuspensionPreventionPredicate(
            Map<ServiceFee2ID, ServiceFee2> suspendedServices)
    {
        this.suspendedServices = suspendedServices;
    }

    @Override
    public boolean f(Context ctx, ServiceFee2ID id)
            throws AbortVisitException
    {
        try
        {
            final Service service = this.suspendedServices.get(id).getService(ctx);
            return (service.getChargeScheme() == ServicePeriodEnum.ONE_TIME) && service.isRestrictProvisioning();
        } 
        catch (Exception e)
        {
            // Nothing to be done; will return false;
        }
        
        return false;
    }
}