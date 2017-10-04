/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.paymentprocessing;

import java.util.Date;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.framework.xhome.context.Context;

/**
 * Invoice payment processor interface.
 * 
 * @author cindy.wong@redknee.com
 */
public interface PaymentPromotionProcessor
{
    /**
     * Process a single invoice.
     * 
     * @param ban
     *            BAN of account.
     * @param invoiceDate
     *            Invoice date.
     */
    void processInvoice(Context context, String ban, Date invoiceDate);

    /**
     * Process a single invoice.
     * 
     * @param ban
     *            BAN of account.
     * @param invoiceDate
     *            Invoice date.
     * @param processDate
     *            Date to process invoice.
     * @param forceReprocess
     *            Whether to force reprocessing
     */
    void processInvoice(Context context, String ban, Date invoiceDate,
            Date processDate, boolean forceReprocess);

    /**
     * Process a single invoice.
     * 
     * @param invoice
     *            Invoice to process.
     * @param invoiceDate
     *            Invoice date.
     */
    void processInvoice(Context context, Invoice invoice);

    /**
     * Process a single invoice.
     * 
     * @param invoice
     *            Invoice to process.
     * @param processDate
     *            Date to process invoice.
     * @param forceReprocess
     *            Whether to force reprocessing
     */
    void processInvoice(Context context, Invoice invoice, Date processDate,
            boolean forceReprocess);
}
