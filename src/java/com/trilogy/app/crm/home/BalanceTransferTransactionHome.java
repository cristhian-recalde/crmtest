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

import com.trilogy.app.crm.bas.recharge.RecurRechargeRequest;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SystemNoteSubTypeEnum;
import com.trilogy.app.crm.bean.SystemNoteTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.service.CalculationServiceInternalException;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.BalanceTransferSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This class splits one balance transfer transaction to two transaction: One for prepaid
 * subscriber with adjustment type - BalanceTransfer-Prepaid, the other for postpaid
 * subscriber with adjustment type - BalanceTransfer-Postpaid.
 *
 * @author daniel.zhang@redknee.com
 */
public class BalanceTransferTransactionHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>BalanceTransferTransactionHome</code>.
     *
     * @param delegate
     *            Home delegate.
     */
    public BalanceTransferTransactionHome(final Home delegate)
	{
        super(delegate);
	}
	

   /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (!(obj instanceof Transaction))
        {
            return super.create(ctx, obj);
        }
        
        Transaction transaction = (Transaction) obj;
        
        if (!CoreTransactionSupportHelper.get(ctx).isBalanceTransfer(ctx, transaction))
        {
        	// Not a balance transfer. Skip this home.
        	return super.create(ctx,obj);
        }
        else
        {
            return performBalanceTransfer(ctx, transaction);
        }
    }
    
    private Transaction performBalanceTransfer(final Context ctx, Transaction transaction) throws HomeException
    {
        
         Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        if (subscriber==null || subscriber.getId()==null || !subscriber.getId().equals(transaction.getSubscriberID()))
        {
            subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx,transaction.getMSISDN(), transaction.getTransDate());
        }
        
        Account account = (Account) ctx.get(Account.class);
        if (account == null || account.getBAN() == null)
        {
            account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class,subscriber);
        }
    	 subscriber.setContext(ctx);
    	 
    	 CRMSpid spid = SpidSupport.getCRMSpid(ctx, account.getSpid());
    	
        final Subscriber supporterSubscriber = BalanceTransferSupport.validateSubscribers(ctx, subscriber);
        
        Subscriber prepaidSub=SubscriberSupport.getSubscriber(ctx, transaction.getSubscriberID());
        
        /*
         * If true, Then check whether balance transfer amount is gretter than Credir limit - amountOwedBySubscriber. This case happen when PPSM postpaid subscriber wants to transfer balance to prepaid subscriber when he is Owing more amount than credit limit.
         * TT: 9022300052
         */
        if(spid.isChkBalanceTransferOnCreditLimit())
        {
			String creditChkStr = BalanceTransferSupport.validateAmount(prepaidSub,transaction,ctx);
			
			if (LogSupport.isDebugEnabled(getContext()))
            {
                new DebugLogMsg(this, "credit limit check string"+creditChkStr, null).log(getContext());
            }
        }	
        	
        supporterSubscriber.setContext(ctx);

        BalanceTransferSupport.createDebitTransaction(ctx, transaction, subscriber,
                supporterSubscriber, false);

        boolean success = false;
        try
        {       
            transaction.setTaxPaid(0);
            transaction.setSubscriptionCharge(false);  
            transaction = (Transaction) super.create(ctx,transaction);
            ERLogger.genAccountAdjustmentER(ctx,transaction, 0, 0);       

            success=true;
        }
        catch (final OcgTransactionException ocge)
        {
            ERLogger.genAccountAdjustmentER(ctx, transaction, ocge.getErrorCode(), RecurRechargeRequest.FAIL_OTHERS);
            throw ocge;
        }
        catch (final HomeException he)
        {
            throw he;
        }
        finally
        {
            if(!success)
            {
                Currency currency = (Currency) ctx.get(Currency.class, Currency.DEFAULT);
                
                BalanceTransferSupport.addSubscriberNote(ctx, subscriber, transaction.getAdjustmentType(),
                    "Unable to credit subscriber for the amount " + currency.formatValue(transaction.getAmount()) + ", but PPSM supporter was already debited.", SystemNoteTypeEnum.ADJUSTMENT,
                    SystemNoteSubTypeEnum.BALANCETRANSFER_FAIL_PPD);
            }
        }
        return transaction;    
     }
}
