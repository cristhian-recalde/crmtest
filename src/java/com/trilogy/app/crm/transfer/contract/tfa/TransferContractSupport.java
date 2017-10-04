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
package com.trilogy.app.crm.transfer.contract.tfa;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.TfaRmiConfig;
import com.trilogy.app.transferfund.rmi.data.AuthCredentials;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;

/**
 * Support class for shared methods on the tfa contract implementation
 * @author arturo.medina@redknee.com
 *
 */
public class TransferContractSupport
{
    //TFA Error codes
    
    public static final int ERR_INVALID_TFA_HOST = 0;
    public static final int ERR_AGREEMENTID_REFERENCED_CONTRACT_TABLE = 2402;
    public static final int ERR_AGREEMENTID_NOT_EXIST = 2403;
    public static final int ERR_CONTRACT_ALREADY_EXIST = 2404;
    public static final int ERR_INVALID_CONTRIB_GRPID = 2405;
    public static final int ERR_INVALID_RECIPIENT_GRPID = 2406;
    public static final int ERR_INCORRECT_CONTRACT_RULES = 2407;
    public static final int ERR_SPID_NOT_FOUND = 2408;
    public static final int ERR_CHARGINGID_ASSIGNED_SAME_TYPE = 2409;
    public static final int ERR_INVALID_ADJUSTMENT_AMOUNT_TYPE = 2410;
    public static final int ERR_TXAMOUNT_NOT_INCREMENTAL_ORDER = 2411;
    public static final int ERR_TRANSFERID_NOT_EXIST = 2412;
    public static final int ERR_CHARGINGID_NOT_EXIST = 2413;
    public static final int ERR_CHARGINGID_ASSIGNED_TWO_GROUPS = 2414;
    public static final int ERR_DELETION_CONTRACT_TABLE = 2415;
    public static final int ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE = 2416;
    public static final int ERR_CONTRACT_NOT_EXIST = 2417;
    public static final int ERR_INVALID_ADJUSTMENT_RATING_TYPE = 2418;
    public static final int ERR_INVALID_ADJUSTMENT_VALUE = 2419;
    public static final int ERR_INVALID_MIN_SURPLUS_DISCOUNT_VALUE = 2420;
    public static final int ERR_MIN_ADJUSTMENT_GREATER_MAX_ADJUSTMENT = 2421;
    public static final int ERR_INVALID_DISTRIBUTION_VALUE_TYPE = 2422;
    public static final int ERR_INTEFACE_AUTHENTICATION_FAILURE = 2423;
    public static final int ERR_CONTRACTID_NOTEXIST = 2424;
    public static final int ERR_INVALID_OWNER = 2425;
    public static final int ERR_CONTRACT_RULES_VIOLATION = 2426;
    public static final int ERR_NEGATIVE_ADJUSTMENT_AMOUNT = 2427;
    public static final int ERR_INVALID_PARAM = 2428;
    public static final int ERR_CONTRACT_OWNER_AGREEMENT_OWNER_MISMATCH = 2429;
    public static final int ERR_CONTRACT_SPID_AGREEMENT_SPID_MISMATCH = 2430;
    public static final int ERR_CONTRACT_SPID_CONT_SPID_MISMATCH = 2431;
    public static final int ERR_CONTRACT_SPID_CONT_GROUP_SPID_MISMATCH = 2432;
    public static final int ERR_EQUAL_CONT_RECP_SPID = 2433;
    public static final int ERR_INVALID_CONTRACT_TYPE = 2434;
    public static final int ERR_INVALID_CONTRIB_CHARGING_TYPE = 2435;
    public static final int ERR_INVALID_RECP_CHARGING_TYPE = 2436;
    public static final int ERR_TRANSFER_GROUP_NOTEXISTS = 2437;
    public static final int ERR_TRANSFER_GROUP_INUSE = 2438;
    public static final int ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP = 2439;
    public static final int ERR_NEGATIVE_MIN_TRANSFERAMT = 2440;
    public static final int ERR_MIN_TRANAMT_GREATER_MAX_TRANAMT = 2441;
    public static final int ERR_OWNER_NOT_EXISTS_AGREEMENT = 2442;
    public static final int ERR_INVALID_ADJUSTMENT_AMOUNT = 2243;
    public static final int ERR_MEMBER_GROUP_NOTEXISTS = 2444;
    public static final int ERR_PUBLIC_OWNER_NOTALLOWED = 2445;
    public static final int ERR_CHARGINGID_MAX_GROUPREACHED = 2446;
    public static final int ERR_MEMBERGROUP_SPID_MISMATCH = 2447;
    public static final int ERR_CHARGINGID_MAX_PUBLIC_GROUPREACHED = 2448;
    public static final int ERR_CHARGINGID_MAX_PRIVATE_GROUPREACHED = 2449;
    public static final int ERR_DEFAULT_GROUP = 2450;
    public static final int ERR_9999_FAIL_ERROR_UNKNOWN = 9999;

    //Mapped  error codes
    
    public static final String ERR_INVALID_TFA_HOST_MSG = "Unable to connect to host";
    public static final String ERR_AGREEMENTID_REFERENCED_CONTRACT_TABLE_MSG = "Agreement is associated with contract";
    public static final String ERR_AGREEMENTID_NOT_EXIST_MSG = "Agreement ID doesn't exist";
    public static final String ERR_CONTRACT_ALREADY_EXIST_MSG = "Contract already exists";
    public static final String ERR_INVALID_CONTRIB_GRPID_MSG = "Invalid contributor group id";
    public static final String ERR_INVALID_RECIPIENT_GRPID_MSG = "Invalid recepient group";
    public static final String ERR_INCORRECT_CONTRACT_RULES_MSG = "Incorrect contract rules";
    public static final String ERR_SPID_NOT_FOUND_MSG = "SPID not found";
    public static final String ERR_CHARGINGID_ASSIGNED_SAME_TYPE_MSG = "Charging id assigned to the same type";
    public static final String ERR_INVALID_ADJUSTMENT_AMOUNT_TYPE_MSG = "Invalid adjustment amount type";
    public static final String ERR_TXAMOUNT_NOT_INCREMENTAL_ORDER_MSG = "Tax amount not in incremental order";
    public static final String ERR_TRANSFERID_NOT_EXIST_MSG = "Transfer ID doesn't exists";
    public static final String ERR_CHARGINGID_NOT_EXIST_MSG = "Charging ID doesn't exists";
    public static final String ERR_CHARGINGID_ASSIGNED_TWO_GROUPS_MSG = "Charging assigned in two groups";
    public static final String ERR_DELETION_CONTRACT_TABLE_MSG = "Deletion contract table";
    public static final String ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE_MSG = "Owner doesn't exists in the contract table";
    public static final String ERR_CONTRACT_NOT_EXIST_MSG = "Contract doesn't exists";
    public static final String ERR_INVALID_ADJUSTMENT_RATING_TYPE_MSG = "Invalid adjustment rating type";
    public static final String ERR_INVALID_ADJUSTMENT_VALUE_MSG = "Invalid adjustment value";
    public static final String ERR_INVALID_MIN_SURPLUS_DISCOUNT_VALUE_MSG = "Invalid min surplus discount value";
    public static final String ERR_MIN_ADJUSTMENT_GREATER_MAX_ADJUSTMENT_MSG = "The minimum adjustment is greater than the maximum adjustment";
    public static final String ERR_INVALID_DISTRIBUTION_VALUE_TYPE_MSG = "Invalid distribution value type";
    public static final String ERR_INTEFACE_AUTHENTICATION_FAILURE_MSG = "Authentication failure";
    public static final String ERR_CONTRACTID_NOTEXIST_MSG = "Contract id doesn't exists";
    public static final String ERR_INVALID_OWNER_MSG = "Invalid owner";
    public static final String ERR_CONTRACT_RULES_VIOLATION_MSG = "Contract rules violation";
    public static final String ERR_NEGATIVE_ADJUSTMENT_AMOUNT_MSG = "Negative adjustment amount";
    public static final String ERR_INVALID_PARAM_MSG = "Invalid parameter";
    public static final String ERR_CONTRACT_OWNER_AGREEMENT_OWNER_MISMATCH_MSG = "Contract owner and agrement owner don't match";
    public static final String ERR_CONTRACT_SPID_AGREEMENT_SPID_MISMATCH_MSG = "Contract spid and agreement spid don't match";
    public static final String ERR_CONTRACT_SPID_CONT_SPID_MISMATCH_MSG = "Transfer contract spid don't match";
    public static final String ERR_CONTRACT_SPID_CONT_GROUP_SPID_MISMATCH_MSG = "Group contract spid don't match";
    public static final String ERR_EQUAL_CONT_RECP_SPID_MSG = "The contributor and recipients spid are the same";
    public static final String ERR_INVALID_CONTRACT_TYPE_MSG = "Invalid contract type";
    public static final String ERR_INVALID_CONTRIB_CHARGING_TYPE_MSG = "Invalid contributor charging type";
    public static final String ERR_INVALID_RECP_CHARGING_TYPE_MSG = "Invalid recepient charging type";
    public static final String ERR_TRANSFER_GROUP_NOTEXISTS_MSG = "Transfer group doesn't exists";
    public static final String ERR_TRANSFER_GROUP_INUSE_MSG = "Transfer group is in use";
    public static final String ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP_MSG = "Owner doesn't exist in the transfer group";
    public static final String ERR_NEGATIVE_MIN_TRANSFERAMT_MSG = "Negative minimum transfer amount";
    public static final String ERR_MIN_TRANAMT_GREATER_MAX_TRANAMT_MSG = "Minimum transfer amount is greater than the maximum transfer amount";
    public static final String ERR_OWNER_NOT_EXISTS_AGREEMENT_MSG = "Owner doesn't exists in the agreement";
    public static final String ERR_INVALID_ADJUSTMENT_AMOUNT_MSG = "Invalid adjustment amount";
    public static final String ERR_MEMBER_GROUP_NOTEXISTS_MSG = "Member group doesn't exists";
    public static final String ERR_PUBLIC_OWNER_NOTALLOWED_MSG = "Public owner is not alloed";
    public static final String ERR_CHARGINGID_MAX_GROUPREACHED_MSG = "Charging id max the group reached";
    public static final String ERR_MEMBERGROUP_SPID_MISMATCH_MSG = "Memeber group spid mismatch";
    public static final String ERR_CHARGINGID_MAX_PUBLIC_GROUPREACHED_MSG = "Charging id maximum reached the public group";
    public static final String ERR_CHARGINGID_MAX_PRIVATE_GROUPREACHED_MSG = "Charging id maximum reached the private group";
    public static final String ERR_DEFAULT_GROUP_MSG = "Unable to remove default contract group";
    public static final String ERR_9999_FAIL_ERROR_UNKNOWN_MSG = "Unknown Error";

    private static Map<Integer, String> errorCodes = new HashMap<Integer, String>();
    static
    {
        errorCodes.put(ERR_INVALID_TFA_HOST, ERR_INVALID_TFA_HOST_MSG);
        errorCodes.put(ERR_AGREEMENTID_REFERENCED_CONTRACT_TABLE, ERR_AGREEMENTID_REFERENCED_CONTRACT_TABLE_MSG);
        errorCodes.put(ERR_AGREEMENTID_NOT_EXIST, ERR_AGREEMENTID_NOT_EXIST_MSG);
        errorCodes.put(ERR_CONTRACT_ALREADY_EXIST, ERR_CONTRACT_ALREADY_EXIST_MSG);
        errorCodes.put(ERR_INVALID_CONTRIB_GRPID, ERR_INVALID_CONTRIB_GRPID_MSG);
        errorCodes.put(ERR_INVALID_RECIPIENT_GRPID , ERR_INVALID_RECIPIENT_GRPID_MSG);
        errorCodes.put(ERR_INCORRECT_CONTRACT_RULES, ERR_INCORRECT_CONTRACT_RULES_MSG);
        errorCodes.put(ERR_SPID_NOT_FOUND , ERR_SPID_NOT_FOUND_MSG);
        errorCodes.put(ERR_CHARGINGID_ASSIGNED_SAME_TYPE, ERR_CHARGINGID_ASSIGNED_SAME_TYPE_MSG);
        errorCodes.put(ERR_INVALID_ADJUSTMENT_AMOUNT_TYPE, ERR_INVALID_ADJUSTMENT_AMOUNT_TYPE_MSG);
        errorCodes.put(ERR_TXAMOUNT_NOT_INCREMENTAL_ORDER, ERR_TXAMOUNT_NOT_INCREMENTAL_ORDER_MSG);
        errorCodes.put(ERR_TRANSFERID_NOT_EXIST, ERR_TRANSFERID_NOT_EXIST_MSG);
        errorCodes.put(ERR_CHARGINGID_NOT_EXIST, ERR_CHARGINGID_NOT_EXIST_MSG);
        errorCodes.put(ERR_CHARGINGID_ASSIGNED_TWO_GROUPS, ERR_CHARGINGID_ASSIGNED_TWO_GROUPS_MSG);
        errorCodes.put(ERR_DELETION_CONTRACT_TABLE, ERR_DELETION_CONTRACT_TABLE_MSG);
        errorCodes.put(ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE, ERR_OWNER_NOT_EXISTS_CONTRACT_TABLE_MSG);
        errorCodes.put(ERR_CONTRACT_NOT_EXIST, ERR_CONTRACT_NOT_EXIST_MSG);
        errorCodes.put(ERR_INVALID_ADJUSTMENT_RATING_TYPE,ERR_INVALID_ADJUSTMENT_RATING_TYPE_MSG);
        errorCodes.put(ERR_INVALID_ADJUSTMENT_VALUE,ERR_INVALID_ADJUSTMENT_VALUE_MSG);
        errorCodes.put(ERR_INVALID_MIN_SURPLUS_DISCOUNT_VALUE,ERR_INVALID_MIN_SURPLUS_DISCOUNT_VALUE_MSG);
        errorCodes.put(ERR_MIN_ADJUSTMENT_GREATER_MAX_ADJUSTMENT,ERR_MIN_ADJUSTMENT_GREATER_MAX_ADJUSTMENT_MSG);
        errorCodes.put(ERR_INVALID_DISTRIBUTION_VALUE_TYPE,ERR_INVALID_DISTRIBUTION_VALUE_TYPE_MSG);
        errorCodes.put(ERR_INTEFACE_AUTHENTICATION_FAILURE,ERR_INTEFACE_AUTHENTICATION_FAILURE_MSG);
        errorCodes.put(ERR_CONTRACTID_NOTEXIST,ERR_CONTRACTID_NOTEXIST_MSG);
        errorCodes.put(ERR_INVALID_OWNER,ERR_INVALID_OWNER_MSG);
        errorCodes.put(ERR_CONTRACT_RULES_VIOLATION,ERR_CONTRACT_RULES_VIOLATION_MSG);
        errorCodes.put(ERR_NEGATIVE_ADJUSTMENT_AMOUNT,ERR_NEGATIVE_ADJUSTMENT_AMOUNT_MSG);
        errorCodes.put(ERR_INVALID_PARAM,ERR_INVALID_PARAM_MSG);
        errorCodes.put(ERR_CONTRACT_OWNER_AGREEMENT_OWNER_MISMATCH,ERR_CONTRACT_OWNER_AGREEMENT_OWNER_MISMATCH_MSG);
        errorCodes.put(ERR_CONTRACT_SPID_AGREEMENT_SPID_MISMATCH,ERR_CONTRACT_SPID_AGREEMENT_SPID_MISMATCH_MSG);
        errorCodes.put(ERR_CONTRACT_SPID_CONT_SPID_MISMATCH,ERR_CONTRACT_SPID_CONT_SPID_MISMATCH_MSG);
        errorCodes.put(ERR_CONTRACT_SPID_CONT_GROUP_SPID_MISMATCH,ERR_CONTRACT_SPID_CONT_GROUP_SPID_MISMATCH_MSG);
        errorCodes.put(ERR_EQUAL_CONT_RECP_SPID,ERR_EQUAL_CONT_RECP_SPID_MSG);
        errorCodes.put(ERR_INVALID_CONTRACT_TYPE,ERR_INVALID_CONTRACT_TYPE_MSG);
        errorCodes.put(ERR_INVALID_CONTRIB_CHARGING_TYPE,ERR_INVALID_CONTRIB_CHARGING_TYPE_MSG);
        errorCodes.put(ERR_INVALID_RECP_CHARGING_TYPE,ERR_INVALID_RECP_CHARGING_TYPE_MSG);
        errorCodes.put(ERR_TRANSFER_GROUP_NOTEXISTS,ERR_TRANSFER_GROUP_NOTEXISTS_MSG);
        errorCodes.put(ERR_TRANSFER_GROUP_INUSE,ERR_TRANSFER_GROUP_INUSE_MSG);
        errorCodes.put(ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP,ERR_OWNER_NOT_EXISTS_TRANSFER_GROUP_MSG);
        errorCodes.put(ERR_NEGATIVE_MIN_TRANSFERAMT,ERR_NEGATIVE_MIN_TRANSFERAMT_MSG);
        errorCodes.put(ERR_MIN_TRANAMT_GREATER_MAX_TRANAMT,ERR_MIN_TRANAMT_GREATER_MAX_TRANAMT_MSG);
        errorCodes.put(ERR_OWNER_NOT_EXISTS_AGREEMENT,ERR_OWNER_NOT_EXISTS_AGREEMENT_MSG);
        errorCodes.put(ERR_INVALID_ADJUSTMENT_AMOUNT,ERR_INVALID_ADJUSTMENT_AMOUNT_MSG);
        errorCodes.put(ERR_MEMBER_GROUP_NOTEXISTS,ERR_MEMBER_GROUP_NOTEXISTS_MSG);
        errorCodes.put(ERR_PUBLIC_OWNER_NOTALLOWED,ERR_PUBLIC_OWNER_NOTALLOWED_MSG);
        errorCodes.put(ERR_CHARGINGID_MAX_GROUPREACHED,ERR_CHARGINGID_MAX_GROUPREACHED_MSG);
        errorCodes.put(ERR_MEMBERGROUP_SPID_MISMATCH,ERR_MEMBERGROUP_SPID_MISMATCH_MSG);
        errorCodes.put(ERR_CHARGINGID_MAX_PUBLIC_GROUPREACHED,ERR_CHARGINGID_MAX_PUBLIC_GROUPREACHED_MSG);
        errorCodes.put(ERR_CHARGINGID_MAX_PRIVATE_GROUPREACHED,ERR_CHARGINGID_MAX_PRIVATE_GROUPREACHED_MSG);
        errorCodes.put(ERR_DEFAULT_GROUP,ERR_DEFAULT_GROUP_MSG);
        errorCodes.put(ERR_9999_FAIL_ERROR_UNKNOWN,ERR_9999_FAIL_ERROR_UNKNOWN_MSG);
    }

    /**
     * 
     * @param ctx
     * @return : returns the owner name which is used to identify Public groups on TFA.
     */
    public static String getPublicGroupOwnerName(final Context ctx) throws HomeException
    {
        TfaRmiConfig config = getConfig(ctx);
        return config.getPublicOwnerName();
    }
    
    public static TfaRmiConfig getConfig(final Context ctx)
    {
        return (TfaRmiConfig) ctx.get(TfaRmiConfig.class);        
    }
    
    /**
     * Gets the username and password from the configuration screen.
     * @param ctx the operating context
     * @return the credentials for CRM
     */
    public static AuthCredentials getCredentials(final Context ctx)
    {
        final TfaRmiConfig config = getConfig(ctx);

        final AuthCredentials credentials = new AuthCredentials();

        credentials.setUserName(config.getUsername());
        credentials.setPassWord(config.getPassword(ctx));
        return credentials;
    }

    /**
     * Gets the erReference from the configuration screen.
     * @param ctx the operating context
     * @return the credentials for CRM
     */
    public static String getErReference(Context ctx)
    {
        final TfaRmiConfig config = getConfig(ctx);
        return config.getErReference();
    }
    
    /**
     * 
     * @param errorCode
     * @return
     */
    public static String mapErrorCodeWithMessage(int errorCode)
    {
        StringBuilder message = new StringBuilder();
        message.append(errorCode);
        message.append("-");
        message.append(errorCodes.get(errorCode));
        return message.toString();
    }
    
    public static String TRANSFER_AGREEMENT_OWNER_ID = "TRANSFER_AGREEMENT_OWNER_ID";
    public static String TRANSFER_CONTRACT_OWNER_ID = "TRANSFER_CONTRACT_OWNER_ID";
    public static String TRANSFER_CONTRACT_PUBLIC_GROUP_ID = "TRANSFER_CONTRACT_PUBLIC_GROUP_ID";
    public static String TRANSFER_CONTRACT_PRIVATE_GROUP_ID = "TRANSFER_CONTRACT_PRIVATE_GROUP_ID";
    public static String TRANSFER_CONTRACT_GROUP_ID = "TRANSFER_CONTRACT_GROUP_ID";
    
}
