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

package com.trilogy.app.crm.deposit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Calendar;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeStateEnum;
import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.TransactionOwnerTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.DepositSupport;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.unit_test.UnitTestSupport;


/**
 * Provides support for deposit release test suites.
 *
 * @author cindy.wong@redknee.com
 */
public abstract class DepositReleaseTestCase extends ContextAwareTestCase
{

    /**
     * Postfix for home class.
     */
    public static final String HOME_POSTFIX = "Home";

    /**
     * Postfix for transient home class.
     */
    public static final String TRANSIENT_HOME_POSTFIX = "TransientHome";

    /**
     * Sets identifier of transactions.
     *
     * @author cindy.wong@redknee.com
     */
    protected static class TransactionIdentifierSettingHome extends HomeProxy
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -4469942766220772661L;

        /**
         * Next transaction identifier.
         */
        private static long nextId = 1L;


        /**
         * Creates a new identifier setting home for transactions.
         *
         * @param context
         *            The operating context
         * @param delegate
         *            The delegate
         */
        TransactionIdentifierSettingHome(final Context context, final Home delegate)
        {
            super(context, delegate);
        }


        /**
         * Updates the identifier when creating a new transaction.
         *
         * @param context
         *            The operating context
         * @param object
         *            The new transaction
         * @return The transaction actually created
         * @throws HomeException
         *             Thrown if there are problems creating the transaction
         * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
         *      java.lang.Object)
         */
        @Override
        public Object create(final Context context, final Object object) throws HomeException
        {
            final Transaction transaction = (Transaction) object;
            transaction.setReceiptNum(nextId++);
            return super.create(context, transaction);
        }
    }

    /**
     * Updates subscriber deposits when a deposit release transaction is made.
     *
     * @author cindy.wong@redknee.com
     */
    protected static class DepositUpdateHome extends HomeProxy
    {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 5524325666709620983L;


        /**
         * Creates a new deposit update home.
         *
         * @param context
         *            The operating context
         * @param delegate
         *            The delegate
         */
        DepositUpdateHome(final Context context, final Home delegate)
        {
            super(context, delegate);
        }


        /**
         * Updates subscriber's deposit value when a deposit release transaction is made.
         *
         * @param context
         *            The operating context
         * @param object
         *            The new transaction
         * @return The actual transaction created
         * @throws HomeException
         *             Thrown if there are problems creating the transaction or updating
         *             the subscriber's deposit
         * @see com.redknee.framework.xhome.home.HomeProxy#create(com.redknee.framework.xhome.context.Context,
         *      java.lang.Object)
         */
        @Override
        public Object create(final Context context, final Object object) throws HomeException
        {
            final Transaction transaction = (Transaction) object;
            // use deposit release category
            if (CoreTransactionSupportHelper.get(context).isDepositRelease(context, transaction)
                || CoreTransactionSupportHelper.get(context).isPaymentConvertedFromDeposit(context, transaction))
            // if (transaction.getAdjustmentType() ==
            // AdjustmentTypeEnum.DepositReleasePayment.getIndex())
            {
                final Subscriber subscriber = (Subscriber) ((Home) context.get(SubscriberHome.class)).find(context,
                    transaction.getSubscriberID());
                subscriber.setDeposit(subscriber.getDeposit(context) - transaction.getAmount());
                ((Home) context.get(SubscriberHome.class)).store(context, subscriber);
            }
            return super.create(context, transaction);
        }
    }


    /**
     * Creates a new <code>DepositReleaseTestCase</code>.
     *
     * @param name
     *            Name of the test suite
     */
    public DepositReleaseTestCase(final String name)
    {
        super(name);
    }


    /**
     * Sets up the environment common to all deposit release test cases.
     *
     * @see com.redknee.app.crm.unit_test.ContextAwareTestCase#setUp()
     */
    @Override
    public void setUp()
    {
        super.setUp();
        setUpTransientHomes(AutoDepositReleaseCriteria.class, CRMSpid.class, CreditCategory.class, Account.class,
            AdjustmentType.class, Transaction.class, Subscriber.class, BillCycle.class);
        setUpLicenseHome();
    }


    /**
     * Create transient homes for the provided bean classes and put them in the context.
     *
     * @param beans
     *            Beans to create transient homes for.
     */
    protected final void setUpTransientHomes(final Class... beans)
    {
        for (final Class bean : beans)
        {
            try
            {
                final String transientName = bean.getName() + TRANSIENT_HOME_POSTFIX;
                final Class transientHome = Class.forName(transientName);
                final Constructor constructor = transientHome.getConstructor(Context.class);
                final Home home = (Home) constructor.newInstance(getContext());
                final String homeName = bean.getName() + HOME_POSTFIX;
                final Class beanHome = Class.forName(homeName);
                getContext().put(beanHome, home);
            }
            catch (final Exception exception)
            {
                fail(bean + " transient home cannot be created or stored in context: " + getStackTrace(exception));
            }
        }
    }


    /**
     * Sets up license home.
     */
    protected final void setUpLicenseHome()
    {
        UnitTestSupport.installLicenseManager(getContext());
    }


    /**
     * Sets up test license for auto deposit release.
     */
    protected final void setUpTestLicense()
    {
        try
        {
            UnitTestSupport.createLicense(getContext(), DepositSupport.AUTO_DEPOSIT_RELEASE_LICENSE_KEY);
        }
        catch (final HomeException e)
        {
            fail("Cannot create test license: " + getStackTrace(e));
        }
    }


    /**
     * Gets the string representation of the throwable.
     *
     * @param throwable
     *            The throwable to print.
     * @return String representation of the throwable's message and stack trace.
     */
    protected final String getStackTrace(final Throwable throwable)
    {
        final StringWriter sw = new StringWriter();
        sw.append(throwable.getMessage()).append(":\n");
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    /**
     * Sets up accounts and one subscriber per account belonging to a SPID.
     *
     * @param spid
     *            SPID to create subscribers for.
     * @param numAccounts
     *            Number of accounts to create.
     * @param billCycle
     *            Bill cycle to create the account with.
     * @param creditCategory
     *            Credit category to create the account with.
     */
    protected final void setUpAccounts(final CRMSpid spid, final int numAccounts, final int billCycle,
        final int creditCategory)
    {
        final int maxBan = 100000;
        final long deposit = 2000L;
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        for (int i = 0; i < numAccounts; i++)
        {
            final Account account = DepositReleaseTestSupport.createAccount(spid.getId(), creditCategory, billCycle);
            final Subscriber subscriber = DepositReleaseTestSupport.createSubscriber(account, deposit,
                    calendar.getTime());
            createBean(account);
            createBean(subscriber);
        }
    }


    /**
     * Sets up default adjustment type.
     */
    protected final void setUpAdjustmentType()
    {
        final AdjustmentType adjustmentType = new AdjustmentType();
        adjustmentType.setCode(AdjustmentTypeEnum.DepositRelease.getIndex());
        adjustmentType.setName("Auto deposit release");
        adjustmentType.setAction(AdjustmentTypeActionEnum.DEBIT);
        adjustmentType.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);
        adjustmentType.setState(AdjustmentTypeStateEnum.ACTIVE);
        createBean(adjustmentType);
    }


    /**
     * Add the bean to the appropriate home in context.
     *
     * @param bean
     *            The bean to add.
     */
    protected final void createBean(final Object bean)
    {
        // find the home
        final String homeName = bean.getClass().getName() + HOME_POSTFIX;
        try
        {
            final Class homeClass = Class.forName(homeName);
            ((Home) getContext().get(homeClass)).create(bean);
        }
        catch (final ClassNotFoundException exception)
        {
            fail(homeName + " not found in context or cannot create bean in home: " + getStackTrace(exception));
        }
        catch (final HomeException exception)
        {
            fail(bean + " cannot be stored in home: " + getStackTrace(exception));
        }
    }


    /**
     * Store the bean to the appropriate home in context.
     *
     * @param bean
     *            The bean to store.
     */
    protected final void storeBean(final Object bean)
    {
        // find the home
        final String homeName = bean.getClass().getName() + HOME_POSTFIX;
        try
        {
            final Class homeClass = Class.forName(homeName);
            ((Home) getContext().get(homeClass)).store(bean);
        }
        catch (final ClassNotFoundException exception)
        {
            fail(homeName + " not found in context or cannot store bean in home: " + getStackTrace(exception));
        }
        catch (final HomeException exception)
        {
            fail(bean + " cannot be created in home: " + getStackTrace(exception));
        }
    }


    /**
     * Test the serializability of an object.
     *
     * @param object
     *            Object to test.
     * @throws IOException
     *             Thrown if there are IO errors.
     */
    protected final void serialize(final Serializable object) throws IOException
    {
        final ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
        out.writeObject(object);
        out.close();
    }
}
