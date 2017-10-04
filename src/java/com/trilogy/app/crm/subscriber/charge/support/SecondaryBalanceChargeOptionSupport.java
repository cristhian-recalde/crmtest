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
package com.trilogy.app.crm.subscriber.charge.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SpidSupport;

/**
 * 
 *
 * @author chandrachud.ingale
 * @since 
 */
public class SecondaryBalanceChargeOptionSupport
{
	
	public static final String SECONDARY_SUBSCRIPTION_TYPE_VALUE = "SecondarySubscriptionTypeValue";
    private static String CLASS_NAME_FOR_LOGGING = SecondaryBalanceChargeOptionSupport.class.getName();
    
    /**
     * @param ctx
     * @param subscriber
     * @param fee
     */
    public static void setSecondaryBalanceChargeOption(Context ctx, Subscriber subscriber) throws HomeInternalException, HomeException
    {
        if(!subscriber.isGroupOrGroupPooledMemberSubscriber(ctx))
        {
            return;
        }
        
        int secondarySubscriptionType = getSecondarySubscriptionType(ctx, subscriber.getSpid());
        if(secondarySubscriptionType == 0)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(CLASS_NAME_FOR_LOGGING, "Returning no SecondarySubscriptionType defined for SPID : " + subscriber.getSpid()).log(ctx);
            }
        }
        else
        {
        	Context context = (Context) ctx.get("app");
        	context.put(SECONDARY_SUBSCRIPTION_TYPE_VALUE, secondarySubscriptionType);
        }
        
    }

    
    /**
     * @param ctx
     * @param spid
     * @return
     * @throws HomeException
     * @throws HomeInternalException
     */
    private static int getSecondarySubscriptionType(Context ctx, int spid)
            throws HomeException, HomeInternalException
    {
    	int secondarySubscriptionType = 0;
        CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
        if (crmSpid != null)
        {
        	secondarySubscriptionType = crmSpid.getSecondarySubscriptionType();
        }
        return secondarySubscriptionType;
    }

}
