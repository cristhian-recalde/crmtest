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
package com.trilogy.app.crm.elang;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xdb.XStatement;

import com.trilogy.app.crm.bean.IPCGDataXInfo;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

public class TestOracleIn extends ContextAwareTestCase
{
    public TestOracleIn(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     * 
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }
    
    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     * 
     * @param context
     *            The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);
        final TestSuite suite = new TestSuite(TestOracleIn.class);
        return suite;
    }
    
    public void setUp()
    {
        super.setUp();
        bufferIds = new HashSet(2000);
        for (int i=1; i<2000; i++)
        {
            bufferIds.add(Long.valueOf(i));
        }
    }
    
    //  INHERIT
    public void tearDown()  
    {
        bufferIds = null;
        super.tearDown();
    }
    
    public void testOracleIn()
    {
        XStatement oracleIn= new OracleIn(IPCGDataXInfo.IPCGDATA_ID, bufferIds);
        String statement = oracleIn.createStatement(getContext());
        int count = countRepetitionsOfIn(statement);
        assertTrue(count == 3);
    }

    private int countRepetitionsOfIn(String statement)
    {
        int count = 0;
        int pos = statement.indexOf("IN");
        if (pos > 0)
        {
            count++;
            statement.substring(pos+1);
        }
        return count;
    }
    
    static Set<Long> bufferIds;
}
