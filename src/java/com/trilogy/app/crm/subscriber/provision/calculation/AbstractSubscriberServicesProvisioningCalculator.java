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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.priceplan.PricePlanVersionUpdateAgent;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * This class implements the generic methods used by the subscriber services provisioning calculators.
 * 
 * @author Marcio Marques
 * @since 8_6
 *
 */
abstract class AbstractSubscriberServicesProvisioningCalculator
{

    /**
     * Create a new instance of <code>SubscriberServicesProvisioningCalculator</code>.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     */
    AbstractSubscriberServicesProvisioningCalculator(Subscriber oldSubscriber, Subscriber newSubscriber)
    {
        this.oldSubscriber_ = oldSubscriber;
        this.newSubscriber_ = newSubscriber;
    }
   

    /**
     * The only public method of this class. This method retrieves the elegible services
     * for provisioning, current provisioned and suspended services, and services whose
     * provisioning/unprovisioning should be retrieved, and call the private calculate() method.
     * 
     * @param ctx
     * @return A map with sets of services for which specific actions should be performed.
     * @throws HomeException
     */
    public final Map<Short, Set<SubscriberServices>> calculate(final Context ctx) throws HomeException
    {
        final Collection<SubscriberServices> servicesToRetry = getServicesCurrentlyProvisionedOrUnprovisionedWithErrors(ctx);
        final Collection<SubscriberServices> elegibleServices = getServicesElegibleForProvisioning(ctx);
        final Collection<SubscriberServices> currentlyProvisionedServices = getServicesCurrentlyProvisioned(ctx, false);
        final Collection<SubscriberServices> currentlySuspendedServices = getServicesCurrentlySuspended(ctx);
        
        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = createLogHeader("Subscriber services provisioning calculation", 
                    getNewSubscriber(), true);
            appendServicesListToLog(sb, "Services currently provisioned or unprovisioned with errors", servicesToRetry, true);
            appendServicesListToLog(sb, "Services elegible for provisioning", elegibleServices, true);
            appendServicesListToLog(sb, "Services currently provisioned", currentlyProvisionedServices, true);
            appendServicesListToLog(sb, "Services currently suspended", currentlySuspendedServices, false);
            LogSupport.debug(ctx, this, sb.toString(), null);
        }

        return calculate(ctx, servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices,
                new HashMap<Short, Set<SubscriberServices>>());

    }
    

    /**
     * This method calculates the services for which a specific action should be performed.
     * @param ctx
     * @param servicesToRetry
     * @param elegibleServices
     * @param currentlyProvisionedServices
     * @param currentlySuspendedServices
     * @return A map with sets of services for which specific actions should be performed.
     * @throws HomeException
     */
    protected abstract Map<Short, Set<SubscriberServices>> calculate(Context ctx,
            Collection<SubscriberServices> servicesToRetry, Collection<SubscriberServices> elegibleServices,
            Collection<SubscriberServices> currentlyProvisionedServices, Collection<SubscriberServices> currentlySuspendedServices, 
            Map<Short, Set<SubscriberServices>> currentResults) throws HomeException; 
    

    /**
     * Retrieve a set of the currently provisioned services.
     * 
     * @param ctx
     * @param subscriber
     * @param includeSuspendedServices
     *            If set to true, the retrieved set will contain also the suspended
     *            services
     * @return
     */
    private Collection<SubscriberServices> getServicesCurrentlyProvisioned(final Context ctx,
            final boolean includeSuspendedServices)
    {
        final Collection<SubscriberServices> result;
        if (getOldSubscriber() != null)
        {
            if (includeSuspendedServices)
            {
                result = SubscriberServicesSupport.getSubscribersServices(ctx, getOldSubscriber()
                                .getId()).values();
            }
            else
            {
                // Unprovision only provisioned services, as suspended services will get the correct values when resumed.
                result = SubscriberServicesSupport.getProvisionedOrProvisionedWithErrorsSubscriberServices(ctx, 
                            getOldSubscriber().getId());
            }
        }
        else
        {
            result = new HashSet<SubscriberServices>();
        }
        
        return result;
    }
    

    /**
     * Retrieve a set or services currently suspended.
     * @param ctx
     * @return
     */
    private Collection<SubscriberServices> getServicesCurrentlySuspended(final Context ctx)
    {
        Collection<SubscriberServices> result = new HashSet<SubscriberServices>();
        if (getOldSubscriber() != null)
        {
            result = SubscriberServicesSupport.getSuspendedServices(ctx,
                    getOldSubscriber().getId());
        }
        
        return result;
    }


    /**
     * Retrieve a set of services currently provisioned or unprovisioned with errors
     * @param ctx
     * @return
     * @throws HomeException
     */
    private Collection<SubscriberServices> getServicesCurrentlyProvisionedOrUnprovisionedWithErrors(final Context ctx) throws HomeException
    {
		final Set<SubscriberServices> result = new HashSet<SubscriberServices>();
		if (getOldSubscriber() != null) {
			if (getNewSubscriber() != null) {
				Collection<SubscriberServices> services = SubscriberServicesSupport
						.getAllSubscribersServices(ctx,
								getNewSubscriber().getId(), true).values();
				for (final SubscriberServices service : services) {
					if (service.getProvisionedState().equals(
							ServiceStateEnum.PROVISIONEDWITHERRORS)
							|| service.getProvisionedState().equals(
									ServiceStateEnum.UNPROVISIONEDWITHERRORS)) {
						result.add(service);
					}
				}
			}
		}
		return result;
    }
    
    
    /**
     * Retrieve a set of services elegible for provisioning
     * @param ctx
     * @return
     * @throws HomeException
     */
    private Collection<SubscriberServices> getServicesElegibleForProvisioning(final Context ctx) throws HomeException
    {
        final Collection<SubscriberServices> result;
        if (getNewSubscriber()!=null)
        {
            result = getNewSubscriber().getElegibleForProvision(ctx);
        }
        else
        {
            result = new HashSet<SubscriberServices>();
        }
        return result;
    }
    
    
    /**
     * Retrieves the list of expected services that should be CLCT suspended.
     * @param ctx
     * @return List of expected services that should be CLCT suspended.
     * @throws HomeException
     */
    protected Collection<SubscriberServices> getExpectedCLCTSuspendedServices(final Context ctx) throws HomeException
    {
        Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
        
        // Only calculated if subscriber is CLCT Suspended 
        if (isCLCTSuspended())
        {
            /*final Collection<SubscriberServices> mandatoryServices = SubscriberServicesSupport
                    .getMandatorySubscriberServices(ctx, getNewSubscriber().getId());*/

            // Find all CLCT services eligible for suspension.
            result = SubscriberServicesSupport.getCLCTServicesWithThresholdAboveBalance(ctx, getNewSubscriber()
                    .getElegibleForProvision(ctx), getNewSubscriber().getSubNewBalance());
            
            // Remove mandatory services from the list of services to be CLCT suspended.
            // result.removeAll(ServiceChargingSupport.getCrossed(ctx, result, mandatoryServices));

            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = createLogHeader("Expected CLCT Suspended services calculation.", 
                        getNewSubscriber(), true);
                appendServicesListToLog(sb, "Expected CLCT Suspended services", result, false);
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }
        else
        {
            // if there is no clct change return all the suspended clct services
            boolean isCLTCEnabled = true;
            result = SubscriberServicesSupport.findSuspendedWithinListByCLTC(ctx, getNewSubscriber()
                    .getElegibleForProvision(ctx), isCLTCEnabled);
        }
        return result;
    }

    
    /**
     * Verifies if a full unprovision is necessary.
     * @param ctx
     * @param oldSubscriber
     * @param newSubscriber
     * @return
     * @throws HomeException
     */
    protected boolean needsFullUnprovision(final Context ctx)
    throws HomeException
    {
        
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY) &&
                (isMSISDNChange(ctx)))   // || isDeactivation(ctx)))
        {
            return false; 
        }   
     
    return (isSubscriberConversion() ||
            isSubscriberStateChangeDuringConversion(ctx) ||
            isPricePlanChangeWithFullUnprovision(ctx) ||
            isPricePlanVersionUpdateWithFullUnprovision(ctx)) ||
            isMSISDNChange(ctx) || 
            isDeactivation(ctx);
    }
    
    
    
    private boolean isSubscriberStateChangeDuringConversion(Context ctx)
    {
        return isSubscriberStateChange() && ctx.get(Common.POSTPAID_PREPAID_CONVERSION_SUBCRIBER) != null;
    }
    
    
    
    private boolean isPricePlanChangeWithFullUnprovision(Context ctx)
    {
        return oldSubscriber_.getPricePlan() != newSubscriber_.getPricePlan() && 
        SystemSupport.isFullProvisionOnPricePlanUpdate(ctx);
    }

    
    
    private boolean isPricePlanVersionUpdateWithFullUnprovision(Context ctx)
    {
        return !isSamePricePlanVersion() && ctx.getBoolean(PricePlanVersionUpdateAgent.PRICE_PLAN_VERSION_UPDATE, false) && 
        SystemSupport.isFullProvisionOnPricePlanVersionUpdate(ctx);
    }

    
    
    protected boolean isSubscriberCreation()
    {
        return oldSubscriber_==null;
    }

    /**
     * Verifies if this is a MSISDN change operation.
     * @param ctx TODO
     * @return
     */
    protected boolean isMSISDNChange(Context ctx)
    {
    	
    	boolean msisdnChanged = oldSubscriber_!=null && newSubscriber_!=null && 
        	(!SafetyUtil.safeEquals(oldSubscriber_.getMSISDN(),newSubscriber_.getMSISDN()));
    	
    	if(newSubscriber_ != null)
    	{
    		try
    		{
    			return !SpidSupport.getCRMSpid(ctx, newSubscriber_.getSpid()).isSkipProvisioningServicesOnChangeMsisdn() && msisdnChanged;
    		}
    		catch(Exception e) 
    		{
    			return msisdnChanged;
    		}
    	}
    	
        return msisdnChanged;
    }
    
    
    /**
     * Verifies if this is a subscriber conversion operation.
     * @return
     */
    protected boolean isSubscriberConversion()
    {
        return oldSubscriber_!=null && newSubscriber_!=null && 
        (!SafetyUtil.safeEquals(oldSubscriber_.getSubscriberType(),newSubscriber_.getSubscriberType()));
    }


    /**
     * Verifies if this is a subscriber state change operation.
     * @return
     */
    protected boolean isSubscriberStateChange()
    {
        return oldSubscriber_!=null && newSubscriber_!=null && 
        (!SafetyUtil.safeEquals(oldSubscriber_.getState(),newSubscriber_.getState()));
    }
    
    
    /**
     * Verifies if this is a deactivation or remove during store operation.
     * @param ctx
     * @return
     */
    protected boolean isDeactivation(final Context ctx)
    {
        return isRemovalDuringStore(ctx) || (oldSubscriber_ != null
                && newSubscriber_ != null
                && !isSubscriberInUnprovisionedState(ctx, oldSubscriber_)
                && isSubscriberInUnprovisionedState(ctx, newSubscriber_));
    }
    
    
    private boolean isSubscriberInUnprovisionedState(final Context ctx, final Subscriber subscriber)
    {
        return EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber,
                getUnprovisionStates(ctx, subscriber));
    }

    
    /**
     * Verifies if this is a remove during store operation. Should never happen.
     * @param ctx
     * @return
     */
    protected boolean isRemovalDuringStore(final Context ctx)
    {
        return oldSubscriber_ != null
                && newSubscriber_ == null
                && !EnumStateSupportHelper.get(ctx).isOneOfStates(oldSubscriber_,
                        getUnprovisionStates(ctx, oldSubscriber_));
    }
    
    
    /**
     * Verifies if the price plan version is not changed.
     * @return
     */
    protected boolean isSamePricePlanVersion()
    {
        return !isPricePlanChange() && oldSubscriber_.getPricePlanVersion() == newSubscriber_.getPricePlanVersion();
    }
    
    /**
     * Verifies if this is a price plan change operation.
     * @param ctx
     * @return
     */
    protected boolean isPricePlanChange()
    {
        return oldSubscriber_ != null && newSubscriber_ != null
                && oldSubscriber_.getPricePlan() != newSubscriber_.getPricePlan();
    }

    
    /**
     * Check if subscriber is CLTC suspended.
     * @return
     */
    protected boolean isCLCTSuspended()
    {
        return newSubscriber_.isClctChange() ? newSubscriber_.getSubNewBalance() < newSubscriber_
                .getSubOldBalance() : false;
    }

    
    /**
     * Check if we're performing a CLTC operation.
     * @return
     */
    protected boolean isCLCTOperation()
    {
        return newSubscriber_.isClctChange();
    }

    
    /**
     * Returns the set of states which would be unprovisioned.
     * 
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being processed.
     * @return The set of indices of the states in which the subscriber should be
     *         unprovisioned.
     */
    protected static Collection<SubscriberStateEnum> getUnprovisionStates(final Context ctx, final Subscriber sub)
    {
        Collection<SubscriberStateEnum> result = null;
        if (sub.isPostpaid())
        {
            result = POSTPAID_UNPROVISION_STATES;
        }
        else
        {
            result = PREPAID_UNPROVISION_STATES;
        }
        return result;
    }


    /**
     * Returns a StringBuilder with a given message and the identifier, MSISDN, and BAN of the given subscriber.
     * @param description
     * @param subscriber
     * @param appendComma
     * @return
     */
    protected static StringBuilder createLogHeader(final String description, final Subscriber subscriber, final boolean appendComma)
    {
        StringBuilder sb = new StringBuilder(description);
        sb.append(": ");
        if (subscriber.isSubscriberIdSet())
        {
            sb.append("SubscriberId = '");
            sb.append(subscriber.getId());
            sb.append("', ");
        }
        sb.append("MSISDN = '");
        sb.append(subscriber.getMSISDN());
        sb.append("', BAN = '");
        sb.append(subscriber.getBAN());
        if (appendComma)
        {
            sb.append("', ");
        }
        else
        {
            sb.append("'.");
        }
        return sb;
    }


    /**
     * Append a list of services to a given StringBuilder
     * @param sb
     * @param name
     * @param services
     * @param appendComma
     */
    protected static void appendServicesListToLog(final StringBuilder sb, final String name, final Collection services, final boolean appendComma)
    {
        sb.append(name);
        sb.append(" = '");
        sb.append(ServiceSupport.getServiceIdString(services));
        if (appendComma)
        {
            sb.append("', ");
        }
        else
        {
            sb.append("'.");
        }
    }
    
    
    /**
     * 
     * @return
     */
    public Subscriber getNewSubscriber()
    {
        return newSubscriber_;
    }

    
    /**
     * 
     * @return
     */
    public Subscriber getOldSubscriber()
    {
        return oldSubscriber_;
    }



    /**
     * If the subscriber is going into any of these states, deactivation should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> DEACTIVATION_STATES = Arrays.asList(
            SubscriberStateEnum.SUSPENDED, SubscriberStateEnum.LOCKED);
    
    /**
     * If the subscriber is going into any of these states, activation should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> ACTIVATION_STATES = Arrays.asList(SubscriberStateEnum.ACTIVE,
            SubscriberStateEnum.PROMISE_TO_PAY);
    
    /**
     * If the subscriber is going into any of these states, unprovision should be
     * performed.
     */
    private static final Collection<SubscriberStateEnum> UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
    /**
     * Postpaid subscriber state change from other states to these states, unprovision
     * charge will apply, vice versa.
     */
    private static final Collection<SubscriberStateEnum> POSTPAID_UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
    /**
     * True prepaid subscriber state change from other states to these states, unprovision
     * charge will apply, vice versa. For Buzzard prepaid mode, even inactive state we
     * need services provisioned.
     */
    private static final Collection<SubscriberStateEnum> PREPAID_UNPROVISION_STATES = Arrays.asList(
            SubscriberStateEnum.PENDING, SubscriberStateEnum.INACTIVE);
    
    protected static short PROVISION = 0;
    protected static short UNPROVISION = 1;
    protected static short RESUME = 2;
    protected static short SUSPEND = 3;
    protected static short UNSUSPEND = 4;
    protected static short UPDATE = 5;
    
    protected static final String COMMA_SEPERATOR = ", ";
    private Subscriber newSubscriber_;
    
    private Subscriber oldSubscriber_;
}
