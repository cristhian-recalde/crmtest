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
package com.trilogy.app.crm.client.bm;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap to store all the message returned by URCS application during
 * Subscriber and Subscription profile creation process.
 *
 * @author victor.stratan@redknee.com
 * @author marcio.marques@redknee.com
 */
public final class URCSReturnCodeMsgMapping
{
    private static final String UNKNOWN_ERROR_MSG = "Unknown error code";

    private static final Map<Integer,String> errorMsgMap_ = new HashMap<Integer,String>();

    // com.redknee.product.bundle.manager.provision.profile.error.ErrorCode
    // Commonly Used Errors
    private static final String UNKNOWN_ERROR = "Unknown error";
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String SQL_ERROR = "SQL error";
    private static final String INTERNAL_ERROR = "Internal error";
    // invalid input parameters format
    private static final String BAD_PARAMETER = "Bad input parameters format";

    // cannot accept more transaction. only used in throttling
    private static final String  MAX_OPS_EXCEEDED = "Maximum number of operators exceeded. Throttling ON.";

    // Sub Profile Provisioning Errors
    private static final String INVALID_SPID = "Invalid SPID";
    private static final String GROUP_ACCOUNT_NOT_FOUND = "Group Account not found";
    private static final String SUBSCRIBER_IN_USE = "Subscriber Account in use";
    private static final String RECORD_ALREADY_EXIST = "Record already exists";
    private static final String GROUP_IN_USE = "Group in use";

    static
    {
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.UNKNOWN_ERROR), UNKNOWN_ERROR);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.RECORD_NOT_FOUND), RECORD_NOT_FOUND);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.SQL_ERROR), SQL_ERROR);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.INTERNAL_ERROR), INTERNAL_ERROR);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.BAD_PARAMETER), BAD_PARAMETER);

        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.MAX_OPS_EXCEEDED), MAX_OPS_EXCEEDED);

        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.INVALID_SPID), INVALID_SPID);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.GROUP_ACCOUNT_NOT_FOUND), GROUP_ACCOUNT_NOT_FOUND);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.SUBSCRIBER_IN_USE), SUBSCRIBER_IN_USE);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.RECORD_ALREADY_EXIST), RECORD_ALREADY_EXIST);
        errorMsgMap_.put(Integer.valueOf(com.redknee.product.bundle.manager.provision.profile.error.ErrorCode.GROUP_IN_USE), GROUP_IN_USE);
    }

    /**
     * Returns the predefined error message default
     * associated with an error code.
     * @param ctx the operating context
     * @param errCode the error code for which to return the message
     * @return the error message
     */
    public static String getMessage(final int errCode)
    {
        String msg = errorMsgMap_.get(Integer.valueOf(errCode));

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
    private URCSReturnCodeMsgMapping()
    {
    }
}
