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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.TestInvoice;
import com.trilogy.app.crm.bean.WalletReport;
import com.trilogy.app.crm.invoice.service.InvoiceServerService;
import com.trilogy.app.crm.invoice.service.InvoiceServerServiceException;
import com.trilogy.app.crm.invoice.service.InvoiceServerServiceInternalException;


/**
 * Home that will execute the invoice/report deletion on IS instead of CRM.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.5
 */
public class DeleteInvoiceHome extends HomeProxy
{

    public DeleteInvoiceHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        InvoiceServerService service = (InvoiceServerService) ctx.get(InvoiceServerService.class);
        if (obj instanceof TestInvoice)
        {
            TestInvoice testInvoice = (TestInvoice) obj;
            try
            {
                service.deleteTestInvoice(ctx, testInvoice.getBAN());
            }
            catch (InvoiceServerServiceInternalException e)
            {
                throw new HomeInternalException("Unable to delete invoice with ID=" + testInvoice.ID(), e);
            }
            catch (InvoiceServerServiceException e)
            {
                throw new HomeException("Unable to delete invoice with ID=" + testInvoice.ID(), e);
            }
        }
        else if (obj instanceof Invoice)
        {
            Invoice invoice = (Invoice) obj;
            try
            {
                service.deleteInvoice(ctx, invoice.getBAN(), invoice.getInvoiceDate());
            }
            catch (InvoiceServerServiceInternalException e)
            {
                throw new HomeInternalException("Unable to delete invoice with ID=" + invoice.ID()+". "+e.getCause().getMessage(), e);
            }
            catch (InvoiceServerServiceException e)
            {
                throw new HomeException("Unable to delete invoice with ID=" + invoice.ID()+". "+e.getCause().getMessage(), e);
            }
        }
        else if (obj instanceof WalletReport)
        {
            WalletReport report = (WalletReport) obj;
            try
            {
                service.deleteWalletReport(ctx, report.getBAN(), report.getReportDate());
            }
            catch (InvoiceServerServiceInternalException e)
            {
                throw new HomeInternalException("Unable to delete report with ID=" + report.ID(), e);
            }
            catch (InvoiceServerServiceException e)
            {
                throw new HomeException("Unable to delete report with ID=" + report.ID(), e);
            }
        }
    }

}
