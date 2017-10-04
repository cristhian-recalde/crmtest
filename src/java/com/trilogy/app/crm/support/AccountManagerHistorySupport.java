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
package com.trilogy.app.crm.support;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountManagerHistory;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryHome;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryXInfo;
import com.trilogy.app.crm.bean.account.AccountManagerXInfo;


/**
 * Support class for AccountManagerHistory
 * 
 * @author ltang
 * 
 */
public class AccountManagerHistorySupport
{

    /**
     * Create an AccountManagerHistory entry for this AccountManager
     * 
     * @param ctx The operating context
     * @param accountMgr The AccountManager to create the entry for
     * @param source The source
     */
    public static void createAccountManagerHistory(final Context ctx, final AccountManager accountMgr,
            final Object source) throws HomeException
    {
        StringBuilder note = new StringBuilder();
        AccountManager oldAccountMgr = HomeSupportHelper.get(ctx).findBean(ctx, AccountManager.class, accountMgr.getAccountMgrId());

        if (oldAccountMgr == null)
        {
            note.append("Create ");
            note.append(accountMgr);
        }
        else
        {
            note.append("Modify ");
            note.append(AccountManagerXInfo.instance().getName());
            note.append(" ");
            note.append(accountMgr.getAccountMgrId());
            createNote(AccountManagerXInfo.ACCOUNT_MGR_ID.getLabel(), oldAccountMgr.getAccountMgrId(), accountMgr.getAccountMgrId(), note);
            createNote(AccountManagerXInfo.NAME.getLabel(), oldAccountMgr.getName(), accountMgr.getName(), note);
            createNote(AccountManagerXInfo.SPID.getLabel(), String.valueOf(oldAccountMgr.getSpid()),
                    String.valueOf(accountMgr.getSpid()), note);
        }

        createAccountManagerHistory(ctx, accountMgr, SystemSupport.getAgent(ctx), note, source);
    }


    /**
     * Create an AccountManagerHistory entry for this AccountManager
     * 
     * @param ctx The operating context
     * @param accountMgr The AccountManager to create the entry for
     * @param agent The agent who performed the modification
     * @param note The note detailing the modification
     * @param source The source
     */
    public static void createAccountManagerHistory(final Context ctx, final AccountManager accountMgr,
            final String agent, final StringBuilder note, Object source)
    {
        if (source == null)
        {
            source = AccountManagerHistorySupport.class;
        }

        new DebugLogMsg(source, "Start - createAccountManagerHistory" + " AccountManager[" + accountMgr + "]"
                + " Agent[" + agent + "]" + " Note[" + note + "]", null).log(ctx);

        if (note.length() > AccountManagerHistory.NOTE_WIDTH)
        {
            new MinorLogMsg(source, "AccountManagerHistory length exceeded maximum " + AccountManagerHistory.NOTE_WIDTH
                    + ", will be truncated: " + note, null).log(ctx);
            note.delete(AccountManagerHistory.NOTE_WIDTH, note.length());
        }

        AccountManagerHistory accountMgrHistory = new AccountManagerHistory();
        accountMgrHistory.setAccountMgrId(accountMgr.getAccountMgrId());
        accountMgrHistory.setAgent(agent);
        accountMgrHistory.setHistoryDate(new Date());
        accountMgrHistory.setNote(note.toString());

        final Home accountMgrHistoryHome = (Home) ctx.get(AccountManagerHistoryHome.class);
        try
        {
            accountMgrHistoryHome.create(accountMgrHistory);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(source, "Failed to create account manager history for AccountManager [" + accountMgr + "]",
                    e).log(ctx);
        }

        new DebugLogMsg(source, "End - createAccountManagerHistory", null).log(ctx);
    }


    /**
     * Appends a note detailing old and new values
     * 
     * @param label
     * @param oldVal
     * @param newVal
     * @param noteBuff
     */
    private static void createNote(String label, String oldVal, String newVal, StringBuilder noteBuff)
    {
        if (oldVal == null)
        {
            oldVal = "";
        }
        if (newVal == null)
        {
            newVal = "";
        }

        if (!newVal.equals(oldVal))
        {
            noteBuff.append("\n[");
            noteBuff.append(label);
            noteBuff.append(" :");
            noteBuff.append(oldVal);
            noteBuff.append("->");
            noteBuff.append(newVal);
            noteBuff.append("]");
        }
    }


    /**
     * Removes all AccountManagerHistory entries for the given AccountManager
     * 
     * @param ctx The operating context
     * @param accountMgr The AccountManager to remove the entries for
     * @param source The source
     */
    public static void removeAllAccountManagerHistory(final Context ctx, final AccountManager accountMgr, Object source)
    {
        if (source == null)
        {
            source = AccountManagerHistorySupport.class;
        }

        new DebugLogMsg(source, "Start - removeAllAccountManagerHistory " + " AccountManager[" + accountMgr + "]", null)
                .log(ctx);

        Home accountMgrHistoryHome = (Home) ctx.get(AccountManagerHistoryHome.class);
        try
        {
            accountMgrHistoryHome.removeAll(ctx, new EQ(AccountManagerHistoryXInfo.ACCOUNT_MGR_ID, accountMgr
                    .getAccountMgrId()));
        }
        catch (HomeException e)
        {
            new MinorLogMsg(source, "Failed to remove account manager history for AccountManager [" + accountMgr + "]",
                    e).log(ctx);
        }

        new DebugLogMsg(source, "End - removeAllAccountManagerHistory", null).log(ctx);
    }
}
