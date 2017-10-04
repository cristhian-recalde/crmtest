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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.BillCycleHome;


/**
 * Helper class for bill cycle home.
 *
 * @author lanny.tse@redknee.com
 */
public final class BillingCycleHomeHelper
{

    /**
     * Creates a new <code>BillingCycleHomeHelper</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private BillingCycleHomeHelper()
	{
        // empty
    }


    /**
     * Return a collection of all billing cycles with the provided bill cycle day.
     *
     * @param ctx
     *            The operating context.
     * @param dayOfMonth
     *            Day of month.
     * @return A collection of all billing cycles with the provided bill cycle day.
     * @throws HomeException
     *             Thrown if there are problems looking up the billing cycles.
     */
    public static Collection selectByBillingDayOfMonth(final Context ctx, final int dayOfMonth) throws HomeException
    {

        final Home billCycleHome = (Home) ctx.get(BillCycleHome.class);

		final int billingDayOfMonth = dayOfMonth;

        /*
         * Passing Predicates in select statement is not serializable. Select all and then
         * filter. Serializability is needed, since this class is being used by the
         * Invoice Server.
         */
		
        final Collection ret = billCycleHome.selectAll(ctx);
        final Iterator i = ret.iterator();
		
        final Collection realRet = new ArrayList();
		while (i.hasNext())
		{
            final BillCycle billCycle = (BillCycle) i.next();
			if (billCycle.getDayOfMonth() == billingDayOfMonth)
			{
				realRet.add(billCycle);
			}
		}

		return realRet;

	}

}
