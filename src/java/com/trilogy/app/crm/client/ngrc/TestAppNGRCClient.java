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
package com.trilogy.app.crm.client.ngrc;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.rmi.impl.CallDetailsImplTest.FakeAuthSPI;
import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.config.NGRCClientConfig;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;

/**
 * Unit test for AppNGRCClient.
 * 
 * @author amahmood
 * @since 8.5
 */
public class TestAppNGRCClient extends ContextAwareTestCase
{
    
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestAppNGRCClient(final String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * {@inheritDoc}
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestAppNGRCClient.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();
        
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        LogSupport.setSeverityThreshold(ctx, SeverityEnum.DEBUG);
        
        //Setup config
        NGRCClientConfig config = new NGRCClientConfig();
        config.setURL("http://skinner-z3:11206/RedkneeSoap/services/SubscriberProv63.SubscriberProv63HttpSoap11Endpoint/");
        config.setUsername("rkadm");
        config.setPassword("rkadm");
        
        ctx.put(NGRCClientConfig.class, config);
        
        
    }
 
    @SuppressWarnings("unchecked")
    public void testConfigSuccessCodes() throws ClientException 
    {
        final Context ctx = getContext();
        
        NGRCClientConfig config = (NGRCClientConfig)ctx.get(NGRCClientConfig.class);
        
        assertTrue("Default success code not set", config.isInSuccessCodes(0));

        config.getSuccessCodes().add(new LongHolder(10));
        config.getSuccessCodes().add(new LongHolder(555333111));
        config.getSuccessCodes().add(new LongHolder(-667));
        
        assertTrue(config.isInSuccessCodes(10));
        assertTrue(config.isInSuccessCodes(555333111));
        assertTrue(config.isInSuccessCodes(-667));
        assertFalse(config.isInSuccessCodes(100));
        assertFalse(config.isInSuccessCodes(1555333111));
        assertFalse(config.isInSuccessCodes(-1));
        
    }
    
    public void testProxy() throws ClientException 
    {
        final Context ctx = getContext();
        final List<String> called = new ArrayList<String>();
    
        AppNGRCClient testClient = generateTestClient(called);
        
        AppNGRCClient debugClient = new AppNGRCClientProxy(ctx, testClient);
        debugClient.addOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, null, false);
        debugClient.deleteOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, 0);
        
        final List<String> exptected = Arrays.asList(new String[] {"deleteOptIn", "addOptIn"});
        assertTrue("Not all methods called in DEBUG client", exptected.containsAll(called));
    }

    public void testDebugLogProxy() throws ClientException 
    {
        final Context ctx = getContext();
        final List<String> called = new ArrayList<String>();

        AppNGRCClient testClient = generateTestClient(called);
        
        AppNGRCClient debugClient = new AppNGRCClientDebug(ctx, testClient);
        debugClient.addOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, null, false);
        debugClient.deleteOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, 0);
        
        final List<String> exptected = Arrays.asList(new String[] {"deleteOptIn", "addOptIn"});
        assertTrue("Not all methods called in DEBUG client", exptected.containsAll(called));
    }

    public void testPMLogProxy() throws ClientException 
    {
        final Context ctx = getContext();
        final List<String> called = new ArrayList<String>();

        AppNGRCClient testClient = generateTestClient(called);
        
        AppNGRCClient debugClient = new AppNGRCClientPM(ctx, testClient);
        debugClient.addOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, null, false);
        debugClient.deleteOptIn(ctx, "9056252005", 1, -1, -1, -1, 7010, 0);
        
        final List<String> exptected = Arrays.asList(new String[] {"deleteOptIn", "addOptIn"});
        assertTrue("Not all methods called in DEBUG client", exptected.containsAll(called));
    }
    
    public void testSOAPClient() throws ClientException, AgentException 
    {
        final Context ctx = getContext();

        AppNGRCClient client = generateSOAPClient(ctx);
        
        client.addOptIn(ctx, "9056252006", 1, -1, -1, -1, 7010, null, false);
        client.deleteOptIn(ctx, "9056252006", 1, -1, -1, -1, 7010, 0);
        
        //Pass
    }
    
    
    private AppNGRCClient generateTestClient(final List<String> methodsCalled)
    {
        AppNGRCClient testClient = new AppNGRCClient(){
            public void deleteOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
                    int roamingRatePlan, int roamingOptIn, Integer delayedOptOut) throws ClientException
            {
                methodsCalled.add("deleteOptIn");
            }
            public void addOptIn(Context ctx, String msisdn, int spid, int baseRatePlan, int baseOptIn,
                    int roamingRatePlan, int roamingOptIn, String deviceType, boolean confirmationRequired)
                    throws ClientException
            {
                methodsCalled.add("addOptIn");
            }

            @Override
            public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
            {
                methodsCalled.add("updateImsi");
            }
        };
        return testClient;
    }

    private AppNGRCClientPM generateSOAPClient(final Context ctx) throws AgentException
    {
        return new AppNGRCClientPM(ctx, 
                                        new AppNGRCClientDebug(ctx, 
                                            new AppNGRCClientImpl(ctx)));
    }
    
    public void updateImsi(Context ctx, String oldIMSI, String newIMSI) throws ClientException
    {
    }
    
}
