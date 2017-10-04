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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.AbstractAccount;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.bank.Bank;
import com.trilogy.app.crm.bean.bank.BankHome;
import com.trilogy.app.crm.bean.bank.BankXInfo;

/**
 * Validates that the Bank id corresponds to an existing Bank.
 * @author victor.stratan@redknee.com
 */
public final class AccountBankValidator implements Validator
{
    private static final AccountBankValidator INSTANCE = new AccountBankValidator();

    private static final String BANK_NOT_FOUND = "Bank not found.";
    private static final String BANK_NOT_FOUND_KEY = "BANK_NOT_FOUND";
    private static final String BANK_ERROR = "Error while retreiving Bank.";
    private static final String BANK_ERROR_KEY = "BANK_ERROR";

    private static final Map DEFAULTS = new HashMap();
    static
    {
        DEFAULTS.put(BANK_NOT_FOUND_KEY, BANK_NOT_FOUND);
        DEFAULTS.put(BANK_ERROR_KEY, BANK_ERROR);
    }

    private AccountBankValidator()
    {
    }

    public static AccountBankValidator instance()
    {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException
    {
        final Account account = (Account) obj;

        final Home bankHome = (Home) ctx.get(BankHome.class);
        final String bankId = account.getBankID();

        if (AbstractAccount.DEFAULT_BANKID.equals(bankId))
        {
            // bank not selected, nothing to do
            return;
        }

        try
        {
            if (HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
            {
                // if store check if the value did not change, no validation needed.
                final Account oldAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
                if (oldAccount.getBankID().equals(bankId))
                {
                    return;
                }
            }

            final EQ condition = new EQ(BankXInfo.BANK_ID, bankId);
            final Bank msisdnGroup = (Bank) bankHome.find(ctx, condition);
            if (msisdnGroup == null)
            {
                throwError(ctx, AccountXInfo.BANK_ID, BANK_NOT_FOUND_KEY);
            }
        }
        catch (HomeException e)
        {
            throwError(ctx, AccountXInfo.BANK_ID, BANK_ERROR_KEY, e);
        }
    }

    private void throwError(final Context ctx, final PropertyInfo property, final String key)
    {
        throwError(ctx, property, key, null);
    }

    private void throwError(final Context ctx, final PropertyInfo property, final String key, final Exception cause)
    {
        final String msg = getErrorMessage(ctx, key);
        final CompoundIllegalStateException compoundException = new CompoundIllegalStateException();
        final IllegalPropertyArgumentException propertyException = new IllegalPropertyArgumentException(property, msg);
        if (cause != null)
        {
            propertyException.initCause(cause);
        }
        compoundException.thrown(propertyException);
        compoundException.throwAll();
    }

    private String getErrorMessage(final Context ctx, final String key)
    {
        final MessageMgr mgr = new MessageMgr(ctx, this);
        return mgr.get(key, (String) DEFAULTS.get(key));
    }
}
