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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AbstractSubscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;
import com.trilogy.app.crm.bean.BillCycleTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionTestClient;
import com.trilogy.app.crm.client.urcs.UrcsClientInstall;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;

/**
 * A suite of test cases for SubscriberSupport.
 *
 * @author jimmy.ng@redknee.com
 */
public class TestSubscriberSupport
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestSubscriberSupport(final String name)
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

        final TestSuite suite = new TestSuite(TestSubscriberSupport.class);

        return suite;
    }


    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();
        
        // Set up the Test Subscriber.
        testSubscriber_ = new Subscriber();
        testSubscriber_.setId("123");
        testSubscriber_.setBAN("123");
        testSubscriber_.setMSISDN("123");
        testSubscriber_.setState(SubscriberStateEnum.ACTIVE);
    }


    // INHERIT
    @Override
    public void tearDown()
    {
        // Tear down the Test Subscriber.
        testSubscriber_ = null;
        
        super.tearDown();
    }


    /**
     * Tests that the getBillCycleDay() method works according to the intent.
     * 
     * @exception HomeException Thrown if failed to create any test object.
     */
    public void testGetBillCycleDay()
        throws HomeException
    {
        // First create an AccountHome in the context.
        final Home acctHome = new AccountTransientHome(getContext());
        getContext().put(AccountHome.class, acctHome);
        
        try
        {
            SubscriberSupport.getBillCycleDay(getContext(), testSubscriber_);
             
            fail("HomeException should have thrown because no Account with BAN \""
                    + testSubscriber_.getBAN()
                    + "\".");
        }
        catch (HomeException e)
        {
            // Do nothing
        }
        
        // Create a test account in the AccountHome.
        final Account acct = new Account();
        acct.setBAN(testSubscriber_.getBAN());
        acct.setBillCycleID(123);
        acctHome.create(getContext(),acct);
        
        try
        {
            SubscriberSupport.getBillCycleDay(getContext(), testSubscriber_);
            
            fail("IllegalStateException should have thrown because no BillCycleHome.");
        }
        catch (IllegalStateException e)
        {
            // Do nothing
        }
        
        // Create a BillCycleHome in the context.
        final BillCycleHome bcHome = new BillCycleTransientHome(getContext());
        getContext().put(BillCycleHome.class, bcHome);
        
        try
        {
            SubscriberSupport.getBillCycleDay(getContext(), testSubscriber_);
            
            fail("HomeException should have thrown because no BillCycle with ID "
                    + acct.getBillCycleID()
                    + ".");
        }
        catch (HomeException e)
        {
            // Do nothing
        }
        
        // Create a BillCycle with
        // 1) the same BillCycleID as that in the account, and
        // 2) an expected BillCycle day.
        final int expectedDay = 10;
        final BillCycle bc = new BillCycle();
        bc.setBillCycleID(acct.getBillCycleID());
        bc.setDayOfMonth(expectedDay); 
        bcHome.create(getContext(),bc);
        
        assertEquals(
                "Simple Test Case if no exception occurred",
                expectedDay,
                SubscriberSupport.getBillCycleDay(getContext(), testSubscriber_));
    }
    
    public void testUpdateSubscriberSummaryABM()
    {
        Context ctx = getContext();
        
        ctx.put(UrcsClientInstall.SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY, new SubscriberProfileProvisionTester());
        
        assertEquals("Initial balance should be default.", AbstractSubscriber.DEFAULT_REALTIMEBALANCE, testSubscriber_.getRealTimeBalance());
        assertEquals("Initial credit limit should be zero.", AbstractSubscriber.DEFAULT_ABMCREDITLIMIT, testSubscriber_.getAbmCreditLimit());
        
        testSubscriber_.setContext(ctx);
        SubscriberSupport.updateSubscriberSummaryABM(ctx, testSubscriber_);
        
        // Ensure the ABM balanced fetched from ABM is negated to match CRM convention
        assertEquals("Did not fetch correct balance from ABM", 500, testSubscriber_.getRealTimeBalance());
        assertEquals("Did not fetch correct credit limit from ABM", 123, testSubscriber_.getAbmCreditLimit());
    }

    public void testGetSubIdSynchronized() throws HomeException
    {
        final int N = 1000;
        final int M = 10;
        final Context ctx = getContext();
        final Home accountHome = new AccountTransientHome(ctx);

        Thread[] workers = new Thread[M];
        final Random rnd = new Random();

        ctx.put(AccountHome.class, accountHome);
        for (int i = 0; i < M; i++)
        {
            final Account account = new Account();
            account.setSpid(1);
            account.setBAN(Integer.toString((i + 1) * 10));

            accountHome.create(ctx, account);

            workers[i] = new Thread(new SubIdGetter(ctx, rnd, N, M));
        }

        for (int i = 0; i < M; i++)
        {
            workers[i].start();
        }

        for (int i = 0; i < M; i++)
        {
            try
            {
                workers[i].join();
            }
            catch (InterruptedException e)
            {
                LogSupport.debug(ctx, this, "Exception", e);
                break;
            }
        }

        int sum = 0;
        for (int i = 0; i < M; i++)
        {
            String ban = Integer.toString((i + 1) * 10);

            final Account account = (Account) accountHome.find(ctx, ban);
            sum += account.getNextSubscriberId();
        }

        assertEquals(M * N + M, sum);
    }

    class SubIdGetter implements Runnable
    {
        final Context ctx;
        final Random rnd;
        final int N;
        final int M;

        public SubIdGetter(final Context ctx, final Random rnd, int n, int m)
        {
            this.ctx = ctx;
            this.rnd = rnd;
            this.N = n;
            this.M = m;
        }

        public void run()
        {
            for (int i = 0; i < N; i++)
            {
                int k;
                synchronized(rnd)
                {
                    k = rnd.nextInt(M) + 1;
                }
                String ban = Integer.toString(k * 10);
                try
                {
                    SubscriberSupport.acquireNextSubscriberIdentifier(ctx, ban);
                }
                catch (HomeException e)
                {
                    LogSupport.debug(ctx, this, "Exception", e);
                }
            }
        }
    }

    /**
     * A reusable Subscriber.
     */
    private Subscriber testSubscriber_ = null;

}

class SubscriberProfileProvisionTester
    extends SubscriberProfileProvisionTestClient
    {
        /**
         * Creates a new BM testing client. 
         */
        public SubscriberProfileProvisionTester()
        {
            super("SubscriberProfileProvisionTester");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Parameters querySubscriptionProfile(Context context, Subscriber subscription)
        {
            return new Parameters().balance(-500).creditLimit(123);
        }
    }