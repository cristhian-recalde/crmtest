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
package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.support.CurrencyPrecisionSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;



/**
 * Overdraft Balance Limit.
 * @author Marcio Marques
 * @since 9.1.1
 *
 */
public class OverdraftBalanceLimit 
extends AbstractOverdraftBalanceLimit
{
    public String getDesc(Context ctx)
    {
        String currency = null;
        if (getSpid()>0)
        {
            try
            {
                CRMSpid spid = SpidSupport.getCRMSpid(ctx, getSpid());
                currency = spid.getCurrency();
            }
            catch (Throwable t)
            {
                LogSupport.minor(ctx,  this, "Unable to retrieve SPID", t);
            }
        }
        
        if (currency == null)
        {
            currency =  ((Currency) ctx.get(Currency.class, Currency.DEFAULT)).getCode();
        }
        
        return CurrencyPrecisionSupportHelper.get(ctx).formatDisplayCurrencyValue(ctx,
                currency, getLimit());
        
    }

}
