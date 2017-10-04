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
package com.trilogy.app.crm.move.support;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequestXInfo;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class AccountMoveValidationSupport
{
    public static Account validateOldAccountExists(Context ctx, AccountMoveRequest request, ExceptionListener el)
    {
        Account oldAccount = request.getOldAccount(ctx);
        Account originalAccount = request.getOriginalAccount(ctx);
        if (oldAccount == null || originalAccount == null)
        {
            el.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.EXISTING_BAN, 
                    "Account (BAN=" + request.getExistingBAN() + ") does not exist."));
        }
        return originalAccount;
    }
    
    public static Account validateNewAccountExists(Context ctx, AccountMoveRequest request, ExceptionListener el)
    {
        Account newAccount = request.getNewAccount(ctx);
        if (newAccount == null)
        {
            final String infoString;
            if (request.getNewBAN() == null || request.getNewBAN().startsWith(MoveConstants.DEFAULT_MOVE_PREFIX))
            {
                infoString = "copy of " + request.getExistingBAN(); 
            }
            else
            {
                infoString = "BAN=" + request.getNewBAN();
            }
            el.thrown(new IllegalPropertyArgumentException(
                    AccountMoveRequestXInfo.NEW_BAN, 
                    "New account (" + infoString + ") does not exist."));
        }
        return newAccount;
    }
    
    public static Account validateNewParentAccountExists(Context ctx, AccountMoveRequest request, ExceptionListener el)
    {
        Account newParentAccount = request.getNewParentAccount(ctx);
        if (newParentAccount == null)
        {
            String newParentBAN = request.getNewParentBAN();
            if (newParentBAN != null && newParentBAN.length() > 0)
            {
                el.thrown(new IllegalPropertyArgumentException(
                        AccountMoveRequestXInfo.NEW_PARENT_BAN, 
                        "New parent account (BAN=" + request.getNewParentBAN() + ") does not exist."));   
            }
        }
        return newParentAccount;
    }
}
