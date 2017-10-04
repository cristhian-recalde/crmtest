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
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Sets the Proration and smart suspension to FULL and false respectively
 * if the service is one time. The service can be auxiliary service or regular services
 * @author arturo.medina@redknee.com
 *
 */
public class ServiceOneTimeSettingHome extends HomeProxy
{
    /**
     * Creates a new ServiceOneTimeSettingHome.
     *
     * @param delegate The home to which we delegate.
     */
    public ServiceOneTimeSettingHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx,
            final Object obj)
        throws HomeException
    {
        setService(ctx, obj);
        return super.create(ctx, obj);
    }

    /**
     * Verifies the type of object and applies the attribute setting for the one time service
     * @param ctx the operating context
     * @param obj the object to analyse
     */
    private void setService(final Context ctx, final Object obj)
    {
        if (obj instanceof AuxiliaryService)
        {
            final AuxiliaryService auxSvc = (AuxiliaryService) obj;
            adapt(ctx, auxSvc);
        }
        else
        {
            if (obj instanceof Service)
            {
                final Service svc = (Service) obj;
                adapt(ctx, svc);
            }
        }
    }

    /**
     * Sets the SmartSuspension and ActivationFee to false and full respectively
     * if the service is one time.
     * @param ctx the operating context
     * @param svc the service to change
     */
    private void adapt(final Context ctx, final Service svc)
    {
        if (svc.getChargeScheme() == ServicePeriodEnum.ONE_TIME)
        {
            svc.setSmartSuspension(false);
            svc.setActivationFee(ActivationFeeModeEnum.FULL);
        }
    }

    /**
     * Sets the SmartSuspension and ActivationFee to false and full respectively
     * if the service is one time.
     * @param ctx the operating context
     * @param auxSvc the service to change
     */
    private void adapt(final Context ctx, final AuxiliaryService auxSvc)
    {
        if (auxSvc.getChargingModeType() == ServicePeriodEnum.ONE_TIME)
        {
            auxSvc.setSmartSuspension(false);
            auxSvc.setActivationFee(ActivationFeeModeEnum.FULL);            
            auxSvc.setRecurrenceInterval(0);
            
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj)
        throws HomeException
    {
        setService(ctx, obj);
        return super.store(ctx, obj);
    }

    /**
     * the serial version uid
     */
    private static final long serialVersionUID = 5830553732061466511L;

}
