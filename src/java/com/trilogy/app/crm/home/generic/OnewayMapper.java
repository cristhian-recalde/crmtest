package com.trilogy.app.crm.home.generic;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

/*
 * it is a one way mapper, used to map an object to 
 * relational db. 
 * 
 * 
 * 
 */

public interface OnewayMapper 
{
	public void delete(Context ctx, Object obj)
	throws HomeException;
	
	public Object create(Context ctx, Object obj)
	throws HomeException;
	
	public Object update(Context ctx, Object obj)
	throws HomeException; 
}
