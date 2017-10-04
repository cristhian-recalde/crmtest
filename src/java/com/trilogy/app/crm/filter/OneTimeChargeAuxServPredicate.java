/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

import com.trilogy.app.crm.bean.AuxiliaryBundleSelection;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.ServicePeriodEnum;

/**
 * Returns TRUE if the bean has a charge type of One Time.
 * 
 * TODO: Extract an ChargingModeTypeEnum interface and make this more generic.
 * TODO: Make the charge type that we're looking for configurable.
 * 
 * @author angie.li@redknee.com
 */
public class OneTimeChargeAuxServPredicate implements Predicate 
{
    public boolean f(Context ctx, Object obj) throws AbortVisitException 
    {
        ServicePeriodEnum chargingMode = getChargingMode(obj);

        if (chargingMode == null)
        {
            chargingMode = getChargingMode(ctx.get(AbstractWebControl.BEAN));
        }

        return ServicePeriodEnum.ONE_TIME.equals(chargingMode);
    }

    public ServicePeriodEnum getChargingMode(Object bean)
    {
        ServicePeriodEnum chargingMode = null;
        if (bean instanceof AuxiliaryServiceSelection)
        {
            chargingMode = ((AuxiliaryServiceSelection) bean).getChargingModeType();
        }
        if (bean instanceof AuxiliaryBundleSelection)
        {
            chargingMode = ((AuxiliaryBundleSelection) bean).getChargingModeType();
        }
        else if (bean instanceof AuxiliaryService)
        {
            chargingMode = ((AuxiliaryService) bean).getChargingModeType();
        }
        return chargingMode;
    }
}
