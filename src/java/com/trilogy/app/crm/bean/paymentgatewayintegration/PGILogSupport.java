package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class PGILogSupport {
	
	private PGILogSupport() {}

	public static void logApiStart(Context ctx , Class clazz , String api , String requestParams)
	{
		if( LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, clazz.getName(), "API [" + api + "] : START. Request Parameters: " + requestParams);
		}
	}
	
	public static void logApiInExecution(Context ctx, Class clazz , String api , String message)
	{
		if(LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, clazz, "API [" + api + "] : IN EXECUTION. Message: " + message);
		}
	}
	
	public static void logApiEnd(Context ctx , Class clazz , String api , int code , String responseParams)
	{
		if( LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, clazz.getName(), "API [" + api + "] : END. StatusCode : " + code + " Response Parameters: " + responseParams);
		}
	}	
	
	public static void logApiEnd(Context ctx , Class clazz , String api , Long code , String responseParams)
	{
		if( LogSupport.isDebugEnabled(ctx))
		{
			LogSupport.debug(ctx, clazz.getName(), "API [" + api + "] : END. StatusCode : " + code + " Response Parameters: " + responseParams);
		}
	}	
}
