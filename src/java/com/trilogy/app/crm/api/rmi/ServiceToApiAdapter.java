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

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ActivationFeeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.Service;


/**
 * Adapts Service object to API objects.
 * 
 * @author bhupendra.pandey@redknee.com
 */
public class ServiceToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptServiceToApi(ctx, (com.redknee.app.crm.bean.Service) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }
    
    public static com.redknee.app.crm.bean.Service adaptApiToService(final Context ctx,
            final Service apiService) throws Exception
    {
        com.redknee.app.crm.bean.Service crmService = null;
        try
        {
            crmService = (com.redknee.app.crm.bean.Service) XBeans.instantiate(com.redknee.app.crm.bean.Service.class,
                    ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(ServiceToApiAdapter.class,
                    "Error instantiating new com.redknee.app.crm.bean.Service.  Using default constructor.", e)
                    .log(ctx);
            crmService = new com.redknee.app.crm.bean.Service();
        }
        adaptApiToService(ctx, apiService, crmService);
        return crmService;
    }

    public static com.redknee.app.crm.bean.Service adaptApiToService(final Context ctx,
            final Service apiService, com.redknee.app.crm.bean.Service crmService) throws Exception
    {        
        if (apiService.getActivationFeeType() != null)
        {
            crmService
                    .setActivationFee(ActivationFeeModeEnum.get((short) apiService.getActivationFeeType().getValue()));
        }
        if (apiService.getEnableCLTC() != null)
        {
            crmService.setEnableCLTC(apiService.getEnableCLTC());
        }
        if (apiService.getExecutionOrder() != null)
        {
            crmService.setExecutionOrder(apiService.getExecutionOrder());
        }
        if (apiService.getName() != null)
        {
            crmService.setName(apiService.getName());
        }
        RecurrenceScheme apiRecScheme = apiService.getRecurrence();
        if (apiRecScheme != null && apiRecScheme.getRecurrenceType() != null)
        {
            if (RecurrenceTypeEnum.ONE_TIME.getValue().getValue() == apiRecScheme.getRecurrenceType().getValue())
            {
                crmService.setChargeScheme(ServicePeriodEnum.ONE_TIME);
                if (apiRecScheme.getStartDate() == null && apiRecScheme.getEndDate() == null)
                {
                    if (apiRecScheme.getPeriodUnitType() == null)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service.recurrenceScheme.periodUnitType",
                            "Mandatory field. NULL value not allowed.");
                    }
                    if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service.recurrenceScheme.period",
                                "The value should be at least 1.");
                    }
                    crmService.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL);
                    crmService.setValidity(apiRecScheme.getPeriod().intValue());
                    if (apiRecScheme.getPeriodUnitType().getValue() == com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.MONTHLY
                            .getValue().getValue())
                    {
                        crmService.setFixedInterval(FixedIntervalTypeEnum.MONTHS);
                    }
                    else if (apiRecScheme.getPeriodUnitType().getValue() == com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.DAILY
                            .getValue().getValue())
                    {
                        crmService.setFixedInterval(FixedIntervalTypeEnum.DAYS);
                    }
                    else
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service",
                                "Only, Daily And Monthly PeriodUnitType allowed for ONE_OFF_FIXED_INTERVAL Service "
                                        + apiService.getIdentifier() + " .");
                    }
                }
                else
                {
                    crmService.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE);
                    crmService.setEndDate(apiRecScheme.getEndDate());
                    crmService.setStartDate(apiRecScheme.getStartDate());
                }
            }
            else
            {
                if (apiRecScheme.getPeriodUnitType() == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("service.recurrenceScheme.periodUnitType",
                        "Mandatory field. NULL value not allowed.");
                }
                if (com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.ANNUAL
                        .getValue().getValue() == apiRecScheme.getPeriodUnitType().getValue())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("service", "Annual PeriodUnitType is not allowed for Service ["
                                    + apiService.getIdentifier() + "].");
                }
                else
                {
                    if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("service.recurrenceScheme.period",
                                "The value should be at least 1.");
                    }
                    ServicePeriodEnum crmServicePeiod = RmiApiSupport.convertApiServicePeriodType2CrmServicePeriodEnum(
                            apiRecScheme.getPeriodUnitType(), apiRecScheme.getPeriod());
                    
                    crmService.setChargeScheme(crmServicePeiod);
                    
                    if (ServicePeriodEnum.MULTIMONTHLY.equals(crmServicePeiod) || ServicePeriodEnum.MULTIDAY.equals(crmServicePeiod))
                    {
                        crmService.setRecurrenceInterval(apiRecScheme.getPeriod().intValue());
                    }
                }
            }
        }
        if (apiService.getReprovisionOnActive() != null)
        {
            crmService.setReprovisionOnActive(apiService.getReprovisionOnActive());
        }
        if (apiService.getSmartSuspension() != null)
        {
            crmService.setSmartSuspension(apiService.getSmartSuspension());
        }
        if (apiService.getSpid() != null)
        {
            crmService.setSpid(apiService.getSpid());
        }
        if (apiService.getTechnology() != null)
        {
            crmService.setTechnology(TechnologyEnum.get((short) apiService.getTechnology().getValue()));
        }
        if (apiService.getType() != null)
        {
            crmService.setType(com.redknee.app.crm.bean.ServiceTypeEnum.get((short) apiService.getType().getValue()));
        }
        if (apiService.getSubscriptionType() != null)
        {
            crmService.setSubscriptionType(apiService.getSubscriptionType());
        }
        return crmService;
    }
    
    
    public static com.redknee.app.crm.bean.Service adaptGenericParametersToCreateService(
            GenericParameter[] apiGenericParameter, com.redknee.app.crm.bean.Service crmService)
    {        
        Object obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ADJUSTMENTTYPEDESCRIPTION,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setAdjustmentTypeDesc((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ALLOWCARRYOVER, apiGenericParameter);
        if (obj != null)
        {
            crmService.setAllowCarryOver((Boolean) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidProvisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDRESUMEHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidResumeConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDSUSPENDHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidSuspendConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidUnprovisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setProvisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setUnprovisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDSUSPENDHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setSuspendConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDRESUMEHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setResumeConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SPGSERVICETYPE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setSPGServiceType((Long) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_TAXAUTHORITY, apiGenericParameter);
        if (obj != null)
        {
            crmService.setTaxAuthority(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VMPLANID, apiGenericParameter);
        if (obj != null)
        {
            crmService.setVmPlanId((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_XMLPROVSVCTYPE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setXmlProvSvcType((Long) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_GLCODE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setAdjustmentGLCode((String) obj);
        }
        obj = RmiApiSupport
                .getGenericParameterValue(Constants.GENERICPARAMETER_INVOICEDESCRIPTION, apiGenericParameter);
        if (obj != null)
        {
            crmService.setAdjustmentInvoiceDesc((String) obj);
        }
        obj = RmiApiSupport
                .getGenericParameterValue(Constants.EXTERNAL_SERVICE_CODE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setExternalServiceCode((String) obj);
        }
        
        crmService.setAdjustmentTypeName(crmService.getAdjustmentTypeDesc());
        return crmService;
    }
    
    public static com.redknee.app.crm.bean.Service adaptGenericParametersToUpdateService(
            GenericParameter[] apiGenericParameter, com.redknee.app.crm.bean.Service crmService)
    {
        Object obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ALLOWCARRYOVER,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setAllowCarryOver((Boolean) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidProvisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDRESUMEHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidResumeConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDSUSPENDHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidSuspendConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setPrepaidUnprovisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setProvisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setUnprovisionConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDSUSPENDHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setSuspendConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDRESUMEHLRCOMMAND,
                apiGenericParameter);
        if (obj != null)
        {
            crmService.setResumeConfigs((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SPGSERVICETYPE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setSPGServiceType((Long) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VMPLANID, apiGenericParameter);
        if (obj != null)
        {
            crmService.setVmPlanId((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_XMLPROVSVCTYPE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setXmlProvSvcType((Long) obj);
        }        
        obj = RmiApiSupport.getGenericParameterValue(Constants.EXTERNAL_SERVICE_CODE, apiGenericParameter);
        if (obj != null)
        {
            crmService.setExternalServiceCode((String) obj);
        }    
        return crmService;
    }

    public static Service adaptServiceToApi(final Context ctx,
            final com.redknee.app.crm.bean.Service crmService) throws HomeException
    {       
        Service apiService = new Service();
        ServiceToApiReferenceAdapter.adaptServiceToReference(ctx, crmService, apiService);
        apiService.setActivationFeeType(ActivationFeeTypeEnum.valueOf(crmService.getActivationFee().getIndex()));
        apiService.setEnableCLTC(crmService.getEnableCLTC());
        apiService.setExecutionOrder(crmService.getExecutionOrder());        
        apiService.setReprovisionOnActive(crmService.getReprovisionOnActive());
        apiService.setSmartSuspension(crmService.getSmartSuspension());        
        apiService.setAdjustmentTypeID(Long.valueOf(crmService.getAdjustmentType()));
        List<GenericParameter> list = new ArrayList<GenericParameter>();
        
        
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_ALLOWCARRYOVER, crmService.getAllowCarryOver()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND, crmService.getProvisionConfigs()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_POSTPAIDRESUMEHLRCOMMAND, crmService.getResumeConfigs()));
        list.add( RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_POSTPAIDSUSPENDHLRCOMMAND, crmService.getSuspendConfigs()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND, crmService.getUnprovisionConfigs()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND, crmService.getPrepaidProvisionConfigs()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_PREPAIDRESUMEHLRCOMMAND, crmService.getPrepaidResumeConfigs()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_PREPAIDSUSPENDHLRCOMMAND, crmService.getPrepaidSuspendConfigs()));
        list.add( RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND, crmService.getPrepaidUnprovisionConfigs()));
        list.add( RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_SPGSERVICETYPE, crmService.getSPGServiceType()));        
        list.add( RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_VMPLANID, crmService.getVmPlanId()));
        list.add(RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_XMLPROVSVCTYPE, crmService.getXmlProvSvcType()));
        list.add( RmiApiSupport.createGenericParameter(Constants.EXTERNAL_SERVICE_CODE, crmService.getExternalServiceCode()));
        
        if (crmService instanceof ExtensionAware)
        {
            APIGenericParameterSupport.getBlacklistWhitelistTemplateServiceCUGParameters(ctx, crmService, list);
        }
        apiService.setParameters(list.toArray(new GenericParameter[list.size()]));
        return apiService;
    }
}
