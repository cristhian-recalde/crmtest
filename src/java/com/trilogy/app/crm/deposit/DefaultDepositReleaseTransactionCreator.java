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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.app.crm.support.TransactionSupport;

/**
 * The default transaction creator using
 * {@link com.redknee.app.crm.support.TransactionSupport#createTransaction(Context, Subscriber, long, long,
 * AdjustmentType, boolean, boolean, String, Date, Date, String)}.
 *
 * @author cindy.wong@redknee.com
 */
public class DefaultDepositReleaseTransactionCreator implements DepositReleaseTransactionCreator
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5593930200131667050L;

    /**
     * Singleton instance.
     */
    protected static final DefaultDepositReleaseTransactionCreator INSTANCE =
        new DefaultDepositReleaseTransactionCreator();

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return An instance of this class.
     */
    public static DefaultDepositReleaseTransactionCreator getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates a new <code>DefaultDepositReleaseTransactionCreator</code>.
     */
    protected DefaultDepositReleaseTransactionCreator()
    {
        // empty
    }

    /**
     * Creates a deposit release transaction using
     * {@link com.redknee.app.crm.support.TransactionSupport#createTransaction(Context, Subscriber, long, long,
     * AdjustmentType, boolean, boolean, String, Date, Date, String)}.
     *
     * @param context
     *            The operating context.
     * @param subscriber
     *            Subscriber whose deposit is being released.
     * @param amountReleased
     *            Amount of deposit released.
     * @param adjustmentType
     *            Adjustment type of this transaction.
     * @param billingDate
     *            Effective date for this transaction.
     * @throws HomeException
     *             Thrown if there ar problems creating the transaction.
     * @see com.redknee.app.crm.deposit.DepositReleaseTransactionCreator#createTransaction(
     *      com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, long,
     *      com.redknee.app.crm.bean.AdjustmentType, java.util.Date)
     */
    public final void createTransaction(final Context context, final Subscriber subscriber, final long amountReleased,
        final AdjustmentType adjustmentType, final Date billingDate) throws HomeException
    {
        TransactionSupport.createTransaction(context, subscriber, amountReleased, 0, adjustmentType, false, false,
            SystemSupport.SYSTEM_AGENT, billingDate, new Date(), "Auto deposit release");
    }



}
