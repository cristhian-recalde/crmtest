/*
 * Created on Jul 4, 2005
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.filter.Predicate;

/**
 * @author psperneac
 */
public class TracerSupport
{
	public static boolean isTraceEnabled(Context ctx,Object value)
	{
		Object filter=ctx.get(Tracer.T);
		
		try {
			
			if(filter==null || ((Predicate)filter).equals(False.instance()))
			{
				return false;
			}
		}
		catch(ClassCastException ccx )
		{
			return false;
		}
		
		Predicate p=(Predicate) XBeans.getInstanceOf(ctx,filter,Predicate.class);
		if(p==null)
		{
			return false;
		}
		
		return p.f(ctx,value);
	}
	
	public static void trace(Context ctx,String message)
	{
		if(ctx.has(Tracer.class))
		{
			((Tracer)ctx.get(Tracer.class)).trace(ctx,message);
		}
	}
	
	public static void trace(Context ctx,String message,Throwable th)
	{
		if(ctx.has(Tracer.class))
		{
			((Tracer)ctx.get(Tracer.class)).trace(ctx,message,th);
		}
	}
	
}
