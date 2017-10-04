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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.urcs.vpn.ErrorCode;

/**
 * Exception thrown by BusinessGroupPricePlanProvisioning interface
 *
 * @author victor.stratan@redknee.com
 */
public class BGroupPricePlanException extends Exception 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BGroupPricePlanException(final short resultCode, final String message)
    {
        super(message);
        this.resultCode_ = resultCode;
    }

    public BGroupPricePlanException(final short resultCode, final String message, final Throwable cause)
    {
        super(message, cause);
        this.resultCode_ = resultCode;
    }

    public static String getVerboseResult(final Context ctx, final short resultCode)
    {
        final MessageMgr mmgr = new MessageMgr(ctx, getModule());
        String value;
        switch (resultCode)
        {
            case ErrorCode.SUCCESS:
                value = mmgr.get(SUCCESS_KEY, SUCCESS_DEFAULT_MESSAGE);
                break;
            case ErrorCode.SQL_ERROR:
                value = mmgr.get(SQL_ERROR_KEY, SQL_ERROR_DEFAULT_MESSAGE);
                break;
            case ErrorCode.INTERNAL_ERROR:
                value = mmgr.get(INTERNAL_ERROR_KEY, INTERNAL_ERROR_DEFAULT_MESSAGE);
                break;
            case ErrorCode.ENTRY_NOT_FOUND:
                value = mmgr.get(ENTRY_NOT_FOUND_KEY, ENTRY_NOT_FOUND_DEFAULT_MESSAGE);
                break;
            case ErrorCode.INVALID_SPID:
                value = mmgr.get(INVALID_SPID_KEY, INVALID_SPID_DEFAULT_MESSAGE);
                break;
            case ErrorCode.INVALID_DATA:
                value = mmgr.get(INVALID_DATA_KEY, INVALID_DATA_DEFAULT_MESSAGE);
                break;
            default:
                value = "Unspecified Error occurred.";
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Result code: ");
        sb.append(this.resultCode_);
        sb.append(", ");
        sb.append(super.toString());
        return sb.toString();
    }

    public short getResultCode()
    {
        return resultCode_;
    }

    private static Class getModule()
    {
        return BGroupPricePlanException.class;
    }

    /**
     * URCS Result Code
     */
    private final short resultCode_;

    private final static String SUCCESS_KEY = "SUCCESS";
    private final static String SUCCESS_DEFAULT_MESSAGE = "Successful Result";
    private final static String SQL_ERROR_KEY = "SQL_ERROR";
    private final static String SQL_ERROR_DEFAULT_MESSAGE = "SQL Error occurred in URCS.";
    private final static String INTERNAL_ERROR_KEY = "INTERNAL_ERROR";
    private final static String INTERNAL_ERROR_DEFAULT_MESSAGE = "Internal Error occurred in URCS.";
    private final static String ENTRY_NOT_FOUND_KEY = "NO_BUSINESS_GROUP_FOUND";
    private final static String ENTRY_NOT_FOUND_DEFAULT_MESSAGE = "No such Business Group was found in URCS.";
    private final static String INVALID_SPID_KEY = "INVALID_SPID";
    private final static String INVALID_SPID_DEFAULT_MESSAGE = "The Service Provider ID passed in is not valid.";
    private final static String INVALID_DATA_KEY = "INVALID_DATA";
    private final static String INVALID_DATA_DEFAULT_MESSAGE = "The data passed in was not valid.";
}
