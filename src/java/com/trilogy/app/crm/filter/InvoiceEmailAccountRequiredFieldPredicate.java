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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.invoice.delivery.InvoiceDeliveryOption;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Predicate matching business accounts.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class InvoiceEmailAccountRequiredFieldPredicate implements AccountRequiredFieldPredicate
{

    /**
     * {@inheritDoc}
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof Account)
        {
            long optionId = ((Account) obj).getInvoiceDeliveryOption();
            try
            {
                InvoiceDeliveryOption option = HomeSupportHelper.get(ctx).findBean(ctx, InvoiceDeliveryOption.class, optionId);
                return option != null && option.isEmail(); 
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error looking up invoice delivery option " + optionId, e).log(ctx);
            }
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * {@inheritDoc}
     */
    public Object deepClone() throws CloneNotSupportedException
    {
        return clone();
    }

}
