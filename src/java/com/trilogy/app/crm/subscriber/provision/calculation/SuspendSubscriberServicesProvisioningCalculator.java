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
package com.trilogy.app.crm.subscriber.provision.calculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.cltc.SubCltcOperationCode;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This class is responsible for calculating the services needing suspension, based on the
 * currently provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
class SuspendSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{
    
    /**
     * Create a new instance of <code>SuspensionSubscriberServicesProvisioningCalculator</code>.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     * @param servicesToUnprovision
     */
    SuspendSubscriberServicesProvisioningCalculator(final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
    {
        super(oldSubscriber, newSubscriber);
    }
    
    
    /**
     * Calculates the list of subscriber services to be suspended.
     * 
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @return
     * @throws HomeException
     */
    protected Map<Short, Set<SubscriberServices>> calculate(final Context ctx,
            final Collection<SubscriberServices> servicesToRetry, final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices,
            final Map<Short, Set<SubscriberServices>> currentResults) throws HomeException
    {
        final Map<Short, Set<SubscriberServices>> result;
        
        if (isSubscriberCreation())
        {            
            result = calculateServicesToSuspendOnCreation(ctx);
        }
        else
        {
            result = calculateServicesToSuspendOnStore(ctx, elegibleServices, currentlyProvisionedServices,
                    currentlySuspendedServices, currentResults);
        }
        
        return result;
    }

    private Map<Short, Set<SubscriberServices>> calculateServicesToSuspendOnCreation(final Context ctx) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.SUSPEND, services);

        if (!EnumStateSupportHelper.get(ctx).isOneOfStates(getNewSubscriber(),
                getUnprovisionStates(ctx, getNewSubscriber())))
        {
            // Provision all services elegible for provisioning.
            services.addAll(getExpectedCLCTSuspendedServices(ctx));
        }
        
        return result;
    }
    
    
    private Map<Short, Set<SubscriberServices>> calculateServicesToSuspendOnStore(final Context ctx,
            final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices,
            final Map<Short, Set<SubscriberServices>> currentResults) throws HomeException
    {
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.SUSPEND, services);

        final Set<SubscriberServices> servicesToProvision = currentResults.get(PROVISION);
        
        if (isCLCTOperation())
        {
            SubscriberCltc subscriberCltc = (SubscriberCltc) ctx.get(Common.ER_447_SUBSCRIBER_CLCT);
            Map<Long, Boolean> serviceTypeExtensionActionFlag = (Map<Long, Boolean>)ctx.get(Common.SERVICETYPE_EXTENSION_ACTION_FLAG);
            
            /* With F-001213 clct is generated for both Airtime depletion and bundle depletion.
            *  Earlier operation code value used to be ignored, we need to execute the current suspension logic for airtime balance and 
            *  also bundle balance. For cases where airtime depletes and this should affect bundle services the operation code is 0.
            *  
            *  Following condition ensures we dont execute suspension logic for bundle related operations
            *
            **/
            if(subscriberCltc != null && subscriberCltc.getOperation() == SubCltcOperationCode.CLCT_DEFAULT)
            {
                if (isCLCTSuspended())
                {
                    services.addAll(getExpectedCLCTSuspendedServices(ctx));
                    services.removeAll(ServiceChargingSupport.getCrossed(ctx, services, currentlySuspendedServices));

                    Iterator<SubscriberServices> iterator = servicesToProvision.iterator();

                    while (iterator.hasNext())
                    {
                        SubscriberServices service = iterator.next();
                        if (service.getProvisionedState().equals(ServiceStateEnum.SUSPENDED))
                        {
                            if (LogSupport.isDebugEnabled(ctx))
                            {
                                StringBuilder sb = createLogHeader(
                                        "Suspension calculation: Suspended service found for CLCT suspended subscriber",
                                        getNewSubscriber(), true);
                                sb.append("ServiceId='");
                                sb.append(service.getServiceId());
                                sb.append("'.");
                                LogSupport.debug(ctx, this, sb.toString(), null);
                            }
                            // If postpaid subscriber is bellow credit limit check treshold, CLCT service should be
                            // suspended.
                            services.add(service);
                        }
                    }
                }
            }
            
            
            if(subscriberCltc != null && (subscriberCltc.getBundleServicesChanged() !=null && !subscriberCltc.getBundleServicesChanged().isEmpty()))
            {
                if(subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED || subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED 
                		|| (subscriberCltc.getOperation() == SubCltcOperationCode.CLCT_DEFAULT && subscriberCltc.getUpCrossThreshold())
                		|| (subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_TOPPED || subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_PROVISIONED) )
                {
                    
                    StringTokenizer strTokenizer = new StringTokenizer(subscriberCltc.getBundleServicesChanged(), ",");
                    StringBuffer successServiceIds = new StringBuffer();
                    StringBuffer failedServiceIds = new StringBuffer();
                    
                    while(strTokenizer.hasMoreTokens())
                    {
                    	boolean actionFlag = false;
                        boolean isServiceFound = false;
                        long suspendedServiceId = Long.parseLong(strTokenizer.nextToken());
                        
                        if(serviceTypeExtensionActionFlag != null)
                        {
                        	if(serviceTypeExtensionActionFlag.get(suspendedServiceId) != null)
                        	{
                        		actionFlag = serviceTypeExtensionActionFlag.get(suspendedServiceId);	
                        	}
                        }
                        
                        for(SubscriberServices service : currentlyProvisionedServices)
                        {
                            if(service.getServiceId() == suspendedServiceId )
                            {
                            
                            	if((subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_TOPPED 
                                		|| subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_PROVISIONED) 
                                		&& actionFlag)
                            	{
                            		services.add(service);
                                    isServiceFound = true;
                                    break;
                            	}
                            	else if( (subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED 
                            			|| subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED) 
                            			&& !actionFlag)
                            	{
                            		services.add(service);
                                    isServiceFound = true;
                                    break;
                            	}
                                
                            }
                        }
                        if(isServiceFound)
                        {
                            successServiceIds.append(suspendedServiceId + COMMA_SEPERATOR);
                        }
                        else
                        {
                            failedServiceIds.append(suspendedServiceId + COMMA_SEPERATOR);
                        }
                    }
                    
                    if (LogSupport.isDebugEnabled(ctx) && successServiceIds.length() > 0)
                    {
                        LogSupport.debug(ctx, this, "Service Id : " + successServiceIds.toString() + " will be CLCT suspended.", null);
                    }
                    if (LogSupport.isDebugEnabled(ctx) && failedServiceIds.length() > 0)
                    {
                        LogSupport.debug(ctx, this, "Service Id : " + failedServiceIds.toString() + " will NOT be CLCT suspended.", null);
                    }
                }
            }
        }
        
        return result;
    }


}
