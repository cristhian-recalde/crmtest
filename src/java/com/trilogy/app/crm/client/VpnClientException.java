/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client;


/**
 * @author ypakran
 *  
 */
public class VpnClientException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * @param string
     * @param result
     */
    public VpnClientException(String msg, int result)
    {
        super(msg + " result="+result );
     
    }


    public VpnClientException(String s)
    {
        super(s);
    }


    public VpnClientException(String msg, Throwable cause)
    {
        super(msg, cause);
    }


}
