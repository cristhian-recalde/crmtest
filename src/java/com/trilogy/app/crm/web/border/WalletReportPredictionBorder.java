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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.WalletReportHome;
import com.trilogy.app.crm.home.WalletReportPredictionHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * Adds the WalletReport Prediction to include ghost reports
 * @author arturo.medina@redknee.com
 *
 */
public class WalletReportPredictionBorder implements Border
{

    /**
     * {@inheritDoc}
     */
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
            Context subCtx  = ctx.createSubContext();
            subCtx.setName("WalletReportPrediction");
            Context session = Session.getSession(ctx);
            Account account = (Account) session.get(Account.class);

            if ( account != null )
            {
               subCtx.put(
                  WalletReportHome.class,
                  new WalletReportPredictionHome(
                     ctx,
                     account.getBAN(),
                     (Home)subCtx.get(WalletReportHome.class)));
            }
            
            delegate.service(subCtx, req, res);
    }

}
