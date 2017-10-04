package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

public class ProvisionCommandFactory implements ContextFactory
{
 
	public ProvisionCommandFactory()
    {
      
    }

    /**
     * {@inheritDoc}
     */
    public Object create(final Context context)
    {
        final ProvisionCommand bean = new ProvisionCommand();

        if ( LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY))
        	bean.setType(SubscriberTypeEnum.POSTPAID); 
        else if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY))
        	bean.setType(SubscriberTypeEnum.PREPAID); 
        	
        return bean;
    }

} // class

