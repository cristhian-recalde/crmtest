/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.account;

import java.sql.SQLException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.filter.SimpleDeepClone;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.xdb.XPreparedStatement;
import com.trilogy.framework.xhome.xdb.XStatement;


public class NewAttachmentWebActionPredicate extends SimpleDeepClone implements Predicate
{

    /**
     * author: Simar Singh.
     * A predicate to restrict max number of attachments per account
     */
    private static final long serialVersionUID = 1L;


    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        Account account = (Account) ctx.get(Account.class);
        if (account == null)
        {
            // no account in context; don't attachment will be for which accoutn
            return false;
        }
        try
        {
            if (AccountAttachmentSupport.getAccountMangement(ctx).getMaxNumberOfAttachments() <= AccountAttachmentSupport
                    .getNumberOfAttachmentsForBan(ctx, account.getBAN()))
            {
                // limt on max number of attachments reached
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    
}