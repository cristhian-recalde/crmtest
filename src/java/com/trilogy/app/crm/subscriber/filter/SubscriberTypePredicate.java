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
package com.trilogy.app.crm.subscriber.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;

/**
 * @author jchen
 *
 * Predicat checking Subscriber Type
 * 
 * @deprecated Use {@link #com.redknee.framework.xhome.elang.EQ}
 */
@Deprecated
public class SubscriberTypePredicate implements Predicate 
{

	/**
	 * If subType equals null, we ignore the checking type
	 * @param responsible
	 */
	public SubscriberTypePredicate(SubscriberTypeEnum subType)
	{
		subType_ = subType;
	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context arg0, Object obj)
	{
		if (subType_ == null)
        {
            return true;
        }
		Subscriber sub = (Subscriber)obj;
		return subType_.equals(sub.getSubscriberType());
	}
	
	private SubscriberTypeEnum subType_;
}
