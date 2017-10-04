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
package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.ConvertSubscriptionBillingTypeRequest;
import com.trilogy.app.crm.move.support.SubscriptionMoveValidationSupport;
import com.trilogy.app.crm.provision.IPCProvisionAgent;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Responsible for moving services in conversion specific call-flows, since it differs from
 * regular move call flows.  To see the differences, refer to ServiceCopyMoveStrategy.
 * 
 * It performs validation required to complete its task successfully.
 *
 * @author Aaron Gourley
 * @since 8.2
 */
public class ConversionServiceCopyMoveStrategy<CSBTR extends ConvertSubscriptionBillingTypeRequest> extends CopyMoveStrategyProxy<CSBTR>
{
    public ConversionServiceCopyMoveStrategy(CopyMoveStrategy<CSBTR> delegate)
    {
        super(delegate);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void initialize(Context ctx, CSBTR request)
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        if (oldSubscription != null)
        {
            Collection<SubscriberServices> currentServices = 
                SubscriberServicesSupport.getProvisionedSubscriberServices(ctx, oldSubscription.getId());
            for (SubscriberServices subService : currentServices)
            {
                Service service = subService.getService(ctx);
                if (service != null)
                {
                    services_.add(subService);
                    serviceIds_.add(subService.getServiceId());
                }
            }
        }
        
        super.initialize(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void validate(Context ctx, CSBTR request) throws IllegalStateException
    {
        CompoundIllegalStateException cise = new CompoundIllegalStateException();

        SubscriptionMoveValidationSupport.validateNewSubscriptionExists(ctx, request, cise);

        SubscriptionMoveValidationSupport.validateOldSubscriptionExists(ctx, request, cise);
        
        cise.throwAll();

        // Don't bother letting the delegate validate unless this processor has passed.
        super.validate(ctx, request);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void createNewEntity(Context ctx, CSBTR request) throws MoveException
    {
        Subscriber oldSubscription = request.getOldSubscription(ctx);
        Subscriber newSubscription = request.getNewSubscription(ctx);
        
        try
        {
            //Unprovision all the services from the old subscription
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Unprovision services entries on conversion from subscription "
                        + oldSubscription.getId() + " to " + newSubscription.getId() + "...", null).log(ctx);
            }

            final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
            SubscriberServicesSupport.unprovisionSubscriberServices(ctx, oldSubscription, services_, oldSubscription,
                    resultCodes);

            new InfoLogMsg(this, "Unprovision service entries on conversion successfully from old subscription "
                    + oldSubscription.getId() + ".", null).log(ctx);
        }
        catch (HomeException e)
        {
            throw new MoveException(request, "Error occured while trying to unprovision services "
                    + " from subscriber " + oldSubscription.getId(), e);
        }
        
        super.createNewEntity(ctx, request);
        
        List<SubscriberServices> provServices  = getSubscriptionServicesOnBillingTypeConversion(ctx, newSubscription);
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Provisioning services entries on conversion for the new subscription "
                        + newSubscription.getId() + "...", null).log(ctx);
            }

            
            // Sync the services set in the subscription as well
            Set<Long> serviceIds = new HashSet<Long>();
            Set<SubscriberServices> provServiceSet = new HashSet<SubscriberServices>();
            for (SubscriberServices subService : provServices)
            {
                serviceIds.add(subService.getServiceId());
                provServiceSet.add(subService);
            }
            // TT#13112159022
            ctx.put(IPCProvisionAgent.SKIP_DATA_SVC_CHECK, Boolean.TRUE);
            
            newSubscription.setIntentToProvisionServices(provServiceSet);
            
            final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
            SubscriberServicesSupport.provisionSubscriberServices(ctx, newSubscription, provServices, newSubscription,
                    resultCodes);


            newSubscription.setServices(serviceIds);
            
            
            new InfoLogMsg(this, "Provisioned service entries on conversion successfully for the subscription "
                    + newSubscription.getId(), null).log(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, "Error occurred while trying to provision services on conversion for new "
                    + "subscription " + newSubscription.getId(), e).log(ctx);
        }
    }
    
    
    /**
     * Obtain all the services that needs to be provisioned.  
     * All services that was provisioned in the old subscription and all  manadatory and default services
     * on the price plan will need to be returned
     * @param ctx
     * @param newSub
     * @param currentlyProvisionedServices
     * @return
     */
    private List<SubscriberServices> getSubscriptionServicesOnBillingTypeConversion(final Context ctx, Subscriber newSub)
    {
        final Map<ServiceFee2ID, ServiceFee2> serviceFees = SubscriberServicesSupport.getServiceFees(ctx, newSub);
        
        List<SubscriberServices> provServices = new ArrayList<SubscriberServices>();

        for (ServiceFee2 fee : serviceFees.values())
        {
            Service service = null;
            try
            {
                service = ServiceSupport.getService(ctx, fee.getServiceId());
            }
            catch (HomeException ex)
            {
                new MinorLogMsg(this," Unable to find the service for id " + fee.getServiceId(), ex).log(ctx);
                continue;
            }

            SubscriberServices bean = new SubscriberServices();
            bean.setService(service);
            bean.setSubscriberId(newSub.getId());
            bean.setServiceId(fee.getServiceId());
            
            if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY) || 
                    fee.getServicePreference().equals(ServicePreferenceEnum.DEFAULT) ||
                    serviceIds_.contains(Long.valueOf(fee.getServiceId())))
            {
                // we only subscribe the mandatory services
                // now we have to subscribe default services as well
                bean.setServiceId(fee.getServiceId());
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
                bean.setMandatory(fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY));
                bean.setSubscriberServiceDates(ctx, new Date());
                bean.setServicePeriod(fee.getServicePeriod());
                provServices.add(bean);
            }
        }
        
        return provServices;
    }
    
    
    
    /**
     * @{inheritDoc}
     */
    @Override
    public void removeOldEntity(Context ctx, CSBTR request) throws MoveException
    {
        super.removeOldEntity(ctx, request);
    }
    
    private List<SubscriberServices> services_ = new ArrayList<SubscriberServices>();
    private Set<Long> serviceIds_ = new HashSet<Long>();
}
