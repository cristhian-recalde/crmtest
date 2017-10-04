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

import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.ServicePeriodEnum;

/**
 * Sets the Postpaid Subscription Only flag.
 * @author vijay.gote
 * @since 9.6
 *
 */
public class ServiceFieldSettingHome extends HomeProxy
{

    /**
     * Creates a new ServiceOneTimeSettingHome.
     * 
     * @param delegate
     *            The home to which we delegate.
     */
    public ServiceFieldSettingHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        if (obj instanceof Service)
        {
            final Service service = (Service) obj;
            if (ActivationFeeModeEnum.PRORATE.equals(service.getActivationFee()))
            {
                service.setPostpaidSubCreationOnly(true);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Setting the Service : Postpaid Sub creation Only : TRUE");
                }
            }
            
            //SET THE DEFAULT VALUE TO MONTHLY SERVICE
            // IT SHOWS BLANK RECURRENCE INTERVAL IN RATING TAB FOR SERVICES (MONTHLY SERVICES)
            if (service.getChargeScheme() == ServicePeriodEnum.MONTHLY)
            {
            	service.setRecurrenceInterval(1);
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
        if (obj instanceof Service)
        {
            final Service service = (Service) obj;
            if (ActivationFeeModeEnum.PRORATE.equals(service.getActivationFee()))
            {
                service.setPostpaidSubCreationOnly(true);
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "Setting the Service : Postpaid Sub creation Only : TRUE");
                }
            }
        }
        return super.store(ctx, obj);
    }

    /**
     * the serial version uid
     */
    private static final long serialVersionUID = 5830553732061466511L;
}
