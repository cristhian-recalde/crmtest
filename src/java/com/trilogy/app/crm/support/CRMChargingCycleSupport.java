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
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.OneTimePeriodHandler;
import com.trilogy.app.crm.service.WeeklyPeriodHandler;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMChargingCycleSupport extends DefaultChargingCycleSupport
{
    protected static ChargingCycleSupport instance_ = null;
    public static ChargingCycleSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMChargingCycleSupport();
        }
        return instance_;
    }

    protected CRMChargingCycleSupport()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChargingCycleHandler getHandler(ChargingCycleEnum chargingCycle)
    {
        switch (chargingCycle.getIndex())
        {
        case ChargingCycleEnum.WEEKLY_INDEX:
            return WeeklyPeriodHandler.instance();

        case ChargingCycleEnum.ONE_TIME_INDEX:
            return OneTimePeriodHandler.instance();
            
        case ChargingCycleEnum.MULTIDAY_INDEX:
            return MultiDayPeriodHandler.instance();
        }
        
        return super.getHandler(chargingCycle);
    }
}
