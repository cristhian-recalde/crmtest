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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;

/**
 * Updates read only field Responsible BAN based on responsible, BAN and parentBAN fields.
 *
 * @author victor.stratan@redknee.com
 */
public class AccountSetResponsibleBANOnCreateHome extends HomeProxy
{
    /**
     * for serialization.
     */
    private static final long serialVersionUID = 1L;

    public AccountSetResponsibleBANOnCreateHome(final Home delegate)
    {
        super(delegate);
    }

    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) obj;
        
        //
        Account parent = account.getParentAccount(ctx);
        if (parent !=null && account.isIndividual(ctx) && parent.isPooled(ctx))
        {
            account.setResponsible(false);
        }
        
        if (account.getResponsibleBAN() == null
                || account.getResponsibleBAN().length() == 0)
        {
            account.setResponsibleBAN(account.computeResponsibleBAN(ctx));
        }

        return super.create(ctx, account);
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) obj;
        
        if (account.getResponsibleBAN() == null
                || account.getResponsibleBAN().length() == 0)
        {
            account.setResponsibleBAN(account.computeResponsibleBAN(ctx));
        }

        return super.store(ctx, account);
    }
}
