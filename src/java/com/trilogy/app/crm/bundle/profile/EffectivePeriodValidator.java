package com.trilogy.app.crm.bundle.profile;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Ensure the effective time period is correctly configured
 * 
 * @author Kumaran Sivsaubramaniam
 */
public class EffectivePeriodValidator implements Validator
{

    /**
     * @param ctx
     * @param obj
     * @throws IllegalStateException
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        BundleProfile bundle = (BundleProfile) obj;
        try
        {
            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
                    && ((bundle.getStartDate() == null) || (bundle.getEndDate() == null))
                    && !bundle.isSecondaryBalance(ctx))
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME,
                        "Both Start and End must be defined for One off fixed time"));
            }
            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL)
                    || bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL))
            {
                if (bundle.getValidity() < 0)
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.VALIDITY,
                            "Validity has to be greater than 0"));
                }
            }
            
            if (bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.MULTIMONTHLY))
            {
            	if (bundle.getRecurringStartInterval() != DurationTypeEnum.MONTH_INDEX)
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRING_START_INTERVAL,
                    		"For Multi-Monthly Bundles, Recurrence Interval has to be Months."));
                }
            }
            
            if (bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.MULTIDAY))
            {
            	if (bundle.getRecurringStartInterval() != DurationTypeEnum.DAY_INDEX)
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRING_START_INTERVAL,
                    		"For Multi-Day Bundles, Recurrence Interval has to be Days."));
                }
            }
            
            if (bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.MULTIMONTHLY))
            {
                if (bundle.getRecurringStartValidity() < 2)
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRING_START_VALIDITY,
                    		"For Multi-Monthly Bundles Recurrence Number of Units has to be more than 1"));
                }
            }
            
            if (bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.MULTIDAY))
            {
                if (bundle.getRecurringStartValidity() < 1)
                {
                    el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRING_START_VALIDITY,
                    		"For Multi-Day Bundles Recurrence Number of Units has to be more than 0"));
                }
            }
            
        }
        catch(HomeException e)
        {
        	el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME, "Error confirming " +
        			"whether the bundle category unit type is Secondary Balance. Message:" + e.getMessage()));
        }
        finally
        {
            el.throwAll();
        }
    }
}
