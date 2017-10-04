/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.api.rmi.impl;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorsInvocationHandler;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.TestSetupAccountHierarchy;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.InvoiceServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;


/**
 * Tests for API methods.
 * 
 * @author kumaran.sivasubramaniam@redknee.com
 */
public class InvoicesImplTest extends ContextAwareTestCase
{

    /**
     * Constructs a test case with the given name.
     * 
     * @param name
     *            The name of the test.
     */
    public InvoicesImplTest(final String name)
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
        final TestSuite suite = new TestSuite(InvoicesImplTest.class);
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
        impl_ = (InvoiceServiceSkeletonInterface) QueryExecutorsInvocationHandler.newInstance(ctx, InvoiceServiceSkeletonInterface.class);
        try
        {
            // Setup default Service Provider
            TestSetupAccountHierarchy.setupSpid(getContext());
        }
        catch (HomeException e)
        {
            throw new IllegalStateException("Cannot continue with test. Failed SPID setup for test.", e);
        }
        home_ = new CallDetailTransientHome(ctx);
        ctx.put(CallDetailHome.class, home_);
        header_ = new CRMRequestHeader();
    }


    public void testListCallDetails() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();
    }


    public void testAllListCallDetails() throws HomeException, CRMExceptionFault, IOException, InstantiationException
    {
        final Context ctx = getContext();
    }

    private Home home_;
    private InvoiceServiceSkeletonInterface impl_;
    private CRMRequestHeader header_;
}
