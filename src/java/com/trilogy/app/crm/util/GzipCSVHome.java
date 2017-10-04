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
package com.trilogy.app.crm.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.csv.CSVSupport;
import com.trilogy.framework.xhome.csv.GenericCSVHome;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * CSV Home that saves to a Gzipped compressed file.
 * 
 * **** This is a temporary fix. *****
 * This class should be supported by the framework.
 * The only thing I have overwritten of the GenericCSVHome was the reallySave
 * method.
 * 
 * The load method won't work since it is using CSVIterator which opens 
 * non-compressed files.  Framework needs to implement the GzipCSVHome to 
 * both WRITE and READ from the CSV file.
 * 
 * Currently, I need this class for Point of Sale (POS) report writing.  No 
 * CSV file reading is done in the POS feature.
 * 
 * NOTE: GzipCSVHome is NOT like GenericCSVHome where you can just call 
 * create(), remove() or store() and expect that the CSV file will be updated.
 * GzipCSVHome requires a call to writeFile(ctx) to actually update the CSV file.
 * 
 * @author Angie Li
 */
public class GzipCSVHome extends GenericCSVHome 
{
    public GzipCSVHome(Context ctx, Home cache, CSVSupport csvSupport, String filename, char seperator)
    throws HomeException
    {
        super(  ctx, cache, csvSupport, filename, seperator);
    }
    
    
    protected synchronized void reallySave(Context ctx) throws HomeException
    {
        /* Do nothing.
         * The GenericCSVHome was preforming a reallySave after every create(), remove() and store().
         * Since this is a customized GzipCSVHome, we may as well optimize it.
         * We won't do anything in the reallySave method during create(), remove() and store().
         * Instead we will have the class that uses GzipCSVHome be responsible for calling
         * the writeFile method after all the beans are done with create(), remove() and store(). 
         */ 
    }
    
    /**
     * This is taken from the reallySave method in GenericCSVHome.
     * Instead of using the FileOutputStream like GenericCSVHome, we use a GZIPOutputStream.
     */
    public synchronized void writeFile(Context ctx) throws HomeException
    {
        try
        {
            GZIPOutputStream gzipOut= new GZIPOutputStream(new FileOutputStream(getFilename()));
            BufferedOutputStream os = new BufferedOutputStream(gzipOut);
            PrintWriter out = new PrintWriter(os);
            StringBuffer buf = new StringBuffer();

            Iterator i = getDelegate().select(ctx, True.instance()).iterator();
            while (i.hasNext())
            {
                Object bean = i.next();
                
                buf.setLength(0);
                // TODO: escape delimiters
                csvSupport_.append(buf, seperator_, bean);
                out.print(buf.toString());
                out.print("\r\n");
            }

            out.close();
            os.close();
        }
        catch (IOException e)
        {
            throw new HomeException("File error during save of [" + getFilename() + "]: " + e.getMessage(), e);
        }
    }
}
