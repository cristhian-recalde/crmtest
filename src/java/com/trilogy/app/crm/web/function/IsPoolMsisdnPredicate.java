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
package com.trilogy.app.crm.web.function;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.util.partitioning.xhome.support.MsisdnAware;

/**
 * Determines if the MSISDN is different from Pooled Group MSISDN of the current account.
 *
 * @author victor.stratan@redknee.com
 */
public class IsPoolMsisdnPredicate implements Predicate
{
    public IsPoolMsisdnPredicate()
    {
    }

    /**
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object obj)
    {
        String msisdn = null;
        if (obj instanceof MsisdnOwnership)
        {
            final MsisdnOwnership msisdnOwnership = (MsisdnOwnership) obj;
            msisdn = msisdnOwnership.getMsisdn();
        }
        else if (obj instanceof MsisdnAware)
        {
            final MsisdnAware msisdnAware = (MsisdnAware) obj;
            msisdn = msisdnAware.getMsisdn();
        }
        return !isPooledSubscription(ctx, msisdn);
    }


    private boolean isPooledSubscription(final Context ctx, final String msisdn)
    {
        boolean result = false;
        if (msisdn != null)
        {
            final Account account = (Account) ctx.get(Account.class);
            if (account != null)
            {
                result = msisdn.equals(account.getPoolMSISDN());
            }

            if (!result)
            {
                try
                {
                    Msisdn poolMsisdn = MsisdnSupport.getMsisdn(ctx, msisdn);
                    if (poolMsisdn != null && poolMsisdn.getSubscriberType() == SubscriberTypeEnum.HYBRID)
                    {
                        result = SpidSupport.isGroupPooledMsisdnGroup(ctx, poolMsisdn.getSpid(), poolMsisdn.getGroup());
                    }
                }
                catch (Exception ex)
                {
                    result = false;
                }
            }
        }
        return result;
    }
}