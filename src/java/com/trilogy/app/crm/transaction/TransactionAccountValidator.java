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

package com.trilogy.app.crm.transaction;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.support.AccountSupport;


/**
 * Validates the account BAN specified in a transaction is correct.
 *
 * @author cindy.wong@redknee.com
 * @since 17-Jan-08
 */
public class TransactionAccountValidator implements Validator
{

    /**
     * Create a new instance of <code>TransactionAccountValidator</code>.
     */
    protected TransactionAccountValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>TransactionAccountValidator</code>.
     *
     * @return An instance of <code>TransactionAccountValidator</code>.
     */
    public static TransactionAccountValidator instance()
    {
        if (instance == null)
        {
            instance = new TransactionAccountValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context context, final Object object) throws IllegalStateException
    {
        final CompoundIllegalStateException el = new CompoundIllegalStateException();
        if (object == null || !(object instanceof Transaction))
        {
            el.thrown(new IllegalStateException("System Error: transaction is must be supplied to this validator"));
        }
        else
        {
            final Transaction transaction = (Transaction) object;
            final String ban = transaction.getBAN();
            if (ban == null || ban.trim().length() == 0)
            {
                el
                    .thrown(new IllegalPropertyArgumentException(TransactionXInfo.BAN,
                        "Account number must be specified"));
            }
            else
            {
                Account account = (Account) context.get(Account.class);
                if (account == null || account.getBAN() == null
                    || !SafetyUtil.safeEquals(account.getBAN().trim(), ban.trim()))
                {
                    // only attempt to look up account in DB if it's not in context.
                    try
                    {
                        account = AccountSupport.getAccount(context, transaction.getBAN());
                        if (account == null)
                        {
                            el.thrown(new IllegalPropertyArgumentException(TransactionXInfo.BAN,
                                "No such account exists"));
                        }
                    }
                    catch (final HomeException exception)
                    {
                        if (LogSupport.isDebugEnabled(context))
                        {
                            final StringBuilder sb = new StringBuilder();
                            sb.append(exception.getClass().getSimpleName());
                            sb.append(" caught in ");
                            sb.append("TransactionAccountValidator.validate(): ");
                            if (exception.getMessage() != null)
                            {
                                sb.append(exception.getMessage());
                            }
                            LogSupport.debug(context, this, sb.toString(), exception);
                            el.thrown(exception);
                        }

                    }
                }
            }
        }
        el.throwAll();
    }

    /**
     * Singleton instance.
     */
    private static TransactionAccountValidator instance;
}
