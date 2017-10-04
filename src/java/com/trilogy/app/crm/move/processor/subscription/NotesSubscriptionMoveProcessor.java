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
package com.trilogy.app.crm.move.processor.subscription;

import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;


/**
 * 
 *
 * @author Aaron Gourley
 * @since 
 */
public class NotesSubscriptionMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{

    public NotesSubscriptionMoveProcessor(MoveProcessor<SMR> delegate)
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
        
        SMR request = this.getRequest();

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        SubscriptionMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
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
        super.move(ctx);
        
        SMR request = getRequest();
        try
        {
            String typeOfChange = "Move";
            
            if ( request instanceof ConvertSubscriptionBillingTypeRequest)
            {
                typeOfChange = "Billing Type Conversion";
            }
            new DebugLogMsg(this, typeOfChange + " successful.  Creating subscription move notes for request " + request, null).log(ctx);
            addSubscriberNotes(ctx, request);
            new InfoLogMsg(this, "Subscription " + typeOfChange + " notes created successfully for request " + request, null).log(ctx);

            new DebugLogMsg(this, "Creating account  " + typeOfChange + " notes for request " + request, null).log(ctx);            
            addAccountNotes(ctx, request);
            new InfoLogMsg(this, "Account " + typeOfChange + " notes created successfully for request " + request, null).log(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Error occurred creating subscription related move notes.", e);
        }
    }

    private void addSubscriberNotes(Context ctx, SMR request) throws HomeException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        final Home home = (Home) ctx.get(NoteHome.class);
        if (home == null)
        {
            throw new HomeException("No Note Home found in context.");
        }

        // The old Subscriber note.
        User user = (User) ctx.get(Principal.class, new User());

        final Note oldSubscriberNote = new Note();
        oldSubscriberNote.setIdIdentifier(oldSubscription.getId());
        oldSubscriberNote.setAgent(user.getId());
        oldSubscriberNote.setCreated(new Date());
        oldSubscriberNote.setType(SystemNoteTypeEnum.EVENTS.getDescription());
        oldSubscriberNote.setSubType(SystemNoteSubTypeEnum.SUBUPDATE.getDescription());
        
        if ( request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            String noteMsg = " BillingType Conversion, BAN from " + oldSubscription.getBAN() + " to " + newSubscription.getBAN();
            oldSubscriberNote.setNote(noteMsg);
        }
        else
        {
            oldSubscriberNote.setNote("Subscriber moved to account " + newSubscription.getBAN() + " with identifier "
                    + newSubscription.getId() + ".");
        }
        
        
        try
        {
            home.create(ctx, oldSubscriberNote);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Failed to create move note for old subscriber.", e).log(ctx);
            throw e;
        }

        if (!SafetyUtil.safeEquals(oldSubscription.getId(), newSubscription.getId()))
        {
            // The new Subscriber note.            
            final Note newSubscriberNote = new Note();
            newSubscriberNote.setIdIdentifier(newSubscription.getId());
            newSubscriberNote.setAgent(oldSubscriberNote.getAgent());
            newSubscriberNote.setCreated(oldSubscriberNote.getCreated());
            newSubscriberNote.setType(oldSubscriberNote.getType());
            newSubscriberNote.setSubType(oldSubscriberNote.getSubType());
            
            if ( request instanceof ConvertSubscriptionBillingTypeRequest)
            {
                String noteMsg = " BillingType Conversion, BAN from " + oldSubscription.getBAN() + " to " + newSubscription.getBAN();
                newSubscriberNote.setNote(noteMsg);
            }
            else
            {    
                newSubscriberNote.setNote("Subscriber moved from account " + oldSubscription.getBAN() + " with identifier "
                        + oldSubscription.getId() + ".");
            }
            try
            {
                home.create(ctx, newSubscriberNote);
            }
            catch (final HomeException e)
            {
                new MinorLogMsg(this, "Failed to create move note for new subscriber.", e).log(ctx);
                throw e;
            }
        }
    }

    private void addAccountNotes(Context ctx, SMR request) throws HomeException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        
        Account oldAccount = request.getOldAccount(ctx);
        Account newAccount = request.getNewAccount(ctx);
        
        String msg = "Subscription (MSISDN=" + oldSubscription.getMSISDN() + ") "
                             + "moved from BAN=" + oldAccount.getBAN() + " to BAN=" + newAccount.getBAN();
        
        if ( request instanceof ConvertSubscriptionBillingTypeRequest)
        {
            msg = "Subscription conversion " + oldSubscription.getMSISDN() + " ";
        }

        // Instantiate a tracker for processed accounts
        Set<String> processedBANs = new HashSet<String>();
        
        createAccountMoveNote(ctx, oldAccount, msg, processedBANs);
        createAccountMoveNote(ctx, newAccount, msg, processedBANs);
    }

    private void createAccountMoveNote(final Context ctx, Account account, String msg, Set<String> processedBANs) throws HomeException
    {
        if (account != null)
        {
            // Don't create 2 notes for same account (applies to responsible account moves)!
            final String ban = account.getBAN();
            if (!processedBANs.contains(ban))
            {
                NoteSupportHelper.get(ctx).addAccountNote(ctx, account.getBAN(), msg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.SUBUPDATE);
                processedBANs.add(ban);
            }
        }
    }
}
