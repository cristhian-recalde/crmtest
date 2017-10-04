package com.trilogy.app.crm.api.generic.entity.bean;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

public interface Interceptor {

	public abstract Home getHome(Context ctx, Object obj);
	
	/**
	 * Before actually performing a home operation, this method can do pre-processing on the bean/home.
	 * @param ctx
	 * @param obj - This would be a single bean for CRUD operations.
	 * @return
	 */
	public abstract void preProcess(Context ctx, Object obj);
	
	/**
	 * After performing a home operation, this method can do post-processing on the bean/home.
	 * @param ctx
	 * @param obj - This could be a single bean for create/update operations. 
	 * Null for delete operation. 
	 * Collection for retrieve operation.
	 * @return
	 */
	public abstract void postProcess(Context ctx, Object obj);

}
