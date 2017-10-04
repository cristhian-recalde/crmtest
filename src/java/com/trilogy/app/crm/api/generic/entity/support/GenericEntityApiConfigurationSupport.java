package com.trilogy.app.crm.api.generic.entity.support;

import com.trilogy.app.crm.api.generic.entity.adapter.GenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.bean.GenericEntityApiConfiguration;
import com.trilogy.app.crm.api.generic.entity.bean.Interceptor;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;

public class GenericEntityApiConfigurationSupport {

	public static GenericEntityAdapter getAdapter(Context ctx, String key)
	{
		Object keyObject = resolveKey(ctx, key);

		if(keyObject == null)
		{
			return null;
		}

		GenericEntityAdapter adapter = (GenericEntityAdapter) ctx.get(keyObject);
		if(adapter == null)
		{
			//TODO sgaidhani : Try to create new instance of adapter.
		}

		return adapter;

	}

	public static Validator getValidator(Context ctx, String key)
	{
		Object keyObject = resolveKey(ctx, key);

		if(keyObject == null)
		{
			return null;
		}

		Validator validator = (Validator) ctx.get(keyObject);
		if(validator == null)
		{
			//TODO sgaidhani : Try to create new instance of validator.
		}

		return validator;

	}

	public static Interceptor getInterceptor(Context ctx, String key)
	{
		Object keyObject = resolveKey(ctx, key);

		if(keyObject == null)
		{
			return null;
		}

		Interceptor interceptor = (Interceptor) ctx.get(keyObject);
		if(interceptor == null)
		{
			//TODO sgaidhani : Try to create new instance of interceptor.
		}

		return interceptor;

	}

	/**
	 * 
	 * This may return null as well as HomeExeption. It is upto the invoker to handle these scenarios.
	 * @param ctx
	 * @param entityName
	 * @return
	 * @throws HomeException
	 */
	public static GenericEntityApiConfiguration getEntityConfiguration(Context ctx, Entity entity) throws  HomeException
	{
		GenericEntityApiConfiguration config  = HomeSupportHelper.get(ctx).findBean(ctx, GenericEntityApiConfiguration.class, entity.getType());
		if(config != null)
		{
			if(!config.getEnabled())
			{
				LogSupport.info(ctx, GenericEntityApiConfigurationSupport.class, config.getEntityName() + " is disabled !");
				return null;
			}

			if(LogSupport.isDebugEnabled(ctx))
			{
				LogSupport.debug(ctx, GenericEntityApiConfigurationSupport.class, "Returning entity : " + config);
			}
		}
		return config;
		
		
	}
	
	private static Object resolveKey(Context ctx, String key) 
	{
		Object contextKey = null;

		if(key != null)
		{
			try
			{
				contextKey = Class.forName(key);
			}catch(ClassNotFoundException cnfe)
			{
				//The class doesn't exists. Try to use the string itself as contextKey
				contextKey = key;
			}
		}

		return contextKey;
	}

}
