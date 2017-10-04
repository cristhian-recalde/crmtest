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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.logger.Logger;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionStateTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;

/**
 * Adapts Service PricePlan to  object to API objects.
 *
 * @author kumaran.sivasubramanaiam@redknee.com
 */
public class PricePlanServiceToPricePlanOptionAdapter implements Adapter
{
    PricePlanServiceToPricePlanOptionAdapter()
    {
        
    }
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return convertServiceToPricePlanOption(ctx, (ServiceFee2) obj, new HashMap());
    }
    
    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static PricePlanOption[] getSubscriberServicesConvertedPricePlanOption(Context ctx, final Subscriber sub) throws HomeException
    {
        
        final Map<ServiceFee2ID, SubscriberServices> services = SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx,
                sub.getId());
        final Map<Long, ServiceFee2> serviceFees = SubscriberServicesSupport.getServiceFeesWithSource(ctx, sub);
        List<PricePlanOption> options = new ArrayList<PricePlanOption>();
        
        for (final Iterator iter = serviceFees.values().iterator(); iter.hasNext();)
        {
            final ServiceFee2 fee = (ServiceFee2) iter.next();
            final PricePlanOption bean = convertServiceToPricePlanOption(ctx,fee, services);
            options.add(bean);
        }
        return options.toArray(new PricePlanOption[]{});
    }
    
    

    public static PricePlanOption[] getServicesConvertedPricePlan(Context ctx, Collection<ServiceFee2> feeList) throws HomeException
    {
        List<PricePlanOption> options = new ArrayList<PricePlanOption>();
        Iterator<ServiceFee2> iter = feeList.iterator();
        while (iter.hasNext())
        {
            ServiceFee2 fee = iter.next();
            PricePlanOption planOption = convertServiceToPricePlanOption(ctx, fee, Collections.EMPTY_MAP);
            options.add(planOption);
        }
        return options.toArray(new PricePlanOption[]{});
    }

    private static PricePlanOption convertServiceToPricePlanOption(Context ctx, ServiceFee2 fee, Map<ServiceFee2ID, SubscriberServices> selectedServices) throws HomeException
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption planOption;
        planOption = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption();
        Service service = fee.getService(ctx);
        
        if (service == null)
        {
            throw new HomeException(" Service " + fee.getServiceId() + " does not exist");
        }
        
        planOption.setOptionType(PricePlanOptionTypeEnum.SERVICE.getValue());
        planOption.setOptionState(PricePlanOptionStateTypeEnum.ACTIVE.getValue());
        planOption.setIdentifier(service.getIdentifier());
        planOption.setName(service.getName());
        planOption.setAdjustmentTypeID(Long.valueOf(service.getAdjustmentType()));
        planOption.setProvisioningState(ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue());
        planOption.setIsSelected(false);
        planOption.setProrationEnabled(ActivationFeeModeEnum.PRORATE.equals(service.getActivationFee()));
        planOption.setFee(fee.getFee());
        RecurrenceScheme scheme = new RecurrenceScheme();
        final Date today = new Date();
               
        planOption.setStartDate(today);
        planOption.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, planOption.getStartDate()));

        if (com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME.equals(service.getChargeScheme()))
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.ONE_TIME.getValue());
            if (OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE.equals(service.getRecurrenceType()))
            {
                if (!selectedServices.containsKey(new ServiceFee2ID(fee.getServiceId(), fee.getPath())))
                {
                    scheme.setStartDate(today);
                    scheme.setEndDate(service.getEndDate());
                	planOption.setEndDate(service.getEndDate());
                }
                else
                {
                	scheme.setEndDate(CalendarSupportHelper.get(ctx).findDateDaysAfter(service.getValidity(),
                			today));
                	planOption.setEndDate(CalendarSupportHelper.get(ctx).findDateDaysAfter(service.getValidity(),
                			today));
                }
            }
            else
            {
                // Fixed interval
                scheme.setPeriod(Long.valueOf(service.getValidity()));
                if(service.getFixedInterval().getIndex() == FixedIntervalTypeEnum.MONTHS_INDEX)
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());  
                    planOption.setEndDate(CalendarSupportHelper.get(ctx).findDateMonthsAfter(service.getValidity(),
                    		today));
                }
                else
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());           
                    planOption.setEndDate(CalendarSupportHelper.get(ctx).findDateDaysAfter(service.getValidity(),
                    		today));
                }     
            }
        }
        else
        {
            scheme.setRecurrenceType(RecurrenceTypeEnum.RECURRING.getValue());
            if (com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY.equals(service.getChargeScheme()))
            {
                scheme.setPeriod(Long.valueOf(service.getRecurrenceInterval()));
                scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
            }
            else if (com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY.equals(service.getChargeScheme()))
            {
                scheme.setPeriod(Long.valueOf(service.getRecurrenceInterval()));
                scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
            }
            else
            {
                scheme.setPeriod(Long.valueOf(1));
                if (com.redknee.app.crm.bean.ServicePeriodEnum.WEEKLY.equals(service.getChargeScheme()))
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.WEEKLY.getValue());
                }
                else if (com.redknee.app.crm.bean.ServicePeriodEnum.DAILY.equals(service.getChargeScheme()))
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.DAILY.getValue());
                }
                else
                {
                    scheme.setPeriodUnitType(ServicePeriodEnum.MONTHLY.getValue());
                }
            }
        }
        
        Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
 
        final SubscriberServices subService = (SubscriberServices) selectedServices.get(new ServiceFee2ID(fee.getServiceId(), fee.getPath()));
        if (subService != null)
        {
            // previously picked service
            planOption.setIsSelected(true);
            planOption.setStartDate(subService.getStartDate());
            planOption.setEndDate(subService.getEndDate());
            planOption.setProvisioningState(getProvisioningState(ctx,subService.getProvisionedState(), subService.getSuspendReason()));
           
            parametersList.add(APIGenericParameterSupport.getIsPersonalizedFeeApplied(subService.getIsfeePersonalizationApplied()));
            parametersList.add(APIGenericParameterSupport.getPersonalizedFeeParameter(subService.getPersonalizedFee()));
            parametersList.add(APIGenericParameterSupport.getServiceQuantity(subService.getServiceQuantity()));

            if (subService.getNextRecurringChargeDate() != null)
            {
                parametersList.add(APIGenericParameterSupport.getNextRecurringChargeDateParam(subService.getNextRecurringChargeDate()));
            }
		} else {
			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(ctx, PricePlanServiceToPricePlanOptionAdapter.class,
						"##### subService is null ##### fee = " + fee);
			}
		}
        
        planOption.setRecurrence(scheme);
        
        parametersList.add(APIGenericParameterSupport.getPricePlanPreferenceTypeParameter(ctx, fee));
        
        parametersList.add(APIGenericParameterSupport.getServiceTypeParameter(service.getType().getIndex()));
        
        parametersList.add(APIGenericParameterSupport.getApplyMRCGroupParam(fee.isApplyWithinMrcGroup()));
        
        parametersList.add(APIGenericParameterSupport.getIsPrimaryParam(fee.isPrimary()));
        
        parametersList.add(APIGenericParameterSupport.getIsPersonalizedFeeParameter(service.getFeePersonalizationAllowed()));
        
        parametersList.add(APIGenericParameterSupport.getPath(ctx, fee));
        
        if (service instanceof ExtensionAware)
        {
            APIGenericParameterSupport.getBlacklistWhitelistTemplateServiceCUGParameters(ctx, service, (List<GenericParameter>) parametersList);
        }

        planOption.setParameters(parametersList.toArray(new GenericParameter[]{}));
        
        return planOption;
    }
    

    public static com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateType getProvisioningState(Context ctx,
            ServiceStateEnum state, SuspendReasonEnum suspendEnum)
    {
        if (state.equals(ServiceStateEnum.PROVISIONED))
        {
            return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.PROVISIONED.getValue();
        }
        else if (state.equals(ServiceStateEnum.PROVISIONEDWITHERRORS))
        {
            return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.PROVISIONED_WITH_ERRORS.getValue();
        }
        else if (state.equals(ServiceStateEnum.PENDING))
        {
            return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.NOT_PROVISIONED.getValue();
        }
        else if (state.equals(ServiceStateEnum.SUSPENDED))
        {
            if (suspendEnum.equals(SuspendReasonEnum.CLCT))
            {
                return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.SUSPENDED_CLTC.getValue();
            }
            else
            {
                return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.SUSPENDED.getValue();
            }
        }
        else if (state.equals(ServiceStateEnum.SUSPENDEDWITHERRORS))
        {
            if (suspendEnum.equals(SuspendReasonEnum.CLCT))
            {
                return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.SUSPENDED_CLTC_WITH_ERRORS
                        .getValue();
            }
            else
            {
                return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.SUSPENDED_WITH_ERRORS
                        .getValue();
            }
        }
        else if (state.equals(ServiceStateEnum.UNPROVISIONEDWITHERRORS))
        {
            return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.UNPROVISIONED_WITH_ERRORS
                    .getValue();
        }
        return com.redknee.util.crmapi.wsdl.v2_0.types.ProvisioningStateTypeEnum.UNPROVISIONED.getValue();
    }
}
