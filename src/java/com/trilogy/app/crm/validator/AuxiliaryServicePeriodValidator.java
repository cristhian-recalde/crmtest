package com.trilogy.app.crm.validator;

import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * Ensure the one time Auxiliary Service time period is correctly configured
 * 
 * @author Sajid
 * @since 9_4
 */
public class AuxiliaryServicePeriodValidator implements Validator
{

    /**
     * @param ctx
     * @param obj
     * @throws IllegalStateException
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        AuxiliaryService auxiliaryService = (AuxiliaryService) obj;
        try
        {
            if (ServicePeriodEnum.ONE_TIME.equals(auxiliaryService.getChargingModeType())
                    && auxiliaryService.getRecurrenceType().equals(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE)
                    && ((auxiliaryService.getStartDate() == null) || (auxiliaryService.getEndDate() == null)))
            {
                el.thrown(new IllegalPropertyArgumentException(AuxiliaryServiceXInfo.RECURRENCE_TYPE,
                        "Both START DATE and END DATE must be defined for One off fixed date range Auxiliary Service"));
            }
        }
        finally
        {
            el.throwAll();
        }
    }
}
