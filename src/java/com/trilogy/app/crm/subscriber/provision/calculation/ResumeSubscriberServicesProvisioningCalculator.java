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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberCltc;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.cltc.SubCltcOperationCode;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;


/**
 * This class is responsible for calculating the services needing resume, based on the
 * currently provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
class ResumeSubscriberServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{
    
    /**
     * Create a new instance of <code>ResumeSubscriberServicesProvisioningCalculator</code>.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     * @param servicesToProvision
     */
    ResumeSubscriberServicesProvisioningCalculator(final Subscriber oldSubscriber,
            final Subscriber newSubscriber)
    {
        super(oldSubscriber, newSubscriber);
    }
    
    
    /**
     * Calculates the list of subscriber services to be resumed.
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
        if (!isSubscriberCreation() && !getNewSubscriber().isInFinalState())
        {
            result = calculateServicesToResumeOnStore(ctx, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices);
        }
        else
        {
            result = new HashMap<Short,Set<SubscriberServices>>();
            final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
            result.put(this.RESUME, services);
        }
        
        return result;
    }
    
    private Map<Short, Set<SubscriberServices>> calculateServicesToResumeOnStore(final Context ctx,
            final Collection<SubscriberServices> elegibleServices,
            final Collection<SubscriberServices> currentlyProvisionedServices,
            final Collection<SubscriberServices> currentlySuspendedServices) throws HomeException
    {
    	
        final Map<Short,Set<SubscriberServices>> result = new HashMap<Short,Set<SubscriberServices>>();
        final Set<SubscriberServices> services = new HashSet<SubscriberServices>();
        result.put(this.RESUME, services);

        // If subscriber is resuming, resume currently suspended eligible services.
        if (isCLCTOperation())
        {
            SubscriberCltc subscriberCltc = (SubscriberCltc) ctx.get(Common.ER_447_SUBSCRIBER_CLCT);
            Map<Long, Boolean> serviceTypeExtensionActionFlag = (Map<Long, Boolean>)ctx.get(Common.SERVICETYPE_EXTENSION_ACTION_FLAG);
            
            /* With F-001213 clct is generated for both Airtime depletion and bundle depletion.
            *  Earlier operation code value used to be ignored, we need to execute the current resume logic for airtime balance and 
            *  also bundle balance. For cases where airtime depletes and this should affect bundle services the operation code is 0.
            *  
            *  Following condition ensures we dont execute resume logic for bundle related operations
            *
            **/
            if (subscriberCltc != null && subscriberCltc.getOperation() == SubCltcOperationCode.CLCT_DEFAULT)
            {

                if (!isCLCTSuspended())
                {
                    services.addAll(elegibleServices);
                    // Only those services whose threshold is crossed should be resumed
                    services.retainAll(ServiceChargingSupport.getCrossed(ctx, services, SubscriberServicesSupport
                            .getCLCTServicesWithThresholdBelowBalance(ctx, currentlySuspendedServices,
                                    getNewSubscriber().getSubNewBalance())));
                }
            }
             
            if(subscriberCltc != null && (subscriberCltc.getBundleServicesChanged() !=null && !subscriberCltc.getBundleServicesChanged().isEmpty()))
            {
                if(subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_TOPPED || subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_PROVISIONED 
                		|| (subscriberCltc.getOperation() == SubCltcOperationCode.CLCT_DEFAULT && !subscriberCltc.getUpCrossThreshold())
                		|| (subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED || subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED ))
                {
                    StringTokenizer strTokenizer = new StringTokenizer(subscriberCltc.getBundleServicesChanged(), ",");
                    StringBuffer successServiceIds = new StringBuffer();
                    StringBuffer failedServiceIds = new StringBuffer();
                    
                    while(strTokenizer.hasMoreTokens())
                    {
                    	boolean actionFlag = false;
                        boolean isServiceFound = false;
                        long serviceToResume = Long.parseLong(strTokenizer.nextToken());

                        if(serviceTypeExtensionActionFlag != null )
                        {
                        	if(serviceTypeExtensionActionFlag.get(serviceToResume) != null)
                        	{
                        		actionFlag = serviceTypeExtensionActionFlag.get(serviceToResume);
                        	}
                        }
                        for (SubscriberServices service : currentlySuspendedServices)
                        {
                            /**
                             * Adding filter on suspend reason for TT#13082852015
                             * Please refer the Ticket for the Use case
                             */
                            if (service.getServiceId() == serviceToResume && service.getSuspendReason().getIndex() == SuspendReasonEnum.CLCT_INDEX)
                            {
                            	
                            	if((subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_TOPPED 
                            			|| subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_PROVISIONED) 
                            			&& !actionFlag)
                            	{
                            		 services.add(service);
                                     isServiceFound = true;
                                     break;
                            	}
                            	else if((subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DEPLETED 
                                        || subscriberCltc.getOperation() == SubCltcOperationCode.BUNDLE_BALANCE_DECREASED) 
                                        && actionFlag)
                            	{
                            		 services.add(service);
                                     isServiceFound = true;
                                     break;
                            	}
                            }
                        }
                        
                        if(isServiceFound)
                        {
                            successServiceIds.append(serviceToResume + COMMA_SEPERATOR);
                        }
                        else
                        {
                            failedServiceIds.append(serviceToResume + COMMA_SEPERATOR);
                        }
                    }
                    
                    if (LogSupport.isDebugEnabled(ctx) && successServiceIds.length() > 0)
                    {
                        LogSupport.debug(ctx, this, "Service Id : " + successServiceIds.toString() + " will be CLCT resumed.", null);
                    }
                    if (LogSupport.isDebugEnabled(ctx) && failedServiceIds.length() > 0)
                    {
                        LogSupport.debug(ctx, this, "Service Id : " + failedServiceIds.toString() + " will NOT be CLCT resumed.", null);
                    }
                    
                }
            }
        }
        // If subscriber is still CLCT suspended and it's a price plan change, resume services that could be CLCT suspended
        // but now are mandatory and should be resumed.
        else if (isPricePlanChange())
        {
            services.addAll(elegibleServices);
            services.removeAll(ServiceChargingSupport.getCrossed(ctx, services, getExpectedCLCTSuspendedServices(ctx)));
            services.retainAll(ServiceChargingSupport.getCrossed(ctx, services, currentlySuspendedServices));
        }
                
        return result;
    }
    
}
