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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.Lookup;

/**
 * This class resets the owner MSISDN (to the group MSISDN) for group-pooled
 * accounts if the subscriber of the owner MSISDN is no longer in valid state.
 *
 * @author jimmy.ng@redknee.com
 */
public class PooledGroupAccountOwnerMsisdnResettingHome extends HomeProxy
{
    /**
     * Creates a new PooledGroupAccountOwnerMsisdnResettingHome for the
     * given home.
     *
     * @param delegate The Home to which this object delegates.
     */
    public PooledGroupAccountOwnerMsisdnResettingHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        final Subscriber newSubscriber = (Subscriber) obj;
        final Subscriber oldSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        Object ret=super.store(ctx,newSubscriber);
        
        if (oldSubscriber.getState() == SubscriberStateEnum.ACTIVE
        && (newSubscriber.getState() == SubscriberStateEnum.SUSPENDED
         || newSubscriber.getState() == SubscriberStateEnum.INACTIVE))
        {
            final Account account =(Account) ctx.get(Account.class);
            
			if (account.isPooled(ctx)
                    && account.getOwnerMSISDN().equals(newSubscriber.getMSISDN()))
            {
                final Home accountHome = (Home) ctx.get(AccountHome.class);

                // setting the Owner MSISDN to the Pool MSISDN will remove the Owner MSISDN assosiation
                // setting the Owner MSISDN to blank will stop SMS from being sent
                account.setOwnerMSISDN(account.getPoolMSISDN());
                accountHome.store(ctx,account);
            }
        }
        
        return ret;
    }
}
