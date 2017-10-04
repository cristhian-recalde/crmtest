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

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Home responsible to clear debt collection agencies when account leaves in collection state.
 *
 * @author Marcio Marques
 *
 */
public class AccountClearDebtCollectionAgencyHome  extends HomeProxy
{
    /**
     * for serialization.
     */
    private static final long serialVersionUID = 1L;

    public AccountClearDebtCollectionAgencyHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Account account = (Account) obj;
        if (!account.isInCollection() || !account.isResponsible() || account.isPrepaid())
        {
            account.setDebtCollectionAgencyId(Account.DEFAULT_DEBTCOLLECTIONAGENCYID);
        }

        return super.store(ctx, account);
    }}
