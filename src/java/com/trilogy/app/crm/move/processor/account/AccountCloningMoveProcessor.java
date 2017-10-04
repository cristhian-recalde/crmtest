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
package com.trilogy.app.crm.move.processor.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.home.ContextFallbackFindHome;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.CopyMoveProcessor;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.NullCopyMoveStrategy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;


/**
 * This processor is responsible for cloning the old account and initializing
 * a new account during the setup.  It uses the provided copy strategy to execute
 * move logic & validation.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountCloningMoveProcessor<AMR extends AccountMoveRequest> extends CopyMoveProcessor<AMR>
{
    public AccountCloningMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate, NullCopyMoveStrategy.instance());
    }


    public AccountCloningMoveProcessor(MoveProcessor<AMR> delegate, CopyMoveStrategy<AMR> strategy)
    {
        super(delegate, strategy);
    }


    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        AMR request = this.getRequest();

        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);

        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }
    

    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {   
        AMR request = getRequest();
        
        Account account = request.getOldAccount(ctx);
        Account newAccount = null;
        if (account != null)
        {
            try
            {
                new DebugLogMsg(this, "Cloning account " + account.getBAN() + " in order to move it while retaining the old copy...", null).log(ctx);
                newAccount = (Account) account.deepClone();
            }
            catch (final CloneNotSupportedException exception)
            {
                throw new MoveException(request, "Unable to clone account " + request.getExistingBAN(), exception);
            }
            
            if (newAccount != null)
            {
                // Lazy-load everything that needs to be moved with the account before setting the new BAN.
                lazyLoadPropertiesToMove(ctx, newAccount);
                newAccount.setBAN(request.getNewBAN());
                
                //Responsible Ban will be set in home pipeline
                newAccount.setResponsibleBAN(null);
                newAccount.setNextSubscriberId(1);
                request.setNewBAN(newAccount);
                new DebugLogMsg(this, "Account " + account.getBAN() + " cloned and initialized successfully.", null).log(ctx);
            }
        }
        
        Context moveCtx = super.setUp(ctx);
        
        if (newAccount != null)
        {
            // Give the illusion to the rest of CRM that this account already exists
            Object id = newAccount.ID();
            moveCtx.put(id.getClass().getName() + "_" + id, newAccount);
            
            Home accountHome = (Home) moveCtx.get(AccountHome.class);
            if (!(accountHome instanceof HomeProxy
                    && ((HomeProxy)accountHome).hasDecorator(ContextFallbackFindHome.class)))
            {
                accountHome = new ContextFallbackFindHome(moveCtx, accountHome);
                moveCtx.put(AccountHome.class, accountHome);
                new InfoLogMsg(this, "Installed home to retrieve transient copies of accounts involved in move requests.", null).log(ctx);
            }
        }
        
        return moveCtx;
    }


    private void lazyLoadPropertiesToMove(Context ctx, Account newAccount)
    {
        AMR request = getRequest();

        if (!newAccount.isContactsLoaded())
        {
            // DATE_OF_BIRTH will also lazy-load all other contact info.
            newAccount.lazyLoad(ctx, AccountXInfo.DATE_OF_BIRTH);   
        }

        Account originalAccount = request.getOriginalAccount(ctx);
        if (originalAccount == null)
        {
            originalAccount = request.getOldAccount(ctx);
        }
        if (originalAccount != null)
        {
            newAccount.setAccountExtensions(originalAccount.getAccountExtensions());
        }
        else
        {
            newAccount.lazyLoad(ctx, AccountXInfo.ACCOUNT_EXTENSIONS);
        }
    }
}
