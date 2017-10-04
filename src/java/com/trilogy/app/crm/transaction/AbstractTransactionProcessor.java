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
package com.trilogy.app.crm.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * Has all shared methods for transaction redirection.
 *
 * @author angie.li@redknee.com
 */
public abstract class AbstractTransactionProcessor extends HomeProxy implements TransactionProcessor
{
    /**
     * Receives the delegate for the chain.
     * @param delegate the next pipe in the pipeline to continue
     */
    public AbstractTransactionProcessor(final Home delegate)
    {
        super(delegate);
        adjustmentTypes_ = new ArrayList<AdjustmentTypeEnum>();
    }

    /**
     *
     * Method Description: For surplus payment, only handle non-prepaid subscribers
     * @param ctx the operating context
     * @param acct the account to search for
     * @param spid the spid to get the configuration for inactive subscribers
     * @return Collection the collection of subscirbers
     * @throws HomeException if somethig wrong happens
     */
    protected Collection<Subscriber> getPostpaidSubscribers(final Context ctx,
            final Account acct,
            final CRMSpid spid)
            throws HomeException
            {
        if (spid.isPaymentAcctLevelToInactive())
        {
            // Return all postpaid subscribers
            return getPostpaidSubscribers(ctx, acct);
        }
        else
        {
            // Don't return deactivated subscribers
            return getActivePostpaidSubscribers(ctx, acct, spid);
        }
            }

    /**
     *
     * Method Description: For surplus payment, only handle non-prepaid subscribers
     * @param ctx the operating context
     * @param acct the account to search for
     * @param spid the spid to get the configuration for inactive subscribers
     * @return Collection the collection of subscirbers
     * @throws HomeException if somethig wrong happens
     */
    protected Collection<Subscriber> getActivePostpaidSubscribers(final Context ctx,
            final Account acct,
            final CRMSpid spid)
            throws HomeException
            {
        // Don't return deactivated subscribers
        return getPostpaidSubscribers(ctx, acct, 
                SubscriberStateEnum.IN_ARREARS,
                SubscriberStateEnum.NON_PAYMENT_WARN,
                SubscriberStateEnum.IN_COLLECTION,
                SubscriberStateEnum.PROMISE_TO_PAY,
                SubscriberStateEnum.SUSPENDED,
                SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                SubscriberStateEnum.ACTIVE);
            }

    /**
     * looks up everywhere for the transaction account
     * @param ctx the operating context
     * @param trans the transaction to get information
     * @return the account for that transaction
     * @throws ProportioningCalculatorException if any HomeExceptio occurs, we wrap
     * such HomeException
     */
    protected Account getTransactionAccount(final Context ctx, final Transaction trans)
    throws ProportioningCalculatorException
    {
        Account acct = null;
        try
        {
            Subscriber subs = null;

            acct = (Account) ctx.get(Account.class);
            subs = (Subscriber) ctx.get(Subscriber.class);
            if (acct == null)
            {
                acct = AccountSupport.getAccount(ctx, trans.getBAN());
                if (acct == null)
                {
                    //Try based on subscriber
                    if (subs == null || !subs.getMSISDN().equals(trans.getMSISDN()))
                    {
                        subs = SubscriberSupport.getSubscriber(ctx, trans.getSubscriberID());
                        if (subs != null)
                        {
                            acct = subs.getAccount(ctx);
                        }
                    }
                }
            }
        }
        catch (final HomeException e)
        {
            throw new ProportioningCalculatorException(e);
        }
        return acct;
    }

    /**
     * This method contains the meat for creating transaction.
     * @param ctx the oprating context
     *
     * @param obj The transaction object.
     *
     * @return Object The resulting object.
     * @throws HomeException if something happened when accesing something from the application
     */
    @Override
    public Transaction createTransaction(final Context ctx, final Transaction trans)
    throws HomeException
    {
        Transaction result = null;
        for (AdjustmentTypeEnum adjustmentType : adjustmentTypes_)
        {
            if (AdjustmentTypeSupportHelper.get(ctx).isInCategory(ctx, trans.getAdjustmentType(), adjustmentType))
            {
                final Account acct = AccountSupport.getAccount(ctx, trans.getBAN());

                if (acct != null && acct.isPooled(ctx))
                {
                    result = handleGroupAccountTransaction(ctx, trans);
					generateNoteForGroupTransaction(ctx, acct, trans);
                }
                else
                {
                    result = handleRegularAccountTransaction(ctx, trans);
                }

                CoreTransactionSupportHelper.get(ctx).addTransactionGLCode(ctx, trans);

                return result;
            }
        }
        return result;
    }

	/**
	 * Creates a note for group transaction.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param acct
	 *            The account the transaction was applied to.
	 * @param trans
	 *            The transaction generated.
	 * @throws HomeException
	 *             Throw if there are problems creating the account note.
	 */
	protected void generateNoteForGroupTransaction(Context ctx, Account acct,
	    Transaction trans) throws HomeException
	{
		final Currency currency =
		    ReportUtilities.getCurrency(ctx, acct.getCurrency());

		StringBuilder sb = new StringBuilder();
		sb.append("Account ");
		sb.append(getTransactionType(trans));
		sb.append(" of ");
		sb.append(currency.formatValue(trans.getAmount()));
		sb.append(" was successfully made to subscription ");
		sb.append(trans.getSubscriberID());
		NoteSupportHelper.get(ctx).addAccountNote(ctx, acct.getBAN(), sb.toString(),
		    SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCUPDATE);
	}

    protected void generateNoteForRegularTransaction(Context ctx, Account acct,
	    Transaction trans, List<Long> success, List<Long> failed)
	    throws HomeException
	{
		final Currency currency =
		    ReportUtilities.getCurrency(ctx, acct.getCurrency());

		long succeededAmount = 0;
		long failedAmount = 0;
		for (Long amt : success)
		{
			succeededAmount += amt.longValue();
		}
		for (Long amt : failed)
		{
			failedAmount += amt.longValue();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Account ");
		sb.append(getTransactionType(trans));
		sb.append(" summary: total = ");
		sb.append(currency.formatValue(trans.getAmount()));
		sb.append(" [");
		sb.append((success.size() + failed.size()));
		sb.append(" subscription(s)], succeeded = ");
		sb.append(currency.formatValue(succeededAmount));
		sb.append(" [");
		sb.append(success.size());
		sb.append(" subscription(s)], failed = ");
		sb.append(currency.formatValue(failedAmount));
		sb.append(" [");
		sb.append(failed.size());
		sb.append(" subscription(s)]");

		NoteSupportHelper.get(ctx).addAccountNote(ctx, acct.getBAN(), sb.toString(),
		    SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCUPDATE);
	}
    /**
     * For Group Accounts, Handle the transaction at the appropriate level and the
     * appropriate adjustment type.
     * @param ctx the operating context
     * @param trans The payment.
     * @throws HomeException if something happened when accessing something from the application
     * @return Transaction The original transaction.
     */

    public abstract Transaction handleGroupAccountTransaction(final Context ctx, final Transaction trans)
    throws HomeException;

    /**
     * For Regular Accounts, Handle the transaction at the appropriate level and the
     * appropriate adjustment type.
     * @param ctx the operating context
     * @param trans The payment.
     * @return Transaction The original transaction.
     * @throws HomeException if something happened when accessing something from the application
     */

    public abstract Transaction handleRegularAccountTransaction(final Context ctx, final Transaction trans)
    throws HomeException;


    /**
     * Verifies if the configured ratio has been reached
     * @param ctx the operating context
     * @param ratio the ratio to compare to
     * @return true if the given ratio is less than the configured ratio threshold divided by 100
     */
    protected boolean isRatioTooSmallToSplit(final Context ctx, final double ratio)
    {
        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
        double ratioThreshold = GeneralConfig.DEFAULT_RATIOTHRESHOLD;
        if( config != null )
        {
            ratioThreshold = config.getRatioThreshold();   
        }
        return ratio <= ratioThreshold / 100;
    }

    /**
     * Verifies if the current precision has been reached
     * @param ctx the operating context
     * @param amount the transaction amount to be submitted
     * @param numSubs  the number of subscribers that this transaction will be forwarded to
     * @return true if (amount / numSubs) <= configured currency precision 
     */
    protected boolean isAmountSmallerThanLowestCurrencyUnit(final Context ctx,
            final long amount,
            final int numSubs)
    {
        GeneralConfig config = (GeneralConfig) ctx.get(GeneralConfig.class);
        long currencyPrecision = GeneralConfig.DEFAULT_CURRENCYPRECISION;
        if( config != null )
        {
            currencyPrecision = config.getCurrencyPrecision();   
        }
        final long currentCurrency = amount / numSubs;
        return  currentCurrency <= currencyPrecision;
    }

    /**
     * Clones and submits the real transaction to the applications.
     * When the creation is done (successful or not), it will add the payments amount in the
     * array it corresponds. This is useful for the ER 1125 creation and account note for
     * payments at account level
     * @param ctx the operating context
     * @param trans the transaction to be cloned and submitted
     * @param msisdn the MSISDN to be submitted
     * @param acctPayment the amount to be submitted
     * @param subscriber the subscriber to submit the transaction
     */
    protected void submitTransaction(final Context ctx,
            final Transaction trans,
            final long acctPayment,
            final Subscriber subscriber)
    {
        submitTransaction(ctx,
                trans,
                acctPayment,
                subscriber,
                null,
                null);
    }

    /**
     * Clones and submits the real transaction to the applications.
     * When the creation is done (successful or not), it will add the payments amount in the
     * array it corresponds. This is useful for the ER 1125 creation and account note for
     * payments at account level
     * @param ctx the operating context
     * @param trans the transaction to be cloned and submitted
     * @param msisdn the MSISDN to be submitted
     * @param acctPayment the amount to be submitted
     * @param subscriber the subscriber to submit the transaction
     * @param succesTxnAmounts array that collects all the successful transactions
     * @param failedTxnAmounts array that collects all the failed transactions
     */
    protected void submitTransaction(final Context ctx,
            final Transaction trans,
            final long acctPayment,
            final Subscriber subscriber,
            final List<Long> succesTxnAmounts,
            final List<Long> failedTxnAmounts)
    {        
        final Context sCtx = ctx.createSubContext();
        sCtx.put(Subscriber.class, subscriber);
        try
        {
            if (sCtx.get(Account.class) == null || !((Account) sCtx.get(Account.class)).getBAN().equals(subscriber.getBAN()))
            {
                sCtx.put(Account.class, subscriber.getAccount(ctx));
            }
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx, this,
                    "Error putting correct account in context while creating transaction for subscription '"
                            + subscriber.getId() + "': " + e.getMessage(), e);
        }
        try
        {
            // Don't screw up the original transaction
            final Transaction subTrans = cloneTransaction(trans);
            subTrans.setBAN(subscriber.getBAN());
            subTrans.setMSISDN(subscriber.getMSISDN());
            subTrans.setAmount(acctPayment);
            subTrans.setSubscriberID(subscriber.getId());
            subTrans.setSubscriptionType(subscriber.getSubscriptionType());
            super.create(sCtx, subTrans);
            if (succesTxnAmounts != null)
            {
                succesTxnAmounts.add(Long.valueOf(acctPayment));
            }
        }
        /*
         * It is very important to keep going on this part to keep track of the remaining payments
         * the is why we need to continue here and send a Subscriber note if it fails
         * we already rely on the ER 1124 to keep track of the failed ERs
         */
        catch (final Throwable e)
        {
            if (CoreTransactionSupportHelper.get(ctx).isPayment(sCtx, trans))
            {
                Currency currency = null;
                final Account acct = (Account) sCtx.get(Account.class);

                if (acct != null)
                {
                    currency = ReportUtilities.getCurrency(sCtx, acct.getCurrency());
                }
                final String fileName = CoreTransactionSupportHelper.get(ctx).getTPSFileName(sCtx);

                String amount = String.valueOf(acctPayment);

                if (currency != null)
                {
                    amount = currency.formatValue(acctPayment);
                }

                final StringBuilder msg = new StringBuilder("Failed to create payment transaction with amount ");
                msg.append(amount);
                if (!fileName.equals(""))
                {
                    msg.append(" and external file ");
                    msg.append(fileName);
                }

                writeSubscriberNote(sCtx, subscriber, msg.toString());

                if (failedTxnAmounts != null)
                {
                    failedTxnAmounts.add(acctPayment);
                }
            }
            else
            {
				/*
				 * [Cindy Wong] Throwing RuntimeException is generally not a
				 * good idea. Handling this error more gracefully.
				 */
				// throw new RuntimeException(e);
				LogSupport
				    .major(
				        sCtx,
				        this,
				        "Exception caught while creating subscriber transaction",
				        e);
				if (failedTxnAmounts != null)
				{
					failedTxnAmounts.add(acctPayment);
				}
            }
        }
    }


    /**
     * Writes a subscriber note if something was wrong in the transaction (if payment)
     * @param ctx the operating context
     * @param subscriber the subscriber to include the note
     * @param msg the message to be added
     */
    protected static void writeSubscriberNote(final Context ctx,
            final Subscriber subscriber,
            final String msg)
    {
        try
        {
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx,
                    subscriber.getId(),
                    msg,
                    SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.ACCUPDATE);
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, AbstractTransactionProcessor.class, "Home Exception when trying to create a Subscriber Payemnt note", e);
        }
    }

    /**
     * Writes a account note if something was wrong in the transaction (if payment)
     * @param ctx the operating context
     * @param subscriber the subscriber to include the note
     * @param msg the message to be added
     */
    protected static void writeAccountNote(final Context ctx,
            final Account account,
            final String msg)
    {
        try
        {
            NoteSupportHelper.get(ctx).addAccountNote(ctx,
                    account.getBAN(),
                    msg,
                    SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.ACCUPDATE);
        }
        catch (final HomeException e)
        {
            LogSupport.major(ctx, AbstractTransactionProcessor.class, "Home Exception when trying to create a Account Payment note", e);
        }
    }
    /**
     * Gets a non-prepaid subscribers.
     * @param ctx operating context
     * @param acct the account to get the subscribers
     * @param states the array of states to include in the search
     * @return the array of subscriber from the search
     * @throws HomeException delegates any error from the search
     */
    protected Collection<Subscriber> getPostpaidSubscribers(final Context ctx,
            final Account acct,
            final SubscriberStateEnum... states)
            throws HomeException
    {
        Collection<Subscriber> cl = AccountSupport.getNonResponsibleSubscribers(ctx, acct);

        cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
        if (states != null && states.length > 0)
        {
            cl = CollectionSupportHelper.get(ctx).findAll(ctx, cl, new InOneOfStatesPredicate(states));
        }

        return cl;
    }

    /**
     *
     * Verifies if the subscriber is inactive, if so, we need to check if the
     * payment is bigger than the amount owing the payment should be the exact
     * amount of the owing
     * @param amount the amount to be verified
     * @param amountOwing the subscriber's amount owing
     * @param sub the subcriber to verify
     * @param spid the spid configuration to check is the subscriber is inactive a be able to get the amount
     * @return the trasnaction amount if the subscriber is permitted
     */
    protected long getCreditAmount(
            final long amount,
            final long amountOwing,
            final Subscriber sub,
            final CRMSpid spid)
    {
        long result = amount;
        if (sub.getState() == SubscriberStateEnum.INACTIVE)
        {
            if (spid.isPaymentAcctLevelToInactive())
            {
                // If the subscriber is inactive, we must only credit at most the amount owing
                result = Math.min(amount, amountOwing);
            }
            else
            {
                result = 0;
            }
        }
        return result;
    }

    protected long getCreditAmountForSubscriber(
            final CRMSpid spid,
            final Subscriber sub,
            final double ratio,
            final long delta,
            final boolean ignoreRatioForPaymentPlan)
    {
        final long subOwing;
        if (!ignoreRatioForPaymentPlan)
        {
            subOwing = sub.getAmountOwing();
        }
        else
        {
            subOwing = sub.getAmountOwingWithoutPaymentPlan();
        }

        long amount = 0;

        // Only subscribers with an outstanding balance are taken into account.
        if (subOwing > 0)
        {
            amount += Math.round(subOwing * ratio);
        }

        // Distribute any surplus among the active subscribers only.
        if (EnumStateSupportHelper.get().stateEquals(sub, SubscriberStateEnum.ACTIVE))
        {
            amount += delta;
        }
        else
        {
            amount = getCreditAmount(amount, subOwing, sub, spid);
        }

        return amount;
    }


    /**
     * Return the total owing of all the subscribers in a given subscriber list.
     * @param ctx the operating context
     * @param subs The subscriber collection.
     *
     * @return long The total owing of the subscribers.
     */
    protected long getTotalOwingOfSubscribers(
            final Context ctx,
            final Collection<Subscriber> subs)
    {
        long totalOwing = 0;
        for (Subscriber sub : subs)
        {
            if (sub.isPostpaid())
            {
                // Reset the amount owing for the subscriber so that the getter will calculate it.
                sub.setAmountOwing(SubscriberSupport.INVALID_VALUE);

                final long subOwing = sub.getAmountOwing(ctx);
                if (subOwing > 0)
                {
                    // Only subscribers with an outstanding balance are taken into account.
                    totalOwing += subOwing;
                }
            }
        }
        return totalOwing;
    }


    /**
     * Return the first postpaid subscriber in a given subscriber list.
     *
     * @param subs The subscriber collection.
     * @param letDeactivated true if the configuratio to let deactive subscribers is on
     * @return Subscriber The first subscriber.
     */
    protected Subscriber getFirstPostpaidSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs)
    {
        return getFirstSubscriber(ctx, spid, subs, new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
    }


    /**
     * Return the first subscriber with an outstanding balance in a given subscriber list.
     *
     * @param subs The subscriber collection.
     * @param letDeactivated true if the configuratio to let deactive subscribers is on
     * @return Subscriber The first subscriber with an outstanding balance.
     */
    protected Subscriber getFirstOwingSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs)
    {
        return getFirstSubscriber(ctx, spid, subs, 
                new And().add(new GT(SubscriberXInfo.AMOUNT_OWING, 0L))
                .add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID)));
    }

    private Subscriber getFirstSubscriber(final Context ctx, final CRMSpid spid, final Collection<Subscriber> subs, Predicate p)
    {
        Subscriber result = null;

        if( spid.isInactiveSubscriberPriority() )
        {
            // SPID flag says to give priority to INACTIVE accounts
            Collection<Subscriber> inactiveSubs = CollectionSupportHelper.get(ctx).findAll(ctx, subs, new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));

            // Return an inactive subscriber with an outstanding balance if one exists.
            // This is done in 2 steps to avoid calculating the amount owing of all subscribers.
            // This way, we only calculate the amount owing of at most all inactive subscribers.
            And predicate = new And();
            predicate.add(new GT(SubscriberXInfo.AMOUNT_OWING, 0L));
            predicate.add(p);
            result = CollectionSupportHelper.get(ctx).findFirst(ctx, inactiveSubs, predicate);
        }

        if( result == null )
        {
            // Find first non-deactivated subscriber matching selection criteria
            And predicate = new And();
            predicate.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
            predicate.add(p);

            result = CollectionSupportHelper.get(ctx).findFirst(ctx, subs, predicate);
        }

        return result;
    }


    /**
     * A helper method to make a clone of the original transaction.
     *
     * @param trans The original transaction.
     * @throws HomeException if somethig wrong happens
     * @return Transaction The clone of the original transaction.
     */
    protected Transaction cloneTransaction(final Transaction trans)
    throws HomeException
    {
        Transaction clone = null;

        try
        {
            clone = (Transaction) trans.clone();
        }
        catch (final CloneNotSupportedException e)
        {
            throw new HomeException("Failed to clone the original transaction");
        }

        return clone;
    }

    /**
     * A colleciton of adjustment types this piepline supports
     */
    protected Collection<AdjustmentTypeEnum> adjustmentTypes_ = new ArrayList<AdjustmentTypeEnum>();

	public abstract String getTransactionType(Transaction trans);
}
