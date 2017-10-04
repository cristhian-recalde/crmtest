/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
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
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.extension.DependencyValidatableExtension;
import com.trilogy.app.crm.extension.ExtendedAssociableExtension;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.extension.ExtensionAware;
import com.trilogy.app.crm.extension.TypeDependentExtension;
import com.trilogy.app.crm.ff.BlacklistWhitelistPLPSupport;
import com.trilogy.app.crm.ff.FFEcareException;
import com.trilogy.app.crm.support.BeanLoaderSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;

/**
 * Extension that contains Blacklist Whitelist template service extension
 * 
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlacklistWhitelistTemplateServiceExtension extends AbstractBlacklistWhitelistTemplateServiceExtension
        implements DependencyValidatableExtension, TypeDependentExtension,
        ExtendedAssociableExtension<SubscriberServices>

{
    private static final long serialVersionUID = -4305713844260235844L;


    public BlacklistWhitelistTemplateServiceExtension()
    {
        super();
    }


    public void validateDependency(Context ctx) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        // Validate whether or not this extension is allowed to be contained within the parent bean.
        ExtensionAware parentBean = this.getParentBean(ctx);
        if (parentBean instanceof Service)
        {
            Service service = (Service) parentBean;
            if (!ServiceTypeEnum.GENERIC.equals(service.getType()))
            {
                cise.thrown(new IllegalArgumentException(this.getName(ctx) + " extension only allowed for "
                        + ServiceTypeEnum.GENERIC + " services."));
            }
        }

        cise.throwAll();
    }


    @Override
    public boolean isValidForType(AbstractEnum serviceType)
    {
        return ServiceTypeEnum.GENERIC.equals(serviceType);
    }

    /**
     * {@inheritDoc}
     */
    public void associate(Context context, SubscriberServices subscriberServices) throws ExtensionAssociationException
    {
        if (subscriberServices.getProvisionedState().getIndex() == ServiceStateEnum.PROVISIONED_INDEX)
        {
            try
            {
                BlacklistWhitelistPLPSupport.addPLP(context, subscriberServices, getCallingGroupId(), getGlCode());
                subscriberServices.setProvisionActionState(true);
            }
            catch (FFEcareException e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
            }
            catch (Exception e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(),
                        ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void updateAssociation(Context context, SubscriberServices subscriberServices)
            throws ExtensionAssociationException
    {
        SubscriberServices oldSubscriberServices = SubscriberServicesSupport
                .getSubscriberServiceRecordWithLookupinCacheFirst(context, subscriberServices.getSubscriberId(),
                        subscriberServices.getServiceId(), subscriberServices.getPath());

        if (subscriberServices.getProvisionedState().getIndex() == ServiceStateEnum.PROVISIONED_INDEX
                && oldSubscriberServices.getProvisionedState().getIndex() != ServiceStateEnum.PROVISIONED_INDEX)
        {
            try
            {
                BlacklistWhitelistPLPSupport.addPLP(context, subscriberServices, this.getCallingGroupId(), getGlCode());
                subscriberServices.setProvisionActionState(true);
            }
            catch (FFEcareException e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
            }
            catch (Exception e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(),
                        ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void dissociate(Context context, SubscriberServices subscriberServices) throws ExtensionAssociationException
    {
        if (subscriberServices.getProvisionedState().getIndex() == ServiceStateEnum.UNPROVISIONED_INDEX)
        {
            try
            {
                BlacklistWhitelistPLPSupport.deletePLP(context, subscriberServices, this.getCallingGroupId());
                subscriberServices.setProvisionActionState(true);
            }
            catch (FFEcareException e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.FF, e.getMessage(), e.getResultCode(), e);
            }
            catch (Exception e)
            {
                subscriberServices.setProvisionActionState(false);
                throw new ExtensionAssociationException(ExternalAppEnum.BSS, e.getMessage(),
                        ExternalAppSupport.BSS_DATABASE_FAILURE_SUBSCRIPTION_RETRIEVAL, e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void postExternalBeanCreation(Context paramContext, SubscriberServices paramT, boolean paramBoolean)
            throws ExtensionAssociationException
    {}


    /**
     * {@inheritDoc}
     */
    public void postExternalBeanUpdate(Context paramContext, SubscriberServices paramT, boolean paramBoolean)
            throws ExtensionAssociationException
    {}


    /**
     * {@inheritDoc}
     */
    public void postExternalBeanRemoval(Context paramContext, SubscriberServices paramT, boolean paramBoolean)
            throws ExtensionAssociationException
    {}

    
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
