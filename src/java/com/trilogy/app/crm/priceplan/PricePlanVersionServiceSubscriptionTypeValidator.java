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
package com.trilogy.app.crm.priceplan;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageVersionXInfo;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Ensure subscription type of all services matches subscription type of the price plan.
 *
 * @author victor.stratan@redknee.com
 */
public class PricePlanVersionServiceSubscriptionTypeValidator implements Validator
{
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        PricePlanVersion ppv = (PricePlanVersion) obj;
        final CompoundIllegalStateException el = new CompoundIllegalStateException();

        final Home ppHome = (Home) ctx.get(PricePlanHome.class);
        Collection serviceFees = ppv.getServicePackageVersion().getServiceFees().values();
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        try
        {
            boolean priceplanHasPrimaryService = false;
        	PricePlan pricePlan = (PricePlan) ppHome.find(ctx, Long.valueOf(ppv.getId()));
            Iterator it = serviceFees.iterator();
            while (it.hasNext())
            {
                ServiceFee2 serviceFee = (ServiceFee2) it.next();

                try
                {
                    Service service = (Service) serviceHome.find(ctx, Long.valueOf(serviceFee.getServiceId()));
                    
                    if(serviceFee.isPrimary())
                    {
                    	priceplanHasPrimaryService = true;
                    }
                    
                    if (pricePlan.getSubscriptionType() != service.getSubscriptionType())
                    {
                        el.thrown(new IllegalPropertyArgumentException(ServicePackageVersionXInfo.SERVICE_FEES,
                                "Service \"" + service.getID()
                                        + "\" is of different SubscriptionType then the Price Plan."));
                    }
                } catch (HomeException e)
                {
                    el.thrown(new IllegalStateException("Cannot find service " + serviceFee.getServiceId() + ".", e));
                }
            }
            if (PricePlanSubTypeEnum.MRC.equals(pricePlan.getPricePlanSubType()) || PricePlanSubTypeEnum.PICKNPAY.equals(pricePlan.getPricePlanSubType()) )
            {
            	if(!priceplanHasPrimaryService)
            	{
            		el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.ID,
                            "PricePlan \"" + pricePlan.getId()
                                    + "\" of MRC / PickNPay type must have at least one Primary service."));
            	}
            }
            if (PricePlanSubTypeEnum.PAYGO.equals(pricePlan.getPricePlanSubType()) || 
            		PricePlanSubTypeEnum.LIFETIME.equals(pricePlan.getPricePlanSubType()))
            {
            	if(priceplanHasPrimaryService)
            	{
            		el.thrown(new IllegalPropertyArgumentException(PricePlanXInfo.ID,
                            "PricePlan \"" + pricePlan.getId()
                                    + "\" of type " + pricePlan.getPricePlanSubType() + " must not have Primary service."));
            	}
            }
        }
        catch (HomeException e)
        {
            el.thrown(new IllegalStateException("Cannot access " + ServiceHome.class.getSimpleName() + " home!", e));
            new MajorLogMsg(this, "Cannot access " + ServiceHome.class.getSimpleName() + " home!", e).log(ctx);
        }
        finally
        {
            el.throwAll();
        }
    }
}