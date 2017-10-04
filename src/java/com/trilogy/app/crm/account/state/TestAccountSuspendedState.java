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
package com.trilogy.app.crm.account.state;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceHome;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.AccountHome;

import org.junit.Before;

/**
 * Unit tests for Subscription.
 *
 * @author victor.stratan@redknee.com
 */
public class TestAccountSuspendedState extends ContextAwareTestCase
{
    /**
     * Creates a new TestService.
     *
     * @param name The name of the set of tests.
     */
    public TestAccountSuspendedState(final String name)
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

        final TestSuite suite = new TestSuite(TestAccountSuspendedState.class);

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    protected void setUp()
    {
        super.setUp();

        final Context ctx = getContext();

        account_ = new Account();
        account_.setBAN(BAN);
        account_.setContext(ctx);

        final Account parentAccount = new Account();
        parentAccount.setBAN(PARENT_BAN);
        parentAccount.setContext(ctx);

        state_ = new AccountSuspendedState();

        config_ = new SysFeatureCfg();
        ctx.put(SysFeatureCfg.class, config_);

        config_.setSupportsInCollection(true);

        Home home = new AccountTransientHome(ctx);
        ctx.put(AccountHome.class, home);

        try
        {
            home.create(ctx, parentAccount);
        }
        catch (HomeException e)
        {
            throw new IllegalStateException("Test unable to innitialize");
        }
    }

    /**
     * Tests that System Feature Configuration disables InCollection Support then the state will not be allowed.
     */
    public void testInCollectionNotPermitedForManualWithConfigDisabled()
    {
        config_.setSupportsInCollection(false);
        account_.setSystemType(SubscriberTypeEnum.POSTPAID);
        account_.setResponsible(true);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        for (final Iterator iter = allowed.iterator(); iter.hasNext();)
        {
            final AccountStateEnum stateEnum = (AccountStateEnum) iter.next();
            assertFalse("Allowed states should not contain InCollection state when SupportsInCollection is false",
                    AccountStateEnum.IN_COLLECTION.equals(stateEnum));
        }
    }

    /**
     * Tests that InCollection is not available when account is postpaid non-responsible.
     */
    public void testInCollectionNotPermitedForManualPostpaidNonresponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.POSTPAID);
        account_.setParentBAN(PARENT_BAN);
        account_.setResponsible(false);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        for (final Iterator iter = allowed.iterator(); iter.hasNext();)
        {
            final AccountStateEnum stateEnum = (AccountStateEnum) iter.next();
            assertFalse("Allowed states should not contain InCollection state when account is postpaid non-responsible",
                    AccountStateEnum.IN_COLLECTION.equals(stateEnum));
        }
    }

    /**
     * Tests that InCollection is available when account is postpaid responsible.
     */
    public void testInCollectionPermitedForManualPostpaidResponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.POSTPAID);
        account_.setResponsible(true);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        final Iterator iter = allowed.iterator();
        assertEquals("At least one state should be returned", true, iter.hasNext());
        AccountStateEnum aState = null;
        for (; iter.hasNext();)
        {
            aState = (AccountStateEnum) iter.next();
            if (AccountStateEnum.IN_COLLECTION.equals(aState))
            {
                break;
            }
        }
        assertEquals("Allowed states should contain InCollection state",
                AccountStateEnum.IN_COLLECTION, aState);
    }

    /**
     * Tests that InCollection is not available when account is postpaid non-responsible.
     */
    public void testInCollectionNotPermitedForManualHybridNonresponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.HYBRID);
        account_.setParentBAN(PARENT_BAN);
        account_.setResponsible(false);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        for (final Iterator iter = allowed.iterator(); iter.hasNext();)
        {
            final AccountStateEnum stateEnum = (AccountStateEnum) iter.next();
            assertFalse("Allowed states should not contain InCollection state when account is postpaid non-responsible",
                    AccountStateEnum.IN_COLLECTION.equals(stateEnum));
        }
    }

    /**
     * Tests that InCollection is available when account is postpaid responsible.
     */
    public void testInCollectionPermitedForManualHybridResponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.HYBRID);
        account_.setResponsible(true);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        final Iterator iter = allowed.iterator();
        assertEquals("At least one state should be returned", true, iter.hasNext());
        AccountStateEnum aState = null;
        for (; iter.hasNext();)
        {
            aState = (AccountStateEnum) iter.next();
            if (AccountStateEnum.IN_COLLECTION.equals(aState))
            {
                break;
            }
        }
        assertEquals("Allowed states should contain InCollection state",
                AccountStateEnum.IN_COLLECTION, aState);
    }

    /**
     * Tests that InCollection is not available when account is prepaid non-responsible.
     */
    public void testInCollectionNotPermitedForManualPrepaidNonresponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.PREPAID);
        account_.setParentBAN(PARENT_BAN);
        account_.setResponsible(false);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        for (final Iterator iter = allowed.iterator(); iter.hasNext();)
        {
            final AccountStateEnum stateEnum = (AccountStateEnum) iter.next();
            assertFalse("Allowed states should not contain InCollection state",
                    AccountStateEnum.IN_COLLECTION.equals(stateEnum));
        }
    }

    /**
     * Tests that InCollection is not available when account is prepaid non-responsible.
     */
    public void testInCollectionNotPermitedForManualPrepaidResponsible()
    {
        account_.setSystemType(SubscriberTypeEnum.PREPAID);
        account_.setResponsible(true);

        final Collection allowed = state_.getStatesPermittedForManualTransition(getContext(), account_);

        for (final Iterator iter = allowed.iterator(); iter.hasNext();)
        {
            final AccountStateEnum stateEnum = (AccountStateEnum) iter.next();
            assertFalse("Allowed states should not contain InCollection state",
                    AccountStateEnum.IN_COLLECTION.equals(stateEnum));
        }
    }

    public static final String BAN = "123";
    public static final String PARENT_BAN = "124";
    public static final long AUX_SRV_ID = 12L;

    private AccountSuspendedState state_;
    private SysFeatureCfg config_;
    private Account account_;
}