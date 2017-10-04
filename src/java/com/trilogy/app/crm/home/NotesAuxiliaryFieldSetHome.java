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
package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountNote;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.NoteOwnerTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberNote;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * Sets the auxiliary fields in the note entity so that config share properly handle entity
 */
public class NotesAuxiliaryFieldSetHome extends HomeProxy
{

    public NotesAuxiliaryFieldSetHome(final Context ctx, Home delegate, NoteOwnerTypeEnum owner)
    {
        super(ctx,delegate);
        ownerType_ = owner;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        new InfoLogMsg(this, " Setting auxiliary fields in the Account/Subscriber note", null).log(ctx);
        if (ownerType_ == NoteOwnerTypeEnum.ACCOUNT)
        {
            updateAccountFields(ctx, (AccountNote) obj);
        }
        else if (ownerType_ == NoteOwnerTypeEnum.SUBSCRIPTION)
        {
            updateSubscriberFields(ctx, (SubscriberNote) obj);
        }
        Object ret = getDelegate().create(ctx, obj);
        return ret;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        new InfoLogMsg(this, " Setting auxiliary fields in the Account/Subscriber note", null).log(ctx);
        if (ownerType_ == NoteOwnerTypeEnum.ACCOUNT)
        {
            updateAccountFields(ctx, (AccountNote) obj);
        }
        else if (ownerType_ == NoteOwnerTypeEnum.SUBSCRIPTION)
        {
            updateSubscriberFields(ctx, (SubscriberNote) obj);
        }
        Object ret = getDelegate().store(ctx, obj);
        return ret;
    }


    public static void updateSubscriberFields(final Context ctx, SubscriberNote note) throws HomeException
    {
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        if (sub == null)
        {
            sub = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class,
                    new EQ(SubscriberXInfo.ID, note.getIdIdentifier()));
        }
        if (sub != null)
        {
            note.setBan(sub.getBAN());
            note.setSpid(sub.getSpid());
            int length = 199;
            if ( note.getNote().length() < length)
            {
                length = note.getNote().length();
            }
            
            String detail = note.getNote().substring(0, length);
            
            String detailWithNospaces = detail.replaceAll("/n", " ");
            note.setNoteTitle(detailWithNospaces);
        }
    }


    public static void updateAccountFields(final Context ctx, AccountNote note) throws HomeException
    {
        Account account = (Account) ctx.get(Account.class);
        if (account == null)
        {
            account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class,
                    new EQ(AccountXInfo.BAN, note.getIdIdentifier()));
        }
        if (account != null)
        {
            note.setSpid(account.getSpid());
        }
        int length = 199;
        if ( note.getNote().length() < length)
        {
            length = note.getNote().length();
        }
        
        String detail = note.getNote().substring(0, length);
        
        String detailWithNospaces = detail.replaceAll("/n", " ");
        note.setNoteTitle(detailWithNospaces);

    }

    private NoteOwnerTypeEnum ownerType_;
}
