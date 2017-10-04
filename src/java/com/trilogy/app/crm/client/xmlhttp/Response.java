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
package com.trilogy.app.crm.client.xmlhttp;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.util.Objects;

/**
 * 
 * @author gary.anderson@redknee.com
 */
public class Response extends Objects
{

    /**
     * Gets the resultCode.
     *
     * @return The resultCode.
     */
    public String getResultCode()
    {
        return resultCode_;
    }
    /**
     * Sets the resultCode.
     *
     * @param resultCode The resultCode.
     */
    public void setResultCode(String resultCode)
    {
        resultCode_ = resultCode;
    }
    /**
     * Gets the resultMessage.
     *
     * @return The resultMessage.
     */
    public String getResultMessage()
    {
        return resultMessage_;
    }
    /**
     * Sets the resultMessage.
     *
     * @param resultMessage The resultMessage.
     */
    public void setResultMessage(String resultMessage)
    {
        resultMessage_ = resultMessage;
    }
    /**
     * Gets the result.
     *
     * @return The result.
     */
    public Object getResult()
    {
        return getObject(Object.class);
    }
    /**
     * Sets the result.
     *
     * @param result The result.
     */
    public void setResult(Object result)
    {
        putObject(Object.class, result);
        resultMap_.put(result.getClass(),result);
    }
    
    
    /**
     * Converts result into String Representing the result code and message 
     */
    @Override
    public String toString()
    {
        return "[Result(Code=" + getResultCode() + "),(message=" + getResultMessage() +")]";
    }
    private String resultCode_;
    private String resultMessage_;
}
