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
import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.api.queryexecutor.QueryExecutorsInvocationHandler;
import com.trilogy.app.crm.bean.LoyaltyCard;
import com.trilogy.app.crm.bean.LoyaltyCardHome;
import com.trilogy.app.crm.bean.LoyaltyCardTransientHome;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.auth.AuthSPI;
import com.trilogy.framework.auth.LoginException;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.api.LoyaltyServiceSkeletonInterface;
import com.trilogy.util.crmapi.wsdl.v3_0.types.CRMRequestHeader;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationCreateRequest;
import com.trilogy.util.crmapi.wsdl.v3_0.types.loyalty.LoyaltyProfileAssociationResponse;

/**
 * Tests for API methods.
 *
 * @author victor.stratan@redknee.com
 */
public class LoyaltyImplTest extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public LoyaltyImplTest(final String name)
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

        final TestSuite suite = new TestSuite(LoyaltyImplTest.class);

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
        impl_ = (LoyaltyServiceSkeletonInterface) QueryExecutorsInvocationHandler.newInstance(ctx, LoyaltyServiceSkeletonInterface.class);

        home_ = new LoyaltyCardTransientHome(ctx);

        ctx.put(LoyaltyCardHome.class, home_);
        ctx.put(AuthSPI.class, new FakeAuthSPI());
        ctx.put(Principal.class, new User());

        header_ = new CRMRequestHeader();

    }

    public void testCreateLoyaltyProfileAssociation() throws CRMExceptionFault, HomeException, IOException, InstantiationException
    {
        final Context ctx = getContext();
        ContextLocator.setThreadContext(ctx);

        
        assertTrue(home_.selectAll(ctx).isEmpty());
        
        final LoyaltyProfileAssociationCreateRequest request = new LoyaltyProfileAssociationCreateRequest();
        request.setAccountID("1002345");
        request.setProgramID("standard");
        request.setLoyaltyCardID("001-123456");
        request.setAccumulationEnabled(true);
        request.setRedemptionEnabled(false);
        request.setExpiryDate(CalendarSupportHelper.get(ctx).dateToCalendar(new Date(System.currentTimeMillis() + 1000*60*60l)));
        
        final LoyaltyProfileAssociationResponse result = impl_.createLoyaltyProfileAssociation(header_, request,null);


        /* Until integration with URCS is complete
        assertNotNull(result);
        assertNotNull(result.getAssociation());
        assertEquals(request.getAccountID(), result.getAssociation().getAccountID());
        assertEquals(request.getProgramID(), result.getAssociation().getProgramID());
        assertEquals(request.getLoyaltyCardID(), result.getAssociation().getLoyaltyCardID());
        assertTrue(request.getAccumulationEnabled() == result.getAssociation().getAccumulationEnabled());
        assertTrue(request.getRedemptionEnabled() == result.getAssociation().getRedemptionEnabled());
        */
        
        Collection<LoyaltyCard> cards = HomeSupportHelper.get(ctx).getBeans(ctx, LoyaltyCard.class);
        LoyaltyCard card = cards.iterator().next();
        assertNotNull(card);
        assertEquals(request.getAccountID(), card.getBAN());
        assertEquals(request.getProgramID(), card.getProgramID());
        assertEquals(request.getLoyaltyCardID(), card.getLoyaltyCardID());
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
    private LoyaltyServiceSkeletonInterface impl_;
    private CRMRequestHeader header_;
}
