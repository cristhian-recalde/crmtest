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

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;

/**
 * This class handles Account Level NON-PAYMENT CREDIT transactions.  
 * Account Level adjustments of this kind are divided equally amongst 
 * all active subscribers.
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
public class AccountCreditTransactionProcessor
    extends AbstractTransactionProcessor
{
	public static final String TRANSACTION_TYPE = "credit";

    /**
     * Creates a new TransactionRedirectionHome.
     * Only a Payment Plan Loan Credit is supported at this time.
     *
     * @param delegate The home to delegate to.
     * @param ctx The operating context.
     */
    public AccountCreditTransactionProcessor(final Home delegate)
    {
        super(delegate);
        adjustmentTypes_.add(AdjustmentTypeEnum.PaymentPlanLoanCredit);
        adjustmentTypes_.add(AdjustmentTypeEnum.EarlyReward);
    }

    
    /**
     * Handle the Account Level Credit Transactions to regular (non-grouped) accounts.
     * Transaction is divided evenly and forwarded to each of the active (postpaid) subscribers. 
     * 
     * Credit Transactions at this level are applied to the following subscribers (the 
     * account ACCT is from the transaction given):
     *  1) ACCT's immediate postpaid subscribers
     *  2) The subscriber in all non-responsible accounts with ACCT as the PARENTBAN 
     * (All responsible accounts with ACCT as a PARENTBAN are skipped).
     *
     * @param transaction The account credit.
     *
     * @return Transaction The original account credit transaction.
     */
    @Override
    public Transaction handleRegularAccountTransaction(Context ctx,final Transaction trans)
        throws HomeException
    {
        final Account account = AccountSupport.getAccount(ctx, trans.getBAN());
		CRMSpid spid = AccountSupport.getServiceProvider(ctx, account);
		List<Long> success = new ArrayList<Long>();
		List<Long> failed = new ArrayList<Long>();

        final Collection<Subscriber> subs = getPostpaidSubscribers(ctx, account, spid);
        if (subs == null || subs.size() <= 0)
        {
            String msg = "No active and non-prepaid subscriber found in account: " + trans.getAcctNum();
            new InfoLogMsg(this, msg, null).log(ctx);
            throw new HomeException(msg);
        }
        
        int numSubs = subs.size();
        
        //Change the transaction amount to a positive amount so that comparisons are simplified
        final long creditAmount = Math.abs(trans.getAmount());
        final long totalAmountOwing = getTotalOwingOfSubscribers(ctx, subs);//DZ exclude prepaid
        final long delta;
        final double ratio;
        final Subscriber creditRecipient;
        if (totalAmountOwing > 0 && creditAmount > 0)
        {
            if (creditAmount >= totalAmountOwing) //overcredit
            {   
                delta = (creditAmount - totalAmountOwing) / numSubs;
                ratio = 1.0;
                creditRecipient = getFirstPostpaidSubscriber(ctx, spid, subs);
            }
            else
            {
                delta = 0;
                ratio = (double) creditAmount / totalAmountOwing;
                creditRecipient = getFirstOwingSubscriber(ctx, spid, subs);
            }
        }
        else
        {
            delta = creditAmount / numSubs;
            ratio = 0.0;
            creditRecipient = getFirstPostpaidSubscriber(ctx, spid, subs);// DZ exclude prepaid
        }
        
        if ( creditRecipient == null )
        {
        	final String msg = "No postpaid subscriber in account "+account.getBAN()+" is eligible to receive credit of "+creditAmount;
        	new InfoLogMsg(this, msg, null).log(ctx);
        	throw new HomeException(msg);        	
        }
        
        creditRecipient.setContext(ctx);

        if (isRatioTooSmallToSplit(ctx, ratio)
                || isAmountSmallerThanLowestCurrencyUnit(ctx, creditAmount, numSubs))
        {
			submitTransaction(ctx, trans, creditAmount, creditRecipient,
			    success, failed);
        }
        else
        {
	        long creditRemaining = creditAmount;  // To ensure there is no surplus

            // Distribute the CREDIT among the subscribers (except the one assigned for getting the remaining credit)
	        Collection<Subscriber> otherSubs = CollectionSupportHelper.get(ctx).findAll(
	                ctx, subs, 
	                new Not(new EQ(SubscriberXInfo.ID, creditRecipient.getId())));
	        
	        for (Subscriber sub : otherSubs)
	        {
	            final long subCredit = getCreditAmountForSubscriber(spid, sub, ratio, delta, false);
	            if (subCredit != 0)
	            {
                    // Create a transaction for this subscriber only if the CREDIT amount is non-zero.
					submitTransaction(ctx, trans, subCredit, sub, success,
					    failed);
                    creditRemaining -= subCredit;
                }
	        }
	        
	        if (creditRemaining != 0)
	        {
                // Create a transaction for the subscriber assigned for getting any remaining CREDIT.
				submitTransaction(ctx, trans, creditRemaining, creditRecipient,
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

		return trans; // Return the original transaction
    }

    /**
     * Account Level (non-payment) Credit transactions are not supported for Pooled accounts 
     *
     * @param trans The credit.
     *
     * @return Transaction The original transaction.
     */
    @Override
    public Transaction handleGroupAccountTransaction(Context ctx,final Transaction trans)
        throws HomeException
	{
    	String msg = "Account Level (non-payment) Credit Transactions are not supported for Pooled Account types";
    	new MinorLogMsg(this, msg, null).log(ctx);
		
		IllegalPropertyArgumentException newException = 
			new IllegalPropertyArgumentException( "AccountCreditTransactionProcessor", msg);
		throw newException;
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
