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
 * This class is a strategy for calculating the automatic deposit release based on a percentage and a minimum deposit
 * specified in the criteria. This class is implemented as a singleton.
 *
 * @author cindy.wong@redknee.com
 */
public class PercentageWithMinimumReleaseCalculation
    implements
    ReleaseCalculation
{

    /**
     * How many percents are in 1 (i.e. 100). Declared here to avoid possible typos.
     */
    public static final double PERCENTAGE_BASE = 100.0;

    /**
     * Singleton instance.
     */
    private static final PercentageWithMinimumReleaseCalculation INSTANCE =
        new PercentageWithMinimumReleaseCalculation();

    /**
     * Creates a new instance of <code>PercentageWithMinimumReleaseCalculation</code>.
     */
    protected PercentageWithMinimumReleaseCalculation()
    {
        // create a new instance
    }

    /**
     * Retrieves an instance of this class.
     *
     * @return An instance of <code>PercentageWithMinimumReleaseCalculation</code>.
     */
    public static PercentageWithMinimumReleaseCalculation getInstance()
    {
        return INSTANCE;
    }

    /**
     * Calculates the automatic deposit release for a subscriber based on the deposit release percentage and minimum
     * amount specified in the criteria.
     *
     * @param context The operating context.
     * @param criteria The automatic deposit release criteria used in the calculation.
     * @param subscriber The subscriber whose deposit release is being calculated.
     * @return The amount of deposit being released according to the criteria.
     * @see com.redknee.app.crm.deposit.ReleaseCalculation#calculate(com.redknee.framework.xhome.context.Context,
     *      com.redknee.app.crm.bean.AutoDepositReleaseCriteria, com.redknee.app.crm.bean.Subscriber)
     */
    public final long calculate(
        final Context context,
        final AutoDepositReleaseCriteria criteria,
        final Subscriber subscriber)
    {
        final long deposit = subscriber.getDeposit(context);
        final long minDeposit = criteria.getMinimumDepositReleaseAmount();
        final long released =
            (long) (deposit * criteria.getDepositReleasePercent() / PERCENTAGE_BASE);

        return Math.min(deposit, Math.max(released, minDeposit));
    }
}
