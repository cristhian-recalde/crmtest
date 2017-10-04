/*
 *  InvoicePredictionHome.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-11-14
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Provides a way of previewing future invoices.  When searched (using the
 * InvoiceSearchBorder) for invoices of a specific account, a "ghost" invoice
 * appears for that invoice for the account's next billing date.  Viewing that
 * predicted invoice causes it to be generated on-the-fly and shown to the
 * user.
 *
 * @author Gary Anderson
 */
public class InvoicePredictionHome
    extends HomeProxy
{
    private String ban_;
    /**
     * Creates a new InvoicePredictionHome.
     *
     * @param delegate The home to delegate to.
     */
    public InvoicePredictionHome(Context ctx, String ban, Home delegate)
    {
        super(ctx, delegate);
        setBan(ban);
    }

    public String getBan()
    {
       return ban_;
    }

    public void setBan(String ban)
    {
       ban_ = ban;
    }

    // INHERIT
    @Override
    public Collection select(Context ctx, Object where)
        throws HomeException, UnsupportedOperationException
    {
        final Collection list = super.select(ctx,where);     

        final String accountNumber = getBan();
        
        if (accountNumber != null)
        {
            final Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, accountNumber);
            if (account != null)
            {
                /* Previous to CRM 8.2 and the introduction of Group Accounts, here we would only 
                 * display the "ghost invoice" record if the given account had direct postpaid 
                 * Subscribers (query was done using a direct SQL query into the Subscriber table
                 * for the given BAN).  Due to the changes in hierarchy, Group Accounts would never 
                 * display the "ghost invoice" record.  
                 * Moving forward we will always display the "ghost invoice" record.  In theory,
                 * there isn't any harm in allowing the account to generate invoice with no activity.
                 */

                final Date nextBillingDate = InvoiceSupport.calculateNextBillingDate(ctx, account.getBillCycleID());

                final Invoice predictedInvoice = new Invoice();
                predictedInvoice.setBAN(accountNumber);
                predictedInvoice.setGeneratedDate(new Date());
                predictedInvoice.setSpid(account.getSpid());
                predictedInvoice.setInvoiceDate(nextBillingDate);
                predictedInvoice.setGhost(true);

                list.add(predictedInvoice);
            }
        }

        return list;
    }

    /**
     * The operating context.
     */
    protected Context context_ = new ContextSupport();

} // class
