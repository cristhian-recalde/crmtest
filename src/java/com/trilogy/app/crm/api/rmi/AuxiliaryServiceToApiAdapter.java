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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.FixedIntervalTypeEnum;
import com.trilogy.app.crm.bean.OneTimeTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.DiscountAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.GroupChargingAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.ProvisionableAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.PRBTAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.PromOptOutAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.SPGAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.URCSPromotionAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VPNAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.VoicemailAuxSvcExtension;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ActivationFeeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;


/**
 * Adapts ServicePackage object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class AuxiliaryServiceToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptAuxiliaryServiceToApi(ctx, (AuxiliaryService) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService adaptAuxiliaryServiceToApi(
            final Context ctx, final AuxiliaryService crmAuxService)
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService apiAuxService;
        apiAuxService = new com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService();
        AuxiliaryServiceToApiReferenceAdapter.adaptAuxiliaryServiceToReference(ctx, crmAuxService, apiAuxService);
        apiAuxService.setLastModified(CalendarSupportHelper.get(ctx).dateToCalendar(crmAuxService.getLastModified()));
        apiAuxService.setSmartSuspension(crmAuxService.getSmartSuspension());
        apiAuxService.setActivationFeeType(ActivationFeeTypeEnum.valueOf(crmAuxService.getActivationFee().getIndex()));
        apiAuxService.setPermission(crmAuxService.getPermission());
        GenericParameter[] apiGenericParameters = new GenericParameter[14];
        
        String postpaidProvCmd = ProvisionableAuxSvcExtension.DEFAULT_POSTPAIDPROVCMD;
        String postpaidUnprovCmd = ProvisionableAuxSvcExtension.DEFAULT_POSTPAIDUNPROVCMD;
        String prepaidProvCmd = ProvisionableAuxSvcExtension.DEFAULT_PREPAIDPROVCMD;
        String prepaidUnprovCmd = ProvisionableAuxSvcExtension.DEFAULT_PREPAIDUNPROVCMD;
        boolean provisionOnSuspendDisable = ProvisionableAuxSvcExtension.DEFAULT_PROVONSUSPENDDISABLE;
        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            postpaidProvCmd = provisionableAuxSvcExtension.getPostpaidProvCmd();
            postpaidUnprovCmd = provisionableAuxSvcExtension.getPostpaidUnProvCmd();
            prepaidProvCmd = provisionableAuxSvcExtension.getPrepaidProvCmd();
            prepaidUnprovCmd = provisionableAuxSvcExtension.getPrepaidUnProvCmd();
            provisionOnSuspendDisable = provisionableAuxSvcExtension.isProvOnSuspendDisable();
        }
        apiAuxService.setProvisionOnSuspendOrDisable(provisionOnSuspendDisable);

        long spgServiceType = SPGAuxSvcExtension.DEFAULT_SPGSERVICETYPE;
        SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, SPGAuxSvcExtension.class);
        if (spgAuxSvcExtension!=null)
        {
            spgServiceType = spgAuxSvcExtension.getSPGServiceType();
        }

        String bearerType = AddMsisdnAuxSvcExtension.DEFAULT_BEARERTYPE;
        boolean provisionToECP = AddMsisdnAuxSvcExtension.DEFAULT_PROVISIONTOECP;
        AddMsisdnAuxSvcExtension addMsisdnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, AddMsisdnAuxSvcExtension.class);
        if (addMsisdnAuxSvcExtension!=null)
        {
            bearerType = addMsisdnAuxSvcExtension.getBearerType();
            provisionToECP = addMsisdnAuxSvcExtension.getProvisionToECP();
        }
        
        long vpnPricePlan = VPNAuxSvcExtension.DEFAULT_VPNPRICEPLAN;
        VPNAuxSvcExtension vpnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, VPNAuxSvcExtension.class);
        if (vpnAuxSvcExtension!=null)
        {
            vpnPricePlan = vpnAuxSvcExtension.getVpnPricePlan();
        }
        
        long groupCharge = GroupChargingAuxSvcExtension.DEFAULT_GROUPCHARGE;
        int groupAdjustmentType = GroupChargingAuxSvcExtension.DEFAULT_GROUPADJUSTMENTTYPE;
        GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, GroupChargingAuxSvcExtension.class);
        if (groupChargingAuxSvcExtension!=null)
        {
            groupCharge = groupChargingAuxSvcExtension.getGroupCharge();
            groupAdjustmentType = groupChargingAuxSvcExtension.getGroupAdjustmentType();
        }

        boolean isEnableThreshold = DiscountAuxSvcExtension.DEFAULT_ENABLETHRESHOLD;
        double discountPercentage = DiscountAuxSvcExtension.DEFAULT_DISCOUNTPERCENTAGE;
        long minimumTotalChargeThreshold = DiscountAuxSvcExtension.DEFAULT_MINIMUMTOTALCHARGETHRESHOLD;

        DiscountAuxSvcExtension discountAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, DiscountAuxSvcExtension.class);
        if (discountAuxSvcExtension!=null)
        {
            isEnableThreshold = discountAuxSvcExtension.isEnableThreshold();
            discountPercentage = discountAuxSvcExtension.getDiscountPercentage();
            minimumTotalChargeThreshold = discountAuxSvcExtension.getMinimumTotalChargeThreshold();
        }

        apiGenericParameters[0] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_SPGSERVICETYPE,
                spgServiceType);

        apiGenericParameters[1] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_BEARERTYPE,
                bearerType);
        
        apiGenericParameters[2] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_DISCOUNTPERCENTAGE,
                discountPercentage);
        apiGenericParameters[3] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_ENABLEDISCOUNTTHRESHOLD, isEnableThreshold);
        apiGenericParameters[4] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_VPNPRICEPLAN,
                vpnPricePlan);
        apiGenericParameters[5] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_WARNINGONSUSPENDDISABLE, crmAuxService.getWarningOnSuspendDisable());
        apiGenericParameters[6] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_MINIMUMDISCOUNTTOTALCHARGETHREASHOLD,
                minimumTotalChargeThreshold);
        apiGenericParameters[7] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND, postpaidProvCmd);
        apiGenericParameters[8] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND, postpaidUnprovCmd);
        apiGenericParameters[9] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND, prepaidProvCmd);
        apiGenericParameters[10] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND, prepaidUnprovCmd);
        apiGenericParameters[11] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_VPNGROUPCHARGE,
                groupCharge);
        apiGenericParameters[12] = RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_VPNGROUPADJUSTMENTTYPEID, groupAdjustmentType);
        apiGenericParameters[13] = RmiApiSupport.createGenericParameter(Constants.GENERICPARAMETER_PROVISION_TO_ECP,
                provisionToECP);
        apiAuxService.setParameters(apiGenericParameters);
        return apiAuxService;
    }
    

    private static <T extends AuxiliaryServiceExtension> void addExtension(Context ctx, Collection<Extension> extensions,
            Class<T> extensionClass, AuxiliaryService crmAuxService) throws Exception
    {
        T extension;
        try
        {
            extension = (T) XBeans.instantiate(extensionClass, ctx);
        }
        catch (Throwable t)
        {
            try
            {
                extension = extensionClass.newInstance();
            }
            catch (Throwable e)
            {
                throw new Exception("Unable to instantiate extension of type " + extensionClass.getSimpleName());
            }
        }
        extension.setAuxiliaryServiceId(crmAuxService.getID());
        extension.setSpid(crmAuxService.getSpid());
        extensions.add(extension);
    }


    public static AuxiliaryService adaptApiToAuxiliaryService(Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService apiAuxService)
            throws Exception
    {
        AuxiliaryService crmAuxService = null;
        try
        {
            crmAuxService = (AuxiliaryService) XBeans.instantiate(AuxiliaryService.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(AuxiliaryServiceToApiAdapter.class,
                    "Error instantiating new AuxiliaryService.  Using default constructor.", e).log(ctx);
            crmAuxService = new AuxiliaryService();
        }
        
        adaptApiToAuxiliaryService(ctx, apiAuxService, crmAuxService);

        Collection<Extension> extensions = new ArrayList<Extension>();

        if (AddMsisdnAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, AddMsisdnAuxSvcExtension.class, crmAuxService);
        }
        
        if (SPGAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, SPGAuxSvcExtension.class, crmAuxService);
        }
        
        if (GroupChargingAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, GroupChargingAuxSvcExtension.class, crmAuxService);
        }
        
        if (VoicemailAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, VoicemailAuxSvcExtension.class, crmAuxService);
        }
        
        if (VPNAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, VPNAuxSvcExtension.class, crmAuxService);
        }

        if (URCSPromotionAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, URCSPromotionAuxSvcExtension.class, crmAuxService);
        }

        if (PRBTAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, PRBTAuxSvcExtension.class, crmAuxService);
        }
        
        if (MultiSimAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, MultiSimAuxSvcExtension.class, crmAuxService);
        }

        if (DiscountAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, DiscountAuxSvcExtension.class, crmAuxService);
        }

        if (CallingGroupAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, CallingGroupAuxSvcExtension.class, crmAuxService);
        }

        if (ProvisionableAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, ProvisionableAuxSvcExtension.class, crmAuxService);
        }

        if (PromOptOutAuxSvcExtension.isExtensionValidForType(crmAuxService.getEnumType()))
        {
            addExtension(ctx, extensions, PromOptOutAuxSvcExtension.class, crmAuxService);
        }
        
        crmAuxService.setAuxiliaryServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, extensions));    

        return crmAuxService;
    }


    public static AuxiliaryService adaptApiToAuxiliaryService(Context ctx,
            final com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryService apiAuxService,
            AuxiliaryService crmAuxService) throws Exception
    {
        if (apiAuxService.getActivationFeeType() != null)
        {
            crmAuxService.setActivationFee(ActivationFeeModeEnum.get((short) apiAuxService.getActivationFeeType()
                    .getValue()));
        }
        if (apiAuxService.getFee() != null)
        {
            crmAuxService.setCharge(apiAuxService.getFee());
        }
        if (apiAuxService.getType() != null)
        {
            crmAuxService.setType(com.redknee.app.crm.bean.AuxiliaryServiceTypeEnum.get((short) apiAuxService.getType()
                    .getValue()));
        }
        if (apiAuxService.getTechnology() != null)
        {
            crmAuxService.setTechnology(TechnologyEnum.get((short) apiAuxService.getTechnology().getValue()));
        }
        // state is not required in set call
        if (apiAuxService.getState() != null)
        {
            crmAuxService.setState(com.redknee.app.crm.bean.AuxiliaryServiceStateEnum.get((short) apiAuxService
                    .getState().getValue()));
        }
        try
        {
            if (apiAuxService.getPaidType() != null)
            {
                crmAuxService.setSubscriberType(RmiApiSupport.convertApiPaidType2CrmSubscriberType(apiAuxService
                        .getPaidType()));
            }
        }
        catch (CRMExceptionFault e)
        {
        }
        if (apiAuxService.getLastModified() != null)
        {
            crmAuxService.setLastModified(apiAuxService.getLastModified().getTime());
        }
        if (apiAuxService.getName() != null)
        {
            crmAuxService.setName(apiAuxService.getName());
        }
        if (apiAuxService.getPermission() != null)
        {
            crmAuxService.setPermission(apiAuxService.getPermission());
        }
        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            if (apiAuxService.getProvisionOnSuspendOrDisable() != null)
            {
                provisionableAuxSvcExtension.setProvOnSuspendDisable(apiAuxService.getProvisionOnSuspendOrDisable());
            }
        }

        URCSPromotionAuxSvcExtension urcsPromotionAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, URCSPromotionAuxSvcExtension.class);
        if (urcsPromotionAuxSvcExtension!=null)
        {
            if (apiAuxService.getServiceOption() != null)
            {
                urcsPromotionAuxSvcExtension.setServiceOption(apiAuxService.getServiceOption());
            }
        }
        
        if (apiAuxService.getSmartSuspension() != null)
        {
            crmAuxService.setSmartSuspension(apiAuxService.getSmartSuspension());
        }
        if (apiAuxService.getSpid() != null)
        {
            crmAuxService.setSpid(apiAuxService.getSpid());
        }

        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            if (apiAuxService.getCallingGroupID() != null)
            {
                callingGroupAuxSvcExtension.setCallingGroupIdentifier(apiAuxService.getCallingGroupID());
            }
            if (apiAuxService.getCallingGroupID() != null)
            {
                callingGroupAuxSvcExtension.setCallingGroup(apiAuxService.getCallingGroupID());
            }
            if (apiAuxService.getCallingGroupType() != null)
            {
                callingGroupAuxSvcExtension.setCallingGroupType(com.redknee.app.crm.bean.CallingGroupTypeEnum.get((short) apiAuxService
                        .getCallingGroupType().getValue()));
            }
        }
        
        if (apiAuxService.getPeriod() != null)
        {
            crmAuxService.setChargingModeType(RmiApiSupport.convertApiServicePeriodType2CrmServicePeriodEnum(
                    apiAuxService.getPeriod(), 1L));
        }
        
        RecurrenceScheme apiRecScheme = apiAuxService.getRecurrence();
        if (apiRecScheme != null && apiRecScheme.getRecurrenceType() != null)
        {
            if (RecurrenceTypeEnum.ONE_TIME.getValue().getValue() == apiRecScheme.getRecurrenceType().getValue())
            {
                crmAuxService.setChargingModeType(ServicePeriodEnum.ONE_TIME);
                if (apiRecScheme.getStartDate() == null && apiRecScheme.getEndDate() == null)
                {
                    if (apiRecScheme.getPeriodUnitType() == null)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService.recurrenceScheme.periodUnitType",
                            "Mandatory field. NULL value not allowed.");
                    }
                    if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService.recurrenceScheme.period",
                                "The value should be at least 1.");
                    }
                    crmAuxService.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_INTERVAL);
                    crmAuxService.setValidity(apiRecScheme.getPeriod().intValue());
                    if (apiRecScheme.getPeriodUnitType().getValue() == com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.MONTHLY
                            .getValue().getValue())
                    {
                        crmAuxService.setFixedInterval(FixedIntervalTypeEnum.MONTHS);
                    }
                    else if (apiRecScheme.getPeriodUnitType().getValue() == com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.DAILY
                            .getValue().getValue())
                    {
                        crmAuxService.setFixedInterval(FixedIntervalTypeEnum.DAYS);
                    }
                    else
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                                "Only, Daily And Monthly PeriodUnitType allowed for ONE_OFF_FIXED_INTERVAL Service "
                                        + apiAuxService.getIdentifier() + " .");
                    }
                }
                else
                {
                    crmAuxService.setRecurrenceType(OneTimeTypeEnum.ONE_OFF_FIXED_DATE_RANGE);
                    crmAuxService.setEndDate(apiRecScheme.getEndDate());
                    crmAuxService.setStartDate(apiRecScheme.getStartDate());
                }
            }
            else
            {
                if (apiRecScheme.getPeriodUnitType() == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService.recurrenceScheme.periodUnitType",
                        "Mandatory field. NULL value not allowed.");
                }
                if (com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum.ANNUAL
                        .getValue().getValue() == apiRecScheme.getPeriodUnitType().getValue())
                {
                    RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService",
                            "Annual PeriodUnitType is not allowed for Auxiliary Service "
                                    + apiAuxService.getIdentifier() + " .");
                }
                else
                {
                    if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("auxiliaryService.recurrenceScheme.period",
                                "The value should be at least 1.");
                    }
                    
                    ServicePeriodEnum crmServicePeiod = RmiApiSupport.convertApiServicePeriodType2CrmServicePeriodEnum(
                            apiRecScheme.getPeriodUnitType(), apiRecScheme.getPeriod());
                    
                    crmAuxService.setChargingModeType(crmServicePeiod);
                    
                    if (ServicePeriodEnum.MULTIMONTHLY.equals(crmServicePeiod) || ServicePeriodEnum.MULTIDAY.equals(crmServicePeiod))
                    {
                        crmAuxService.setRecurrenceInterval(apiRecScheme.getPeriod().intValue());
                    }
                }
            }
        }
        
        return crmAuxService;
    }


    public static AuxiliaryService adaptGenericParametersToCreateAuxiliaryService(Context ctx,
            GenericParameter[] apiGenericParameter, AuxiliaryService crmAuxService)
    {
        Object obj = null;
        
        SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, SPGAuxSvcExtension.class);
        
        if (spgAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SPGSERVICETYPE,
                    apiGenericParameter);
            if (obj != null)
            {
                spgAuxSvcExtension.setSPGServiceType((Long) obj);
            }
        }
        
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ADJUSTMENTTYPEDESCRIPTION,
                apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setAdjustmentTypeDescription((String) obj);
        }
        AddMsisdnAuxSvcExtension addMsisdnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, AddMsisdnAuxSvcExtension.class);

        if (addMsisdnAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_BEARERTYPE, apiGenericParameter);
            if (obj != null)
            {
                addMsisdnAuxSvcExtension.setBearerType((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PROVISION_TO_ECP, apiGenericParameter);
            if (obj != null)
            {
                addMsisdnAuxSvcExtension.setProvisionToECP((Boolean) obj);
            }
        }

        DiscountAuxSvcExtension discountAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, DiscountAuxSvcExtension.class);
        
        if (discountAuxSvcExtension!=null)
        {
            obj = RmiApiSupport
                    .getGenericParameterValue(Constants.GENERICPARAMETER_DISCOUNTPERCENTAGE, apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setDiscountPercentage(((BigDecimal) obj).doubleValue());
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ENABLEDISCOUNTTHRESHOLD,
                    apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setEnableThreshold((Boolean) obj);
            }

            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_MINIMUMDISCOUNTTOTALCHARGETHREASHOLD,
                    apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setMinimumTotalChargeThreshold((Long) obj);
            }
        }
        
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_GLCODE, apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setGLCode((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_TAXAUTHORITY, apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setTaxAuthority(((Long) obj).intValue());
        }

        VPNAuxSvcExtension vpnAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, VPNAuxSvcExtension.class);

        if (vpnAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNPRICEPLAN, apiGenericParameter);
            if (obj != null)
            {
                vpnAuxSvcExtension.setVpnPricePlan((Long) obj);
            }
        }
        
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_WARNINGONSUSPENDDISABLE,
                apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setWarningOnSuspendDisable((String) obj);
        }
        obj = RmiApiSupport
                .getGenericParameterValue(Constants.GENERICPARAMETER_INVOICEDESCRIPTION, apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setInvoiceDescription((String) obj);
        }

        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPostpaidProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPostpaidUnProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPrepaidProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPrepaidUnProvCmd((String) obj);
            }
        }
      
        obj = RmiApiSupport
        .getGenericParameterValue(Constants.GENERICPARAMETER_RBTID, apiGenericParameter);

        PRBTAuxSvcExtension prbtAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, PRBTAuxSvcExtension.class);
        if (prbtAuxSvcExtension!=null)
        {
            if (obj != null)   
            {
                prbtAuxSvcExtension.setRbtId( (Long)obj);
            }
        }
        
        VoicemailAuxSvcExtension voicemailAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, VoicemailAuxSvcExtension.class);
        if (voicemailAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VOICEMAILPLANID, apiGenericParameter);
            if (obj != null)
            {
                voicemailAuxSvcExtension.setVmPlanId((String) obj);
            }
        }
        
        GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, GroupChargingAuxSvcExtension.class);
        if (groupChargingAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNADJUSTMENTTYPEDESCRIPTION,
                    apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupAdjustmentTypeDescription((String) obj);
            }
        
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNGROUPCHARGE, apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupCharge((Long) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNGLCODE, apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupGLCode((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNINVOICEDESCRIPION,
                    apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupInvoiceDescription((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNTAXAUTHORITY, apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupTaxAuthority(((Long) obj).intValue());
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNGROUPADJUSTMENTTYPEID,
                    apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupAdjustmentType(((Long) obj).intValue());
            }
        }
        return crmAuxService;
    }


    public static AuxiliaryService adaptGenericParametersToUpdateAuxiliaryService(Context ctx,
            GenericParameter[] apiGenericParameter, AuxiliaryService crmAuxService)
    {
        Object obj = null;
        
        SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, SPGAuxSvcExtension.class);
        
        if (spgAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_SPGSERVICETYPE,
                apiGenericParameter);
            if (obj != null)
            {
                spgAuxSvcExtension.setSPGServiceType((Long) obj);
            }
        }
        DiscountAuxSvcExtension discountAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, DiscountAuxSvcExtension.class);
        
        if (discountAuxSvcExtension!=null)
        {
            obj = RmiApiSupport
                    .getGenericParameterValue(Constants.GENERICPARAMETER_DISCOUNTPERCENTAGE, apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setDiscountPercentage(((BigDecimal) obj).doubleValue());
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ENABLEDISCOUNTTHRESHOLD,
                    apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setEnableThreshold((Boolean) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_MINIMUMDISCOUNTTOTALCHARGETHREASHOLD,
                    apiGenericParameter);
            if (obj != null)
            {
                discountAuxSvcExtension.setMinimumTotalChargeThreshold((Long) obj);
            }
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_WARNINGONSUSPENDDISABLE,
                apiGenericParameter);
        if (obj != null)
        {
            crmAuxService.setWarningOnSuspendDisable((String) obj);
        }
        
        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPostpaidProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_POSTPAIDUNPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPostpaidUnProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPrepaidProvCmd((String) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_PREPAIDUNPROVISIONHLRCOMMAND,
                    apiGenericParameter);
            if (obj != null)
            {
                provisionableAuxSvcExtension.setPrepaidUnProvCmd((String) obj);
            }
        }
        
        PRBTAuxSvcExtension prbtAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, crmAuxService, PRBTAuxSvcExtension.class);
        if (prbtAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RBTID, apiGenericParameter);
            if (obj != null)
            {
                prbtAuxSvcExtension.setRbtId((Long) obj);
            }
        }
     
        GroupChargingAuxSvcExtension groupChargingAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx,crmAuxService, GroupChargingAuxSvcExtension.class);
        if (groupChargingAuxSvcExtension!=null)
        {
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNGROUPCHARGE, apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupCharge((Long) obj);
            }
            obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_VPNGROUPADJUSTMENTTYPEID,
                    apiGenericParameter);
            if (obj != null)
            {
                groupChargingAuxSvcExtension.setGroupAdjustmentType(((Long) obj).intValue());
            }
        }
        return crmAuxService;
    }
}
