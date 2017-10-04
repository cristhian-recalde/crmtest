/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceTypeEnum;


/**
 * Adapts Service object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class ServiceToApiReferenceAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptServiceToReference(ctx, (com.redknee.app.crm.bean.Service) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static ServiceReference adaptServiceToReference(final Context ctx, final com.redknee.app.crm.bean.Service crmService)
            throws HomeException
    {
        ServiceReference apiServiceRef = new ServiceReference();
        adaptServiceToReference(ctx, crmService, apiServiceRef);
        return apiServiceRef;
    }
    

    public static ServiceReference adaptServiceToReference(Context ctx, final Service crmService,
            final ServiceReference apiServiceRef)
    {
        apiServiceRef.setIdentifier(crmService.getIdentifier());
        apiServiceRef.setName(crmService.getName());
        apiServiceRef.setSpid(crmService.getSpid());
        apiServiceRef.setSubscriptionType((int) crmService.getSubscriptionType());
        apiServiceRef.setTechnology(TechnologyTypeEnum.valueOf(crmService.getTechnology().getIndex()));
        apiServiceRef.setType(ServiceTypeEnum.valueOf(crmService.getType().getIndex()));
        RecurrenceScheme apiRecScheme = new RecurrenceScheme();
        switch (crmService.getChargeScheme().getIndex())
        {
        case com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(1));
            // Set period unit type to monthly
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(crmService.getRecurrenceInterval()));
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.DAILY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(1));
            // Set period unit type to daily
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(crmService.getRecurrenceInterval()));
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.WEEKLY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(1));
            // set period unit type to weekly
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.WEEKLY.getValue());
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.ONE_TIME.getValue());
            if (OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE.getIndex() == crmService.getRecurrenceType().getIndex())
            {
                apiRecScheme.setStartDate(crmService.getStartDate());
                apiRecScheme.setEndDate(crmService.getEndDate());
            }
            else if (OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL.getIndex() == crmService.getRecurrenceType().getIndex())
            {
                apiRecScheme.setPeriod(Long.valueOf(crmService.getValidity()));
                if(crmService.getFixedInterval().getIndex() == FixedIntervalTypeEnum.MONTHS_INDEX)
                {
                    apiRecScheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());                    
                }
                else
                {
                    apiRecScheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());                     
                }                
            }            
            break;
        }
        apiServiceRef.setRecurrence(apiRecScheme);
        return apiServiceRef;
    }
}
