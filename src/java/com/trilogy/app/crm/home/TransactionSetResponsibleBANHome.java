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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * @author victor.stratan@redknee.com
 */
public class TransactionSetResponsibleBANHome extends HomeProxy
{
    private static final long serialVersionUID = 1L;


    public TransactionSetResponsibleBANHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Transaction trans = (Transaction) obj;

        if (trans == null)
        {
            throw new HomeException("Cannot create Tranasction.  Object is null");
        }

        if (trans.getResponsibleBAN() == null || trans.getResponsibleBAN().length() == 0)
        {
            Account account = (Account) ctx.get(Account.class);
            if (account == null || account.getBAN() == null
                || !SafetyUtil.safeEquals(account.getBAN().trim(), trans.getBAN().trim()))
            {
                account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, trans.getBAN());
            }

            if (account != null)
			{
				if (account.isResponsible())
				{
					trans.setResponsibleBAN(account.getBAN());
				}
				else
				{
					trans.setResponsibleBAN(account.getResponsibleBAN());
				}
			}
        }

        return super.create(ctx, trans);
    }
}
