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
package com.trilogy.app.crm.client.ringbacktone;

/**
 * The exception that is thrown during RBT provisioning operations.
 * 
 * @author nick.landry@redknee.com
 */
public class RBTClientException extends Exception
{
    private static final long serialVersionUID = -7745766074267164391L;
    
    public RBTClientException(int resultCode)
    {
        super();
        resultCode_ = resultCode;
    }

    public RBTClientException(String arg0, int resultCode, Throwable arg1)
    {
        super(arg0, arg1);
        resultCode_ = resultCode;
    }

    public RBTClientException(String arg0, int resultCode)
    {
        super(arg0);
        resultCode_ = resultCode;
    }

    public RBTClientException(Throwable arg0, int resultCode)
    {
        super(arg0);
        resultCode_ = resultCode;
    }

    public int getResultCode()
    {
        return resultCode_;
    }

    
    protected int resultCode_;
}
