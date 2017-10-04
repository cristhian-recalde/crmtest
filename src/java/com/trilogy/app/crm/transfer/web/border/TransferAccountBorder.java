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

package com.trilogy.app.crm.transfer.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeXInfo;
import com.trilogy.app.crm.transfer.TransfersView;
import com.trilogy.app.crm.transfer.TransfersViewHome;
import com.trilogy.app.crm.transfer.TransfersViewXInfo;
import com.trilogy.app.crm.transfer.TransfersXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

public class TransferAccountBorder
    implements Border
{
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, final RequestServicer delegate)
        throws ServletException, IOException
    {
        Context subCtx = ctx.createSubContext();
        Home h = (Home)ctx.get(TransfersViewHome.class);
        Account account = (Account)ctx.get(Account.class);
        Or where = new Or();

        where.add(new EQ(TransfersViewXInfo.CONT_SUB_ACCOUNT, account.getBAN()));
        where.add(new EQ(TransfersViewXInfo.RECP_SUB_ACCOUNT, account.getBAN()));

        subCtx.put(TransfersViewHome.class, h.where(ctx, where));

        delegate.service(subCtx, req, res);
    }
}