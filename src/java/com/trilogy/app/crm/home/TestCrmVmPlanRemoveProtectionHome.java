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
package com.trilogy.app.crm.home;

import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;

import com.trilogy.app.crm.bean.CrmVmPlan;
import com.trilogy.app.crm.bean.CrmVmPlanHome;
import com.trilogy.app.crm.bean.CrmVmPlanTransientHome;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceTransientHome;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.exception.RemoveException;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * Unit tests for the TestCrmVmPlanRemoveProtectionHome.
 *
 * @author victor.stratan@redknee.com
 */
public class TestCrmVmPlanRemoveProtectionHome extends ContextAwareTestCase
{

    public TestCrmVmPlanRemoveProtectionHome(final String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static junit.framework.Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static junit.framework.Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestCrmVmPlanRemoveProtectionHome.class);

        return suite;
    }

    @Override
    public void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        testObj_ = new CrmVmPlanRemoveProtectionHome(NullHome.instance());

        final Home srvHome = new ServiceTransientHome(ctx);
        ctx.put(ServiceHome.class, srvHome);

        final Home planHome = new CrmVmPlanTransientHome(ctx);
        ctx.put(CrmVmPlanHome.class, planHome);

        vmPlan_ = new CrmVmPlan();
        vmPlan_.setId(123);
        vmPlan_.setName("test vm plan");
        vmPlan_.setDescription("test vm plan");

        try
        {
            planHome.create(ctx, vmPlan_);
        }
        catch (HomeException e)
        {
        }
    }

    @Override
    public void tearDown()
    {
        super.tearDown();

    }

    public void testIsVmPlanUsedByServicesWithNoServices() throws HomeException
    {
        final Context ctx = getContext();
        final boolean result = testObj_.isVmPlanUsedByServices(ctx, vmPlan_);

        assertFalse("VM Plan cannot be used when there are no services", result);
    }

    public void testIsVmPlanUsedByServicesWithNoVmServices() throws HomeException
    {
        final Context ctx = getContext();
        createService(ctx, ServiceTypeEnum.VOICE, vmPlan_.getId() + 5);
        boolean result = testObj_.isVmPlanUsedByServices(ctx, vmPlan_);

        assertFalse("VM Plan cannot be used when there are no vm services", result);

        createService(ctx, ServiceTypeEnum.VOICE, vmPlan_.getId());
        result = testObj_.isVmPlanUsedByServices(ctx, vmPlan_);

        assertFalse("VM Plan cannot be used when there are no vm services", result);
    }

    public void testIsVmPlanUsedByServicesWithNoUsingServices() throws HomeException
    {
        final Context ctx = getContext();
        createService(ctx, ServiceTypeEnum.VOICEMAIL, vmPlan_.getId() + 5);
        final boolean result = testObj_.isVmPlanUsedByServices(ctx, vmPlan_);

        assertFalse("VM Plan cannot be used when there are no services", result);
    }

    public void testIsVmPlanUsedByServicesWithUsingServices() throws HomeException
    {
        final Context ctx = getContext();
        createService(ctx, ServiceTypeEnum.VOICEMAIL, vmPlan_.getId());
        final boolean result = testObj_.isVmPlanUsedByServices(ctx, vmPlan_);

        assertTrue("VM Plan should be used", result);
    }

    public void testNoExceptionOnRemoveWithNoUsingServices() throws HomeException
    {
        final Context ctx = getContext();
        createService(ctx, ServiceTypeEnum.VOICEMAIL, vmPlan_.getId() + 5);

        try
        {
            testObj_.remove(ctx, vmPlan_);
        }
        catch (HomeException e)
        {
            fail("Unexpected Exception");
        }
    }

    public void testExceptionOnRemoveWithUsingServices() throws HomeException
    {
        final Context ctx = getContext();
        createService(ctx, ServiceTypeEnum.VOICEMAIL, vmPlan_.getId());
        try
        {
            testObj_.remove(ctx, vmPlan_);
            fail("Exception should be thrown");
        }
        catch (HomeException e)
        {
            assertTrue("RemoveException should be thrown", e instanceof RemoveException);
        }
    }

    private void createService(final Context ctx, final ServiceTypeEnum serviceType, final long id) throws HomeException
    {
        final Service service = new Service();
        service.setName("test srv " + nextSrvId_);
        service.setID(nextSrvId_++);
        service.setType(serviceType);
        service.setVmPlanId(String.valueOf(id));

        final Home home = (Home) ctx.get(ServiceHome.class);
        home.create(ctx, service);
    }

    private CrmVmPlanRemoveProtectionHome testObj_;
    private CrmVmPlan vmPlan_;
    private long nextSrvId_;
}
