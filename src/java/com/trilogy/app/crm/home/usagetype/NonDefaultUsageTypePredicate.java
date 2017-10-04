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

package com.trilogy.app.crm.home.usagetype;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.UsageType;
import com.trilogy.app.crm.home.UsageTypePreventDefaultItemDeleteHome;

/**
 * Check if the ID is different from the default ID.
 * @author victor.stratan@redknee.com
 */
public class NonDefaultUsageTypePredicate implements Predicate
{
    private static final long serialVersionUID = 12L;

    /**
     * Check if the ID is different from the default ID.
     * @param ctx the operating context
     * @param obj the object to run the predicate on
	 * @return true if not default ID.
	 */
	public boolean f(final Context ctx, final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else if (obj instanceof UsageType)
		{
			return ((UsageType) obj).getId() != UsageTypePreventDefaultItemDeleteHome.DEFAULT;
		}
		else
		{
			return false;
		}
	}
}
