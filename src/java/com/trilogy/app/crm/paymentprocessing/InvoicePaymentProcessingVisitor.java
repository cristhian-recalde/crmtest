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
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Visitor to do invoice payment processing.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
public class InvoicePaymentProcessingVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -8050844092270528829L;

    /**
     * Invoice postprocessor.
     */
    private PaymentPromotionProcessor processor_;

    /**
     * Billing date.
     */
    private Date billingDate_;

    /**
     * Whether to force reprocessing.
     */
    private boolean forceReprocess_;

    /**
     * Creates an <code>InvoicePostprocessingVisitor</code> instance.
     * 
     * @param processor
     *            Invoice postprocessor.
     * @param billingDate
     *            Billing date to use.
     * @param forceReprocess
     *            Whether to force reprocessing.
     */
    public InvoicePaymentProcessingVisitor(PaymentPromotionProcessor processor,
            Date billingDate, boolean forceReprocess)
    {
        processor_ = processor;
        billingDate_ = billingDate;
        forceReprocess_ = forceReprocess;
    }

    /**
     * Invoice postprocessing.
     * 
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        this.processor_.processInvoice(ctx, (Invoice) obj, billingDate_,
                forceReprocess_);
    }

}
