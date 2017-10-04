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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.InfoLogMsg;


/**
 * This border exists for backwards compatibility with existing menu definitions only.  It does
 * nothing but output a log informing admin to use the following instead:
 * 
 * new com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder("AcctSubSearch")
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 * 
 * @deprecated Use {@link com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder}  instead.
 */
@Deprecated
public class AccountSearchBorder implements Border
{
    /**
     * @deprecated Use {@link com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder#RedirectSearchMenuBorder(String)}  instead.
     */
    @Deprecated
    public AccountSearchBorder()
    {
        // NOP - Deprecated
    }

    /**
     * @param ctx
     * 
     * @deprecated Use {@link com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder#RedirectSearchMenuBorder(String)}  instead.
     */
    @Deprecated
    public AccountSearchBorder(Context ctx)
    {
        // NOP - Deprecated
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder#service(Context, HttpServletRequest, HttpServletResponse, RequestServicer)}  instead.
     */
    @Deprecated
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        // NOP - Should be replaced by new com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder("AcctSubSearch") 
        
        new InfoLogMsg(this, "AccountSearchBorder is deprecated and doesn't do anything anymore.  Please use the following border instead: new com.redknee.app.crm.web.border.search.RedirectSearchMenuBorder(\"AcctSubSearch\")", null).log(ctx);
        
        delegate.service(ctx, req, res);
    }

}
