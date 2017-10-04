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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import com.trilogy.app.crm.util.GzipCSVHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * IVR Extract GZIP CSV Home.
 * 
 * The class com.redknee.app.crm.util.GzipCSVHome doesn't support loading from 
 * a CSV file.  It is important that this file be empty before initializing  
 * POSIVRExtractGzipCSVHome.
 * 
 * @author Angie Li
 */
public class POSIVRExtractGzipCSVHome extends GzipCSVHome 
{

	public POSIVRExtractGzipCSVHome(Context ctx, String filename)
	throws HomeException
	{
		this(ctx, filename, com.redknee.framework.xhome.csv.Constants.DEFAULT_SEPERATOR);
	}
	
	public POSIVRExtractGzipCSVHome(Context ctx, String filename, char seperator)
	throws HomeException
	{
		super(
				ctx,
				new POSIVRExtractTransientHome(ctx),
				POSIVRExtractCustomizedCSVSupport.getInstance(),
				filename,
				seperator);
	}
	
	/**
     * This is taken from the reallySave method in GenericCSVHome.
     * Instead of using the FileOutputStream like GenericCSVHome, we use a GZIPOutputStream.
     */
    @Override
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
                //TT6032032172 IVR File will have continuous lines, no carriage return or line feed. 
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
