package com.trilogy.app.crm.bundle.profile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.BundleProfileXInfo;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Ensure the Recurrence Scheme is correctly configured
 * 
 * @author sajid.memon@redknee.com, mangaraj.sahoo@redknee.com
 * @since 9.3
 */
public class RecurrenceSchemeValidator implements Validator
{
    
    private static final Collection<ServicePeriodEnum> FIXED_INTERVAL_SCHEMES = Collections
            .unmodifiableCollection(Arrays.asList(ServicePeriodEnum.MULTIMONTHLY, ServicePeriodEnum.MULTIDAY,
                    ServicePeriodEnum.DAILY));

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
            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.RECUR_CYCLE_FIXED_DATETIME)  
            		&& !(bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.MONTHLY)))
                    
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME,
                        "Recurring (Fixed Date/Time), should map to Monthly Charging Recurrence Scheme"));
            }
            
            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL) 
                    && !FIXED_INTERVAL_SCHEMES.contains(bundle.getChargingRecurrenceScheme()))
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME,
                        "Recurring (Fixed Interval) should map to Multi-Monthly, Multi-Day or Daily Charging Recurrence Scheme"));
            }
            
            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE)  
            		&& !(bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.ONE_TIME)))
                    
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME,
                        "One off fixed date range, should map to One Time Charging Recurrence Scheme"));
            }

            if (bundle.getRecurrenceScheme().equals(RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL)  
            		&& !(bundle.getChargingRecurrenceScheme().equals(ServicePeriodEnum.ONE_TIME)))
                    
            {
                el.thrown(new IllegalPropertyArgumentException(BundleProfileXInfo.RECURRENCE_SCHEME,
                        "One off fixed interval, should map to One Time Charging Recurrence Scheme"));
            }
            
        }
        finally
        {
            el.throwAll();
        }
    }
}
