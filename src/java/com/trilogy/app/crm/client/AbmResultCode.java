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
package com.trilogy.app.crm.client;


/**
 * Provides utilities for using ABM CORBA interface result codes.
 *
 * @author gary.anderson@redknee.com
 */
public final
class AbmResultCode
{
    // TODO This class should be autogenerated from ABM's errors.idl
    // Note: These map to com.redknee.product.abm.error.ErrorCode values
    public static final short SUCCESS = 0;
    public static final short UNKNOWN_ERROR = 104;
    public static final short RECORD_NOT_FOUND = 201;
    public static final short SQL_ERROR = 202;
    public static final short INTERNAL_ERROR = 203;
    public static final short OPS_NOT_ALLOWED = 204;
    public static final short MAX_OPS_EXCEEDED = 205;
    public static final short MAX_AMT_EXCEEDED = 206;
    public static final short NOT_ENOUGH_BAL = 207;
    public static final short ACCOUNT_EXPIRED = 208;
    public static final short SCP_NOT_ALLOWED = 209;
    public static final short BAD_DATA = 211;
    public static final short UNSUPPORTED_CURRENCY_CONVERSION = 213;
    public static final short SYSTEM_OVERLOAD = 214;
    public static final short UNEXPECTED_MESSAGE = 215;
    public static final short BAD_PROFILE_STATE = 216;
    public static final short BAD_DATA_SERVICE = 219;
    public static final short CMD_TIME_OUT = 220;
    public static final short CMD_RETRY_TIME_OUT = 221;
    public static final short INVALID_GROUP_MSISDN = 250;
    public static final short CREDIT_LIMIT_DECR_AMT_TOO_LARGE = 251;
    public static final short CREDIT_LIMIT_INCR_MAX_AMT_EXCEEDED = 252;
    public static final short INVALID_BUCKETID = 253;
    public static final short INVALID_SERVICEID = 254;
    public static final short DUPLICATE_BUCKETID = 255;
    public static final short DUPLICATE_SERVICEID = 256;
    public static final short INVALID_SPID = 257;
    public static final short DUPLICATE_MSISDN = 258;
    public static final short GROUP_MEMBERS_EXIST = 259;
    public static final short BUCKET_IN_USE = 260;
    public static final short GROUPCREDITLIMIT_EXCEEDED = 261;


    /**
     * Prevent instantiation of this utility class.
     */
    private AbmResultCode()
    {
        // Empty
    }


    /**
     * Gets a textual description of the result code.  Returns "Unknown Result
     * Code" if the code is not one of those defined in this class.
     *
     * @param code The AABM result code for which to return a textual
     * description.
     *
     * @return A textual description of the result code.
     */
    public static String toString(final short code)
    {
        final String message;

        switch (code)
        {
            case SUCCESS:
            {
                message = "SUCCESS";
                break;
            }
            case UNKNOWN_ERROR:
            {
                message = "UNKNOWN_ERROR";
                break;
            }
            case RECORD_NOT_FOUND:
            {
                message = "RECORD_NOT_FOUND";
                break;
            }
            case SQL_ERROR:
            {
                message = "SQL_ERROR";
                break;
            }
            case INTERNAL_ERROR:
            {
                message = "INTERNAL_ERROR";
                break;
            }
            case OPS_NOT_ALLOWED:
            {
                message = "OPS_NOT_ALLOWED";
                break;
            }
            case MAX_OPS_EXCEEDED:
            {
                message = "MAX_OPS_EXCEEDED";
                break;
            }
            case MAX_AMT_EXCEEDED:
            {
                message = "MAX_AMT_EXCEEDED";
                break;
            }
            case NOT_ENOUGH_BAL:
            {
                message = "NOT_ENOUGH_BAL";
                break;
            }
            case ACCOUNT_EXPIRED:
            {
                message = "ACCOUNT_EXPIRED";
                break;
            }
            case SCP_NOT_ALLOWED:
            {
                message = "SCP_NOT_ALLOWED";
                break;
            }
            case BAD_DATA:
            {
                message = "BAD_DATA";
                break;
            }
            case UNSUPPORTED_CURRENCY_CONVERSION:
            {
                message = "UNSUPPORTED_CURRENCY_CONVERSION";
                break;
            }
            case SYSTEM_OVERLOAD:
            {
                message = "SYSTEM_OVERLOAD";
                break;
            }
            case UNEXPECTED_MESSAGE:
            {
                message = "UNEXPECTED_MESSAGE";
                break;
            }
            case BAD_PROFILE_STATE:
            {
                message = "BAD_PROFILE_STATE";
                break;
            }
            case BAD_DATA_SERVICE:
            {
                message = "BAD_DATA_SERVICE";
                break;
            }
            case CMD_TIME_OUT:
            {
                message = "CMD_TIME_OUT";
                break;
            }
            case CMD_RETRY_TIME_OUT:
            {
                message = "CMD_RETRY_TIME_OUT";
                break;
            }
            case INVALID_GROUP_MSISDN:
            {
                message = "INVALID_GROUP_MSISDN";
                break;
            }
            case CREDIT_LIMIT_DECR_AMT_TOO_LARGE:
            {
                message = "CREDIT_LIMIT_DECR_AMT_TOO_LARGE";
                break;
            }
            case CREDIT_LIMIT_INCR_MAX_AMT_EXCEEDED:
            {
                message = "CREDIT_LIMIT_INCR_MAX_AMT_EXCEEDED";
                break;
            }
            case INVALID_BUCKETID:
            {
                message = "INVALID_BUCKETID";
                break;
            }
            case INVALID_SERVICEID:
            {
                message = "INVALID_SERVICEID";
                break;
            }
            case DUPLICATE_BUCKETID:
            {
                message = "DUPLICATE_BUCKETID";
                break;
            }
            case DUPLICATE_SERVICEID:
            {
                message = "DUPLICATE_SERVICEID";
                break;
            }
            case INVALID_SPID:
            {
                message = "INVALID_SPID";
                break;
            }
            case DUPLICATE_MSISDN:
            {
                message = "DUPLICATE_MSISDN";
                break;
            }
            case GROUP_MEMBERS_EXIST:
            {
                message = "GROUP_MEMBERS_EXIST";
                break;
            }
            case BUCKET_IN_USE:
            {
                message = "BUCKET_IN_USE";
                break;
            }
            case GROUPCREDITLIMIT_EXCEEDED:
                message = "GROUPCREDITLIMIT_EXCEEDED";
                break;
            default:
            {
                message = "Unknown Result Code";
            }
        }

        return message;
    }

} // class
