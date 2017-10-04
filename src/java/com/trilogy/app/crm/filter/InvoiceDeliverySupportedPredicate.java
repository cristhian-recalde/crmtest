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
package com.trilogy.app.crm.filter;

import java.util.Set;

import com.trilogy.framework.xhome.beans.XDeepCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Invoice;
import com.trilogy.app.crm.bean.WalletReport;
import com.trilogy.app.crm.invoice.delivery.DeliveryTypeEnum;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 
 */
public class InvoiceDeliverySupportedPredicate implements Predicate, XDeepCloneable
{
    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        boolean result = false;
        
        if (isDeliveryTypeAvailableAndSupported(ctx, obj))
        {
            result = true;
        }

        // TODO: Should we only return true if the invoice exists?
        
        return result;
    }


    private boolean isDeliveryTypeAvailableAndSupported(Context ctx, Object obj)
    {
        boolean result = false;
        boolean someEmailIdExists = false;
        boolean nonGhostInvoice = false;
        InvoiceDeliveryOption deliveryOption = null;
        try
        {
            Account account = getAccount(ctx, obj);
            if (account != null)
            {
                long optionID = account.getInvoiceDeliveryOption();
                deliveryOption = HomeSupportHelper.get(ctx).findBean(ctx, InvoiceDeliveryOption.class, optionID);
                if (account.getEmailID().trim().length() > 0)
                {
                    someEmailIdExists = true;
                }
            }
            
            if (obj instanceof Invoice)
            {
                final Invoice invoice = (Invoice) obj;
                if (invoice.getInvoiceId() != null && invoice.getInvoiceId().trim().length() > 0)
                {
                    nonGhostInvoice = true;
                }
            }
        }
        catch (HomeException e)
        {
            // NOP
        }
        
        if (deliveryOption != null)
        {
            boolean validTypeFound = false;
            Set deliveryTypes = deliveryOption.getDeliveryType();
            for (Object deliveryTypeObj : deliveryTypes)
            {
                DeliveryTypeEnum deliveryType = null;
                if (deliveryTypeObj instanceof DeliveryTypeEnum)
                {
                    deliveryType = (DeliveryTypeEnum) deliveryTypeObj;
                }
                else if (deliveryTypeObj instanceof Short)
                {
                    deliveryType = DeliveryTypeEnum.get((Short)deliveryTypeObj);
                }
                
                // For now, only email delivery type is supported for anything that this
                // predicate is intended to be used for (i.e. manual invoice emailing)
                if (deliveryType == DeliveryTypeEnum.EMAIL)
                {
                    validTypeFound = true;
                }
            }
            if (validTypeFound)
            {
                result = true;
            }
        }
        
        return result && someEmailIdExists && nonGhostInvoice;
    }


    private Account getAccount(Context ctx, Object bean) throws HomeException
    {
        Account account = (Account) ctx.get(Account.class);
        if (bean instanceof Account)
        {
            account = (Account) bean;
        }
        else if (bean instanceof Invoice)
        {
            Invoice invoice = (Invoice) bean;
            if (account == null
                    || !account.getBAN().equals(invoice.getBAN()))
            {
                account = AccountSupport.getAccount(ctx, invoice.getBAN());
            }
        }
        else if (bean instanceof WalletReport)
        {
            WalletReport report = (WalletReport) bean;
            if (account == null
                    || !account.getBAN().equals(report.getBAN()))
            {
                account = AccountSupport.getAccount(ctx, report.getBAN());
            }
        }
        return account;
    }


    /**
     * {@inheritDoc}
     */
    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

}
