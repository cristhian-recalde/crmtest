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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.client.test;

import org.omg.CORBA.LongHolder;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author rchen
 */
public final class TestAppOcgClient extends ContextAwareTestCase
{
    // This profile exists only on shelby
    private static final String TEST_MSISDN = "1234567892";

    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestAppOcgClient.class);
        
        return suite;
        
    }

    public void testRequestBalance()
    {
        Context ctx = getContext();
        AppOcgClient client = (AppOcgClient) ctx.get(AppOcgClient.class);
        
        LongHolder outputBalance = new LongHolder();
        LongHolder outputOverdraftBalance = new LongHolder();
        LongHolder outputOverdraftDate = new LongHolder();
        int result = client.requestBalance(TEST_MSISDN, SubscriberTypeEnum.PREPAID, "CAD", false, "", 1, outputBalance,
                outputOverdraftBalance , outputOverdraftDate , null);
        
        assertEquals(0, result);
        assertEquals(10000, outputBalance.value);
    }
}
