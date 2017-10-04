package com.trilogy.app.crm.subscriber.charge.support;

import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

/**
 * A simple Txn holder.
 * @author sbanerjee
 */
public class TransactionResultHolder
{
    private Transaction transaction;
    private int transactionResultCode = BundleChargingSupport.TRANSACTION_SUCCESS;
    private long transactionAmount;
    
    /**
     * If the transaction wasn't a success; there could be an exception behind it.
     */
    private Exception exception;
    
    /**
     * @return the exception
     */
    public final Exception getException()
    {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public final void setException(Exception exception)
    {
        this.exception = exception;
    }

    /**
     * @return the transaction
     */
    public Transaction getTransaction()
    {
        return transaction;
    }

    /**
     * @return the transactionResultCode
     */
    public int getTransactionResultCode()
    {
        return transactionResultCode;
    }

    /**
     * @return the transactionAmount
     */
    public long getTransactionAmount()
    {
        return transactionAmount;
    }

    /**
     * @param trans
     * @param transactionResultCode
     * @param amount
     * @param exception TODO
     */
    public TransactionResultHolder(Transaction trans,
            int transactionResultCode, long amount, Exception exception)
    {
        this.transaction = trans;
        this.transactionResultCode = transactionResultCode;
        this.transactionAmount = amount;
        this.exception = exception;
    }
    
    public boolean isSuccess()
    {
        return this.transactionResultCode == ChargingConstants.TRANSACTION_SUCCESS;
    }
    
    /*
     * Helper methods
     */
    public boolean isFailedDueToOCG()
    {
        return this.exception instanceof OcgTransactionException;
    }
    
    public int getOCGResultCode()
    {
        if(isFailedDueToOCG())
            return ((OcgTransactionException)this.exception).getErrorCode();
        
        return 0;
    }
    
    public String getOCGErrorMessage()
    {
        if(isFailedDueToOCG())
            return ((OcgTransactionException)this.exception).getMessage();
        
        return "OCG Success";
    }
}