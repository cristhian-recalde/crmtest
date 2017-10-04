package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.MsisdnOwnershipXInfo;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;


public class MultiLanguageLicenseCheckBorder implements Border
{

    public MultiLanguageLicenseCheckBorder()
    {
    }


    public void service(Context ctx, HttpServletRequest req,
            HttpServletResponse res, RequestServicer delegate) throws ServletException,
            IOException
    {
        if (!SystemSupport.supportsMultiLanguage(ctx))
        {
            AbstractWebControl.setMode(ctx, MsisdnOwnershipXInfo.LANGUAGE, ViewModeEnum.NONE);
        }
        delegate.service(ctx, req, res);
    }
}
