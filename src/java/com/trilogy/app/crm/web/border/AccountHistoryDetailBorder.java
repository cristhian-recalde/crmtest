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
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;

import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryTableWebControl;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;


/**
 * Provide detail list of split transaction for the account payments.
 *
 * @author victor.stratan@redknee.com
 * @since 8.6
 */
public class AccountHistoryDetailBorder implements Border
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        delegate.service(ctx, req, res);

        Object bean = WebController.getBean(ctx);
        if (bean != null && bean instanceof AccountHistory)
        {
            AccountHistory ah = (AccountHistory) bean;
            if (ah.getType().equals(AccountHistoryTypeEnum.ACCOUNT_TRANSACTION))
            {
                PrintWriter out = res.getWriter();
                out.println("<hr noshade width=\"90%\">");

                AccountHistoryTableWebControl webControl = new AccountHistoryTableWebControl();
                webControl.toWeb(ctx, out, "AccountHistorySplit", ah.getSplitTransactions(ctx));
            }
        }
    }

}
