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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.api.Constants;
import com.trilogy.app.crm.api.rmi.support.RmiApiErrorHandlingSupport;
import com.trilogy.app.crm.api.rmi.support.RmiApiSupport;
import com.trilogy.app.crm.bean.core.BundleCategoryAssociation;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.DurationTypeEnum;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.license.BMLicenseConstants;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.RecurrenceScheme;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.RecurrenceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServicePeriodEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ActivationFeeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ActivationSchemeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.Bundle;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ExpirySchemeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.GroupChargingSchemeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.QuotaSchemeTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceBalanceLimit;


/**
 * Adapts BundleProfile object to API objects.
 * 
 * @author victor.stratan@redknee.com
 */
public class BundleProfileToApiAdapter implements Adapter
{

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptBundleProfileToApi(ctx, (com.redknee.app.crm.bundle.BundleProfile) obj);
    }


    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }


    public static BundleProfile adaptApiToBundleProfile(Context ctx, final Bundle apiBundle,
            List<BundleCategoryAssociation> bundleCategory) throws Exception
    {
        BundleProfile crmBundleProfile = null;
        try
        {
            crmBundleProfile = (BundleProfile) XBeans.instantiate(BundleProfile.class, ctx);
        }
        catch (Exception e)
        {
            new MinorLogMsg(BundleProfileToApiAdapter.class,
                    "Error instantiating new BundleProfile.  Using default constructor.", e).log(ctx);
            crmBundleProfile = new BundleProfile();
        }
        adaptApiToBundleProfile(ctx, apiBundle, crmBundleProfile, bundleCategory);
        return crmBundleProfile;
    }


    public static BundleProfile adaptApiToBundleProfile(Context ctx, final Bundle apiBundle,
            BundleProfile crmBundleProfile, List<BundleCategoryAssociation> bundleCategoryList) throws Exception
    {
        if (apiBundle.getActivationFeeType() != null)
        {
            if (apiBundle.getActivationFeeType().getValue() == 0)
            {
                crmBundleProfile.setActivationFeeCalculation(ActivationFeeCalculationEnum.FULL);
            }
            else
            {
                crmBundleProfile.setActivationFeeCalculation(ActivationFeeCalculationEnum.PRORATE);
            }
        }
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        // Set activation scheme if any of the bm activation license is enabled.
        // and keep default one, if it is not provided in request
        if (lMgr != null
                && (lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_FIRST_CALL_ACTIVATION)
                        || lMgr.isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_ACTIVE_ON_PROVISION) || lMgr
                        .isLicensed(ctx, BMLicenseConstants.ACTIVATION_SCHEME_SCHEDULED_ACTIVATION)))
        {
            if (apiBundle.getActivationScheme() != null)
            {
                crmBundleProfile.setActivationScheme(ActivationTypeEnum.get((short) apiBundle.getActivationScheme()
                        .getValue()));
            }
        }
        if (apiBundle.getAuxiliary() != null)
        {
            crmBundleProfile.setAuxiliary(apiBundle.getAuxiliary());
        }
        if (crmBundleProfile.getAuxiliary() && apiBundle.getAuxiliaryServiceCharge() != null)
        {
            crmBundleProfile.setAuxiliaryServiceCharge(apiBundle.getAuxiliaryServiceCharge());
        }
        if (apiBundle.getChargingPriority() != null)
        {
            crmBundleProfile.setChargingPriority(apiBundle.getChargingPriority());
        }
        if (apiBundle.getCurrency() != null && apiBundle.getCurrency())
        {
            crmBundleProfile.setAssociationType(com.redknee.app.crm.bundle.CategoryAssociationTypeEnum.CURRENCY);
        }
        else if (bundleCategoryList!=null && bundleCategoryList.size()>1)
        {
            crmBundleProfile.setAssociationType(com.redknee.app.crm.bundle.CategoryAssociationTypeEnum.CROSS_UNIT);
        }
        else
        {
            crmBundleProfile.setAssociationType(com.redknee.app.crm.bundle.CategoryAssociationTypeEnum.SINGLE_UNIT);
        }
        if (apiBundle.getExpiryPercent() != null)
        {
            crmBundleProfile.setExpiryPercent(apiBundle.getExpiryPercent());
        }
        if (apiBundle.getExpiryScheme() != null)
        {
            crmBundleProfile.setExpiryScheme(ExpiryTypeEnum.get((short) apiBundle.getExpiryScheme().getValue()));
        }
        ServiceBalanceLimit[] apiServiceBalLimits = apiBundle.getServiceInitialLimits();
        if (apiServiceBalLimits != null)
        {
            Map crmServiceBalLimits = new HashMap();
            for (ServiceBalanceLimit obj : apiServiceBalLimits)
            {
                crmServiceBalLimits.put(obj.getApplicationID(),
                        RmiApiSupport.getCrmServiceBalanceLimitFromApiServiceBalanceLimit(obj));
            }
            crmBundleProfile.setServiceInitialLimits(crmServiceBalLimits);
        }
        if (apiBundle.getName() != null)
        {
            crmBundleProfile.setName(apiBundle.getName());
        }
        if (apiBundle.getPaidType() != null)
        {
            crmBundleProfile.setSegment(BundleSegmentEnum.get((short) apiBundle.getPaidType().getValue()));
        }
        // not required in update
        if (apiBundle.getQuotaScheme() != null)
        {
            crmBundleProfile.setQuotaScheme(QuotaTypeEnum.get((short) apiBundle.getQuotaScheme().getValue()));
        }
        if (crmBundleProfile.getSegment().getIndex() == BundleSegmentEnum.POSTPAID_INDEX
                && crmBundleProfile.getQuotaScheme().getIndex() == QuotaTypeEnum.UNLIMITED_QUOTA_INDEX)
        {
            RmiApiErrorHandlingSupport.simpleValidation("bundle", "Unlimited Quota not allowed for Postpaid Segment");
        }
        if (apiBundle.getReprovisionOnActive() != null)
        {
            crmBundleProfile.setReprovisionOnActive(apiBundle.getReprovisionOnActive());
        }
        if (crmBundleProfile.getQuotaScheme().getIndex() != QuotaTypeEnum.UNLIMITED_QUOTA_INDEX)
        {
            if (apiBundle.getInitialBalanceLimit() != null)
            {
                crmBundleProfile.setInitialBalanceLimit(apiBundle.getInitialBalanceLimit());
            }
            RmiApiErrorHandlingSupport.validateMandatoryObject(apiBundle.getGroupChargingScheme(),
                    "bundle.groupChargingScheme");
            if (apiBundle.getGroupChargingScheme() != null)
            {
                crmBundleProfile.setGroupChargingScheme(GroupChargingTypeEnum.get((short) apiBundle
                        .getGroupChargingScheme().getValue()));
            }
            if (apiBundle.getRolloverMax() != null)
            {
                crmBundleProfile.setRolloverMax(apiBundle.getRolloverMax());
            }
            if (apiBundle.getRolloverMaxPercentage() != null)
            {
                crmBundleProfile.setRolloverMaxPercentage(apiBundle.getRolloverMaxPercentage());
            }
            if (apiBundle.getRolloverPercent() != null)
            {
                crmBundleProfile.setRolloverPercent(apiBundle.getRolloverPercent());
            }
        }
        if (apiBundle.getSpid() != null)
        {
            crmBundleProfile.setSpid(apiBundle.getSpid());
        }
        if (apiBundle.getSmartSuspension() != null)
        {
            crmBundleProfile.setSmartSuspensionEnabled(apiBundle.getSmartSuspension());
        }
        RecurrenceScheme apiRecScheme = apiBundle.getRecurrence();
        if (apiRecScheme != null && apiRecScheme.getRecurrenceType() != null)
        {
            if (RecurrenceTypeEnum.RECURRING.getValue().getValue() == apiRecScheme.getRecurrenceType().getValue())
            {
                if (apiRecScheme.getPeriodUnitType() == null)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle.recurrenceScheme.periodUnitType",
                        "Mandatory field. NULL value not allowed.");
                }
                if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle.recurrenceScheme.period",
                            "The value should be at least 1.");
                }
                
                if(ServicePeriodEnum.MONTHLY.getValue().getValue() == apiRecScheme.getPeriodUnitType().getValue())
                {
                    if (apiRecScheme.getPeriod() == 1)
                    {
                        crmBundleProfile.setRecurrenceScheme(com.redknee.app.crm.bundle.RecurrenceTypeEnum.RECUR_CYCLE_FIXED_DATETIME);
                        crmBundleProfile.setChargingRecurrenceScheme(com.redknee.app.crm.bean.ServicePeriodEnum.MONTHLY);
                    }
                    else
                    {
                        crmBundleProfile.setRecurrenceScheme(com.redknee.app.crm.bundle.RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL);
                        crmBundleProfile.setRecurringStartInterval(DurationTypeEnum.MONTH_INDEX);
                        crmBundleProfile.setRecurringStartValidity(apiRecScheme.getPeriod().intValue());
                        crmBundleProfile.setChargingRecurrenceScheme(com.redknee.app.crm.bean.ServicePeriodEnum.MULTIMONTHLY);
                    }
                }
                else if(ServicePeriodEnum.DAILY.getValue().getValue() == apiRecScheme.getPeriodUnitType().getValue())
                {
                    crmBundleProfile.setRecurrenceScheme(com.redknee.app.crm.bundle.RecurrenceTypeEnum.RECUR_CYCLE_FIXED_INTERVAL);
                    crmBundleProfile.setRecurringStartInterval(DurationTypeEnum.DAY_INDEX);
                    crmBundleProfile.setRecurringStartValidity(apiRecScheme.getPeriod().intValue());
                    if (crmBundleProfile.getRecurringStartValidity() == 1)
                    {
                        crmBundleProfile.setChargingRecurrenceScheme(com.redknee.app.crm.bean.ServicePeriodEnum.DAILY);
                    }
                    else
                    {
                        crmBundleProfile.setChargingRecurrenceScheme(com.redknee.app.crm.bean.ServicePeriodEnum.MULTIDAY);
                    }
                }
                else
                {
                    RmiApiErrorHandlingSupport.simpleValidation("bundle",
                            "Only, Daily And Monthly PeriodUnitType allowed for RECUR_CYCLE_FIXED_INTERVAL Bundle "
                                    + apiBundle.getIdentifier() + " .");
                }
            }
            else
            {
                crmBundleProfile.setChargingRecurrenceScheme(com.redknee.app.crm.bean.ServicePeriodEnum.ONE_TIME);
                if (apiRecScheme.getStartDate() == null && apiRecScheme.getEndDate() == null)
                {
                    if (apiRecScheme.getPeriodUnitType() == null)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle.recurrenceScheme.periodUnitType",
                            "Mandatory field. NULL value not allowed.");
                    }
                    if(apiRecScheme.getPeriod() == null || apiRecScheme.getPeriod() < 1)
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle.recurrenceScheme.period",
                                "The value should be at least 1.");
                    }
                    crmBundleProfile.setRecurrenceScheme(com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL);
                    crmBundleProfile.setValidity(apiRecScheme.getPeriod().intValue());
                    
                    if (apiRecScheme.getPeriodUnitType().getValue() == ServicePeriodEnum.MONTHLY.getValue().getValue())
                    {
                        crmBundleProfile.setInterval(DurationTypeEnum.MONTH_INDEX);
                    }
                    else if (apiRecScheme.getPeriodUnitType().getValue() == ServicePeriodEnum.DAILY.getValue().getValue())
                    {
                        crmBundleProfile.setInterval(DurationTypeEnum.DAY_INDEX);
                    }
                    else
                    {
                        RmiApiErrorHandlingSupport.simpleValidation("bundle",
                                "Only, Daily And Monthly PeriodUnitType allowed for ONE_OFF_FIXED_INTERVAL Bundle "
                                        + apiBundle.getIdentifier() + " .");
                    }
                }
                else
                {
                    crmBundleProfile.setRecurrenceScheme(com.redknee.app.crm.bundle.RecurrenceTypeEnum.ONE_OFF_FIXED_DATE_RANGE);
                    crmBundleProfile.setEndDate(apiRecScheme.getEndDate());
                    crmBundleProfile.setStartDate(apiRecScheme.getStartDate());
                }
            }
        }
        
        if (bundleCategoryList != null && bundleCategoryList.size() != 0)
        {
            if (crmBundleProfile.isCrossService())
            {
                Map bundleCategoryIds = new HashMap();
                for (BundleCategoryAssociation category : bundleCategoryList)
                {
                    BundleCategoryAssociation assoc = new BundleCategoryAssociation();
                    assoc.setCategoryId(category.getCategoryId());
                    assoc.setRate(category.getRate());
                    assoc.setContext(ctx);
                    bundleCategoryIds.put(category.getCategoryId(), assoc);
                }
                crmBundleProfile.setBundleCategoryIds(bundleCategoryIds);
            }
            else
            {
                crmBundleProfile.setBundleCategoryId(bundleCategoryList.get(0).getCategoryId());
            }
        }
        
        crmBundleProfile.setUseRolloverFirst(apiBundle.getChargeRolloverFirst());
        
        return crmBundleProfile;
    }


    public static BundleProfile adaptGenericParametersToCreateBundleProfile(GenericParameter[] apiGenericParameters,
            BundleProfile crmBundleProfile) throws CRMExceptionFault
    {
        Object obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_INVOICEDESCRIPTION,
                apiGenericParameters);
        GenericParameterParser parser = new GenericParameterParser(apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setInvoiceDesc((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_GLCODE, apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setGLCode((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_ADJUSTMENTTYPEDESCRIPTION,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setAdjustmentTypeDescription((String) obj);
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RECURRINGSTARTHOUR,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRecurringStartHour(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RECURRINGSTARTINTERVAL,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRecurringStartInterval(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RECURRINGSTARTMINUTES,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRecurringStartMinutes(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RECURRINGSTARTVALIDITY,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRecurringStartValidity(((Long) obj).intValue());
        }
        obj = RmiApiSupport
                .getGenericParameterValue(Constants.GENERICPARAMETER_RELATIVESTARTHOUR, apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRelativeStartHour(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RELATIVESTARTINTERVAL,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRelativeStartInterval(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RELATIVESTARTMINUTES,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRelativeStartMinutes(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_RELATIVESTARTVALIDITY,
                apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRelativeStartValidity(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_TAXAUTHORITY, apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setTaxAuthority(((Long) obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_REPURCHASABLE, apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setRepurchasable(((Boolean)obj).booleanValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_REPURCHASE_EXPIRY_EXTENSION, apiGenericParameters);
        if (obj != null)
        {
            crmBundleProfile.setExpiryExtensionOnRepurchase(((Integer)obj).intValue());
        }
        obj = RmiApiSupport.getGenericParameterValue(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER, apiGenericParameters);
        if (obj != null)
        {
            if(parser.containsParam(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER))
            {
                try
                {
                    crmBundleProfile.setExecutionOrder(parser.getParameter(Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER, Long.class).intValue());
                }
                catch (CRMExceptionFault e)
                {
                    throw e;
                }
            }
        }
        return crmBundleProfile;
    }
    
    public static Bundle adaptBundleProfileToApi(Context ctx,
            final com.redknee.app.crm.bundle.BundleProfile crmBundleProfile)
    {
        final Bundle apiBundle = new Bundle();
        BundleProfileToApiReferenceAdapter.adaptBundleProfileToReference(ctx, crmBundleProfile, apiBundle);
        switch (crmBundleProfile.getActivationFeeCalculation().getIndex())
        {
        case 1:
            apiBundle.setActivationFeeType(ActivationFeeTypeEnum.FULL.getValue());
            break;
        case 0:
            apiBundle.setActivationFeeType(ActivationFeeTypeEnum.PRORATED.getValue());
            break;
        }
        apiBundle.setActivationScheme(ActivationSchemeTypeEnum.valueOf(crmBundleProfile.getActivationScheme()
                .getIndex()));
        apiBundle.setAdjustmentTypeID(Long.valueOf(crmBundleProfile.getAdjustmentType()));
        apiBundle.setAuxiliaryAdjustmentTypeID(Long.valueOf(crmBundleProfile.getAuxiliaryAdjustmentType()));
        apiBundle.setAuxiliaryServiceCharge(crmBundleProfile.getAuxiliaryServiceCharge());
        BundleCategoryAssociation[] crmBundleCatetoryAssociations = (BundleCategoryAssociation[]) crmBundleProfile
                .getBundleCategoryIds().values().toArray(new BundleCategoryAssociation[0]);
        Long[] apiBundleCategoryIDs = new Long[crmBundleCatetoryAssociations.length];
        int i = 0;
        for (BundleCategoryAssociation bundleCategoryAssociation : crmBundleCatetoryAssociations)
        {
            apiBundleCategoryIDs[i] = Long.valueOf(bundleCategoryAssociation.getCategoryId());
            i++;
        }
        apiBundle.setBundleCategoryIDs(apiBundleCategoryIDs);
        apiBundle.setChargingPriority(crmBundleProfile.getChargingPriority());
        apiBundle.setCurrency(crmBundleProfile.isCurrency());
        apiBundle.setEnabled(crmBundleProfile.getEnabled());
        apiBundle.setEnablePromotionProvision(crmBundleProfile.getEnablePromotionProvision());
        apiBundle.setExpiryPercent(crmBundleProfile.getExpiryPercent());
        apiBundle.setExpiryScheme(ExpirySchemeTypeEnum.valueOf(crmBundleProfile.getExpiryScheme().getIndex()));
        apiBundle.setGroupChargingScheme(GroupChargingSchemeTypeEnum.valueOf(crmBundleProfile.getGroupChargingScheme()
                .getIndex()));
        apiBundle.setGroupIdentifier(crmBundleProfile.getGroupBundleId());
        apiBundle.setInitialBalanceLimit(crmBundleProfile.getInitialBalanceLimit());
        apiBundle.setQuotaScheme(QuotaSchemeTypeEnum.valueOf(crmBundleProfile.getQuotaScheme().getIndex()));
        apiBundle.setReprovisionOnActive(crmBundleProfile.getReprovisionOnActive());
        apiBundle.setRolloverMax(crmBundleProfile.getRolloverMax());
        apiBundle.setRolloverMaxPercentage(crmBundleProfile.getRolloverMaxPercentage());
        apiBundle.setRolloverPercent(crmBundleProfile.getRolloverPercent());
        Map crmServiceBalanceLimitMap = crmBundleProfile.getServiceInitialLimits();
        if (crmServiceBalanceLimitMap != null)
        {
            Collection crmServiceBalanceLimits = crmServiceBalanceLimitMap.values();
            ServiceBalanceLimit[] apiServiceBalanceLimits = new ServiceBalanceLimit[crmServiceBalanceLimits.size()];
            int j = 0;
            for (Object obj : crmServiceBalanceLimits)
            {
                apiServiceBalanceLimits[j++] = RmiApiSupport
                        .getApiServiceBalanceLimitFromCrmServiceBalanceLimit((com.redknee.app.crm.bundle.ServiceBalanceLimit) obj);
            }
        }
        apiBundle.setSmartSuspension(crmBundleProfile.getSmartSuspensionEnabled());
        apiBundle.setChargeRolloverFirst(crmBundleProfile.isUseRolloverFirst());

        /*
         * Add API GenericParams
         */
        apiBundle.addParameters(RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_REPURCHASABLE, Boolean.valueOf(crmBundleProfile.getRepurchasable())));
        apiBundle.addParameters(RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_REPURCHASE_EXPIRY_EXTENSION, 
                    Integer.valueOf(crmBundleProfile.getExpiryExtensionOnRepurchase())));
        apiBundle.addParameters(RmiApiSupport.createGenericParameter(
                Constants.GENERICPARAMETER_BUNDLE_EXECUTION_ORDER, 
                    Integer.valueOf(crmBundleProfile.getExecutionOrder())));
        
        return apiBundle;
    }
}
