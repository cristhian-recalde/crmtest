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
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CreditCategory;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * Checks if the current transaction is a deposit release and updates the subscriber
 * deposit data, credit limit and deposit attribute.
 *
 * @author arturo.medina@redknee.com
 * @since 6.0
 */
public class DepositReleaseTransactionAgent extends PipelineAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2256592848029048166L;


    /**
     * Creates a new DepositReleaseTransactionAgent.
     *
     * @param delegate
     *            The delegate of this action.
     */
    public DepositReleaseTransactionAgent(final ContextAgent delegate)
    {
        super(delegate);
    }


    /**
     * Checks if the current transaction is a deposit release, and updates the
     * subscriber's data accordingly. Also
     *
     * @param ctx
     *            A context
     * @exception AgentException
     *                thrown if one of the services fails to initialize
     */
    @Override
    public void execute(final Context ctx) throws AgentException
    {
        final Home home = (Home) ctx.get(SubscriberHome.class);
        final Account acct = (Account) ctx.get(Account.class);
        final Subscriber subs = (Subscriber) ctx.get(Subscriber.class);
        final Transaction trans = (Transaction) ctx.get(Transaction.class);

        validateSubscriberHome(ctx, home);
        validateSubscriber(ctx, subs);
        validateAccount(ctx, acct);
        validateTransaction(ctx, trans);

        final CRMSpid spid = getSubscriberSpid(ctx, subs);

        try
        {
            if (CoreTransactionSupportHelper.get(ctx).isPaymentConvertedFromDeposit(ctx, trans))
            {
                // Setting the credit limit

                /*
                 * Danny Ng; Do not lower credit limit on the deposit release transaction
                 * of the subscriber move regardless of the spid change credit limit tag.
                 * This limitation was okayed by Mike B. This was done because, when we do
                 * a sub move, we do a deposit release on the entire deposit, but this may
                 * cause the credit limit to be lowered below the sub's existing credit
                 * limit and thus failing the sub move.
                 */
                if(!ctx.getBoolean("IS_DEPOSIT_RELEASE_DONE"))
                {
                    if (spid.getChangeCreditLimitOnDepositRelease() && ctx.get(Common.MOVE_SUBSCRIBER) == null)
                    {
                        updateCreditLimit(ctx, subs, trans, acct);
                    }
                
                    // Setting the deposit attribute
                    updateSubscriberDeposit(subs, trans);

                    // amedina: 6.0 sets the new deposit date for the change

                    /*
                     * Cindy Wong: TT 6122743030 - deposit date should be the transaction's
                     * date, not the current time.
                     */
                    subs.setDepositDate(trans.getTransDate());

                    convertDepositReleaseToPayment(ctx, trans, subs, spid);

                    final Currency currency =
                        ReportUtilities.getCurrency(ctx, subs.getCurrency(ctx));
                    
                    NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subs.getId(), DEPOSIT_RELEASE + currency.formatValue(trans.getAmount()),
                        SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.SUBUPDATE);

                }
               
                home.store(ctx,subs);
            }

            pass(ctx, this, "credit limit and deposit changed");
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, this, "Exception caught", e);
            throw new AgentException(e.getMessage(), e);
        }
    }


    /**
     * Converts a deposit release to payment towards the subscriber balance if the
     * SPID-level option is enabled.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            The deposit release transaction.
     * @param subs
     *            Subscriber whose deposit is being released.
     * @param spid
     *            Service provider of the subscriber.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    private void convertDepositReleaseToPayment(final Context ctx, final Transaction trans, final Subscriber subs,
        final CRMSpid spid) throws HomeException
    {
        long convertedPayment = 0L;

        if (spid.isConvertToPaymentOnDepositRelease())
        {
            /*
             * [Cindy] 2008-01-18: When it is a subscriber move, only the portion of
             * deposit actually being released is converted into payment. In other case,
             * the full deposit release amount is converted into payment.
             */
            final Long delta = (Long) ctx.get(Common.SUBSCRIBER_MOVE_DEPOSIT_DELTA, null);
            if (delta == null)
            {
                convertedPayment = trans.getAmount();
            }
            else if (delta.longValue() > 0)
            {
                convertedPayment = delta.longValue();
            }
        }

        /*
         * [Cindy] 2007-11-12: Convert deposit release to payment if option is enabled and
         * deposit was actually released.
         */
        if (convertedPayment > 0)
        {
            final AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx,
                AdjustmentTypeEnum.PaymentConvertedFromDeposit);
            TransactionSupport.createTransaction(ctx, subs, convertedPayment, type);
        }
    }


    /**
     * Returns the service provider of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param subs
     *            The subscriber being looked up.
     * @return The service provider of the subscriber.
     * @throws AgentException
     *             Thrown if the service provider cannot be found.
     */
    private CRMSpid getSubscriberSpid(final Context ctx, final Subscriber subs) throws AgentException
    {
        CRMSpid spid = null;
        final StringBuilder sb = new StringBuilder();
        sb.append("Cannot find service provider " + subs.getSpid());
        try
        {
            spid = SpidSupport.getCRMSpid(ctx, subs.getSpid());
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Cannot update subscriber deposit amount: error looking up service provider "
                + subs.getSpid(), e).log(ctx);
            throw new AgentException(sb.toString(), e);
        }

        if (spid == null)
        {
            throw new AgentException(sb.toString());
        }
        return spid;
    }


    /**
     * Verifies the transaction is valid.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            The deposit release transaction.
     * @throws AgentException
     *             Thrown if the transaction provided is invalid.
     */
    private void validateTransaction(final Context ctx, final Transaction trans) throws AgentException
    {
        if (trans == null)
        {
            final AgentException exception = new AgentException("No transaction found in context!");
            LogSupport.minor(ctx, this, exception.getMessage(), exception);
            throw exception;
        }
    }


    /**
     * Verifies the subscriber home is valid.
     *
     * @param ctx
     *            The operating context.
     * @param home
     *            Subscriber home.
     * @throws AgentException
     *             Thrown if the subscriber home is invalid.
     */
    private void validateSubscriberHome(final Context ctx, final Home home) throws AgentException
    {
        if (home == null)
        {
            final AgentException exception = new AgentException("Subscriber home not found in context!");
            LogSupport.minor(ctx, this, exception.getMessage(), exception);
            throw exception;
        }
    }


    /**
     * Verifies the account is valid.
     *
     * @param ctx
     *            The operating context.
     * @param acct
     *            The account.
     * @throws AgentException
     *             Thrown if the account is invalid.
     */
    private void validateAccount(final Context ctx, final Account acct) throws AgentException
    {
        if (acct == null)
        {
            final AgentException exception = new AgentException("No account found in context!");
            LogSupport.minor(ctx, this, exception.getMessage(), exception);
            throw exception;
        }
    }


    /**
     * Verifies the subscriber is valid.
     *
     * @param ctx
     *            The operating context.
     * @param subs
     *            The subscriber.
     * @throws AgentException
     *             Thrown if the subscriber is invalid.
     */
    private void validateSubscriber(final Context ctx, final Subscriber subs) throws AgentException
    {
        if (subs == null)
        {
            final AgentException exception = new AgentException("No subscriber found in context!");
            LogSupport.minor(ctx, this, exception.getMessage(), exception);
            throw exception;
        }
    }


    /**
     * Sets the deposit attribute for a particular subscriber.
     *
     * @param sub
     *            The subscriber.
     * @param trans
     *            The related transaction.
     * @throws AgentException
     *             Thrown by home.
     */
    private void updateSubscriberDeposit(final Subscriber sub, final Transaction trans) throws AgentException
    {
        // set deposit, the deposit should not be negative.
        long amount = trans.getAmount();

        if (amount < 0)
        {
            amount = -trans.getAmount();
        }

        if (sub.getDeposit() < amount)
        {
            throw new AgentException("The Deposit release exceeds the deposit for the subscriber " + sub.getId());
        }

        sub.setDeposit(sub.getDeposit() - amount);

    }


    /**
     * Sets the credit limit of the subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber.
     * @param trans
     *            The transaction.
     * @param acct
     *            The account owning the subscriber.
     * @throws HomeException
     *             Thrown by home.
     */
    private void updateCreditLimit(final Context ctx, final Subscriber sub, final Transaction trans, final Account acct)
        throws HomeException
    {
        long amount = trans.getAmount();

        if (amount > 0)
        {
            amount = -trans.getAmount();
        }

        final CreditCategory creditCategory = HomeSupportHelper.get(ctx).findBean(ctx, CreditCategory.class, acct.getCreditCategory());

        if (sub.getState() != SubscriberStateEnum.INACTIVE)
        {
            final double creditLimit = sub.getCreditLimit(ctx) + amount * creditCategory.getFactor();

            if (creditLimit > 0)
            {
                sub.setCreditLimit((long) creditLimit);
            }
            else
            {
                throw new HomeException("The transaction amount (" + trans.getAmount()
                    + ") reaches the credit limit amount (" + sub.getCreditLimit(ctx) + ")");
            }

        }
    }

    // public static final int FACTOR_DOLLAR_TO_CENT = 100;

    /**
     * Deposit release note.
     */
    private static final String DEPOSIT_RELEASE = "Deposit release for ";
}
