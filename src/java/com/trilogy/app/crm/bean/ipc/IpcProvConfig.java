package com.trilogy.app.crm.bean.ipc;

import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

public class IpcProvConfig extends AbstractIpcProvConfig 
{
	
	/**
	 * Refer to the old configuration in System Feature Config, if the configuration is not
	 * set in this bean. 
	 * TODO For CRM 8.0 release, delete this overriding method and just use the configuration 
	 * in this bean.   
	 */
	public boolean getIpcgRatePlanAware()
	{
		if (IpcgRatePlanAware_)
		{
			return IpcgRatePlanAware_;
		}
		else
		{
			// Check the System Feature Configuration
			Context context = ContextLocator.locate();
			
			SysFeatureCfg systemFeatureConfig = (SysFeatureCfg) context.get(SysFeatureCfg.class);
			return systemFeatureConfig.getIpcgRatePlanAware();
		}
	}
	
	/**
	 * After updating the property in this bean, set the value of the System Feature Configuration
	 * parameter.  We do this to deprecate the use of the old configuration.
	 * TODO For CRM 8.0 release, delete this overriding method and just use the configuration 
	 * in this bean.   
	 */
	public void setIpcgRatePlanAware(boolean IpcgRatePlanAware)
	throws IllegalArgumentException
	{
		assertBeanNotFrozen();

		assertIpcgRatePlanAware(IpcgRatePlanAware);

		boolean old = IpcgRatePlanAware_;

		IpcgRatePlanAware_ = IpcgRatePlanAware;


		firePropertyChange(IPCGRATEPLANAWARE_PROPERTY, old, IpcgRatePlanAware);

		/* Overwrite System Feature Configuration property to deprecate its use. We don't 
		 * want to be changing parts of the System Feature Configuration often so we will use 
		 * a flag to indicate if this property has been properly deprecated. */ 
		Context context = ContextLocator.locate();
		SysFeatureCfg systemFeatureConfig = (SysFeatureCfg) context.get(SysFeatureCfg.class);
		if (!systemFeatureConfig.getDeprecatedIpcgRatePlanAware())
		{
			systemFeatureConfig.setIpcgRatePlanAware(false);
			systemFeatureConfig.setDeprecatedIpcgRatePlanAware(true);
		}
		
	}

}
