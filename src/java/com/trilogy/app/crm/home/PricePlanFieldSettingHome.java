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

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Sets PricePlanSubType field of Postpaid priceplan to -1.
 * @author vijay.gote
 * @since 9.9
 *
 */
public class PricePlanFieldSettingHome extends HomeProxy
{

    /**
     * Creates a new ServiceOneTimeSettingHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public PricePlanFieldSettingHome(Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof PricePlan)
        {
            final PricePlan pricePlan = (PricePlan) obj;
            if (SubscriberTypeEnum.POSTPAID.equals(pricePlan.getPricePlanType()))
            {
                pricePlan.setPricePlanSubType(PricePlanSubTypeEnum.NA);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Setting PricePlanSubType of Postpaid priceplan as -1");
                }
            }
        }
        return super.create(ctx, obj);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	if (obj instanceof PricePlan)
        {
            final PricePlan pricePlan = (PricePlan) obj;
            if (SubscriberTypeEnum.POSTPAID.equals(pricePlan.getPricePlanType()))
            {
                pricePlan.setPricePlanSubType(PricePlanSubTypeEnum.NA);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Setting PricePlanSubType of Postpaid priceplan as -1");
                }
            }
        }
        return super.store(ctx, obj);
    }

    /**
     * the serial version uid
     */
    private static final long serialVersionUID = 5830553732061466512L;
}
