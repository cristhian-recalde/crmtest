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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.TechnologyTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.AuxiliaryServiceStateEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.CallingGroupTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryServiceReference;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryServiceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;

/**
 * Adapts ServicePackage object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class AuxiliaryServiceToApiReferenceAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptAuxiliaryServiceToReference(ctx, (AuxiliaryService) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static AuxiliaryServiceReference adaptAuxiliaryServiceToReference(Context ctx, final AuxiliaryService crmAuxService)
    {
        final AuxiliaryServiceReference apiAuxServiceRef = new AuxiliaryServiceReference();
        adaptAuxiliaryServiceToReference(ctx, crmAuxService, apiAuxServiceRef);
        return apiAuxServiceRef;
    }

    public static AuxiliaryServiceReference adaptAuxiliaryServiceToReference(Context ctx, final AuxiliaryService crmAuxService,
            final AuxiliaryServiceReference apiAuxServiceRef)
    {
        apiAuxServiceRef.setIdentifier(crmAuxService.getIdentifier());
        apiAuxServiceRef.setSpid(crmAuxService.getSpid());
        apiAuxServiceRef.setName(crmAuxService.getName());
        apiAuxServiceRef.setType(AuxiliaryServiceTypeEnum.valueOf(crmAuxService.getType().getIndex()));
        apiAuxServiceRef.setTechnology(TechnologyTypeEnum.valueOf(crmAuxService.getTechnology().getIndex()));

        long callingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
        com.redknee.app.crm.bean.CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroupIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
            callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
        }
        apiAuxServiceRef.setCallingGroupID(callingGroupIdentifier);
        apiAuxServiceRef.setCallingGroupType(CallingGroupTypeEnum.valueOf(callingGroupType.getIndex()));

        apiAuxServiceRef.setFee(crmAuxService.getCharge());
        apiAuxServiceRef.setAdjustmentTypeID(Long.valueOf(crmAuxService.getAdjustmentType()));
        apiAuxServiceRef.setPaidType(RmiApiSupport.convertCrmSubscriberPaidType2Api(crmAuxService.getSubscriberType()));
        apiAuxServiceRef.setState(AuxiliaryServiceStateEnum.valueOf(crmAuxService.getState().getIndex()));
        apiAuxServiceRef.setPeriod(com.redknee.app.crm.api.rmi.support.RmiApiSupport.convertCrmServicePeriodEnum2ApiServicePeriodType(crmAuxService.getChargingModeType()));
        final boolean isAMsisdn = crmAuxService.getType() == com.redknee.app.crm.bean.AuxiliaryServiceTypeEnum.AdditionalMsisdn;
        apiAuxServiceRef.setRequiresAdditionalMobileNumber(isAMsisdn);
        
        long serviceOption = URCSPromotionAuxSvcExtension.DEFAULT_SERVICEOPTION;
        URCSPromotionAuxSvcExtension urcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, URCSPromotionAuxSvcExtension.class);
        if (urcsPromotionAuxSvcExtension!=null)
        {
            serviceOption = urcsPromotionAuxSvcExtension.getServiceOption();
        }
        
        apiAuxServiceRef.setServiceOption(serviceOption);
        RecurrenceScheme apiRecScheme = new RecurrenceScheme();
        switch (crmAuxService.getChargingModeType().getIndex())
        {
        case com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(1));
            // Set period unit type to monthly
            apiRecScheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());         
            break;
        case com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY_INDEX:
            apiRecScheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            apiRecScheme.setPeriod(Long.valueOf(crmAuxService.getRecurrenceInterval()));
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
            apiRecScheme.setPeriod(Long.valueOf(crmAuxService.getRecurrenceInterval()));
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
            if (OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE.getIndex() == crmAuxService.getRecurrenceType().getIndex())
            {
                apiRecScheme.setStartDate(crmAuxService.getStartDate());
                apiRecScheme.setEndDate(crmAuxService.getEndDate());                
            }
            else if (OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL.getIndex() == crmAuxService.getRecurrenceType().getIndex())
            {
                apiRecScheme.setPeriod(Long.valueOf(crmAuxService.getValidity()));
                if(crmAuxService.getFixedInterval().getIndex() == FixedIntervalTypeEnum.MONTHS_INDEX)
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
        apiAuxServiceRef.setRecurrence(apiRecScheme);        
        return apiAuxServiceRef;
    }
}
