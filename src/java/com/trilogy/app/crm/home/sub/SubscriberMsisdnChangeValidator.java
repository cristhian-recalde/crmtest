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
package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;


/**
 * This class determines whether or not a MSISDN change is allowed or not.
 * 
 * As of CRM 7.5, this is not supported for group leaders of prepaid group pooled accounts. 
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class SubscriberMsisdnChangeValidator implements Validator
{

    private static SubscriberMsisdnChangeValidator instance_;
    public static SubscriberMsisdnChangeValidator instance()
    {
        if( instance_ == null )
        {
            instance_ = new SubscriberMsisdnChangeValidator();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        final HomeOperationEnum operation = (HomeOperationEnum) ctx.get(HomeOperationEnum.class);
        if( obj instanceof Subscriber
                && SafetyUtil.safeEquals(operation, HomeOperationEnum.STORE) )
        {
            Subscriber newSub = (Subscriber)obj;
            Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLDSUBSCRIBER);
            if( oldSub != null )
            {
                if( oldSub.isPrepaid()
                        && !SafetyUtil.safeEquals(newSub.getMSISDN(), oldSub.getMSISDN()) )
                {
                    if (oldSub.isPooledGroupLeader(ctx))
                    {
                        // This is a group leader of a prepaid group pooled account, and the MSISDN changed
                        // OID 36174 - groupMSISDN's can not change their own MSISDN (not supported)
                        throw new IllegalPropertyArgumentException(
                                SubscriberXInfo.MSISDN,
                                "Prepaid group leaders can not change their own MSISDN.");
                    }
                }
            }
            else
            {
                throw new IllegalStateException("Error looking up original subscriber profile for MSISDN change validation.");
            }
        }
    }
}
