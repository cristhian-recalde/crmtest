package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;


public class MultiMonthRecurrenceIntervalValidator implements Validator
{
    protected static Validator instance_ = null;
    private static int MIN_RECURRENCEINTERVAL_VALUE = 2;
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new MultiMonthRecurrenceIntervalValidator();
        }
        return instance_;
    }
    
    protected MultiMonthRecurrenceIntervalValidator()
    {
    }
    
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        if (obj instanceof Service)
        {
            Service svc = (Service) obj;
            validate(ctx, svc.getChargeScheme(), svc.getRecurrenceInterval(), ServiceXInfo.RECURRENCE_INTERVAL);
        }
        else if (obj instanceof AuxiliaryService)
        {
            AuxiliaryService svc = (AuxiliaryService) obj;
            validate(ctx, svc.getChargingModeType(), svc.getRecurrenceInterval(), AuxiliaryServiceXInfo.RECURRENCE_INTERVAL);
        }
    }    
    
    
    private void validate(Context ctx, ServicePeriodEnum servicePeriod, int recurrenceInterval, PropertyInfo xInfo) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        if (ServicePeriodEnum.MULTIMONTHLY.equals(servicePeriod) && recurrenceInterval < MIN_RECURRENCEINTERVAL_VALUE)
        {
            cise.thrown(new IllegalPropertyArgumentException(xInfo, "Recurrence interval should be greater than " + (MIN_RECURRENCEINTERVAL_VALUE-1)));
        }
        cise.throwAll();
    }

    
}
