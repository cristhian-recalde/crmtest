/*
 *  InvoicePreviewAction.java
 *
 *  Author : Gary Anderson
 *  Date   : 2003-10-09
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;
import java.security.Permission;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.util.Link;
import com.trilogy.framework.xhome.webcontrol.DateWebControl;

import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.AccountHistoryTypeEnum;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.WalletReport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;


/**
 * Provides a "preview" action for viewing the PDF of the invoice.
 *
 * TODO - 2009-06-26 - Refactor this along with ReportPreviewAction/ReportDeliveryAction, all subclasses of each, 
 *                     InvoiceViewerRequestServicer, InvoicePreviewFallbackBorder, InvoiceAccountNoteWebBorder,
 *                     all wallet reports derivations of each.  Together, it is all terrible and not maintainable.
 *                     
 * @author Gary Anderson
 */
public abstract class ReportPreviewAction
    extends SimpleWebAction
{   
    public static final String VIEW_BUTTON = "View";
    
    public ReportPreviewAction(String cmdDelegate)
    {
        super();
        cmdName_ = cmdDelegate;
    }


    /**
     * Creates a new "preview" action.
     *
     * @param action The name of the action associated with this link.
     * @param label The label of the link.
     */
    public ReportPreviewAction(final String action, final String label, String cmdDelegate)
    {
        super(action, label);
        cmdName_ = cmdDelegate;
    }


    /**
     * Creates a new "preview" action.
     *
     * @param permission The permissions required for the action.
     * @param action The name of the action associated with this link.
     * @param label The label of the link.
     */
    public ReportPreviewAction(final Permission permission, final String action, final String label, String cmdDelegate)
    {
        this(action, label, cmdDelegate);
        setPermission(permission);
    }


    /**
     * Writes out the link used to initiate this action.
     *
     * @param ctx The operating context.
     * @param out The writer creating the web page.
     * @param bean The invoice for which the action link is being rendered.
     * @param link The parent link object providing session information.
     */
    @Override
    public void writeLink(
        final Context ctx,
        final PrintWriter out,
        final Object bean,
        final Link link)
    {
        boolean render = canRender(bean);
        if ( render )
        {            
            // TODO - 2003-11-15 - See if there is a way of automatically generating
            // the required parameters.  Explicitly writing them out here is error
            // prone.
            final Link newLink = new Link(link);
            
            // Make it so that this action's link brings you to another page
            newLink.addRaw("cmd", cmdName_);
            
            // Hide everything from the other page
            newLink.addRaw("header", "hide");
            newLink.addRaw("border", "hide");
            newLink.addRaw("menu", "hide");
            
            // Make the destination page think that its InvoiceViewerForm is populated 
            newLink.addRaw(".accountIdentifier", getBAN(bean));
            newLink.addRaw(".billingDate", DateWebControl.DEFAULT_FORMAT.format(getBillingDate(bean)));
            //newLink.addRaw(".billingDateNoTZ", Long.toString(getBillingDate(bean).getTime()));
            newLink.addRaw(".agentIdentifier", CoreTransactionSupportHelper.get(ctx).getCsrIdentifier(ctx));
            
            
            // Make the destination page think that its view button was clicked
            newLink.addRaw(DefaultButtonRenderer.BUTTON_KEY, VIEW_BUTTON);
            newLink.addRaw(VIEW_BUTTON + DefaultButtonRenderer.BUTTON_KEY + ".x", "submit");
            
            // This part is really ugly.  It implies that the destination page may be aware of this action's key,
            // which is configurable in the GUI
            newLink.addRaw("action", getKey());
            
            super.writeLink(ctx, out, bean, newLink);
        }
    }

    /**
     * Depending on the report type it will return the BAN
     * @param bean
     * @return the BAN
     */
    public String getBAN(Object bean)
    {
        String ban = null;
        if ( bean instanceof Invoice )
        {
            final Invoice invoice = (Invoice)bean;
            ban = invoice.getBAN();
        }
        else if ( bean instanceof WalletReport )
        {
            final WalletReport report = (WalletReport)bean;
            ban = report.getBAN();
        }
        else if ( bean instanceof AccountHistory )
        {
            AccountHistory acctHist = (AccountHistory) bean;
            if ( acctHist.getType() == AccountHistoryTypeEnum.INVOICE )
            {
                ban = acctHist.getBAN();
            }
        }
        
        return ban;
    }

    /**
     * Depending on the report type it will return the BillingDate
     * @param bean
     * @return the BAN
     */
    public Date getBillingDate(Object bean)
    {
        Date date = null;
        if ( bean instanceof Invoice )
        {
            final Invoice invoice = (Invoice)bean;
            date = invoice.getInvoiceDate();
        }
        else if ( bean instanceof WalletReport )
        {
            final WalletReport report = (WalletReport)bean;
            date = report.getReportDate();
        }
        else if ( bean instanceof AccountHistory )
        {
            AccountHistory acctHist = (AccountHistory) bean;
            if ( acctHist.getType() == AccountHistoryTypeEnum.INVOICE )
            {
                date = acctHist.getKeyDate();
            }
        }
        
        return date;
    }
    
    
    /**
     * Depending on the report type it will return the BillingDate
     * @param bean
     * @return the BAN
     */
    public boolean canRender(Object bean)
    {
        boolean render = false;
        if ( bean instanceof Invoice )
        {
            render = true;
        }
        else if ( bean instanceof WalletReport )
        {
            render = true;
        }
        else if ( bean instanceof AccountHistory )
        {
            AccountHistory acctHist = (AccountHistory) bean;
            if ( acctHist.getType() == AccountHistoryTypeEnum.INVOICE )
            {
                render = true;
            }
        }
        
        return render;
    }

    
    /**
     * The abstract command name
     */
    protected final String cmdName_;
} // class
