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
package com.trilogy.app.crm.deposit;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.AdjustmentType;

/**
 * A transaction creator strategy for testing.
 *
 * @author cindy.wong@redknee.com
 */
public class DummyDepositReleaseTransactionCreator implements DepositReleaseTransactionCreator
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5616101774573389294L;

    /**
     * Map of deposit released. Key is subscriber ID, and value is amount released.
     */
    private Map<String, Long> releasedAmounts_ = new TreeMap<String, Long>();

    /**
     * Map of deposit release date. Key is subscriber ID, and value is date of the transaction.
     */
    private Map<String, Date> billingDates_ = new TreeMap<String, Date>();

    /**
     * {@inheritDoc}
     */
    public void createTransaction(
        final Context context,
        final Subscriber subscriber,
        final long amountReleased,
        final AdjustmentType adjustmentType,
        final Date billingDate)
    {
        releasedAmounts_.put(subscriber.getId(), Long.valueOf(amountReleased));
        billingDates_.put(subscriber.getId(), billingDate);
        new DebugLogMsg(this, "Deposit released: subscriber=" + subscriber.getId() + ", amount=" + amountReleased
            + ", on " + billingDate.toString(), null).log(context);
    }

    /**
     * Returns the map of released amounts.
     *
     * @return the map containing (subscriber ID, amount of deposit released).
     */
    public Map<String, Long> getReleasedAmounts()
    {
        return releasedAmounts_;
    }

    /**
     * Returns the map of billing dates.
     *
     * @return the map containing (subscriber ID, billing date).
     */
    public Map<String, Date> getBillingDates()
    {
        return billingDates_;
    }
}
