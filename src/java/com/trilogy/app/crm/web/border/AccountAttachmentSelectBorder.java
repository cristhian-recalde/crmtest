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
package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountAttachmentHome;
import com.trilogy.app.crm.bean.account.AccountAttachmentTransientHome;
import com.trilogy.app.crm.bean.account.AccountAttachmentXInfo;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * author: simar.singh@redknee.com 
 * A Border for the AccountAttachment screen Presets the
 * the BAN selection
 * 
 */
public class AccountAttachmentSelectBorder implements Border
{

    /** Blank Home to select nothing **/
    private final Home blankAttachmentHome_;


    public AccountAttachmentSelectBorder(Context ctx)
    {
        blankAttachmentHome_ = new AccountAttachmentTransientHome(ctx);
    }


    public void service(Context ctx, final HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        ctx = ctx.createSubContext();
        Context session = Session.getSession(ctx);
        Account acct = (Account) session.get(Account.class);
        if (ctx.has(Account.class))
        {
            acct = (Account) ctx.get(Account.class);
        }
        else
        {
            ctx.put(Account.class, acct);
        }
        Home home = (Home) ctx.get(AccountAttachmentHome.class);
        if (acct != null)
        {
            home = home.where(ctx, new EQ(AccountAttachmentXInfo.BAN, acct.getBAN()));
            try
            {
                int numOfAttachments = AccountAttachmentSupport.getNumberOfAttachements(ctx, home);
                int allowedNumberOfAttachments = AccountAttachmentSupport.getAccountMangement(ctx)
                        .getMaxNumberOfAttachments();
                if (!WebController.isCmd("New", req) && !WebController.isCmd("Save", req))
                {
                    res.getWriter().write(
                            new StringBuilder("<br/> <br/> The account [").append(acct.getBAN()).append("] has ")
                                    .append(numOfAttachments).append(" attachment(s). ").append(
                                            " Total number of attachments allowed are ").append(
                                            allowedNumberOfAttachments).append("<br/> <br/>").toString());
                }
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Could count the number of attachments for BAN: " + acct.getBAN(), e).log(ctx);
            }
        }
        else
        {
            res.getWriter().write("<br/>No accouts in selection. Please browse an account first.");
            home = blankAttachmentHome_;
        }
        ctx.put(AccountAttachmentHome.class, home);
        delegate.service(ctx, req, res);
    }
}
