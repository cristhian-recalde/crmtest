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

import com.trilogy.app.homezone.corba.SubscriberHomezoneInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author pkulkarni
 */
public class AppHomezoneSupport implements ContextAgent
{

    /*
     * A test method to create a AppHomezone provisioning client and testing various
     * provisioning methods on it
     */
    public void execute(Context ctx) throws AgentException
    {
        SubscriberHomezoneInfo info0 = new SubscriberHomezoneInfo("919326035308", 0, 1, 0.0, 0.0, 0);
        SubscriberHomezoneInfo info1 = new SubscriberHomezoneInfo("919326035308", 1, 2, 0.1, 0.1, 1);
        SubscriberHomezoneInfo info2 = new SubscriberHomezoneInfo("919326035308", 2, 3, 0.2, 0.2, 2);
        SubscriberHomezoneInfo info3 = new SubscriberHomezoneInfo("919326035308", 3, 4, 0.3, 0.3, 3);
        SubscriberHomezoneInfo info4 = new SubscriberHomezoneInfo("919326035308", 9, 5, 5.5, 0.5, 0);
        SubscriberHomezoneInfo[] infoArr = new SubscriberHomezoneInfo[]
            {info0, info1, info2, info3};
        AppHomezoneClient hzClient = (AppHomezoneClient) ctx.get(com.redknee.app.crm.client.AppHomezoneClient.class);
        if (LogSupport.isDebugEnabled(ctx))
        {
            if (hzClient == null)
                new DebugLogMsg(this, "hzClient is null", null).log(ctx);
            else
            {
                new DebugLogMsg(this, "hzClient is not null", null).log(ctx);
            }
        }
        //try creating a subscriber
        hzClient.createSubscriberHomezone("919326035308", infoArr);
        hzClient.modifySubscriberHomezone(info4);
        
    }
}
