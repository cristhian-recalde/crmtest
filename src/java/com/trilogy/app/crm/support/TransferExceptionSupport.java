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
package com.trilogy.app.crm.support;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeActionEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.transfer.ReprocessException;
import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;
import com.trilogy.app.crm.transfer.TransferExceptionXInfo;
import com.trilogy.app.crm.transfer.TransferFailureStateEnum;
import com.trilogy.app.crm.transfer.TransferFailureTypeEnum;


public class TransferExceptionSupport
{
    /**
     * A static method for creating a transaction record with the given transfer exception record properties
     * 
     * @param ctx
     * @param exception
     * @throws ReprocessException
     */
    public static void processTransferException(Context ctx, final TransferException exception)
    throws ReprocessException
    {
        try
        {
            // Format Transaction
            Transaction trans = adaptTransferExceptionToTransaction(ctx, exception);
            
            //Create Transaction
            Home home = (Home) ctx.get(TransactionHome.class);
            home.create(trans);
        }
        catch (HomeException e)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, 
                        "TransferExceptionSupport.processTransferException", 
                        "Failed to create the Transaction for Transfer Exception [ID=" + exception.getId() +"].");
            }
            
            throw new ReprocessException("Error encountered processing Transfer Exception [ID=" + exception.getId() +"]: " + e.getMessage(), 
                    e);
        }
    }
    
    /**
     * Retrieves the Transfer Exception matching the given ID.
     * @param ctx
     * @param id
     * @return
     * @throws HomeException
     */
    public static TransferException getTransferExceptionByID(Context ctx, Long id)
        throws HomeException
    {
        Home home = (Home) ctx.get(TransferExceptionHome.class);
        if (home == null)
        {
            throw new HomeException("The TransferExceptionHome is not installed in the Context.");
        }
        return (TransferException) home.find(ctx, id); 
    }
    
    public static void updateTransferExceptionState(Context ctx, TransferException record, TransferFailureStateEnum state)
    throws HomeException
    {
        Home transferExceptionHome = (Home) ctx.get(TransferExceptionHome.class);
        
        record.setState(state);
        try
        {
            transferExceptionHome.store(ctx,record);
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, 
                    "TransferExceptionSupport.updateTransferExceptionState", 
                    " Failed to update the state in the TransferException Record=" + record);
            throw e;
        }
    }
    
    public static void updateTransferExceptionCounters(Context ctx, long recordId, Throwable t)
    throws HomeException
    {
        Home transferExceptionHome = (Home) ctx.get(TransferExceptionHome.class);
        try
        {
            TransferException record = (TransferException) transferExceptionHome.find(ctx, new EQ(TransferExceptionXInfo.ID, Long.valueOf(recordId)));
            // Update counters.
            record.setAttempts(record.getAttempts() + 1);
            record.setLastAttemptDate(new Date());
            record.setLastAttemptAgent(findCurrentAgent(ctx));
            
            // Update failure notes
            if (t != null && t.getMessage() != null)
            {
                String note = "";
                if (t.getMessage().length() >= TransferException.LASTATTEMPTNOTE_WIDTH)
                {
                    note = t.getMessage().substring(0, TransferException.LASTATTEMPTNOTE_WIDTH);
                }
                else
                {
                    note = t.getMessage();
                }
                record.setLastAttemptNote(note);
            }
        
            transferExceptionHome.store(ctx,record);
            new OMLogMsg(Common.OM_MODULE, Common.OM_TRANSFER_EXCEPTION_UPDATED, 1).log(ctx);
        }
        catch (HomeException e)
        {
            LogSupport.debug(ctx, 
                    "TransferExceptionSupport.updateTransferExceptionCounters", 
                    " Failed to update the markers for Attempts in the TransferException Record " + recordId);
            throw e;
        }
    }
    
    /**
     * The subset of Transaction fields adapted from TransferException records
     * @param context
     * @param exception
     * @return
     * @throws ReprocessException
     */
    private static Transaction adaptTransferExceptionToTransaction(
            Context context, 
            TransferException exception)
        throws HomeException, ReprocessException
    {
        int spid = exception.getSpid();
        AdjustmentType adjustmentType = getAdjustmentTypeMapping(context, exception.getType());
        
        final Transaction trans;
        try
        {
            trans = (Transaction) XBeans.instantiate(com.redknee.app.crm.bean.Transaction.class, context);
        }
        catch (Exception e)
        {
            throw new HomeException("Cannot instantiate transaction bean", e);
        }

        trans.setSpid(spid);
        trans.setBAN(exception.getBan());
        trans.setSubscriberID(exception.getSubscriptionId());
        trans.setSubscriptionTypeId(exception.getSubscriptionType());    
        trans.setPayee(PayeeEnum.Subscriber);
        trans.setMSISDN(exception.getMsisdn());
        trans.setAmount(exception.getAmount());
        trans.setAgent(exception.getAgent());
        trans.setAdjustmentType(adjustmentType.getCode());
        trans.setAction(getAdjustmentTypeActionMapping(exception.getType()));
        trans.setGLCode(AdjustmentTypeSupportHelper.get(context).getGLCodeForAdjustmentType(context, adjustmentType.getCode(), spid));
        trans.setTransDate(exception.getTransDate());
        trans.setReceiveDate(new Date());
        trans.setExtTransactionId(exception.getExtTransactionId());
        trans.setLocationCode(exception.getLocationCode());
        trans.setPaymentDetails(exception.getPaymentDetails());
        trans.setTransactionMethod(exception.getTransactionMethod());
        trans.setCSRInput(exception.getRecipientMsisdn());
        
        return trans;
    }
    
    private static AdjustmentTypeActionEnum getAdjustmentTypeActionMapping(final TransferFailureTypeEnum type)
    {
        switch (type.getIndex())
        {
            case (TransferFailureTypeEnum.DEBIT_INDEX):
                return AdjustmentTypeActionEnum.DEBIT;
            case (TransferFailureTypeEnum.CREDIT_INDEX):
                return AdjustmentTypeActionEnum.CREDIT;
            default:
                return AdjustmentTypeActionEnum.EITHER;
        }
    }
    
    private static AdjustmentType getAdjustmentTypeMapping(final Context ctx, final TransferFailureTypeEnum type)
    throws HomeException
    {
        switch (type.getIndex())
        {
            case(TransferFailureTypeEnum.DEBIT_INDEX):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsContributorDebit_INDEX);                
            case(TransferFailureTypeEnum.CREDIT_INDEX):
                return AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.TransferFundsContributorCredit_INDEX);
            default:
                throw new IllegalStateException("Invalid TransferFailureType " + type);
        }
    }
    
    /**
     * Returns the Name of the Current user that is logged in.  
     * Returns a blank string if the user cannot be found.
     * @param context
     * @return
     */
    private static String findCurrentAgent(Context context)
    {
        String value = "";
        User principal = (User) context.get(Principal.class);
        if (principal != null)
        {
            value = principal.getId();
        }
        else
        {
            if (LogSupport.isDebugEnabled(context))
            {
                LogSupport.debug(context, "TransferExceptionSupport.findCurrentAgent", "Could not retrieve current User.");
            }
        }
        return value;
    }   

}
