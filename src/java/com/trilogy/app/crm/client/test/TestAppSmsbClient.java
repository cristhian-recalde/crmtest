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

import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.smsb.dataserver.smsbcorba.subsProfile7;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author rchen
 *
 */
public final class TestAppSmsbClient extends ContextAwareTestCase
{
    private static final String BAN = "1234567";
    private static final String MSISDN = "9990010000";

    public static Test suite(final Context ctx)
    {
        setParentContext(ctx);
        
        final TestSuite suite = new TestSuite(TestAppSmsbClient.class);
        
        return suite;
        
    }

    public void testSubscriberProvisioning()
    {
        Context ctx = getContext();
        AppSmsbClient client = (AppSmsbClient) ctx.get(AppSmsbClient.class);
        
        assertNotNull(client);
        
        subsProfile7 profile = new subsProfile7();
        profile.ban = BAN;
        profile.barringplan = 0;
        profile.billcycledate = 2;
        profile.birthdate = "";
        profile.enable = true;
        profile.eqtype = "G";
        profile.gender = "";
        profile.groupMsisdn = MSISDN;
        profile.hlrid = 0;
        profile.imsi = MSISDN;
        profile.incomingSmsCount = "";
        profile.language = "E";
        profile.location = "";
        profile.msisdn = MSISDN;
        profile.outgoingSmsCount = "0";
        profile.ratePlan = 0;
        profile.recurDate = 0;
        profile.scpid = 0;
        profile.spid = 1;
        profile.svcGrade = 3;
        profile.svcid = 6;
        profile.TzOffset = 0;
        
        int result = client.addSubscriber(profile);
        
        assertEquals(0, result);
        
        profile = client.getSubsProfile(MSISDN);
        
        assertEquals(MSISDN, profile.msisdn);
        assertEquals(BAN, profile.ban);
        
        result = client.deleteSubscriber(MSISDN);
        
        assertEquals(0, result);
    }
    
}
