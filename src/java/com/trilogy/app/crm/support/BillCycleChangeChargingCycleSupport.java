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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.service.MonthlyPeriodHandler;


/**
 * This is a custom version of the charging cycle support class for bill cycle change.
 * 
 * It is needed because we don't want any charging logic to go looking in the old bill
 * cycle for past charges.  We want to do prorated charges for the time between the
 * old bill cycle end date and the new bill cycle start date.
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleChangeChargingCycleSupport extends CRMChargingCycleSupport
{
    protected static ChargingCycleSupport instance_ = null;
    public static ChargingCycleSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new BillCycleChangeChargingCycleSupport();
        }
        return instance_;
    }

    protected BillCycleChangeChargingCycleSupport()
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
        case ChargingCycleEnum.MONTHLY_INDEX:
            return new MonthlyPeriodHandler()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public Date calculateCycleStartDate(Context context, Date billingDate, int billingCycleDay, int spid,
                        String subscriberId, Object item)
                {
                    return CalendarSupportHelper.get(context).getRunningDate(context);
                }    
            };
        }
        
        return super.getHandler(chargingCycle);
    }
}
