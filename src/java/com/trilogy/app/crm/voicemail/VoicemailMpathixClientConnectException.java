/*
* This code is a protected work and subject to domestic and international
* copyright law(s). A complete listing of authors of this work is readily
* available. Additionally, source code is, by its very nature, confidential
* information and inextricably contains trade secrets and other information
* proprietary, valuable and sensitive to Redknee, no unauthorised use,
* disclosure, manipulation or otherwise is permitted, and may only be used
* in accordance with the terms of the licence agreement entered into with
* Redknee Inc. and/or its subsidiaries.
*
* Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
*/ 
package com.trilogy.app.crm.voicemail;


/**
 * @author Prasanna.Kulkarni
 * @time Oct 12, 2005
 */
public class VoicemailMpathixClientConnectException extends Exception
{
    public VoicemailMpathixClientConnectException(String msg, Exception e)
    {
        super(msg,e);
    }

    public VoicemailMpathixClientConnectException(String msg)
    {
        super(msg);
    }
    
    public VoicemailMpathixClientConnectException(Exception e)
    {
        super(e);
    }

    public VoicemailMpathixClientConnectException(Throwable thr)
    {
        super(thr);
    }
}
