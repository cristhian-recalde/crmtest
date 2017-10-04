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
package com.trilogy.app.crm.transfer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.transfer.OwnerFilteredContractGroupKeyWebControl.OwnerExtractor;


/**
 * Provides an OwnerExtractor that uses the Account in the Context to extract an
 * owner value.
 *
 * @author gary.anderson@redknee.com
 */
public class AccountInContextOwnerExtractor
    implements OwnerExtractor
{
    /**
     * Gets a reusable instance of AccountInContextOwnerExtractor.
     *
     * @return A reusable instance of AccountInContextOwnerExtractor.
     */
    public static AccountInContextOwnerExtractor instance()
    {
        return INSTANCE;
    }


    /**
     * Prevents unintended instantiation except by subclasses.
     */
    protected AccountInContextOwnerExtractor()
    {
        // Empty
    }


    /**
     * {@inheritDoc}
     */
    public String getOwner(final Context context)
    {
        final Account account = (Account)context.get(Account.class);
        final String ownerID;

        if (account != null)
        {
            ownerID = account.getBAN();
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "No account found in context.", null).log(context);
            }

            ownerID = "";
        }

        return ownerID;
    }


    /**
     * A reusable instance of this AccountInContextOwnerExtractor.
     */
    private static final AccountInContextOwnerExtractor INSTANCE = new AccountInContextOwnerExtractor();
}
