package com.trilogy.app.crm.home.sub.suspensionPrevention;

import java.util.Map;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * 
 * @author sbanerjee
 *
 */
public final class PrepaidServiceSuspensionPreventionPredicate implements
        TypedPredicate<ServiceFee2ID>
{
    private final Map<ServiceFee2ID, ServiceFee2> suspendedServices;

    public PrepaidServiceSuspensionPreventionPredicate(
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
            /**
             * TT#13053146038
             * Reason for adding this flag (isRecurring) as an extra check is 
               because during removal of a bundle while recurring recharge we are calling 
               store() on the subscriber . This code returns true if RP
               flag is true for the services attached to the subscription . 
               If returned true , the application removes the service from 
               IntentToProvisionServices Collection of subscriber and the service gets removed ;
               which is wrong.
               RP flag should not interfere the process of removal of any service during recurring re-charge.
               Therefore, this flag (true) has been put in Context during bundle removal while recurring re-charge only.
               See method:
               com.redknee.app.crm.bas.recharge.RechargeSubscriberBundleVisitor.removeBundle(Context, BundleFee) 
             *
             */
            boolean isRecurring = ctx.getBoolean(ChargingConstants.IS_RECURRING_RECHARGE, false);
            if(!isRecurring)
            {
                return this.suspendedServices.get(id).getService(ctx).isRestrictProvisioning();
            }
        } 
        catch (Exception e)
        {
            // Nothing to be done; will return false;
        }
        
        return false;
    }
}