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

import java.security.Permission;

import com.trilogy.framework.xhome.auth.SimplePermission;

import com.trilogy.app.crm.util.OMHelper;

/**
 * contains common constants
 */
public interface Common extends CommonTime, com.redknee.app.crm.calculation.Constants
{
    // This is a small test to ensure that the compiler is JVM 1.5 or newer,
    // since that is where the autoboxing feature was introduced.
    Integer JAVA5_CHECK = 5;

    public static final Permission PRICE_PLAN_RESTRICTION_OVERRIDE_PERMISSION = new SimplePermission("app.crm.priceplan.restrictionoverride.*");
    public static final Permission PRICE_PLAN_RESTRICTION_OVERRIDE_CONTRACT_PERMISSION = new SimplePermission("app.crm.priceplan.restrictionoverride.contractduration");

    public static final String BAS_HOSTNAME = "candy";

    public static final String MOVE_SUBSCRIBER = "Move Subscriber";

    // moved here from ProvisioningHome
    public static final String CREDIT_LIMIT_CHANGE_EXECUTOR = "ups.credit_limit.executor";
    

    //This is the Activation Reason used for subscriber conversion
    public static final String CONVERSION_ACTIVATION_REASON_CODE = "Conversion";

    /** OM module name */
    public static final String OM_MODULE = "CRM";

    /** OM name */
    public static final String OM_SUB_STATE_CHANGE_ATTEMPT = "Subs_State_Change_Attempt";

    /** OM name */
    public static final String OM_SUB_STATE_CHANGE_SUCCESS = "Subs_State_Change_Success";

    /** OM name */
    public static final String OM_SUB_STATE_CHANGE_FAIL = "Subs_State_Change_Fail";

    /** OM name */
    public static final String OM_SUB_CREATE_ATTEMPT = "Subs_Create_Attempt";

    /** OM name */
    public static final String OM_SUB_CREATE_SUCCESS = "Subs_Create_Success";

    /** OM name */
    public static final String OM_SUB_CREATE_FAIL = "Subs_Create_Fail";

    /** OM name */
    public static final String OM_SUB_ACTIVATE_ATTEMPT = "Subs_Activate_Attempt";

    /** OM name */
    public static final String OM_SUB_ACTIVATE_SUCCESS = "Subs_Activate_Success";

    /** OM name */
    public static final String OM_SUB_ACTIVATE_FAIL = "Subs_Activate_Fail";

    /** OM name */
    public static final String OM_SUB_SUSPEND_ATTEMPT = "Subs_Suspend_Attempt";

    /** OM name */
    public static final String OM_SUB_SUSPEND_SUCCESS = "Subs_Suspend_Success";

    /** OM name */
    public static final String OM_SUB_SUSPEND_FAIL = "Subs_Suspend_Fail";

    /** OM name */
    public static final String OM_SUB_DEACTIVATE_ATTEMPT = "Subs_Deactivate_Attempt";

    /** OM name */
    public static final String OM_SUB_DEACTIVATE_SUCCESS = "Subs_Deactivate_Success";
    /** OM name */
    public static final String OM_SUB_LOCKING_SUCCESS = "Subscriber_Barred_Success";

    /** OM name */
    public static final String OM_SUB_DEACTIVATE_FAIL = "Subs_Deactivate_Fail";
    /** OM name */
    public static final String OM_SUB_LOCKING_FAIL = "Subscriber_Barred_Fail";

    /** OM name */
    public static final String OM_SUB_EXPIRE_ATTEMPT =  "Subs_Expire_Attempt";

    /** OM name */
    public static final String OM_SUB_EXPIRE_SUCCESS = "Subs_Expire_Success";

    /** OM name */
    public static final String OM_SUB_EXPIRE_FAIL = "Subs_Expire_Fail";

    // Subscriber Reactivation
    public static final String OM_SUB_REACTIVATE_ATTEMPT = "Subs_Reactivate_Attempt";

    public static final String OM_SUB_REACTIVATE_SUCCESS = "Subs_Reactivate_Success";

    public static final String OM_SUB_REACTIVATE_FAIL = "Subs_Reactivate_Fail";

    public static final Object VIEW_MSG = "Account Note View Message";

    /** OM name */
    public static final String OM_SUB_REMOVE_ATTEMPT = "Subs_Remove_Attempt";

    /** OM name */
    public static final String OM_SUB_REMOVE_SUCCESS = "Subs_Remove_Success";

    /** OM name */
    public static final String OM_SUB_REMOVE_FAIL = "Subs_Remove_Fail";

    /** OM name */
    public static final String OM_SUB_CONVERSION_ATTEMPT = "Sub_Conversion_Attempt";

    /** OM name */
    public static final String OM_SUB_CONVERSION_SUCCESS = "Sub_Conversion_Success";

    /** OM name */
    public static final String OM_SUB_CONVERSION_FAIL = "Sub_Conversion_Fail";

    /** OM name */
    public static final String OM_SUB_ADJUSTMENT = "Sub_Adjustment";

    /** OM name */
    public static final String OM_SMSB_ERROR = "SMSB_Error";

    /** OM name */
    public static final String OM_IPC_ERROR = "IPC_Error";

    /** OM name */
    public static final String OM_ECP_ERROR = "ECP_Error";

    /** OM name */
    public static final String OM_OCG_ERROR = "OCG_Error";

    /** OM name */
    public static final String OM_HLR_PROV_ERROR = "HLR_Error";

    /** OM name */
    public static final String OM_VOICEMAIL_ERROR = "VOICEMAIL_Error";

    /** OM name */
    public static final String OM_UPS_ERROR = "UPS_Prov_Error";

    /** OM name */
    public static final String OM_PM_ERROR = "PM_Prov_Error";

    /** OM name */
    public static final String OM_ALCATEL_PROV = "Alcatel_Prov";
    
    /** OM name */
    public static final String OM_ALCATEL_PROV_ERROR = "Alcatel_Prov_Error";
    
    /** OM name */
    public static final String OM_ALCATEL_SERVICE_UNPROV_ERROR = "Alcatel_Service_UnProv_Error";
    
    /** OM name */
    public static final String OM_ALCATEL_SERVICE_SUSPEND_ERROR = "Alcatel_Service_Suspend_Error";
    
    /** OM name */
    public static final String OM_ALCATEL_SERVICE_RESUME_ERROR = "Alcatel_Service_Resume_Error";
    
    /** OM name */
    public static final String OM_ALCATEL_ACCOUNT_UNPROV_ERROR = "Alcatel_Account_UnProv_Error";
        
    /** OM name */
    public static final String OM_PRICE_PLAN_CHANGE = "Price_Plan_Change";

    /** OM name */
    public static final String OM_PRICE_PLAN_CHANGE_ERROR = "Price_Plan_Change_Error";

    /** OM name */
    public static final String OM_CREDIT_LIMIT_CHANGE = "Credit_Limit_Change";

    /** OM name */
    public static final String OM_SUBSCRIBER_STATE_CHANGE = "Subscriber_State_Change";

    /** OM name */
    public static final String OM_PACKAGE_SWAP = "Package_Swap_Change";

    /** OM name */
    public static final String OM_ACCT_ACTIVATE_ATTEMPT = "Acct_Activate_Attempt";

    /** OM name */
    public static final String OM_ACCT_ACTIVATE_FAIL = "Acct_Activate_Fail";

    /** OM name */
    public static final String OM_ACCT_ACTIVATE_SUCCESS = "Acct_Activate_Success";

    /** OM name */
    public static final String OM_ACCT_MODIFY_ATTEMPT = "Acct_Modify_Attempt";

    /** OM name */
    public static final String OM_ACCT_MODIFY_FAIL = "Acct_Modify_Fail";

    /** OM name */
    public static final String OM_ACCT_MODIFY_SUCCESS = "Acct_Modify_Success";

    /** OM name */
    public static final String OM_ACCT_MOVE_ATTEMPT = "Acct_Move_Attempt";

    /** OM name */
    public static final String OM_ACCT_MOVE_FAIL = "Acct_Move_Fail";

    /** OM name */
    public static final String OM_ACCT_MOVE_SUCCESS = "Acct_Move_Success";
    
    /** OM name */
    public static final String OM_ACCT_CONVERSION_ATTEMPT = "Acct_Conversion_Attempt";

    /** OM name */
    public static final String OM_ACCT_CONVERSION_FAIL = "Acct_Conversion_Fail";

    /** OM name */
    public static final String OM_ACCT_CONVERSION_SUCCESS = "Acct_Conversion_Success";

    /** OM name */
    public static final String OM_ACCT_TYPE_CHANGE_ATTEMPT = "Acct_Type_Change_Attempt";

    /** OM name */
    public static final String OM_ACCT_TYPE_CHANGE_FAIL = "Acct_Type_Change_Fail";

    /** OM name */
    public static final String OM_ACCT_TYPE_CHANGE_SUCCESS = "Acct_Type_Change_Success";

    /** OM name */
    public static final String OM_ACCT_REMOVE_ATTEMPT = "Acct_Remove_Attempt";

    /** OM name */
    public static final String OM_ACCT_REMOVE_SUCCESS = "Acct_Remove_Success";

    /** OM name */
    public static final String OM_ACCT_REMOVE_FAIL = "Acct_Remove_Fail";

    /** OM name */
    public static final String OM_PRICE_PLAN_MODIFY = "Price_Plan_Modification";

    /** OM name */
    public static final String OM_CLCT_ER = "Credit_Limit_Threshold_ER";

    /** OM name */
    public static final String OM_VRA_ER = "VRA_TRANSACTION_ER";

    public static final String OM_OCG375_ER = "OCGER375Poll";

    public static final OMHelper OM_OCG375 = new OMHelper(OM_OCG375_ER);

    //Generates OMs for FnFSelfCare Poller
    public static final String OM_FnFSELFCARE_1900_ER = "FnF_SELF_CARE_ERPoll";
    public static final OMHelper OM_FnFSELFCARE_1900 = new OMHelper(OM_FnFSELFCARE_1900_ER);


    /**
     * Generates OMs for screening templates removal.
     */
    public static final String OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018_ER = "PPSM_Screening_Template_Removal";
    public static final OMHelper OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018 = new OMHelper(OM_PPSM_SCREENING_TEMPLATE_REMOVAL_7018_ER);

    /**
     * Generates OMs for price plan version modification.
     */
    public static final OMHelper OM_PRICE_PLAN_VERSION_MODIFICATION = new OMHelper("Price_Plan_Modification");

    /**
     * Generates OMs for price plan version deletion.
     */
    public static final OMHelper OM_PRICE_PLAN_VERSION_DELETION = new OMHelper("Price_Plan_Version_Deletion");

    /**
     * Generates OMs for price plan version activation.
     */
    public static final OMHelper OM_PRICE_PLAN_VERSION_ACTIVATION = new OMHelper("Price_Plan_Version_Activation");

    /**
     * Generates OMs for free call time template creation.
     */
    public static final OMHelper OM_FREE_CALL_TIME_TEMPLATE_CREATION = new OMHelper("FCTT_Create");

    public static final String DEFAULT_FONT = "Courier";

    /**
     * Generates OMs for free call time template modification.
     */
    public static final OMHelper OM_FREE_CALL_TIME_TEMPLATE_MODIFICATION = new OMHelper("FCTT_Modify");

    public static final String BILL_MARKER = "*";

    /**
     * Generates OMs for free call time template deletion.
     */
    public static final OMHelper OM_FREE_CALL_TIME_TEMPLATE_DELETION = new OMHelper("FCTT_Delete");

    public static final int DEFAULT_FONT_SIZE = 8;

    /** OM name */
    public static final String OM_ACCT_SUSPEND_ATTEMPT = "Acct_Suspend_Attempt";

    /** OM name */
    public static final String OM_ACCT_SUSPEND_FAIL = "Acct_Suspend_Fail";

    /** OM name */
    public static final String OM_ACCT_SUSPEND_SUCCESS = "Acct_Suspend_Success";

    /** OM name */
    public static final String OM_ACCT_DEACTIVATE_ATTEMPT = "Acct_Deactivate_Attempt";

    /** OM name */
    public static final String OM_ACCT_DEACTIVATE_FAIL = "Acct_Deactivate_Fail";

    /** OM name */
    public static final String OM_ACCT_DEACTIVATE_SUCCESS = "Acct_Deactivate_Success";

    /** OM name */
    public static final String OM_ACCT_PTP_ATTEMPT = "Acct_PTP_DEACTIVATE_Attempt";

    /** OM name */
    public static final String OM_ACCT_PTP_FAIL = "Acct_PTP_Fail";

    /** OM name */
    public static final String OM_ACCT_PTP_SUCCESS = "Acct_PTP_Success";

    /** OM name */
    public static final String OM_POLLER_MOUNT_CONN_LOST = "Mount_Connection_Lost";

    /** OM name */
    public static final String OM_POLLER_MOUNT_ERROR = "Mount_Error";

    /** OM name. Attempts to apply recurring charges to an account. */
    public static final String OM_RECURRING_CHARGE_ATTEMPT = "Recurring Charge_Attempt";

    /** OM name. Successfull recurring charge applied. */
    public static final String OM_RECURRING_CHARGE_SUCCESS = "Recurring Charge_Success";

    /** OM name. Failed recurring charge application. */
    public static final String OM_RECURRING_CHARGE_FAIL = "Recurring Charge_Fail";

    /** OM name. Attempts to apply roaming charge to an account. */
    public static final String OM_ROAMING_CHARGE_ATTEMPT = "Roaming Charge_Attempt";

    /** OM name. Successfull roaming charge applied to an account. */
    public static final String OM_ROAMING_CHARGE_SUCCESS = "Roaming Charge_Success";

    /** OM name. Failed roaming charge applied to account. */
    public static final String OM_ROAMING_CHARGE_FAIL = "Roaming Charge_Fail";

    /** OM name */
    public static final String OM_DROPPED_CALL_REIMBURSEMENT_ATTEMPT = "Dropped_Call_Reimbursement_Attempt";

    /** OM name */
    public static final String OM_DROPPED_CALL_REIMBURSEMENT_SUCCESS = "Dropped_Call_Reimbursement_Success";

    /** OM name */
    public static final String OM_DROPPED_CALL_REIMBURSEMENT_FAIL = "Dropped_Call_Reimbursement_Fail";

    /** OM name */
    public static final String OM_DUNNING_ACTIVE_ACTION_ATTEMPT = "Dunning_Active_Action_Attempt";

    /** OM name */
    public static final String OM_DUNNING_ACTIVE_ACTION_SUCCESS = "Dunning_Active_Action_Success";

    /** OM name */
    public static final String OM_DUNNING_ACTIVE_ACTION_FAIL = "Dunning_Active_Action_Fail";

    /** OM name */
    public static final String OM_DUNNING_WARNED_ACTION_ATTEMPT = "Dunning_Warned_Action_Attempt";

    /** OM name */
    public static final String OM_DUNNING_WARNED_ACTION_SUCCESS = "Dunning_Warned_Action_Success";

    /** OM name */
    public static final String OM_DUNNING_WARNED_ACTION_FAIL = "Dunning_Warned_Action_Fail";

    /** OM name */
    public static final String OM_DUNNING_DUNNED_ACTION_ATTEMPT = "Dunning_Dunned_Action_Attempt";

    /** OM name */
    public static final String OM_DUNNING_DUNNED_ACTION_SUCCESS = "Dunning_Dunned_Action_Success";

    /** OM name */
    public static final String OM_DUNNING_DUNNED_ACTION_FAIL = "Dunning_Dunned_Action_Fail";

    /** OM name */
    public static final String OM_DUNNING_ARREARED_ACTION_ATTEMPT = "Dunning_Arreared_Action_Attempt";

    /** OM name */
    public static final String OM_DUNNING_ARREARED_ACTION_SUCCESS = "Dunning_Arreared_Action_Success";

    /** OM name */
    public static final String OM_DUNNING_ARREARED_ACTION_FAIL = "Dunning_Arreared_Action_Fail";

    /** OM name. Attempts to apply payments to an account. */
    public static final String OM_PAYMENT_ATTEMPT = "PaymentAttempt";

    /** OM name. Successfull payment applied to account. */
    public static final String OM_PAYMENT_SUCCESS = "PaymentSuccess";

    /** OM name. Failed payment application to account. */
    public static final String OM_PAYMENT_FAIL = "PaymentFail";

    /** OM name */
    public static final String OM_ACCT_ADJUSTMENT_ATTEMP = "AcctAdjustmentAttempt";

    /** OM name */
    public static final String OM_ACCT_ADJUSTMENT_SUCCESS = "AcctAdjustmentSuccess";

    /** OM name */
    public static final String OM_ACCT_ADJUSTMENT_FAIL = "AcctAdjustmentFail";

    /** OM name */
    public static final String OM_VOICEBUCKET_RESET_FAIL = "VoiceBucket_Reset_Fail";

    /** OM name */
    public static final String OM_VOICEBUCKET_RESET_SUCCESS = "VoiceBucket_Reset_Success";

    /** OM name */
    public static final String OM_TEXTBUCKET_RESET_FAIL = "TextBucket_Reset_Fail";

    /** OM name */
    public static final String OM_TEXTBUCKET_RESET_SUCCESS = "TextBucket_Reset_Success";

    /** Failed to reset the data bucket.*/
    public static final String OM_DATABUCKET_RESET_FAIL = "Databucket_Reset_Fail";

    /** Successfully reset the data bucket. */
    public static final String OM_DATABUCKET_RESET_SUCCESS = "Databucket_Reset_Success";

    /** OM name */
    public static final String OM_RECOVERY_PENDING_SUCCESS = "Recovery_Pending_Subscriber_Success";

    /** Attempt to move subscriber from one account to another. */
    public static final String OM_ACCOUNT_CHANGE_ATTEMPT = "Account_Change_Attempt";
    public static final String OM_SUBSCRIBER_MOVE_ATTEMPT = "Subscriber_Move_Attempt";
    public static final String OM_SUBSCRIBER_CONVERSION_ATTEMPT = "Subscription_Conversion_Attempt";

    /** Successfully moved subscriber from one account to another. */
    public static final String OM_ACCOUNT_CHANGE_SUCCESS = "Account_Change_Success";
    public static final String OM_SUBSCRIBER_MOVE_SUCCESS = "Subscriber_Move_Success";
    public static final String OM_SUBSCRIBER_CONVERSION_SUCCESS = "Subscription_Conversion_Success";

    
    /** Failed to move subscriber from one account to another. */
    public static final String OM_ACCOUNT_CHANGE_FAIL = "Account_Change_Fail";
    public static final String OM_SUBSCRIBER_MOVE_FAIL = "Subscriber_Move_Fail";
    public static final String OM_SUBSCRIBER_CONVERSION_FAIL = "Subscription_Conversion_Fail";

    /** key in the context under which the Cached Account home with no other decorators is found */
    public static final String ACCOUNT_CACHED_HOME = "AccountCachedHome";

    /** key in the context Account Transaction home with no other decorators is found */
    public static final String ACCOUNT_TRANSACTION_HOME = "AccountTransactionHome";

    /** key in the context under which the MT Call Detail Home is found */
    public static final String MT_CALL_DETAIL_HOME = "MTCallDetailHome";

    /** key in the context under which the Prepaid Calling Card Call Detail Home is found */
    public static final String PREPAID_CALLING_CARD_CALL_DETAIL_HOME = "PccCallDetailHome";
    
    /** permission name for users with the ability to provision */
    public static final String PROVISIONERS_GROUP_PERMISSION = "provision";

    /** OM name */
    public static final String OM_IPCG_AGGREGATION_ATTEMPT = "Ipcg_Aggregation_Attempt";

    /** OM name */
    public static final String OM_IPCG_AGGREGATION_SUCCESS = "Ipcg_Aggregation_Success";

    /** OM name */
    public static final String OM_IPCG_AGGREGATION_FAIL = "Ipcg_Aggregation_Fail";

    /* Personal List Plan OMs */
    /** OM name */
    public static final String OM_PLP_CREATION_ATTEMPT = "PersonalListPlan_Creation_Attempt";

    /** OM name */
    public static final String OM_PLP_CREATION_SUCCESS = "PersonalListPlan_Creation_Success";

    /** OM name */
    public static final String OM_PLP_CREATION_FAIL = "PersonalListPlan_Creation_Fail";

    /** OM name */
    public static final String OM_PLP_MODIFICATION_ATTEMPT = "PersonalListPlan_Modification_Attempt";

    /** OM name */
    public static final String OM_PLP_MODIFICATION_SUCCESS = "PersonalListPlan_Modification_Success";

    /** OM name */
    public static final String OM_PLP_MODIFICATION_FAIL = "PersonalListPlan_Modification_Fail";

    /** OM name */
    public static final String OM_PLP_DELETION_ATTEMPT = "PersonalListPlan_Deletion_Attempt";

    /** OM name */
    public static final String OM_PLP_DELETION_SUCCESS = "PersonalListPlan_Deletion_Success";

    /** OM name */
    public static final String OM_PLP_DELETION_FAIL = "PersonalListPlan_Deletion_Fail";

    /* Closed User Group Creation */
    public static final String OM_CUG_CREATION_ATTEMPT = "ClosedUserGroup_Creation_Attempt";

    public static final String OM_CUG_CREATION_SUCCESS = "ClosedUserGroup_Creation_Success";

    public static final String OM_CUG_CREATION_FAIL = "ClosedUserGroup_Creation_Fail";

    /* Closed User Group Modification */
    public static final String OM_CUG_MODIFICATION_ATTEMPT = "ClosedUserGroup_Modification_Attempt";

    public static final String OM_CUG_MODIFICATION_SUCCESS = "ClosedUserGroup_Modification_Success";

    public static final String OM_CUG_MODIFICATION_FAIL = "ClosedUserGroup_Modification_Fail";

    /* Closed User Group Deletion */
    public static final String OM_CUG_DELETION_ATTEMPT = "ClosedUserGroup_Deletion_Attempt";

    public static final String OM_CUG_DELETION_SUCCESS = "ClosedUserGroup_Deletion_Success";

    public static final String OM_CUG_DELETION_FAIL = "ClosedUserGroup_Deletion_Fail";

    /* Closed User Group Creation */
    public static final String OM_BP_CREATION_ATTEMPT = "BirthdayPlan_Creation_Attempt";

    public static final String OM_BP_CREATION_SUCCESS = "BirthdayPlan_Creation_Success";

    public static final String OM_BP_CREATION_FAIL = "BirthdayPlan_Creation_Fail";

    /* Closed User Group Modification */
    public static final String OM_BP_MODIFICATION_ATTEMPT = "BirthdayPlan_Modification_Attempt";

    public static final String OM_BP_MODIFICATION_SUCCESS = "BirthdayPlan_Modification_Success";

    public static final String OM_BP_MODIFICATION_FAIL = "BirthdayPlan_Modification_Fail";

    /* Closed User Group Deletion */
    public static final String OM_BP_DELETION_ATTEMPT = "BirthdayPlan_Deletion_Attempt";

    public static final String OM_BP_DELETION_SUCCESS = "BirthdayPlan_Deletion_Success";

    public static final String OM_BP_DELETION_FAIL = "BirthdayPlan_Deletion_Fail";

    // AuxiliaryService Creation
    public static final String OM_AUXILIARY_SERVICE_CREATION_ATTEMPT = "AuxiliaryService_Creation_Attempt";

    public static final String OM_AUXILIARY_SERVICE_CREATION_SUCCESS = "AuxiliaryService_Creation_Success";

    public static final String OM_AUXILIARY_SERVICE_CREATION_FAIL = "AuxiliaryService_Creation_Fail";

    // AuxiliaryService Modification
    public static final String OM_AUXILIARY_SERVICE_MODIFICATION_ATTEMPT = "AuxiliaryService_Modification_Attempt";

    public static final String OM_AUXILIARY_SERVICE_MODIFICATION_SUCCESS = "AuxiliaryService_Modification_Success";

    public static final String OM_AUXILIARY_SERVICE_MODIFICATION_FAIL = "AuxiliaryService_Modification_Fail";

    // AuxiliaryService Deletion
    public static final String OM_AUXILIARY_SERVICE_DELETION_ATTEMPT = "AuxiliaryService_Deletion_Attempt";

    public static final String OM_AUXILIARY_SERVICE_DELETION_SUCCESS = "AuxiliaryService_Deletion_Success";

    public static final String OM_AUXILIARY_SERVICE_DELETION_FAIL = "AuxiliaryService_Deletion_Fail";

    // SubscriberAuxiliaryService Change
    public static final String OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_ATTEMPT =
            "SubscriberAuxiliaryService_Change_Attempt";

    public static final String OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_SUCCESS =
            "SubscriberAuxiliaryService_Change_Success";

    public static final String OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_FAIL = "SubscriberAuxiliaryService_Change_Fail";

    // SAT Creation
    public static final String OM_SAT_CREATION_ATTEMPT = "SubscriberCreationTemplate_Creation_Attempt";

    public static final String OM_SAT_CREATION_SUCCESS = "SubscriberCreationTemplate_Creation_Success";

    public static final String OM_SAT_CREATION_FAIL = "SubscriberCreationTemplate_Creation_Fail";

    // SAT Modification
    public static final String OM_SAT_MODIFICATION_ATTEMPT = "SubscriberCreationTemplate_Modification_Attempt";

    public static final String OM_SAT_MODIFICATION_SUCCESS = "SubscriberCreationTemplate_Modification_Success";

    public static final String OM_SAT_MODIFICATION_FAIL = "SubscriberCreationTemplate_Modification_Fail";

    // SAT Deletion
    public static final String OM_SAT_DELETION_ATTEMPT = "SubscriberCreationTemplate_Deletion_Attempt";

    public static final String OM_SAT_DELETION_SUCCESS = "SubscriberCreationTemplate_Deletion_Success";

    public static final String OM_SAT_DELETION_FAIL = "SubscriberCreationTemplate_Deletion_Fail";

    // Voucher Recharging Process
    public static final String OM_VOUCHER_RECHARGE_ATTEMPT = "Voucher_Recharge_Attempt";

    public static final String OM_VOUCHER_RECHARGE_SUCCESS = "Voucher_Recharge_Success";

    public static final String OM_VOUCHER_RECHARGE_FAILURE = "Voucher_Recharge_Failure";

    /** Default adjustment limit for transactions */
    public static final long DEFAULT_ADJUST_LIMIT = Long.MAX_VALUE;

    /** The delegate Home key*/
    public static final String DELEGATE_HOME = "Delegatehome";

    public static final String PREPAID_POSTPAID_CONVERSION_SUBCRIBER = "PrepaidPostpaidConversionSubscriber";

    public static final String POSTPAID_PREPAID_CONVERSION_SUBCRIBER = "PostpaidPrepaidConversionSubscriber";

    public static final String CONVERSION_SUBCRIBER_ID = "PostpaidPrepaidConversionSubscriberId";
    
    public static final String BILLING_TYPE_CONVERSION = "BillingTypeConversion";

    // Price Plan Creation
    public static final String OM_PRICE_PLAN_CREATION_ATTEMPT = "PricePlan_Creation_Attempt";

    public static final String OM_PRICE_PLAN_CREATION_SUCCESS = "PricePlan_Creation_Success";

    public static final String OM_PRICE_PLAN_CREATION_FAIL = "PricePlan_Creation_Fail";
    public static final String OM_GLCODEN_VERSIONN_CREATION_FAIL = "GLCodeN_VersionN_Creation_Fail";
    
    // SubGLCodeN and SubGLCodeVersionN Creation
    public static final String OM_SUBGLCODEN_VERSIONN_CREATION_FAIL = "SubGLCodeN_VersionN_Creation_Fail";
    /**
     * Generates OMs for SubGLCodeN and SUbGLCodeVersionN modification.
     */
    public static final OMHelper OM_SUBGLCODEN_VERSION_MODIFICATION = new OMHelper("SubGLCodeN_Modification");
    public static final OMHelper OM_SUBGLCODEN_VERSION_DELETION = new OMHelper("SubGLCodeN_Deletion");
    
    
    /**
     * Generates OMs for GLCodeN and GLCodeVersionN modification.
     */
    public static final OMHelper OM_GLCODEN_VERSION_MODIFICATION = new OMHelper("GLCodeN_Modification");
    
    /**
     * Generates OMs for GLCodeNVersion deletion.
     */
    public static final OMHelper OM_GLCODE_VERSION_DELETION = new OMHelper("GLCodeN_Version_Deletion");

    // Blackberry
    public static final String OM_RIM_BB_PROV_ATTEMPT = "RIM_BLACKBERRY_Attempt";

    public static final String OM_RIM_BB_PROV_SUCCESS = "RIM_BLACKBERRY_Success";

    public static final String OM_RIM_BB_PROV_FAILURE = "RIM_BLACKBERRY_Failure";
    
    //Payment Plan Creation
    public static final String OM_PAYMENT_PLAN_CREATION_ATTEMPT = "Payment_Plan_Creation_Attempt";

    public static final String OM_PAYMENT_PLAN_CREATION_FAILURE = "Payment_Plan_Creation_Failure";

    public static final String OM_PAYMENT_PLAN_CREATION_SUCCESS = "Payment_Plan_Creation_Success";

    // Payment Plan Account Installation
    public static final String OM_PAYMENT_PLAN_INSTALLATION_ATTEMPT = "Payment_Plan_Account_Installation_Attempt";

    public static final String OM_PAYMENT_PLAN_INSTALLATION_FAILURE = "Payment_Plan_Account_Installation_Failure";

    public static final String OM_PAYMENT_PLAN_INSTALLATION_SUCCESS = "Payment_Plan_Account_Installation_Success";

    // Payment Plan Removal
    public static final String OM_PAYMENT_PLAN_REMOVAL_ATTEMPT = "Payment_Plan_Removal_Attempt";

    public static final String OM_PAYMENT_PLAN_REMOVAL_FAILURE = "Payment_Plan_Removal_Failure";

    public static final String OM_PAYMENT_PLAN_REMOVAL_SUCCESS = "Payment_Plan_Removal_Success";

    /** Provides OMs for AAA Provisioning. */
    public static final OMHelper OM_AAA_PROVISION = new OMHelper("AAA_Provision");

    /** Provides OMs for AAA Unprovisioning. */
    public static final OMHelper OM_AAA_UNPROVISION = new OMHelper("AAA_Unprovision");
    
    /** Provides OMs for AAA Suspend. */
    public static final OMHelper OM_AAA_SUSPEND = new OMHelper("AAA_Suspend");

    // Call Detail table name prefix
    public static final String CALL_DETAIL_TABLE_PREFIX = "CD";

    //  Rerate Call Detail table name prefix
    public static final String RERATE_CALL_DETAIL_TABLE_PREFIX = "RCD";

    // Call Detail table name prefix
    public static final String MT_CALL_DETAIL_TABLE_PREFIX = "MTCD";

    // Subscriber Move deposit delta
    public static final String SUBSCRIBER_MOVE_DEPOSIT_DELTA = "Subscriber_Move_Deposit_Delta";

    // Force provisioning call to BM
    public static final String FORCE_BM_PROVISION_CALL = "Force_BM_Provision_Call";

    public static final String SUBSCRIBERTYPEENUM_COLLECTION = "SUBSCRIBERTYPEENUM_COLLECTION";

    //Auto-Blocking on Voucher Fraud
    public static final String OM_AUTOBLOCK_VOUCHER_FRAUD = "Auto-Block_on_Voucher_Fraud";

    public static final OMHelper OM_AUTOBLOCK_VOUCHERFRAUD = new OMHelper(OM_AUTOBLOCK_VOUCHER_FRAUD);

    // Auto deposit release criteria creation
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_ATTEMPT = "AutoDepositReleaseCriteria_Creation_Attempt";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_SUCCESS = "AutoDepositReleaseCriteria_Creation_Success";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_CREATE_FAILURE = "AutoDepositReleaseCriteria_Creation_Failure";

    // Auto deposit release criteria modificaton
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_ATTEMPT = "AutoDepositReleaseCriteria_Modification_Attempt";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_SUCCESS = "AutoDepositReleaseCriteria_Modification_Success";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_UPDATE_FAILURE = "AutoDepositReleaseCriteria_Modification_Failure";

    // Auto deposit release criteria deletion
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_ATTEMPT = "AutoDepositReleaseCriteria_Deletion_Attempt";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_SUCCESS = "AutoDepositReleaseCriteria_Deletion_Success";
    public static final String OM_AUTO_DEPOSIT_RELEASE_CRITERIA_DELETE_FAILURE = "AutoDepositReleaseCriteria_Deletion_Failure";

    // Auto deposit release
    public static final String OM_AUTO_DEPOSIT_RELEASE_ATTEMPT = "AutoDepositRelease_Attempt";
    public static final String OM_AUTO_DEPOSIT_RELEASE_SUCCESS = "AutoDepositRelease_Success";
    public static final String OM_AUTO_DEPOSIT_RELEASE_FAILURE = "AutoDepositRelease_Failure";

    public static final String PRICE_PLAN_VERSION_UPDATE_REQUEST_ERROR_HOME = "PricePlanVersionUpdateRequestErrorHome";
    public static final String MOBILE_NUMBER_MONITOR = "MOBILE_NUMBER_MONITOR";

    //Payment Exception Recording
    // Indicates a new Payment Exception Record was added
    public static final String OM_PAYMENT_EXCEPTION_CREATED = "PaymentException_Created";
    // Indicates a Payment Exception Record was updated, probably due to errors while reprocessing the Payment
    public static final String OM_PAYMENT_EXCEPTION_UPDATED = "PaymentException_Updated";
    // Indicates a Payment Exception was resolved; the payment transaction was successfully created,
    // and the Payment Exception record was deleted.
    public static final String OM_PAYMENT_EXCEPTION_RESOLVED = "PaymentException_Resolved";

    //Transfer Exception Recording
    // Indicates a new Transfer Exception Record was added
    public static final String OM_TRANSFER_EXCEPTION_CREATED = "TransferException_Created";    
    // Indicates a Transfer Exception Record was updated, probably due to errors while reprocessing the Payment
    public static final String OM_TRANSFER_CREATED = "Transfer_Created";    
    // Indicates a Transfer Record was updated, probably due to new successful transaction
    
    public static final String OM_TRANSFER_EXCEPTION_UPDATED = "TransferException_Updated";
    // Indicates a Transfer Exception was resolved; the OCG charge was successful
    // and the Transfer Exception record was deleted.
    public static final String OM_TRANSFER_EXCEPTION_RESOLVED = "TransferException_Resolved";
    

    /**
     * Whether dunning is exempted in the transaction pipeline.
     */
    public static final String DUNNING_EXEMPTION = "Transaction_Dunning_Exemption";

    /**
     * The old account. Needed to determine the account state transition from In
     * Arrears to Active/PTP.
     */
    public static final String OLD_ACCOUNT = "OLD_ACCOUNT";

    /**
     * This context field will be set when crm migration is happening
     */
    public static final String DURING_MIGRATION ="DURING_MIGRATION";

    public static final Object DURING_BILL_CYCLE_CHANGE = "DURING_BILL_CYCLE_CHANGE";
    
    /**
     * Unapplied Transaction constants
     */
    public static final String UNAPPLIED_TRANSACTION_HOME = "UNAPPLIED_TRANSACTION_HOME"; 
    
    /**
     * To hold reference of services affected by ER447 
     */
    public static final String ER_447_SUBSCRIBER_CLCT = "ER_447_SUBSCRIBER_CLCT";
    
    /**
     * Parameter received in API call (updateSubscriptionProfile) which would force sub-services to be synced with MVNO
     */
    public static final String SYNC_EXTERNAL_SERVICES = "SyncExternalServices";
    
    /**
     * Parameter received in API call which indicates the time this profile was last modified.
     * 
     */
    public static final String LAST_MODIFIED = "LastModified";
    
    public static final String AGED_DEBT_CALCULATION_SESSION_KEY = "AgedDebtCaLculationSessionKey";
    
    public static final String INVOK_ATU = "ATU";
    
    /**
     * To hold reference of services extension action flag 
     */
    public static final String SERVICETYPE_EXTENSION_ACTION_FLAG = "serviceTypeExtensionActionFlag";
    
    
}
