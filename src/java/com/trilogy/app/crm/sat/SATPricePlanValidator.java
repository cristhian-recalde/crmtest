/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.sat;

import com.trilogy.app.crm.bas.tps.CreationPreferenceEnum;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.priceplan.PricePlanEnabledValidator;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * @author Aaron Gourley
 * @since 
 *
 */
public class SATPricePlanValidator implements Validator
{
    private static SATPricePlanValidator instance_ = null;
    
    public static SATPricePlanValidator instance()
    {
        if( instance_ == null )
        {
            instance_ = new SATPricePlanValidator();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object bean) throws IllegalStateException
    {
        if( bean instanceof ServiceActivationTemplate )
        {
            ServiceActivationTemplate template = (ServiceActivationTemplate) bean;
            // TT7031346182 - According to ITP (Obj ID :5571) => PricePlan can not be selected for a SCT if the PricePlan is not enabled.
            if (template.getPricePlanPreference().equals(CreationPreferenceEnum.MANDATORY))
            {
                PricePlanEnabledValidator.instance().validate(ctx, template.getPricePlan());
            }
            
            if (template.getSecondaryPricePlanPreference().equals(CreationPreferenceEnum.MANDATORY))
            {
                PricePlanEnabledValidator.instance().validate(ctx, template.getSecondaryPricePlan());
            }
        }
    }

}
