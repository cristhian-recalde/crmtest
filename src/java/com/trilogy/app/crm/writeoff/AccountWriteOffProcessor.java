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
package com.trilogy.app.crm.writeoff;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bas.tps.TPSSupport;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.SystemTransactionMethodsConstants;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.CalculationServiceSupport;
//import com.trilogy.app.crm.bean.TransactionMethodTypeEnum;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.transaction.AccountPaymentDistribution;
import com.trilogy.app.crm.transaction.SubscriberPaymentDistribution;
import com.trilogy.app.crm.writeoff.WriteOffErrorReport.ResultCode;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 *
 * @author ray.chen@redknee.com
 */
@SuppressWarnings("unchecked")
public class AccountWriteOffProcessor extends AccountPaymentDistribution
{
    public static class Result
    {
        public long WriteOffAmount = 0;
        public long TaxPaid = 0;
        public boolean IsSuccess = false;
    }

    private final WriteOffInput writeOff_;
    private final PrintWriter logOutput_;

    public AccountWriteOffProcessor(Context ctx, Account acct, WriteOffInput writeOff, 
            PrintWriter logOutput)
    {
        super(ctx, acct);
        writeOff_ = writeOff;
        logOutput_ = logOutput;
    }
    
    public Result process(Context ctx) throws HomeException 
    {
        
        Context subCtx = ctx.createSubContext();

        String sessionKey = CalculationServiceSupport.createNewSession(subCtx);
        
        Result ret = new Result();
        try
        {
            OBOPayableSubscribers = TPSSupport.getPayableSubscribers(subCtx, account); 
            
            calculateOBO(subCtx); 
            long totalOwing = getOutStandingOwing();
            if ( totalOwing !=0)
            {   
                Transaction trans = createTransaction(subCtx); 
                setOrignalTransaction(trans); 
                createDistributions(subCtx);
                String msg = setWriteOffFlag(subCtx, account, true);
                if(msg != null)
                {
                    logError(subCtx, ResultCode.ACCOUNT_WRITE_OFF_FAILURE,msg);
                }
                ret.IsSuccess = true;
                ret.WriteOffAmount = successAmount;
                ret.TaxPaid = -getOutStandingTaxOwing();
            } 
            else 
            {
                ret.IsSuccess = false;
                String msg ="Account balance is 0; nothing to write off"; 
                logError(subCtx, WriteOffErrorReport.ResultCode.ACCOUNT_WRITE_OFF_FAILURE, msg);
            }
        }
        catch (Exception e)
        {
            logError(subCtx, WriteOffErrorReport.ResultCode.ACCOUNT_WRITE_OFF_FAILURE, e.getMessage());
        }
        finally
        {
            CalculationServiceSupport.endSession(subCtx, sessionKey);
        }
        
        return ret;
    }

    public static String setWriteOffFlag(Context ctx, Account account, boolean flag)
    {
        String msg = null;
        Collection accounts = null;
        try
        {
            accounts = AccountSupport.getNonResponsibleAccounts(ctx, account);
        }
        catch (HomeException e)
        {
            msg = "Failed to fetch non-responsible subaccounts";
            return msg;
        }
        
        Home accHome = (Home) ctx.get(AccountHome.class);
        
        for (Iterator it = accounts.iterator(); it.hasNext();)
        {
            Account acc = (Account) it.next();
            if(flag != acc.getWrittenOff())
            {
                acc.setWrittenOff(flag);
                try
                {
                    accHome.store(acc);
                }
                catch (Exception e)
                {
                    msg = "Failed to set write off flag";
                    if(LogSupport.isDebugEnabled(ctx)){
                        LogSupport.minor(ctx, AccountWriteOffProcessor.class.getSimpleName(), e);
                    }
                }
            }
        }
        return msg;
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.transaction.AccountPaymentDistribution#calculateOBO(com.redknee.framework.xhome.context.Context)
     * Override to meet write-off requirements: Total owing amount instead of last invoice balance.
     */
    
    @Override
    protected void calculateOBO(Context ctx) throws HomeException
    {
        long outstatndingOwingOfSubscribers =0;
        long outstandingOwingTax = 0;
        
        try
        {
            for (Iterator i = OBOPayableSubscribers.iterator(); i.hasNext();)
            {
                SubscriberPaymentDistribution subDistribution = getSubscriberPaymentDistribution(ctx, (Subscriber) i.next());  
                outstatndingOwingOfSubscribers +=  subDistribution.getOutStandingOwing();
                outstandingOwingTax += subDistribution.getOutStandingTaxOwing();
            }
            long accountOutStandingOwing = account.getAccumulatedBalance(); 
            
            if (outstatndingOwingOfSubscribers != accountOutStandingOwing)
            {
                String msg = "The account out standing owing is " + accountOutStandingOwing + 
                        " but subscriber total owing is " + outstatndingOwingOfSubscribers;
                ResultCode rCode = ResultCode.GENERAL_ERROR;
                logError(ctx, rCode, msg);
            }
        }catch(Exception ex)
        {
            ResultCode rCode = ResultCode.GENERAL_ERROR;
            logError(ctx, rCode, ex.getMessage());
        }
        setOutStandingOwing(outstatndingOwingOfSubscribers);
        setOutStandingTaxOwing(outstandingOwingTax);
    }

    private void logError(Context ctx, ResultCode rCode, String msg)
    {
        WriteOffErrorReport.logError(logOutput_, rCode, msg, account);
        LogSupport.minor(ctx, this, msg);
    }

    /* (non-Javadoc)
     * @see com.redknee.app.crm.transaction.AccountPaymentDistribution#getSubscriberPaymentDistribution(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber)
     */
    //@Override
    
    protected SubscriberPaymentDistribution getSubscriberPaymentDistribution(Context ctx, Subscriber sub)
    {
        String subId = sub.getId();
        SubscriberPaymentDistribution subDistribution = (SubscriberPaymentDistribution)subscriberDistributions.get(subId);
        if ( subDistribution == null)
        {
            try
            {
                subDistribution = new SubscriberWriteOffProcessor(ctx, sub);
                subscriberDistributions.put(subId, subDistribution);
            }
            catch (CalculationServiceException e)
            {
                ResultCode rCode = ResultCode.GENERAL_ERROR;
                logError(ctx, rCode, e.getMessage());
            }
        }
        return subDistribution; 
    }
    
    private void logError(Context ctx, Subscriber subscriber, ResultCode rCode, long writeOffAmount)
    {
        WriteOffErrorReport errReport = new WriteOffErrorReport(account, subscriber, rCode, null);
        errReport.setWriteOffAmount(writeOffAmount);
        errReport.print(logOutput_);
        LogSupport.minor(ctx, this, errReport.toString());
    }

    protected void createDistributions(Context ctx)
    {
        Home transHome = (Home) ctx.get(TransactionHome.class); 

        for (Iterator i = subscriberDistributions.values().iterator(); i.hasNext();)
        {
            SubscriberPaymentDistribution subDistribution = (SubscriberPaymentDistribution)i.next();
            Subscriber subscriber = subDistribution.getSubscriber();
            long subWriteOffAmount = subDistribution.getPaymentForOutStandOwing();
            if (subDistribution.createTransactionForWriteOff(ctx, orignalTransaction, transHome, getCurrency(ctx)) )
            {
                successAmount += subWriteOffAmount; 
                ++successCount; 
            } 
            else 
            {
                this.failedAmount += subWriteOffAmount; 
                ++failedCount; 
                logError(ctx, subscriber, WriteOffErrorReport.ResultCode.WRITE_OFF_TRXN_FAILURE, subWriteOffAmount);        
            }
        }
    }
    
    
    public Transaction createTransaction(Context ctx)
        throws HomeException
    {
        Transaction trans = new Transaction(); 
        trans.setBAN(writeOff_.BAN);
        trans.setAmount(getOutStandingOwing() * -1);
        
        int spid = account.getSpid();
        trans.setSpid(spid);
        
        long externalTxnId = writeOff_.ExternalTransactionId;
        if (externalTxnId!=WriteOffInput.INVALID_NUMBER)
        {
            trans.setExtTransactionId(String.valueOf(externalTxnId));  
        }
        trans.setResponsibleBAN(account.getResponsibleBAN());
        trans.setPayee(PayeeEnum.Subscriber); 
        //trans.setTransactionMethod(TransactionMethodTypeEnum.Cash.getIndex());
        trans.setTransactionMethod(SystemTransactionMethodsConstants.TRANSACTION_METHOD_CASH);
        
        Date today = new Date();
        trans.setReceiveDate(today);
        trans.setTransDate(today);
        
        AdjustmentType adj = WriteOffSupport.getWriteOffAdjustmentType(ctx);
        
        trans.setAdjustmentType(adj.getCode());
        trans.setAction(adj.getAction());
        
        AdjustmentInfo adjustInfo = (AdjustmentInfo)adj.getAdjustmentSpidInfo().get(
                new Integer(spid));
        trans.setGLCode(adjustInfo.getGLCode());
        
        return trans; 
    }
    
    

}