package com.trilogy.app.crm.validator;

import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.ui.ServiceXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * Ensure the one time service time period is correctly configured
 * 
 * @author Marcio Marques
 * @since 8_8/9_0
 */
public class ServicePeriodValidator implements Validator
{

    /**
     * @param ctx
     * @param obj
     * @throws IllegalStateException
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        Service service = (Service) obj;
        try
        {
            if (ServicePeriodEnum.ONE_TIME.equals(service.getChargeScheme())
                    && service.getRecurrenceType().equals(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
                    && ((service.getStartDate() == null) || (service.getEndDate() == null)))
            {
                el.thrown(new IllegalPropertyArgumentException(ServiceXInfo.RECURRENCE_TYPE,
                        "Both START DATE and END DATE must be defined for One off fixed date range services"));
            }
        }
        finally
        {
            el.throwAll();
        }
    }
}
