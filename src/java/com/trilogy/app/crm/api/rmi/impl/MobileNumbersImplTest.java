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
package com.trilogy.app.crm.api.rmi.impl;

import java.io.IOException;
import java.security.Permission;
import java.security.Principal;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorsInvocationHandler;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.MobileNumberServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberQueryResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.mobilenumber.MobileNumberStateEnum;

/**
 * Tests for API methods.
 *
 * @author victor.stratan@redknee.com
 */
public class MobileNumbersImplTest extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public MobileNumbersImplTest(final String name)
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

        final TestSuite suite = new TestSuite(MobileNumbersImplTest.class);

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

        impl_ = (MobileNumberServiceSkeletonInterface) QueryExecutorsInvocationHandler.newInstance(ctx, MobileNumberServiceSkeletonInterface.class);
        home_ = new MsisdnTransientHome(ctx);

        ctx.put(MsisdnHome.class, home_);
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        header_ = new CRMRequestHeader();

    }

    public void testListMobileNumbers() throws CRMExceptionFault, HomeException, IOException, InstantiationException
    {
        final Context ctx = getContext();

        final Msisdn mobileA = (Msisdn) XBeans.instantiate(Msisdn.class, ctx);
        mobileA.setSpid(1);
        mobileA.setGroup(1);
        mobileA.setMsisdn("407220000100");
        final Msisdn mobileB = (Msisdn) XBeans.instantiate(Msisdn.class, ctx);
        mobileB.setSpid(1);
        mobileB.setGroup(1);
        mobileB.setMsisdn("407220000120");

        home_.create(ctx, mobileA);
        home_.create(ctx, mobileB);

        ContextLocator.setThreadContext(ctx);

        final MobileNumberQueryResult result = impl_.listMobileNumbers(header_, 1, MobileNumberStateEnum.AVAILABLE.getValue(), null,
                10, true,null, null);

        assertNotNull(result);
        assertNotNull(result.getReferences());
        assertEquals(2, result.getReferences().length);
    }

    public static class FakeAuthSPI implements AuthSPI
    {
        public void login(final Context ctx, final String username, final String password) throws LoginException
        {
        }

        public void logout(final Context ctx)
        {
        }

        public boolean checkPermission(final Context ctx, final Principal principal, final Permission permission)
        {
            return true;
        }

        public void updatePassword(final Context ctx, final Principal principal, final String oldPassword,
                final String newPassword) throws IllegalStateException
        {
        }

        public void validatePrincipal(final Context ctx, final Principal oldValue, final Principal newValue)
            throws IllegalStateException
        {
        }

        @Override
        public void release()
        {
            
        }
        
    }

    private Home home_;
    private MobileNumberServiceSkeletonInterface impl_;
    private CRMRequestHeader header_;
}
