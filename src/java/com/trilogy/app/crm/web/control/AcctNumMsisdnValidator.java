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
package com.trilogy.app.crm.web.control;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.transaction.PrepaidPaymentException;


/**
 * Validator used by TransactionHome to make sure that user input valid Account number and
 * MSISDN. arturo.medina: This class requires an urgent refactoring job. Will do it on the
 * next revision. To avoid cyclomatic complexity this validator will be a compound one.
 *
 * @author danny.zou@redknee.com
 */
public class AcctNumMsisdnValidator extends ContextAwareSupport implements Validator
{

    /**
     * Default constructor.
     *
     * @param ctx
     *            the context to get information
     */
    public AcctNumMsisdnValidator(final Context ctx)
    {
        setContext(ctx);
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final Transaction trans = (Transaction) obj;

        try
        {
            validateAmount(ctx, trans);
        }
        catch (final HomeException e)
        {
            LogSupport.crit(ctx, this, "Home exception when validating the transaction amount", e);
        }

        if (CoreTransactionSupportHelper.get(ctx).isPayment(ctx, trans))
        {
            /*
             * Payments need to be searched by subscriber id instead of MSISDN To keep
             * track of subscriber payements
             */

            if (PayeeEnum.Subscriber.equals(trans.getPayee()))
            {
                final Subscriber sub = setupAccountAndSubscriber(ctx, trans);
                if (sub == null )
                {
                    final IllegalStateException exception = new IllegalStateException("Invalid subscriber " + trans.getSubscriberID());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Failing attempt to create payment for invalid subscriber .  " + "Type: " + trans.getAdjustmentType() + " " + "Value: " + trans.getAmount(), exception);
                    }
                    throw exception;
                }
                else if ( sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
                {
                    final IllegalStateException exception = new PrepaidPaymentException("Can not make Account Payment to Prepaid subscriber " + trans.getSubscriberID());
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Failing attempt to create payment for prepaid subscriber.  " + "Type: " + trans.getAdjustmentType() + " " + "Value: " + trans.getAmount(), exception);
                    }
                    throw exception;
                }
            }

        }
        else
        {
            if (trans != null && !PayeeEnum.Account.equals(trans.getPayee()) && !trans.getMSISDN().equals("0"))
            {
                // check 1, both of them need to be filled
                if (trans.getAcctNum().length() == 0 && trans.getMSISDN().length() == 0)
                {
                    throw new IllegalStateException("Please fill up both Account Number "
                        + "and Mobile Number or just Mobile Number. ");
                }
                // check 2, has account num but no MSISDN
                else if (trans.getAcctNum().length() > 0 && trans.getMSISDN().length() == 0
                    || trans.getAcctNum().length() > 0 && trans.getMSISDN().equals("0"))
                {
                    throw new IllegalStateException("Please fill in Mobile Number field. ");
                }
                // check 4, no account but has MSISDN
                else if (trans.getAcctNum().length() == 0 && trans.getMSISDN().length() > 0)
                {
                    setupAccountAndSubscriberByMSISDN(ctx, trans);

                }
                /*
                 * check 5, both exist, need to check if MSISDN actually belongs to this
                 * account
                 */
                else
                {
                    verifyIfMSISDNBelongs(ctx, trans);
                }
            }
            else if (trans != null)
            {
                validateAccount(ctx, trans);
            }

            if (trans != null)
            {

                Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
                if (sub != null)
                {
                    LogSupport.debug(ctx, this, "The subscriber is not null, the type is " + sub.getSubscriberType());
                    if (!sub.getMSISDN().equals(trans.getMSISDN()))
                    {
                        LogSupport.debug(ctx, this, "The subscriber MSISN in context is not equals "
                            + "to the one on the trasnaction...looking for the right one...");
                        try
                        {
                            sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx,trans.getMSISDN(), trans.getTransDate());
                        }
                        catch (final HomeException e1)
                        {
                            LogSupport.crit(ctx, this, "Home Exception : " + e1.getMessage(), e1);
                        }
                    }
                    validateBalanceTransfer(ctx, trans, sub);
                }
            }
        }
    }


    /**
     * Validates if the balance transfers are properly set up.
     *
     * @param ctx
     *            The context to get all the information
     * @param trans
     *            the transaction to validate
     * @param sub
     *            the subscriber for validation
     */
    private void validateBalanceTransfer(final Context ctx, final Transaction trans, final Subscriber sub)
    {
        if (CoreTransactionSupportHelper.get(ctx).isBalanceTransfer(ctx, trans)
            && sub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID) && !trans.isInternal())
        {
            throw new IllegalStateException("Can't activate a balance transfer from a postpaid subscriber. ");
        }
        if (CoreTransactionSupportHelper.get(ctx).isBalanceTransferDebit(ctx, trans)
            && sub.getSubscriberType().equals(SubscriberTypeEnum.PREPAID))
        {
            throw new IllegalStateException("Balance Transfer(Debit) is only for postpaid subscriber");
        }
    }


    /**
     * Verifies if MSISDN actually belongs to this account.
     *
     * @param ctx
     *            the context to get information from the account and subscriber
     * @param trans
     *            the transaction to validate
     */
    private void verifyIfMSISDNBelongs(final Context ctx, final Transaction trans)
    {
        Account acct = (Account) ctx.get(Account.class);

        if (acct == null || !SafetyUtil.safeEquals(acct.getBAN(), trans.getBAN()))
        {
            acct = getAccount(trans);
            ctx.put(Account.class, acct);
        }
        
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        // Try based on subscriber
        if (sub == null || !sub.getMSISDN().equals(trans.getMSISDN()))
        {
            sub = getSubscriber(ctx, trans);
            ctx.put(Subscriber.class, sub);
        }

        if (sub == null || !sub.getBAN().equals(trans.getAcctNum()))
        {
            String msgPrefix = "Input Mobile Number " + trans.getMSISDN()
                                    + " does not belong to input Account Number " + trans.getAcctNum();
            if (sub == null)
            {
                throw new IllegalStateException(msgPrefix + ". The mobile number is not associated with a subscriber.");
            }
            else
            {
                throw new IllegalStateException(msgPrefix + ". The mobile number is associated with Account Number " + sub.getBAN());
            }
        }
    }


    /**
     * Puts the account and subscriber in the context based on the MSISDN attribute from
     * the transaction.
     *
     * @param ctx
     *            the context to setup the beans
     * @param trans
     *            the transaction to get information
     */
    private void setupAccountAndSubscriberByMSISDN(final Context ctx, final Transaction trans)
    {
        String accountNum = null;

        final String msisdn = trans.getMSISDN();
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        try
        {
            if (sub == null)
            {
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
            }
        }
        catch (final HomeException exception)
        {
            final IllegalStateException newException = new IllegalStateException(exception.getMessage());
            newException.initCause(exception);

            throw newException;
        }

        if (sub == null)
        {
            throw new IllegalStateException("Mobile Number is invalid. No subscriber has been found");
        }

        accountNum = sub.getBAN();

        if (accountNum == null)
        {
            throw new IllegalStateException("Mobile Number is invalid. Wrong subscriber account number");
        }

        trans.setAcctNum(accountNum);
        ctx.put(Subscriber.class, sub);
    }


    /**
     * Looks up the Subscriber based on ID and gets the Account based on that subscriber
     * and puts them on the Context. If The subscriber is found and the transaction MSISDN
     * doesn't match the subscriber's MSISDN The transaction will get modified to the
     * right MSISDN.
     *
     * @param ctx
     *            the context where the account and subscriber will be
     * @param trans
     *            the transaction to match in the context
     * @throws IllegalStateException
     *             if no subscriber ID is found in the transaction attribute or the
     *             subscriber is not found
     * @return The subscriber setup in th econtext
     */
    private Subscriber setupAccountAndSubscriber(final Context ctx, final Transaction trans)
    {
        Subscriber txnSubscriber = null;

        if (PayeeEnum.Subscriber.equals(trans.getPayee()))
        {
            if (!matchSubscriber(ctx, trans) && trans.getSubscriberID() != null && trans.getSubscriberID().length() > 0)
            {
                try
                {
                    txnSubscriber = SubscriberSupport.lookupSubscriberForSubId(ctx, trans.getSubscriberID());
                    if (txnSubscriber == null)
                    {
                        throw new IllegalStateException("Subscriber " + trans.getSubscriberID()
                            + " not found in the system");
                    }
                    final Account acct = txnSubscriber.getAccount(ctx);
                    if (acct == null)
                    {
                        throw new IllegalStateException("Subscriber " + trans.getSubscriberID()
                            + " doesn't have a corresponding account");
                    }
                    ctx.put(Subscriber.class, txnSubscriber);
                    ctx.put(Account.class, acct);
                }
                catch (final HomeException e)
                {
                    LogSupport.major(ctx, this,
                        "Home Exception when trying to get the subscriber or account for transaction " + trans, e);
                    throw new IllegalStateException(e);
                }
            }
            else
            {
                txnSubscriber = (Subscriber) ctx.get(Subscriber.class);
            }
        }
        else
        {
            validateAccount(ctx, trans);
        }

        return txnSubscriber;
    }


    /**
     * Verifies if the Subscriber and account in the transaction matches with the
     * subscriber ID.
     *
     * @param ctx
     *            the operating context
     * @param trans
     *            the transaction to verify
     * @return true if matches
     */
    private boolean matchSubscriber(final Context ctx, final Transaction trans)
    {
        boolean matches = false;

        final Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        final Account acct = (Account) ctx.get(Account.class);

        // First verifies if the account and subscriber are not null
        matches = sub != null && acct != null;

        // Verifies if the transaction information matches whatever we have on the Context
        matches = matches && trans.getSubscriberID().equals(sub.getId()) && trans.getBAN().equals(acct.getBAN());

        return matches;
    }


    /**
     * if the transaction is at account level, validate and setup the account in the
     * context.
     *
     * @param ctx
     *            The Context to setup the account
     * @param trans
     *            The transaction for validation
     */
    private void validateAccount(final Context ctx, final Transaction trans)
    {
        if (trans.getPayee() == PayeeEnum.Account)
        {
            if (trans.getAcctNum().length() == 0)
            {
                throw new IllegalStateException("Please fill in Account Number field. ");
            }
            ctx.put(Account.class, getAccount(trans));
        }
    }


    /**
     * Gets the account based on the transaction ban.
     *
     * @param trans
     *            the transaction to get the BAN
     * @return The found account
     */
    public Account getAccount(final Transaction trans)
    {
        Account acct = null;

        final Home acctHome = (Home) getContext().get(AccountHome.class);
        if (acctHome == null)
        {
            throw new IllegalStateException("Create failed. Cannot find AccountHome in context.");
        }

        try
        {
            acct = (Account) acctHome.find(getContext(), trans.getAcctNum());
        }
        catch (final Exception e)
        {
            new MinorLogMsg(this, e.getMessage(), e).log(getContext());
            throw new IllegalStateException("Fail to query account table");
        }

        if (acct == null)
        {
            throw new IllegalStateException("Create failed. Cannot find account with acctnumber=" + trans.getAcctNum());
        }
        return acct;
    }


    /**
     * Gets the subscriber based on the MSISDN recorded on the transaction.
     *
     * @param ctx
     *            the context to look for information
     * @param trans
     *            the current transaction for MSISDN information
     * @return the subscriber found in the application
     */
    public Subscriber getSubscriber(final Context ctx, final Transaction trans)
    {
        /*
         * override lookup if Subscriber in the context is put by ProcessSubscriberVisitor
         * because this will improve performance and ProcessSubscriberVisitor will do
         * charging based on subscriber it put in the Context, so the forced lookup will
         * not solve any issue in this case.
         */
    	Subscriber sub = null; 
            try
            {
                // force a subscriber look up because data is otherwise outdated
                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "looking up Subscriber by msisdn " + trans.getMSISDN(), null).log(ctx);
                }
                
                sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, trans.getMSISDN(), trans.getSubscriptionTypeId(), trans.getTransDate());
            }
            catch (final HomeException exception)
            {
                final IllegalStateException newException = new IllegalStateException(exception.getMessage());
                newException.initCause(exception);

                throw newException;
            }
 
        return sub;
    }


    /**
     * Validates and sets the correct amount This is made by the GUI as well but the
     * Transaction is made in the back end so setting this for backend process.
     *
     * @param ctx
     *            the context to get information
     * @param transaction
     *            the transaction to validate
     * @throws HomeException
     *             if something is wrong when trying to get the adjustment type
     *             information
     */
    private void validateAmount(final Context ctx, final Transaction transaction) throws HomeException
    {
        final PMLogMsg pmLogMsg = new PMLogMsg("Transaction", "AcctNumMsisdnValidator: Validate Amout", "");

        final AdjustmentType adjType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, transaction
            .getAdjustmentType());

        if (adjType != null)
        {
            final AdjustmentTypeActionEnum action = adjType.getAction();

            if (AdjustmentTypeActionEnum.CREDIT.equals(action)  || AdjustmentTypeActionEnum.DEBIT.equals(action))
            {
                transaction.setAction(action);
            }
        }

        if (transaction.getAmount() > 0 && AdjustmentTypeActionEnum.CREDIT.equals(transaction.getAction()))
        {
            transaction.setAmount(transaction.getAmount() * -1);
        }

        if (transaction.getAmount() < 0 && AdjustmentTypeActionEnum.DEBIT.equals(transaction.getAction()))
        {
            transaction.setAmount(transaction.getAmount() * -1);
        }
        
        pmLogMsg.log(ctx);

    }

}
