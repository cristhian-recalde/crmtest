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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

/**
 * This Home decorator sets the SubscriberType property in the Transaction bean.
 *
 * @author daniel.zhang@redknee.com
 */
public class TransactionSetSubTypeHome extends SetSubTypeHome
{
    /**
     * Constructor.
     * @param delegate the home decorator to delegate to
     */
    public TransactionSetSubTypeHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSubType(final Context ctx, final Object obj) throws HomeException
    {
        if (obj != null)
        {
            if (obj instanceof Transaction)
            {
                final Transaction t = (Transaction) obj;
                final String ban = t.getAcctNum();
                final String msisdn = t.getMSISDN();
                final Date date = t.getTransDate();
                final SubscriberTypeEnum st = getSubscriberType(ctx, ban, msisdn, date);
                if (st != null)
                {
                    t.setSubscriberType(st);
                }
                if (SubscriberTypeEnum.PREPAID.equals(st) && CoreTransactionSupportHelper.get(ctx).isDeposit(ctx, t))
                    
                {
                    throw new HomeException(
                        "For prepaid subscribers, the adjustment type cannot be under the Deposit Payments category. ");
                }
            }
        }
    }

}
