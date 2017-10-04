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
package com.trilogy.app.crm.move.request.factory;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.extension.account.MoveAccountExtensionHolder;
import com.trilogy.app.crm.move.MoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountExtensionMoveRequestFactory
{

    static MoveRequest getInstance(Context ctx, Account account)
    {
        AccountExtensionMoveRequest request = null;

        if (account != null
                && account.getAccountExtensions() != null
                && account.getAccountExtensions().size() > 0)
        {   
            request = new AccountExtensionMoveRequest();
            
            request.setExistingBAN(account);
            
            // Add a copy of each extension to the move request
            List<ExtensionHolder> accountExtensions = request.getOriginalAccount(ctx).getAccountExtensions();
            for (ExtensionHolder extensionHolder : accountExtensions)
            {
                if (extensionHolder != null && extensionHolder.getExtension() != null)
                {
                    addExtensionToRequest(request, extensionHolder.getExtension());
                }
            }
        }
        
        return request;
    }

    static MoveRequest getInstance(Context ctx, AccountExtension extension)
    {
        AccountExtensionMoveRequest request = null;

        if (extension != null)
        {
            request = new AccountExtensionMoveRequest();
            
            request.setExistingBAN(extension.getBAN());
            addExtensionToRequest(request, extension);
        }
        
        return request;
    }

    private static void addExtensionToRequest(AccountExtensionMoveRequest request, Extension extension)
    {
        ExtensionHolder newHolder = new MoveAccountExtensionHolder();
        newHolder.setExtension(extension);
        
        List<ExtensionHolder> extensions = request.getAccountExtensions();
        if (extensions == null)
        {
            extensions = new ArrayList<ExtensionHolder>();
        }
        else
        {
            extensions = new ArrayList<ExtensionHolder>(extensions);
        }
        
        extensions.add(newHolder);

        // Trigger property change
        request.setAccountExtensions(extensions);
    }
}
