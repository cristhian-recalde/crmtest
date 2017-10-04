/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.writeoff;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberInvoice;
import com.trilogy.app.crm.bean.SubscriberXDBHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.transaction.SubscriberPaymentDistribution;


/**
 * 
 * 
 * @author alpesh.champeneri@redknee.com
 */
public class SubscriberWriteOffProcessor extends SubscriberPaymentDistribution
{

    /**
     * @param ctx
     * @param sub
     * @throws CalculationServiceException
     */
    public SubscriberWriteOffProcessor(Context ctx, Subscriber sub) throws CalculationServiceException
    {
        try
        {
            this.sub = sub;
            Account account = sub.getAccount(ctx);
            calculateTotalOwing(ctx, account.getTaxExemption());
        }
        catch (Exception ex)
        {
            LogSupport.debug(ctx, this, "Raised Ex SubscriberWriteOffProcessor:" + ex);
        }
    }


    private void calculateTotalOwing(Context ctx, boolean taxExemption)
    {
        Context subCtx = ctx.createSubContext();
        
        long previousBalance = 0;
        long previousTax = 0;
        final SubscriberInvoice previousInvoice = InvoiceSupport.getMostRecentSubscriberInvoice(subCtx, sub.getId());
        if (previousInvoice != null)
        {
            previousBalance = previousInvoice.getTotalAmount();
            previousTax = previousInvoice.getTaxAmount();
        }
        try
        {
            CalculationService service = (CalculationService) subCtx.get(CalculationService.class);
            long monthToDateDue = service.getAccountAccumulatedMDUsage(subCtx, sub.getBAN());
            //SubscriberInvoice invoice = service.getMostRecentSubscriberInvoice(subCtx, sub.getId());
            long monthToDateTax = service.getAccountAccumulatedMDUsageTax(subCtx, sub.getBAN());// InvoiceCalculationSupport.getTotalTax(monthToDateCalculation);
            setOutStandingOwing(previousBalance + monthToDateDue);
            setOutStandingTaxOwing(previousTax + monthToDateTax);
            
        }
        catch (Exception ex)
        {
            LogSupport.debug(subCtx, this, "Raised Ex calculateTotalOwing() :" + ex);
        }
    }


    private Transaction cloneTransaction(final Transaction trans) throws HomeException
    {
        Transaction clone = null;
        try
        {
            clone = (Transaction) trans.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new HomeException("Failed to clone the original transaction");
        }
        return clone;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.transaction.SubscriberPaymentDistribution#createTransaction
     * (com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Transaction,
     * com.redknee.framework.xhome.home.Home, com.redknee.framework.core.locale.Currency,
     * boolean)
     */
    @Override
    public boolean createTransaction(Context ctx, Transaction txn, Home transHome, Currency currency)
    {
        if (sub.getWrittenOff())
            return true;
        boolean ret = false;
        long payment = getPaymentForOutStandOwing();
        try
        {
            final Transaction subTrans = cloneTransaction(txn);
            subTrans.setMSISDN(sub.getMSISDN());
            String subId = sub.getId();
            subTrans.setSubscriberID(subId);
            subTrans.setBAN(sub.getBAN());
            long tax = getPaymentForOutStandTaxOwing();
            long payCharge = payment - tax;
            subTrans.setAmount(payCharge);
            subTrans.setTaxPaid(0);
            createTransaction(ctx, transHome, subTrans);
            
            subTrans.setReceiptNum(Transaction.DEFAULT_RECEIPTNUM);
            AdjustmentType adType = WriteOffSupport.getWriteOffTaxAdjustmentType(ctx);
            subTrans.setAdjustmentType(adType.getCode());
            subTrans.setAction(adType.getAction());
            AdjustmentInfo adjustInfo = (AdjustmentInfo) adType.getAdjustmentSpidInfo().get(new Integer(txn.getSpid()));
            txn.setGLCode(adjustInfo.getGLCode());
            subTrans.setAmount(tax);
            subTrans.setTaxPaid(tax);
            createTransaction(ctx, transHome, subTrans);
            setWriteOffFlag(ctx, sub, true);
            ret = true;
        }
        catch (Throwable e)
        {
            handleCreationFailure(ctx, payment, currency, e);
        }
        return ret;
    }


    public static void setWriteOffFlag(Context ctx, Subscriber sub, boolean flag)
    {
        Home home = (Home) ctx.get(SubscriberXDBHome.class);
        sub.setWrittenOff(flag);
        try
        {
            home.store(sub);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, SubscriberWriteOffProcessor.class.getSimpleName(),
                    "Failed to set write off flag for sub -" + sub.getId(), e);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.redknee.app.crm.transaction.AbstractPaymentDistribution#getPaymentForOutStandOwing
     * ()
     */
    @Override
    public long getPaymentForOutStandOwing()
    {
        return -getOutStandingOwing();
    }


    /*
     * (non-Javadoc)
     * 
     * @see com.redknee.app.crm.transaction.AbstractPaymentDistribution#
     * getPaymentForOutStandTaxOwing()
     */
    
    public long getPaymentForOutStandTaxOwing()
    {
        return -getOutStandingTaxOwing();
    }
    // private Collection<TaxAuthority> getZeroTaxAuths(Context ctx, int spid)
    // {
    // Home taxAuthHome = (Home)ctx.get(TaxAuthorityHome.class);
    // return null;
    // }
}