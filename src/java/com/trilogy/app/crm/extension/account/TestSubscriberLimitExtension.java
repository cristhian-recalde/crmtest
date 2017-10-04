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

package com.trilogy.app.crm.extension.account;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NoSelectAllHome;
import com.trilogy.framework.xhome.home.ValidatingHome;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountTransientHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.extension.ExtensionForeignKeyAdapter;
import com.trilogy.app.crm.extension.ExtensionHandlingHome;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.ExtensionInstallationHome;
import com.trilogy.app.crm.extension.ExtensionSpidAdapter;
import com.trilogy.app.crm.home.sub.SubscriberLimitHomeValidator;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;


/**
 * @author cindy.wong@redknee.com
 * @since 2008-09-08
 */
public class TestSubscriberLimitExtension extends ContextAwareTestCase
{

    /**
     * Default subscriber limit.
     */
    public static int LIMIT = 5;
    /**
     * BAN to test.
     */
    public static String BAN = "34523";


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

        final TestSuite suite = new TestSuite(TestSubscriberLimitExtension.class);

        return suite;
    }


    /**
     * Create a new instance of <code>TestSubscriberLimitExtension</code>.
     *
     * @param name
     *            Name of the test case.
     */
    public TestSubscriberLimitExtension(final String name)
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
        initAccount();
        initSubscriber();
        initSubscriberLimitExtension();
    }


    /**
     * Initialize Account.
     */
    private void initAccount()
    {
        Home home = new AccountTransientHome(getContext());
        home = new AdapterHome(home, new ExtensionSpidAdapter());
        home = new ExtensionHandlingHome<AccountExtension>(
                getContext(), 
                AccountExtension.class, 
                AccountExtensionXInfo.BAN, 
                home);
        home = new AdapterHome(home, new ExtensionForeignKeyAdapter(AccountExtensionXInfo.BAN));

        Account bean = new Account();
        bean.setBAN(BAN);

        try
        {
            bean = (Account) home.create(getContext(), bean);
        }
        catch (final HomeException exception)
        {
            fail("Exception caught when initializing Account");
        }
        getContext().put(AccountHome.class, home);
    }


    /**
     * Initialize Subscriber.
     */
    private void initSubscriber()
    {
        Home home = new SubscriberTransientHome(getContext());
        home = new ValidatingHome(SubscriberLimitHomeValidator.instance(), home);
        getContext().put(SubscriberHome.class, home);
    }


    /**
     * Initialize SubscriberLimitExtension.
     */
    private void initSubscriberLimitExtension()
    {
        Home home = new SubscriberLimitExtensionTransientHome(getContext());
        home = new ValidatingHome(new ExtensionInstallationHome(getContext(), new NoSelectAllHome(home)));

        getContext().put(SubscriberLimitExtensionHome.class, home);
        ExtensionSupportHelper.get(getContext()).registerExtension(getContext(), SubscriberLimitExtension.class,
            SubscriberLimitExtensionHome.class);
    }


    /**
     * Test method for installation of {@link SubscriberLimitExtension} on an account with
     * no subscribers.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testInstallNoSubscribers() throws HomeException
    {
        setExtension(LIMIT, true);
    }


    /**
     * Test method for installation of {@link SubscriberLimitExtension} on an account with
     * subscribers under the intended limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testInstallSubscribersUnderLimit() throws HomeException
    {
        for (int i = 1; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }

        setExtension(LIMIT, true);
    }


    /**
     * Test method for installation of {@link SubscriberLimitExtension} on an account with
     * subscribers at the intended limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testInstallSubscribersAtLimit() throws HomeException
    {
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }

        setExtension(LIMIT, true);
    }


    /**
     * Test method for installation of {@link SubscriberLimitExtension} on an account with
     * subscribers over the intended limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testInstallSubscribersOverLimit() throws HomeException
    {
        for (int i = 0; i < 2 * LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        setExtension(LIMIT, false);
    }


    /**
     * Test method for installation of {@link SubscriberLimitExtension} on an account with
     * total subscribers over the intended limit, but non-inactive subscriber under the
     * intended limit. This test makes sure inactive subscribers are not counted towards
     * the limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testInstallSubscriberUnderLimitExcludingDeactivated() throws HomeException
    {
        for (int i = 1; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.INACTIVE, true);
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        setExtension(LIMIT, true);
    }


    /**
     * Test method for subscriber creation under the limit when account has
     * {@link SubscriberLimitExtension} installed.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberCreationUnderLimit() throws HomeException
    {
        setExtension(LIMIT, true);

        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
    }


    /**
     * Test method for subscriber creation over the limit when account has
     * {@link SubscriberLimitExtension} installed.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberCreationOverLimit() throws HomeException
    {
        setExtension(LIMIT, true);

        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, false);
        }
    }


    /**
     * Test method for subscriber creation, when the total number of subscribers is over
     * the limit, but some of the subscribers are inactive.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testSubscriberCreationUnderLimitExcludingDeactivated() throws HomeException
    {
        setExtension(LIMIT, true);
        String lastId = null;
        for (int i = 0; i < LIMIT; i++)
        {
            lastId = addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        deactivateSubscriber(lastId);
        addSubscriber(SubscriberStateEnum.ACTIVE, true);
    }


    /**
     * Test method for extension update, when the current number of subscribers is under
     * the new limit, which is higher than the current limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testUpdateUnderNewHigherLimit() throws HomeException
    {
        setExtension(LIMIT, true);
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }

        setExtension(LIMIT + 1, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, false);
    }


    /**
     * Test method for extension update, when the current number of subscribers is at the
     * new limit, which is lower than the current limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testUpdateAtNewLowerLimit() throws HomeException
    {
        setExtension(LIMIT, true);
        for (int i = 1; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }

        setExtension(LIMIT - 1, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, false);
    }


    /**
     * Test method for extension update, when the current number of subscribers is under
     * the new limit, which is lower than the current limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testUpdateUnderNewLowerLimit() throws HomeException
    {
        setExtension(LIMIT, true);
        for (int i = 2; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }

        setExtension(LIMIT - 1, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, false);
    }


    /**
     * Test method for extension update, when the current number of subscribers is over
     * the new limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testUpdateOverNewLimit() throws HomeException
    {
        setExtension(LIMIT, true);
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        setExtension(LIMIT - 1, false);
    }


    /**
     * Test method for extension update, when the current number of subscribers is under
     * the new limit if inactive subscribers are excluded from the count.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testUpdateUnderNewLimitExcludingDeactivated() throws HomeException
    {
        setExtension(LIMIT, true);
        String lastId = null;
        for (int i = 0; i < LIMIT; i++)
        {
            lastId = addSubscriber(SubscriberStateEnum.ACTIVE, true);
            deactivateSubscriber(lastId);
        }
        setExtension(1, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, true);
        addSubscriber(SubscriberStateEnum.ACTIVE, false);
    }


    /**
     * Test removal of {@link SubscriberLimitExtension}. After removal, creation of
     * subscriber should succeed even if it over the (old) limit.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    public void testRemove() throws HomeException
    {
        setExtension(LIMIT, true);
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
        addSubscriber(SubscriberStateEnum.ACTIVE, false);
        removeExtension();
        for (int i = 0; i < LIMIT; i++)
        {
            addSubscriber(SubscriberStateEnum.ACTIVE, true);
        }
    }


    /**
     * Deactivates a subscriber.
     *
     * @param id
     *            ID of subscriber to deactivate.
     * @throws HomeException
     *             Thrown by home.
     */
    private void deactivateSubscriber(final String id) throws HomeException
    {
        final Home home = (Home) getContext().get(SubscriberHome.class);
        final Subscriber subscriber = (Subscriber) home.find(new EQ(SubscriberXInfo.ID, id));
        subscriber.setState(SubscriberStateEnum.INACTIVE);
        home.store(getContext(), subscriber);
    }


    /**
     * Adds a subscriber to the account.
     *
     * @param state
     *            Initial subscriber state.
     * @param expectedSuccess
     *            Whether this is expected to be a successful operation.
     * @return ID of the created subscriber.
     * @throws HomeException
     *             Thrown by home.
     */
    private String addSubscriber(final SubscriberStateEnum state, final boolean expectedSuccess) throws HomeException
    {
        final String id = BAN + "-" + nextSubscriberId++;
        Subscriber subscriber = new Subscriber();
        subscriber.setBAN(BAN);
        subscriber.setId(id);
        subscriber.setState(state);
        final Home home = (Home) getContext().get(SubscriberHome.class);

        if (expectedSuccess)
        {
            subscriber = (Subscriber) home.create(getContext(), subscriber);
            return id;
        }

        try
        {
            subscriber = (Subscriber) home.create(getContext(), subscriber);
            fail("Exception expected");
        }
        catch (final HomeException exception)
        {
            // do nothing
        }
        return null;
    }


    /**
     * Installs or updates the {@link SubscriberLimitExtension} of an account.
     *
     * @param limit
     *            Subscriber limit to set on the account.
     * @param expectedSuccess
     *            Whether this is expected to be a successful or failure operation.
     * @throws HomeException
     *             Thrown by home.
     */
    private void setExtension(final int limit, final boolean expectedSuccess) throws HomeException
    {
        Account account = AccountSupport.getAccount(getContext(), BAN);
        final Home home = (Home) getContext().get(AccountHome.class);
        final List<ExtensionHolder> extensions = new ArrayList<ExtensionHolder>();
        final ExtensionHolder holder = new AccountExtensionHolder();
        final SubscriberLimitExtension extension = new SubscriberLimitExtension();
        extension.setMaxSubscribers(limit);
        holder.setExtension(extension);
        extensions.add(holder);
        account.setAccountExtensions(extensions);
        if (expectedSuccess)
        {
            account = (Account) home.store(getContext(), account);
        }
        else
        {
            try
            {
                account = (Account) home.store(getContext(), account);
                fail("Exception expected.");
            }
            catch (final HomeException exception)
            {
                // do nothing
            }
        }
    }


    /**
     * Removes any existing {@link SubscriberLimitExtension} from the account.
     *
     * @throws HomeException
     *             Thrown by home.
     */
    private void removeExtension() throws HomeException
    {
        Account account = AccountSupport.getAccount(getContext(), BAN);
        account.setAccountExtensions(new ArrayList<AccountExtensionHolder>());
        final Home home = (Home) getContext().get(AccountHome.class);
        account = (Account) home.store(getContext(), account);
    }

    /**
     * Next subscriber ID.
     */
    private static int nextSubscriberId = 1;
}
