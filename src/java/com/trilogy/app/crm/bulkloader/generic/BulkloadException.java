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
package com.trilogy.app.crm.bulkloader.generic;


/**
 * A Bulk Load Exception
 */
public class BulkloadException extends Exception 
{

    public BulkloadException(int sessionId, String msg)
    {
        super(msg);
        sessionId_ = sessionId;
    }

    
    public BulkloadException(int sessionId, String msg, Throwable cause)
    {
        super(msg, cause);
        sessionId_ = sessionId;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public String getMessage()
    {
        return getMessage(true);
    }
    

    public String getMessage(boolean includeSessionId)
    {
        StringBuilder message = new StringBuilder();
        message.append("[Session ID=" + sessionId_ + "] ");
        message.append(super.getMessage());
        
        return message.toString();
    }
    

    private int sessionId_;
}
