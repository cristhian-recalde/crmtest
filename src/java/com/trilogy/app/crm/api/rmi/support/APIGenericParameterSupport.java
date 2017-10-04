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
package com.trilogy.app.crm.api.rmi.support;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.amsisdn.AdditionalMsisdnAuxiliaryServiceSupport;
import com.trilogy.app.crm.api.queryexecutor.transaction.TransactionQueryExecutors;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.ReasonCode;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.TransactionMethod;
import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.bean.calldetail.BillingCategory;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.FlexTypeEnum;
import com.trilogy.app.crm.bundle.InvalidBundleApiException;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.web.MapUnitWebControl;
import com.trilogy.app.crm.defaultvalue.CharValue;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.auxiliaryservice.core.ProvisionableAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.custom.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.service.BlacklistWhitelistTemplateServiceExtension;
import com.trilogy.app.crm.support.BundleSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.ServicePreferenceEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.transaction.ProfileType;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.CallingGroupTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.generalprovisioning.BlacklistTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.AuxiliaryServiceTypeEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.ServiceTypeEnum;



public class APIGenericParameterSupport
{

    public static final String PARAM_GENERIC_PARAMETERS_NAME = "parameters";
    public static final int PARAM_GENERIC_PARAMETERS = 4;
    public static final String PATH = "path";
    public final static String PRICE_PLAN_PREFERENCE_TYPE = "PricePlanPreferenceType";
    public final static String AUXILIARY_SERVICE_TYPE = "AuxiliaryServiceType";
    public final static String CALLING_GROUP_TYPE = "CallingGroupType";
    public final static String CALLING_GROUP_ID = "CallingGroupID";
	public final static String PRIVATE_CUG_OWNER = "PrivateCUGOwner";
	public final static String CUG_SHORT_CODES_ENABLED = "CUGShortCodesEnabled";
    public final static String REQUIRES_ADDITIONAL_MOBILE_NUMBER = "RequiresAdditionalMobileNumber";
    public final static String ADDITIONAL_MOBILE_NUMBER = "AdditionalMobileNumber";
    public final static String PROVISION_ON_SUSPEND_OR_DISABLE = "ProvisionOnSuspendOrDisable";
    public final static String BUNDLE_CATEGORY_ID = "BundleCategoryID";
    public final static String BUNDLE_TYPE = "BundleType�";
    public final static String BUNDLE_PROFILE_PRIMARY_FLEX_BUNDLE_ID = "PrimaryFlexBundleIDs";
    public static final String BUNDLE_PROFILE_SECONDARY_FLEX_BUNDLE_ID  = "SecondaryFlexBundleIDs";
    public final static String BUNDLE_PROFILE_FLEX_BUNDLE_CATEGORY_ID = "FlexBundleCategoryIDs";
    public final static String SECONDARY_BALANCE_BUNDLE_CATEGORY_ID = "SecondaryBalanceCategories";
    public final static String INITIAL_BALANCE_LIMIT = "InitialBalanceLimit";
    public final static String BALANCE_UNIT_TYPE = "BalanceUnitType";
    public final static String EXPIRY_DATE = "ExpiryDate";
    public final static String EXPIRY_SCHEME = "ExpiryScheme";
    public final static String DISABLE_SWITCH_THRESHOLD = "DisableSwitchThreshold";
    public final static String ORIGINATING_APPLICATION = "OriginatingApplication";
    public final static String EXEMPT_FROM_CREDIT_LIMIT_CHECK = "ExemptFromCreditLimitCheck";
    public final static String OCG_ADJUSTMENTTYPE_OVERRIDE = "OCGAdjustmentTypeOverride";
    public final static String RESULTING_BALANCE = "ResultingBalance";
    public final static String VRA = "VRA";
    public final static String MVNE = "MVNE";
    public final static String EXT = "EXT";
    public final static String PORT_OUT_FLAG = "PortOut";
    public static final String PORT_IN = "PortIn";
    public static final String EFFECTIVE_DATE = "EffectiveDate";
    public static final String CLEAN_UP_OLD_MDN = "CleanUpOldMdn";
    public static final String SNAP_BACK_IN = "SnapBackIn";
    public final static String TIGO_INFORMATION = "TigoInformation";
    public final static String TIGO_AUTHORIZATION = "TigoAuthorization";
    public final static String TIGO_STATUS = "TigoStatus";
    public final static String TIGO_TRANSACTION_ID = "TigoTransactionId";
    public final static String EXTENSION_DAYS = "ExtensionDays";
    public final static String VRA_CLAWBACK = "VRAClawback";
    public final static String VRA_EVENT_TYPE = "VRAEventType";
    public final static String VRA_VOUCHER = "VRAVoucher";
    public final static String TFA = "TFA";
    public final static String OCG = "OCG";
    public final static String SELF_CARE = "SC";
    public final static String REQ_BSS_FWD_TO_OCG = "RequestBSSForwardToOCG";
    public final static String COVERED_BY_CONTRACT = "CoveredByContract";
    public final static String DEVICE_TYPE = "DeviceType";
   
    public static final String DEVICE_MODEL_NAME = "DeviceModelName";
    public static final String DEVICE_LIST_PRICE= "DeviceListPrice";
    public static final String IMEI = "IMEI";
    
    public final static String CRM_ADJUSTMENT_REQUEST = "CRM01";
    public final static String CRM_ADJUSTMENT_SUBSCRIPTION_REQUEST = "CRM02";
    public final static String CRM_ADJUSTMENT_ACCOUNT_REQUEST = "CRM03";
    public final static String CRM_ADJUSTMENT_OWNER_REQUEST = "CRM04";
    public final static String CRM_ADJUSTMENT_ADJUSTMENT_CATEGORY_REQUEST = "CRM05";
    public final static String CRM_ADJUSTMENT_SPID_REQUEST = "CRM06";
    public final static String CRM_ADJUSTMENT_APPROVED_BY_REQUEST = "CRM07";    
    public final static String PRICE_PLAN_UPDATE_RESULT_NAME = "PricePlanOptionName";
	public static final String RN_USAGE_TYPE = "RNUsageType";
	public static final String RN_BILLING_CATEGORY_ID = "RNBillingCategoryID";
	public static final String RN_PRIMARY_TAX_AUTHORITY_ID =
	    "RNPrimaryTaxAuthorityID";
	public static final String RN_SECONDARY_TAX_AUTHORITY_ID =
	    "RNSecondaryTaxAuthorityID";
	public static final String CALL_DETAIL_RATING_RULE ="RatingRule";
	public static final String CALL_DETAIL_CHARGED_PARTY ="ChargedParty";
	public static final String BLACKLIST_STATUS = "BlacklistStatus";
	public static final String RN_ADJUSTMENT_TYPE_ID = "RNAdjustmentTypeID";
	public static final String RN_REASON_CODE = "RNReasonCode";
	public static final String RN_TRANSACTION_METHOD_ID =
	    "RNTransactionMethodID";
	public static final String FIXED_STOP_PRICEPLAN_SWITCH = "newPricePlanID";
	public final static String NEXT_RECURRING_CHARGE_DATE = "NextRecurringChargeDate";
	public final static String SERVICE_TYPE = "ServiceType";
	
	public final static String CONTRACT_REMAINING_YEARS = "RemainingYears";
    public final static String CONTRACT_REMAINING_MONTHS = "RemainingMonths";
    public final static String CONTRACT_REMAINING_DAYS = "RemainingDays";
    public final static String DEVICE_PRODUCT_ID = "DeviceProductId";
    public static final String NEW_AUTO_BILLCYLE_ID = "newAutoBillCycleId";
    
    public final static String RESPONSIBLE_BAN = "ResponsibleBAN";
    public final static String OLD_BAN = "OldBAN";
    public static final String ACCOUNT_CREATE_REASON = "AccountCreateReason";
    

    public static final String SECONDARY_PACKAGE = "secondaryPackage";
    public static final String MSID = "MSID";
    public static final String CSA = "CSA";

    public final static String INITIAL_BALANCE_BY_CREDIT_CARD = "initialBalanceByCreditCard";
    
    public static final String CURRENT_NUM_PTP_TRANSITIONS = "CurrentNumPTPTransitions";
    public static final String MAX_NUM_PTP_TRANSITIONS = "MaxNumPTPTransitions";
    /**
     * following constants have been added for PTUB feature.
     */

    
    public static final String CATEGORY_ID = "CategoryId";
    public static final String SECONDARY_BALANCE = "SecondaryBalance";
    public static final String APPLY_TAX = "ApplyTax";
    public static final String SECONDARY_BALANCE_INDICATOR = "secondaryBalanceIndicator";
    public static final String SECONDARY_BALANCE_AMOUNT= "secondaryBalanceAmount" ;
    public static final String TAX_AMOUNT = "TaxAmount";
    
    
    /**
     * Generic parameters added as part of MRC & PAYGO feature.
     */
    public static final String BALANCE_THRESHOULD_AMOUNT = "ATUBalanceThreshold";
    public static final String AUTO_TOPUP_AMOUNT = "ATUAmount";
    
    /**
     * Generic parameters added as part of F-0001526 Group Account Enhancement feature.
     */
    public static final String GROUP_SCREENING_TEMPLATE_ID = "GroupScreeningTemplateId";
    public static final String REMOVE_GROUP_SCREENING_TEMPLATE  = "RemoveGroupScreeningTemplate";
    
    /**
     * Generic parameters added as part of Grandfather feature.
     */
    public static final String PRICE_PLAN_STATE = "PricePlanState";
    public static final String GRANDFATHER_PRICE_PLAN_ID = "GrandfatherPricePlanId";
    
    /**
     *  Generic parameter added as part of PickNPay feature
     */
    public final static String APPLY_MRC_GROUP = "ApplyMrcGroup";
    public static final String PRICE_PLAN_SUBTYPE = "PricePlanSubType";
    public static final String SEND_MRC_AND_DISCOUNT = "SendMRCAndDiscount";
    public static final String MRC_AMOUNT = "MRCAmount";
    public static final String DISCOUNT_AMOUNT = "DiscountAmount";
    public final static String IS_PRIMARY = "IsPrimary";
    
	/**
	 * To initiate a bundle repurchase (topup) request via API.
	 */
	public static final String REPURCHASE_IN = "Repurchase";
	/**
	 * Response parameter to tell the client if the bundle is 'Repurchasable'. BSS allows configuration 
	 * to mark a bundle 'Repurchasable'.
	 */                                          
	public static final String REPURCHASABLE_OUT = "Repurchaseable";
	
	public static final String NON_STANDARD_RENEWAL_PARAM = "MBGRenewal";

	public static final String SUBSIDY_AMOUNT_PARAM = "SubsidyAmount";
	public static final String PENALTY_FEE_PER_MONTH_PARAM = "PenaltyFeePerMonth";
	public static final String DEVICE_PRODUCTID_PARAM = "DeviceProductId";
	
	
    public static final String IS_USE_PLAN_FEE = "isUsePlanFee";
    public static final String IS_PLAN_CHANGE_SCHEDULED = "isPlanChangeScheduled";
    public static final String SIM_SWAP_OCCURED = "SIM_SWAP_OCCURED";
    public static final String SIM_SWAP_DETAILS = "SIM_SWAP_DETAILS";
    public static final String ATU_CREDIT_CARD_TOKEN = "ATUCreditCardToken";
    public static final String PERSONALIZED_FEE="PersonalizedFee";
    public final static String IS_PERSONALIZED_FEE_SELECTED = "isPersonalizedFeeSelected";
    public static final String IS_PERSONALIZED_FEE="isPersonalizedFeeAllowed";
    public static final String SERVICE_QUANTITY = "ServiceQuantity";
    public static final String SHOW_TAXAMOUNT= "TaxAmount";
    public static final String CHANGE_INDICATOR_APPLICABLE = "changeIndicatorApplicable";
    public static final String SECURITY_QUESTION_GUID_QUEID = "SecurityQuestionGuidId";
    public static final String CHANGE_INDICATOR = "changeIndicator";
    public static final String CHANGE_INDICATOR_OPTION_MODIFIED = "MODIFIED";
    public static final String PRICEPLAN_ID = "PricePlanId";
    /**
     *  Indicates Subscriber Activation Date
     */
    public static final String START_DATE = "StartDate";
    public static final String SUB_EXPIRY_DATE = "ExpiryDate";
    public static final String VALID_AFTER = "validAfter";
    
    /**
     *  Indicates Subscription Class
     */
    public static final String SUBSCRIPTION_CLASS = "SubscriptionClass";

    /**
     *  Indicates Contract in Days Required
     */
    public static final String CONTRACT_IN_DAYS = "ContractInDays";
    /**
     *  Indicates Service Address Id of subscriber
     */
    public static final String ADDRESS_ID = "addressId";
    public static final String ALIAS = "alias";
    
    //Sprint 7: Suspension reason to be sent as a short by DCRM when suspension is done manually
  	public static final String SUBSCRIBER_SUSPENSION_REASON = "SubscriberSuspensionReason";
  	
  	public static final String SUBSCRIBER_DEACTIVATION_REASON_CODE = "SubscriberDeactivationReasonCode";
  	
  	public static final String SUBSCRIBER_SUSPEND_PERIOD = "SuspendPeriod";
  	
	//TT fix ITSC-13240
	public static final String ACTIVATION_DATE="ActivationDate";
	
	public static final String OFFERING_BUSINESS_KEY = "offeringBusinessKey";
    
    public static GenericParameter getPricePlanPreferenceTypeParameter(Context ctx, Object obj)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(PRICE_PLAN_PREFERENCE_TYPE);
        if (obj instanceof ServiceFee2)
        {
            ServiceFee2 fee = (ServiceFee2) obj;
            parameter.setValue(ServicePreferenceEnum.valueOf(fee.getServicePreference().getIndex()).getValue());
        }
        else if (obj instanceof BundleFee)
        {
            BundleFee fee = (BundleFee) obj;
            parameter.setValue(ServicePreferenceEnum.valueOf(fee.getServicePreference().getIndex()).getValue());
        }
        else if (obj instanceof AuxiliaryService)
        {
            parameter.setValue(ServicePreferenceEnum.OPTIONAL.getValue().getValue());
        }
        else
        {
            parameter.setValue(ServicePreferenceEnum.OPTIONAL.getValue().getValue());
        }
        return parameter;
    }


 	
	public static GenericParameter getSecurityQuesGUID(Context ctx,Map<String, String> guidIDMap) {
 	
	       GenericParameter parameter = new GenericParameter();
 	
	       parameter.setName(SECURITY_QUESTION_GUID_QUEID);
 	
        parameter.setValue(String.valueOf(guidIDMap));
 	
	       return parameter;
 	
	}

    public static GenericParameter getBundleCategoryIdParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BUNDLE_CATEGORY_ID);
        parameter.setValue(bundleProfile.getBundleCategoryId());
        return parameter;
    }


    public static GenericParameter getBundleTypeParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BUNDLE_TYPE);
        parameter.setValue(bundleProfile.getType());
        return parameter;
    }


    public static GenericParameter getInitialBalanceLimitParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(INITIAL_BALANCE_LIMIT);
        parameter.setValue(bundleProfile.getInitialBalanceLimit());
        return parameter;
    }


    public static GenericParameter getBalanceUnitTypeParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BALANCE_UNIT_TYPE);
        UnitTypeEnum unit = getUnitType(ctx, bundleProfile);
        parameter.setValue(unit.getIndex());
        return parameter;
    }


    public static GenericParameter getExpiryDateParameter(Context ctx, BundleFee fee)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(EXPIRY_DATE);
        parameter.setValue(fee.getEndDate());
        return parameter;
    }
	
	
		public static GenericParameter getSubscriptionExpiryDateParameter(Context ctx, Date expDate)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(EXPIRY_DATE);
		parameter.setValue(CalendarSupportHelper.get(ctx).dateToCalendar(expDate));
		return parameter;
	}
	
    
    public static GenericParameter getExpirySchemeParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(EXPIRY_SCHEME);
        parameter.setValue(bundleProfile.getExpiryScheme().getIndex());
        return parameter;
    }
    
    public static GenericParameter getRepurchasableParameter(Context ctx, BundleProfile bundleProfile)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(REPURCHASABLE_OUT);
        parameter.setValue(bundleProfile.getRepurchasable());
        return parameter;
    }


    public static GenericParameter getAuxiliaryServiceTypeParameter(Context ctx, AuxiliaryService auxService)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(AUXILIARY_SERVICE_TYPE);
        parameter.setValue(AuxiliaryServiceTypeEnum.valueOf(auxService.getType().getIndex()).getValue());
        return parameter;
    }


    public static GenericParameter getCallingGroupTypeParameter(Context ctx, AuxiliaryService auxService)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(CALLING_GROUP_TYPE);
        com.redknee.app.crm.bean.CallingGroupTypeEnum callingGroupType = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPTYPE;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroupType = callingGroupAuxSvcExtension.getCallingGroupType();
        }
        parameter.setValue(CallingGroupTypeEnum.valueOf(callingGroupType.getIndex()).getValue());
        return parameter;
    }


    public static GenericParameter getTransactionOwnerTypeParameter(ProfileType type)
    {
        GenericParameter gp = new GenericParameter();
        gp.setName(TransactionQueryExecutors.OWNER_TYPE);
        gp.setValue(type.getValue());
        return gp;
    }
    

    public static List<GenericParameter> getBlacklistWhitelistTemplateServiceCUGParameters(Context ctx, Service service, List<GenericParameter> list)
    {
        BlacklistWhitelistTemplateServiceExtension blwlServiceExtension = ExtensionSupportHelper.get(ctx).getExtension(
                ctx, (ExtensionAware) service, BlacklistWhitelistTemplateServiceExtension.class);
        if (blwlServiceExtension != null)
        {
            list.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CALLING_GROUP_ID,
                    blwlServiceExtension.getCallingGroupId()));
            list.add(RmiApiSupport.createGenericParameter(APIGenericParameterSupport.CALLING_GROUP_TYPE,
                    (long)blwlServiceExtension.getCallingGroupType().getIndex()));
        }

        return list;
    }
    
	public static GenericParameter getCugCallingGroupIdParameter(Context ctx,
	    ClosedUserGroup cug)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(CALLING_GROUP_ID);
			parameter.setValue(cug.getID());
		return parameter;
	}

	public static GenericParameter getNonCugCallingGroupIdParameter(
	    Context ctx, AuxiliaryService auxService)
	{
		GenericParameter parameter = new GenericParameter();
        long callingGroupIdentifier = CallingGroupAuxSvcExtension.DEFAULT_CALLINGGROUPIDENTIFIER;
        CallingGroupAuxSvcExtension callingGroupAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, CallingGroupAuxSvcExtension.class);
        if (callingGroupAuxSvcExtension!=null)
        {
            callingGroupIdentifier = callingGroupAuxSvcExtension.getCallingGroupIdentifier();
        }

		parameter.setName(CALLING_GROUP_ID);
			parameter.setValue(callingGroupIdentifier);
        return parameter;
    }

	public static GenericParameter getPrivateCUGOwnerParameter(Context ctx,
	    ClosedUserGroup cug)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(PRIVATE_CUG_OWNER);
		parameter.setValue(cug.getOwnerMSISDN());
		return parameter;
	}

	public static GenericParameter getCUGShortCodesEnabled(Context ctx,
	    ClosedUserGroup cug)
	{
		GenericParameter parameter = new GenericParameter();
			parameter.setName(CUG_SHORT_CODES_ENABLED);
			parameter.setValue(Boolean.valueOf(cug.isShortCodeEnable(ctx)));
		return parameter;
	}

    public static GenericParameter getProvisionOnSuspendOrDisableParameter(Context ctx, AuxiliaryService auxService)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(PROVISION_ON_SUSPEND_OR_DISABLE);
        boolean provisionOnSuspendDisable = ProvisionableAuxSvcExtension.DEFAULT_PROVONSUSPENDDISABLE;
        ProvisionableAuxSvcExtension provisionableAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, auxService, ProvisionableAuxSvcExtension.class);
        if (provisionableAuxSvcExtension!=null)
        {
            provisionOnSuspendDisable = provisionableAuxSvcExtension.isProvOnSuspendDisable();
        }
        parameter.setValue(provisionOnSuspendDisable);
        return parameter;
    }


    public static GenericParameter getCoveredByContract(Context ctx, long fee)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(COVERED_BY_CONTRACT);
        parameter.setValue(Long.valueOf(fee));
        return parameter;
    }

    public static GenericParameter getPricePlanUpdateResultName(Context ctx, String name )
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(PRICE_PLAN_UPDATE_RESULT_NAME);
        parameter.setValue(name);
        return parameter;
    }


    public static GenericParameter getAdditionalMobileNumberParameter(Context ctx, AuxiliaryService auxService)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(REQUIRES_ADDITIONAL_MOBILE_NUMBER);
        if (AdditionalMsisdnAuxiliaryServiceSupport.isAdditionalMsisdnEnabled(ctx)
                && auxService.getType().equals(com.redknee.app.crm.bean.AuxiliaryServiceTypeEnum.AdditionalMsisdn))
        {
            parameter.setValue(true);
        }
        else
        {
            parameter.setValue(false);
        }
        return parameter;
    }


    public static UnitTypeEnum getUnitType(Context ctx, BundleProfile bundle)
    {
        if (bundle.getType() == -1)
        {
            return null;
        }
        try
        {
            UnitTypeEnum unitType = UnitTypeEnum.get((short) bundle.getType());
            return unitType;
        }
        catch (Throwable t)
        {
            /*
             * If bundle is being created (id=0), it may not have a bundle type defined
             * yet. No need to log an exception in this case, unless a category is already
             * associated to it.
             */
            if (bundle.getBundleId() > 0 || bundle.getBundleCategoryIds().size() > 0)
            {
                new MinorLogMsg(MapUnitWebControl.class, "unable to discover unit type", t).log(ctx);
            }
            return null;
        }
    }

	public static GenericParameter getAccountBlacklistStatus(Context ctx,
	    BlackTypeEnum blacklistColour)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(BLACKLIST_STATUS);
		if (blacklistColour != null)
		{
	        parameter.setValue(BlacklistTypeEnum.valueOf(blacklistColour.getIndex()).getValue());
		}
		else
		{
            parameter.setValue(BlacklistTypeEnum.NONE.getValue().getValue());
		}
		return parameter;
	}
	
	public static GenericParameter getAccountBillCycleId(Context ctx,
	        Account account)
	{
        GenericParameter parameter = new GenericParameter();
        parameter.setName(NEW_AUTO_BILLCYLE_ID);
        if (account != null)
        {
            parameter.setValue(account.getBillCycleID());
        }
        return parameter;
	}

	public static GenericParameter getCallDetailRNUsageType(Context ctx,
	    UsageType usageType)
	{
		GenericParameter parameter = new GenericParameter();
			parameter.setName(RN_USAGE_TYPE);
			parameter.setValue(usageType.getDescription());
		return parameter;
	}

	public static GenericParameter getCallDetailRNBillingCategoryID(
	    Context ctx, BillingCategory category)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_BILLING_CATEGORY_ID);
		parameter.setValue(category.getName());
		return parameter;
	}

	public static GenericParameter getCallDetailRNPrimaryTaxAuthorityID(
	    Context ctx, TaxAuthority taxAuthority)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_PRIMARY_TAX_AUTHORITY_ID);
		parameter.setValue(taxAuthority.getTaxAuthName());
		return parameter;
	}
	
	public static GenericParameter getCallDetailSecondaryBalanceIndicator(
		    Context ctx, short secondaryBalanceIndicator)
		{
			GenericParameter parameter = new GenericParameter();
			parameter.setName(SECONDARY_BALANCE_INDICATOR);
			parameter.setValue(secondaryBalanceIndicator);
			return parameter;
		}
	
	public static GenericParameter getCallDetailSecondaryBalanceAmount(
		    Context ctx, long secondaryBalanceAmount)
		{
			GenericParameter parameter = new GenericParameter();
			parameter.setName(SECONDARY_BALANCE_AMOUNT);
			parameter.setValue(secondaryBalanceAmount);
			return parameter;
		}

	public static GenericParameter getCallDetailRNSecondaryTaxAuthorityID(
	    Context ctx, TaxAuthority taxAuthority)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_SECONDARY_TAX_AUTHORITY_ID);
		parameter.setValue(taxAuthority.getTaxAuthName());
		return parameter;
	}

	public static GenericParameter getCallDetailRatingRule(
            Context ctx, CallDetail callDetail)
    {
        String ratingRule = null;
        if(callDetail.getRatingRule() != null && !callDetail.getRatingRule().trim().isEmpty())
        {
            ratingRule = callDetail.getRatingRule();
        }
        GenericParameter parameter = new GenericParameter();
        parameter.setName(CALL_DETAIL_RATING_RULE);
        parameter.setValue(ratingRule);
        return parameter;
    }

	public static GenericParameter getCallChargedParty(
            Context ctx, String chargedParty)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(CALL_DETAIL_CHARGED_PARTY);
        parameter.setValue(chargedParty);
        return parameter;
    
    }
	

	public static GenericParameter getTransactionRNAdjustmentTypeID(
	    Context ctx, AdjustmentType adjustmentType)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_ADJUSTMENT_TYPE_ID);
		parameter.setValue(adjustmentType.getName());
		return parameter;
	}

	public static GenericParameter getTransactionRNReasonCode(Context ctx,
	    ReasonCode reasonCode)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_REASON_CODE);
		parameter.setValue(reasonCode.getMessage());
		return parameter;
	}

	public static GenericParameter getTransactionRNTransactionMethodID(
	    Context ctx, TransactionMethod method)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(RN_TRANSACTION_METHOD_ID);
		parameter.setValue(method.getName());
		return parameter;
	}
	
	public static GenericParameter getTransactionRNTigoAuthorizationCode(
		    Context ctx, String authorizationCode)
		{
			GenericParameter parameter = new GenericParameter();
			parameter.setName(TIGO_AUTHORIZATION);
			parameter.setValue(authorizationCode);
			return parameter;
		}
	
	public static GenericParameter getTransactionRNTigoStatus(
		    Context ctx, int tigoStatusCode)
		{
			GenericParameter parameter = new GenericParameter();
			parameter.setName(TIGO_STATUS);
			parameter.setValue(tigoStatusCode);
			return parameter;
		}
	
	public static GenericParameter getTransactionTaxAmount(
			Context ctx, long taxAmount)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(SHOW_TAXAMOUNT);
		parameter.setValue(taxAmount);
		return parameter;
	}    
	
	public static GenericParameter getTransactionRNTigoTransactionId(
		    Context ctx, String tigoTransactionId)
		{
			GenericParameter parameter = new GenericParameter();
			parameter.setName(TIGO_TRANSACTION_ID);
			parameter.setValue(tigoTransactionId);
			return parameter;
		}
    
    public static GenericParameter getNextRecurringChargeDateParam(Date nextRecurringChargeDate)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(NEXT_RECURRING_CHARGE_DATE);
        parameter.setValue(nextRecurringChargeDate);
        
        return parameter;
    }
    
    public static GenericParameter[] getSubscriptionStateTransitionGenericParameters(final Context ctx, Subscriber subscriber, com.redknee.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionState oldState)
    {
    	GenericParameter[] parameters = null;
    	
    	if(subscriber.getState().getIndex() == SubscriberStateEnum.ACTIVE_INDEX && oldState.getValue() == SubscriberStateEnum.AVAILABLE_INDEX)
    	{
	    	// Activation Date to be sent only if state transition to Active state. 
    		// By this stage Subscriber pipeline would have taken care of valid state transitions, so no need to check for FROM state 
	    	parameters = new GenericParameter[] {};
	        
	    	Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
	    	parametersList.add(getSubscriptionActivationDate(subscriber.getStartDate()));
	    	
	    	if(LogSupport.isDebugEnabled(ctx))
	    	{
	    		LogSupport.debug(ctx, APIGenericParameterSupport.class.getName(), "Setting Subscriber Activation Date: " +subscriber.getStartDate());
	    	}
	    	
	    	parametersList.add(getSubscriptionExpiryDate(subscriber.getExpiryDate()));
	    	
	    	parameters = parametersList.toArray(new GenericParameter[]{});
    	}    	
    	// else return null
    	
        return parameters;
        
    }
    
    public static GenericParameter getSubscriptionActivationDate(Date activationDate)
    {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(activationDate);
    	
    	GenericParameter parameter = new GenericParameter();
    	parameter.setName(START_DATE);
    	parameter.setValue(cal);
    	
    	return parameter;
    }
    
    public static GenericParameter getSubscriptionExpiryDate(Date expiryDate)
    {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(expiryDate);
    	
    	GenericParameter parameter = new GenericParameter();
    	parameter.setName(SUB_EXPIRY_DATE);
    	parameter.setValue(cal);
    	
    	return parameter;
    }
    
    public static GenericParameter getServiceTypeParameter(Short serviceTypeIndex)
    {
    	GenericParameter parameter = new GenericParameter();
    	parameter.setName(SERVICE_TYPE);
    	parameter.setValue((long)serviceTypeIndex);
    	
    	return parameter;
    }
    
    public static GenericParameter getApplyMRCGroupParam(boolean applyMRCGroup)
    {
    	GenericParameter parameter = new GenericParameter();
    	parameter.setName(APPLY_MRC_GROUP);
    	parameter.setValue(applyMRCGroup);
    	
    	return parameter;
    }
    
    public static GenericParameter getIsPrimaryParam(boolean isPrimary)
    {
    	GenericParameter parameter = new GenericParameter();
    	parameter.setName(IS_PRIMARY);
    	parameter.setValue(isPrimary);
    	
    	return parameter;
    }
    
    public static GenericParameter getIsPlanChangeScheduledParam(boolean isPlanChangeScheduled)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(IS_PLAN_CHANGE_SCHEDULED);
        parameter.setValue(isPlanChangeScheduled);
        
        return parameter;
    }
    
    public static GenericParameter getIsUsePlanFeeParam(boolean isUsePlanFee)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(IS_USE_PLAN_FEE);
        parameter.setValue(isUsePlanFee);
        
        return parameter;
    }
    
    public static GenericParameter getIsPersonalizedFeeParameter(boolean value)
	 {
		GenericParameter parameter = new GenericParameter();
		parameter.setName(IS_PERSONALIZED_FEE);
		parameter.setValue(value);

		return parameter;
	 }

	public static GenericParameter getPersonalizedFeeParameter(long value)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(PERSONALIZED_FEE);
		parameter.setValue(value);

		return parameter;
	}
	public static GenericParameter getIsPersonalizedFeeApplied(boolean value)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(IS_PERSONALIZED_FEE_SELECTED);
		parameter.setValue(value);

		return parameter;
	}
	
	public static GenericParameter getServiceQuantity(long serviceQuantity)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(SERVICE_QUANTITY);
		parameter.setValue(serviceQuantity);

		return parameter;
	}
    
	public static GenericParameter getChangeIndicator(String changeIndicator) {
		GenericParameter parameter = new GenericParameter();
		parameter.setName(CHANGE_INDICATOR);
		parameter.setValue(changeIndicator);

		return parameter;
	}
    
	public static GenericParameter getAuxiliaryServiceIsPersonalizedFeeParameter(boolean value)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(IS_PERSONALIZED_FEE);
		parameter.setValue(value);

		return parameter;
	}
	
	
	
	public static GenericParameter getAuxiliaryServicePersonalizedFeeParameter(long value)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(PERSONALIZED_FEE);
		parameter.setValue(value);

		return parameter;
	}
	public static GenericParameter getIsAuxiliaryServicePersonalizedFeeApplied(boolean value)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(IS_PERSONALIZED_FEE_SELECTED);
		parameter.setValue(value);

		return parameter;
	}


    /**
     * 
     * Populates the passed array of Generic Parameters with information related to
     * Flex Bundles and Secondary Balance Bundles.
     * 
     * @param ctx
     * @param buckets
     * @param params
     * @param startIndex
     * @return
     */
    public static GenericParameter[] getBundleProfileGenericParameters(final Context ctx,
            List<SubscriberBucket> buckets, GenericParameter[] params, int startIndex)
    {
        StringBuffer primaryIds = new StringBuffer();
        StringBuffer secondaryIds = new StringBuffer();
        StringBuffer secondaryBalanceCategoryIds = new StringBuffer();
        HashSet<Integer> flexIds  = new HashSet<Integer>();
        for (SubscriberBucket bucket : buckets)
        {
            try
            {
                com.redknee.app.crm.bean.core.BundleProfile profile = BundleSupportHelper.get(ctx).getBundleProfile(
                        ctx, bucket.getBundleId());
                if (profile.isFlex())
                {
                    if (profile.getFlexType() == FlexTypeEnum.SECONDARY)
                    {
                        if (secondaryIds.length() > 0)
                        {
                            secondaryIds.append(CharValue.COMMA);
                        }
                        secondaryIds.append(profile.getBundleId());

                    }
                    if (profile.getFlexType() == FlexTypeEnum.ROOT)
                    {
                        if (primaryIds.length() > 0)
                        {
                            primaryIds.append(CharValue.COMMA);
                        }
                        primaryIds.append(profile.getBundleId());
                    }         
                    flexIds.add(Integer.valueOf(profile.getBundleCategoryId()));
                }
                else if(profile.isSecondaryBalance(ctx))
                {
                	if(secondaryBalanceCategoryIds.length() > 0)
                	{
                		secondaryBalanceCategoryIds.append(CharValue.COMMA);
                	}
                	secondaryBalanceCategoryIds.append(profile.getBundleCategoryId());
                }
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(APIGenericParameterSupport.class, " Unable to find bundle profile for bundleId " + bucket.getBundleId()  , homeEx).log(ctx);
            }
            catch(InvalidBundleApiException invalidEx)
            {
                new MinorLogMsg(APIGenericParameterSupport.class, " Unable to find bundle profile for bundleId " + bucket.getBundleId()  , invalidEx).log(ctx);                
            }

        }
        if (!primaryIds.toString().isEmpty())
        {
            params[startIndex] = APIGenericParameterSupport.getBundleProfilePrimaryFlexBundleIDs(primaryIds.toString());
        }
        if (!secondaryIds.toString().isEmpty())
        {
            params[startIndex + 1] = APIGenericParameterSupport.getBundleProfileSecondaryFlexBundleIDs(secondaryIds
                    .toString());
        }
        if (!flexIds.isEmpty())
        {
            params[startIndex + 2] = APIGenericParameterSupport.getBundleProfileCategoryIDs(flexIds);
        }
        
        if(!secondaryBalanceCategoryIds.toString().isEmpty())
        {
        	params[startIndex + 3] = getSecondaryBalanceCategoryIds(secondaryBalanceCategoryIds.toString());
        }
        
        return params;
    }
    
    public static GenericParameter getSecondaryBalanceCategoryIds(final String csvSecondaryBalanceCategoryIds)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(SECONDARY_BALANCE_BUNDLE_CATEGORY_ID);
        parameter.setValue(csvSecondaryBalanceCategoryIds);
        
        return parameter;    	
    }

    
    public static GenericParameter getBundleProfileCategoryIDs(final HashSet<Integer> setIds)
    {
        StringBuffer categoryIDs = new StringBuffer();
        boolean notFirstTime = false;
        for (Integer id : setIds)
        {
            if (notFirstTime)
            {
                categoryIDs.append(',');                
            }
            categoryIDs.append(id.toString());
            notFirstTime = true;
        }
        
        
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BUNDLE_PROFILE_FLEX_BUNDLE_CATEGORY_ID);
        parameter.setValue(categoryIDs.toString());
        
        return parameter;
    }
    
    
    public static GenericParameter getBundleProfilePrimaryFlexBundleIDs(final String primaryIds)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BUNDLE_PROFILE_PRIMARY_FLEX_BUNDLE_ID);
        parameter.setValue(primaryIds);
        
        return parameter;
    }
    
    public static GenericParameter getBundleProfileSecondaryFlexBundleIDs(final String secondaryIds)
    {
        GenericParameter parameter = new GenericParameter();
        parameter.setName(BUNDLE_PROFILE_SECONDARY_FLEX_BUNDLE_ID);
        parameter.setValue(secondaryIds);
        
        return parameter;
    }

	/**
	 * If parameter {@link name} exists in {@link parameters} and has true
	 * value, then
	 * returns true. Otherwise, false is returned.
	 * 
	 * @param name
	 * 
	 *            parameter name to search for
	 * @param parameters
	 *            an array of {@link GenericParameter}, possibly null
	 * @return
	 */
    public static boolean getParameterBoolean(final String name, final GenericParameter[] parameters)
    {
        if (name == "" || name == null || parameters == null)
        {
            return false;
        }
        for (GenericParameter parameter : parameters)
        {
            if (name.equals(parameter.getName()))
            {
                return getValueBoolean(parameter.getValue());
            }
        }
        return false;
    }


    public static boolean getValueBoolean(final Object value)
    {
        if (value == null)
        {
            return false;
        }
        else if (value instanceof Boolean)
        {
            return ((Boolean) value).booleanValue();
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        return false;
    }

    public static Boolean getBooleanOrNull(final Object value)
    {
        if (value instanceof Boolean)
        {
            return ((Boolean) value);
        }
        else if (value instanceof String)
        {
            return Boolean.valueOf((String) value);
        }
        return null;
    }
    
    /**
     * Deprecated
     * Use com.redknee.app.crm.api.rmi.GenericParameterParser
     * @param parameters
     * @return
     */
    @Deprecated
    public static Map<String, Object> createGenericParameterMap(GenericParameter[] parameters)
    {
        if (parameters == null)
        {
            return Collections.emptyMap();
        }
        final Map<String, Object> map = new HashMap<String, Object>(parameters.length);
        for (GenericParameter parameter : parameters)
        {
            map.put(parameter.getName(), parameter.getValue());
        }
        return map;
    }
    
    /**
     * 
     * Verify if 'RequestBSSForwardToOCG' is available in the generic parameter map. 
     * if present and true return true else, false.
     * 
     * @param genericParameterMap
     * @return true if RequestBSSForwardToOCG:=true else false.
     */
    public static boolean isUnappliedTransaction(Map<String,Object> genericParameterMap) throws CRMExceptionFault
    {
    	boolean unapplied = false;
    	
    	if( genericParameterMap.containsKey(REQ_BSS_FWD_TO_OCG))
    	{
    	    if (genericParameterMap.get(REQ_BSS_FWD_TO_OCG) instanceof Boolean)
    	    {
        	    if (((Boolean)genericParameterMap.get(REQ_BSS_FWD_TO_OCG)).booleanValue())
        	    {
        	        if ((genericParameterMap.get(ORIGINATING_APPLICATION).equals(SELF_CARE)))
        	        {
        	            unapplied = true;
        	        }
        	        else
        	        {
        	            RmiApiErrorHandlingSupport.simpleValidation("parameters", "Generic Paramater RequestBSSForwardToOCG can only be set to true if the originating application is Self Care");
        	        }
        	    }
    	    }
    	    else
    	    {
                RmiApiErrorHandlingSupport.simpleValidation("parameters", "Generic Paramater RequestBSSForwardToOCG must be a Boolean");
    	    }
    	}
    	
    	return unapplied;
    }
    
    /**
	 *  This method is used while doing PP change if  we add  new Services without replace ID 
	 *   under New PP Change Enhancement  
	 */


	public static GenericParameter[] getSubscriberServiceParamAddInPPChange(SubscriberServices service , long pricePlanID)
	{
		String DATE_FORMAT_NOW ="yyyy-MM-ddZ";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		SubscriberServices tempService = null;

		GenericParameter parameter1 = new GenericParameter();
		parameter1.setName(APIGenericParameterSupport.PRICEPLAN_ID);
		parameter1.setValue(pricePlanID);

		GenericParameter[] arr = new GenericParameter[3];
		arr[0] = parameter1;
		return arr;
}
	
	public static GenericParameter getPath(Context ctx, Object obj)
	{
		GenericParameter parameter = new GenericParameter();
		parameter.setName(PATH);

		if (obj instanceof ServiceFee2)
		{
			ServiceFee2 fee = (ServiceFee2) obj;
			parameter.setValue(fee.getPath());
		}

		return parameter;
	}



	/**
	 *  This method is used while doing PP change if  we remove  old  Services from OLD PP 
	 *   under New PP Change Enhancement  
	 */


	public static GenericParameter[] getSubscriberServiceParamforServiceGettingRemovedFromOldPP(Context ctx ,Subscriber subscriber , SubscriberServices service)
	{
		//String DATE_FORMAT_NOW = "yyyy-MM-dd";
		String DATE_FORMAT_NOW ="yyyy-MM-ddZ";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		SubscriberServices tempService = null;

		Properties  p = subscriber.getOldPPServiceRemovePropOnPPChange(service.getServiceId());

		GenericParameter parameter1 = new GenericParameter();
		parameter1.setName(APIGenericParameterSupport.PRICEPLAN_ID);
		String pricePlanId = null;
		if(p != null){
			if(p.containsKey(APIGenericParameterSupport.PRICEPLAN_ID) && p.get(APIGenericParameterSupport.PRICEPLAN_ID) != null){
				pricePlanId = (String)p.get(APIGenericParameterSupport.PRICEPLAN_ID);
			}
		}

		parameter1.setValue(pricePlanId);

		GenericParameter[] arr = new GenericParameter[3];
		arr[0] = parameter1;

		return arr;
	}
}
