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
package com.trilogy.app.crm.client.urcs;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;

/**
 * HashMap to store all the message returned by URCS application during
 * Provision and Operation commands.
 *
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public final class LoyaltyReturnCodeHelper
{
    private static final String UNKNOWN_ERROR_MSG = "Unknown error code";


    //Common
    private static final String SUCCESS = "SUCCESS";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    //Provision
    private static final String SUBSCRIBER_NOT_FOUND  = "SUBSCRIBER_NOT_FOUND";
    private static final String SQL_ERROR = "SQL_ERROR";
    private static final String BAD_PARAMETER = "BAD_PARAMETER";
    private static final String MAX_OPS_EXCEEDED = "MAX_OPS_EXCEEDED";
    private static final String LOYALTY_RECORD_NOT_FOUND = "LOYALTY_RECORD_NOT_FOUND";
    private static final String RECORD_ALREADY_EXISTS = "RECORD_ALREADY_EXISTS";
    
    private static final Map<Integer,String> errorProvisionMap_ = new HashMap<Integer,String>();
    static
    {
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.SUCCESS), SUCCESS);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.INTERNAL_ERROR), INTERNAL_ERROR);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.SUBSCRIBER_NOT_FOUND), SUBSCRIBER_NOT_FOUND);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.SQL_ERROR), SQL_ERROR);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.BAD_PARAMETER), BAD_PARAMETER);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.MAX_OPS_EXCEEDED), MAX_OPS_EXCEEDED);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.LOYALTY_RECORD_NOT_FOUND), LOYALTY_RECORD_NOT_FOUND);
        errorProvisionMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.v4_0.loyalty.error.ErrorCode.RECORD_ALREADY_EXISTS), RECORD_ALREADY_EXISTS);
    }
    
    //Operations
    private static final String LOYALTY_PROFILE_NOT_FOUND = "LOYALTY_PROFILE_NOT_FOUND";
    private static final String LOYALTY_RATE_NOT_FOUND  = "LOYALTY_RATE_NOT_FOUND";
    private static final String LOYALTY_ACCUMULATION_DISABLED = "LOYALTY_ACCUMULATION_DISABLED";
    private static final String LOYALTY_REDEMPTION_DISABLED = "LOYALTY_REDEMPTION_DISABLED";
    private static final String LOYALTY_COUNTER_ERROR = "LOYALTY_COUNTER_ERROR";
    private static final String LOYALTY_UPDATE_FAIL_AS_BALLANCE_ZERO = "LOYALTY_UPDATE_FAIL_AS_BALLANCE_ZERO";

    private static final Map<Integer,String> errorOperationsMap_ = new HashMap<Integer,String>();
    static
    {
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.SUCCESS), SUCCESS);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_PROFILE_NOT_FOUND), LOYALTY_PROFILE_NOT_FOUND);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_RATE_NOT_FOUND), LOYALTY_RATE_NOT_FOUND);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.INTERNAL_ERROR), INTERNAL_ERROR);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_ACCUMULATION_DISABLED), LOYALTY_ACCUMULATION_DISABLED);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_REDEMPTION_DISABLED), LOYALTY_REDEMPTION_DISABLED);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_COUNTER_ERROR), LOYALTY_COUNTER_ERROR);
        errorOperationsMap_.put(Integer.valueOf(com.redknee.app.urcs.loyaltyoperation.error.ErrorCode.LOYALTY_UPDATE_FAIL_AS_BALLANCE_ZERO), LOYALTY_UPDATE_FAIL_AS_BALLANCE_ZERO);
    }

    /**
     * Returns LoyaltyProfileProvision interface error description
     * @param ctx the operating context
     * @param errCode the error code for which to return the message
     * @return the error message
     */
    public static String getProfileMessage(Context ctx, final int errCode)
    {
        return getMessage(errorProvisionMap_, errCode);
    }

    /**
     * Returns LoyaltyOperations interface error description
     * @param ctx the operating context
     * @param errCode the error code for which to return the message
     * @return the error message
     */
    public static String getOperationMessage(Context ctx, final int errCode)
    {
        return getMessage(errorOperationsMap_, errCode);
    }
    
    private static String getMessage(final Map<Integer, String> map, final int errCode)
    {
        String msg = map.get(Integer.valueOf(errCode));

        if (msg != null)
        {
            return msg;
        }
        else
        {
            return UNKNOWN_ERROR_MSG;
        }
    }
    /**
     * This prevents instantiation of this class.
     */
    private LoyaltyReturnCodeHelper()
    {
    }
}
