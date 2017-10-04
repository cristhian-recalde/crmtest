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
package com.trilogy.app.crm.provision.gateway;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.trilogy.app.crm.bas.recharge.SuspensionSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SuspendedEntity;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.extension.auxiliaryservice.core.CallingGroupAuxSvcExtension;
import com.trilogy.app.crm.extension.auxiliaryservice.core.SPGAuxSvcExtension;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.SuspendedEntitySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Calculates the Block parameter value for the Intervoice service based on Subscription state.
 *
 * @author victor.stratan@redknee.com
 * @since 
 */
public class IntervoiceBlockProvider implements SPGCustomParameterValueProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object provideValue(Context ctx, SPGParameter spgParameter, Subscriber subscriber)
    {

        SubscriberStateEnum state = subscriber.getState();
        
        switch (state.getIndex())
        {
        case SubscriberStateEnum.AVAILABLE_INDEX:
        case SubscriberStateEnum.LOCKED_INDEX:
        case SubscriberStateEnum.SUSPENDED_INDEX:
        case SubscriberStateEnum.DORMANT_INDEX:
        case SubscriberStateEnum.INACTIVE_INDEX:
            return "YES";
        }
        if ( shouldSuspendPrepaid(ctx, subscriber, spgParameter))
        {
            return "YES";
        }
        return "NO";
    }


    private boolean shouldSuspendPrepaid(Context ctx, Subscriber subscriber, SPGParameter spgParameter)
    {
        Object unsuspendSvc= ctx.get(SuspensionSupport.UNSUSPEND_GATEWAY_SERVICE);
        if (unsuspendSvc == null && subscriber.isPrepaid())
        {
            if (subscriber.getState() == SubscriberStateEnum.ACTIVE)
            {
                Object svc = ctx.get(SuspensionSupport.SUSPEND_GATEWAY_SERVICE);
                if (svc != null)
                {
                    if (svc instanceof Service)
                    {
                        Service service = (Service) svc;
                        if (service.getType() == ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY
                                && service.getSPGServiceType() == spgParameter.getServiceId())
                        {
                            return true;
                        }
                    }
                    if (svc instanceof AuxiliaryService)
                    {
                        AuxiliaryService service = (AuxiliaryService) svc;
                        if (service.getType().equals(AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY))
                        {
                            SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, SPGAuxSvcExtension.class);
                            if (spgAuxSvcExtension!=null && spgAuxSvcExtension.getSPGServiceType() == spgParameter.getServiceId())
                            {
                                return true;
                            }
                            else if (spgAuxSvcExtension==null)
                            {
                                LogSupport.minor(ctx, this,
                                        "Unable to find required extension of type '" + SPGAuxSvcExtension.class.getSimpleName()
                                                + "' for auxiliary service " + service.getIdentifier());
                            }
                        }
                    }
                }
                else
                {
                    Collection<SuspendedEntity> suspendEntities = SuspendedEntitySupport.getSuspendedEntities(ctx,
                            subscriber.getId(), null);
                    if (suspendEntities.size() > 0)
                    {
                        HashSet<Long> suspendAuxServiceSet = new HashSet<Long>();
                        HashSet<Long> suspendSubServiceSet = new HashSet<Long>();
                        for (SuspendedEntity suspendEntity : suspendEntities)
                        {
                            if (suspendEntity.getType().equals(AuxiliaryService.class.getName()))
                            {
                                suspendAuxServiceSet.add(Long.valueOf(suspendEntity.getIdentifier()));
                            }
                            else if (suspendEntity.getType().equals(ServiceFee2.class.getName()))
                            {
                                suspendSubServiceSet.add(Long.valueOf(suspendEntity.getIdentifier()));
                            }
                        }
                        List<SubscriberAuxiliaryService> subAuxServices = subscriber.getAuxiliaryServices(ctx);
                        for (SubscriberAuxiliaryService subAuxService : subAuxServices)
                        {
                            try
                            {
                                AuxiliaryService auxService = subAuxService.getAuxiliaryService(ctx);
                                if (auxService.getType().equals(AuxiliaryServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY))
                                {
                                    SPGAuxSvcExtension spgAuxSvcExtension = ExtensionSupportHelper.get(ctx)
                                            .getExtension(ctx, auxService, SPGAuxSvcExtension.class);
                                    if (spgAuxSvcExtension != null
                                            && spgAuxSvcExtension.getSPGServiceType() == spgParameter.getServiceId()
                                            && suspendSubServiceSet.contains(Long.valueOf(auxService.getIdentifier())))
                                    {
                                        return true;
                                    }
                                }
                            }
                            catch (HomeException homeEx)
                            {
                                new MinorLogMsg(this, "Unable to load the auxiliary service "
                                        + subAuxService.getAuxiliaryServiceIdentifier(), homeEx).log(ctx);
                            }
                        }
                        Set<SubscriberServices> subServices = subscriber.getIntentToProvisionServices(ctx);
                        for (SubscriberServices subService : subServices)
                        {
                            Service service = subService.getService(ctx);
                            if (service.getType().equals(ServiceTypeEnum.SERVICE_PROVISIONING_GATEWAY)
                                    && spgParameter.getServiceId() == service.getSPGServiceType())
                            {
                                if (suspendSubServiceSet.contains(Long.valueOf(service.getIdentifier())))
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
