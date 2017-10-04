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
package com.trilogy.app.crm.home.sub.conversion;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * Has all the shared methods for conversion.
 *
 * @author arturo.medina@redknee.com
 */
public abstract class AbstractSubscriberConversion implements SubscriberConversion
{

    /**
     * @param ctx
     * @param sub
     * @return
     * @throws HomeException
     */
    protected Account getAccount(final Context ctx, final Subscriber sub) throws HomeException
    {
        Account acct = (Account) ctx.get(Account.class);
        if (acct == null)
        {
            acct = sub.getAccount(ctx);
        }
        return acct;
    }

    /**
     * Executes the Subscriber conversion.
     *
     * @param ctx
     * @param prevSubscriber
     * @param currentSubscriber
     * @param delegate
     * @return
     */
    public Subscriber convertSubscriber(final Context ctx, final Subscriber prevSubscriber,
            final Subscriber currentSubscriber, final Home delegate) throws HomeException
    {
        Subscriber sub = null;
        if (from_ == prevSubscriber.getSubscriberType() && to_ == currentSubscriber.getSubscriberType())
        {
            try
            {
                if (from_ == SubscriberTypeEnum.PREPAID)
                {
                    ctx.put(Common.PREPAID_POSTPAID_CONVERSION_SUBCRIBER, currentSubscriber);
                }
                else
                {
                    ctx.put(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER, currentSubscriber);
                }
                validate(ctx, prevSubscriber);
                sub = executeConvertion(ctx, prevSubscriber, currentSubscriber, delegate);
            }
            catch (IllegalStateException e)
            {
                throw new HomeException(e.getMessage());
            }
        }
        return sub;
    }

    public abstract Subscriber executeConvertion(final Context ctx, final Subscriber prevSubscriber,
            final Subscriber currentSubscriber, final Home delegate) throws HomeException;

    //Attributes
    protected SubscriberTypeEnum from_;
    protected SubscriberTypeEnum to_;

}
