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

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.ValidatingHome;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.factory.core.TransactionFactory;
import com.trilogy.app.crm.home.WebExceptionHandlingHome;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.transaction.TransactionAdjustmentTypeLimitValidator;

/**
 * Decorates the Validation at GUI side 
 *
 * @author arturo.medina@redknee.com
 */
public class TransactionBorder implements Border, Validator 
{
    /**
     * {@inheritDoc}
     */
    public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res,
            final RequestServicer delegate) throws ServletException, IOException 
    {
        Context subCtx = ctx.createSubContext();
        
        if ("y".equals(req.getParameter("accountPayment")))
        {
            subCtx.put(TransactionFactory.ACCOUNT_PAYMENT_TRANSACTION, true);
        }
        
		subCtx.put(TransactionHome.class, new WebExceptionHandlingHome(new ValidatingHome(this, (Home) subCtx.get(TransactionHome.class))));

        subCtx.put(TransactionHome.class, new ValidatingHome(this, (Home) subCtx.get(TransactionHome.class)));
        
        subCtx.put(TransactionAdjustmentTypeLimitValidator.VALIDATE_KEY, true);
        
        delegate.service(subCtx,req,res);
    }

    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj) throws IllegalStateException 
    {
        if (obj instanceof Transaction)
        {
            final Transaction txn = (Transaction) obj;
            
            
            if (txn.getAmount() == 0 && !CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, txn) && txn.getExpiryDaysExt()== 0)
            {
                throw new IllegalStateException("The Amount and the Expiry Days Extension should not be zero");
            }
        }
        else
        {
            throw new IllegalStateException("Wrong argument ");
        }
    }
}
