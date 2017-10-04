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
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.service.AnnualPeriodHandler;
import com.trilogy.app.crm.service.DailyPeriodHandler;
import com.trilogy.app.crm.service.MonthlyPeriodHandler;
import com.trilogy.app.crm.service.MultiMonthlyPeriodHandler;
import com.trilogy.app.crm.service.OneTimePeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.service.WeeklyPeriodHandler;


/**
 * This is a custom version of the service period support class for bill cycle change.
 * 
 * It is needed because we don't want any charging logic to go looking in the old bill
 * cycle for past charges.  We want to do prorated charges for the time between the
 * old bill cycle end date and the new bill cycle start date.
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class MoveServicePeriodSupport extends CRMServicePeriodSupport
{
    protected static ServicePeriodSupport instance_ = null;
    public static ServicePeriodSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new MoveServicePeriodSupport();
        }
        return instance_;
    }

    protected MoveServicePeriodSupport()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServicePeriodHandler getHandler(ServicePeriodEnum servicePeriod)
    {
        switch (servicePeriod.getIndex())
        {
        case ServicePeriodEnum.DAILY_INDEX:
            return new DailyPeriodHandler()
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public double calculateRefundRate(final Context context, final Date billingDate, final int billingCycleDay, final int spid, final String subscriberId, final Object item)
                {
                    return -1 * calculateRate(context, billingDate, billingCycleDay, spid);
                }
                
            };
        }
        
        return super.getHandler(servicePeriod);
    }
}
