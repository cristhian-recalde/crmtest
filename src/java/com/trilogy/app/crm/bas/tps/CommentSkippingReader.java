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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bas.tps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;


/**
 * This class creates a stream that skips all comment lines (that start with '#').
 *
 * @author jimmy.ng@redknee.com
 */
public class CommentSkippingReader extends BufferedReader
{

    /**
     * Creates a new CommentSkippingReader.
     *
     * @param in
     *            A buffered reader.
     */
    public CommentSkippingReader(final Reader in)
    {
        super(in);
    }


    /**
     * Read the next non-comment line of text. A line is considered to be a comment if it
     * starts with the character '#'.
     *
     * @return String The next non-comment line.
     */
    @Override
    public String readLine() throws IOException
    {
        String line = super.readLine();
        while (line != null && line.trim().startsWith("#"))
        {
            line = readLine();
        }

        return line;
    }
}
