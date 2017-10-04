/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.account.AccountAttachment;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.move.support.AccountMoveValidationSupport;
import com.trilogy.app.crm.web.control.AccountAttachmentSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;


/**
 * Handles the moving of an account's attachments.  The attachments must be copied
 * in the file system and the database records must be copied.  Only warnings should
 * be reported in the event of failure, and records should only be copied if the
 * physical file copy was successful.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class AccountAttachmentMoveProcessor<AMR extends AccountMoveRequest> extends MoveProcessorProxy<AMR>
{
    public AccountAttachmentMoveProcessor(MoveProcessor<AMR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        AMR request = this.getRequest();
        
        AccountMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
        
        AccountMoveValidationSupport.validateNewAccountExists(ctx, request, cise);
        
        cise.throwAll();

        super.validate(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        super.move(ctx);

        AMR request = this.getRequest();
        
        if (!SafetyUtil.safeEquals(request.getExistingBAN(), request.getNewBAN()))
        {
            Set<String> copiedFiles = copyAttachments(ctx, request);
            if (copiedFiles != null && copiedFiles.size() > 0)
            {
                copyAttachmentRecords(ctx, request, copiedFiles);
            }
        }
    }


    private Set<String> copyAttachments(Context ctx, final AMR request) throws MoveException
    {
        final Account newAccount = request.getNewAccount(ctx);
        final Set<String> copiedFiles = new HashSet<String>();
        // clean all attachments to the account
        Home home = AccountAttachmentSupport.getBanAttachmentsHome(ctx, request.getExistingBAN());
        try
        {
            Context subCtx = ctx.createSubContext();
            home.forEach(subCtx, new Visitor()
            {

                @Override
                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                {
                    AccountAttachment attachment = (AccountAttachment) obj;
                    File oldAttachmentDir = new File(attachment.getFilePath());
                    if (oldAttachmentDir.exists())
                    {
                        File newAttachmentDir = AccountAttachmentSupport.getAvailableFolderPath(ctx,
                                newAccount.getBAN());
                        if (oldAttachmentDir.isDirectory())
                        {
                            String files[] = oldAttachmentDir.list();
                            for (int i = 0; i < files.length; i++)
                            {
                                try
                                {
                                    copyFile(new File(oldAttachmentDir, files[i]), new File(newAttachmentDir, files[i]));
                                    copiedFiles.add(files[i]);
                                }
                                catch (IOException ioe)
                                {
                                    request.reportWarning(ctx,
                                            new MoveWarningException(request, "Error copying attachment " + files[i]
                                                    + " from account " + request.getExistingBAN() + " to account "
                                                    + newAccount.getBAN(), ioe));
                                }
                            }
                        }
                    }
                }
            });
        }
        catch (Exception ex)
        {
            throw new MoveException(request, "Unable to copy files ", ex);
        }
        return copiedFiles;
    }


    private void copyAttachmentRecords(Context ctx, AMR request, Set<String> copiedFiles)
    {
        Account newAccount = request.getNewAccount(ctx);
        
        Home attachmentHome = AccountAttachmentSupport.getBanAttachmentsHome(ctx, request.getExistingBAN());
        
        Collection<AccountAttachment> attachments = null;
        try
        {
            attachments = attachmentHome.selectAll(ctx);
        }
        catch (HomeException e)
        {
            request.reportWarning(ctx, new MoveWarningException(request, "Error retrieving attachment records for account (BAN=" + request.getExistingBAN() + ").  Attachments may not have been moved properly.", e));
        }
        
        if (attachments != null)
        {
            for (AccountAttachment oldAttachment : attachments)
            {
                if (copiedFiles.contains(oldAttachment.getFileLocation()))
                {
                    try
                    {   
                        AccountAttachment newAttachment = (AccountAttachment) XBeans.instantiate(AccountAttachment.class, ctx);
                        String newKey = newAttachment.getAttachmentKey();
                        XBeans.copy(ctx, oldAttachment, newAttachment);
                        newAttachment.setAttachmentKey(newKey);
                        newAttachment.setBAN(newAccount.getBAN());
                        attachmentHome.create(ctx, newAttachment);
                    }
                    catch (Exception e)
                    {
                        request.reportWarning(ctx, new MoveWarningException(request, "Error moving attachment record (ID=" + oldAttachment.ID() + ") for account (BAN=" + oldAttachment.getBAN() + ").", e));
                    }   
                }
                else
                {
                    request.reportWarning(ctx, new MoveWarningException(request, "Skipping moving of attachment record (ID=" + oldAttachment.ID() + ") for account (BAN=" + oldAttachment.getBAN() + ") because the physical file was not successfully moved."));
                }
            }
        }
    }

    
    private void copyFile(File srcFile, File destFile) throws IOException
    {
        if (!srcFile.isDirectory()
                && srcFile.exists())
        {
            if (!destFile.exists())
            {
                destFile.getParentFile().mkdirs();
                destFile.createNewFile();
            }
            
            InputStream in = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(destFile); 
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];

            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}
