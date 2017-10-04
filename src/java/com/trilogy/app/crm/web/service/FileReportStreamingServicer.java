/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.service;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.service.AbstractStreamingServicer;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/*
 * Author : Simar Singh Date : 25 Feb 2010
 */
public class FileReportStreamingServicer extends AbstractStreamingServicer
{

    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.web.service.AbstractStreamingServicer#getFile(com.redknee
     * .framework.xhome.context.Context, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public File getFile(Context ctx, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String key = req.getParameter("key");
        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (key == null)
        {
            return null;
        }
        try
        {
            final File filePath = new File(key + ".err");
            if (!filePath.isFile())
            {
                return null;
            }
            return filePath;
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, t.getMessage(), t).log(ctx);
            final ExceptionListener exceptionListner = (ExceptionListener) ctx.get(ExceptionListener.class);
            exceptionListner.thrown(t);
            return null;
        }
    }
    
    /**
     * If you do not know the file type, stream it in binary
     */
    @Override
    public String getDefaultContentType(Context ctx)
    {
        // TODO Auto-generated method stub
        return "application/octet-stream";
    }
}
