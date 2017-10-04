package com.trilogy.app.crm.home.sub.suspensionPrevention;

import java.util.Map;

import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * 
 * @author sbanerjee
 *
 */
public final class PrepaidBundleSuspensionPreventionPredicate implements
        TypedPredicate<Long>
{
    private final Map<Long, BundleFee> suspendedBundles;

    public PrepaidBundleSuspensionPreventionPredicate(
            Map<Long, BundleFee> suspendedBundles)
    {
        this.suspendedBundles = suspendedBundles;
    }

    @Override
    public boolean f(Context ctx, Long id)
            throws AbortVisitException
    {
        try
        {
            /**
             * TT#13053146038
             * Reason for adding this flag (isRecurring) as an extra check is 
               because during removal of a bundle while recurring recharge we are calling 
               store() on the subscriber . This code returns true if RP
               flag is true for the services/bundles attached to the subscription . 
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
                return this.suspendedBundles.get(id).getBundleProfile(ctx).isRestrictProvisioning();
            }
        } 
        catch (Exception e)
        {
            // Nothing to be done; will return false;
        }
        
        return false;
    }
}