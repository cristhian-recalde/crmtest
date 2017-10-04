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

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.service.AnnualPeriodHandler;
import com.trilogy.app.crm.service.DailyPeriodHandler;
import com.trilogy.app.crm.service.MonthlyPeriodHandler;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.MultiMonthlyPeriodHandler;
import com.trilogy.app.crm.service.OneTimePeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.service.WeeklyPeriodHandler;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class CRMServicePeriodSupport extends DefaultServicePeriodSupport
{
    protected static ServicePeriodSupport instance_ = null;
    public static ServicePeriodSupport instance()
    {
        if (instance_ == null)
        {
            instance_ = new CRMServicePeriodSupport();
        }
        return instance_;
    }

    protected CRMServicePeriodSupport()
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
        case ServicePeriodEnum.ANNUAL_INDEX:
            return AnnualPeriodHandler.instance();
            
        case ServicePeriodEnum.MULTIMONTHLY_INDEX:
            return MultiMonthlyPeriodHandler.instance();
            
        case ServicePeriodEnum.MONTHLY_INDEX:
            return MonthlyPeriodHandler.instance();

        case ServicePeriodEnum.WEEKLY_INDEX:
            return WeeklyPeriodHandler.instance();

        case ServicePeriodEnum.ONE_TIME_INDEX:
            return OneTimePeriodHandler.instance();

        case ServicePeriodEnum.DAILY_INDEX:
            return DailyPeriodHandler.instance();
            
        case ServicePeriodEnum.MULTIDAY_INDEX:
            return MultiDayPeriodHandler.instance();
        }
        
        return null;
    }
}
