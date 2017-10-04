package com.trilogy.app.crm;

import com.trilogy.framework.xhome.context.Context;


/**
 * this helper class will return reference of context  
 * to these who need query but don't want to keep its 
 * own context. the context was set in storageInstall. 
 * 
 * 
 * @author lxia
 *
 */
public class ContextHelper 
{
	static public Context getContext()
	{
		return context_; 
	}
	
	static public synchronized void setContext(Context ctx)
	{
		if (context_ == null)
		{
			context_ = ctx.createSubContext(); 
		}
	}

	
	static Context context_; 
}
