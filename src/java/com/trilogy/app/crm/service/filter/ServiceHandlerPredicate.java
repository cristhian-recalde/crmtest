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
package com.trilogy.app.crm.service.filter;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

/**
 * @author jchen
 */
public class ServiceHandlerPredicate implements Predicate
{
	/**
	 * if handler is null, we skip handler checking
	 * @param handler
	 */
	public ServiceHandlerPredicate(String handler)
	{
		handler_ = handler;
	}
	/* (non-Javadoc)
	 * @see com.redknee.framework.xhome.filter.Predicate#f(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public boolean f(Context ctx, Object obj) throws AbortVisitException 
	{
		if (handler_ == null)
			return true;
		
		Service svb = (Service)obj;
		return handler_.equals(svb.getHandler());
	}
	
	private String handler_ = null;
}
