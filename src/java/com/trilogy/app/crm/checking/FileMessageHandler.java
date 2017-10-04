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
package com.trilogy.app.crm.checking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Provides a file-based MessageHandler.  The directory is given, and the name
 * of the file is derived from the date and time.  For example, a file created
 * in the late afternoon of October 18th, in the directory "/tmp/diva" would
 * look like "/tmp/diva/2004-10-18_17:16:43.txt".
 *
 * @author gary.anderson@redknee.com
 */
public
class FileMessageHandler
    implements MessageHandler
{
    /**
     * Creates a new FileMessageHandler for the given directory.
     *
     * @param directoryName The name of the directory into which the report will
     * be written.
     *
     * @exception IOException Thrown if there are problems accessing the file.
     */
    public FileMessageHandler(final String directoryName)
        throws IOException
    {
        this(new File(directoryName));
    }


    /**
     * Creates a new FileMessageHandler for the given directory.
     *
     * @param directory The directory into which the report will be written.
     *
     * @exception IOException Thrown if there are problems accessing the file.
     */
    public FileMessageHandler(final File directory)
        throws IOException
    {
        // TODO - 2004-10-18 - Check the file validity and permissions.
        directory.mkdirs();

        final File file = createFile(directory);

        final FileWriter fileWriter = new FileWriter(file);
        final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        writer_ = new PrintWriter(bufferedWriter);
    }


    /**
     * Flushes and closes the file.
     */
    public void close()
    {
        writer_.flush();
        writer_.close();
    }


    /**
     * {@inheritDoc}
     */
    public void print(final String message)
    {
        writer_.println(message);
    }


    /**
     * Creates a file name suitable for use by this handler.
     *
     * @param directory The directory into which the report will be written.
     * @return A file name suitable for use by this handler.
     */
    private static File createFile(final File directory)
    {
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        final String timestamp = formatter.format(new Date());
        return new File(directory, timestamp + ".txt");
    }


    /**
     * The writer to which this handler delegates.
     */
    private final PrintWriter writer_;

} // class
