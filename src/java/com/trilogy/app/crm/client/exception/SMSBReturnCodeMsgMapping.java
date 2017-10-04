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
package com.trilogy.app.crm.client.exception;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.support.ExternalAppSupport;
/**
 * HashMap to store all the message returned by URCS SMS application.
 *
 * @author marcio.marques@redknee.com
 */
public final class SMSBReturnCodeMsgMapping
{
    private static final String UNKNOWN_ERROR_MSG = "Unknown error code";
    
    private static final Map<Integer,String> errorMsgMap_ = new HashMap<Integer,String>();
    private static final Map<Integer,String> bucketErrorMsgMap_ = new HashMap<Integer,String>();
    
    // com.redknee.product.bundle.manager.provision.bundle.error.ErroCode
    // Commonly Used Errors
    private static final String SUBSCRIBER_NOT_FOUND = "Subscription profile not found";
    private static final String SQL_ERROR = "SQL error occurred";
    private static final String INTERNAL_ERROR = "Internal error occurred";
    private static final String CORBA_COMM_FAILURE = "Communication error with URCS";
    private static final String RECORD_NOT_FOUND = "Record not found";
    private static final String SERVICE_NOT_AVAILABLE = "URCS down or not available";
    

   
    
    static
    {
        errorMsgMap_.put(Integer.valueOf(AppSmsbClient.RECORD_NOT_FOUND), RECORD_NOT_FOUND);
        errorMsgMap_.put(Integer.valueOf(AppSmsbClient.SUBSCRIBER_NOT_FOUND), SUBSCRIBER_NOT_FOUND);
        errorMsgMap_.put(Integer.valueOf(AppSmsbClient.SQL_ERROR), SQL_ERROR);
        errorMsgMap_.put(Integer.valueOf(AppSmsbClient.INTERNAL_ERROR), INTERNAL_ERROR);
        errorMsgMap_.put(Integer.valueOf(ExternalAppSupport.COMMUNICATION_FAILURE), CORBA_COMM_FAILURE);
        errorMsgMap_.put(Integer.valueOf(ExternalAppSupport.NO_CONNECTION), SERVICE_NOT_AVAILABLE);
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
            return msg + " (Code=" + String.valueOf(errCode)  +")";
        }
        else
        {
            return UNKNOWN_ERROR_MSG + " (Code=" + String.valueOf(errCode)  +")";
        }
    }
    
    /**
     * This prevents instantiation of this class.
     */
    private SMSBReturnCodeMsgMapping()
    {
    }
}
