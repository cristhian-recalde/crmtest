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
package com.trilogy.app.crm.account;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.bean.account.AccountAttachmentManagementConfig;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.util.FileUtil;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.app.crm.web.service.ImageUtil;
import com.trilogy.framework.core.http.MimeType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class adapts creation of BANAware enities First use: AccountAttachmentHome chain.
 * 
 * @author Simar Singh
 * @date Jan 12, 2009
 */
public class AttachmentSettingsGenerationHome extends HomeProxy
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new BANAwareHome decorator.
     * 
     * @param context
     * @param delegate
     *            The Home to which we delegate.
     */
    public AttachmentSettingsGenerationHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }


    public Object create(Context ctx, Object obj) throws HomeException
    {
        final AccountAttachment attachment = (AccountAttachment) obj;
        final Account account = AccountSupport.getAccount(ctx, attachment.getBAN());
        if (account == null)
        {
            throw new HomeException(
                    "Attachments can be created against a valid account only. No valid account found for BAN: "
                            + attachment.getBAN());
        }
        if (AccountAttachmentSupport.getAccountMangement(ctx).getMaxNumberOfAttachments() < AccountAttachmentSupport
                .getNumberOfAttachmentsForBan(ctx, account.getBAN()))
        {
            throw new HomeException("Total number of attachments must not exceed the limit per Account ["
                    + attachment.getBAN() + "]. The limit is "
                    + AccountAttachmentSupport.getAccountMangement(ctx).getMaxNumberOfAttachments());
        }
        setFilePath(ctx, attachment);        
        File filePath = AccountAttachmentSupport.getFilePath(ctx, attachment);
        MimeType mimeType = FileUtil.getMimeTypeForFile(ctx, attachment.getFileName());
        if (mimeType != null)
        {
            attachment.setMimeType(mimeType.getExt());
            if (FileUtil.isMimeTypeImage(mimeType))
            {
                // String thumnalFilePath =
                // AccountAttachmentSupport.getThumbnailFilePath(ctx, attachment);
                final AccountAttachmentManagementConfig config = AccountAttachmentSupport.getAccountMangement(ctx);
                attachment
                        .setPreviewLocation(AccountAttachmentSupport.generatePreviewFileLocationName(ctx, attachment));
                String type = config.getMimeType();
                try
                {
                    ImageUtil.resizeImage(filePath, AccountAttachmentSupport.getPreviewFilePath(ctx, attachment), type,
                            config.getMaxWidthImage(), config.getMaxHeightImage());
                }
                catch (NoSuchElementException nse)
                {
                    // attachment.setThumbnailLocation(false);
                    new MinorLogMsg(
                            this,
                            "The Image type is not supported for conversion in the scope of platform. Preview Image could not get generated for BAN-attachment: "
                                    + attachment.getFileLocation(), nse).log(ctx);
                    attachment.setPreviewLocation("");
                }
                catch (IOException e)
                {
                    // attachment.setThumbnailLocation(false);
                    new MinorLogMsg(this, "Preview Image could not get generated for BAN-attachment: "
                            + attachment.getFileLocation(), e).log(ctx);
                    attachment.setPreviewLocation("");
                }
                catch (Throwable t)
                {
                    // attachment.setThumbnailLocation(false);
                    new MinorLogMsg(this, "Error!! Preview Image could not get generated for BAN-attachment: "
                            + attachment.getFileLocation(), t).log(ctx);
                    attachment.setPreviewLocation("");
                }
            }
        }
        return super.create(ctx, obj);
    }

    /*
     * @Override(non-Javadoc)
     * 
     * @see
     * com.redknee.framework.xhome.home.HomeProxy#remove(com.redknee.framework.xhome.context
     * .Context, java.lang.Object)
     */
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final AccountAttachment attachment = (AccountAttachment) obj;
        final File file = new File(attachment.getFilePath(),attachment.getFileLocation());
        final File previewFile = AccountAttachmentSupport.getPreviewFilePath(ctx, attachment);
        try
        {
            file.delete();
            Account account = AccountSupport.getAccount(ctx, attachment.getBAN());
            if (attachment.getAttachmentKey().equals(account.getProfileAttachmentKey()))
            {
                account.setProfileAttachmentKey("");
                
            }
        }
        catch (Throwable t)
        {
           // eat it; deletion may still proceed
            new MinorLogMsg(this, "Could not delete attachment dependencies :" + file + " for BAN = "
                    + attachment.getBAN(), t).log(ctx);
        }
        if (previewFile.isFile())
        {
            try
            {
                previewFile.delete();

            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Could not delete attachment file :" + previewFile + " for BAN = "
                        + attachment.getBAN(), t).log(ctx);
                // eat it; deletion may still proceed
            }
        }
        super.remove(ctx, obj);
    }
    
    private void setFilePath(Context ctx, AccountAttachment attachment)
    {
        attachment.setFilePath(com.redknee.app.crm.web.control.AccountAttachmentSupport.getAvailableFolderPath(ctx, attachment.getBAN()).getAbsolutePath());
    }
}
