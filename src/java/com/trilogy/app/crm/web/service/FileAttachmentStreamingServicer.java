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

import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentHome;
import com.trilogy.app.crm.util.FileUtil;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.core.http.MimeType;
import com.trilogy.framework.core.http.MimeTypeHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.web.service.AbstractStreamingServicer;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/*
 * Author : Simar Singh Date : 18 Jan 2009
 */
public class FileAttachmentStreamingServicer extends AbstractStreamingServicer
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
        if (key == null)
        {
            return null;
        }
        try
        {
            AccountAttachment attachment = (AccountAttachment) ((Home) ctx.get(AccountAttachmentHome.class)).find(ctx,
                    key);
            if (attachment == null)
            {
                new MinorLogMsg(this, "unknown Node [" + req.getParameter("key") + "]", null).log(ctx);
                return null;
            }
            else
            {
                final File filePath = getFileForAttachment(ctx, attachment);
                if (!filePath.isFile())
                {
                    return null;
                }
                return filePath;
            }
        }
        catch (HomeException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
            return null;
        }
    }


    protected File getFileForAttachment(Context ctx, AccountAttachment attachment)
    {
        return new File(attachment.getFilePath(),attachment.getFileLocation());
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.web.service.AbstractStreamingServicer#setContentType
     * (com.redknee.framework.xhome.context.Context,
     * javax.servlet.http.HttpServletResponse, java.io.File) over-riding it to just change
     * 2 things a. default content type made application/octet b. watch for a case where
     * file ends with extension
     */
    public void setContentType(Context ctx, HttpServletResponse res, File file)
    {
        /*
         * if we do not know the file type...browser should download it as binary
         */
        String type = "application/octet-stream";
        if (file != null)
        {
            String name = file.getName();
            String ext = null;
            ext = FileUtil.getFileExtension(name);
            if (ext != null && !"".equals(ext))
            {
                try
                {
                    MimeType mimeType = (MimeType) ((Home) ctx.get(MimeTypeHome.class)).find(ctx, ext);
                    if (mimeType != null)
                    {
                        type = mimeType.getMimeType();
                    }
                    else
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "unable to find mime type for extension [" + ext + "]", null)
                                    .log(ctx);
                        }
                    }
                }
                catch (HomeException e)
                {
                    new MajorLogMsg(this, e.getMessage(), e).log(ctx);
                }
            }
            else
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "unable to determime extension for file [" + name + "]", null).log(ctx);
                }
            }
        }
        res.setContentType(type);
    }
}
