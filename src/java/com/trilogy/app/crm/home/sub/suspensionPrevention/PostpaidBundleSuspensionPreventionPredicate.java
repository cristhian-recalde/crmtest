package com.trilogy.app.crm.home.sub.suspensionPrevention;

import java.util.Map;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.util.TypedPredicate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * 
 * @author sbanerjee
 *
 */
public final class PostpaidBundleSuspensionPreventionPredicate implements
        TypedPredicate<Long>
{
    private final Map<Long, BundleFee> suspendedBundles;

    public PostpaidBundleSuspensionPreventionPredicate(
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
            final BundleProfile bundleProfile = this.suspendedBundles.get(id).getBundleProfile(ctx);
            return (bundleProfile.getChargingRecurrenceScheme() == ServicePeriodEnum.ONE_TIME) && bundleProfile.isRestrictProvisioning();
        } 
        catch (Exception e)
        {
            // Nothing to be done; will return false;
        }
        
        return false;
    }
}