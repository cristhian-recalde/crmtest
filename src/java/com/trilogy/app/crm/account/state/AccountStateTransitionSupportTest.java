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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AccountCategoryTransientHome;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.BlackListConfig;
import com.trilogy.app.crm.bean.BlackListConfigHome;
import com.trilogy.app.crm.bean.BlackListConfigTransientHome;
import com.trilogy.app.crm.bean.BlackTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.app.crm.state.EnumStateTransitionSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * JUnit test for account state transition validation.
 *
 * @author cindy.wong@redknee.com
 * @since 13-Mar-08
 */
public class AccountStateTransitionSupportTest extends ContextAwareTestCase
{

    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by standard JUnit tools (i.e., those that do not provide a context).
     *
     * @return A new suite of Tests for execution
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution. This method is intended to be invoked
     * by the Redknee Xtest code, which provides the application's operating context.
     *
     * @param context
     *            The operating context
     * @return A new suite of Tests for execution
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(AccountStateTransitionSupportTest.class);

        return suite;
    }


    /**
     * Create a new instance of <code>AccountStateTransitionSupportTest</code>.
     *
     * @param name
     *            Unit test case name.
     */
    public AccountStateTransitionSupportTest(final String name)
    {
        super(name);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp()
    {
        super.setUp();

        initAccountType();

        initRootAccount();

        initSystemFeatureConfig();

        initBlackListConfig();
    }


    /**
     * Initialize black list configurations.
     */
    private void initBlackListConfig()
    {
        final BlackListConfigHome blackListConfigHome = new BlackListConfigTransientHome(getContext());
        final BlackListConfig blackListConfig = new BlackListConfig();
        blackListConfig.setBlackTypeId(BlackTypeEnum.BLACK_INDEX);
        blackListConfig.setExistingAccountReactivation(false);
        blackListConfig.setNewAccountActivation(false);

        final BlackListConfig greyListConfig = new BlackListConfig();
        greyListConfig.setBlackTypeId(BlackTypeEnum.GRAY_INDEX);
        greyListConfig.setExistingAccountReactivation(true);
        greyListConfig.setNewAccountActivation(true);

        try
        {
            blackListConfigHome.create(getContext(), blackListConfig);
            blackListConfigHome.create(getContext(), greyListConfig);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when creating black list configuration: " + exception);
        }
        getContext().put(BlackListConfigHome.class, blackListConfigHome);
    }


    /**
     * Initialize system feature configuration.
     */
    private void initSystemFeatureConfig()
    {
        final SysFeatureCfg config = new SysFeatureCfg();
        config.setSupportsInCollection(false);
        getContext().put(SysFeatureCfg.class, config);
    }


    /**
     * Initialize root account.
     */
    private void initRootAccount()
    {
        this.rootAccount_ = new Account();
        this.rootAccount_.setBAN("1010");
        this.rootAccount_.setType(this.accountType_.getIdentifier());
        this.rootAccount_.setResponsible(true);
        final AccountHome accountHome = new AccountTransientHome(getContext());
        try
        {
            accountHome.create(getContext(), this.rootAccount_);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when creating account: " + exception);
        }
        getContext().put(AccountHome.class, accountHome);
    }


    /**
     * Initialize account type.
     */
    private void initAccountType()
    {
        // set up account types
        this.accountType_ = new AccountCategory();
        this.accountType_.setIdentifier(1);
        this.accountType_.setName("AccountType");
		final AccountCategoryHome accountCategoryHome =
		    new AccountCategoryTransientHome(getContext());
        try
        {
			accountCategoryHome.create(getContext(), this.accountType_);
        }

        catch (final HomeException exception)
        {
            fail("Exception caught when creating account type: " + exception);
        }
		getContext().put(AccountCategoryHome.class, accountCategoryHome);
    }


    /**
     * Enables in-collection support.
     */
    protected void enableInCollectionSupport()
    {
        final SysFeatureCfg config = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        config.setSupportsInCollection(true);
        getContext().put(SysFeatureCfg.class, config);
    }


    /**
     * Disables in-collection support.
     */
    protected void disableInCollectionSupport()
    {
        final SysFeatureCfg config = (SysFeatureCfg) getContext().get(SysFeatureCfg.class);
        config.setSupportsInCollection(false);
        getContext().put(SysFeatureCfg.class, config);
    }


    /**
     * Test a single account state transition.
     *
     * @param account
     *            Account to be transitioned.
     * @param state
     *            New state.
     * @param expected
     *            Whether the state transition is expected to be allowed or not.
     * @param manual
     *            Whether this is a manual transition.
     */
    protected void testTransition(final Account account, final AccountStateEnum state, boolean expected,
        final boolean manual)
    {
        final StringBuilder sb = new StringBuilder();
        final AccountStateEnum startState = account.getState();
        if (manual)
        {
            sb.append("manual ");
        }
        sb.append("state transition from ");
        sb.append(startState.getIndex());
        sb.append(":");
        sb.append(startState.getDescription());
        sb.append(" to ");
        sb.append(state.getIndex());
        sb.append(":");
        sb.append(state.getDescription());
        sb.append(" is");
        if (!expected)
        {
            sb.append(" not");
        }
        sb.append(" allowed for ");
        if (account.getResponsible())
        {
            sb.append("responsible");
        }
        else
        {
            sb.append("non-responsible");
        }
        sb.append(" accounts");

        boolean result;
        final EnumStateTransitionSupport support = AccountStateTransitionSupport.instance();
        if (manual)
        {
            result = support.isManualStateTransitionAllowed(getContext(), account, state);
        }
        else
        {
            result = support.isStateTransitionAllowed(getContext(), account, state);
        }
        assertEquals(sb.toString(), expected, result);
    }


    /**
     * Test state transitions.
     *
     * @param account
     *            Account.
     * @param map
     *            Map of start state, end state, and expected status.
     * @param manual
     *            Whether to test manual state transition.
     */
    protected void testTransitions(final Account account,
        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map, final boolean manual)
    {
        final EnumStateTransitionSupport stateTransitionSupport = AccountStateTransitionSupport.instance();

        // other states are invalid
        for (final Map.Entry<AccountStateEnum, Map<AccountStateEnum, Boolean>> entry : map.entrySet())
        {
            final AccountStateEnum state = entry.getKey();
            final Map<AccountStateEnum, Boolean> endStateMap = entry.getValue();
            for (final Iterator it = AccountStateEnum.COLLECTION.iterator(); it.hasNext();)
            {
                final AccountStateEnum endState = (AccountStateEnum) it.next();
                if (!endStateMap.containsKey(endState))
                {
                    endStateMap.put(endState, Boolean.FALSE);
                }
            }
            endStateMap.put(state, Boolean.TRUE);
        }

        for (final Iterator it = AccountStateEnum.COLLECTION.iterator(); it.hasNext();)
        {
            final AccountStateEnum state = (AccountStateEnum) it.next();
            if (!map.containsKey(state))
            {
                final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
                for (final Iterator it2 = AccountStateEnum.COLLECTION.iterator(); it2.hasNext();)
                {
                    final AccountStateEnum endState = (AccountStateEnum) it2.next();
                    endStateMap.put(endState, Boolean.FALSE);
                }
                map.put(state, endStateMap);
            }
        }

        // populate the map
        for (final Map.Entry<AccountStateEnum, Map<AccountStateEnum, Boolean>> stateEntry : map.entrySet())
        {
            final AccountStateEnum startState = stateEntry.getKey();
            account.setState(startState);
            final Map<AccountStateEnum, Boolean> transitionMap = stateEntry.getValue();

            final Collection possibleStates;

            if (manual)
            {
                possibleStates = stateTransitionSupport.getPossibleManualStateCollection(getContext(), account);
            }
            else
            {
                possibleStates = stateTransitionSupport.getPossibleStateCollection(getContext(), account);
            }

            for (final Map.Entry<AccountStateEnum, Boolean> entry : transitionMap.entrySet())
            {
                testTransition(account, entry.getKey(), entry.getValue().booleanValue(), manual);

                final StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey());
                sb.append(" is ");
                if (entry.getValue().booleanValue())
                {
                    sb.append("a valid");
                }
                else
                {
                    sb.append("an invalid");
                }
                sb.append(" possible state to transition from ");
                sb.append(startState);
                assertEquals(sb.toString(), entry.getValue().booleanValue(), possibleStates.contains(entry.getKey()));
            }
        }
    }


    /**
     * Test state transitions for responsible accounts.
     */
    public void testResponsibleAccountStateTransition()
    {
        final Account account = new Account();
        account.setBAN("1234");
        account.setType(this.accountType_.getIdentifier());
        account.setResponsible(true);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, false);
    }


    /**
     * Test state transitions for non-responsible accounts.
     */
    public void testNonResponsibleAccountStateTransition()
    {
        final Account account = new Account();
        account.setBAN("5678");
        account.setType(this.accountType_.getIdentifier());
        account.setParentBAN(this.rootAccount_.getBAN());
        account.setResponsible(false);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_WARN, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_ARREARS, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, false);
    }


    /**
     * Test manual state transitions for responsible accounts with in-collection support
     * enabled.
     */
    public void testResponsibleAccountManualStateTransitionInCollectionEnabled()
    {
        enableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("9012");
        account.setType(this.accountType_.getIdentifier());
        account.setResponsible(true);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }


    /**
     * Test manual state transitions for non-responsible accounts with in-collection
     * support enabled.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testNonResponsibleAccountManualStateTransitionInCollectionEnabledParentActive() throws HomeException
    {
        enableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("3456");
        account.setType(this.accountType_.getIdentifier());
        account.setParentBAN(this.rootAccount_.getBAN());
        account.setResponsible(false);
        this.rootAccount_.setState(AccountStateEnum.ACTIVE);
        ((Home) getContext().get(AccountHome.class)).store(getContext(), this.rootAccount_);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }


    /**
     * Test manual state transitions for non-responsible accounts with in-collection
     * support enabled and parent account not active.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testNonResponsibleAccountManualStateTransitionInCollectionEnabledParentNotActive() throws HomeException
    {
        enableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("3457");
        account.setType(this.accountType_.getIdentifier());
        account.setParentBAN(this.rootAccount_.getBAN());
        account.setResponsible(false);
        this.rootAccount_.setState(AccountStateEnum.SUSPENDED);
        ((Home) getContext().get(AccountHome.class)).store(getContext(), this.rootAccount_);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }


    /**
     * Test manual state transitions for responsible accounts with in-collection support
     * disabled.
     */
    public void testResponsibleAccountManualStateTransitionInCollectionDisabled()
    {
        disableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("3344");
        account.setType(this.accountType_.getIdentifier());
        account.setResponsible(true);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.PROMISE_TO_PAY, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.IN_COLLECTION, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }


    /**
     * Test manual state transitions for non-responsible accounts with in-collection
     * support disabled and parent active.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testNonResponsibleAccountManualStateTransitionInCollectionDisabledParentActive() throws HomeException
    {
        disableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("5555");
        account.setType(this.accountType_.getIdentifier());
        account.setParentBAN(this.rootAccount_.getBAN());
        account.setResponsible(false);
        this.rootAccount_.setState(AccountStateEnum.ACTIVE);
        ((Home) getContext().get(AccountHome.class)).store(getContext(), this.rootAccount_);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.ACTIVE, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }


    /**
     * Test manual state transitions for non-responsible accounts with in-collection
     * support disabled and parent not active.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testNonResponsibleAccountManualStateTransitionInCollectionDisabledParentNotActive()
        throws HomeException
    {
        disableInCollectionSupport();
        final Account account = new Account();
        account.setBAN("5556");
        account.setType(this.accountType_.getIdentifier());
        account.setParentBAN(this.rootAccount_.getBAN());
        account.setResponsible(false);
        this.rootAccount_.setState(AccountStateEnum.SUSPENDED);
        ((Home) getContext().get(AccountHome.class)).store(getContext(), this.rootAccount_);

        final Map<AccountStateEnum, Map<AccountStateEnum, Boolean>> map;
        map = new HashMap<AccountStateEnum, Map<AccountStateEnum, Boolean>>();

        // from active
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.ACTIVE, endStateMap);
        }

        // from suspended
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.SUSPENDED, endStateMap);
        }

        // from warned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_WARN, endStateMap);
        }

        // from dunned
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.NON_PAYMENT_SUSPENDED, endStateMap);
        }

        // from in-arrears
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_ARREARS, endStateMap);
        }

        // from in-collection
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.IN_COLLECTION, endStateMap);
        }

        // from promise-to-pay
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.SUSPENDED, Boolean.TRUE);
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.PROMISE_TO_PAY, endStateMap);
        }

        // from deactivated
        {
            final Map<AccountStateEnum, Boolean> endStateMap = new HashMap<AccountStateEnum, Boolean>();
            endStateMap.put(AccountStateEnum.INACTIVE, Boolean.TRUE);
            map.put(AccountStateEnum.INACTIVE, endStateMap);
        }

        testTransitions(account, map, true);
    }

    /**
     * Account type used.
     */
    private AccountCategory accountType_;

    /**
     * Responsible root account.
     */
    private Account rootAccount_;
}
