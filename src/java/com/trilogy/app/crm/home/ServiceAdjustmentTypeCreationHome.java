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

import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.home.pipelineFactory.AdjustmentTypeHomePipelineFactory;
import com.trilogy.app.crm.support.ServiceSupport;


/**
 * Creates new adjustment types for new services, updates and look-ups existing adjustment
 * types for existing services.
 * 
 * @author jimmy.ng@redknee.com
 */
public class ServiceAdjustmentTypeCreationHome extends HomeProxy
{

    /**
     * Creates a new ServiceAdjustmentTypeCreationHome.
     * 
     * @param delegate The home to which we delegate.
     */
    public ServiceAdjustmentTypeCreationHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * Creates a new ServiceAdjustmentTypeCreationHome.
     * 
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public ServiceAdjustmentTypeCreationHome(final Context context, final Home delegate)
    {
        super(delegate);
        setContext(context);
    }


    // INHERIT
    @Override
    public Object find(Context ctx, Object key) throws HomeException
    {
        // TODO 2008-09-18 this is weird and probably not necesary.
        if (key instanceof Number)
        {
            key = Long.valueOf(((Number) key).longValue());
        }
        final Service service = (Service) super.find(ctx, key);

        if (service != null)
        {
            updateAdjustmentTypeInformation(ctx, service);
        }

        return service;
    }


    // INHERIT
    @Override
    public Object create(Context ctx, final Object bean) throws HomeException
    {
        Service service = (Service) super.create(ctx, bean);

        try
        {
            boolean update = false;
            if (service.getAdjustmentType() == -1)
            {
                // only if really new service created
                final AdjustmentType type = createAdjustmentType(ctx, service);

                service.setAdjustmentGLCode(type.getGLCodeForSPID(ctx, service.getSpid()));
                service.setAdjustmentType(type.getCode());
                service.setAdjustmentTypeName(type.getName()); // Just for display purpose
                update = true;
            }
                        
            if (update)
            {
                service = (Service) super.store(ctx, service);
            }
        }
        catch (final HomeException exception)
        {
            // If an exception occurs, then we want to act as if the service
            // creation failed, so we must attempt to remove it.  We can ignore
            // any complaints during the removal.
            try
            {
                super.remove(ctx, service);
            }
            catch (final Throwable throwable)
            {
                new MinorLogMsg(this, "Failed to remove defunct service from database.", throwable).log(ctx);
            }

            throw exception;
        }

        return service;
    }


    // INHERIT
    @Override
    public Object store(Context ctx, final Object bean) throws HomeException
    {
        final Service service = (Service) bean;

        if (service != null)
        {
            storeAdjustmentType(ctx, service);
        }

        return super.store(ctx, service);
    }


    /**
     * Creates and returns a new AdjustmentType for the given Service.
     * 
     * @param service The Service for which to create a new AdjustmentType.
     * 
     * @return A new AdjustmentType.
     */
    private AdjustmentType createAdjustmentType(final Context ctx, final Service service) throws HomeException
    {
        return ServiceSupport.createAdjustmentType(ctx, service);
    }


    /**
     * Stores an AdjustmentType for the given Service.
     * 
     * @param service The Service for which to update an AdjustmentType.
     */
    private void storeAdjustmentType(Context ctx, final Service service) throws HomeException
    {
        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        final AdjustmentType type = (AdjustmentType) home.find(ctx, Integer.valueOf(service.getAdjustmentType()));

        if (type == null)
        {
            throw new HomeException("Unable to locate AdjustmentType [" + service.getAdjustmentType() + "]");
        }
        type.setDesc(service.getAdjustmentTypeDesc());
        type.setLoyaltyEligible(true);
        
        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(service.getSpid());
        AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information == null)
        {
            information = new AdjustmentInfo();
            spidInformation.put(key, information);
        }

        information.setSpid(service.getSpid());
        information.setGLCode(service.getAdjustmentGLCode());
        information.setInvoiceDesc(service.getAdjustmentInvoiceDesc());
        information.setTaxAuthority(service.getTaxAuthority());

        home.store(ctx, type);
    }



    /**
     * Updates the given Service with it's AdjustmenType information.
     * 
     * @param service The Service for which to fill in AdjustmentType details.
     */
    private void updateAdjustmentTypeInformation(final Context ctx, final Service service) throws HomeException
    {
        final Home home = (Home) ctx.get(AdjustmentTypeHomePipelineFactory.ADJUSTMENT_TYPE_SYSTEM_HOME);
        final AdjustmentType type = (AdjustmentType) home.find(ctx, Integer.valueOf(service.getAdjustmentType()));

        if (type == null)
        {
            return;
        }

        service.setAdjustmentTypeName(type.getName());
        service.setAdjustmentTypeDesc(type.getDesc());

        final Map spidInformation = type.getAdjustmentSpidInfo();
        final Object key = Integer.valueOf(service.getSpid());
        final AdjustmentInfo information = (AdjustmentInfo) spidInformation.get(key);

        if (information != null)
        {
            service.setAdjustmentGLCode(information.getGLCode());
            service.setAdjustmentInvoiceDesc(information.getInvoiceDesc());
            service.setTaxAuthority(information.getTaxAuthority());
        }
    }

}
