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

package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

public final class ParentAccountTypeValidator implements Validator
{
    protected static final ParentAccountTypeValidator INSTANCE = new ParentAccountTypeValidator();

    /**
     * Prevents instantiation
     */
    private ParentAccountTypeValidator()
    {
    }

    public static ParentAccountTypeValidator instance()
    {
        return INSTANCE;
    }

    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
         // TODO 2007-09-18 create exception only if needed
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        final Subscriber sub = (Subscriber) obj;
        final Account account = (Account) ctx.get(Lookup.ACCOUNT);

        if (account == null)
        {
            el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.BAN,
                    "Can't get account " + sub.getBAN() + " for subscriber " + sub.getId()));
        }
        else
        {
            if (SubscriberTypeEnum.PREPAID.equals(account.getSystemType()))
            {
                if (!sub.isPrepaid())
                {
                    el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_TYPE,
                            "Non-prepaid subscriber cannot exist in prepaid account"));
                }
            }
            else if (SubscriberTypeEnum.POSTPAID.equals(account.getSystemType()))
            {
                if (!sub.isPostpaid())
                {
                    el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_TYPE,
                            "Non-postpaid subscriber cannot exist in postpaid account"));
                }
            }
            else if (SubscriberTypeEnum.HYBRID.equals(account.getSystemType()) )
            {
                if( account.isPooled(ctx) && !sub.isPostpaid() )
                {
                    /*
                     * CRM HLD OID 36148:
                     * For Account Type where pooled = true, when Billing Type = Postpaid or Hybrid,
                     * the group leader can only be postpaid with postpaid & prepaid members.
                     */
                    if ( sub.isPooledGroupLeader(ctx) || !sub.isPooled(ctx) )
                    {
                        // This non-postpaid subscriber is one of:
                        // a) group leader of a hybrid group-pooled account
                        // b) first subscriber in a hybrid group-pooled account (which hasn't had its MSISDN set yet)
                        el.thrown(new IllegalPropertyArgumentException(SubscriberXInfo.SUBSCRIBER_TYPE,
                                "Group leader in hybrid pooled account must be postpaid"));
                    }
                }
            }
        }

        el.throwAll();
    }
}
