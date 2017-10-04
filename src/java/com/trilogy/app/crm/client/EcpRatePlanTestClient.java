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
package com.trilogy.app.crm.client;

import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.service.rating.RatePlanInfo;


/**
 * Provides test client for communicating with ECP to retrieve the list of
 * available rate plans.  This class always returns the same list of available
 * rate plans.
 *
 * @author gary.anderson@redknee.com
 */
public
class EcpRatePlanTestClient
    implements EcpRatePlanClient
{
    /**
     * {@inheritDoc}
     */
    public RatePlanInfo[] getRatePlans(final int spid)
        throws EcpRatePlanClientException
    {
        final RatePlanInfo[] plans = new RatePlanInfo[NUMBER_OF_RATE_PLANS];

        for (int n = 0; n < NUMBER_OF_RATE_PLANS; ++n)
        {
            final RatePlanInfo plan = new RatePlanInfo();
            plan.ratePlanId = "XX" + n;
            plan.spId = spid;
            plan.desc = "Test client rate plan #" + n;

            plans[n] = plan;
        }

        return plans;
    }


    /**
     * {@inheritDoc}
     */
    public void setContext(final Context context)
    {
        // Empty.
    }


    /**
     * {@inheritDoc}
     */
    public Context getContext()
    {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "EcpRatePlanClient(Test)";
    }


    /**
     * {@inheritDoc}
     */
    public String getDescription()
    {
        return "For testing retrieval of all the rate plans from ECP.";
    }


    
    /**
     * {@inheritDoc}
     */
    public boolean isAlive()
    {
        return true;
    }


    /**
     * {@inheritDoc}
     */    public void connectionUp()
    {
        // Empty.
    }


    /**
     * {@inheritDoc}
     */
    public void connectionDown()
    {
        // Empty.
    }


    private static final int NUMBER_OF_RATE_PLANS = 5;


    @Override
    public ConnectionStatus[] getConnectionStatus()
    {
        return SystemStatusSupportHelper.get().generateConnectionStatus("Test Client -- No remote information available.", isAlive());
    }


    @Override
    public String getServiceStatus()
    {
        return SystemStatusSupportHelper.get().generateServiceStatusString(isAlive());
    }
}
