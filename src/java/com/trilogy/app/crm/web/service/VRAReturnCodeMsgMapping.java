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

package com.trilogy.app.crm.web.service;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.vra.interfaces.VRAResultCodes;

/**
 * HashMap to store all the message returned by VRA application during
 * Voucher Recharging Process.
 *
 * @author lily.zou@redknee.com
 */
public final class VRAReturnCodeMsgMapping
{
    private static final String NON_EXISTING_ERROR_MSG = "Non-Existing Error Code";
    
    // 0, 1, 2, 3, 4, 5, 6, 7
    private static final String SUCCESS_MSG = "Completed successfully";
    private static final String ERROR_MANDATORY_FIELDS_MISSING_MSG = "Mandatory Field(s) Missing";
    private static final String ERROR_INVALID_METHOD_CALL_AUTH_MSG = "Invalid method call authorization";
    private static final String ERROR_MSISDN_LOCKED_BYANOTHER_TXN_MSG = "MSISDN locked by another transaction";
    private static final String ERROR_MSISDN_LOCKED_OUT_FRAUD_MSG = "MSISDN is locked out by fraud management";
    private static final String ERROR_MSISDN_NOT_ASSOC_WITH_CARRIER_MSG = "MSISDN is not associated with any carrier";
    private static final String ERROR_SELFCARE_NOT_AVAILABLE_MSG = "Self-Care Profile not found";
    private static final String ERROR_VOUCHER_MSISDN_COMBO_LOCKED_OUT_MSG = "Voucher-MSISDN combination locked out";

    // 10, 11, 12, 13, 14, 15, 16, 19
    private static final String ERROR_INVALID_MSISDN_UPON_MSISDN_CHECK_MSG = "Invalid MSISDN upon MSISDN check";
    private static final String ERROR_SUBSCRIBER_BLOCKED_UPON_MSISDN_CHECK_MSG = "Subscriber blocked upon MSISDN check";
    private static final String ERROR_SUBSCRIBER_EXPIRED_UPON_MSISDN_CHECK_MSG = "Subscriber expired upon MSISDN check";
    private static final String ERROR_SUBSCRIBER_TO_BE_ACTIVATED_UPON_MSISDN_CHECK_MSG =
            "Subscriber to be activated upon MSISDN check";
    private static final String ERROR_MAX_BALANCE_VALIDATE_SUBSCRIBER_CALL_MSG =
            "Max balance reached when validating subscriber";
    private static final String ERROR_RECHARGE_SYSTEM_OVERLOADED_UPON_MSISDN_CHECK_MSG =
            "Recharge system overloaded upon MSISDN check";
    private static final String ERROR_RECHARGE_SYSTEM_TIMEOUT_UPON_MSISDN_CHECK_MSG =
            "Recharge system timeout upon MSISDN check";
    private static final String ERROR_GENERIC_FAILURE_UPON_MSISDN_CHECK_MSG = "Generic failure upon MSISDN check";

    // 20, 21, 22, 23, 24, 25, 26, 27, 28, 29
    private static final String ERROR_INVALID_VOUCHER_NUM_FORMAT_MSG = "Invalid voucher number format";
    private static final String ERROR_NON_EXISTING_VOUCHER_NUM_MSG = "Voucher number does not exist on the system";
    private static final String ERROR_VOURCHER_MARKED_USED_MSG = "Voucher number already marked used";
    private static final String ERROR_VOURCHER_MARKED_STOLEN_MSG = "Voucher number marked stolen";
    private static final String ERROR_VOUCHER_SHELFLIFE_EXPIRED_MSG = "Voucher shelf-life expired";
    private static final String ERROR_VOUCHER_LOCKEDOUT_BY_FRAUD_MSG = "Voucher locked out by fraud";
    private static final String ERROR_VOUCHER_SYSTEM_SESSION_HAS_EXPIRED_MSG = "Voucher system sessioin has expired";
    private static final String ERROR_VOUCHER_SYSTEM_INTERNAL_TIMEOUT_MSG = "Voucher system internal timeout";
    private static final String ERROR_VOUCHER_NOT_LOCKED_UPON_BURNING_ATTEMPT_MSG =
            "Voucher not locked apon burning attempt";
    private static final String ERROR_GENERIC_FAILURE_AT_VOUCHER_SYSTEM_MSG = "Generic failure at Voucher system";

    // 30, 31, 33
    private static final String ERROR_INCORRECT_SP_FOR_VOUCHER_SYSTEM_MSG = "Incorrect SP for Voucher system";
    private static final String ERROR_VOUCHER_MAX_VALUE_EXCEEDED_MSG = "Voucher max value exceded";
    private static final String ERROR_GLOBAL_VOUCHER_LOCKOUT_FAIL_MSG = "Global Voucher lockout failed";

    // 40, 41, 42, 43, 44, 45, 46, 49
    private static final String ERROR_INVALID_MSISDN_MSG = "Invalid MSISDN upon credit attempt";
    private static final String ERROR_MAX_BALANCE_MSG = "Maximum Balance error upon credit attempt";
    private static final String ERROR_SUBSCRIBER_EXPIRED_MSG = "Subscriber account expired upon credit attempt";
    private static final String ERROR_MAX_NUMBER_EXCEEDED_MSG =
            "Maximum number of recharge operations exceeded upon credit attempt";
    private static final String ERROR_INCORRECT_CURRENCY_MSG = "Incorrect credit currency upon credit attempt";
    private static final String ERROR_RECHARGE_SYSTEM_OVERLOADED_MSG = "Recharge system overloaded upon credit attempt";
    private static final String ERROR_RECHARGE_SYSTEM_INTERNAL_TIMEOUT_MSG =
            "Recharge system's internal timeout upon credit attempt";
    private static final String ERROR_GENERIC_FAILURE_MSG = "Generic failure upon credit ";

    // 51, 52
    private static final String ERROR_CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE_MSG =
            "Currency conversion system unavailable";
    private static final String ERROR_CURRENCY_CONVERSION_FAILED_MSG = "Currency conversion failed";

    // 60, 61, 62, 63
    private static final String ERROR_INVALID_VMS_SYSTEM_MSG = "Invalid VMS System";
    private static final String ERROR_VOUCHER_SYSTEM_UNAVAILABLE_MSG = "Voucher System unavailable";
    private static final String ERROR_VOUCHER_SYSTEM_MAX_RETRY_REACH_MSG = "Voucher System max retry reached";
    private static final String ERROR_VOUCHER_SYSTEM_TIMEOUT_MSG = "Voucher System timeout";

    // 70, 71, 72, 73, 74
    private static final String ERROR_CHARGING_SYSTEM_UNDEFINED_MSG = "Charging System Undefined";
    private static final String ERROR_CHARGING_SYSTEM_UNAVAILABLE_MSG = "Charging System unavailable";
    private static final String ERROR_CHARGING_SYSTEM_MAX_RETRY_MSG = "Charging System max retry reached";
    private static final String ERROR_CHARGING_SYSTEM_TIMEOUT_MSG = "Charging System timeout";
    private static final String ERROR_CHARGING_SYSTEM_IN_PROGRESS_MSG = "Charging System in progress";

    // 80, 81
    private static final String ERROR_NON_STANDARD_VOUCHER_SYSTEM_ERR_RCV_MSG =
            "Non-Standard Voucher System error received";
    private static final String ERROR_NON_STANDARD_CHARGING_SYSTEM_ERR_RCV_MSG =
            "Non-Standard Charging System error received";

    // 99
    private static final String ERROR_INTERNAL_SYSTEM_ERROR_MSG = "Internal System Error";

    // 120, 121, 122, 123, 124, 125, 126, 127, 128, 129
    private static final String INVAILD_VOUCHER_NUMBER_FORMAT_MSG = "Invalid Voucher Number Format";
    private static final String VOUCHER_NUMBER_DOES_NOT_EXIST_MSG = "Voucher Number Does Not Exist";
    private static final String VOUCHER_NUMBER_ALREADY_MARKED_MSG = "Voucher Number Already Marked";
    private static final String EXTENDED_VOUCHER_NUMBER_MARKED_STOLEN_MSG = "Extended Voucher Number Marked Stolen";
    private static final String EXTENDED_VOUCHER_SHELF_LIFE_EXPIRED_MSG = "Extended Voucher Shelf Life Expired";
    private static final String EXTENDED_VOUCHER_LOCKED_OUT_BY_FRAUD_MSG = "Extended Voucher Locked Out By Fraud";
    private static final String VOUCHER_SYSTEM_SESSION_EXPIRED_MSG = "Voucher System Session Expired";
    private static final String EXTENDED_VOUCHER_SYSTEM_INTERNAL_TIMEOUT_MSG =
            "Extended Voucher System Internal Timeout";
    private static final String EXTENDED_VOUCHER_NOT_LOCKED_UPON_BURN_ATTEMPT_MSG =
            "Extended Voucher Not Locked Upon Burn Attempt";
    private static final String GENERIC_FAILURE_VOUCHER_SYSTEM_MSG = "Generic Failure Voucher System";

    // 130
    private static final String INCORRECT_SERVICE_PROVIDER_FOR_VOUCHER_SYSTEM_MSG =
            "Incorrect Service Provider for Voucher System";

    // 140, 141, 142, 143, 144, 145, 146, 149
    private static final String INVALID_MSISDN_UPON_CREDIT_ATTEMPT_MSG = "Invalid MSISDN Upon Credit Attempt";
    private static final String MAX_BALANCE_ERROR_UPON_CREDIT_ATTEMPT_MSG = "Max Balance Error Upon Credit Attempt";
    private static final String SUBSCRIBER_ACCOUNT_EXPIRED_UPON_CREDIT_ATTEMPT_MSG =
            "Subscriber Account Expired Upon Credit Attempt";
    private static final String MAX_RECHARGE_EXCEEDED_UPON_CREDIT_ATTEMPT_MSG =
            "Max Recharge Exceeded Upon Credit Attempt";
    private static final String INCORRECT_CREDIT_CUR_UPON_CREDIT_ATTEMPT_MSG =
            "Incorrect Credit Currency Upon Credit Attempt";
    private static final String RECHARGE_SYSTEM_OVERLOADED_UPON_CREDIT_ATTEMPT_MSG =
            "Recharge System Overloaded Upon Credit Attempt";
    private static final String RECHARGE_SYSTEM_INTERNAL_TIMEOUT_UPON_CREDIT_ATTEMPT_MSG =
            "Recharge System Interval Timeout Upon Credit Attempt";
    private static final String GENERIC_FAILURE_UPON_CREDIT_ATTEMPT_MSG = "Generic Failure Upon Credit Attempt";

    // 151, 152
    private static final String EXTENDED_CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE_MSG =
            "Extended Currency Conversion System Unavailable";
    private static final String EXTENDED_CURERNCY_CONVERSION_FAILED_MSG = "Extended Currency Conversion Failed";

    // 160, 161, 162, 163
    private static final String VOUCHER_SYSTEM_UNDEFINED_MSG = "Voucher System Undefined";
    private static final String EXTENDED_VOUCHER_SYSTEM_UNAVAILABLE_MSG = "Extended Voucher System Unavailable";
    private static final String EXTENDED_VOUCHER_SYSTEM_MAX_RETRY_REACHED_MSG =
            "Extended Voucher System Max Retry Reached";
    private static final String EXTENDED_VOUCHER_SYSTEM_TIMEOUT_MSG = "Extended Voucher System Timeout";

    // 170, 171, 172, 173
    private static final String EXTENDED_CHARGING_SYSTEM_UNDEFINED_MSG = "Extended Charging System Undefined";
    private static final String EXTENDED_CHARGING_SYSTEM_UNAVAILABLE_MSG = "Extended Charging System Unavailable";
    private static final String EXTENDED_CHARGING_SYSTEM_MAX_RETRY_REACHED_MSG =
            "Extended Charging System Max Retry Reached";
    private static final String EXTENDED_CHARGING_SYSTEM_TIMEOUT_MSG = "Extended Charging System Timeout";

    // 180, 181, 182, 183, 184, 185, 186
    private static final String NON_STANDARD_VOUCHER_SYSTEM_ERROR_MSG = "Non Standard Voucher System Error";
    private static final String NON_STANDARD_CHARGING_SYSTEM_ERROR_MSG = "Non Standard Charging System Error";
    private static final String INVALID_VOUCHER_TYPE_MSG = "Invalid Voucher Type (Voucher exists but no mapping found)";
    private static final String PRICE_PLAN_NOT_FOUND_MSG = "Price plan not found upon price-plan query to CRM/URCS";
    private static final String VOUCHER_TYPE_NOT_FOUND_MSG = "Voucher type not found";
    private static final String URCS_UNAVAILABLE_MSG = "URCS unavailable";
    private static final String URCS_TIMEOUT_MSG = "URCS Timed Out";
    private static final String CRM_NOT_AVAILABLE_MSG = "CRM not available";

    // 199
    private static final String EXTENDED_INTERNAL_SYSTEM_ERROR_MSG = "Extended Internal System Error";

    // 229
    private static final String GENERIC_FAILURE_VOUCHER_VALIDATION_AT_VOUCHER_SYSTEM_MSG =
            "Generic Failure Voucher Validation at Voucher System";

    private static final String ERROR_ETU_PROFILE_NOT_FOUND_MSG = "Emergency Top-up Profile not found";
    
    private static final Map<Integer,String> errorMsgMap_ = new HashMap<Integer,String>();
    
    static
    {
        // 0, 1, 2, 3, 4, 5, 6, 7, 8
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SUCCESS), SUCCESS_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MANDATORY_FIELDS_MISSING), ERROR_MANDATORY_FIELDS_MISSING_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_METHOD_CALL_AUTH), ERROR_INVALID_METHOD_CALL_AUTH_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MSISDN_LOCKED_BYANOTHER_TXN), ERROR_MSISDN_LOCKED_BYANOTHER_TXN_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MSISDN_LOCKED_OUT_FRAUD), ERROR_MSISDN_LOCKED_OUT_FRAUD_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MSISDN_NOT_ASSOC_WITH_CARRIER), ERROR_MSISDN_NOT_ASSOC_WITH_CARRIER_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SELFCARE_NOT_AVAILABLE), ERROR_SELFCARE_NOT_AVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_MSISDN_COMBO_LOCKED_OUT), ERROR_VOUCHER_MSISDN_COMBO_LOCKED_OUT_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.ETU_PROFILE_NOT_FOUND), ERROR_ETU_PROFILE_NOT_FOUND_MSG);

        // 10, 11, 12, 13, 14, 15, 16, 19
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_MSISDN_UPON_MSISDN_CHECK),
                ERROR_INVALID_MSISDN_UPON_MSISDN_CHECK_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SUBSCRIBER_BLOCKED_UPON_MSISDN_CHECK),
                ERROR_SUBSCRIBER_BLOCKED_UPON_MSISDN_CHECK_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SUBSCRIBER_EXPIRED_UPON_MSISDN_CHECK),
                ERROR_SUBSCRIBER_EXPIRED_UPON_MSISDN_CHECK_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MAX_BALANCE_VALIDATE_SUBSCRIBER_CALL),
                ERROR_MAX_BALANCE_VALIDATE_SUBSCRIBER_CALL_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_OVERLOADED_UPON_MSISDN_CHECK),
                ERROR_RECHARGE_SYSTEM_OVERLOADED_UPON_MSISDN_CHECK_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_TIMEOUT_UPON_MSISDN_CHECK),
                ERROR_RECHARGE_SYSTEM_TIMEOUT_UPON_MSISDN_CHECK_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE_UPON_MSISDN_CHECK),
                ERROR_GENERIC_FAILURE_UPON_MSISDN_CHECK_MSG);

        // 20, 21, 22, 23, 24, 25, 26, 27, 28, 29
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_VOUCHER_FORMAT), ERROR_INVALID_VOUCHER_NUM_FORMAT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_NUMBER_DNE_ON_SYSTEM), ERROR_NON_EXISTING_VOUCHER_NUM_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUNCHER_NUMBER_ALREADY_MARKED_USED), ERROR_VOURCHER_MARKED_USED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_NUMBER_MARKED_STOLEN), ERROR_VOURCHER_MARKED_STOLEN_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SHELF_LIFE_EXPIRED), ERROR_VOUCHER_SHELFLIFE_EXPIRED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_LOCKED_OUT_BY_FRAUD), ERROR_VOUCHER_LOCKEDOUT_BY_FRAUD_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_SESSION_HAS_EXPIRED),
                ERROR_VOUCHER_SYSTEM_SESSION_HAS_EXPIRED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_INTERNAL_TIMEOUT), ERROR_VOUCHER_SYSTEM_INTERNAL_TIMEOUT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_NOT_LOCKED_UPON_BURNING_ATTEMPT),
                ERROR_VOUCHER_NOT_LOCKED_UPON_BURNING_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE_AT_VOUCHER_SYSTEM),
                ERROR_GENERIC_FAILURE_AT_VOUCHER_SYSTEM_MSG);

        // 30, 31, 33
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INCORRECT_SP_FOR_VOUCHER_SYSTEM), ERROR_INCORRECT_SP_FOR_VOUCHER_SYSTEM_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_MAX_VALUE_EXCEEDED), ERROR_VOUCHER_MAX_VALUE_EXCEEDED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GLOBAL_VOUCHER_LOCKOUT_FAIL), ERROR_GLOBAL_VOUCHER_LOCKOUT_FAIL_MSG);

        // 40, 41, 42, 43, 44, 45, 46, 49
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_MSISDN), ERROR_INVALID_MSISDN_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MAX_BALANCE), ERROR_MAX_BALANCE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SUBSCRIBER_EXPIRED), ERROR_SUBSCRIBER_EXPIRED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MAX_NUMBER_EXCEEDED), ERROR_MAX_NUMBER_EXCEEDED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INCORRECT_CURRENCY), ERROR_INCORRECT_CURRENCY_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_OVERLOADED), ERROR_RECHARGE_SYSTEM_OVERLOADED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_INTERNAL_TIMEOUT),
                ERROR_RECHARGE_SYSTEM_INTERNAL_TIMEOUT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE), ERROR_GENERIC_FAILURE_MSG);

        // 51, 52
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE),
                ERROR_CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CURRENCY_CONVERSION_FAILED), ERROR_CURRENCY_CONVERSION_FAILED_MSG);

        // 60, 61, 62, 63
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_VMS_SYSTEM), ERROR_INVALID_VMS_SYSTEM_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_UNAVAILABLE), ERROR_VOUCHER_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_MAX_RETRY_REACH), ERROR_VOUCHER_SYSTEM_MAX_RETRY_REACH_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_TIMEOUT), ERROR_VOUCHER_SYSTEM_TIMEOUT_MSG);

        // 70, 71, 72, 73, 74
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CHARGING_SYSTEM_UNDEFINED), ERROR_CHARGING_SYSTEM_UNDEFINED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CHARGING_SYSTEM_UNAVAILABLE), ERROR_CHARGING_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CHARGING_SYSTEM_MAX_RETRY), ERROR_CHARGING_SYSTEM_MAX_RETRY_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CHARGING_SYSTEM_TIMEOUT), ERROR_CHARGING_SYSTEM_TIMEOUT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.CHARGING_SYSTEM_IN_PROGRESS), ERROR_CHARGING_SYSTEM_IN_PROGRESS_MSG);

        // 80, 81
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.NON_STANDARD_VOUNCHER_SYSTEM_ERROR_RECEIVED),
                ERROR_NON_STANDARD_VOUCHER_SYSTEM_ERR_RCV_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.NON_STANDARD_CHARGING_SYSTEM_ERROR_RECEIVED),
                ERROR_NON_STANDARD_CHARGING_SYSTEM_ERR_RCV_MSG);

        // 99
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INTERNAL_SYSTEM_ERROR), ERROR_INTERNAL_SYSTEM_ERROR_MSG);

        // 120, 121, 122, 123, 124, 125, 126, 127, 128, 129
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVAILD_VOUCHER_NUMBER_FORMAT), INVAILD_VOUCHER_NUMBER_FORMAT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_NUMBER_DOES_NOT_EXIST), VOUCHER_NUMBER_DOES_NOT_EXIST_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_NUMBER_ALREADY_MARKED), VOUCHER_NUMBER_ALREADY_MARKED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_NUMBER_MARKED_STOLEN),
                EXTENDED_VOUCHER_NUMBER_MARKED_STOLEN_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_SHELF_LIFE_EXPIRED),
                EXTENDED_VOUCHER_SHELF_LIFE_EXPIRED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_LOCKED_OUT_BY_FRAUD),
                EXTENDED_VOUCHER_LOCKED_OUT_BY_FRAUD_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_SESSION_EXPIRED), VOUCHER_SYSTEM_SESSION_EXPIRED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_SYSTEM_INTERNAL_TIMEOUT),
                EXTENDED_VOUCHER_SYSTEM_INTERNAL_TIMEOUT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_NOT_LOCKED_UPON_BURN_ATTEMPT),
                EXTENDED_VOUCHER_NOT_LOCKED_UPON_BURN_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE_VOUCHER_SYSTEM), GENERIC_FAILURE_VOUCHER_SYSTEM_MSG);

        // 130
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INCORRECT_SERVICE_PROVIDER_FOR_VOUCHER_SYSTEM),
                INCORRECT_SERVICE_PROVIDER_FOR_VOUCHER_SYSTEM_MSG);

        // 140, 141, 142, 143, 144, 145, 146, 149
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INVALID_MSISDN_UPON_CREDIT_ATTEMPT), INVALID_MSISDN_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MAX_BALANCE_ERROR_UPON_CREDIT_ATTEMPT),
                MAX_BALANCE_ERROR_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.SUBSCRIBER_ACCOUNT_EXPIRED_UPON_CREDIT_ATTEMPT),
                SUBSCRIBER_ACCOUNT_EXPIRED_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.MAX_RECHARGE_EXCEEDED_UPON_CREDIT_ATTEMPT),
                MAX_RECHARGE_EXCEEDED_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.INCORRECT_CREDIT_CUR_UPON_CREDIT_ATTEMPT),
                INCORRECT_CREDIT_CUR_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_OVERLOADED_UPON_CREDIT_ATTEMPT),
                RECHARGE_SYSTEM_OVERLOADED_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.RECHARGE_SYSTEM_INTERNAL_TIMEOUT_UPON_CREDIT_ATTEMPT),
                RECHARGE_SYSTEM_INTERNAL_TIMEOUT_UPON_CREDIT_ATTEMPT_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE_UPON_CREDIT_ATTEMPT),
                GENERIC_FAILURE_UPON_CREDIT_ATTEMPT_MSG);

        // 151, 152
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE),
                EXTENDED_CURRENCY_CONVERSION_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CURERNCY_CONVERSION_FAILED),
                EXTENDED_CURERNCY_CONVERSION_FAILED_MSG);

        // 160, 161, 162, 163
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.VOUCHER_SYSTEM_UNDEFINED), VOUCHER_SYSTEM_UNDEFINED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_SYSTEM_UNAVAILABLE),
                EXTENDED_VOUCHER_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_SYSTEM_MAX_RETRY_REACHED),
                EXTENDED_VOUCHER_SYSTEM_MAX_RETRY_REACHED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_VOUCHER_SYSTEM_TIMEOUT), EXTENDED_VOUCHER_SYSTEM_TIMEOUT_MSG);

        // 170, 171, 172, 173
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CHARGING_SYSTEM_UNDEFINED), EXTENDED_CHARGING_SYSTEM_UNDEFINED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CHARGING_SYSTEM_UNAVAILABLE),
                EXTENDED_CHARGING_SYSTEM_UNAVAILABLE_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CHARGING_SYSTEM_MAX_RETRY_REACHED),
                EXTENDED_CHARGING_SYSTEM_MAX_RETRY_REACHED_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_CHARGING_SYSTEM_TIMEOUT), EXTENDED_CHARGING_SYSTEM_TIMEOUT_MSG);

        // 180, 181, 182, 183, 184, 185, 186
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.NON_STANDARD_VOUCHER_SYSTEM_ERROR), NON_STANDARD_VOUCHER_SYSTEM_ERROR_MSG);
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.NON_STANDARD_CHARGING_SYSTEM_ERROR), NON_STANDARD_CHARGING_SYSTEM_ERROR_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.INVALID_VOUCHER_TYPE), INVALID_VOUCHER_TYPE_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.PRICE_PLAN_NOT_FOUND), PRICE_PLAN_NOT_FOUND_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.VOUCHER_TYPE_NOT_FOUND), VOUCHER_TYPE_NOT_FOUND_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.URCS_NOT_AVAILABLE), URCS_UNAVAILABLE_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.URCS_ACCOUNT_CREATION_FAILED), URCS_TIMEOUT_MSG);
        errorMsgMap_.put(new Integer(VRAResultCodes.CRM_NOT_AVAILABLE), CRM_NOT_AVAILABLE_MSG);

        // 199
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.EXTENDED_INTERNAL_SYSTEM_ERROR), EXTENDED_INTERNAL_SYSTEM_ERROR_MSG);

        // 229
        errorMsgMap_.put(Integer.valueOf(VRAResultCodes.GENERIC_FAILURE_VOUCHER_VALIDATION_AT_VOUCHER_SYSTEM),
                GENERIC_FAILURE_VOUCHER_VALIDATION_AT_VOUCHER_SYSTEM_MSG);
    }

    /**
     * Returns the configured message from MessageManager or the predefined error message default
     * associated with an error code.
     * @param ctx the operating context
     * @param errCode the error code for which to return the message
     * @return the error message
     */
    public static String getMessage(final Context ctx, final int errCode)
    {
        final MessageMgr manager = new MessageMgr(ctx, VRAReturnCodeMsgMapping.class);
        final String key = XMESSAGE_KEY_PREFIX + String.valueOf(errCode);
        String msg = manager.get(key);
        
        if (msg != null)
        {
            return msg;
        }
        
        msg = (String) errorMsgMap_.get(new Integer(errCode));

        if (msg != null)
        {
            return msg;
        }
        else
        {
            return NON_EXISTING_ERROR_MSG;
        }
    }

    private static final String XMESSAGE_KEY_PREFIX = "VRA.ResultCode.";

    /**
     * This prevents instantiation of this class.
     */
    private VRAReturnCodeMsgMapping()
    {
    }
}
