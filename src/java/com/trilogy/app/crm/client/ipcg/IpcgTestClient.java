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
package com.trilogy.app.crm.client.ipcg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s5600.ipcg.rating.provisioning.RatePlan;
import com.trilogy.product.s5600.iprc.rating.provisioning.IprcRatePlan;


/**
 * Provides a simple test class for the IpcgClient.
 *
 * @author gary.anderson@redknee.com
 */
public
class IpcgTestClient
    implements IpcgClient
{
    /**
     * {@inheritDoc}
     */
    public boolean resetBalance(final Context context, final Subscriber subscriber)
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public RatePlan[] getAllRatePlans(final Context context)
        throws IpcgRatingProvException
    {
        final int numberOfPlans = 4;

        final RatePlan[] plans = new RatePlan[numberOfPlans];

        for (int n = 0; n < numberOfPlans; ++n)
        {
            plans[n] = new RatePlan();
            plans[n].spId = 1;
            plans[n].rpId = n;
            plans[n].description = "Testing Rate Plan #" + n;
        }

        return plans;
    }
    
    /**
     * {@inheritDoc}
     */
    public IprcRatePlan[] getAllRatePlans(final Context context, final int spid)
        throws IpcgRatingProvException
    {
        final int numberOfPlans = 4;

        final IprcRatePlan[] plans = new IprcRatePlan[numberOfPlans];

        for (int n = 0; n < numberOfPlans; ++n)
        {
            plans[n] = new IprcRatePlan();
            plans[n].spId = 1;
            plans[n].rpId = n;
            plans[n].description = "Testing Rate Plan #" + n;
        }

        return plans;
    }


    /**
     * {@inheritDoc}
     */
    public int addSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final String timeZone,
        final int ratePlan,
        final int scpId,
        final boolean subBasedRatingEnabled,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSub(
        final Context context,
        final Subscriber subscriber,
        final short billingCycleDate,
        final int ratePlan,
        final int serviceGrade)
        throws IpcgSubProvException
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    public int addChangeSubBillCycleDate(
        final Context context,
        final Subscriber subscriber,
        final short billCycleDate)
        throws IpcgSubProvException
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    public int setSubscriberEnabled(
        final Context context,
        final Subscriber subscriber,
        final boolean enabled)
        throws IpcgSubProvException
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isSubscriberProfileAvailable(
        final Context context,
        final Subscriber subscriber)
        throws IpcgSubProvException
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    public int removeSubscriber(final Context context, final Subscriber subscriber)
        throws IpcgSubProvException
    {
        return 0;
    }


} // class
