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

import com.trilogy.app.crm.client.PricePlanMgmtClient;
import com.trilogy.app.crm.client.PricePlanMgmtException;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.urcs.provision.RatePlanInfo;
import com.trilogy.app.urcs.provision.RatePlanType;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author rchen
 *
 */
public final class TestPricePlanMgmtClient extends ContextAwareTestCase
{
    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestPricePlanMgmtClient.class);
        
        return suite;
        
    }
    
    public void testQueryRatePlans()
    {
        Context ctx = getContext();
        PricePlanMgmtClient client = (PricePlanMgmtClient) ctx.get(PricePlanMgmtClient.class);
        try
        {
            RatePlanInfo[] ratePlans = client.queryRatePlans(1, RatePlanType.VOICE, "");
            assertTrue(ratePlans.length>0);
        }
        catch (PricePlanMgmtException e)
        {
            fail("Catch PricePlanMgmtException when querying rate plans.");
        }
    }

}
