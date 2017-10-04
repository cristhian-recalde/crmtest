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
package com.trilogy.app.crm.home.account;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.CoreCrmConstants;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.payment.PaymentPlan;
import com.trilogy.app.crm.bean.payment.PaymentPlanActionEnum;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistory;
import com.trilogy.app.crm.bean.payment.PaymentPlanHistoryHome;
import com.trilogy.app.crm.bean.payment.PaymentPlanHome;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.invoice.PaymentPlanInvoiceCalculation;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.state.InOneOfStatesPredicate;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.PaymentPlanSupport;
import com.trilogy.app.crm.support.PaymentPlanSupportHelper;


/**
 * Upon activating the Payment Plan feature on an account, CRM shall change the Credit
 * Category field value to Payment Plan Credit Category, so the Dunning process will be
 * based on these values instead of the SPID level values. This class takes care of: A)
 * transferring balances, B) updating account and subscriber states (to Active), C)
 * adjusting subscriber credit limits
 *
 * @author angie.li@redknee.com
 */
public class AccountPaymentPlanTransferBalanceHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>AccountPaymentPlanTransferBalanceHome</code>.
     *
     * @param ctx
     *            The operating context.
     * @param delegate
     *            Delegate of this home.
     */
    public AccountPaymentPlanTransferBalanceHome(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * If Payment Plan is enabled as a Licensed feature, check if Payment Plan is selected
     * during Account Store. If payment plan is not licensed, then this method, defers to
     * the delegate store method.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The account being updated.
     * @return The updated account.
     * @throws HomeException
     *             Thrown if there are problems updating the account, either by this
     *             decorator or further down the pipeline.
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        Account createdAccount = null;
        if (PaymentPlanSupportHelper.get(ctx).isEnabled(ctx))
        {
            try
            {
                final Account newAccount = (Account) obj;
                final Account updatedAccount = processAccount(ctx, newAccount);
                if (updatedAccount != null)
                {
                    createdAccount = (Account) super.store(ctx, updatedAccount);
                }
                else
                {
                    createdAccount = (Account) super.store(ctx, obj);
                }
            }
            catch (final IllegalStateException ex)
            {
                // We want all errors to be listed in the GUI, so they have to be thrown
                // as CompoundIllegalStateExceptions
                if (ex instanceof CompoundIllegalStateException)
                {
                    throw new HomeException(ex.getMessage(), ex);
                }
                throw new HomeException(ex.getMessage(), new CompoundIllegalStateException(ex));
            }
        }
        else
        {
            createdAccount = (Account) super.store(ctx, obj);
        }
        return createdAccount;
    }


    /**
     * Returns the an updated Account if the PaymentPlan has changed. Account will be
     * updated with a new PaymentPlanAmount. Otherwise returns null. If a payment plan was
     * chosen, then the subscribers are visited to apply credits to match the outstanding
     * balances, as well as decreasing their credit limit. If the payment plan has been
     * deselected, then subscribers are visited to apply charges from the payment plan
     * balance, as well as reverting their credit limit.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account being processed.
     * @return The processed account.
     * @throws HomeException
     *             Thrown if there are problems processing the payment plan update.
     */
    private Account processAccount(final Context ctx, final Account account) throws HomeException
    {
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        if (AccountSupport.hasPaymentPlanChanged(ctx, account))
        {
            /*
             * If a valid payment plan has been chosen, transfer the balance
             */
            if (AccountSupport.isSettingPaymentPlan(ctx, account))
            {
                new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_INSTALLATION_ATTEMPT).log(ctx);

                installPaymentPlan(ctx, account, el);

                if (el.getSize() != 0)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_INSTALLATION_FAILURE).log(ctx);
                }
                else
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_INSTALLATION_SUCCESS).log(ctx);
                }
            }
            else if (PaymentPlanSupportHelper.get(ctx).isValidPaymentPlan(ctx, account.getPaymentPlan()))
            {
                final IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(
                    AccountXInfo.PAYMENT_PLAN,
                    "Switching from a valid Payment Plan to another Payment Plan is prohibited.");
                el.thrown(newException);
            }
            else
            {
                /*
                 * If PaymentPlan has been removed (i.e. changed to and invalid Payment
                 * Plan) then Transfer the Payment Plan Outstanding Balance back to this
                 * account's subscribers.
                 */
                new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_REMOVAL_ATTEMPT).log(ctx);

                removePaymentPlan(ctx, account, el);

                if (el.getSize() != 0)
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_REMOVAL_FAILURE).log(ctx);
                }
                else
                {
                    new OMLogMsg(Common.OM_MODULE, Common.OM_PAYMENT_PLAN_REMOVAL_SUCCESS).log(ctx);
                }
            }
        }
        else
        // Payment Plan hasn't changed
        {
            return null;
        }
        el.throwAll();

        return account;
    }


    /**
     * Transfer the Total Outstanding Balance to the Payment Plan financial bucket.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account to install payment plan.
     * @param el
     *            Exception listener.
     * @throws HomeException
     *             Thrown if there are problems enrolling the account in payment plan.
     */
    private void installPaymentPlan(Context ctx, final Account account, final CompoundIllegalStateException el)
        throws HomeException
    {
        ctx = ctx.createSubContext();
        String sessionKey = CalculationServiceSupport.createNewSession(ctx);
        try
        {
            // Lookup the Total Outstanding Balance = Last Invoice Balance - Payments to date
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            Invoice previousInvoice = null;
            try
            {
                previousInvoice = service.getMostRecentInvoice(ctx, account.getBAN());
            }
            catch (CalculationServiceException e)
            {
                new MinorLogMsg(TPSSupport.class, "Exception while fetching Most recent invoice for account", e).log(ctx);
            }
            if (previousInvoice == null)
            {
                throw new HomeException("This account may not enroll in Payment Plan, since it doesn't have an Invoice.");
            }
            final long totalPaymentsReceived = getTotalPayments(ctx, account);
            /*
             * The fix for TT5112327176 has the Total Outstanding amount = Total Amount +
             * Current Tax Amount + Discount Amount from the Invoice. So there is no need to
             * add them manually.
             */
            final long totalOutstandingBalance = previousInvoice.getTotalAmount() + totalPaymentsReceived;

            if (totalOutstandingBalance <= 0)
            {
                final IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(
                    AccountXInfo.PAYMENT_PLAN, "Cannot enroll account with no Accumulated Balance Owing into Payment Plan.");
                el.thrown(newException);
            }
            else
            {
                // Transfer the Total Outstanding Balance to the Payment Plan financial bucket
                account.setPaymentPlanAmount(totalOutstandingBalance);
                final int numOfMonths = PaymentPlanSupportHelper.get(ctx).getPaymentPlanNumberOfPayments(ctx, account.getPaymentPlan());
                final long monthlyPayments = (long) Math.ceil((double) totalOutstandingBalance / numOfMonths);
                account.setPaymentPlanMonthlyAmount(monthlyPayments);
                account.setPaymentPlanInstallmentsCharged(0);

                /*
                 * [Cindy Wong] 2008-04-01: Payment plan start date now denotes the start date
                 * of the current payment plan.
                 */
                account.setPaymentPlanStartDate(new Date());

                if (LogSupport.isDebugEnabled(ctx))
                {
                    new DebugLogMsg(this, "Account " + account.getBAN() + ": Transfered Total Outstanding Balance of "
                        + totalOutstandingBalance + " to Payment Plan.", null).log(ctx);
                }

                processSubscribers(ctx, account, totalOutstandingBalance, true, el);

                /*
                 * Enrolling in a Payment Plan changes the Account (all its non-responsible
                 * accounts) state to ACTIVE.
                 */
                final Account processedAccount = processNonResponsibleSubAccounts(ctx, account);

                // Write to Payment Plan History
                writePaymentPlanEnrollmentRecord(ctx, account);
                
                // Log Payment Plan Activation ER
                ERLogger.generatePaymentPlanActivationEr(ctx, processedAccount.getSpid(), processedAccount.getBAN(),
                    processedAccount.getPaymentPlan(), 1, totalOutstandingBalance, numOfMonths, 0);

                // Write to subscriber note
                final String notemsg = "Account has enrolled in a Payment Plan.\n" + processedAccount.getPaymentPlan()
                    + " - " + PaymentPlanSupportHelper.get(ctx).getPaymentPlanName(ctx, processedAccount.getPaymentPlan());
                NoteSupportHelper.get(ctx).addAccountNote(ctx, processedAccount.getBAN(), notemsg, SystemNoteTypeEnum.EVENTS,
                    SystemNoteSubTypeEnum.ACCUPDATE);
            }
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey);
        }
    }


    /**
     * Transfers the Total Remaining Payment Plan Bucket Balance to the Current
     * Outstanding Charges.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to remove payment plan.
     * @param el
     *            Exception listener.
     * @throws HomeException
     *             Thrown if there are problems removing the payment plan.
     */
    private void removePaymentPlan(final Context ctx, final Account account, final CompoundIllegalStateException el)
        throws HomeException
    {
        final Date exitDate = new Date();
        final Date paymentPlanStartDate = account.getPaymentPlanStartDate(ctx, exitDate);

        final PaymentPlanInvoiceCalculation invoiceCalculation = new PaymentPlanInvoiceCalculation(ctx, account
            .getBAN(), paymentPlanStartDate, exitDate);
        final long remainingPaymentPlanBalance = -invoiceCalculation.getPaymentPlanLoanRemainder();
        account.setPaymentPlanAmount(0);
        account.setPaymentPlanMonthlyAmount(0);
        // We don't want the last invoice for payment plan to show up as "Installment 0 of
        // X"
        // account.setPaymentPlanInstallmentsCharged(0);
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Account " + account.getBAN()
                + ": Has deselected the Payment Plan option with a remaining balance of $"
                + remainingPaymentPlanBalance + ".", null).log(ctx);
        }

        processSubscribers(ctx, account, remainingPaymentPlanBalance, false, el);

        //Write Payment Plan History Record for Exit
        long origPaymentPlan = account.getPaymentPlan();
        final Account oldAccount = AccountSupport.getAccount(ctx, account.getBAN());
        if (oldAccount != null)
        {
            origPaymentPlan = oldAccount.getPaymentPlan();
        }
        writePaymentPlanExitRecord(ctx, account.getBAN(), exitDate, origPaymentPlan, remainingPaymentPlanBalance );
        
        // Log Payment Plan De-activation ER
        final int numOfMonths = PaymentPlanSupportHelper.get(ctx).getPaymentPlanNumberOfPayments(ctx, account.getPaymentPlan());
        ERLogger.generatePaymentPlanActivationEr(ctx, account.getSpid(), account.getBAN(), origPaymentPlan, 2, 0,
            numOfMonths, remainingPaymentPlanBalance);

        // Write to subscriber note
        final String notemsg = "Account has been removed from Payment Plan.";
        NoteSupportHelper.get(ctx).addAccountNote(ctx, account.getBAN(), notemsg, SystemNoteTypeEnum.EVENTS, SystemNoteSubTypeEnum.ACCUPDATE);
    }


    /**
     * If entering Payment Plan, process all active subscribers under this account by 1)
     * applying credit for the Payment Plan loan, and 2) by lowering their credit limits.
     * If exiting Payment Plan, process all active subscribers to: 1) make charges against
     * them accumulating to the remaining Payment Plan Loan Balance, and 2) reverting
     * their credit limits to their original amounts.
     *
     * @param ctx
     *            The operating context
     * @param account
     *            account entering/exiting payment plan
     * @param balance
     *            amount that is being transfered to/from payment plan bucket
     * @param entering
     *            true= account is entering Payment Plan, false=account is exiting Payment
     *            Plan option.
     * @param el
     *            Exception listener
     */
    private void processSubscribers(final Context ctx, final Account account, final long balance,
        final boolean entering, final CompoundIllegalStateException el)
    {
        try
        {
            if (entering)
            {
                /*
                 * Apply an Account level Credit for the totalOutstandingBalance to date.
                 * This will propagate to all subscribers and should result in zero-ing
                 * out their balances.
                 */
                createPaymentPlanLoanCreditForSubscribers(ctx, balance, account);
            }
            else
            {
                /*
                 * Apply an Account level Charge for the remaining Payment Plan Loan
                 * Balance to date. This will propagate to all active subscribers and
                 * should split the charge evenly amongst them all. Please see the method
                 * notes.
                 */
                if (balance != 0)
                {
                    createPaymentPlanLoanReversalForSubscribers(ctx, balance, account);
                }
            }
        }
        catch (final HomeException hEx)
        {
            final String type;
            if (entering)
            {
                type = "Credit";
            }
            else
            {
                type = "Charge";
            }
            // Catch exception so that it will process all subsequent subscribers
            // properly.
            new MinorLogMsg(this, "ERROR creating transaction for Payment Plan Loan " + type + " for Account "
                + account.getBAN() + " Home Exception thrown:" + hEx.getMessage(), null).log(ctx);

            final IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(
                AccountXInfo.PAYMENT_PLAN, "Applying Payment Plan Loan " + type + " to this account failed. "
                    + hEx.getMessage());
            newException.initCause(hEx);
            el.thrown(newException);
        }

        /*
         * If entering Payment Plan, apply a Credit Limit Decrease for all active
         * subscribers. If exiting Payment Plan, revert to original Credit Limit for all
         * active subscribers.
         */
        try
        {
            modifyCreditLimitForActiveSubscribers(ctx, account, entering, el);
        }
        catch (final HomeException hEx)
        {
            new MinorLogMsg(this, "ERROR modifying credit limit for subscribers in account=" + account.getBAN()
                + " Home Exception thrown:" + hEx.getMessage(), null).log(ctx);

            final IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(
                AccountXInfo.PAYMENT_PLAN, "Failed to modify credit limit for subscribers in account="
                    + account.getBAN() + ". " + hEx.getMessage());
            newException.initCause(hEx);
            el.thrown(newException);
        }
    }


    /**
     * Create an account level Transaction for Payment Plan Loan Credit.
     *
     * @param ctx
     *            The operating context.
     * @param totalOutstandingBalance
     *            Total balance outstanding.
     * @param account
     *            The account to install payment plan.
     * @throws HomeException
     *             Thrown if there are problems creating payment plan loan credit
     *             transactions for subscribers.
     */
    private void createPaymentPlanLoanCreditForSubscribers(final Context ctx, final long totalOutstandingBalance,
        final Account account) throws HomeException
    {
        final AdjustmentType paymentPlanCreditType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
            AdjustmentTypeEnum.PaymentPlanLoanCredit);
        final String glCode = paymentPlanCreditType.getGLCodeForSPID(ctx, account.getSpid());

        // Make an account level "payment" for this
        // Create transaction that will credit subscribers the Payment Plan Loan Credit
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }
        transaction.setBAN(account.getBAN());
        transaction.setResponsibleBAN(account.getResponsibleBAN());
        transaction.setAmount(-1 * totalOutstandingBalance);
        transaction.setTaxPaid(0);
        transaction.setAdjustmentType(paymentPlanCreditType.getCode());
        transaction.setAction(paymentPlanCreditType.getAction());
        transaction.setGLCode(glCode);
        transaction.setTransDate(new Date());
        transaction.setPayee(PayeeEnum.Account);
        transaction.setAgent(((User) ctx.get(Principal.class, new User())).getId());

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Creating Payment Plan loan credit transaction " + transaction);
        }

        final Home home = (Home) ctx.get(Common.FULL_TRANSACTION_HOME);
        if (home == null)
        {
            throw new HomeException("System error: no TransactionHome found in context.");
        }
        home.create(ctx, transaction);

    }


    /**
     * The Payment Plan Reversal Process: 1) Void all charges made during this current
     * invoicing period 2) Create the Payment Plan Loan Reversal Transaction for the
     * remaining payment plan bucket amount The remaining payment Plan bucket amount
     * includes the charges amount still owed. That is why we do step 1. if we didn't (and
     * there were Payment Plan Charges made during the invoicing period) and we just did
     * step 2, we would be overcharging.
     *
     * @param ctx
     *            The operating context.
     * @param remainingOutstandingBalance
     *            The remaining balance of the payment plan loan bucket.
     * @param account
     *            The account being removed from payment plan.
     * @throws HomeException
     *             Thrown if there are problems with creating payment plan loan reversal
     *             transactions for subscribers.
     */
    private void createPaymentPlanLoanReversalForSubscribers(final Context ctx, final long remainingOutstandingBalance,
        final Account account) throws HomeException
    {

        // Nullify any Payment Plan Charges done in the current invoicing period
        final long chargesToVoid = getTotalUnpaidPaymentPlanCharges(ctx, account);

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Unpaid payment plan charges = " + chargesToVoid);
        }

        if (chargesToVoid > 0)
        {
            PaymentPlanSupportHelper.get(ctx).createPaymentPlanLoanAdjustment(ctx, chargesToVoid, account, new Date(),
                AdjustmentTypeActionEnum.CREDIT);
        }

        // Create Reversal Transaction (Replaces the charges)
        createPaymentPlanLoanReversalTransaction(ctx, remainingOutstandingBalance, account);
    }


    /**
     * Returns the total amount of Payment Plan Charges on this account during the current
     * invoicing period (since the last invoice date until today).
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be removed from payment plan.
     * @return Total amount of payment plan charges which are not yet paid.
     */
    private long getTotalUnpaidPaymentPlanCharges(final Context ctx, final Account account)
    {
        final Date now = new Date();
        long unpaidCharges = 0;
        try
        {
            final Date paymentPlanStartDate = account.getPaymentPlanStartDate(ctx, now);
            
            CalculationService service = (CalculationService) ctx.get(CalculationService.class);
            
            unpaidCharges = service.getAccountPaymentPlanUnpaidAdjustments(ctx, account.getBAN(), paymentPlanStartDate, now, now);

            
        }
        catch (final Exception exception)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(exception.getClass().getSimpleName());
                sb.append(" caught in ");
                sb.append("AccountPaymentPlanTransferBalanceHome.getTotalPaymentPlanChargesSinceLastInvoice(): ");
                if (exception.getMessage() != null)
                {
                    sb.append(exception.getMessage());
                }
                LogSupport.debug(ctx, this, sb.toString(), exception);
            }

        }
        return Math.max(0, unpaidCharges);
    }


    /**
     * Returns the total amount of Standard Payments made to this account since the date
     * indicated.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            The account in question.
     * @param start
     *            Start date of calculation.
     * @return Total amount of payments made to this account since the provided date.
     */
    private long getTotalPayments(final Context ctx, final Account account)
    {
        CalculationService service = (CalculationService) ctx.get(CalculationService.class);
        long accountPaymentRecieved = 0;
        try
        {
            accountPaymentRecieved = service.getAccountPaymentsReceived(ctx, account
                    .getBAN(), CalendarSupportHelper.get(ctx).getRunningDate(ctx));
        }
        catch (CalculationServiceException e)
        {
            new MinorLogMsg(this, "Exception while fetching Subscriber Invoice for subscriber", e);
        }
        return accountPaymentRecieved;
    }


    /**
     * Create an account level Transaction for Payment Plan Loan Reversal.
     *
     * @param ctx
     *            The operating context.
     * @param remainingOutstandingBalance
     *            The outstanding payment plan loan balance.
     * @param account
     *            Account to be removed from payment plan.
     * @throws HomeException
     *             Thrown if there are problems creating the transaction.
     */
    private void createPaymentPlanLoanReversalTransaction(final Context ctx, final long remainingOutstandingBalance,
        final Account account) throws HomeException
    {
        final AdjustmentType paymentPlanReversalType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
            AdjustmentTypeEnum.PaymentPlanLoanReversal);
        final String glCode = paymentPlanReversalType.getGLCodeForSPID(ctx, account.getSpid());

        /*
         * Make an account level "charge" for this by creating transaction that will
         * charge subscribers the remainging Payment Plan Loan Balance
         */
        final Transaction transaction;
        try
        {
            transaction = (Transaction) XBeans.instantiate(Transaction.class, ctx);
        }
        catch (Exception exception)
        {
            throw new HomeException("Cannot instantiate transaction bean", exception);
        }
        transaction.setBAN(account.getBAN());
        transaction.setResponsibleBAN(account.getResponsibleBAN());
        transaction.setAmount(remainingOutstandingBalance);
        transaction.setTaxPaid(0);
        transaction.setAdjustmentType(paymentPlanReversalType.getCode());
        transaction.setAction(paymentPlanReversalType.getAction());
        transaction.setGLCode(glCode);
        transaction.setReceiveDate(new Date());
        transaction.setPayee(PayeeEnum.Account);
        transaction.setAgent(((User) ctx.get(Principal.class, new User())).getId());

        if (LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, this, "Creating Payment Plan loan reversal transaction " + transaction);
        }

        final Home home = (Home) ctx.get(CoreCrmConstants.FULL_TRANSACTION_HOME);
        if (home == null)
        {
            throw new HomeException("System error: no TransactionHome found in context.");
        }
        home.create(ctx, transaction);
    }


    /**
     * Returns a collection of Active Subscribers in this account in the following states:
     * Active, NonPayment Suspended, Suspended, Promise to Pay, In Collection, NonPayment
     * Warned, and In Arrears. Applied to the following subscribers (the account ACCT is
     * the given account): 1) ACCT's immediate postpaid subscribers 2) The subscriber in
     * all non-responsible accounts with ACCT as the PARENTBAN (All responsible accounts
     * with ACCT as a PARENTBAN are skipped).
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account in question.
     * @return A collection of all subscribers which payment plan should be applied.
     * @throws HomeException
     *             Thrown if there are problems looking up the subscribers.
     */
    protected Collection getApplySubscribers(final Context ctx, final Account account) throws HomeException
    {
        Collection subs = null;
        try
        {
            subs = AccountSupport.getNonResponsibleSubscribers(ctx, account);
            
            And predicate = new And();
            predicate.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
            predicate.add(new InOneOfStatesPredicate(
                    SubscriberStateEnum.IN_ARREARS, 
                    SubscriberStateEnum.NON_PAYMENT_WARN,
                    SubscriberStateEnum.IN_COLLECTION, 
                    SubscriberStateEnum.PROMISE_TO_PAY,
                    SubscriberStateEnum.SUSPENDED, 
                    SubscriberStateEnum.NON_PAYMENT_SUSPENDED,
                    SubscriberStateEnum.ACTIVE));
            subs = CollectionSupportHelper.get(ctx).findAll(ctx, subs, predicate);
        }
        catch (final Exception exp)
        {
            throw new HomeException("Failed to retrieve all Active Subscribers from account=" + account.getBAN(), exp);
        }
        return subs;
    }


    /**
     * Apply a Credit Limit adjustment for all active subscribers in this account.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account being updated.
     * @param decrease
     *            true=credit limit decrease, false=credit limit undoing decrease
     * @param el
     *            Exception listener
     * @throws HomeException
     *             Thrown if there are problems updating the credit limit.
     */
    private void modifyCreditLimitForActiveSubscribers(final Context ctx, final Account account,
        final boolean decrease, final CompoundIllegalStateException el) throws HomeException
    {
        final Iterator activeSubscribers = getApplySubscribers(ctx, account).iterator();
        Account lookupAccount = account;
        if (!decrease)
        {
            /*
             * Increasing the credit limit only happens on exiting the Payment Plan
             * option. That means the we have to reference the previous Payment Plan ID
             * value, since the current account will not have a Payment Plan ID value set.
             */
            lookupAccount = (Account) ctx.get(AccountConstants.OLD_ACCOUNT);
        }
        // Credit Limit Decrease Factor
        final Home ppHome = (Home) ctx.get(PaymentPlanHome.class);
        final PaymentPlan paymentplan = (PaymentPlan) ppHome.find(ctx, Long.valueOf(lookupAccount.getPaymentPlan()));
        final long clAdjustment = paymentplan.getCreditLimitDecrease();

        while (activeSubscribers.hasNext())
        {
            Subscriber subscriber;
            /*
             * The IndividualAccountAdapterHome's store method overwrites individual
             * account's subscribers with an old unmodified copy of the subscriber. In
             * that case, it's important to use the transient subscriber stored in the
             * account. Else, look up subscribers from the list of subscriber invoices
             */
            if (account.isIndividual(ctx) && account.getIndividualSubscriber(ctx) != null)
            {
                
                subscriber = account.getSubscriber();
            }
            else
            {
                subscriber = (Subscriber) activeSubscribers.next();
            }

            // Write Note to account
            String notemsg;
            if (decrease)
            {
                notemsg = "Subscriber is enrolling in Payment Plan.  Credit Limit will be lowered.";
            }
            else
            {
                notemsg = "Subscriber is removed from Payment Plan.  Credit Limit will be increased.";
            }
            NoteSupportHelper.get(ctx).addSubscriberNote(ctx, subscriber.getId(), notemsg, SystemNoteTypeEnum.EVENTS,
                SystemNoteSubTypeEnum.SUBUPDATE);

            // Adjusts Subscribers Credit Limit by the set percentage
            modifySubscriberCL(ctx, subscriber, clAdjustment, decrease, el);

            if (account.isIndividual(ctx) && account.getSubscriber() != null)
            {
                break;
            }
        }
    }


    /**
     * Modifies this subscriber's Credit Limit.
     *
     * @param ctx
     *            The operating context.
     * @param subscriber
     *            Subscriber to be modified.
     * @param percentage
     *            percentage by which to lower the credit limit.
     * @param decrease
     *            true=lower the credit limit by the percentage, false=raise the credit
     *            limit back to it's original value.
     * @param el
     *            Exception listener.
     */
    private void modifySubscriberCL(final Context ctx, final Subscriber subscriber, final long percentage,
        final boolean decrease, final CompoundIllegalStateException el)
    {
        final double clAdjustmentFactor = 1 - percentage * 0.01;
        final Home subHome = (Home) ctx.get(SubscriberHome.class);
        if (decrease)
        {
            final long newCreditLimit = Math.round(subscriber.getCreditLimit(ctx) * clAdjustmentFactor);
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Credit Limit Adjustment for subscriber=" + subscriber.getId()
                    + " from credit limit=" + subscriber.getCreditLimit(ctx) + " to " + newCreditLimit
                    + " Decreasing by: " + percentage + "%.", null).log(ctx);
            }
            subscriber.setCreditLimit(newCreditLimit);
        }
        else
        {
            final long newCreditLimit = Math.round(subscriber.getCreditLimit(ctx) * Math.pow(clAdjustmentFactor, -1));
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Restoring original Credit Limit for subscriber=" + subscriber.getId()
                    + " from credit limit=" + subscriber.getCreditLimit(ctx) + " to " + newCreditLimit + ".", null)
                    .log(ctx);
            }
            subscriber.setCreditLimit(newCreditLimit);
        }
        try
        {
            subHome.store(ctx, subscriber);
        }
        catch (final HomeException e)
        {
            new MinorLogMsg(this, "Fail to update the credit limit of subscriber=" + subscriber.getId(), e).log(ctx);
            final IllegalPropertyArgumentException newException = new IllegalPropertyArgumentException(
                AccountXInfo.PAYMENT_PLAN, "Failed to modify credit limit for subscriber=" + subscriber.getId() + "."
                    + e.getMessage());
            newException.initCause(e);
            el.thrown(newException);
        }
    }


    /**
     * Returns an account who's state has be set to ACTIVE. Also iterates through the
     * given account and finds all its non-responsible sub-accounts and set their states
     * to Active as well.
     *
     * @param ctx
     *            The operating context.
     * @param account
     *            Account to be processed.
     * @return The processed account.
     * @throws HomeException
     *             Thrown if there are problems processing the account.
     */
    private Account processNonResponsibleSubAccounts(final Context ctx, final Account account) throws HomeException
    {
        // Change account state to ACTIVE once enrolled in Payment Plan if it is in one of
        // the defined states.
        final Collection accountsToActivate = AccountSupport.getNonResponsibleAccounts(ctx, account);
        final Iterator acctIter = accountsToActivate.iterator();
        final Home acctHome = (Home) ctx.get(AccountHome.class);
        if (acctHome == null)
        {
            throw new HomeException("No Account Home was found in the context.");
        }
        while (acctIter.hasNext())
        {
            final Account acctToStore = (Account) acctIter.next();
            if (!account.getBAN().equals(acctToStore.getBAN())
                && EnumStateSupportHelper.get(ctx).isOneOfStates(acctToStore, PaymentPlanSupport.STATES_TO_CHANGE_TO_ACTIVE))
            {
                acctToStore.setState(AccountStateEnum.ACTIVE);
                acctHome.store(ctx, acctToStore);
            }
        }
        if (EnumStateSupportHelper.get(ctx).isOneOfStates(account, PaymentPlanSupport.STATES_TO_CHANGE_TO_ACTIVE))
        {
            account.setState(AccountStateEnum.ACTIVE);
        }
        return account;
    }
    
    /**
     * Create a persistent record of the Payment Plan Enrollment
     * @param context
     * @param account Account that enrolled in Payment Plan
     */
    private void writePaymentPlanEnrollmentRecord(final Context context, final Account account)
    {
        if (PaymentPlanSupportHelper.get(context).isHistoryEnabled(context))
        {
            PaymentPlanHistory record = new PaymentPlanHistory();
            record.setAccountId(account.getBAN());
            record.setRecordDate(account.getPaymentPlanStartDate());
            record.setPaymentPlanId(account.getPaymentPlan());
            record.setAmount(account.getPaymentPlanAmount());
            record.setAction(PaymentPlanActionEnum.ENROLL);

            try
            {
                Home home = (Home) context.get(PaymentPlanHistoryHome.class);
                home.create(record);
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, 
                        "Failure to create Payment Plan History record for enrolling into Payment Plan.  History Record=" 
                        + record,
                        e).log(context);
            }
        }
    }
    
    /**
     * Create a persistent record for Payment Plan Loan Exit
     * @param context
     * @param accountId Account that exited the Payment Plan Loan
     */
    private void writePaymentPlanExitRecord(final Context context, 
            final String accountId, 
            final Date date,
            final long paymentPlanId,
            final long remainingBalance)
    {
        if (PaymentPlanSupportHelper.get(context).isHistoryEnabled(context))
        {
            PaymentPlanHistory record = new PaymentPlanHistory();
            record.setAccountId(accountId);
            record.setRecordDate(date);
            record.setPaymentPlanId(paymentPlanId);
            record.setAmount(remainingBalance);
            record.setAction(PaymentPlanActionEnum.EXIT);

            try
            {
                Home home = (Home) context.get(PaymentPlanHistoryHome.class);
                home.create(record);
            }
            catch (HomeException e)
            {
                new MajorLogMsg(this, 
                        "Failure to create Payment Plan History record for exiting from Payment Plan.  History Record=" 
                        + record,
                        e).log(context);
            }
        }
    }
}
