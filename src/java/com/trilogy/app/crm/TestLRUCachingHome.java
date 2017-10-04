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
package com.trilogy.app.crm;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.CacheConfigHome;
import com.trilogy.framework.xhome.home.CacheConfigTransientHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.LRUCachingHome;

import com.trilogy.app.crm.bean.BillingMessage;
import com.trilogy.app.crm.bean.core.CreditCategory;
import com.trilogy.app.crm.bean.CreditCategoryTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * This is an example of problems that can are encountered while using LRUCachingHome
 *
 * @author victor.stratan@redknee.com
 */
public class TestLRUCachingHome extends ContextAwareTestCase
{
    private static final int ID = 123;

    public TestLRUCachingHome(final String name)
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
    public static Test suite()
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
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestLRUCachingHome.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    protected void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        ctx.put(CacheConfigHome.class, new CacheConfigTransientHome(ctx));

        storage_ = new CreditCategoryTransientHome(ctx);
        home_ = new LRUCachingHome(ctx, "TestCreditCategory", false, storage_);
    }

    protected void tearDown()
    {
        super.tearDown();
    }

    public void testFindDoesNotClone() throws HomeException
    {
        final Context ctx = getContext();
        final CreditCategory in = new CreditCategory();
        in.setCode(ID);
        in.setDesc("alpha");

        storage_.create(ctx, in);

        final CreditCategory out = (CreditCategory) home_.find(ctx, ID);

        out.setDesc("beta");

        final CreditCategory beta = (CreditCategory) home_.find(ctx, ID);

        out.setDesc("gamma");

        final CreditCategory gamma = (CreditCategory) home_.find(ctx, ID);

        assertEquals("Values retreived from cache should not be different!", beta.getDesc(), gamma.getDesc());
    }

    public void testCreateClones() throws HomeException
    {
        final Context ctx = getContext();
        final CreditCategory in = new CreditCategory();
        in.setCode(ID);
        in.setDesc("alpha");

        home_.create(ctx, in);

        final CreditCategory out = (CreditCategory) home_.find(ctx, ID);

        out.setDesc("beta");

        final CreditCategory beta = (CreditCategory) home_.find(ctx, ID);

        out.setDesc("gamma");

        final CreditCategory gamma = (CreditCategory) home_.find(ctx, ID);

        assertEquals("Values retreived from cache should not be different!", beta.getDesc(), gamma.getDesc());
    }

    public void testCacheDeepClonesBeans() throws HomeException
    {
        final Context ctx = getContext();
        final CreditCategory in = new CreditCategory();
        in.setCode(ID);
        in.setDesc("alpha");
        in.getDunningAction().setName("alpha");

        home_.create(ctx, in);

        final CreditCategory out = (CreditCategory) home_.find(ctx, ID);

        out.getDunningAction().setName("beta");

        final CreditCategory beta = (CreditCategory) home_.find(ctx, ID);

        out.getDunningAction().setName("gamma");

        final CreditCategory gamma = (CreditCategory) home_.find(ctx, ID);

        assertEquals("Values retreived from cache should not be different!",
                beta.getDunningAction().getName(), gamma.getDunningAction().getName());
    }

    public void testCacheShallowClonesCollections() throws HomeException
    {
        final Context ctx = getContext();
        final CreditCategory in = new CreditCategory();
        in.setCode(ID);
        in.setDesc("alpha");
        BillingMessage bm = new BillingMessage();
        bm.setMessage("alpha");
        in.getBillingMessages().add(bm);

        home_.create(ctx, in);

        final CreditCategory out = (CreditCategory) home_.find(ctx, ID);

        bm = (BillingMessage) out.getBillingMessages().get(0);
        bm.setMessage("beta");

        final CreditCategory beta = (CreditCategory) home_.find(ctx, ID);
        final BillingMessage betaBm = (BillingMessage) out.getBillingMessages().get(0);
        final String betaMessage = betaBm.getMessage();

        bm.setMessage("gamma");

        final CreditCategory gamma = (CreditCategory) home_.find(ctx, ID);
        final BillingMessage gammaBm = (BillingMessage) out.getBillingMessages().get(0);
        final String gammaMessage = gammaBm.getMessage();

        assertEquals("Values retreived from cache should not be different!",
                betaMessage, gammaMessage);
    }

    private Home storage_;
    private Home home_;
}
