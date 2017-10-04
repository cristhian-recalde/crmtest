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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

/**
 * This class handles forwarding Account Level Charge transactions to the account's
 * active (postpaid) subscribers. 
 *
 * TODO:
 * 01/18/2005: Eventually we want to separate the Transaction Proportioning 
 * Logic to a class that inherits from AbstractProportioningCalculator.  That 
 * would leave the transaction redirection to this processor.
 * Clean up of the AbstractTransactionProcessor will need to be done (since 
 * most of the shared methods will have been moved to the 
 * AbstractProportioningCalculator).   
 * 
 * @author Angie Li
 */
public class AccountChargeTransactionProcessor
    extends AbstractTransactionProcessor
{
	public static final String TRANSACTION_TYPE = "charge";

    /**
     * Creates a new AccountChargeTransactionProcessor
     * At this time the only type of Charges that are supported as 
     * Account Level Transactions are:
     *  Payment Plan Loan Charges
     *  Payment Plan Loan Reversal - for exiting Payment Plan prematurely
     *  Payment Plan Loan Allocation - to balance every payment plan overpayment with a charge
     *
     * @param delegate The home to delegate to.
     * @param ctx The operating context.
     */
    public AccountChargeTransactionProcessor(final Home delegate)
    {
        super(delegate);
        adjustmentTypes_.add(AdjustmentTypeEnum.PaymentPlanLoanAdjustment);
        adjustmentTypes_.add(AdjustmentTypeEnum.PaymentPlanLoanReversal);
        adjustmentTypes_.add(AdjustmentTypeEnum.PaymentPlanLoanAllocation);
        adjustmentTypes_.add(AdjustmentTypeEnum.LateFee);
    }

    /**
     * Handle the non-pooled Account Charge case.  The Charge will be split 
     * evenly across all active postpaid subscribers.
     *
     * Charge Transactions at this level are applied to the following subscribers (the 
     * account ACCT is from the transaction given):
     *  1) ACCT's immediate postpaid subscribers
     *  2) The subscriber in all non-responsible accounts with ACCT as the PARENTBAN 
     * (All responsible accounts with ACCT as a PARENTBAN are skipped).
     *
     * @param transaction The account charge.
     *
     * @return Transaction The original account charge.
     */
    @Override
    public Transaction handleRegularAccountTransaction(Context ctx,final Transaction trans)
        throws HomeException
    {
        final Account account = AccountSupport.getAccount(ctx, trans.getBAN());
        final long amount = Math.abs(trans.getAmount());
        CRMSpid spid = AccountSupport.getServiceProvider(ctx, account);

        final Collection<Subscriber> activeSubs = getActivePostpaidSubscribers(ctx,account, spid);
        if (activeSubs == null || activeSubs.size() <= 0)
        {
            String msg = "No active and non-prepaid subscriber found in account: " + trans.getAcctNum();
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);
        }
        
        int numSubs = activeSubs.size();
        
        Subscriber chargeRecipient = getFirstPostpaidSubscriber(ctx, spid, activeSubs);
        if ( chargeRecipient == null )
        {
            final String msg = "No postpaid subscriber in account "+account.getBAN()+" is eligible to receive charge of "+amount;
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);            
        }
        
        chargeRecipient.setContext(ctx);

		List<Long> success = new ArrayList<Long>();
		List<Long> failed = new ArrayList<Long>();

        //Change the transaction amount to positive value so taht comparisons are simplified
        double ratio = Math.pow(numSubs, -1); // 1/numSubs
        
        if (isAmountSmallerThanLowestCurrencyUnit(ctx, amount, numSubs))
        {
			submitTransaction(ctx, trans, amount, chargeRecipient, success,
			    failed);
        }
        else
        {
            long chargeRemaining = amount;  // To ensure there is no surplus

            // Distribute the CHARGE among the subscribers (except the one assigned for getting the remaining CHARGE)
            Collection<Subscriber> otherSubs = CollectionSupportHelper.get(ctx).findAll(
                    ctx, activeSubs, 
                    new Not(new EQ(SubscriberXInfo.ID, chargeRecipient.getId())));
            
            for (Subscriber sub : otherSubs)
            {
                long subCharge = Math.min(Math.round(amount * ratio), chargeRemaining);
				submitTransaction(ctx, trans, subCharge, sub, success, failed);
                chargeRemaining -= subCharge;
            }

            // Create a transaction for the subscriber assigned for getting any remaining CHARGE.
            if (chargeRemaining != 0)
            {
				submitTransaction(ctx, trans, chargeRemaining, chargeRecipient,
				    success, failed);
            }
        }
        
		generateNoteForRegularTransaction(ctx, account, trans, success, failed);

        /*
		 * [Cindy Wong] Throws an exception to make sure the transaction
		 * handling ends here.
		 */
		if (!failed.isEmpty())
		{
			throw new HomeException(
			    "Encountered one or more errors while splitting the transaction.");
		}

        if (CoreTransactionSupportHelper.get(ctx).isPaymentPlanLoanAdjustment(ctx, trans) 
                && amount >= 0)
        {
            /* 
             * For Payment Plan Charges, we need to increment the Installments counter 
             * in the account profile. Only count transactions with amounts > 0 since
             * PaymentPlanLoanAdjustment can be a credit adjustment too.
             */
            incrementPaymentPlanInstallmentCounter(ctx, trans);
        }
        
        return trans;  // Return the original transaction
    }

    
    /**
     * Account Level Charge transactions are not supported for Pooled Accounts 
     *
     * @param trans The charge.
     *
     * @return Transaction The original transaction.
     */
    @Override
    public Transaction handleGroupAccountTransaction(Context ctx,final Transaction trans)
        throws HomeException
    {
        String msg = "Account Level Charge (debit) Transactions are not supported for Pooled Account types";
        new MinorLogMsg(this, msg, null).log(ctx);
        throw new IllegalArgumentException( msg);
    }

    /**
     * Increments the Payment Plan Installment Counter and stores the account
     * @param ctx
     * @param trans
     * @throws HomeException
     */
    private void incrementPaymentPlanInstallmentCounter(Context ctx, Transaction trans) throws HomeException
    {
        Home acctHome = (Home) ctx.get(AccountHome.class);
        if (acctHome != null)
        {
            //Account is retrieved again so that we are sure that only this property is changed and stored.
            Account account = AccountSupport.getAccount(ctx, trans.getBAN());
            account.setPaymentPlanInstallmentsCharged(account.getPaymentPlanInstallmentsCharged() + 1);
            acctHome.store(ctx, account);
            
            if(LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this,
                        "Account " + account.getBAN()+
                        ": incremented account's Payment Plan Installment count to " 
                        + account.getPaymentPlanInstallmentsCharged(), null).log(ctx);
            }
        }
        else
        {
            throw new HomeException("No Account Home was found in the context.");
        }
    }

	/**
	 * @param trans
	 * @return
	 * @see com.redknee.app.crm.transaction.AbstractTransactionProcessor#getTransactionType(com.redknee.app.crm.bean.core.Transaction)
	 */
	@Override
	public String getTransactionType(Transaction trans)
	{
		return TRANSACTION_TYPE;
	}

}
