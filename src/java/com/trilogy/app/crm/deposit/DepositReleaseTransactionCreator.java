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

import java.io.Serializable;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * A strategy for creating deposit release transaction.
 *
 * @author cindy.wong@redknee.com
 */
public interface DepositReleaseTransactionCreator extends Serializable
{
    /**
     * Creates a deposit release transaction.
     *
     * @param context The operating context
     * @param subscriber The subscriber whose deposit is being released
     * @param amountReleased The amount of deposit released
     * @param adjustmentType The adjustment type for this transaction
     * @param billingDate The effective billing date for this transaction
     * @throws HomeException Thrown if there are problems creating the transaction
     */
    void createTransaction(
        Context context,
        Subscriber subscriber,
        long amountReleased,
        AdjustmentType adjustmentType,
        Date billingDate) throws HomeException;
}
