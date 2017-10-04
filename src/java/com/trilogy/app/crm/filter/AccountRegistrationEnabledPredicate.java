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
package com.trilogy.app.crm.filter;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.RegistrationStatusEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Returns true if the given account (or account referenced by BANAware object)
 * satisfies any criteria for account registration.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class AccountRegistrationEnabledPredicate implements Predicate
{
    public AccountRegistrationEnabledPredicate()
    {
        this(false);
    }
    
    public AccountRegistrationEnabledPredicate(boolean ignoreAccountRegistrationStatus)
    {
        ignoreStatus_ = ignoreAccountRegistrationStatus;
    }

    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        LicenseMgr lMgr = (LicenseMgr) ctx.get(LicenseMgr.class);
        if (lMgr != null && lMgr.isLicensed(ctx, LicenseConstants.ACCOUNT_REGISTRATION))
        {
            Account account = getAccount(ctx, obj);
            
            if (account != null)
            {
                return (ignoreStatus_ 
                        || account.getRegistrationStatus() != RegistrationStatusEnum.NOT_APPLICABLE_INDEX) 
                        && account.isRegistrationRequired(ctx);
            }
            else
            {
                Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
                if (sub != null)
                {
                    return f(ctx.createSubContext().put(Subscriber.class, null), sub);
                }
            }
        }
        
        return false;
    }

    protected Account getAccount(Context ctx, Object obj)
    {
        Account account = null;
        if (obj instanceof Account)
        {
            account = (Account) obj;
        }
        else if (obj instanceof BANAware)
        {
            String ban = ((BANAware) obj).getBAN();
            try
            {
                account = AccountSupport.getAccount(ctx, ban);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Unable to retrieve account " + ban, e).log(ctx);
            }
        }

        if (account == null)
        {
            account = (Account) ctx.get(Account.class);
        }
        return account;
    }

    private boolean ignoreStatus_;
}
