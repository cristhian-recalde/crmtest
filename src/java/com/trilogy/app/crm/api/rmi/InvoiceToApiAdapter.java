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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.invoice.InvoiceReference;

/**
 * Adapts Invoice object to API objects.
 *
 * @author victor.stratan@redknee.com
 */
public class InvoiceToApiAdapter implements Adapter
{
    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptInvoiceToReference((Invoice) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static com.redknee.util.crmapi.wsdl.v3_0.types.invoice.Invoice adaptInvoiceToApi(final Invoice invoice)
    {
        final com.redknee.util.crmapi.wsdl.v3_0.types.invoice.Invoice apiInvoice;
        apiInvoice = new com.redknee.util.crmapi.wsdl.v3_0.types.invoice.Invoice();
        adaptInvoiceToReference(invoice, apiInvoice);
        apiInvoice.setTaxAmount(Long.valueOf(invoice.getTaxAmount()));
        apiInvoice.setRecurringChargeAmount(Long.valueOf(invoice.getRecurringCharges()));
        apiInvoice.setDomesticCallsAmount(Long.valueOf(invoice.getDomesticCallsAmount()));
        apiInvoice.setInternationalCallsAmount(Long.valueOf(invoice.getInternationalCallsAmount()));
        apiInvoice.setRoamingAmount(Long.valueOf(invoice.getRoamingAmount()));
        apiInvoice.setDataAmount(Long.valueOf(invoice.getDataAmount()));
        apiInvoice.setSupplementaryChargesAmount(Long.valueOf(invoice.getSupplementaryChargesAmount()));
        apiInvoice.setDroppedCallCompensationAmount(Long.valueOf(invoice.getDroppedCallCreditAmount()));
        apiInvoice.setOtherChargesAmount(Long.valueOf(invoice.getOtherChargesAmount()));
        apiInvoice.setOneTimeChangesAmount(Long.valueOf(invoice.getOneTimeChargesAmount()));
        apiInvoice.setDebtLastBillCycle1(Long.valueOf(invoice.getDebtLastBillCycle1()));
        apiInvoice.setDebtLastBillCycle2(Long.valueOf(invoice.getDebtLastBillCycle2()));
        apiInvoice.setDebtLastBillCycle3(Long.valueOf(invoice.getDebtLastBillCycle3()));
        apiInvoice.setPdfFilePath(invoice.getURL());

        return apiInvoice;
    }

    public static InvoiceReference adaptInvoiceToReference(final Invoice invoice)
    {
        final InvoiceReference reference = new InvoiceReference();
        adaptInvoiceToReference(invoice, reference);

        return reference;
    }

    public static InvoiceReference adaptInvoiceToReference(final Invoice invoice, final InvoiceReference reference)
    {
        reference.setInvoiceID(invoice.getInvoiceId());
        reference.setAccountID(invoice.getBAN());
        reference.setInvoiceDate(CalendarSupportHelper.get().dateToCalendar(invoice.getInvoiceDate()));
        reference.setGenerationDate(CalendarSupportHelper.get().dateToCalendar(invoice.getGeneratedDate()));
        reference.setDueDate(CalendarSupportHelper.get().dateToCalendar(invoice.getDueDate()));
        reference.setPreviousBalance(Long.valueOf(invoice.getPreviousPayableBalance()));
        reference.setCurrentAmount(Long.valueOf(invoice.getCurrentAmount()));
        reference.setTotalAmount(Long.valueOf(invoice.getPayableAmount()));
        reference.setPaymentAmount(Long.valueOf(invoice.getPaymentAmount()));
        reference.setDiscountAmount(Long.valueOf(invoice.getDiscountAmount()));
        reference.setCurrentTaxAmount(Long.valueOf(invoice.getCurrentTaxAmount()));
        reference.setPdfFilePath(invoice.getURL());

        return reference;
    }
}
