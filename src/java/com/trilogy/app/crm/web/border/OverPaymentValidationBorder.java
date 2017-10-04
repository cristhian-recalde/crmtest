
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

import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.home.ValidatingOverPaymentTransactionHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;


/**
 * Provides a border that adds a Transaction validating home to the context to
 * enforce the restriction for creating OverPayment Credit transaction.
 *
 * @author shailesh.makhijani@redknee.com
 */
public class OverPaymentValidationBorder
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
        Home originalHome = (Home)ctx.get(TransactionHome.class);

        final Context subcontext = ctx.createSubContext();

        originalHome = new ValidatingOverPaymentTransactionHome(originalHome);
        subcontext.put(TransactionHome.class, originalHome);

        delegate.service(subcontext, req, res);
    }

} 
