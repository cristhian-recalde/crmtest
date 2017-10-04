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

import com.trilogy.framework.xhome.beans.Child;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;

/**
 * @author jchen
 *
 *  Uses to Predicate Parent Object, the Object bean passed in
 *  need to implement Child interface.
 */
final public class ParentPredicate implements Predicate 
{	
	
	public ParentPredicate(Object parent)
	{
		parent_ = parent;
	}
    
	/**
	 * 
	 * @param context
	 * @param bean
	 * @return
	 */
	public boolean f(Context context, Object bean)
    {
       Child child = (Child)bean;
       return SafetyUtil.safeEquals(child.getParent(), parent_);
    }
    
    private Object parent_ = null; 
}
