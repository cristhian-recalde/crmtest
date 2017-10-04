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

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.io.FileSupport;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.FileUploadDownloadWebControl;

import com.trilogy.app.crm.bean.FileAware;
import com.trilogy.service.http.MultipartWrapper;
import com.trilogy.service.http.catalina.CatalinaEntity;


/**
 * @author simar.singh@redknee.com [A web-control that loads a local file (via browser) to
 *         system and Populates the location of the uploaded file in the FileAware bean.
 *         Class object of the Generic type is included in constructors to improve
 *         compile-time adherence. If the bean is not FileAware, the use of this
 *         WebControl will result in ClassCastException]
 */
public class FileAwareUploadDownloadWebControl<BEAN extends AbstractBean & FileAware>
        extends
            FileUploadDownloadWebControl
{

    /**
     * Create a new File upload webcontrol.
     */
    public FileAwareUploadDownloadWebControl(Class<? extends BEAN> classObject)
    {
        super();
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param cmd
     *            command
     */
    public FileAwareUploadDownloadWebControl(String cmd, Class<? extends BEAN> classObject)
    {
        super(cmd);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param maxWidth
     *            max width of the field
     */
    public FileAwareUploadDownloadWebControl(int maxWidth, Class<? extends BEAN> classObject)
    {
        super(maxWidth);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param displayWidth
     *            display width of the field
     * @param maxWidth
     *            max width of the field
     */
    public FileAwareUploadDownloadWebControl(int displayWidth, int maxWidth, Class<? extends BEAN> classObject)
    {
        super(displayWidth, maxWidth);
    }


    /**
     * Create a new File upload webcontrol.
     * 
     * @param displayWidth
     *            display width of the field
     * @param maxWidth
     *            max width of the field
     * @param cmd
     *            command to process
     */
    public FileAwareUploadDownloadWebControl(int displayWidth, int maxWidth, String cmd,
            Class<? extends BEAN> classObject)
    {
        super(displayWidth, maxWidth, cmd);
    }


    /**
     * Process file for upload
     * 
     * @see com.redknee.framework.xhome.webcontrol.FileUploadWebControl#processFile(com.redknee.framework.xhome.context.Context,
     *      java.io.PrintWriter, java.io.File, com.redknee.service.http.MultipartWrapper)
     */
    @Override
    public void processFile(Context ctx, PrintWriter out, File file, MultipartWrapper multi) throws IOException
    {
        BEAN bean = (BEAN) ctx.get(AbstractWebControl.BEAN);
        CatalinaEntity entity = (CatalinaEntity) ctx.get(CatalinaEntity.class);
        long fileLength = file.length();
        if (entity != null && fileLength > entity.getMaxFileUploadSize())
        {
            throw new IOException("File size of [" + fileLength + " (bytes)] exceeds maximum file size limit of ["
                    + entity.getMaxFileUploadSize() + " (bytes)]");
        }
        
        final String fileNamePrefix;
        final String fileNameSuffix;
        {
            String fileName = file.getName();
            int fileNamePeriodIndex = fileName.lastIndexOf('.');
            if (fileNamePeriodIndex > 0 && fileNamePeriodIndex <= fileName.length())
            {
                fileNamePrefix = fileName.substring(0, fileNamePeriodIndex);
                fileNameSuffix = fileName.substring(fileNamePeriodIndex);
            } else
            {
                fileNamePrefix = fileName;
                fileNameSuffix = "";
            }

        }
        
        
        File newFile = File.createTempFile(fileNamePrefix, fileNameSuffix);
        FileSupport.copy(ctx, file, newFile, true);
        bean.setFileLocation(newFile.getAbsolutePath());
        // no need to delete the original source file, the super.super
        // [FileUploadWebControl] does it after completing the call to this
        // function
    }
    
    //Overriding because the super from fw does not add hide=border in menu link
    @Override
    public String toDisplayString(Context ctx, Object obj)
    {
        if ( obj == null || obj.toString().length() == 0 )
            return "";

        Object bean = ctx.get(AbstractWebControl.BEAN);
        IdentitySupport identitySupport = (IdentitySupport) XBeans.getInstanceOf(ctx, bean.getClass(), IdentitySupport.class);
        String id = identitySupport.ID(bean).toString();

        Context subCtx = ctx.createSubContext();
        WebAgents.setDomain(subCtx, "");

        Link link = new Link(subCtx);
        link.add("key", id); 
        link.add("cmd", getCmd());
        // hide the menu, else the response will be corrupted and 
        // the browser will not pop up the save-as dialog.
        link.add("menu", "hide");
        link.add("border", "hide");

        StringBuffer buffer = new StringBuffer(128);
        link.writeLink(buffer, obj.toString());
        return buffer.toString();
    }
}
