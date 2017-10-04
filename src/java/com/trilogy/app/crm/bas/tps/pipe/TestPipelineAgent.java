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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * A suite of test cases for TestPipelineAgent.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestPipelineAgent extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestPipelineAgent(final String name)
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

        final TestSuite suite = new TestSuite(TestPipelineAgent.class);

        return suite;
    }


    // INHERIT
    public void setUp()
    {
        super.setUp();
        
        pipelineAgent_ = new PipelineAgent(
            new ContextAgent()
            {
                public void execute(Context ctx)
                {
                }
            });
    }


    // INHERIT
    public void tearDown()
    {
        pipelineAgent_ = null;
        
        super.tearDown();
    }


    /**
     * Tests that the fail() method works according to the intent.
     */
    public void testFail()
    {
        final TPSRecord tpsRecord = new TPSRecord();
        final String msg = "Message to be returned";
        final Throwable throwable = new Throwable();
        final int errorCode = 999999;
        
        getContext().put(TPSRecord.class, tpsRecord);
        
        // Invoke the fail() method of the Test PipelineAgent.
        pipelineAgent_.fail(getContext(), this, msg, throwable, errorCode);
        
        // Test 1: The input throwable object should be found in the context
        //         with Exception.class as the key.
        assertEquals(
            "The input throwable object should have been put into the context",
            throwable,
            getContext().get(Exception.class));
        
        // Test 2: The input error code should be found in the context with
        //         TPSPipeConstant.TPS_PIPE_RESULT_CODE as the key.
        assertEquals(
            "The input error code should have been put into the context",
            Integer.valueOf(errorCode),
            getContext().get(TPSPipeConstant.TPS_PIPE_RESULT_CODE));
        
        // Test 3: The input error code should be found in the TPS record.
        assertEquals(
            "The input message should have been put into the TPS record",
            msg,
            tpsRecord.getLastError());
    }
    
    
    /**
     * Some reusable objects.
     */
    private PipelineAgent pipelineAgent_ = null;
}
