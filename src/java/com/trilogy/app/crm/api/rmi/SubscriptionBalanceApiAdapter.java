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

import com.trilogy.app.crm.bundle.Balance;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_1.types.subscription.SubscriptionBundleBalance;

/**
 * @author sbanerjee
 *
 */
public class SubscriptionBalanceApiAdapter
    implements Adapter
{

    public static void adapt(Balance from,
            SubscriptionBundleBalance to)
    {
        to.setBundleCategoryId(from.getApplicationId());
        to.setGroupBalance(from.getGroupBalance());
        to.setGroupLimit(from.getGroupLimit());
        to.setGroupUsed(from.getGroupUsed());
        to.setPersonalBalance(from.getPersonalBalance());
        to.setPersonalLimit(from.getPersonalLimit());
        to.setPersonalUsed(from.getPersonalUsed());
        to.setRolloverBalance(from.getRolloverBalance());
        to.setRolloverLimit(from.getRolloverLimit());
        to.setRolloverUsed(from.getRolloverUsed());
    }

    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        Balance from = (Balance) obj;
        SubscriptionBundleBalance to = new SubscriptionBundleBalance();
        adapt(from, to);
        return to;
    }

    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

}
