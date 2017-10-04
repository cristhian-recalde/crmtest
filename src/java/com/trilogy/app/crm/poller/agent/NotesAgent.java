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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.poller.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Note;
import com.trilogy.app.crm.bean.NoteHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.notesdetail.NotesDetail;
import com.trilogy.app.crm.poller.Constants;
import com.trilogy.app.crm.poller.FilterOutException;
import com.trilogy.app.crm.poller.NotesDetailCreator;
import com.trilogy.app.crm.poller.ProcessorInfo;
import com.trilogy.app.crm.poller.event.CRMProcessor;
import com.trilogy.app.crm.poller.event.CRMProcessorSupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.CompoundAgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/**
 * @author skularajasingham
 * @author angie.li@redknee.com
 */
public class NotesAgent implements ContextAgent, Constants
{
    public NotesAgent(Context ctx, CRMProcessor processor)
    {
        super();
        processor_ = processor;

        this.creator_ = new NotesDetailCreator(ctx);
    }


    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        final List params = new ArrayList();
        final ProcessorInfo info = (ProcessorInfo) ctx.get(ProcessorInfo.class);
        this.transDate= new Date(info.getDate());

        final PMLogMsg pmLogMsg = new PMLogMsg(PM_MODULE, "execute()");

        try
        {
            try 
            {
                CRMProcessorSupport.makeArray(ctx, params, info.getRecord(), info.getStartIndex(), ',',info.getErid(), this);
            } 
            catch ( FilterOutException e)
            {
                return; 
            }

            LogSupport.debug(ctx, this, "Params after processing = " + params , null);

            final NotesDetail t = creator_.createNotesDetail(ctx, info, params);

            if (t == null)
            {
                LogSupport.minor(ctx, this, "Could not obtain parsed Notes ER !");
                processor_.saveErrorRecord(ctx, info.getRecord());
            }
            else
            {
                this.store(ctx,t);
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this, "Error encountered while processing ER.", t);
            processor_.saveErrorRecord(ctx, info.getRecord());
        }
        finally
        {
            pmLogMsg.log(ctx);
            CRMProcessor.playNice(ctx, CRMProcessor.HIGH_ER_THROTTLING);
        }
    }

    public void store(Context ctx,NotesDetail noteDetail)
    throws AgentException
    {

        CompoundAgentException el = new CompoundAgentException("Generic Notes Poller");

        //Create Subscriber note if the MSISDN field is populated
        if (noteDetail.getMsisdn_value()!=null && noteDetail.getMsisdn_value().length()>0)
        {
            try 
            {    
                Subscriber sub = getSubscriber(ctx, noteDetail);
                this.createSubNote(ctx, noteDetail,sub);
            }
            catch (HomeException e)
            {
                el.thrown(e);
            }

        }

        /* Create Account Note (only) if the BAN field is populated in the Note Detail.
         * In CRM 7+ implementations of Generic Notes Poller created an Account Note
         * along with the Subscriber Note.
         * As of CRM 8.2, only generate Account Notes if the notes detail has the BAN 
         * specified.
         */
        if ((noteDetail.getBan_value()!=null && noteDetail.getBan_value().length()>0))
        {
            try 
            {    
                Account acc = getAccount(ctx, noteDetail);
                this.createAccountNote(ctx, noteDetail,acc);
            }
            catch (HomeException e)
            {
                el.thrown(e);
            }
        }

        el.throwAll();
    }

    private Account getAccount(Context ctx, NotesDetail noteDetail) 
    throws HomeException
    {
        try
        {    
            String subBan = noteDetail.getBan_value();
            return AccountSupport.getAccount(ctx, subBan);
        }
        catch(HomeException e)
        {
            throw new HomeException("Failed to lookup Account from Note Detail " + noteDetail.toString(), e);
        }
    }


    /**
     * Retrieve the subscriber indicated by the Note Detail
     * @param ctx
     * @param noteDetail
     * @return
     * @throws HomeException
     */
    private Subscriber getSubscriber(Context ctx, NotesDetail noteDetail) 
        throws HomeException
    {
        /* In CRM 8.0, the Notes poller made an assumption and polled all notes
         * into the AIR TIME Subscriptions.  
         * As of CMR 8.2, and the implementation of Auxiliary Service Promotions,
         * Notes will need to be polled into any type of Subscription (Wallet, Air Time, etc.)
         * Use the subscription type given by the polled in data or the default note 
         * poller configuration to find the appropriate subscriber.
         */
        Subscriber sub = null;

        try
        {
            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, noteDetail.getMsisdn_value(),
                    noteDetail.getSubscriptionType_value(), this.transDate);
            if (sub == null)
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    String msg = "Subscriber doesn't exist in the system. NoteDetail " + noteDetail.toString();
                    new MinorLogMsg(this, msg, null).log(ctx);
                    throw new HomeException(msg);
                }
            }
        }
        catch(HomeException e)
        {
            throw new HomeException("Failed to find the subscriber to apply the note. " + e.getMessage(), e);
        }
        return sub;
    }


    /**
     * Form Subscriber Note and save to CRM.
     * @param ctx
     * @param noteDetail
     * @param newSub
     */
    public void createSubNote(Context ctx, NotesDetail noteDetail, Subscriber newSub)
        throws HomeException
    {
        //This note bean will store all the information for the note
        Note subNote = new Note();

        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "START - Creating Subscriber Note for sub=" + newSub.getId(), null).log(ctx);
        }
        //Set the Subscriber's id in the note
        subNote.setIdIdentifier(newSub.getId());
        subNote.setAgent(noteDetail.getAgent_value());
        //Set the creation date
        subNote.setCreated(this.transDate);

        subNote.setType(noteDetail.getNotetype_value());
        subNote.setSubType(noteDetail.getSubtype_value());

        StringBuilder buf = new StringBuilder(noteDetail.getSubject_value());
        buf.append(" ");
        buf.append(noteDetail.getNote_value());
        buf.append("  ");
        buf.append(noteDetail.getResult_value());

        subNote.setNote(buf.toString().trim());

        subNote.setShowOnInvoice(noteDetail.getShowOnInvoice_value());

        Home noteHome = (Home) ctx.get(NoteHome.class);
        try
        {
            noteHome.create(ctx,subNote);
        }
        catch (HomeException e)
        {
            String msg = "Fail to save Note for Subscriber [Subscriber=" + newSub.getId() + "]";
            new MinorLogMsg(this, msg, e).log(ctx);
            throw new HomeException(msg, e);
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "FINISH - Creating Subscriber Note for sub=" + newSub.getId(), null).log(ctx);
            }
        }
    }

    /**
     * Form Account Note and save to CRM.
     * @param ctx
     * @param noteDetail
     * @param newAccount
     */
    public void createAccountNote(Context ctx, NotesDetail noteDetail, Account newAccount)
        throws HomeException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "START- Creating Account Note for account=" + newAccount.getBAN(), null).log(ctx);
        }

        Note accNote = new Note();

        accNote.setIdIdentifier(newAccount.getBAN());
        accNote.setType(noteDetail.getNotetype_value());
        accNote.setSubType(noteDetail.getSubtype_value());

        accNote.setCreated(this.transDate);
        accNote.setLastModified(this.transDate);
        accNote.setAgent(noteDetail.getAgent_value());
        //Set Note content
        StringBuilder buf = new StringBuilder(noteDetail.getSubject_value());
        buf.append(" ");
        buf.append(noteDetail.getNote_value());
        buf.append(" ");
        buf.append(noteDetail.getResult_value());

        accNote.setNote(buf.toString().trim());

        accNote.setShowOnInvoice(noteDetail.getShowOnInvoice_value());

        Home noteHome = (Home) ctx.get(Common.ACCOUNT_NOTE_HOME);

        //Store the Note in the Account Note Home.
        try
        {
            noteHome.create(accNote);
        }
        catch (HomeException e)
        {
            String msg = "Fail to save Note for Account [BAN=" + newAccount.getBAN() + "]";
            new MinorLogMsg( this, msg,e).log(ctx);
            throw new HomeException(msg, e);
        }
        finally
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "FINISH - Creating Account Note for account=" + newAccount.getBAN(), null).log(ctx);
            }
        }
    }


    private CRMProcessor processor_ = null;
    private NotesDetailCreator creator_ = null;
    private static final String PM_MODULE = NotesAgent.class.getName();
    private Date transDate = null;
}
