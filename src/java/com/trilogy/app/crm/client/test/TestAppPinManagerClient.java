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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.client.AppPinManagerClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author rchen
 *
 */
public final class TestAppPinManagerClient extends ContextAwareTestCase
{
    private static final String MSISDN = "9990010000";

    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestAppPinManagerClient.class);
        
        return suite;
        
    }

    public void testPinProvision()
    {
        Context ctx = getContext();
        AppPinManagerClient client = (AppPinManagerClient) ctx.get(AppPinManagerClient.class);
        
        assertNotNull(client);
        
        Account testAccount = new Account();
        testAccount.setSpid(1);
        testAccount.setBAN("1");
        
        short result = client.generatePin(ctx, MSISDN, testAccount,"Junit- Class[ " + this.getClass().getName() + "]. Method [testPinProvision]");
        
        assertEquals(0, result);
        
        result = client.deletePin(ctx, MSISDN, "");
        
        assertEquals(0, result);
    }

}
