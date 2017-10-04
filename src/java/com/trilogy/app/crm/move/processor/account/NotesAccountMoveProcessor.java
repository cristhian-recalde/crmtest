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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountConversionRequest;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.move.request.ConvertAccountGroupTypeRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * This processor is responsible for creating any notes relevant to the account
 * move. Note that it does not create notes for the moving of any dependencies 
 * (e.g. subscriptions or child accounts). 
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class NotesAccountMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{

    public NotesAccountMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
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
    public void move(Context ctx) throws MoveException
    {
        try
        {
            super.move(ctx);
        
        
            AMR request = getRequest();
            try
            {
                new DebugLogMsg(this, "The Request successful.  Creating move notes for request " + request, null).log(ctx);
                createAccountMovedNotes(ctx, request, true);
                new InfoLogMsg(this, "The request notes created successfully for request " + request, null).log(ctx);
            }
            catch (HomeException e)
            {
                throw new MoveException(request, "Error occurred creating account move note.", e);
            }
        }
        catch (MoveException e) 
        {
            AMR request = getRequest();
            try
            {
                new DebugLogMsg(this, "The Request successful.  Creating move notes for request " + request, null).log(ctx);
                createAccountMovedNotes(ctx, request, false);
                new InfoLogMsg(this, "The request notes created successfully for request " + request, null).log(ctx);
            }
            catch (HomeException he)
            {
                throw new MoveException(request, "Error occurred creating account move note.", he);
            }
            throw e;
        }
    }

    /**
     * Add notes for all account involved (old/new accounts, immediate parents and responsible parents)
     *
     * @param ctx
     * @param request
     */
    private void createAccountMovedNotes(final Context ctx, final AMR request, boolean success) throws HomeException
    {
        Account oldAccount = request.getOriginalAccount(ctx);
        Account oldParentAccount = oldAccount.getParentAccount(ctx);
        Account oldResponsibleParent = oldAccount.getResponsibleParentAccount(ctx);

        Account newAccount = request.getNewAccount(ctx);
        Account newParentAccount = request.getNewParentAccount(ctx);
        Account newResponsibleParent = newAccount.getResponsibleParentAccount(ctx);
        
        StringBuilder msg = new StringBuilder();
        if(request instanceof AccountConversionRequest)
        {
        	msg = msg.append("Account Converted.");
        }
        else
        {
        	msg = msg.append("Account Moved");
        }

        if ( request instanceof ConvertAccountBillingTypeRequest)
        {
            msg.append(" (on Billing Type Conversion)");
        }
        msg.append(": BAN=").append(oldAccount.getBAN());
        
        if (!SafetyUtil.safeEquals(oldAccount.getBAN(), newAccount.getBAN()))
        {
        	String newBan = "";
        	if(!newAccount.getBAN().contains(MoveConstants.DEFAULT_MOVE_PREFIX))
        	{
        		newBan = newAccount.getBAN();
        	}
        	
        	msg.append(", new BAN=").append(newBan+" ");
        }
        if (oldParentAccount != null)
        {
            msg.append(", old parent=").append(oldParentAccount.getBAN()+" ");
            msg.append(", old responsible parent=").append(oldResponsibleParent.getBAN()+" ");
        }
        if (newParentAccount != null)
        {
            msg.append(", new parent=").append(newParentAccount.getBAN()+" ");
            msg.append(", new responsible parent=").append(newResponsibleParent.getBAN()+" ");
        }

        if(request instanceof ConvertAccountGroupTypeRequest)
        {
            ConvertAccountGroupTypeRequest convRequest = (ConvertAccountGroupTypeRequest) request;
            if(convRequest.getRetainOriginalAccount())
            {
                msg = msg.append("User id : "+SystemSupport.getAgent(ctx)+" ");
                msg = msg.append("Conversion date/time : "+new Date()+" ");
                if(convRequest.getMigrateOnly())
                {
                    msg = msg.append("Data Migration process ");
                    if(request.getWarnings(ctx).size() > 0)
                    {
                    	msg.append("failed. ");
                    }
                    else
                    {
                    	msg.append(success ? "passed. " : "failed. ");
                    }
                }
                else
                {
                    msg = msg.append("Online Conversion Process "+(success ? "passed. " : "failed. ")+" ");
                }
                msg = msg.append(request.getStatusMessages(ctx));
            }
        }
        
        String moveMsg = msg.toString();

        // Instantiate a tracker for processed accounts
        Set<String> processedBANs = new HashSet<String>();

        // Create note for old and new account
        createMoveNote(ctx, oldAccount, moveMsg, processedBANs);
        createMoveNote(ctx, newAccount, moveMsg, processedBANs);

        // Create notes for old parent account and old responsible parent account
        createMoveNote(ctx, oldParentAccount, moveMsg, processedBANs);
        createMoveNote(ctx, oldResponsibleParent, moveMsg, processedBANs);

        // Create notes for new parent account and new responsible parent account
        createMoveNote(ctx, newParentAccount, moveMsg, processedBANs);
        createMoveNote(ctx, newResponsibleParent, moveMsg, processedBANs);
    }

    private void createMoveNote(final Context ctx, Account account, String msg, Set<String> processedBANs) throws HomeException
    {
        if (account != null)
        {
            // Don't create 2 notes for same account!
            final String ban = account.getBAN();
            if (!processedBANs.contains(ban))
            {
                NoteSupportHelper.get(ctx).addAccountNote(ctx, account.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.ACCMOVED);
                processedBANs.add(ban);
            }
        }
    }
}
