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
package com.trilogy.app.crm.home.account;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.LastModifiedAwareHome;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;


/**
 * This home updates all modification related dates associated with the bean on create/store.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public class AccountLastModifiedHome extends LastModifiedAwareHome implements Home
{
    public AccountLastModifiedHome(Home delegate)
    {
        super(delegate);
    }


    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException 
    {
        if (obj instanceof Account)
        {
            Account account = (Account) obj;
            account.setLastStateChangeDate(new Date());
        }

        return super.create(ctx, obj);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {

        Account oldAcct =  (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        Account account = (Account) obj;
        if(oldAcct.getState().getIndex() != account.getState().getIndex()
               && account.getState().getIndex() == AccountStateEnum.INACTIVE_INDEX)
        {
            account.setLastStateChangeDate(new Date());
        }
        
        return super.store(ctx, account);
    }
}
