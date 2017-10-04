/*
 *  CreditPaymentEnforcementBorder.java
 *
 *  Author : Gary Anderson
 *  Date   : 2004-01-19
 *
 *  Copyright (c) 2004, Redknee
 *  All rights reserved.
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.home.ValidatingAmountTransactionHome;


/**
 * Provides a border that adds a Transaction validating home to the context to
 * enforce the rule that payments are credits and not debits.
 *
 * @author gary.anderson@redknee.com
 */
public class CreditPaymentEnforcementBorder
    implements Border
{
    // INHERIT
    public void service(
        final Context ctx,
        final HttpServletRequest req,
        final HttpServletResponse res,
        final RequestServicer delegate)
        throws ServletException, IOException
    {
        final Home originalHome = (Home)ctx.get(TransactionHome.class);

        final Context subcontext = ctx.createSubContext();

        final Home enforcementHome =
            new ValidatingAmountTransactionHome(subcontext, originalHome);

        subcontext.put(TransactionHome.class, enforcementHome);

        delegate.service(subcontext, req, res);
    }

} // class
