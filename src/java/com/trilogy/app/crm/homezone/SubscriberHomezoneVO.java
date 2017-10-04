/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.homezone;

import com.trilogy.app.homezone.corba.SubscriberHomezoneInfo;

/**
 * @author pkulkarni
 * 
 * VO to hold the return resultcode and the SubscriberHomezoneInfo object
 * together
 *  
 */
public class SubscriberHomezoneVO
{

    private SubscriberHomezoneInfo[] subHZInfos_;

    private int resultCode_;

    private String message_;

    public SubscriberHomezoneVO(SubscriberHomezoneInfo[] subHZInfos,
            int resultCode, String message)
    {
        subHZInfos_ = subHZInfos;
        resultCode_ = resultCode;
        message_ = message;
    }

    /**
     * @return Returns the subHZInfo_.
     */
    public SubscriberHomezoneInfo[] getSubHZInfos_()
    {
        return subHZInfos_;
    }

    /**
     * @return Returns the resultCode_.
     */
    public int getResultCode_()
    {
        return resultCode_;
    }

    /**
     * @return Returns the message_.
     */
    public String getMessage_()
    {
        return message_;
    }
}
