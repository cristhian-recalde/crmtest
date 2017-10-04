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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;



/**
 * This class is responsible for calculating the required actions for all the currently
 * provisioned or suspended, elegible to provision, and provisioned with errors or
 * unprovisioned with errors subscriber services.
 * 
 * @author Marcio Marques
 * @since 8_6
 * 
 */
public class CompoundServicesProvisioningCalculator extends AbstractSubscriberServicesProvisioningCalculator
{

    /**
     * Create a new instance of <code>CompoundServicesProvisioningCalculator</code> with the default calculators.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     */
    public CompoundServicesProvisioningCalculator(final Subscriber oldSubscriber, final Subscriber newSubscriber)
    {
        this(oldSubscriber, newSubscriber, 
                new UnprovisionSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber),
                new UpdateAndUnsuspendSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber),
                new ProvisionSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber),
                new SuspendSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber),
                new ResumeSubscriberServicesProvisioningCalculator(oldSubscriber, newSubscriber));
    }
    

    /**
     * Create a new instance of <code>CompoundServicesProvisioningCalculator</code> with the given list of calculators.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     * @param calculators
     */
    public CompoundServicesProvisioningCalculator(final Subscriber oldSubscriber, final Subscriber newSubscriber, final Collection<AbstractSubscriberServicesProvisioningCalculator> calculators)
    {
        super(oldSubscriber, newSubscriber);
        calculationDone_ = false;
        calculators_ = calculators;
    }

    /**
     * Create a new instance of <code>CompoundServicesProvisioningCalculator</code> with the given list of calculators.
     * 
     * @param oldSubscriber
     * @param newSubscriber
     * @param calculators
     */
    public CompoundServicesProvisioningCalculator(final Subscriber oldSubscriber, final Subscriber newSubscriber, AbstractSubscriberServicesProvisioningCalculator... calculators)
    {
        super(oldSubscriber, newSubscriber);
        calculationDone_ = false;
        
        calculators_ = new ArrayList<AbstractSubscriberServicesProvisioningCalculator>();

        for (int i=0; i< calculators.length; i++)
        {
            calculators_.add(calculators[i]);
        }
    }
    /**
     * Calculates the list of subscriber services to be provisioned, unprovisioned,
     * suspended, resumed, updated and unsuspended.
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
        Map<Short, Set<SubscriberServices>> result = new HashMap<Short, Set<SubscriberServices>>();
        if (getNewSubscriber().isPooledGroupLeader(ctx))
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = createLogHeader("Subscriber services provision skipped for dummy owner MSISDN subscriber", 
                        getNewSubscriber(), false);
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }
        else
        {
            for (AbstractSubscriberServicesProvisioningCalculator calculator : calculators_)
            {
                Map<Short, Set<SubscriberServices>> individualResult = calculator.calculate(ctx,
                        servicesToRetry, elegibleServices, currentlyProvisionedServices, currentlySuspendedServices, result);
                for (Short key : individualResult.keySet())
                {
                    result.put(key, individualResult.get(key));
                }
            }
            
            servicesToUnprovision_ = retrieveSetFromMap(UNPROVISION, result);
            servicesToUpdate_ = retrieveSetFromMap(UPDATE, result);
            servicesToUnsuspend_ = retrieveSetFromMap(UNSUSPEND, result);
            servicesToProvision_ = retrieveSetFromMap(PROVISION, result);
            servicesToSuspend_ = retrieveSetFromMap(SUSPEND, result);
            servicesToResume_ = retrieveSetFromMap(RESUME, result);

			CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, getNewSubscriber()
					.getSpid());
			/*
			 * If Spid level flag 'Skip Retry of Provisioning or Unprovisioning
			 * of Failed Services On Subscription Update' is false then legacy
			 * behavior. This flag is introduced for Gamma TCBSUP-1483, to skip
			 * the retry action on provisioning and unprovisioning for failed
			 * services.
			 */
			if (crmSpid.getSkipRetryOfProvOrUnprovOfFailedServicesOnSubscriptionUpdate()) 
			{
				filterOutFailedServices(ctx);
			}
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = createLogHeader("Subscriber services provisioning calculation", 
                        getNewSubscriber(), true);
                appendServicesListToLog(sb, "Expected services to unprovision", servicesToUnprovision_, true);
                appendServicesListToLog(sb, "Expected services to provision", servicesToProvision_, true);
                appendServicesListToLog(sb, "Expected services to resume", servicesToResume_, true);
                appendServicesListToLog(sb, "Expected services to suspend", servicesToSuspend_, true);
                appendServicesListToLog(sb, "Expected services to unsuspend", servicesToUnsuspend_, true);
                appendServicesListToLog(sb, "Expected services to update", servicesToUpdate_, false);
                LogSupport.debug(ctx, this, sb.toString(), null);
            }
        }
        
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.TELUS_GATEWAY_LICENSE_KEY)) 
        {   
            filterOutVMService(ctx); 
        }
        if (LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.BLACKBERRY_LICENSE)) 
        {   
        	filterDuplicateBBService(ctx); 
        }
        
        calculationDone_ = true;

        return result;
    }
    
    
	private void filterOutFailedServices(Context ctx) {
		Set<SubscriberServices> servicesToSkipFromProvOrUnProv = new HashSet<SubscriberServices>();
		for (SubscriberServices srvToSkipFromUnProv : servicesToUnprovision_) 
		{
			
			if (ServiceStateEnum.PROVISIONEDWITHERRORS.equals(srvToSkipFromUnProv.getProvisionedState())
					|| ServiceStateEnum.UNPROVISIONEDWITHERRORS.equals(srvToSkipFromUnProv.getProvisionedState())) 
			{
				servicesToSkipFromProvOrUnProv.add(srvToSkipFromUnProv);
			}
		}

		for (SubscriberServices srvToSkipFromProv : servicesToProvision_) 
		{
			if (ServiceStateEnum.PROVISIONEDWITHERRORS.equals(srvToSkipFromProv.getProvisionedState())
					|| ServiceStateEnum.UNPROVISIONEDWITHERRORS.equals(srvToSkipFromProv.getProvisionedState())) 
			{
				servicesToSkipFromProvOrUnProv.add(srvToSkipFromProv);
			}
		}
		removeServices(servicesToUnprovision_, servicesToSkipFromProvOrUnProv);
		removeServices(servicesToProvision_, servicesToSkipFromProvOrUnProv);
		
	}

/*
 * we can use servicesToUnprovision_.removeAll()/ servicesToProvision_.removeAll() method easily but 
 * service object are not same into set. they was changed so we have to check with service id and filter it, 
 * servicesToUnprovision_ and servicesToProvision_ have duplicate services but its object are different.
 */
	private void removeServices(Set<SubscriberServices> serviceFromRemovedSet, Set<SubscriberServices> removeServices)
	{
		Set<SubscriberServices> removeServiceSet = new HashSet<SubscriberServices>();
		for (SubscriberServices srvToSkipFromUnProv : serviceFromRemovedSet) 
		{
			for (SubscriberServices serviceToRemove : removeServices) 
			{
				if(serviceToRemove.getServiceId() == srvToSkipFromUnProv.getServiceId())
				{
					removeServiceSet.add(srvToSkipFromUnProv);
				}
			}
		}
		serviceFromRemovedSet.removeAll(removeServiceSet);
	}
	
	private void filterDuplicateBBService(Context ctx)
    {
        SubscriberServices bbToRemove =null, bbToProvision=null;
         
        for (SubscriberServices toRemove : this.servicesToUnprovision_)
        {
        	
        	if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "SubscriberService Unprovision Service : "+ toRemove.getServiceId() + " Provision State : "+ toRemove.getProvisionedState(), null);
            }
        	 
            Service serv = toRemove.getService(ctx); 
            if (serv.getType().equals(ServiceTypeEnum.BLACKBERRY) 
            		&& ServiceStateEnum.PROVISIONEDWITHERRORS.equals(toRemove.getProvisionedState()))
            {
                bbToRemove = toRemove; 
                break;
            }
        }
        
        for (SubscriberServices toProvision : this.servicesToProvision_)
        {
        	if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "SubscriberService Provision Service : "+ toProvision.getServiceId() + " Provision State : "+ toProvision.getProvisionedState(), null);
            }
       	 
            Service serv = toProvision.getService(ctx);
            if (serv.getType().equals(ServiceTypeEnum.BLACKBERRY)
            		&& ServiceStateEnum.PROVISIONEDWITHERRORS.equals(toProvision.getProvisionedState()))
            {
                bbToProvision = toProvision; 
                break; 
            }
        }
        
        if ( bbToRemove != null && bbToProvision != null && 
        		(bbToRemove.getService().getID() == bbToProvision.getService().getID()) )
        {
        	this.servicesToUnprovision_.remove(bbToRemove);
        }
    }
    
    private void filterOutVMService(Context ctx)
    {
        SubscriberServices vmToRemove =null, vmToProvision=null;
         
        for (SubscriberServices toRemove : this.servicesToUnprovision_)
        {
            Service serv = toRemove.getService(ctx); 
            if (serv.getType().equals(ServiceTypeEnum.VOICEMAIL))
            {
                vmToRemove = toRemove; 
                break;
            }
        }
        
        for (SubscriberServices toProvision : this.servicesToProvision_)
        {
            Service serv = toProvision.getService(ctx);
            if (serv.getType().equals(ServiceTypeEnum.VOICEMAIL))
            {
                vmToProvision = toProvision; 
                break; 
            }
        }
        
        // 13020153062 - This check is required to ensure that provisioning is skipped only for 
        // same Voice Mail service ID. For different service ID, voicemail box may change.
        if ( vmToRemove != null && vmToProvision != null && 
        		(vmToRemove.getService().getID() == vmToProvision.getService().getID()) )
        {
            vmToRemove.setSkipProvision(true);
            vmToProvision.setSkipProvision(true);
        }
        
        
    }
    
    
    
    /**
     * Retrieve a given set from the map
     * @param index
     * @param map
     * @return
     */
    private Set<SubscriberServices> retrieveSetFromMap(final short index, final Map<Short, Set<SubscriberServices>> map)
    {
        Set<SubscriberServices> result = null;
        
        if (map!=null)
        {
            result = map.get(index);
        }
        
        if (result == null)
        {
            result = new HashSet<SubscriberServices>();
        }
        
        return result;
    }

    
    /**
     * Updates the new subscriber intent to provision services from suspended to
     * provisioned, to force provision and charge after a price plan change.
     * 
     * @param ctx
     * @throws HomeException
     */
    private void updateSuspendedServicesForFullUnprovision(final Context ctx) throws HomeException
    {
        // If it's a price plan change set all services to requiring provision.
        if (!SubscriberSupport.isSamePricePlanVersion(ctx, getOldSubscriber(), getNewSubscriber()))
        {
            for (SubscriberServices service : (Set<SubscriberServices>) getNewSubscriber().getIntentToProvisionServices(ctx))
            {
                if (service.getProvisionedState().equals(ServiceStateEnum.SUSPENDED))
                {
                    service.setProvisionedState(ServiceStateEnum.PROVISIONED);
                }
            }
        }
    }

        
    /**
     * Retrieve services to provision.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToProvision(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToProvision_;
    }

    
    /**
     * Retrieve services to unprovision.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToUnprovision(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToUnprovision_;
    }


    /**
     * Retrieve services to suspend.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToSuspend(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToSuspend_;
    }
    
    
    /**
     * Retrieve services to resume.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToResume(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToResume_;
    }
    
    
    /**
     * Retrieve services to update.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToUpdate(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToUpdate_;
    }
    
    
    /**
     * Retrieve services to unsuspend.
     * @param ctx
     * @return
     * @throws HomeException
     */
    public Collection<SubscriberServices> getServicesToUnsuspend(final Context ctx) throws HomeException
    {
        if (!calculationDone_)
        {
            calculate(ctx);
        }
        return servicesToUnsuspend_;
    }
    
    private Set<SubscriberServices> servicesToProvision_ = new HashSet<SubscriberServices>();;
    
    private Set<SubscriberServices> servicesToUnprovision_ = new HashSet<SubscriberServices>();;

    private Set<SubscriberServices> servicesToSuspend_ = new HashSet<SubscriberServices>();;

    private Set<SubscriberServices> servicesToUnsuspend_ = new HashSet<SubscriberServices>();;

    private Set<SubscriberServices> servicesToResume_ = new HashSet<SubscriberServices>();;
    
    private Set<SubscriberServices> servicesToUpdate_ = new HashSet<SubscriberServices>();;
    
    private boolean calculationDone_ = false;
    
    Collection<AbstractSubscriberServicesProvisioningCalculator> calculators_;
}
