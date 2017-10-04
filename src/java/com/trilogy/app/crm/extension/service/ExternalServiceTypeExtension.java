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
package com.trilogy.app.crm.extension.service;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.xenum.AbstractEnum;

import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.extension.DependencyValidatableExtension;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.TypeDependentExtension;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * @author chandrachud.ingale
 * @since  9.7
 * 
 * This extension keeps the info of external service type i.e. service type defined at MNO.
 * 
 */
public class ExternalServiceTypeExtension extends AbstractExternalServiceTypeExtension implements DependencyValidatableExtension,
        TypeDependentExtension
{

    private static final long serialVersionUID = -5126602926019013575L;

    /**
     * 
     */
    public ExternalServiceTypeExtension()
    {}

     
    public void validateDependency(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        // Validate whether or not this extension is allowed to be contained within the parent bean.
        ExtensionAware parentBean = this.getParentBean(ctx);
        if (parentBean instanceof Service)
        {
            Service service = (Service) parentBean;
            if (!(ServiceTypeEnum.GENERIC.equals(service.getType()) || ServiceTypeEnum.EXTERNAL_PRICE_PLAN.equals(service.getType())))
            {
                cise.thrown(new IllegalArgumentException(this.getName(ctx) + " extension only allowed for "
                        + ServiceTypeEnum.GENERIC + " and " + ServiceTypeEnum.EXTERNAL_PRICE_PLAN + " services."));
            }
        }

        cise.throwAll();
    }


    @Override
    public boolean isValidForType(AbstractEnum serviceType)
    {
        return ServiceTypeEnum.GENERIC.equals(serviceType) || ServiceTypeEnum.EXTERNAL_PRICE_PLAN.equals(serviceType);
    }

    
    /**
     * Enhanced to use bean in context if available. {@inheritDoc}
     */
    @Override
    public Service getService(Context ctx)
    {
        Service service = BeanLoaderSupportHelper.get(ctx).getBean(ctx, Service.class);
        if (service != null
                && (AbstractServiceExtension.DEFAULT_SERVICEID == this.getServiceId() || SafetyUtil.safeEquals(
                        service.getID(), this.getServiceId())))
        {
            return service;
        }

        if (AbstractServiceExtension.DEFAULT_SERVICEID == this.getServiceId())
        {
            return null;
        }
        
        try
        {
            return HomeSupportHelper.get(ctx).findBean(ctx, Service.class,
                        new EQ(ServiceXInfo.ID, getServiceId()));
        }
        catch (HomeException e)
        {}

        if (service != null && SafetyUtil.safeEquals(service.getID(), this.getServiceId()))
        {
            return service;
        }

        return null;
    }
}
