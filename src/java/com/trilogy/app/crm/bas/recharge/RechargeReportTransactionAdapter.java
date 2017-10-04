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

package com.trilogy.app.crm.bas.recharge;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.RecurringChargeErrorReport;
import com.trilogy.app.crm.bean.Transaction;


/**
 * Convert a transaction to RecurringChargeErrorReport or vice versa.
 *
 * @author larry.xia@redknee.com
 */
public class RechargeReportTransactionAdapter implements Adapter
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Converts a transaction to recharge error report.
     *
     * @param trans
     *            Transaction.
     * @return A corresponding <code>RecurringChargeErrorReport</code>.
     */
    public static RecurringChargeErrorReport toReport(final Transaction trans)
    {

        final RecurringChargeErrorReport report = new RecurringChargeErrorReport();

        report.setReceiptNum(trans.getReceiptNum());
        report.setAgent(trans.getAgent());
        report.setPayee(trans.getPayee());
        report.setBAN(trans.getBAN());
        report.setSubscriberID(trans.getSubscriberID());
        report.setMSISDN(trans.getMSISDN());
        report.setSpid(trans.getSpid());
        report.setAdjustmentType(trans.getAdjustmentType());
        report.setAmount(trans.getAmount());
        report.setBalance(trans.getBalance());
        report.setExpiryDaysExt(trans.getExpiryDaysExt());
        report.setTaxPaid(trans.getTaxPaid());
        report.setPaymentAgency(trans.getPaymentAgency());
        report.setLocationCode(trans.getLocationCode());
        report.setExtTransactionId(trans.getExtTransactionId());
        report.setPaymentDetails(trans.getPaymentDetails());
        report.setReceiveDate(trans.getReceiveDate());
        report.setTransDate(trans.getTransDate());
        report.setCSRInput(trans.getCSRInput());
        report.setGLCode(trans.getGLCode());
        report.setReasonCode(trans.getReasonCode());
        report.setReconciliationState(trans.getReconciliationState());
        report.setTransactionMethod(trans.getTransactionMethod());
        report.setCreditCardNumber(trans.getCreditCardNumber());
        report.setExpDate(trans.getExpDate());
        report.setBankTransit(trans.getBankTransit());
        report.setBankAccount(report.getBankAccount());
        report.setHolderName(trans.getHolderName());
        report.setSubscriptionCharge(trans.getSubscriptionCharge());
        report.setSupportedSubscriberID(trans.getSupportedSubscriberID());
        report.setSubscriberType(trans.getSubscriberType());
        report.setSubscriptionTypeId(trans.getSubscriptionTypeId());
        report.setResponsibleBAN(trans.getResponsibleBAN());
        report.setSAPCenterCode1(trans.getSAPCenterCode1());
        report.setSAPCenterCode2(trans.getSAPCenterCode2());
        report.setSAPCenterType1(trans.getSAPCenterType1());
        report.setSAPCenterType2(trans.getSAPCenterType2());
        report.setSapDocHeader(trans.getSapDocHeader()); 
        report.setAccountType(trans.getAccountType());
        report.setGlcode2(trans.getGlcode2()); 
        report.setPMethodCardTypeId(trans.getPMethodCardTypeId());
        report.setPMethodBankID(trans.getPMethodBankID());
        return report;
    }


    /**
     * Convert a recharge error report to transaction.
     *
     * @param ctx
     *            The operating context.
     * @param report
     *            Recurring error report.
     * @return The corresponding transaction.
     */
    public static Transaction toTransaction(final RecurringChargeErrorReport report)
    {

        final Transaction trans = new com.redknee.app.crm.bean.core.Transaction();

        trans.setReceiptNum(report.getReceiptNum());
        trans.setAgent(report.getAgent());
        trans.setPayee(report.getPayee());
        trans.setBAN(report.getBAN());
        trans.setSubscriberID(report.getSubscriberID());
        trans.setMSISDN(report.getMSISDN());
        trans.setSpid(report.getSpid());
        trans.setAdjustmentType(report.getAdjustmentType());
        trans.setAmount(report.getAmount());
        trans.setBalance(report.getBalance());
        trans.setExpiryDaysExt(report.getExpiryDaysExt());
        trans.setTaxPaid(report.getTaxPaid());
        trans.setPaymentAgency(report.getPaymentAgency());
        trans.setLocationCode(report.getLocationCode());
        trans.setExtTransactionId(report.getExtTransactionId());
        trans.setPaymentDetails(report.getPaymentDetails());
        trans.setReceiveDate(report.getReceiveDate());
        trans.setTransDate(report.getTransDate());
        trans.setCSRInput(report.getCSRInput());
        trans.setGLCode(report.getGLCode());
        trans.setReasonCode(report.getReasonCode());
        trans.setReconciliationState(report.getReconciliationState());
        trans.setTransactionMethod(report.getTransactionMethod());
        trans.setCreditCardNumber(report.getCreditCardNumber());
        trans.setExpDate(report.getExpDate());
        trans.setBankTransit(report.getBankTransit());
        trans.setBankAccount(report.getBankAccount());
        trans.setHolderName(report.getHolderName());
        trans.setSubscriptionCharge(report.getSubscriptionCharge());
        trans.setSupportedSubscriberID(report.getSupportedSubscriberID());
        trans.setSubscriberType(report.getSubscriberType());
        trans.setSubscriptionTypeId(report.getSubscriptionTypeId());
        trans.setResponsibleBAN(report.getResponsibleBAN());
        trans.setSAPCenterCode1(report.getSAPCenterCode1());
        trans.setSAPCenterCode2(report.getSAPCenterCode2());
        trans.setSAPCenterType1(report.getSAPCenterType1());
        trans.setSAPCenterType2(report.getSAPCenterType2());
        trans.setSapDocHeader(report.getSapDocHeader()); 
        trans.setAccountType(report.getAccountType());
        trans.setGlcode2(report.getGlcode2());
        trans.setPMethodCardTypeId(report.getPMethodCardTypeId());
        trans.setPMethodBankID(report.getPMethodBankID());
        return trans;
    }


    /**
     * {@inheritDoc}
     */
    public Object adapt(final Context ctx, final Object trans) throws HomeException
    {
        return toReport((Transaction) trans);

    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(final Context ctx, final Object report) throws HomeException
    {
        return toTransaction((RecurringChargeErrorReport) report);
    }
}
