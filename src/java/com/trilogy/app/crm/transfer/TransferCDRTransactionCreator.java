/**
 * 
 */
package com.trilogy.app.crm.transfer;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.poller.event.TFABillingProcessor;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * @author ltang
 *
 */
public class TransferCDRTransactionCreator implements CDRTransactionCreator
{
    /**
     * Singleton instance.
     */
    protected static final TransferCDRTransactionCreator INSTANCE =
        new TransferCDRTransactionCreator();
    
    /**
     * Retrieves the singleton instance of this class.
     *
     * @return An instance of this class.
     */
    public static TransferCDRTransactionCreator getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * Creates a new <code>TransferCDRTransactionCreator</code>.
     */
    protected TransferCDRTransactionCreator()
    {
        // empty
    }
    
    /* (non-Javadoc)
     * @see com.redknee.app.crm.transfer.CDRTransactionCreator#createTransaction(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, long, com.redknee.app.crm.bean.AdjustmentType, java.util.Date)
     */
    public void createTransaction(Context context, Subscriber subscriber, long amount, long newBalance,
            AdjustmentType adjustmentType, String csrIdentifier, Date billingDate, String csrInput, 
            String extTransactionId, long transactionMethod) 
            throws HomeException
    {
        context = context.createSubContext();
        context.put(TFABillingProcessor.TRANSACTION_FROM_TFA_CDR_POLLER, true);
        
        TransactionSupport.createTransaction(context, subscriber, amount, newBalance, adjustmentType, false, false,
                csrIdentifier, billingDate, new Date(), csrInput, 0, extTransactionId, transactionMethod);
    }
}
