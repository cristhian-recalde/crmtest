/**
 * 
 */
package com.trilogy.app.crm.transfer;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;


/**
 * @author ltang
 *
 */
public interface CDRTransactionCreator
{
    /**
     * Creates a deposit release transaction.
     *
     * @param context The operating context
     * @param subscriber The subscriber
     * @param amount The amount
     * @param newBalance The final balance
     * @param adjustmentType The adjustment type for this transaction
     * @param csrIdentifier The user that performed the transaction
     * @param billingDate The effective billing date for this transaction
     * @param csrInput CSR Input
     * @param extTransactionId Unique transaction ID used to correlate all transactions
     * @param transactionMethod The transaction method used for the transaction
     * 
     * @throws HomeException Thrown if there are problems creating the transaction
     */
    void createTransaction(
        Context context,
        Subscriber subscriber,
        long amount,
        long newBalance,
        AdjustmentType adjustmentType,
        String csrIdentifier,
        Date billingDate,
        String csrInput,
        String extTransactionId,
        long transactionMethod) throws HomeException;
}
