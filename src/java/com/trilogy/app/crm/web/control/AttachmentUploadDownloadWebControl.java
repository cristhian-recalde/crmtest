/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.io.FileSupport;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.FileUploadDownloadWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.service.http.MultipartWrapper;
import com.trilogy.service.http.catalina.CatalinaEntity;

/**
 * 
 * @author simar.singh@redknee.com
 *
 */
public class AttachmentUploadDownloadWebControl extends FileUploadDownloadWebControl
{

    public AttachmentUploadDownloadWebControl()
    {
        super();
    }


    public AttachmentUploadDownloadWebControl(int displayWidth, int maxWidth, String cmd)
    {
        super(displayWidth, maxWidth, cmd);
    }


    public AttachmentUploadDownloadWebControl(int displayWidth, int maxWidth)
    {
        super(displayWidth, maxWidth);
    }


    public AttachmentUploadDownloadWebControl(int maxWidth)
    {
        super(maxWidth);
    }


    public AttachmentUploadDownloadWebControl(String cmd)
    {
        super(cmd);
    }


    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws IllegalArgumentException
    {
        return super.fromWeb(ctx, req, name);
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.webcontrol.FileUploadWebControl#processFile(com.redknee
     * .framework.xhome.context.Context, java.io.PrintWriter, java.io.File,
     * com.redknee.service.http.MultipartWrapper)
     */
    public void processFile(Context ctx, PrintWriter out, File file, MultipartWrapper mwrap) throws IOException
    {
        AccountAttachment attachment = (AccountAttachment) ctx.get(AbstractWebControl.BEAN);
        CatalinaEntity entity = (CatalinaEntity) ctx.get(CatalinaEntity.class);
        final long maxUploadLimit = entity.getMaxFileUploadSize(); 
        final long fileLength = file.length();
        long attachUploadLimit = AccountAttachmentSupport.getAccountMangement(ctx).getMaxAttachmentSizeInBytes();
        attachUploadLimit = (attachUploadLimit > maxUploadLimit) ? maxUploadLimit :attachUploadLimit;  
        if (fileLength > attachUploadLimit)
        {
            out.print("<font color=\"red\"> File Oversize by (" + (fileLength-attachUploadLimit) + " bytes)</font><br/><br/>");
            try
            {
                // I am going to throw an exception that will fail from-web of super
                // The from-web of super will not be to delete the file
                // Let me delete this file
                file.delete();
            } catch(Throwable t)
            {
                //eat it!! not important enough to be more than logged
                new MinorLogMsg(this, "Could not delete file after failed upload due to size limit: " + file.toString(), null).log(ctx);
            }
            // this exception will fail the bean creation
            throw new IllegalArgumentException("File size of [" + fileLength + " (bytes)] exceeds either Maximum Upload Size Limit of ["
                    + maxUploadLimit + " (bytes) or Attachment upload limit [" + attachUploadLimit + " (bytes)]");
            //((HTMLExceptionListener) ctx.get(HTMLExceptionListener.class)).thrown(new CompoundIllegalStateException(e));
        }
        attachment.setSize(fileLength); // size in bytes
        attachment.setFileName(file.getName());
        // set file location relative to root path
        attachment.setFileLocation(AccountAttachmentSupport.generateFileLocationName(ctx, attachment));
        attachment.setMimeType(AccountAttachment.getFileExtension(attachment.getFileLocation()));
        // move file into place
        FileSupport.copy(ctx, file, AccountAttachmentSupport.getFilePath(ctx, attachment), true);
        // delete the temporary
        file.delete();
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.webcontrol.FileUploadDownloadWebControl#toDisplayString
     * (com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public String toDisplayString(Context ctx, Object obj)
    {
        if (obj == null || obj.toString().length() == 0)
        {
            // no file name means no file was uploaded
            return "";
        }
        // render link to attachment based on the the web-control mode and display control
        // options.
        boolean tableMode = ctx.getBoolean("TABLE_MODE", false);
        // int mode = ctx.getInt("MODE", DISPLAY_MODE);
        Object bean = ctx.get(AbstractWebControl.BEAN);
        IdentitySupport identitySupport = (IdentitySupport) XBeans.getInstanceOf(ctx, bean.getClass(),
                IdentitySupport.class);
        String id = identitySupport.ID(bean).toString();
        Context subCtx = ctx.createSubContext();
        WebAgents.setDomain(subCtx, "");
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("<a href=\"");
        buffer.append("?cmd=FileAttachmentStreamingServicer&key=");
        buffer.append(id);
        buffer.append("\" id=");
        buffer.append(id);
        buffer.append("\">");
        final AccountAttachment attachment = (AccountAttachment) bean;
        if (AccountAttachmentSupport.isAttachmentDisplayableImage(subCtx, attachment))
        {
            if (tableMode == true && AccountAttachmentSupport.getAccountMangement(ctx).isSummaryPreview())
            {
                // link image with thumbnail when in table view
                buffer.append(getPreviewImage(ctx, "?cmd=ThumbnailAttachmentStreamingServicer&key=" + id));
            }
            else if (tableMode == false && AccountAttachmentSupport.getAccountMangement(ctx).isDetailedPreview())
            {
                // link image with preview when in detailed mode
                buffer.append(getPreviewImage(ctx, "?cmd=PreviewAttachmentStreamingServicer&key=" + id));
            }
            else
            {
                // always diplay the file link with file-name in text if no other option
                // seems
                // good.
                buffer.append(obj.toString());
                buffer.append("</a>");
            }
        }
        else
        {
            // always diplay the file link with file-name in text if no other option seems
            // good.
            buffer.append(obj.toString());
            buffer.append("</a>");
        }
        return buffer.toString();
    }


    protected String getPreviewImage(Context ctx, String imageSource)
    {
        StringBuilder img = new StringBuilder();
        img.append("<img border=\"0\"");
        img.append(" src=\"");
        img.append(imageSource);
        img.append("\" alt=\"");
        img.append("Open");
        img.append("\"/> ");
        return img.toString();
    }
}
