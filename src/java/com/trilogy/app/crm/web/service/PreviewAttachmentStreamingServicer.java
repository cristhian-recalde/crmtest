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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.account.AttachmentSettingsGenerationHome;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;


/*
 * Author : Simar Singh Date : 18 Jan 2009 This request servicer streams attachment images
 * resized and converted to desired dimensions and type
 */
public class PreviewAttachmentStreamingServicer extends FileAttachmentStreamingServicer
{

    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res) throws ServletException,
            IOException
    {
        final PMLogMsg pmPreview = new PMLogMsg(PM_IMAGE_PROCESSING, PM_PREVIEW_ATTACHMENT);
        AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
        try
        {
            File file = getFile(ctx, req, res);
            if (file == null)
            {
                // no file = nothing to stream
                return;
            }
            setHeader(ctx, req, res);
            ImageProp prop = getImageProp(ctx);
            final PMLogMsg pmImageResize = new PMLogMsg(PM_IMAGE_PROCESSING, PM_PREVIEW_IMAGE_RESIZING);
            BufferedImage image = ImageUtil.resizeImage(file, prop.x_Width, prop.y_Height);
            pmImageResize.log(ctx);
            final PMLogMsg pmImageStreaming = new PMLogMsg(PM_IMAGE_PROCESSING, PM_PREVIEW_IMAGE_TYPE_STREAMING);
            ByteArrayOutputStream imageByteStream = new ByteArrayOutputStream();
            ImageUtil.writeImageToStream(image, config.getMimeType(), imageByteStream);
            res.setContentLength(imageByteStream.size());
            res.setContentType(config.getMimeType());
            imageByteStream.writeTo(res.getOutputStream());
            // flush and comit response
            res.flushBuffer();
            pmImageStreaming.log(ctx);
            pmPreview.log(ctx);
            new InfoLogMsg(this, "isCommitted [" + res.isCommitted() + "]", null).log(ctx);
        }
        catch (FileNotFoundException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
            res.getWriter().println("<script language=\"JavaScript\">alert('File not found');history.go(-1);</script>");
        }
        catch (IOException e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(ctx);
            res.getWriter().println(
                    "<script language=\"JavaScript\">alert('Unknown error occured [" + e.getMessage()
                            + "]');history.go(-1);</script>");
        }
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.web.service.FileAttachmentStreamingServicer#getFileForAttachment
     * (com.redknee.framework.xhome.context.Context,
     * com.redknee.app.crm.bean.account.AccountAttachment)
     */
    protected File getFileForAttachment(Context ctx, AccountAttachment attachment)
    {
        return AccountAttachmentSupport.getPreviewFilePath(ctx, attachment);
    }


    protected ImageProp getImageProp(Context ctx)
    {
        final AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
        return new ImageProp(config.getMaxWidthPreviewImage(), config.getMaxHeightPreviewImage(), config.getMimeType());
    }

    protected class ImageProp
    {

        public ImageProp(int width, int height, String type)
        {
            super();
            x_Width = width;
            y_Height = height;
            this.type = type;
        }

        protected final int x_Width;
        protected final int y_Height;
        protected final String type;
    }

    public static final String PM_PREVIEW_ATTACHMENT = "PM_PREVIEW_ATTACHMENT";
    public static final String PM_PREVIEW_IMAGE_RESIZING = "PM_PREVIEW_IMAGE_RESIZING";
    public static final String PM_PREVIEW_IMAGE_TYPE_STREAMING = "PM_PREVIEW_IMAGE_TYPE_STREAMING";
    public static final String PM_IMAGE_PROCESSING = "PM_IMAGE_PROCESSING";
}
