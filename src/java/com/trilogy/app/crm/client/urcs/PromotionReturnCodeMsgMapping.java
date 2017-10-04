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

import com.trilogy.app.urcs.promotion.v2_0.PromotionErrorCode;

/**
 * HashMap to store all the message returned by Promotion application
 *
 * @author marcio.marques@redknee.com
 */
public final class PromotionReturnCodeMsgMapping
{
    private static final String UNKNOWN_ERROR_MSG = "Unknown error code";
    
    private static final Map<Integer,String> errorMsgMap_ = new HashMap<Integer,String>();

    // com.redknee.product.bundle.manager.provision.bundle.error.ErroCode
    // Commonly Used Errors
    private static final String SUBSCRIBER_NOT_FOUND = "Subscriber not found";
    private static final String PROMOTION_NOT_FOUND = "Promotion not found";
    private static final String PROFILE_NOT_FOUND = "Profile not found";
    private static final String COUNTER_NOT_FOUND = "Counter not found";
    private static final String INVALID_DATA = "Invalid data";
    private static final String UNSUPPORTED_OPERATION = "Unsupported operation";
    private static final String SERVER_BUSY = "Server is busy";
    private static final String INTERNAL_ERROR = "Internal error occurred";
    private static final String CONNECTION_ERROR = "Connection error occurred";

    
    
    static
    {
        errorMsgMap_.put(PromotionErrorCode.SUBSCRIBER_NOT_FOUND, SUBSCRIBER_NOT_FOUND);
        errorMsgMap_.put(PromotionErrorCode.PROMOTION_NOT_FOUND, PROMOTION_NOT_FOUND);
        errorMsgMap_.put(PromotionErrorCode.PROFILE_NOT_FOUND, PROFILE_NOT_FOUND);
        errorMsgMap_.put(PromotionErrorCode.COUNTER_NOT_FOUND, COUNTER_NOT_FOUND);
        errorMsgMap_.put(PromotionErrorCode.INVALID_DATA, INVALID_DATA);
        errorMsgMap_.put(PromotionErrorCode.UNSUPPORTED_OPERATION, UNSUPPORTED_OPERATION);
        errorMsgMap_.put(PromotionErrorCode.SERVER_BUSY, SERVER_BUSY);
        errorMsgMap_.put(PromotionErrorCode.INTERNAL_ERROR, INTERNAL_ERROR);
        errorMsgMap_.put(PromotionErrorCode.CONNECTION_ERROR, CONNECTION_ERROR);
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
    private PromotionReturnCodeMsgMapping()
    {
    }
}
