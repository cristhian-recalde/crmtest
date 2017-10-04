/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home;

import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.PricePlanSupport;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 *
 *	Validate if PP Version has a single Primary Service.
 *
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 * 
 *  @author : abhay.kumar@redknee.com	
 *  Another validation : primary service must be mandatory 
 */

public class PricePlanVersionPrimaryServiceValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
    	boolean primaryServiceExists = false;
        final PricePlanVersion ppv = (PricePlanVersion) obj;
        Map serviceMap = ppv.getServicePackageVersion().getServiceFees();
        
        if(serviceMap.isEmpty())
        {
        	return;
        }
        
        PricePlan pricePlan = null;
        try
        {
            pricePlan = PricePlanSupport.getPlan(ctx, ppv.getId());
        }
        catch (HomeException e)
        {
            String msg = "Exception occured when trying to retrieve the Price Plan for Price Plan Version: " + ppv.getId() + ". " + e.getMessage();
            LogSupport.minor(ctx, this, msg, e);
            throw new IllegalStateException (msg);
        }

        
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        
        Iterator servicesIterator = serviceMap.entrySet().iterator();

        while (servicesIterator.hasNext())
        {
        	Map.Entry entry = (Map.Entry)servicesIterator.next();
        	ServiceFee2 fee = (ServiceFee2)entry.getValue();
        	if(!fee.isPrimary())
        	{
        		continue;
        	}
        	
        	if(!fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) && !(pricePlan.getPricePlanSubType().getName().equals(PricePlanSubTypeEnum.PICKNPAY.getName())))
        	{
        		 final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                         ServicePackageVersionXInfo.SERVICE_FEES, "Primary Service must be mandatory!!");
         		exceptions.thrown(ex); 
        	}
        		
        		
        	if(primaryServiceExists && !(pricePlan.getPricePlanSubType().getName().equals(PricePlanSubTypeEnum.PICKNPAY.getName())) )
        	{
                final IllegalPropertyArgumentException ex = new IllegalPropertyArgumentException(
                        ServicePackageVersionXInfo.SERVICE_FEES, "Only one service can be a Primary Service.");
        		exceptions.thrown(ex);
        	}
        	else 
        	{
        		primaryServiceExists = true;
        	}
        	
        }
        exceptions.throwAllAsCompoundException();
    }
}



