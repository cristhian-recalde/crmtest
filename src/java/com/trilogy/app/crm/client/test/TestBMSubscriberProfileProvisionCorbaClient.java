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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author rchen
 *
 */
public final class TestBMSubscriberProfileProvisionCorbaClient extends ContextAwareTestCase
{
    // This profile exists only on shelby
    private static final String TEST_MSISDN = "123456501";


    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestBMSubscriberProfileProvisionCorbaClient.class);
        
        return suite;
        
    }
    

    public void testGetSubscriptionProfile()
    {
        Context ctx = getContext();
        SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(ctx);
        
        assertNotNull(client);
        
        Subscriber subscription = new Subscriber();
        subscription.setMSISDN(TEST_MSISDN);
        subscription.setId("001");
        subscription.setSubscriptionType(1);
        try
        {
            Parameters result = client.querySubscriptionProfile(ctx, subscription);
            assertEquals(TEST_MSISDN, result.getMsisdn());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
