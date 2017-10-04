package com.trilogy.app.crm.factory;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.AccountHistory;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextFactory;

public class AccountHistoryFactory implements ContextFactory
{
 
	public AccountHistoryFactory()
    {
      
    }

    /**
     * {@inheritDoc}
     */
    public Object create(final Context context)
    {
        final AccountHistory bean = new AccountHistory();

        if ( LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.POSTPAID_LICENSE_KEY))
        	bean.setSubscriberType(SubscriberTypeEnum.POSTPAID); 
        else if (LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.PREPAID_LICENSE_KEY))
        	bean.setSubscriberType(SubscriberTypeEnum.PREPAID); 
        	
        return bean;
    }

} // class
