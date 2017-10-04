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

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.AutoDepositReleaseCriteria;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * An interface for the strategy for calculating the automatic deposit release of a subscriber.
 *
 * @author cindy.wong@redknee.com
 */
public interface ReleaseCalculation
{
    /**
     * Calculates the automatic deposit release for a particular subscriber using the specified criteria.
     *
     * @param context The operating context
     * @param criteria The automatic deposit release criteria to apply upon the subscriber
     * @param subscriber The subscriber whose deposit is being automatically released
     * @return The amount of deposit being released according to the criteria
     */
    long calculate(
        Context context,
        AutoDepositReleaseCriteria criteria,
        Subscriber subscriber);
}
