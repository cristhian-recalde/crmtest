package com.trilogy.app.crm.transaction;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.TransactionSupportForPaymentLogic;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * The Class that performs the transaction creation based on the Payment distribution per Subscriber.
 * @author angie.li@redknee.com
 *
 */
public class SubscriberPaymentPlanLoanPaymentDistribution 
extends AbstractPaymentDistribution
{

    public SubscriberPaymentPlanLoanPaymentDistribution(final Context ctx, String subscriberId,
            final long outStandingOwing, final long loanBucketRemaining)
    {
        Subscriber subscriber = null;
        try
        {
            subscriber = HomeSupportHelper.get(ctx).findBean(ctx, Subscriber.class, subscriberId);
        }        
        catch (HomeException e)
        {
            LogSupport.major(ctx, this, "Failed to fetch subscriber : " + subscriberId,e);
        }
        this.sub = subscriber;
        this.outStandingOwing = outStandingOwing;
        this.loanBucketRemaining = loanBucketRemaining;
    }
    

    /**
     * Handle creation of Payment Plan adjustments (payments and over payments) for the given subscription
     * @param ctx
     * @param origTrans
     * @param transHome
     * @param currency
     */
    protected boolean createTransactions(final Context ctx,
            final Transaction origTrans, final Home transHome, Currency currency)
    {
        //Create payment plan payment transaction: includes the payment plan payment to charges and the over payment amount.
        long receiptNum = createPaymentPlanPaymentTrans(ctx, origTrans, transHome, currency);
        //Create a balanced payment plan over payment transaction
        createPaymentPlanOverPaymentBalancingAdjustment(ctx, origTrans, currency);
        
        /* TODO: determine a proper way to determine how to report the result of creating
         * Payment Plan transactions.  Since we create more than one transaction, maybe there should 
         * be multiple result codes?
         */
        return receiptNum != DEFAULT_RECEIPT_NUM; 
    }

    /**
     * Post a Payment Plan _Payment_ adjustment for the correct amount.
     * @param ctx
     * @param origTrans
     * @param transHome
     * @param currency
     * @return DEFAULT_RECEIPT_NUM if no transaction was created, the transaction receipt number if it
     * was created successfully, and 0 if the transaction was skipped for creation.
     */
    private long createPaymentPlanPaymentTrans(Context ctx,
            Transaction origTrans, Home transHome, Currency currency) 
    {
        long receiptNum = DEFAULT_RECEIPT_NUM;

        long paymentAmount = getPaymentForOutStandOwing() + getOverPayment();

        if ( origTrans.getAmount() != 0 && paymentAmount == 0)
        {
            // skip 0 transaction, except it is a 0 account level payment 
            // which is used to synch up with ABM. 
            return 0L; 
        }
        
        try
        {
            // Don't screw up the original transaction
            final Transaction subTrans = TransactionSupportForPaymentLogic.cloneTransaction(origTrans);
            subTrans.setMSISDN(sub.getMSISDN());
            subTrans.setAmount(paymentAmount);
            subTrans.setBAN(sub.getBAN());
            //subTrans.setTaxPaid(tax); 
            subTrans.setSubscriberID(sub.getId());
            /* When the Payment is made at the Account Level, we want the payment to apply to the 
             * subscribers selected and these subscribers may potentially have different Subscription Types.
             * We leave it up to the Subscriber selection for Payment logic to determine to which 
             * Subscribers the payment applies.  Here we try to model the Transaction to the 
             * Subscriber (subscription) chosen.  Set the Subscription Type appropriately.
             */
            subTrans.setSubscriptionTypeId(sub.getSubscriptionType());
            // remember the account transaction of which this is a portion
            subTrans.setAccountReceiptNum(origTrans.getReceiptNum());

            final int paymentAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                    AdjustmentTypeEnum.PaymentPlanLoanPayment).getCode();

            subTrans.setAdjustmentType(paymentAdjustmentType);

            Context sCtx = ctx.createSubContext();
            sCtx.put(Subscriber.class, this.sub);
            Account subAccount = (Account) sCtx.get(Account.class);
            try
            {
                if (subAccount == null || !subAccount.getBAN().equals(this.sub.getBAN()))
                {
                    subAccount = this.sub.getAccount(ctx);
                    sCtx.put(Account.class, subAccount);
                }
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this,
                        "Error putting correct account in context while creating payment plan loan payment for subscription '"
                                + this.sub.getId() + "': " + e.getMessage(), e);
            }

            subTrans.setResponsibleBAN(subAccount.getResponsibleBAN());

            final Transaction result = (Transaction) transHome.create(sCtx, subTrans);
            receiptNum = result.getReceiptNum();

            if (Logger.isDebugEnabled())
            {
                Logger.debug(sCtx, this, "Create Payment Plan Payment Trans Result: " + result);
            }
        }
        catch (Throwable e)
        {
            /*
             * It is very important to keep going on this part to keep track of the remaining
             * payments the is why we need to continue here and send a Subscriber note if it
             * fails we already rely on the ER 1124 to keep track of the failed ERs
             */
            handleCreationFailure(ctx, "Payment", paymentAmount, currency); 
        }
        return receiptNum;
    }
    
    
    /**
     * Create an subscription-level Payment Plan Loan Allocation Transaction, meant to balance
     * out just the Over Payment amount.
     * @param ctx
     * @param origTrans
     * @param transHome
     * @param currency
     */
    private void createPaymentPlanOverPaymentBalancingAdjustment(final Context context,
            final Transaction origTrans, final Currency currency) 
    {
        Context ctx = context.createSubContext();
        long amount = getOverPayment();
        
        if ( origTrans.getAmount() != 0 && amount == 0)
        {
            // skip 0 transaction, except it is a 0 account level payment 
            // which is used to synch up with ABM. 
            return; 
        }
        
        try
        {
            // Don't screw up the original transaction
            final Transaction subTrans =  TransactionSupportForPaymentLogic.cloneTransaction(origTrans);
            subTrans.setMSISDN(sub.getMSISDN());
            subTrans.setSubscriberID(sub.getId());
            subTrans.setBAN(sub.getBAN());
            subTrans.setAmount(amount);
            /* When the Payment is made at the Account Level, we want the payment to apply to the 
             * subscribers selected and these subscribers may potentially have different Subscription Types.
             * We leave it up to the Subscriber selection for Payment logic to determine to which 
             * Subscribers the payment applies.  Here we try to model the Transaction to the 
             * Subscriber (subscription) chosen.  Set the Subscription Type appropriately.
             */
            subTrans.setSubscriptionTypeId(sub.getSubscriptionType());
            // remember the account transaction of which this is a portion
            subTrans.setAccountReceiptNum(origTrans.getReceiptNum());

            final int allocationAdjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
                AdjustmentTypeEnum.PaymentPlanLoanAllocation).getCode();

            subTrans.setAdjustmentType(allocationAdjustmentType);
            // Complete Transaction Pipeline
            final Home transHome = (Home) ctx.get(Common.FULL_TRANSACTION_HOME);
            ctx.put(Subscriber.class, this.sub);
            Account subAccount = (Account) ctx.get(Account.class);
            try
            {
                if (subAccount == null || !subAccount.getBAN().equals(this.sub.getBAN()))
                {
                    subAccount = this.sub.getAccount(ctx);
                    ctx.put(Account.class, subAccount);
                }
            }
            catch (Exception e)
            {
                LogSupport.minor(ctx, this,
                        "Error putting correct account in context while creating payment plan loan payment for subscription '"
                                + this.sub.getId() + "': " + e.getMessage(), e);
            }

            subTrans.setResponsibleBAN(subAccount.getResponsibleBAN());

            final Transaction result = (Transaction) transHome.create(ctx, subTrans);

            if (Logger.isDebugEnabled())
            {
                Logger.debug(ctx, this, "Create Payment Plan Allocation Trans Result: " + result);
            }

        }
        catch (Throwable e)
        {
            /*
             * It is very important to keep going on this part to keep track of the remaining
             * payments the is why we need to continue here and log a Major error we already
             * rely on the ER 1124 to keep track of the failed ERs
             */
            handleCreationFailure(ctx, "Allocation", amount, currency); 
        }
    }
    
    private void handleCreationFailure(Context ctx, String type, long payment, Currency currency)
    {
        final String fileName = CoreTransactionSupportHelper.get(ctx).getTPSFileName(ctx);

        String amount = String.valueOf(payment);

        if (currency != null)
        {
            amount = currency.formatValue(payment);
        }

        final StringBuffer msg = new StringBuffer("Failed to create Payment Plan " + type + " transaction with amount ");
        msg.append(amount);
        if (!fileName.equals(""))
        {
            msg.append(" and external file ");
            msg.append(fileName);
        }

        AbstractTransactionProcessor.writeSubscriberNote(ctx, sub, msg.toString());
    }
    
    /**
     * Returns the details of this SubscriberPaymentPlanLoanPaymentDistribution
     * @param subDistribution
     * @return
     */
    public String appendDistributionDetails() 
    {
        StringBuilder str = new StringBuilder();
        str.append(" Subscription's Payment Plan Outstanding Owing="); 
        str.append(this.getOutStandingOwing());
        str.append(", Payment towards Payment Plan Charges=");
        str.append(this.getPaymentForOutStandOwing());
        str.append(", Payment towards Payment Plan Overpayment=");
        str.append(this.getOverPayment());
        return str.toString();
    }

    protected Subscriber sub;

    
    /**
     * Amount of Payment Plan OBO (invoiced charges)
    protected long outStandingOwing;
     */ 
    
    /**
     * Amount still in the Payment Plan Loan yet to be charged (in installments)
     */
    protected long loanBucketRemaining;

    /**
     * Portion of the original Payment meant for this subscriber's Payment Plan OBO
    protected long paymentForOutStandOwing;
    */
    
    /**
     * Portion of the original Payment meant for this subscriber's Payment Plan OverPayment
    protected long overPayment;
    */ 
    
    private long DEFAULT_RECEIPT_NUM = -1L;
}
