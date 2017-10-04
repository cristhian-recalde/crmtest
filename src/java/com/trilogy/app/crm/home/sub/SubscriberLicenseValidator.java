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

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is meant to reject any subscribers that have an unlicensed configuration
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class SubscriberLicenseValidator implements Validator
{
    private static Validator instance_;

    /**
     * Returns an instance of <code>SubscriberDatesValidator</code>.
     *
     * @return An instance of <code>SubscriberDatesValidator</code>.
     */
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SubscriberLicenseValidator();
        }
        return instance_;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.beans.Validator#validate(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public void validate(Context ctx, Object bean) throws IllegalStateException
    {
        if( bean instanceof Subscriber )
        {
            Subscriber sub = (Subscriber) bean;
            
            if( sub.isPostpaid()
                    && !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.POSTPAID_LICENSE_KEY) )
            {
                throw new IllegalPropertyArgumentException(
                        SubscriberXInfo.SUBSCRIBER_TYPE, 
                        "No license for postpaid subscribers.");
            }
            
            if( sub.isPrepaid() )
            {
                if( !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_LICENSE_KEY) )
                {
                    throw new IllegalPropertyArgumentException(
                            SubscriberXInfo.SUBSCRIBER_TYPE, 
                            "No license for prepaid subscribers.");
                }
                if( !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.PREPAID_GROUP_POOLED_LICENSE_KEY) )
                {
                    Account account = (Account) ctx.get(Lookup.ACCOUNT);
                    if( sub.isPooled(ctx)
                            && account.isPrepaid() )
                    {
                        throw new IllegalPropertyArgumentException(
                                SubscriberXInfo.SUBSCRIBER_TYPE,
                                "No license for prepaid group-pooled subscribers.");
                    }
                }
            }
            
            if( sub.getQuotaLimit() != Subscriber.DEFAULT_QUOTALIMIT
                    && !LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.GROUP_MEMBER_QUOTA_LIMITS_KEY) )
            {
                throw new IllegalPropertyArgumentException(
                        SubscriberXInfo.QUOTA_LIMIT, 
                        "No license for subscriber group/member quota limits.");
            }
        }
    }
}
