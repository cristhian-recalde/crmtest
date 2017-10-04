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
package com.trilogy.app.crm.bas.tps.pipe;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bas.tps.IBISTPSProcessor;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for TestPipeline.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestPipeline extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPipeline(final String name)
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

        final TestSuite suite = new TestSuite(TestPipeline.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        tpsProcessor_ = new TestWrapperTPSProcessor(getContext(), new File("dummy.PMT"));
        tpsRecord_ = new TPSRecord();
    }


    // INHERIT
    public void tearDown()
    {
        tpsRecord_ = null;
        tpsProcessor_ = null;
        
        super.tearDown();
    }


    /**
     * Tests that the pump() method works according to the intent.
     */
    public void testPump()
    {
        // Test 1: If the TPS Pipe is not found in the context, an error message
        //         should be recorded in the TPS record and the error file should
        //         be updated.
        getContext().put(IBISTPSProcessor.class, tpsProcessor_);
        Pipeline.pump(getContext(), tpsRecord_);
        assertEquals(
            "An error message should have been put into the TPS record",
            "can not find TPS pipe in the context",
            tpsRecord_.getLastError());
        assertTrue(
            "The writeErrFile() method should have been invoked due to an exception",
            tpsProcessor_.isWriteErrFileInvoked());
        
        // TODO:  More test cases may be required here.
    }
    
    
    /**
     * Some reusable objects.
     */
    private TestWrapperTPSProcessor tpsProcessor_ = null;
    private TPSRecord tpsRecord_ = null;
    
    
    /**
     * Provides a convenient test wrapper class for TPSProcessor.
     */
    private class TestWrapperTPSProcessor extends IBISTPSProcessor
    {
        public TestWrapperTPSProcessor(final Context context, final File file)
        {
            super(context, file);
        }
        
        public synchronized void writeErrFile(Context ctx)
        {
            writeErrFileInvoked_ = true;
        }

        public boolean isWriteErrFileInvoked()
        {
            return writeErrFileInvoked_;
        }
        
        private boolean writeErrFileInvoked_ = false;
    } // inner-class
}
