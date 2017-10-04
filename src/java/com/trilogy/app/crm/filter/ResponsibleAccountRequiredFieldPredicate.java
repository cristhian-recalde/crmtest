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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.account.BANAware;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Account predicate that returns true if the account is in the configured responsible.
 *
 * @author Marcio Marques
 * @since 8.6
 */
public class ResponsibleAccountRequiredFieldPredicate extends AbstractResponsibleAccountRequiredFieldPredicate
{

    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof Account)
        {
            return ((Account) obj).isResponsible() == isResponsible();
        }
        else if (obj instanceof BANAware)
        {
            Account account = null;
            String ban = ((BANAware)obj).getBAN();
            try
            {
                account = AccountSupport.getAccount(ctx, ban);
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error retrieving account " + ban, e).log(ctx);
            }
            if (account != null)
            {
                return account.isResponsible() == isResponsible();
            }
        }
        else if (ctx.has(Account.class))
        {
            return ((Account) ctx.get(Account.class)).isResponsible() == isResponsible();
        }
        return false;
    }

}
