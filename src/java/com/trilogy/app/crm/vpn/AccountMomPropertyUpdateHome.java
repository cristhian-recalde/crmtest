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
package com.trilogy.app.crm.vpn;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;


/**
 * Updates account's VPN and ICM flags to false
 * if the account type is non-MOM
 * 
 * @author danny.ng@redknee.com
 * @created Mar 20, 2006
 */
public class AccountMomPropertyUpdateHome extends HomeProxy
{

    /**
     * Generated UID
     */
    private static final long serialVersionUID = -1169467503175500788L;


    public AccountMomPropertyUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    public Object create(Context ctx, Object bean) throws HomeException
    {
        Account acct = (Account) bean;
        updateMomProperties(ctx, acct);
        return super.create(ctx, bean);
    }
    
    
    
    public Object store(Context ctx, Object bean) throws HomeException
    {
        Account acct = (Account) bean;
        updateMomProperties(ctx, acct);
        return super.store(ctx, bean);
    }
    
    
    public void remove(Context ctx, Object bean) throws HomeException
    {
        Account acct = (Account) bean;
        updateMomProperties(ctx, acct);
        super.remove(ctx, bean);
    }
    
    
    /**
     * Sets the Account's VPN and ICM flag to false if <code>account</code>
     * is not of a MOM account type
     * @param ctx
     * @param account
     */
    private static void updateMomProperties(Context ctx, Account account)
    {
        if (!account.isMom(ctx))
        {
            account.setVpn(false);
            account.setIcm(false);
        }
    }

}
