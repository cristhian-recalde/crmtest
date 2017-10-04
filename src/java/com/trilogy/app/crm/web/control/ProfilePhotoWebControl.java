package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.beans.facets.java.lang.StringWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.InputWebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class ProfilePhotoWebControl extends AttachmentUploadDownloadWebControl
{

    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.webcontrol.OutputWebControl#toWeb(com.redknee.framework
     * .xhome.context.Context, java.io.PrintWriter, java.lang.String, java.lang.Object)
     */
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        try
        {
            ctx = ctx.createSubContext();
            if((ctx.getInt("MODE", DISPLAY_MODE) == CREATE_MODE)|| !(Session.getSession(ctx).has(Account.class) && ctx.has(Account.class)))
            {
                out.write(" Available on existing accounts only.");
                return;
            }
            String attachmentKey = (String) obj;
            out.write(allAttachmentsHref);
            if (attachmentKey != null && !"".equals(attachmentKey) && AccountAttachmentSupport.getAccountMangement(ctx).isProfileImage())
            {
                try
                {
                    AccountAttachment attachment = AccountAttachmentSupport.getAttachment(ctx, (String) obj);
                    out.write(getLink(ctx, attachment));
                    
                    // ctx.put(AbstractWebControl.BEAN, attachment);
                    // super.toWeb(ctx, out, name, attachment.getFileName());
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Could not get attachment: " + obj.toString(), e).log(ctx);
                    //defaultWebControl_.toWeb(ctx, out, name, "Could not open attachment: " + attachmentKey);
                }
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Could not get attachment: " + obj.toString(), t).log(ctx);
            //defaultWebControl_.toWeb(ctx, out, name, "Could not open attachment: " + obj.toString());
        }
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.webcontrol.FileUploadWebControl#fromWeb(com.redknee
     * .framework.xhome.context.Context, javax.servlet.ServletRequest, java.lang.String)
     */
    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        return defaultWebControl_.fromWeb(ctx, req, name);
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.web.control.AttachmentUploadDownloadWebControl#getAttachment
     * (com.redknee.framework.xhome.context.Context)
     */
    public AccountAttachment getAttachment(Context ctx, Object obj)
    {
        try
        {
            return AccountAttachmentSupport.getAttachment(ctx, (String) obj);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Could not get attachment: " + obj.toString(), null).log(ctx);
            return null;
        }
    }


    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.webcontrol.PrimitiveWebControl#fromWeb(com.redknee.
     * framework.xhome.context.Context, java.lang.Object, javax.servlet.ServletRequest,
     * java.lang.String)
     */
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        defaultWebControl_.fromWeb(ctx, req, name);
    }


    public String getLink(Context ctx, AccountAttachment attachment)
    {
        // final boolean tableMode = ctx.getBoolean("TABLE_MODE", false);
        final String id = attachment.getAttachmentKey();
        Context subCtx = ctx.createSubContext();
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("<a href=\"");
        buffer.append("?cmd=AccountAttachments&action=edit&key=");
        buffer.append(id);
        buffer.append("\" id=");
        buffer.append(id);
        buffer.append("\">");
        if (AccountAttachmentSupport.isAttachmentDisplayableImage(subCtx, attachment))
        {
            // link image with thumbnail when in table view
            buffer.append(getPreviewImage(subCtx, "?cmd=ProfilePhotoStreamingServicer&key=" + id));
        }
        else
        {
            // always display the file link with file-name in text if no other option
            // seems
            // good.
            buffer.append(attachment.getFileName());
            buffer.append("</a>");
        }
        return buffer.toString();
    }

    protected final StringWebControl defaultWebControl_ = new StringWebControl();
    public final String allAttachmentsHref = "<br/><a href=\"?cmd=AccountAttachments\"> All Attachments </a> <br/><br/>";
}
