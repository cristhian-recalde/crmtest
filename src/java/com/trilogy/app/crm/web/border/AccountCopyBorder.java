package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebController;


public class AccountCopyBorder implements Border
{
    @Override
    public void service(Context ctx, HttpServletRequest req,
        HttpServletResponse res, RequestServicer delegate)
        throws ServletException, IOException
    {
        boolean isCopy = WebController.isCmd("Copy", req);
        if (isCopy)
        {
            Context subCtx = ctx.createSubContext();
            subCtx.put(ACCOUNT_COPY_PERFORMED, Boolean.TRUE);
            delegate.service(subCtx, req, res);
        }
        else
        {
            delegate.service(ctx, req, res);
        }


    }
    
    public static String ACCOUNT_COPY_PERFORMED = "accountCopyPerformed";
}
