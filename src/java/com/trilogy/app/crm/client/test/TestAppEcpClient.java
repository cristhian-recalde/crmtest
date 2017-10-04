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

import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.osa.ecp.provision.SubsProfile;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author rchen
 *
 */
public final class TestAppEcpClient extends ContextAwareTestCase
{
    // This profile exists only on shelby
    private static final String TEST_MSISDN = "1234567892";


    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestAppEcpClient.class);
        
        return suite;
        
    }
    
    
    public void testGetSubsProfile()
    {
        Context ctx = getContext();
        AppEcpClient client = (AppEcpClient) ctx.get(AppEcpClient.class);
        SubsProfile sp = client.getSubsProfile(TEST_MSISDN);
        
        assertEquals(TEST_MSISDN, sp.msisdn);
        assertEquals(TEST_MSISDN, sp.imsi);
        assertEquals(1, sp.spid);
        assertEquals(0, sp.ratePlan);
        
        LogSupport.debug(ctx, this, String.format("%s IMSI: %s SPID: %d Rate Plan: %d", sp.msisdn, sp.imsi, sp.spid, sp.ratePlan));
    }
}
