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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.framework.xhome.context.Context;


/**
 * Make use of reusable elements to an abstract class AbstractCrmClient<T>
 * @author victor.stratan@redknee.com
 * @since CRM 8.2.2
 */
public interface BGroupPricePlanClient 
{
    /**
     *  Create, update an entry in the pricePlan to ratePlan mapping table.
     */
    void setBusinessGroupPricePlan(final Context ctx, final int spid, final String businessGroupID,
            final long pricePlanID) throws BGroupPricePlanException;

    /**
     * Query existing business group price plan mapping. Mappings are segmented by Service Provider.
     */
    long getBusinessGroupPricePlan(final Context ctx, final int spid, final String businessGroupID)
        throws BGroupPricePlanException;
}
