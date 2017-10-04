/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.factory;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.DependencyMoveProcessor;
import com.trilogy.app.crm.move.processor.ReadOnlyMoveProcessor;
import com.trilogy.app.crm.move.processor.extension.MovableExtensionMoveProcessor;
import com.trilogy.app.crm.move.processor.extension.account.AccountExtensionCreationMoveProcessor;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;


/**
 * Creates an appropriate instance of an AccountMoveRequest processor.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class AccountExtensionMoveProcessorFactory
{
    static <AEMR extends AccountExtensionMoveRequest> MoveProcessor<AEMR> getNewInstance(Context ctx, AEMR request)
    {
        boolean validationError = false;
        
        Account account = request.getOriginalAccount(ctx);
        if (account == null)
        {
            new InfoLogMsg(AccountExtensionMoveProcessorFactory.class, 
                    "Account with BAN " + request.getExistingBAN() + " does not exist.", null).log(ctx);
            validationError = true;
        }
        
        Account newAccount = request.getNewAccount(ctx);
        if (newAccount == null
                || !newAccount.isBANSet())
        {
            new InfoLogMsg(AccountExtensionMoveProcessorFactory.class, 
                    "New BAN not set properly.  This move request can be validated but not executed.", null).log(ctx);
            validationError = true;
        }

        // Create move processor to store the request
        MoveProcessor<AEMR> processor = new DependencyMoveProcessor<AEMR>(request);

        // Add processors that perform actual move logic
        processor = new MovableExtensionMoveProcessor<AEMR>(processor);
        
        if (!SafetyUtil.safeEquals(request.getExistingBAN(), request.getNewBAN()))
        {
            // If the extension is changing accounts, we need to create a new entry in the extension's home.
            processor = new AccountExtensionCreationMoveProcessor<AEMR>(processor);
        }
        
        if (validationError)
        {
            new InfoLogMsg(AccountExtensionMoveProcessorFactory.class, 
                    "Error occurred while creating a move processor for request " + request
                    + ".  Returning a read-only move processor so that validation can be run.", null).log(ctx);
            processor = new ReadOnlyMoveProcessor<AEMR>(
                    processor,
                    "Error occurred while creating a move processor for request " + request.toString());
        }
        
        return processor;
    }
}
