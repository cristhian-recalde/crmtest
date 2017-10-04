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
package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.support.TransactionSupportForPaymentLogic;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.log.Logger;
/**
 * This Class was added when porting the new Account Level Payment Splitting Logic from 
 * CRM 7.3.
 * @since 7.3, ported to 8.2, Sept 21, 2009.
 * 
 * 
 * @author Larry Xia
 * @author Angie Li
 *
 */
public class SubscriberPaymentDistribution 
extends AbstractPaymentDistribution
{
    public SubscriberPaymentDistribution(final Context ctx, 
            final Subscriber sub)
    throws CalculationServiceException
    {
        this.sub = sub;
        setSubscriberOutstandingBalanceOwing(ctx, sub);
    }

    protected SubscriberPaymentDistribution(){}


    private void setSubscriberOutstandingBalanceOwing(Context ctx, Subscriber sub)
    throws CalculationServiceException
    {
        this.setOutStandingOwing(0);

        long owing = TPSSupport.getSubscriberOutstandingBalanceOwing(ctx, sub.getId());

        //Why do we only set the owing when it is > 0?
        if (owing > 0 )
        {
            //already over paid
            this.setOutStandingOwing(owing);     
        }

        if (Logger.isDebugEnabled())
        {
            Logger.debug(ctx, this, "The calculated Subscriber " + sub.getId() + " OBO (in cents) is " + owing);
        }
    }

  
    protected void createTransaction(final Context ctx, final Home transHome, final Transaction subTrans)
            throws HomeException, HomeInternalException
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.put(Subscriber.class, sub);
        subTransaction = (Transaction)transHome.create(subCtx, subTrans);
    }
  
    public boolean createTransactionForWriteOff(final Context ctx,
            final Transaction trans, final Home transHome, Currency currency)
    {
        return createTransaction(ctx,trans,transHome,currency);
    }

    protected boolean createTransaction(final Context ctx,
            final Transaction trans, final Home transHome, Currency currency)
    {
        boolean ret = false; 
        long payment = this.paymentForOutStandOwing + this.overPayment;

        if ( trans.getAmount() != 0 && payment == 0)
        {
            // skip 0 transaction, except it is a 0 account level payment 
            // which is used to synch up with ABM. 
            return true; 
        }


        try
        {
            // Don't screw up the original transaction
            final Transaction subTrans = TransactionSupportForPaymentLogic.cloneTransaction(trans);
            subTrans.setMSISDN(sub.getMSISDN());
            subTrans.setAmount(payment);
            subTrans.setTaxPaid(0L); 
            subTrans.setSubscriberID(sub.getId());
            subTrans.setBAN(sub.getBAN());
            /* When the Payment is made at the Account Level, we want the payment to apply to the 
             * subscribers selected and these subscribers may potentially have different Subscription Types.
             * We leave it up to the Subscriber selection for Payment logic to determine to which 
             * Subscribers the payment applies.  Here we try to model the Transaction to the 
             * Subscriber (subscription) chosen.  Set the Subscription Type appropriately.
             */
            subTrans.setSubscriptionTypeId(sub.getSubscriptionType());
            // remember the account transaction of which this is a portion
            subTrans.setAccountReceiptNum(trans.getReceiptNum());

            //Don't screw up the context
            final Context subCtx = ctx.createSubContext();
            subCtx.put(Subscriber.class, sub);
            Account subAccount = (Account) ctx.get(Account.class);
            try
            {
                subTrans.setResponsibleBAN(sub.getAccount(ctx).getResponsibleBAN());

            	if (subAccount == null || !subAccount.getBAN().equals(sub.getBAN()))
                {
                    subAccount = sub.getAccount(ctx);
                    subCtx.put(Account.class, subAccount);
                }
             }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this,
                        "Error putting correct account in context while creating subscriber payment for subscription '"
                                + this.sub.getId() + "': " + e.getMessage(), e);
            }

            subTransaction = (Transaction)transHome.create(subCtx, subTrans);
            ret = true; 
        }
        catch (Throwable e)
        {
            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "An unexpected error occurred when creating the Subscriber level payment for Subscriber " 
                        + sub.getId() + " due to " + e.getMessage(), e);
            }
            handleCreationFailure(ctx, payment, currency, e); 
        }
        return ret; 
    }



    protected void handleCreationFailure(Context ctx, long payment, Currency currency, Throwable e)
    {
        final String fileName = CoreTransactionSupportHelper.get(ctx).getTPSFileName(ctx);

        String amount = String.valueOf(payment);

        if (currency != null)
        {
            amount = currency.formatValue(payment);
        }

        final StringBuilder msg = new StringBuilder("Unable to create payment transaction with amount ");
        msg.append(amount);
        if (!fileName.equals(""))
        {
            msg.append(" and external file ");
            msg.append(fileName);
        }
        
        AbstractTransactionProcessor.writeSubscriberNote(ctx, sub, msg.toString());
        
        msg.append(" for subscriber '");
        msg.append(sub.getId());
        msg.append("': ");
        msg.append(e.getMessage());

        FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new HomeException(msg.toString()));

    }
    public Subscriber getSubscriber()
    {
        return sub;
    }
    /**
     * Returns the details of this SubscriberPaymentDistribution
     * @param subDistribution
     * @return
     */
    public String appendDistributionDetails() 
    {
        StringBuilder str = new StringBuilder();
        str.append(" Subscriber ");
        str.append(this.sub.getId());
        str.append(" has Outstanding Balance Owing="); 
        str.append(this.getOutStandingOwing());
        str.append(", Payment towards OBO Charges=");
        str.append(this.getPaymentForOutStandOwing());
        str.append(", Payment towards Overpayment=");
        str.append(this.getOverPayment());
        
        return str.toString();
    }
    public long getPostWriteOffPayment()
    {
        return postWriteOffPayment_;
    }
    
    private long postWriteOffPayment_ = 0L;
    protected Subscriber sub;
    protected Transaction subTransaction; 
}
