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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bulkloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.trilogy.framework.xhome.csv.CSVIterator;
import com.trilogy.framework.xhome.csv.CSVSupport;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * CVSIterator for the Bulk Load Utility.
 * Emulates the com.redknee.framework.xhome.csv.CSVIterator.
 * A new method was needed to return the line (String) being processed
 * by the CSVIterator: getLine()
 *
 * @author ali@redknee.com
 */
public class BulkLoadCSVIterator extends CSVIterator
{
    private FileChannel fileChannel_;
    
    public BulkLoadCSVIterator(final CSVSupport csvSupport, final FileChannel fileChannel, final String filename) throws HomeException
    {
        super(csvSupport, filename, DEFAULT_SEPERATOR);
        fileChannel_ = fileChannel;
        updateIn();
    }

    public BulkLoadCSVIterator(final CSVSupport csvSupport, final String filename, final FileChannel fileChannel, final char seperator)
        throws HomeException
    {
        super(csvSupport, filename, seperator);
        fileChannel_ = fileChannel;
        updateIn();
    }

    public String getLine()
    {
        return (String) next_;
    }

    protected void updateIn() throws HomeException
    {
       try
       {
           if (fileChannel_!=null)
           {
               if (in_!=null)
               {
                   in_.close();
               }
               in_ = new BufferedReader(Channels.newReader(fileChannel_, Charset.defaultCharset().name()));
               next_ = load();
           }
       }
       catch (FileNotFoundException e)
       {
          throw new HomeException("FileNotFound");
       }
       catch (Exception e)
       {
          throw new HomeException(e);
       }
    }

}
