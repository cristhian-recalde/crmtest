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
import com.trilogy.framework.xhome.web.xmenu.service.XMenuService;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;


/**
 * Redirects to another screen defined by the 'cmd' key if 'redirect' is in request
 * 
 * @author ltang
 */
public class RedirectSearchMenuBorder implements Border
{

    public RedirectSearchMenuBorder()
    {
        super();
    }


    /**
     * {@inheritDoc}
     */
    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        String redirect = req.getParameter("redirect");

        if (redirect != null)
        {
            String servicerKey = req.getParameter("cmd");
            XMenuService srv = (XMenuService) ctx.get(XMenuService.class);
            RequestServicer advSearch = srv.getServicer(ctx, servicerKey);
            advSearch.service(ctx, req, res);
        }
        else
        {
            delegate.service(ctx, req, res);
        }
    }

}