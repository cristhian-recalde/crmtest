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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.TPSProcessor;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.dunning.DunningProcess;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;
import com.trilogy.app.crm.transaction.AccountChargeTransactionProcessor;
import com.trilogy.app.crm.transaction.AccountCreditTransactionProcessor;
import com.trilogy.app.crm.transaction.AccountPaymentTransactionProcessor;
import com.trilogy.app.crm.transaction.PaymentDistributionException;
import com.trilogy.app.crm.transaction.TransactionProcessor;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * This class either breaks down Account Payments into individual Subscriber Payments or
 * forwards Payments (made to Group-Pooled Accounts) to the Group Subscriber.
 *
 * @author angie.li@redknee.com
 */
public class TransactionRedirectionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new TransactionRedirectionHome. Adds the Account Level Transaction
     * Handlers/Processors.
     *
     * @param delegate
     *            The home to delegate to.
     */
    public TransactionRedirectionHome(final Home delegate)
    {
        super(delegate);
        this.transactionProcessors_ = new ArrayList<TransactionProcessor>();
        this.add(new AccountPaymentTransactionProcessor(delegate));
        this.add(new AccountCreditTransactionProcessor(delegate));
        this.add(new AccountChargeTransactionProcessor(delegate));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Context subContext = ctx.createSubContext();

        if (obj instanceof Transaction)
        {
            final Transaction trans = (Transaction) obj;

            // Needed by ERLogger.genAccountAdjustmentER
            final Account account = getTransactionAccount(subContext, trans);
            if (account == null)
            {
                throw new HomeException("System Error: cannot find the account associated with this transaction");
            }

            subContext.put(Account.class, account);

            Object result = null;
            try
            {
                trans.setSpid(account.getSpid());
                result = createTransaction(subContext, obj);
                
                if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, (Transaction)result))
                {
                	this.processDunning(ctx, (Transaction)result);
                }	
            }
            catch (final HomeException e)
            {
                // For TPS, ER (for error) will get generated somewhere else.
                if (!subContext.has(TPSProcessor.class))
                {
                    int resultCode = TPSPipeConstant.FAIL_TO_FILL_IN_TRASACTION_RECORD;
                    if(e.getCause() instanceof PaymentDistributionException)
                    {
                    	resultCode = ((PaymentDistributionException)e.getCause()).getReason();
                    }
                	ERLogger.genAccountAdjustmentER(subContext, trans,
                        TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
                        resultCode);
                }

                throw e;
            }

            ERLogger.genAccountAdjustmentER(subContext, trans, TPSPipeConstant.RESULT_CODE_SUCCESS,
                TPSPipeConstant.RESULT_CODE_SUCCESS);

            return result;
        }
        return super.create(subContext, obj);
    }


    /**
     * Adds a transaction processor.
     *
     * @param convertion
     *            Transaction processor to add.
     * @return Returns this home.
     */
    public TransactionRedirectionHome add(final TransactionProcessor convertion)
    {
        this.transactionProcessors_.add(convertion);
        return this;
    }


    /**
     * Looks up everywhere for the transaction account.
     *
     * @param ctx
     *            The operating context.
     * @param trans
     *            Transaction to retrieve account for.
     * @return The account of this transaction.
     * @throws HomeException
     *             Thrown if there are problems looking up the correct account.
     */
    private Account getTransactionAccount(final Context ctx, final Transaction trans) throws HomeException
    {
        Account acct = null;
        Subscriber subs = null;

        acct = (Account) ctx.get(Account.class);

        if (acct == null || !SafetyUtil.safeEquals(acct.getBAN(), trans.getBAN()))
        {
            /*
             * [2007-01-17] Cindy Wong: change the way the transaction's account is looked
             * up. The account must match the BAN specified.
             */
            acct = AccountSupport.getAccount(ctx, trans.getBAN());
        }

        /*
         * If account cannot be found, attempt to look up based on the subscriber.
         */
        if (acct == null)
        {
            subs = (Subscriber) ctx.get(Subscriber.class);
            // Try based on subscriber
            if (subs == null || !subs.getMSISDN().equals(trans.getMSISDN()))
            {
                subs = SubscriberSupport.getSubscriber(ctx, trans.getSubscriberID());
                if (subs != null && SafetyUtil.safeEquals(subs.getBAN(), trans.getBAN()))
                {
                    acct = subs.getAccount(ctx);
                }
            }
        }

        return acct;
    }


    /**
     * This method contains the meat for creating transaction.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            The transaction object.
     * @return Object The resulting object.
     * @throws HomeException
     *             Thrown if the transaction cannot be created.
     */
    private Object createTransaction(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof Transaction)
        {
            final Transaction trans = (Transaction) obj;
            // **** ACCOUNT-LEVEL PAYMENTS ****
            if (PayeeEnum.Account.equals(trans.getPayee()) || trans.getMSISDN().equals("0"))
            {
                /*
                 * Allow all standard payments and any Account level Payment Plan
                 * transaction
                 */
                if (!CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, trans)
                    && !CoreTransactionSupportHelper.get(ctx).isPaymentPlanLoanCategory(ctx, trans)
                    && !CoreTransactionSupportHelper.get(ctx).isLateFeeCategory(ctx, trans)
                    && !CoreTransactionSupportHelper.get(ctx).isEarlyPaymentCategory(ctx, trans))
                {
                    throw new HomeException(
                        "For Account Payments, the Adjustment Type must be standard payment or under Payment plan category. ");
                }

                Transaction result = null;
                final Iterator<TransactionProcessor> iter = this.transactionProcessors_.iterator();

                Context sCtx = ctx.createSubContext();
                
                // AccountPaymentTransactionProcessor uses calculation service.  Since we are potentially executing more than
                // one processor, install a session.
                String sessionKey = CalculationServiceSupport.createNewSession(sCtx);
                try
                {
                    for (TransactionProcessor transactionProcessor : transactionProcessors_)
                    {
                        result = transactionProcessor.createTransaction(sCtx, trans);
                        if (result != null)
                        {
                            break;
                        }
                    }
                    return result;
                }
                finally
                {
                    CalculationServiceSupport.endSession(sCtx, sessionKey);
                }
            }
            final Account acct = (Account) ctx.get(Account.class);

            // **** PAYMENTS TO INDIVIDUAL IN GROUP POOLED ACCOUNTS ****
            if (acct != null && acct.isPooled(ctx))
            {
                if (CoreTransactionSupportHelper.get(ctx).isStandardPayment(ctx, trans))
                {
                    return handleGroupSubscriberPayment(ctx, trans);
                }
            }
        }

        return super.create(ctx, obj);
    }


    /**
     * Handle the Payment to individual in Group Pooled Account case.
     *
     * @param ctx
     *            The operating context
     * @param trans
     *            The payment.
     * @return Transaction The original payment.
     * @throws HomeException
     *             Thrown by delegate
     */

    private Transaction handleGroupSubscriberPayment(final Context ctx, final Transaction trans) throws HomeException
    {
        // TODO: check if the method can be eliminated completely
        return (Transaction) super.create(ctx, trans);
    }
    
    
    
    public void processDunning(Context ctx, Transaction trans) 
    throws HomeException
    {
    	if (!((Boolean)ctx.get(Common.DUNNING_EXEMPTION, Boolean.FALSE)).booleanValue())
        {
     		String ban = trans.getResponsibleBAN();
            final Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, ban);
            if (account.getState() != AccountStateEnum.IN_COLLECTION)
            {
            	// TT#13031133022 - put flag in context to indicate that the dunning process is initiated by payment
            	if(ctx != null)
            	{
            		Context subCtx = ctx.createSubContext();
            		subCtx.put(IS_DEBT_CLEARED_BY_TRANSACTION, Boolean.FALSE);
            	
            		try
            		{
            			DunningProcess dunningProcess = (DunningProcess)getContext().get(DunningProcess.class);
            			dunningProcess.processAccount(
            			subCtx,
                        new Date(),
                        ban);
            		}
            		catch (DunningProcessException e )
            		{
            			new MinorLogMsg(this, "Dunning process fails " + ban, e).log(getContext());
            		}
            	}
            }
        }
      }

    /**
     * Transaction processors to use.
     */
    private final List<TransactionProcessor> transactionProcessors_;
	public static final String IS_DEBT_CLEARED_BY_TRANSACTION = "IS_DEBT_CLEARED_BY_TRANSACTION";
}
