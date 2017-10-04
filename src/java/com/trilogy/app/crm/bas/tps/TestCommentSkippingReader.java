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
import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for TestCommentSkippingReader.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestCommentSkippingReader extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestCommentSkippingReader(final String name)
    {
        super(name);
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestCommentSkippingReader.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
    }


    // INHERIT
    public void tearDown()
    {
        super.tearDown();
    }


    /**
     * Tests that the readLine() method works according to the intent.
     */
    public void testReadLine()
    {
        StringReader stringReader = null;
        CommentSkippingReader commentSkippingReader = null;
        
        try
        {
            // Test 1: Simulate the input file has only one comment line.
            stringReader = new StringReader("#comment1");
            commentSkippingReader = new CommentSkippingReader(new BufferedReader(stringReader));
            assertEquals(
                "No line should be returned",
                null,
                commentSkippingReader.readLine());
            
            // Test 2: Simulate the input file has three lines and only the
            //         second line is NOT a comment line.
            stringReader = new StringReader("#comment1\nline1\n#comment2");
            commentSkippingReader = new CommentSkippingReader(new BufferedReader(stringReader));
            assertEquals(
                "The second line should be returned",
                "line1",
                commentSkippingReader.readLine());
        }
        catch (IOException e)
        {
            // Do nothing
        }
    }
}
