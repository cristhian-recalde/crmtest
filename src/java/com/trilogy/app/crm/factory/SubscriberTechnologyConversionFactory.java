package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversion;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

public class SubscriberTechnologyConversionFactory implements ContextFactory{

	
    /**
     * {@inheritDoc}
     */
    public Object create(final Context context)
    {
        final SubscriberTechnologyConversion bean = new SubscriberTechnologyConversion();

        if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY))
         		bean.setSubscriberType(SubscriberTypeEnum.PREPAID); 
          else if ( LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY))
        	bean.setSubscriberType(SubscriberTypeEnum.POSTPAID); 

        return bean;
    }

}
