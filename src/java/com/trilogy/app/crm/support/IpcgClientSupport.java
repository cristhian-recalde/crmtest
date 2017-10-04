package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.bean.ipc.RateRetrievalMethodEnum;
import com.trilogy.framework.xhome.context.Context;

public class IpcgClientSupport 
{
	/**
	 * Returns True if the IPC Client configuration is set to retrieve
	 * all existing Rate Plans (no criteria). Returns False, otherwise.
	 * @param context
	 * @return
	 */
	public static boolean supportsRetrieveAllRatePlans(Context context)
	{
		IpcProvConfig config = (IpcProvConfig) context.get(IpcProvConfig.class);
		return config.getRetrievalMethod().equals(RateRetrievalMethodEnum.RETRIEVEALLRATEPLANS);
	}
	
	/**
	 * Returns True, if the IPC Client configuration is set to retrieve 
	 * Rate Plans by the Service Provider criteria.  Returns False, otherwise.
	 * @param context
	 * @return
	 */
	public static boolean supportsQueryRatePlans(Context context)
	{
		IpcProvConfig config = (IpcProvConfig) context.get(IpcProvConfig.class);
		return config.getRetrievalMethod().equals(RateRetrievalMethodEnum.QUERYRATEPLANS);
	}
	
	/**
	 * Returns True, if CRM is configured to retrieve Rate Plans from URCS Data
	 * for price plan configuration.
	 * @param context
	 * @return
	 */
	public static boolean pricePlanSupportsUrcsDataRatePlan(Context context)
	{
		IpcProvConfig config = (IpcProvConfig) context.get(IpcProvConfig.class);
		return config.isIpcgRatePlanAware();
	}
	
	/**
     * Returns True, if CRM is configured allow mapping of Rate Plans to Price-Plan
     * for price plan configuration.
     * @param context
     * @return
     */
    public static boolean supportsRateToPricePlanMapping(Context context)
    {
        IpcProvConfig config = (IpcProvConfig) context.get(IpcProvConfig.class);
        return config.isIpcgRatePlanAware();
    }
}
