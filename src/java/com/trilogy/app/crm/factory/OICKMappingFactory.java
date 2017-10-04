package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.OICKMapping;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

public class OICKMappingFactory implements ContextFactory
{
 
	public OICKMappingFactory()
    {
      
    }

    /**
     * {@inheritDoc}
     */
    public Object create(final Context context)
    {
        final OICKMapping bean = new OICKMapping();

        if ( LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY))
   		bean.setSubscriberType(SubscriberTypeEnum.PREPAID); 
        else if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY))
        	bean.setSubscriberType(SubscriberTypeEnum.POSTPAID); 
        	
        return bean;
    }

} // class
