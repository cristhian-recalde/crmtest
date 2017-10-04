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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.processor.subscription;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xdb.SUBXStatement;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberUsage;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveProcessor;
import com.trilogy.app.crm.move.processor.MoveProcessorProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;


/**
 * This processor is responsible for pegging any relevent ER's for subscription move
 * business logic.
 * 
 * It only performs validation required to perform its duty.  No business use case
 * validation is performed here.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
public class ERSubscriptionMoveProcessor<SMR extends SubscriptionMoveRequest> extends MoveProcessorProxy<SMR>
{
    public ERSubscriptionMoveProcessor(MoveProcessor<SMR> delegate)
    {
        super(delegate);
    }
    

    /**
     * @{inheritDoc}
     */
    @Override
    public Context setUp(Context ctx) throws MoveException
    {
        Context moveCtx = super.setUp(ctx);

        SMR request = this.getRequest();
        
        Subscriber subscription = request.getOriginalSubscription(ctx);
        Account account = request.getOldAccount(ctx);

        if (account != null && subscription != null)
        {
            moveCtx.put(MoveConstants.AMOUNT_OWING_CTX_KEY, Long.valueOf(calculateAmountOwing(ctx, subscription, account)));
        }
        
        sessionKey_ = CalculationServiceSupport.createNewSession(moveCtx);
        
        return moveCtx;
    }
    

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();
        
        SMR request = this.getRequest();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        Subscriber subscription = SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        if (subscription != null)
        {
            Account account = SubscriptionMoveValidationSupport.validateOldAccountExists(ctx, request, cise);
            
            Object amountOwingObj = ctx.get(MoveConstants.AMOUNT_OWING_CTX_KEY);
            if (amountOwingObj instanceof Long
                    && subscription != null
                    && account != null)
            {
                long amountOwing = ((Long) amountOwingObj).longValue();
                if (amountOwing != calculateAmountOwing(ctx, subscription, account))
                {
                    cise.thrown(new IllegalStateException("Previously calculated amount owing for subscription " + request.getOldSubscriptionId() + " is not correct.  Please try again."));
                }
            }
            else
            {
                cise.thrown(new IllegalStateException("Amount owing for subscription " + request.getOldSubscriptionId() + " not available."));
            }
        }
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void move(Context ctx) throws MoveException
    {
        boolean moveSuccess = false;
        try
        {
            super.move(ctx);
            moveSuccess = true;
        }
        finally
        {
            SMR request = this.getRequest();

            Subscriber originalSubscription = request.getOriginalSubscription(ctx);
            Subscriber newSubscription = request.getNewSubscription(ctx);

            if (!SafetyUtil.safeEquals(originalSubscription.getBAN(), request.getNewBAN()))
            {
                // Log Subscriber Change Account ER 768
                ERLogger.subscriberChangeAccountEr(
                        ctx, 
                        originalSubscription.getBAN(), 
                        request.getNewBAN(),
                        newSubscription, 
                        moveSuccess ? 0 : -1);
            }
            
            try
            {
                // Log "Subscriber Modification Event" ER 762
                logSubscriberMoveEventER(
                        ctx, 
                        request, 
                        ctx.getLong(MoveConstants.AMOUNT_OWING_CTX_KEY));
            }
            catch (HomeException e)
            {
                throw new MoveException(request, "Error occurred creating 'Subscriber Modification Event' ER 762.", e);
            }
        }
    }


    /**
     * Calculate the outstanding balance of the a subscriber.
     *
     * @param ctx The move context.
     * @param subscription The subscription to calculate the amount owing for.
     * @param account The subscription's account.
     * 
     * @return The amount owing.
     */
    private long calculateAmountOwing(final Context ctx, final Subscriber subscription, Account account)
    {
        long previousBalance = 0;

        /*
         * Calculate amounts only for the transfers from non-group accounts. There is no
         * point to calculate it for the transfers from group accounts since the "The
         * balance for a subscriber moving from a group pooled account should always be
         * considered 0, thus no credit/debit transactions are required. E-Care FS, in
         * section 4.5.1.7.
         */

        CalculationService service = (CalculationService) ctx.get(CalculationService.class);        
        SubscriberInvoice previousInvoice = null;
        try
        {
            previousInvoice = service.getMostRecentSubscriberInvoice(ctx, subscription.getId());
        }
        catch (CalculationServiceException e)
        {
            new MinorLogMsg(this, "Exception while fetching Subscriber Invoice for subscriber", e);
        }
        if (previousInvoice != null)
        {
            previousBalance = previousInvoice.getTotalAmount();
        }

        /*
         * [2007-01-18] Cindy Wong: It is intentional to include tax in the subscriber
         * move credit/debit amount, in anticipation of allowing subscriber move across
         * different bill cycle days. Upon invoice generation of the old subscriber, tax
         * will be calculated on the original charges, thus the invoice's balance would be
         * zeroed.
         */
        try
        {
            long paymentReceived = service.getSubscriberPaymentsReceived(ctx, subscription.getId(), 
                    CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            long subTotal = service.getSubscriberTotalBalance(ctx, subscription.getId(),
                    CalendarSupportHelper.get(ctx).getRunningDate(ctx));
            // Note: UPS uses the opposite sign for the balance, so we negate here.
            previousBalance += paymentReceived + subTotal;
            
            //TT#13040908014 Fixed.
            //When first invoice is not generated for Postpaid Accounts, in case of deposit charge and balance carry over,
            //Deposit amount should not be considered in amount owing. 
            if (isDepositChargeLicenseEnabled(ctx) 
            		&& SubscriberTypeEnum.POSTPAID.equals(account.getSystemType()) 
            		&& !account.getAccountCategory(ctx).getPrepaymentDepositRequired())
            {
            	if(previousInvoice == null)
            	{
            		long depositAmount = calculateDepositChargedForSubscriber(ctx, subscription,
							account);
            		previousBalance = previousBalance - depositAmount;
            		
            		if(LogSupport.isDebugEnabled(ctx))
            		{
            			LogSupport.debug(ctx, this, "Total Owing :" + previousBalance  + " After deducting the Deposit Charged :" + depositAmount);
            		}
            	}
            }
        }
        catch (CalculationServiceException e)
        {
            new MinorLogMsg(this, "Exception while fetching payment for subscriber", e);
        }
        
        return previousBalance;
    }


	private long calculateDepositChargedForSubscriber(final Context ctx,
			final Subscriber subscription, Account account) {
		Home txnHome =(Home) ctx.get(TransactionHome.class);

		And filter = new And();
		filter.add(new EQ(TransactionXInfo.BAN, account.getBAN()));
		filter.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, (int)(AdjustmentTypeEnum.DepositCharge_INDEX)));
		filter.add(new GTE(TransactionXInfo.RECEIVE_DATE, subscription.getDateCreated()));
		filter.add(new LTE(TransactionXInfo.RECEIVE_DATE, new Date()));

		long totalDeposit = 0;
		try 
		{
			Collection coll = txnHome.select(ctx,filter);
			for(Object txn : coll)
			{
				if(txn instanceof Transaction)
				{
					totalDeposit +=  ((Transaction) txn).getAmount();
				}
			}
		} 
		catch (HomeException e) 
		{
			LogSupport.major(ctx, this, "Home Exception encounterred while trying to fetch Deposite for the Subscriber :" + subscription.getId() , e);
		}
		
		return totalDeposit;
	}


    /**
     * Logs the "Subscriber Move Event" Event Record.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriber
     *            The old subscriber.
     * @param newSubscriber
     *            The new subscriber.
     * @param amount
     *            The amount owing.
     * @param result
     *            The result code from UPS/ABM.
     */
    private void logSubscriberMoveEventER(
            final Context ctx, 
            final SMR request, 
            final long amount) throws HomeException
    {
        final Subscriber originalSubscription = request.getOriginalSubscription(ctx);
        final Subscriber newSubscription = request.getNewSubscription(ctx);
        final Account oldAccount = request.getOldAccount(ctx);
        final Account newAccount = request.getNewAccount(ctx);

        // FCT no longer supported
        final SubscriberUsage usage = new SubscriberUsage();

        ERLogger.logModificationER(
                ctx, 
                newSubscription.getSpid(), 
                originalSubscription, 
                newSubscription,
                usage.getFreeCallTimeAvailable(), 
                usage.getFreeCallTimeAvailable(), 
                usage.getFreeCallTimeUsed(), 
                0,
                amount, 
                0, 
                originalSubscription.getState(), 
                newSubscription.getState(), 
                0, 
                originalSubscription.getDeposit(ctx),
                newSubscription.getDeposit(ctx),
                originalSubscription.getCreditLimit(ctx),
                newSubscription.getCreditLimit(ctx),
                0, 
                oldAccount.getCurrency(),
                newAccount.getCurrency(), 
                originalSubscription.getServices(ctx), 
                newSubscription.getServices(ctx), 
                0, 
                (originalSubscription.getSupportMSISDN(ctx)),
                (newSubscription.getSupportMSISDN(ctx)),
                0);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown(Context ctx) throws MoveException
    {
        try
        {
            super.tearDown(ctx);
        }
        finally
        {
            CalculationServiceSupport.endSession(ctx, sessionKey_);
        }
    }
    
    private boolean isDepositChargeLicenseEnabled(final Context ctx)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.DEPOSIT_CHARGE_ADJUSTMENT);
    }

    protected String sessionKey_ = null;
}
