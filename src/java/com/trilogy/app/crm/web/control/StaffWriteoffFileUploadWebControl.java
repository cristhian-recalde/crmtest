/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.app.crm.bean.StaffWriteOffGui;
import com.trilogy.app.crm.bulkloader.StaffWriteOffBulkLoadServicer;
import com.trilogy.app.crm.bulkloader.StaffWriteOffBulkLoadSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.io.FileSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.FileUploadDownloadWebControl;
import com.trilogy.service.http.MultipartWrapper;
import com.trilogy.service.http.catalina.CatalinaEntity;

/**
 * @author cindy.wong@redknee.com
 * 
 */
public class StaffWriteoffFileUploadWebControl extends
        FileUploadDownloadWebControl
{

    /**
     * Create a new File upload webcontrol.
     */
    public StaffWriteoffFileUploadWebControl()
    {
        super();
    }

    /**
     * Create a new File upload webcontrol.
     * 
     * @param cmd
     *            command
     */
    public StaffWriteoffFileUploadWebControl(String cmd)
    {
        super(cmd);
    }

    /**
     * Create a new File upload webcontrol.
     * 
     * @param maxWidth
     *            max width of the field
     */
    public StaffWriteoffFileUploadWebControl(int maxWidth)
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
    public StaffWriteoffFileUploadWebControl(int displayWidth, int maxWidth)
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
    public StaffWriteoffFileUploadWebControl(int displayWidth, int maxWidth,
            String cmd)
    {
        super(displayWidth, maxWidth, cmd);
    }

    /**
     * Process file for upload
     * 
     * @see com.redknee.framework.xhome.webcontrol.FileUploadWebControl#processFile(com.redknee.framework.xhome.context.Context,
     *      java.io.PrintWriter, java.io.File,
     *      com.redknee.service.http.MultipartWrapper)
     */
    @Override
    public void processFile(Context ctx, PrintWriter out, File file,
            MultipartWrapper multi) throws IOException
    {

        StaffWriteOffGui form = (StaffWriteOffGui) ctx
                .get(AbstractWebControl.BEAN);
        CatalinaEntity entity = (CatalinaEntity) ctx.get(CatalinaEntity.class);
        long fileLength = file.length();
        if (entity != null && fileLength > (long) entity.getMaxFileUploadSize())
        {
            throw new IOException("File size of [" + fileLength
                    + " (bytes)] exceeds maximum file size limit of ["
                    + entity.getMaxFileUploadSize() + " (bytes)]");
        }

        File newFile = File.createTempFile("write_off_copy", ".csv");
        FileSupport.copy(ctx, file, newFile, true);

        form.setFullAdjustmentFilePath(newFile.getAbsolutePath());
    }

}
