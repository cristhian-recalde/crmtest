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

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.AdjustmentTypeTransientHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionOwnerTypeEnum;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.app.crm.xhome.home.TransactionOwnerTypeSettingHome;


/**
 * A suite of test cases for TransactionOwnerTypeSettingHome.
 *
 * @author gary.anderson@redknee.com
 */
public class TestTransactionOwnerTypeSettingHome
    extends ContextAwareTestCase
{
    /**
     * Constructs a test case with the given name.
     *
     * @param name The name of the test.
     */
    public TestTransactionOwnerTypeSettingHome(final String name)
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

        final TestSuite suite = new TestSuite(TestTransactionOwnerTypeSettingHome.class);

        return suite;
    }


    /**
     * Adds a standard-out logger to the Context and sets the severity level to
     * DEBUG.  Adds an AdjustmentType Home with two AdjustmentTypes.  Adds an
     * empty Transaction Home.
     */
    public void setUp()
    {
        super.setUp();

        // Set-up the AdjustmentTypeHome and a couple of AdjustmentTypes.
        try
        {
            {
                final Home home = new AdjustmentTypeTransientHome(getContext());
                getContext().put(AdjustmentTypeHome.class, home);

                AdjustmentType adjustmentType = new AdjustmentType();
                adjustmentType.setCode(SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE);
                home.create(adjustmentType);

                adjustmentType = new AdjustmentType();
                adjustmentType.setCode(ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE);
                adjustmentType.setOwnerType(TransactionOwnerTypeEnum.ACCOUNT);
                home.create(adjustmentType);
            }

            // Set-up the TransactionHome.
            {
                Home home = new TransactionTransientHome(getContext());
                home = new TransactionOwnerTypeSettingHome(getContext(), home);
                getContext().put(TransactionHome.class, home);
            }
        }
        catch (final HomeException exception)
        {
            final IllegalStateException newException = new IllegalStateException(exception.getMessage());
            newException.initCause(exception);
            throw newException;
        }
    }


    /**
     * Tests that a new Transaction with a null owner type inherits the owner
     * type of the associated AdjustmentType.
     */
    public void testProperSettingOfTypeFromAdjustmentType()
        throws HomeException
    {
        final Home home = (Home)getContext().get(TransactionHome.class);

        // Type SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE: Subscriber-level.
        {
            final long receiptNum = 3;

            final Transaction transaction;
            try
            {
                transaction = (Transaction) XBeans.instantiate(Transaction.class, getContext());
            }
            catch (Exception exception)
            {
                throw new HomeException("Cannot instantiate transaction bean", exception);
            }

            transaction.setReceiptNum(receiptNum);
            transaction.setAdjustmentType(SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE);

            assertNull(
                "The default owner type of the transaction should be null.",
                transaction.getOwnerType());

            home.create(getContext(), transaction);

            assertEquals(
                "The returned transaction should now have an owner type of Subscriber.",
                TransactionOwnerTypeEnum.SUBSCRIBER, transaction.getOwnerType());

            final Transaction storedTransaction = (Transaction)home.find(getContext(), Long.valueOf(receiptNum));

            assertEquals(
                "The stored transaction should have an owner type of Subscriber.",
                TransactionOwnerTypeEnum.SUBSCRIBER, storedTransaction.getOwnerType());
        }


        // Type ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE: Account-level.
        {
            final long receiptNum = 11;

            final Transaction transaction = new Transaction();
            transaction.setReceiptNum(receiptNum);
            transaction.setAdjustmentType(ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE);

            assertNull(
                "The default owner type of the transaction should be null.",
                transaction.getOwnerType());

            home.create(getContext(), transaction);

            assertEquals(
                "The returned transaction should now have an owner type of SUBSCRIBER.",
                TransactionOwnerTypeEnum.ACCOUNT, transaction.getOwnerType());

            final Transaction storedTransaction = (Transaction)home.find(getContext(), Long.valueOf(receiptNum));

            assertEquals(
                "The stored transaction should have an owner type of SUBSCRIBER.",
                TransactionOwnerTypeEnum.ACCOUNT, storedTransaction.getOwnerType());
        }
    }


    /**
     * Tests that the decorator does not override the owner type if it is
     * already set on the transaction.
     */
    public void testNonOverridingBehaviour()
        throws HomeException
    {
        final Home home = (Home)getContext().get(TransactionHome.class);

        // Type SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE: Subscriber-level adjustment type.
        {
            final long receiptNum = 3;

            final Transaction transaction;
            try
            {
                transaction = (Transaction) XBeans.instantiate(Transaction.class, getContext());
            }
            catch (Exception exception)
            {
                throw new HomeException("Cannot instantiate transaction bean", exception);
            }

            transaction.setReceiptNum(receiptNum);
            transaction.setAdjustmentType(SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE);
            transaction.setOwnerType(TransactionOwnerTypeEnum.ACCOUNT);

            // Check the adjustment type.
            {
                final Home adjustmentTypeHome = (Home)getContext().get(AdjustmentTypeHome.class);
                final AdjustmentType adjustmentType =
                    (AdjustmentType)adjustmentTypeHome.find(
                        getContext(),
                        Integer.valueOf(SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE));

                assertEquals(
                    "The owner type of the AdjustmentType should be SUBSCRIBER.",
                    TransactionOwnerTypeEnum.SUBSCRIBER, adjustmentType.getOwnerType());
            }

            assertEquals(
                "The owner type of the transaction should be ACCOUNT.",
                TransactionOwnerTypeEnum.ACCOUNT, transaction.getOwnerType());

            home.create(getContext(), transaction);

            assertEquals(
                "The returned transaction should still have an owner type of ACCOUNT.",
                TransactionOwnerTypeEnum.ACCOUNT, transaction.getOwnerType());

            final Transaction storedTransaction = (Transaction)home.find(getContext(), Long.valueOf(receiptNum));

            assertEquals(
                "The stored transaction should have an owner type of ACCOUNT.",
                TransactionOwnerTypeEnum.ACCOUNT, storedTransaction.getOwnerType());
        }

        // Type ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE: Account-level adjustment type.
        {
            final long receiptNum = 17;

            final Transaction transaction = new Transaction();
            transaction.setReceiptNum(receiptNum);
            transaction.setAdjustmentType(ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE);
            transaction.setOwnerType(TransactionOwnerTypeEnum.SUBSCRIBER);

            // Check the adjustment type.
            {
                final Home adjustmentTypeHome = (Home)getContext().get(AdjustmentTypeHome.class);
                final AdjustmentType adjustmentType =
                    (AdjustmentType)adjustmentTypeHome.find(
                        getContext(),
                        Integer.valueOf(ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE));

                assertEquals(
                    "The owner type of the AdjustmentType should be ACCOUNT.",
                    TransactionOwnerTypeEnum.ACCOUNT, adjustmentType.getOwnerType());
            }

            assertEquals(
                "The owner type of the transaction should be SUBSCRIBER.",
                TransactionOwnerTypeEnum.SUBSCRIBER, transaction.getOwnerType());

            home.create(getContext(), transaction);

            assertEquals(
                "The returned transaction should still have an owner type of SUBSCRIBER.",
                TransactionOwnerTypeEnum.SUBSCRIBER, transaction.getOwnerType());

            final Transaction storedTransaction = (Transaction)home.find(getContext(), Long.valueOf(receiptNum));

            assertEquals(
                "The stored transaction should have an owner type of SUBSCRIBER.",
                TransactionOwnerTypeEnum.SUBSCRIBER, storedTransaction.getOwnerType());
        }
    }

    /**
     * AdjustmentType.CODE of Subscriber-level AdjustmentType.
     */
    private static final int SUBSCRIBER_LEVEL_ADJUSTMENTTYPE_CODE = 5;

    /**
     * AdjustmentType.CODE of Account-level AdjustmentType.
     */
    private static final int ACCOUNT_LEVEL_ADJUSTMENTTYPE_CODE = 7;


} // class
