/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.framework.xlog.log.SeverityLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.paymentprocessing.LateFeeEarlyRewardAccountProcessor;
import com.trilogy.app.crm.report.ReportUtilities;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.NoteSupport;
import com.trilogy.app.crm.support.NoteSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * This class handles Account Level PAYMENT transactions.
 * Account Level adjustments of this kind are divided equally amongst
 * all active subscribers and take into account Payment Plan Payment logic.
 * 
 * @author angie.li@redknee.com
 * @author larry.xia@rdknee.com
 */
public class AccountPaymentTransactionProcessor extends
AbstractTransactionProcessor
{
	public static final String TRANSACTION_TYPE = "payment";

	/**
	 * Creates a new TransactionRedirectionHome.
	 * 
	 * @param delegate
	 *            The home to delegate to.
	 * @param ctx
	 *            The operating context.
	 */
	public AccountPaymentTransactionProcessor(final Home delegate)
	{
		super(delegate);
		adjustmentTypes_ = new ArrayList();
		adjustmentTypes_.add(AdjustmentTypeEnum.StandardPayments);
	}

	/**
	 * @param ctx
	 *            the operating context
	 * @param trans
	 *            The account payment.
	 * @return Transaction The original account payment.
	 * @throws HomeException
	 *             the exception to throw if something went wrong
	 */
	@Override
	public Transaction handleRegularAccountTransaction(final Context ctx,
			final Transaction trans) throws HomeException
			{
		final Account acct = AccountSupport.getAccount(ctx, trans.getBAN());

		//		Added to fix TT#13060527027 & updated to fix TT#13111851023
		if(acct.isIndividual(ctx)){
			Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, trans.getSubscriberID());
			CRMSpid spid = SpidSupport.getCRMSpid(ctx, trans.getSpid());
			if(subscriber != null && spid != null)
			{
				if(!spid.isPaymentAcctLevelToInactive() && SubscriberStateEnum.INACTIVE.getName().equals(subscriber.getState().getName()))
					throw new HomeException("Account Payments towards Deactivated Subscriptions is disabled.");
			}
		}	


		final Transaction accountTrans =
				createAccountTransaction(ctx, trans, acct, null);
		// we need to pass the returned transaction because only the returned
		// transaction will have the ID set in case
		// of MS SQL
		AccountPaymentDistribution acctDistribution =
				new AccountPaymentDistribution(ctx, acct, accountTrans);

		Context sCtx = ctx.createSubContext();
		String sessionKey = CalculationServiceSupport.createNewSession(sCtx);
		// Proted From 7.4
        final LicenseMgr licenseManager = (LicenseMgr) ctx.get(LicenseMgr.class);
        
        Subscriber subscriber = SubscriberSupport.getSubscriber(ctx, trans.getSubscriberID());
        
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, trans.getSpid());
        
        
        
        if (spid.isEnableDebitForDeactivatedSubscriber())
        {
           //Check if any inactive subscribers in credit state, if so reset the balance to 0
           //and distribute the amount equally to active subs, TT: 8030600036 
           Collection realActiveSubs = acctDistribution.getNonDeactivatedPostpaidSubscribers();
           if (realActiveSubs!=null && realActiveSubs.size()>0)
           {
               List <Subscriber> inactiveSubs = getInactiveSubsWithCredit(acctDistribution.getPostpaidSubscribers());
               this.reconcileInactiveSubCredit(ctx, inactiveSubs, realActiveSubs,trans);

           }
        } 
		
		
		try
		{
			acctDistribution.Distribute(sCtx, this.getDelegate());
			/*
			 * [Cindy Wong] Add early reward transactions, if applicable.
			 */
			processEarlyReward(sCtx, accountTrans, trans, acct, sessionKey);
		}
		catch (CalculationServiceException e)
		{
			throw new HomeException(
					"Error performing payment distribution calculations: "
							+ e.getMessage(), e);
		}
		finally
		{
			CalculationServiceSupport.endSession(sCtx, sessionKey);
		}

		writePaymentReports(ctx, acctDistribution);

		/* For Unit Test Test */
		{
			setUnitTestSubAssignedWithPaymentRemainder(acctDistribution
					.getRemainingAssignee(ctx));
		}

		LogSupport.debug(ctx, this, "Returning the original transaction.");

		// Return the account transaction
		return accountTrans;
			}

	
    /**
     * This method will return a List of inactive subscriber which has credits
     *
     * @param cl, Collection of all the subscribers for a given account
     * @throws HomeException if somethig wrong happens
     * @return List of inactive subscribers which has credits, if not inactive sub has credits
     * then it returns null
     */        
    protected ArrayList <Subscriber> getInactiveSubsWithCredit(Collection<Subscriber> cl)
    {
 	   ArrayList <Subscriber>  inactiveSubWithCredit=null;
 	   if (cl!=null)
 	   {
 		   Iterator<Subscriber> it = cl.iterator();
 		   while (it.hasNext())
 			   {
 				   Subscriber sub =(Subscriber) it.next();
 				   if (sub.getState().equals(SubscriberStateEnum.INACTIVE) && sub.getAmountOwing()<0)
 				   {
 					   if (inactiveSubWithCredit==null) {
 						   inactiveSubWithCredit = new ArrayList <Subscriber>();
 					   }
 					   
 					   inactiveSubWithCredit.add(sub);
 				   }
 			   }

 	   }
 	   
 	   return inactiveSubWithCredit;
    }
	
    /**
     * This method which accumulate the credits from all inactive subscriber, and transfer the 
     * cedits to active subscriber
     *
     * @param ctx, the Context
     * @param inactiveSubs, List of inactive subscribers within an account
     * @param allActiveSubs, Collection of all the active subscribers within an account
     * @param trans, The original transaction.
     * @throws HomeException if somethig wrong happens
     */
    public void reconcileInactiveSubCredit(Context ctx, List <Subscriber> inactiveSubs, 
 		   Collection allActiveSubs, Transaction trans)    throws HomeException
    {
 	   if (inactiveSubs!=null && allActiveSubs!=null)
 	   {
 		   if (inactiveSubs.size()>0 && allActiveSubs.size()>0)
 		   {
 			   	//Accumulate extra credit from inactive sub, and debit the inactive sub of the extra credit
 		        long totalCredit = 0;
 		        for (int i=0; i<inactiveSubs.size(); i++)
 		        {
 		        	Subscriber sub = inactiveSubs.get(i);
 		        	long subCredit = sub.getAmountOwing(ctx);
 		        	//totalCredit holds all the sum of extra credits from inactive subs
 		        	totalCredit = totalCredit + subCredit;
 		        	//sub.setAmountOwing(0L);
 		       
 		        	createTransaction(ctx,trans,sub,Math.abs(subCredit),AdjustmentTypeEnum.DebitInactiveSub_INDEX);
 	 		   
 		        }
 		        
 		        //Divide the totalCredit equally among active subscribers
 		        totalCredit = Math.abs(totalCredit);
 		        long creditDelta = (long) totalCredit / allActiveSubs.size();
 		        
 		        Iterator it = allActiveSubs.iterator();
 		        int countActiveSubs = 0;

 		        	//Move the extra credits to Active subs evenly
 		        	while (it.hasNext())
 		        	{
 		        		countActiveSubs++;
 		        		Subscriber sub = (Subscriber) it.next();
 		        		if (countActiveSubs<allActiveSubs.size())
 		        		{
 		 		        	createTransaction(ctx,trans,sub,Math.abs(creditDelta),AdjustmentTypeEnum.CreditActiveSub_INDEX);
 		 		        	totalCredit = totalCredit - creditDelta;
 		        		}
 		        		else //for the Last active sup give the remaning amount, since not all the time creditDelta will be evenly divided
 		        		{
 		        			createTransaction(ctx,trans,sub,Math.abs(totalCredit),AdjustmentTypeEnum.CreditActiveSub_INDEX);
 		        		}
 		        	}

 		   }
 	   }
    }

    /**
     * This method creates Transaction for a particular subscriber which is passed in
     *
     * @param ctx, the Context
     * @param trans, The original transaction.
     * @param subscriber, The subscriber for which transaction will be made
     * @param payment, Transaction amount
     * @param adjustmentType, The adjustment type will be used for the transaction
     * @throws HomeException if somethig wrong happens
     */
    private void createTransaction(Context ctx, Transaction trans, Subscriber subscriber, 
    		long payment,short adjustmentType)
    throws HomeException
    {
 	   Transaction subTrans = cloneTransaction(trans);
 	   subTrans.setMSISDN(subscriber.getMSISDN());
 	  subTrans.setBAN(subscriber.getBAN());
 	   subTrans.setAmount(payment);
 	   subTrans.setSubscriberID(subscriber.getId());
 	   subTrans.setAdjustmentType(adjustmentType);
 	   subTrans.setTaxPaid(0);
 	   //Don't screw up the context
 	   final Context subCtx = ctx.createSubContext();
 	   subCtx.put(Subscriber.class, subscriber);

       super.create(subCtx, subTrans);
    }
    
	private void processEarlyReward(Context ctx, Transaction accountTrans, Transaction trans, Account acct, String sessionKey)
	{
		if (!AdjustmentTypeSupportHelper.get(getContext()).isInCategory(
				getContext(), accountTrans.getAdjustmentType(),
				AdjustmentTypeEnum.EarlyReward))
		{
			acct.setCurrentEarlyReward(AccountSupport.INVALID_VALUE);
			long eligibleReward = acct.getCurrentEarlyReward(ctx, sessionKey);
			long amount = trans.getAmount();
			if (eligibleReward != AccountSupport.INVALID_VALUE)
			{
				amount += eligibleReward;
			}
			LateFeeEarlyRewardAccountProcessor.getGenerateEarlyRewardInstance().processAccount(ctx, acct,
					trans.getTransDate(), amount);
		}
	}

	protected Transaction createAccountTransaction(final Context ctx,
			final Transaction trans, final Account account, final Long balance)
	{
		Transaction result = trans;
		try
		{
			final Home home =
					HomeSupportHelper.get(ctx).getHome(ctx,
							Common.ACCOUNT_TRANSACTION_HOME);
			trans.setResponsibleBAN(account.getResponsibleBAN());
			if (balance != null)
			{
				trans.setBalance(balance);
			}
			result = (Transaction) home.create(ctx, trans);
		}
		catch (HomeException e)
		{
			LogSupport.minor(ctx, this, "Unable to save account transaction.",
					e);
		}

		return trans;
	}

	/**
	 * Writes the new ER 1125 and an Account note reporting the payment at
	 * account level result
	 * 
	 * @param ctx
	 *            the operating context
	 * @param trans
	 *            the original transaction
	 * @param successAmounts
	 *            the array with successful amounts
	 * @param failedAmounts
	 *            the array with failed amounts
	 */
	private void writePaymentReports(final Context ctx,
			final AccountPaymentDistribution acctDistribution)
	{
		final String fileName =
				CoreTransactionSupportHelper.get(ctx).getTPSFileName(ctx);
		final Account acct = (Account) ctx.get(Account.class);

		addNote(ctx, acctDistribution.orignalTransaction, acct,
				acctDistribution.successAmount, acctDistribution.failedAmount,
				acctDistribution.successCount, acctDistribution.failedCount);

		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this,
					"Adding an ER 1125 to BAN " + acct.getBAN());
		}

		ERLogger.writePaymentAtAccountLevelER(ctx, fileName,
				acctDistribution.orignalTransaction,
				acctDistribution.successAmount, acctDistribution.failedAmount,
				acctDistribution.successCount, acctDistribution.failedCount, 0);

		if (LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, this, "ER 1125 to BAN " + acct.getBAN()
					+ " succesfuly created");
		}
	}

	/**
	 * Overloading this method to add a Payment Account note
	 * reporting the amount successfully added t the system and the
	 * amount that didn't go through the system
	 * 
	 * @param ctx
	 *            the operating context
	 * @param trans
	 *            the transactions to report
	 * @param acct
	 *            the account to report
	 * @param successAmounts
	 *            the amount that successfully went to the system
	 * @param failedAmounts
	 *            the amount that failed to get into the system
	 * @param succesPayments
	 *            the number of successful payments
	 * @param failedPayments
	 *            the number of failed payments
	 */
	private void addNote(final Context ctx, final Transaction trans,
			final Account acct, final long successAmounts,
			final long failedAmounts, final int succesPayments,
			final int failedPayments)
	{

		AdjustmentType adjustmentType = null;

		try
		{
			adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, trans.getAdjustmentType());
		}
		catch(Exception e)
		{
			LogSupport.minor(ctx, this, "error retrieving adjustment type", e);
		}

		final StringBuffer builder =
				new StringBuffer("Account payment summary: ");
		final Currency currency =
				ReportUtilities.getCurrency(ctx, acct.getCurrency());

		if( adjustmentType != null && 
				(AdjustmentTypeActionEnum.DEBIT.equals(adjustmentType.getAction())
						||
						AdjustmentTypeActionEnum.EITHER.equals(adjustmentType.getAction())
						)
				)
		{
			builder.append(currency.formatValue((Math.abs(trans.getAmount()))));
		}
		else
		{
			builder.append(currency.formatValue((trans.getAmount() * -1)));
		}
		builder.append(" original, ");
		builder.append(currency.formatValue(successAmounts));
		builder.append(" succesful, ");
		builder.append(currency.formatValue(failedAmounts));
		builder.append(" failed. ");
		builder.append(succesPayments);
		builder.append("/");
		builder.append(succesPayments + failedPayments);
		/**
		 * TT#13012426036 Adding a condition for AdjustmentType "Payment Reversal" to add 
		 * the below line in Account notes instead of generic statement
		 */

		if(CoreTransactionSupportHelper.get(ctx).isPayment(ctx, trans) && 
				( 	(adjustmentType != null && AdjustmentTypeActionEnum.DEBIT.equals(adjustmentType.getAction()))
						||
						(adjustmentType != null && AdjustmentTypeActionEnum.EITHER.equals(adjustmentType.getAction()) && trans.getAmount() > 0 )
						) 
				)
		{
			builder.append(" Subscriber's Payment/Adjustment:");
			builder.append(adjustmentType.getName());
			builder.append(" reversed successfully.");
		}
		else
		{
			builder.append(" Subscriber's Payment/Adjustment:");
			builder.append(adjustmentType.getName());
			builder.append(" paid successfully.");
		}

		try
		{
			LogSupport.debug(ctx, this, "Adding a note to BAN " + acct.getBAN()
					+ " Note : " + builder.toString());

			NoteSupportHelper.get(ctx).addAccountNote(ctx, acct.getBAN(), builder.toString(),
					SystemNoteTypeEnum.ADJUSTMENT, SystemNoteSubTypeEnum.ACCUPDATE);

			LogSupport.debug(ctx, this, "Note to BAN " + acct.getBAN()
					+ " successfuly added.");

		}
		catch (HomeException e)
		{
			LogSupport.major(ctx, this,
					"Unable to make note '" + builder.toString() + "' to account "
							+ acct.getBAN(), e);
		}
	}

	/**
	 * Handle the Group Account Payment case.
	 * 
	 * @param transaction
	 *            The account payment.
	 * @return Transaction The original account payment.
	 */
	@Override
	public Transaction handleGroupAccountTransaction(Context context,
			final Transaction trans) throws HomeException
			{
		Context ctx = context.createSubContext();
		final Account acct = AccountSupport.getAccount(ctx, trans.getBAN());
		CRMSpid spid = AccountSupport.getServiceProvider(ctx, acct);
		trans.setResponsibleBAN(acct.getResponsibleBAN());
		final Collection<Subscriber> all_subs =
				getActivePostpaidSubscribers(ctx, acct, spid);
		//
		// No subscriber transaction can be created if the account has no
		// subscriber.
		//
		if (all_subs == null || all_subs.size() <= 0)
		{
			String msg =
					"No subscriber found in account: " + trans.getAcctNum();
			new SeverityLogMsg(SeverityEnum.MAJOR, this.getClass().getName(),
					msg, null).log(ctx);
			throw new HomeException(msg);
		}

		// MAALP: Group Accounts RFF - set group leader's MSISDN as
		// transaction's MSISDN
		// The Leader MSISDN is virtual hence no charge is expected to exist on
		// it.
		// since the leader will not have any charge, payment must split.
		final Subscriber groupSub =
				SubscriberSupport.lookupSubscriberForMSISDN(ctx, AccountSupport
						.getAccount(ctx, trans.getBAN()).getPoolMSISDN(), trans
						.getSubscriptionTypeId(), new Date());

		final Transaction sub_trans = cloneTransaction(trans); // Don't screw up
		// the
		// original transaction
		sub_trans.setMSISDN(groupSub.getMSISDN());
		sub_trans.setBAN(groupSub.getBAN());

		sub_trans.setSubscriberID(groupSub.getId());

		ctx.put(Subscriber.class, groupSub);
		try
		{
			ctx.put(Account.class, groupSub.getAccount(ctx));
		}
		catch (Exception e)
		{
			LogSupport
			.minor(
					ctx,
					this,
					"Error putting correct account in context while creating subscriber payment for subscription '"
							+ groupSub.getId() + "': " + e.getMessage(), e);
		}

		final Transaction accountTrans =
				createAccountTransaction(ctx, trans, acct, null);

		sub_trans.setAccountReceiptNum(accountTrans.getReceiptNum());

		Transaction createdTransaction = (Transaction) super.create(ctx, sub_trans);

		// Returning the correct balance.
		accountTrans.setBalance(createdTransaction.getBalance());

		Context sCtx = ctx.createSubContext();
		/*
		 * [Cindy Wong] Add early reward transactions, if applicable.
		 */
		processEarlyReward(sCtx, accountTrans, trans, acct, null);

		return accountTrans; // Return the created transaction.
			}

	/**
	 * For Unit testing use only. Do not use this variable in business logic.
	 * Keep track of the subscriber that gets the payment remainder (when Even
	 * Payment Splitting is impossible)
	 * The name of the variable is purposely lengthy.
	 * Added the check for the unit test flag, so that it will be obvious
	 * anytime this
	 * method is used outside the unit test harness.
	 * 
	 * @return
	 */
	public void setUnitTestSubAssignedWithPaymentRemainder(Subscriber sub)
	{
		unitTestSubAssignedWithPaymentRemainder_ = sub;
	}

	/**
	 * For Unit testing use only. Do not use this variable in business logic.
	 * Keep track of the subscriber that gets the payment remainder (when Even
	 * Payment Splitting is impossible)
	 * The name of the variable is purposely lengthy.
	 * Added the check for the unit test flag, so that it will be obvious
	 * anytime this
	 * method is used outside the unit test harness.
	 * 
	 * @return
	 */
	public Subscriber getUnitTestSubAssignedWithPaymentRemainder(Context ctx)
	{
		if (com.redknee.app.crm.TestPackage.isRunningUnitTest(ctx))
		{
			return unitTestSubAssignedWithPaymentRemainder_;
		}
		else
		{
			throw new UnsupportedOperationException(
					"This method can only be used in unit test scenarios. See JavaDoc description.");
		}
	}

	/**
	 * For Unit testing use only. Do not use this variable in business logic.
	 * Keep track of the subscriber that gets the payment remainder (when Even
	 * Payment Splitting is impossible)
	 * The name of the variable is purposely lengthy.
	 */
	private Subscriber unitTestSubAssignedWithPaymentRemainder_ = null;

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
