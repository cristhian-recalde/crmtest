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
package com.trilogy.app.crm.move.processor.extension.account;

import java.util.Collection;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.MovableExtension;
import com.trilogy.app.crm.extension.account.AccountExtension;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequest;
import com.trilogy.app.crm.move.request.AccountExtensionMoveRequestXInfo;
import com.trilogy.app.crm.support.ExtensionSupportHelper;


/**
 * This processor is responsible for setting the 'move in progress' flag
 * during the setup to prevent the ExtensionInstallationHome from executing
 * the install logic.  During the move it is responsible for creating a
 * copy of the account extension referencing the new account's BAN via Home
 * operations.  It does not permanently modify the account extensions in
 * the request.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountExtensionCreationMoveProcessor<AEMR extends AccountExtensionMoveRequest> extends MoveProcessorProxy<AEMR>
{
    public AccountExtensionCreationMoveProcessor(MoveProcessor<AEMR> delegate)
    {
        super(delegate);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);
        
        moveCtx.put(MovableExtension.MOVE_IN_PROGRESS_CTX_KEY, true);
        
        return moveCtx;
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        AEMR request = this.getRequest();
        
        Account oldAccount = request.getOriginalAccount(ctx);
        if (oldAccount == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountExtensionMoveRequestXInfo.EXISTING_BAN, 
                    "Account (BAN=" + request.getExistingBAN() + ") does not exist."));
        }
        else
        {
            Collection<Extension> extensions = request.getExtensions();
            if (extensions != null)
            {
                for (Extension ext : extensions)
                {
                    if (ext == null)
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                AccountExtensionMoveRequestXInfo.ACCOUNT_EXTENSIONS, 
                                "Extension list has a null extension in it."));
                    }
                    else if (!(ext instanceof AccountExtension))
                    {
                        cise.thrown(new IllegalPropertyArgumentException(
                                AccountExtensionMoveRequestXInfo.ACCOUNT_EXTENSIONS, 
                                "Extension list has an invalid extension type in it."));
                    }
                    else
                    {
                        AccountExtension extension = (AccountExtension) ext;
                        if(!SafetyUtil.safeEquals(extension.getBAN(), request.getExistingBAN()))
                        {
                            cise.thrown(new IllegalPropertyArgumentException(
                                    AccountExtensionMoveRequestXInfo.ACCOUNT_EXTENSIONS, 
                                    "Account (BAN=" + request.getExistingBAN() + ") does not match extension (BAN=" + extension.getBAN() + ")."));   
                        }
                    }
                }
            }
        }
        
        Account newAccount = request.getNewAccount(ctx);
        if (newAccount == null)
        {
            cise.thrown(new IllegalPropertyArgumentException(
                    AccountExtensionMoveRequestXInfo.NEW_BAN, 
                    "New account (BAN=" + request.getNewBAN() + ") does not exist."));
        }

        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        AEMR request = this.getRequest();

        Account oldAccount = request.getOriginalAccount(ctx);
        Account newAccount = request.getNewAccount(ctx);
        
        Collection<Extension> extensions = request.getExtensions();
        if (extensions != null)
        {
            for (Extension ext : extensions)
            {
                if (ext instanceof AccountExtension)
                {
                    AccountExtension extension = (AccountExtension) ext;
                    
                    // Set the new BAN/SPID before creating the extension for the new account.
                    extension.setBAN(newAccount.getBAN());
                    extension.setSpid(newAccount.getSpid());
                    try
                    {
                        Home extensionHome = ExtensionSupportHelper.get(ctx).getExtensionHome(ctx, extension);
                        if (extensionHome != null)
                        {
                            extensionHome.create(ctx, extension);
                        }
                        else
                        {
                            request.reportWarning(ctx, new MoveWarningException(request, "Unable to move unsupported extension of type: " + (extension != null ? extension.getClass().getName() : null)));
                        }
                    }
                    catch (HomeException e)
                    {
                        throw new MoveException(request, "Error occurred creating " + extension.getName(ctx) + " for account " + request.getNewBAN(), e);
                    }
                    finally
                    {
                        // Reset the extension BAN/SPID
                        extension.setBAN(oldAccount.getBAN());
                        extension.setSpid(oldAccount.getSpid());   
                    }
                }
            }
        }
        
        super.move(ctx);
    }

}
