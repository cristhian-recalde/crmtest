/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.pos;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;

/**
 * Shared Class with Utilities to help write POS files.
 * @author Angie Li
 */
public class POSFileWriteUtils 
{

    /**
     * Creates the file (with the ".gz" extension) for the processor if it doesn't exist.
     * If the file exists, it is deleted and newly created.  It is important to delete the 
     * existing csv file, because we don't want it to be loaded by CRM. 
     * 
     * Returns the file name and path.
     * 
     * @param ctx
     * @return
     * @throws IOException
     */
    public String initFileWriter(Context ctx, PointOfSaleFileWriter processor) 
    {
        PrintWriter writer = null;
        PointOfSaleConfiguration config = (PointOfSaleConfiguration) ctx.get(PointOfSaleConfiguration.class);
        
        String path = ".";
        if (config != null && config.getRepositoryDirectory().trim().length() > 0 )
        {
            path = config.getRepositoryDirectory().trim();
        }
        
        File dir = new File(path);
        
        if (!dir.exists())
        {
            dir.mkdir();
        }
        //Append the ".gz" extension on to the end of the filename
        String filePath = path + File.separator + processor.getFileName(ctx) + POS_FILE_EXTENSION;
        File subFile = new File(filePath);

        if (subFile.exists())
        {
            subFile.delete();
        }
        try
        {
            subFile.createNewFile();
        }
        catch (IOException ioe)
        {
            Exception e = new IOException("Error creating POS Cashier file.");
            e.initCause(ioe);
            getLogger().thrown(e);
        }
        
        return filePath;
    }
    
    /**
     * Returns the Log File Writer for this processor.
     * @return
     */
    public POSLogWriter getLogger()
    {
        return logWriter_;
    }
    
    protected POSLogWriter logWriter_;
    private static final String POS_FILE_EXTENSION = ".gz";
}
