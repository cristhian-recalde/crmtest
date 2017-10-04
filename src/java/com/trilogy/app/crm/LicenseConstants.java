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
package com.trilogy.app.crm;

import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.extension.account.GroupPricePlanExtension;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;

public interface LicenseConstants extends CoreCrmLicenseConstants
{

   


    public static final String PREPAID_GROUP_POOLED_LICENSE_KEY = "PREPAID_GROUP_POOLED_LICENSE_KEY";

    public static final String GROUP_MEMBER_QUOTA_LIMITS_KEY = "GROUP_MEMBER_QUOTA_LIMITS_KEY";
    
    public static final String DORMANT_STATE_LICENSE_KEY = "DORMANT_STATE_LICENSE_KEY";

    /**
     * MT Call History feature License Key.
     * HLD 5.2.29 object 32070
     */
    public static final String MT_CALL_HISTORY_LICENSE_KEY = "MT_CALL_HISTORY_LICENSE_KEY";
    
    /**
     * Closed User Group now requires a license key to validate
     * TT : 7012244068 
     */
    public static final String CUG_MSISDN_VALIDATION_LICENSE = "CUG_MSISDN_VALIDATION_LICENSE";



    /**
     * Friends and Family Birthday Plan License
     */
    public static final String FNF_BIRTHDAYPLAN_LICENSE = "FNF_BIRTHDAYPLAN_LICENSE";


    /**
     * enabled if hlr service is used, if the customer have both app.crm.driver.hlrService and app.crm.driver.sog license, 
     * HLR service will be used. 
     */
    public static final String LICENSE_DRIVER_HLR_SERVICER = "app.crm.driver.hlrService";
    
    /** 
     * enabled if SOG is used
     */
    public static final String LICENSE_DRIVER_HLR_SOG = "app.crm.driver.sog";
    
    /**
     * enabled if Nortel SOG is used. 
     */
    public static final String LICENSE_DRIVER_HLR_NORTEL = "app.crm.driver.nortel";
    
    /**
     *  enabled if voice mail is offered
     */
    public static final String LICENSE_APP_CRM_VOICE_MAIL = "app.crm.voicemail";
    
    /**
     * enabled if mpathix is used
     */
    public static final String LICENSE_APP_CRM_VOICE_MAIL_MPATHIX = "app.crm.voicemail.mpathix";
    
    /**
     * enagle if SOG is used. 
     */
    public static final String LICENSE_APP_CRM_VOICE_MAIL_SOG = "app.crm.voicemail.sog";

    /**
     * PIN Manager license key
     */
    public static final String PIN_MANAGER_LICENSE_KEY = "PIN Manager";

    /**
     * License Key for account service API
     */
    public static final String ACCT_SVC_LICENSE_KEY = "ACCOUNT_SERVICE_API_LICENSE";

    /**
     * License Key for billing service API
     */
    public static final String BILLING_SVC_LICENSE_KEY = "BILLING_SERVICE_API_LICENSE";


    /**
     * Technology Segmentation Licence Key GSM
     */
    public static final String GSM_LICENSE_KEY = "GSM_FEATURE_LICENSE";
    
    

    /**
     * Auto BillCycle Assignment License Key
     */
    public static final String AUTO_BILLCYCLE_KEY = "AUTO_BILLCYCLE_LICENSE";
    
    /**
     * This is the key for the EVDO license.
     */
    public static final String EVDO_LICENSE = "EVDO Service";
    
    /**
     * This is the key for the PPSM license.
     */
    public static final String PPSM_LICENSE = "PPSM Support";

    /**
     * This is the key for the Overdraft Balance license.
     */
    public static final String OVERDRAFT_BALANCE_LICENSE = "Overdraft Balance Limit Support";
    
    /**
     * This is the key for the Msisdn Swap Limit SPID Extension.
     */
    public static final String MSISDN_SWAP_LIMIT_SPID_EXTENSION = "com.redknee.app.crm.extension.spid.MsisdnSwapLimitSpidExtension";

    /**
     * This is the key for the Overdraft Balance license.
     */
    public static final String OVERDRAFT_BALANCE_LIMIT_ENFORCEMENT_LICENSE = "Overdraft Balance Limit Enforcement on API";
    
    /**
     * This is the key for the Overdraft Balance license.
     */
    public static final String SUBSCRIBER_FIXED_STOP_PRICEPLAN_LICENSE = "Fixed Stop Subscriber Price Plan Support";
    
    public static final String SUBSCRIBER_FIXED_STOP_PRICEPLAN_BUCKET_REPROV_LICENSE = "Fixed Stop Subscriber Price Plan Support - Disable Bucket Reprovision";
    
    
    /**
     * Account Extension licenses
     */
    public static final String ACCOUNT_EXTENSION_SUBSCRIBER_LIMIT_LICENSE = SubscriberLimitExtension.class.getName();
    public static final String ACCOUNT_EXTENSION_FRIENDS_AND_FAMILY_LICENSE = FriendsAndFamilyExtension.class.getName();
    public static final String ACCOUNT_EXTENSION_GROUP_PRICE_PLAN_LICENSE = GroupPricePlanExtension.class.getName();

    public static final String ACCOUNT_CREATION_TEMPLATE_LICENSE = "ACCOUNT_CREATION_TEMPLATE_LICENSE";
    
    public static final String NEWDEPLOYMENT_LICENSE_KEY = "New_Deployment";
    
    /**
     * License to disable tracking of extraneous parameters (i.e. MSISDN) in the 
     * RIM Provisioning Server.  By default, this license is not enabled, and CRM
     * does track the extra (MSISDN) information in the Provisioning System.
     */
    public static final String BLACKBERRY_DISABLE_PARAM_TRACKING = "Disable BlackBerry Parameter Tracking";

    /**
     * License to enable Advanced Features Support
     */
    public static final String ADVANCED_FEATURES_SUPPORT = "Advanced Features Menu Support";

    public static final String ACCOUNT_REGISTRATION = "Account Registration";

    /**
     * Dunning reports
     */
    public static final String DUNNING_REPORT_SUPPORT = "Dunning Report Support";

    /**
     * License to automatically process dunning exempt accounts
     */
    public static final String DUNNING_REPORT_AUTOMATIC_EXEMPT_ACCOUNTS_PROCESSING = "Automatic processing of exempt accounts on Dunning Report generation";

    /**
     * License to enable Deposit Charge adjustmenttype rather than Deposit Made
     */
    public static final String DEPOSIT_CHARGE_ADJUSTMENT = "Use Deposit Charge Adjustment";

    /**
     * License to enable Deposit Charge adjustmenttype rather than Deposit Made
     */
    public static final String RECURRING_ONETIME_OCG_CHARGE = "Charge Ocg Once During Recurring Charges";
    
    /**
     * License to enable cross unit bundles on BSS
     */
    public static final String CROSS_UNIT_BUNDLES = "Cross Service Bundles Support";

    /**
     * License to use only one character for SPID when extracting POS files (even if SPID can have up to 5 characters).
     */
    public static final String STUPID_STORK_HACK_POS_FILE_ONE_CHARACTER = "Use ONE character for SPID in extracted POS files (Stupid hack)";
    
    /**
     * License to enable the validation of the account SMS number SPID being the same SPID as the account one. 
     */
    public static final String ACCOUNT_SMS_NUMBER_SPID_VALIDATION = "Account SMS Number SPID validation";

    /**
     * License to enable the validation of the account SMS number SPID being the same SPID as the account one. 
     */
    public static final String ACCOUNT_POOLED_QUOTA_USAGE_BALANCE_SCREEN = "Pooled Account Quota Usage Display Support";

    
    /**
     * Telus gateway license. 
     */
    public static final String TELUS_GATEWAY_LICENSE_KEY = "MVNE_GATEWAY_LICENSE_KEY";
    
    public static final String 	VOICEMAIL_GRR_LICENSE = "VOICEMAIL_GRR_LICENSE_KEY";
}
