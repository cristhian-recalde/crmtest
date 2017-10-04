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
package com.trilogy.app.crm.agent;

/**
 * @author larry.xia@redknee.com
 */
public interface CronConstant
{
    /**
     * The name of the cron agent
     */
    public static final String REPORT_AGENT_NAME =
            "Usage Report";

    /**
     * The description of the cron agent
     */
    public static final String REPORT_AGENT_DESCRIPTION =
            "Usage Report ER Generation Process";

    public static final String SERVICE_EXPIRY_NOTIFICATION_AGENT_NAME = "Service Recurrence and Expiry Notification Agent";
    public static final String SERVICE_EXPIRY_NOTIFICATION_AGENT_DESC = "Apply Service Recurrence and Expiry Notification Batch Process";
    
    
    public static final String WRITE_OFF_AGENT_NAME = "Write-off Agent";
    public static final String WRITE_OFF_AGENT_DESC = "Apply Write-off Batch Process";
    /**
     * The name of the cron agent
     */
    public static final String MULTI_DAY_RECURRING_CHARGES_AGENT_NAME =
            "Multi-Day Recurring Charges";

    /**
     * The description of the cron agent
     */
    public static final String MULTI_DAY_RECURRING_CHARGES_AGENT_DESCRIPTION =
            "Apply Multi-Day Recurring Charges";

    
    /**
     * The name of the cron agent
     */
    public static final String MONTHLY_RECURRING_CHARGES_AGENT_NAME =
            "Monthly Recurring Charges";

    /**
     * The description of the cron agent
     */
    public static final String MONTHLY_RECURRING_CHARGES_AGENT_DESCRIPTION =
            "Apply Monthly Recurring Charges";

    /**
     * The name of the cron agent
     */
    public static final String WEEKLY_RECURRING_CHARGES_AGENT_NAME =
            "Weekly Recurring Charges";
    
    /**
     * The name of the cron agent
     */
    public static final String GENERIC_BEAN_BULK_LOAD_PROCESS_NAME = "Generic bean bulk load process";
 
     /**
     * The description of the cron agent
     */
    public static final String GENERIC_BEAN_BULK_LOAD_PROCESS_DESCRIPTION =
               "Generic bean bulk load process";    
    
    /**
    

    /**
     * The description of the cron agent
     */
    public static final String WEEKLY_RECURRING_CHARGES_AGENT_DESCRIPTION =
            "Apply Weekly Recurring Charges";

    /**
     * The name of the cron agent
     */
    public static final String DAILY_RECURRING_CHARGES_AGENT_NAME =
            "Daily Recurring Charges";

    /**
     * The description of the cron agent
     */
    public static final String DAILY_RECURRING_CHARGES_AGENT_DESCRIPTION =
            "Apply Daily Recurring Charges";


    /**
     * The name of the cron agent
     */
    public static final String RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_NAME =
            "Prepaid Recurring Charges Pre-Warning Notification";

    /**
     * The description of the cron agent
     */
    public static final String RECURRING_CHARGES_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION =
            "Send Prepaid Recurring Charges Pre-Warning Notification Messages";
    
    /**
     * The name of the cron agent
     */
    public static final String RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_NAME =
            "Prepaid Recurring Charges Insufficient Balance Pre-Warning Notification";

    /**
     * The description of the cron agent
     */
    public static final String RECURRING_CHARGES_INSUFFICIENT_BALANCE_PRE_WARNING_NOTIFICATION_AGENT_DESCRIPTION =
            "Prepaid Recurring Charges Insufficient Balance Pre-Warning Notification";    


    /**
     * The name of the cron agent
     */
    public static final String PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_NAME =
            "Price Plan Versions Modifications";

    /**
     * The description of the cron agent
     */
    public static final String PRICE_PLAN_VERSIONS_MODIFICATIONS_AGENT_DESCRIPTION =
            "Apply Price Plan Versions Modifications";

    //Moved to Lifecycle.
    /** The name of the cron agent */
    //public static final String TPS_PROVISIONING_AGENT_NAME = "TPSProvisioning";
    /** The description of the cron agent */
    //public static final String TPS_PROVISIONING_AGENT_DESCRIPTION =
    //      "TPS Batch Process";

    /**
     * The name of the cron agent
     */
    public static final String APPLY_ROAMING_CHARGES_AGENT_NAME =
            "ApplyRoamingCharges";

    /**
     * The description of the cron agent
     */
    public static final String APPLY_ROAMING_CHARGES_AGENT_DESCRIPTION =
            "Apply Roaming Charges Batch Process";

    /**
     * The name of the cron agent
     */
    public static final String USER_DAILY_ADJUSTMENT_LIMIT_CLEAN_UP_AGENT_NAME = "User Daily Adjustment Limit Clean Up";

    /**
     * The description of the cron agent
     */
    public static final String USER_DAILY_ADJUSTMENT_LIMIT_CLEAN_UP_AGENT_DESCRIPTION =
            "Clean up User Daily Adjustment Limit entries";


    /**
     * The name of the cron agent
     */
    public static final String CLEAN_UP_AGENT_NAME = "CleanUp Process";

    /**
     * The description of the cron agent
     */
    public static final String CLEAN_UP_AGENT_DESCRIPTION =
            "Clean up old entries from all database, change states for Package/MSISDN";

    /**
     * The name of the cron agent
     */
    public static final String PACKAGE_STATE_MODIFY_AGENT_NAME =
            "Package State Modify Process";

    /**
     * The description of the cron agent
     */
    public static final String PACKAGE_STATE_MODIFY_AGENT_DESCRIPTION =
            "Set HELD Package to be Available";

    /**
     * The name of the cron agent
     */
    public static final String MSISDN_STATE_MODIFY_AGENT_NAME =
            "MSISDN State Modify Process";

    /**
     * The description of the cron agent
     */
    public static final String MSISDN_STATE_MODIFY_AGENT_DESCRIPTION =
            "Set HELD MISIDN to be Available";
    
    /**
     * The name of the cron agent
     */
    public static final String MSISDN_DELETION_AGENT_NAME =
            "MSISDN Deletion Process";

    /**
     * The description of the cron agent
     */
    public static final String MSISDN_DELETION_AGENT_DESCRIPTION =
            "Delete HELD MSISDN from System";

    /**
     * The name of the cron agent
     */
    public static final String AVAILABLE_PENDING_SUB_AGENT_NAME =
            // TODO correct the name
            "Available Pending Prepaid Subscriber Process";

    /**
     * The description of the cron agent
     */
    public static final String AVAILABLE_PENDING_SUB_AGENT_DESCRIPTION =
            // TODO correct the name
            "Set Pending Prepaid Subscriber to be Available";

    /**
     * The name of the cron agent
     */
    public static final String ACTIVE_PENDING_PREPAID_SUB_AGENT_NAME =
            "Active Pending Prepaid Subscriber Process";

    /**
     * The description of the cron agent
     */
    public static final String ACTIVE_PENDING_PREPAID_SUB_AGENT_DESCRIPTION =
            "Set Pending Prepaid Subscriber to be Active";

    /**
     * The name of the cron agent
     */
    public static final String SUSCRIBER_CLEANUP_AGENT_NAME =
            "Remove Subscriber Process";

    /**
     * The description of the cron agent
     */
    public static final String SUSCRIBER_CLEANUP_AGENT_DESCRIPTION =
            "Remove deactivate out-of-dated Subscriber from All application";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_ZERO_DEACTIVATION_AGENT_NAME =
            "Deactivate Zero Balance Dormant Subscriber Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_ZERO_DEACTIVATION_AGENT_DESCRIPTION =
            "Deactivate out-of-dated Dormant Subscriber from where remaining balance is zero.";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_FINITE_DEACTIVATION_AGENT_NAME =
            "Deactivate Finite Balance Deactivated Subscriber Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_FINITE_DEACTIVATION_AGENT_DESCRIPTION =
            "Deactivate out-of-dated Dormant Subscriber and write-off their remaining balance";

    /**
     * The name of the cron agent
     */
    public static final String ACCOUNT_CLEANUP_AGENT_NAME =
            "Remove Account Process";

    /**
     * The description of the cron agent
     */
    public static final String ACCOUNT_CLEANUP_AGENT_DESCRIPTION =
            "Remove Deactive out-of-dated Account from database";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBERCLTC_CLEANUP_AGENT_NAME =
            "Remove Subscriber CLCT Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBERCLTC_CLEANUP_AGENT_DESCRIPTION =
            "Remove Subscriber out-of-dated CLCT records from database";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_ENDDATE_CHECK_AGENT_NAME =
            "Subscriber Deactivated Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_ENDDATE_CHECK_AGENT_DESCRIPTION =
            "Set Subscriber into Deactivated State";

    /**
     * The name of the cron agent
     */
    public static final String TRANSACTION_HISTORY_CLEANUP_AGENT_NAME =
            "Remove Transaction Process";

    /**
     * The description of the cron agent
     */
    public static final String TRANSACTION_HISTORY_CLEANUP_AGENT_DESCRIPTION =
            "Remove out-of-dated Transaction history from database";

    /**
     * The name of the cron agent
     */
    public static final String TRANSFER_EXCEPTION_CLEANUP_AGENT_NAME =
            "Remove Transfer Exception Process";

    /**
     * The description of the cron agent
     */
    public static final String TRANSFER_EXCEPTION_CLEANUP_AGENT_DESCRIPTION =
            "Remove Corrected or Deleted Transfer Exception records from database";

    /**
     * The name of the cron agent
     */
    public static final String INVOICE_HISTORY_CLEANUP_AGENT_NAME =
            "Remove Invoice Process";
    
    /**
     * The name of the MT CallDetail clean-up cron agent
     */
    public static final String MT_CALLDETAIL_HISTORY_CLEANUP_AGENT_NAME =
            "Remove MT CallDetail Process";

    /**
     * The description of the MT CallDetail clean-up cron agent
     */
    public static final String MT_CALLDETAIL_HISTORY_CLEANUP_AGENT_DESCRIPTION =
            "Remove out-of-date MT CallDetail history from database";

    /**
     * The description of the cron agent
     */
    public static final String INVOICE_HISTORY_CLEANUP_AGENT_DESCRIPTION =
            "Remove out-of-dated Invoice History from database";

    /**
     * The name of the cron agent
     */
    public static final String BUCKET_COUNTER_RESETTING_MANUAL_AGENT_NAME =
            "Manually Resetting Bucket Counter";

    /**
     * The description of the cron agent
     */
    public static final String BUCKET_COUNTER_RESETTING_MANUAL_AGENT_DESCRIPTION =
            "Manually Resetting Subscriber's Bucket Counter";

    /**
     * The name of the cron agent
     */
    public static final String BUCKET_COUNTER_RESETTING_AGENT_NAME =
            "Resetting Bucket Counter Process";

    /**
     * The description of the cron agent
     */
    public static final String BUCKET_COUNTER_RESETTING_AGENT_DESCRIPTION =
            "Resetting Subscriber's Bucket Counter";

    /**
     * The name of the cron agent
     */
    public static final String GENERATE_INVOICES_NAME = "Generate Invoices";

    /**
     * The description of the cron agent
     */
    public static final String GENERATE_INVOICES_DESCRIPTION =
            "Generate Invoices for the Current Billing Cycle";

    /**
     * The name of the cron agent
     */
    public static final String GENERATE_HTML_INVOICES_NAME =
            "Generate HTML Invoices";

    /**
     * The description of the cron agent
     */
    public static final String GENERATE_HTML_INVOICES_DESCRIPTION =
            "Generates HTML invoices from existing XML invoices.";

    /**
     * The name of the cron agent
     */
    public static final String DUNNING_PROCESS_NAME = "Dunning Process";

    /**
     * The description of the cron agent
     */
    public static final String DUNNING_PROCESS_DESCRIPTION =
            "Run the Dunning Process On All Accounts";

    /**
     * The name of the cron agent
     */
    public static final String DUNNING_REPORT_GENERATION_NAME = "Dunning Report Generation";

    /**
     * The description of the cron agent
     */
    public static final String DUNNING_REPORT_GENERATION_DESCRIPTION =
            "Generate Dunning Report";

    /**
     * The name of the cron agent
     */
    public static final String DUNNING_REPORT_PROCESSING_NAME = "Dunning Report Processing";

    /**
     * The description of the cron agent
     */
    public static final String DUNNING_REPORT_PROCESSING_DESCRIPTION =
            "Process Dunning Report";

    /**
     * The name of the cron agent
     */
    public static final String DUNNING_NOTICE_NAME = "Dunning Notices Delivery";

    /**
     * The description of the cron agent
     */
    public static final String DUNNING_NOTICE_DESCRIPTION =
            "Send Dunning Notices";

    /**
     * The name of the cron agent
     */
    public static final String DISCOUNT_ASSIGNMENT_TASK_NAME = "Discount Class Assignment"; 

    /**
     * The name of the cron agent
     */
    public static final String DISCOUNT_ASSIGNMENT_TASK_DESCRIPTION = "Assign Discount Class"; 
	
    
    /**
	 * The name of the cron agent
	 */
	public static final String DUNNING_POLICY_ASSIGNMENT_NAME = "Dunning Policy Assignment"; 

	/**
	 * The name of the cron agent
	 */
	public static final String DUNNING_POLICY_ASSIGNMENT_DESCRIPTION = "Assign Dunning Policy"; 

    /**
     * The name of the cron agent
     */
    public static final String PROVISION_AUXILIARYSERVICE_PROCESS_NAME = "Auxiliary Service Provisioning Process";

    /**
     * The description of the cron agent
     */
    public static final String PROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION =
            "Provision future Auxiliary Services";
    
    /**
     * The name of the cron agent
     */
    public static final String RECURRING_TOP_UP_PROCESS_NAME = "Recurring Credit Card Top Up Process";

    /**
     * The description of the cron agent
     */
    public static final String RECURRING_TOP_UP_PROCESS_DESCRIPTION =
            "Recurring Credit Card Top Up Process";    

    /**
     * The name of the cron agent
     */
    public static final String UNPROVISION_AUXILIARYSERVICE_PROCESS_NAME = "AuxiliaryService Unprovisioning Process";

    /**
     * The description of the cron agent
     */
    public static final String UNPROVISION_AUXILIARYSERVICE_PROCESS_DESCRIPTION =
            "Unprovision expired Auxiliary Services";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_STATE_UPDATE_AGENT_NAME =
            "Subscriber State Update Process";
    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_STATE_UPDATE_AGENT_DESCRIPTION =
            "Update Subscriber States Based on their State Timers";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_NAME =
            "Subscriber Future activation/deactivation Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_FUTURE_ACTIVE_DEACTIVE_AGENT_DESCRIPTION =
            "Subscriber Future activation/deactivation Process";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_PRE_EXPIRY_AGENT_NAME =
            "Subscriber Pre Expiry Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_PRE_EXPIRY_AGENT_DESCRIPTION =
            "Subscriber Pre Expiry Process";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_NAME =
            "Subscriber Expired to Dormant Process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_EXPIRED_TO_DORMANT_AGENT_DESCRIPTION =
            "Subscriber Expired to Dormant Process";

    /**
     * The name of the cron agent
     */
    public static final String DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_NAME =
            "Deactivate Subscriber in Expired or Available or Barred / Locked state Process";

    /**
     * The description of the cron agent
     */
    public static final String DEACTIVATE_SUBSCRIBER_IN_EXPIRED_OR_AVAILABLE_OR_BARRED_AGENT_DESCRIPTION =
    		"Deactivate Subscriber in Expired or Available or Barred / Locked state Process";

    /**
     * The name of the cron agent
     */
    public static final String POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_NAME = "Postpaid Recurring Credit Card Top Up Process";
    
    
    /**
     * The description of the cron agent
     */
    public static final String POSTPAID_RECURRING_CREDIT_CARD_TOP_UP_AGENT_DESCRIPTION = "Postpaid Recurring Credit Card Top Up Process";
    
    
    /**
     * The name of the cron agent
     */
    public static final String DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_NAME = "Direct Debit Outbound File Processor";
    
    
    /**
     * The description of the cron agent
     */
    public static final String DIRECT_DEBIT_OUTBOUND_FILE_PROCESSOR_AGENT_DESCRIPTION = "Process Direct Debit Out Bound file";
    
    
    /**
     * The name of the cron agent
     */
    public static final String DEACTIVATE_IN_COLLECTION_ACCOUNT_NAME = "Deactivate IN_COLLECTION Accounts Process";

    /**
     * The description of the cron agent
     */
    public static final String DEACTIVATE_IN_COLLECTION_ACCOUNT_DESCRIPTION =
            "Deactivates all IN_COLLECTION accounts after a configurable period of time";

    /**
     * The name of the cron agent
     */
    public static final String SUBSCRIBER_SCHEDULED_ACTIVATION_DEACTIVATION_NAME = "Deactivate Subscriber process";

    /**
     * The description of the cron agent
     */
    public static final String SUBSCRIBER_SCHEDULED_ACTIVATION_DEACTIVATION_DESCRIPTION =
            "Deactive subscribers based on scheduled Start and End date.";
    
    /**
     * The name of the cron agent
     */
    public static final String CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_NAME =
            "Remove references to closed Auxiliary Services";

    /**
     * The description of the cron agent
     */
    public static final String CLOSED_AUXILIARY_SERVICE_CLEANUP_AGENT_DESCRIPTION =
            "Remove references to closed Auxiliary Services (e.g. Subscriber associtions/SCT associations).";

    /**
     * The name of the cron agent
     */
    public static final String SECONDARY_PRICE_PLAN_ACTIVATION_NAME = "Secondary price plan activation process";

    /**
     * The description of the cron agent
     */
    public static final String SECONDARY_PRICE_PLAN_ACTIVATION_DESCRIPTION =
            "Checks if the secondary price plan should be activated or not";

    /**
     * Cron agent for Monitoring the Mobile Number Groups
     */
    public static final String MOBILE_NUMBER_GROUP_MONITOR_NAME = "Mobile Number Group Monitor Agent";

    /**
     * The name of the cron agent
     */
    public static final String PRICE_PLAN_SERVICE_MONITORING_NAME = "Price plan services monitoring process";

    /**
     * The description of the cron agent
     */
    public static final String PRICE_PLAN_SERVICE_MONITORING_DESCRIPTION =
            "Checks to see if the services should be provisioned or unprovisioned based on the start and end date";

    /**
     * The name of the cron agent
     */
    public static final String LNP_BULK_LOAD_NAME = "LNP Bulk Load Process";

    /**
     * The description of the cron agent
     */
    public static final String LNP_BULK_LOAD_DESCRIPTION = "Generates LNP Bulk Load Files";


    /**
     * The name of the cron agent
     */
    public static final String USER_DUMP_NAME = "UserDump";

    /**
     * The description of the cron agent
     */
    public static final String USER_DUMP_DESCRIPTION = "User Dump for Datamart";


    public static final String RERATED_CALL_DETAIL_ALARM_NAME = "ReratedCallDetailAlarm";
    public static final String RERATED_CALL_DETAIL_ALARM_DESCRIPTION = "Alarm task for unreceived rerated call details";

    public static final String RERATED_CALL_DETAIL_CLEAN_UP_NAME = "ReratedCallDetailCleanUp";
    public static final String RERATED_CALL_DETAIL_CLEAN_UP_DESCRIPTION = "Clean up task for rerated call details";

    /**
     * The name of the cron agent
     */
    public static final String POS_EXTRACT_NAME = "Account POS Extractor";

    /**
     * The name of the cron agent
     */
    public static final String POS_SUBSCRIBER_EXTRACT_NAME = "MSISDN POS extractor";

    /**
     * The description of the cron agent
     */
    public static final String POS_EXTRACT_DESCRIPTION = "Point of Sale Extractor";

    /**
     * The description of the cron agent
     */
    public static final String POS_EXTRACT_MSISDN_DESCRIPTION = "Point of Sale IVR Extractor";

    /**
     * The name of the cron agent
     */
    public static final String BUNDLE_AUXILIARYSERVICE_PROCESS_NAME =
            "Bundle Auxiliary Service Provision and Unprovision Process";

    /**
     * The description of the cron agent
     */
    public static final String BUNDLE_AUXILIARYSERVICE_PROCESS_DESCRIPTION =
            "Checks if Bundle Auxiliary Services need Provision or Unprovision";

    public static final String PRICE_PLAN_VERSION_UPDATE_NAME = "Priceplan version update agent";

    public static final String PRICE_PLAN_VERSION_UPDATE_DESCRIPTION =
            "Priceplan version update agent will apply new price plan versions to subscribers";

    /** The name of the auto deposit release cron agent */
    public static final String AUTO_DEPOSIT_RELEASE_NAME = "Auto Deposit Release";

    /** The description of the auto deposit release cron agent */
    public static final String AUTO_DEPOSIT_RELEASE_DESCRIPTION = "Automatically release subscribers' deposits";

	/** The name of the late fee cron agent */
	public static final String LATE_FEE_NAME = "Late Fee";

    /** The description of the late fee cron agent */
	public static final String LATE_FEE_DESCRIPTION =
	    "Generates late fee transactions";
	

    /** The name of the contract end update cron agent */
    public static final String CONTRACT_END_UPDATE_NAME = "Subscription Contract End Update";
    
    /** The name of the contract cleanup cron agent */
    public static final String CONTRACT_HISTORY_CLEANUP_NAME = "Subscription Contract History Cleanup";
    
    /** The name of the scheduled priceplan executor cron agent */
    public static final String SCHEDULED_SUBSCRIBER_MODIFICATION_EXECUTOR_NAME = "Apply Future Dated Priceplan Change to Subscribers";
    
    /** The name of the scheduled priceplan executor cron agent */
    public static final String IVP_PRICEPLAN_SERVICE = "InterfaceSelfcareapi.PricePlanService.2";
    
    public static final int PRICEPLAN_OPTIONTYPE_PACKAGE = 1;
    
    public static final int PRICEPLAN_OPTIONTYPE_SERVICE = 0;
    
    public static final int PRICEPLAN_OPTIONTYPE_AUX_SERVICE = 3;
    
    public static final int PRICEPLAN_OPTIONTYPE_BUNDLE = 2;
    
    public static final int PRICEPLAN_OPTIONTYPE_AUX_BUNDLE = 4;
    
    public static final int INFORMATION_QUERY = 0;
    
    public static final int UPDATE_REMOVE_QUERY = 1;
    
    public static final long SCHEDULED_PRICEPLAN_CHANGE_EXECUTOR = 0;
    public static final String NEW_PRICEPLAN_ID = "newPricePlanId";
    public static final String DEVICE_MODEL_ID = "deviceModelId";
    public static final String CHANNEL_TYPE = "channelType";
    public static final String CREDIT_CATEGORY_TYPE = "creditCategoryType";
    public static final String PROVINCE = "PROVINCE";
    public static final String SERVICES = "services";
    public static final String BUNDLES = "bundles";
    public static final String REMOVE_SERVICES = "removeServices";
    public static final String REMOVE_BUNDLES = "removeBundles";
    public static final String AGENT_ID = "agentId";
    public static final String DEALER_CODE = "dealerCode";
    public static final String OLD_ATU_AMOUNT = "oldAtu";
    public static final String OLD_PRICEPLAN_ID = "oldPricePlanId";
    public static final String OLD_PRICEPLAN_ID_VERSION = "oldPricePlanIdVersion";
    public static final int SCHEDULED_PENDING = 0;
    
    public static final int SCHEDULED_PP_CHANGE_SUCCESS = 1;
    
    public static final int SCHEDULED_PP_CHANGE_COMPLETE_WITH_ERRORS = 2;
    
    public static final int SCHEDULED_PP_CHANGE_FAILED = 3;
    
    public static final int SCHEDULED_PP_CHANGE_CANCELLED = 4;
    
    public static final int SCHEDULED_PP_VALIDATE_OPTIONS_SUCCESS = 0;
    
    public static final int SCHEDULED_PP_VALIDATE_OPTIONS_BALANCE_CHECK_SUCCESS = 0;
    
    public static final int SCHEDULED_PP_VALIDATE_OPTIONS_BALANCE_CHECK_SKIPPED = -1;
    
    public static final int SCHEDULED_PP_VALIDATE_OPTIONS_PROVISION_SUCCESS = 0;
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG = "System Error";
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_PP_MISMATCH_ERR_MSG = "Priceplan Mismatch";
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_SUB_NOT_ACTIVE_ERR_MSG = "Subscriber Not Active";
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_ACTIVE_ERR_MSG = "New Priceplan Not Active";
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_ALLOW_ERR_MSG = "Options Not Valid";
    
    public static final String SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_VALID_ERR_MSG = "New Priceplan Not Valid";
    /** The name of the contract end update description cron agent */
    public static final String CONTRACT_END_UPDATE_DESCRIPTION = "Priceplan switch will occur to those subscriptions who contract have been completed. Use Parameter 1=> endDate and Parameter 2 (Optional) => BAN";
    

    public static final String BILL_CYCLE_CHANGE_AGENT_NAME = "Bill Cycle Day Change Processor";
    
    public static final String BILL_CYCLE_CHANGE_AGENT_DESCRIPTION = "Executes pending bill cycle date changes for bill cycles ending tomorrow.";
    
    
    //Unapplied Transaction Processing.
    public static final String UNAPPLIED_TRANSACTION_CRON_NAME = "Unapplied Transaction Processing";

    public static final String UNAPPLIED_TRANSACTION_CRON_DESCRIPTION = "Process Unapplied Transactions";
    
    public static final String SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_NAME = "Fixed Stop Price Plan Subscriber Processor";
    
    public static final String SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_DESCRIPTION = "Fixed Stop Price Plan Extension - Move subscribers to Barred or Closed state";
    
    public static final String SUBSCRIBER_FIXED_STOP_PRICE_PLAN_AGENT_HELP = "Fixed Stop Price Plan Extension - Move active subscribers to Barred state if fixed stop end date past today. Also deactivate subscribered in Barred state after configured number of days in SPID. Only subscribers with Fixed Stop Price Plan feature are processed.";
    
    public static final String FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_NAME = "Retry Failed Subscriber Services Task";
    
    public static final String FAILED_SUBSCRIBER_SERVICES_RETRY_AGENT_DESCRIPTION = "Retry to provision/unprovision/suspend Subscriber services which failed in past";
    
    /**
     * The name of the Over Payment Processing Cron Agent
     */
    public static final String OVER_PAYMENT_PROCESSING_AGENT_NAME = "Over Payment Processing";
    
    
    /**
     * The description of the Over Payment Processing Cron Agent
     */
    public static final String OVER_PAYMENT_PROCESSING_AGENT_DESCRIPTION = "Over Payment Processing";
    
    /** The name of the on time payment cron agent */
    public static final String ON_TIME_PAYMENT_NAME = "On-Time Payment Promotion";

    /** The description of the on time payment cron agent */
    public static final String ON_TIME_PAYMENT_DESCRIPTION = "Determines whether invoices are paid on-time for promotion purposes";
    
    /** The name of the BalanceBundleUsageSummary cleanup cron agent */
    public static final String BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_NAME = "Balance Bundle Usage Summary CleanUp";
    
    /** The description of the BalanceBundleUsageSummary clean up cron agent. NoOfDays is parameter wherein entries older than those NoOfDays from today would be cleaned up. */
    public static final String BALANCE_BUNDLE_USAGE_SUMMARY_CLEANUP_AGENT_DESCRIPTION = "To clean BalanecBundleUsageSummary table records. Parameters need to configure : SPID (Parameter 1) and Number of Days (Parameter 2).";
        
    /** The name of the BalanceBundleUsageSummary cron agent, which is used to populate data in BalanceBundleUsageSummary for calls with aggregation */
    public static final String BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_NAME = "Balance Bundle Usage Summary ";
    
    /** The description of the BalanceBundleUsageSummary cron agent */
    public static final String BALANCE_BUNDLE_USAGE_SUMMARY_AGENT_DESCRIPTION = "Populate BalanceBundleUsageSummary records in database.";
	
	/** The name of the Subscription Segment Update cron agent, which is used to update Subscriptions segment based on Billing/usage */
	public static final String SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_NAME = "Subscription Segment Update";
    
	/** The description of the Subscription Segment Update cron agent*/
	public static final String SUBSCRIPTION_SEGMENT_UPDATE_LIFECYCLE_AGENT_DESCRIPTION = "Update Subscription Segment for Account";
            
    public static final String RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_NAME = "Recharge subscriber services upon pool account payments Task";

    public static final String FINAL_DISCOUNT_EVENT_AGENT_NAME = "Generate Discount Event Transaction";
    
    public static final String FINAL_DISCOUNT_EVENT_AGENT_DESCRIPTION = "Generate Discount Event Transaction";
    
    public static final String RECHARGE_SUBSCRIBER_SERVICES_FOR_PAYMENTS_AGENT_DESCRIPTION = "Recharge subscriber services upon pool account payments";
    public static final String GENERATE_AUTOMATIC_REFUNDS = "Generate Automatic Refunds";
    public static final String GENERATE_AUTOMATIC_REFUNDS_DESCRIPTION = "Generate Automatic Refund";
    public static final String CREDIT_CATEGORY_UPDATE_ASSIGNMENT_NAME = "Credit Category Update";
    public static final String CREDIT_CATEGORY_UPDATE_ASSIGNMENT_DESCRIPTION = "Credit Category Update ";
    
    //BSS-5196 Auto deposit release
    public static final String DEPOSIT_RELEASE_PROCESSING_AGENT_NAME = "Deposit release";
	public static final String DEPOSIT_RELEASE_PROCESSING_AGENT_DESCRIPTION = "Deposit release";
    public static final String EVERY_MINUTE_CRON_ENTRY = "everyMinute";
    public static final String EVERY_MONTH_15_CRON_ENTRY = "everyMonth_15th";
    public static final String CRM_DAILY_5AM_CRON_ENTRY = "CRM_Daily_5AM";
}
