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
package com.trilogy.app.crm.provision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Limit;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.OrderBy;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.OrderByHome;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.EventSuccessEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryXInfo;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.cltc.ServiceCreditLimitCheckSupport;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.subscriber.charge.CrmCharger;
import com.trilogy.app.crm.subscriber.charge.SubscriberServiceCharger;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 *  Retrieve each failed service and retry to provision/unprovision/suspend
 *
 * @author shailesh.makhijani
 * @since 9.7
 */
public class FailedSubscriberServicesRetryVisitor implements Visitor, ContextAgent {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException {
		
		final Subscriber sub = (Subscriber) obj;
		sub.setContext(ctx);
		retryFailedSubscriberServices(ctx, sub);
	}

	/**
	 * @param ctx
	 * @param sub
	 */
	private void retryFailedSubscriberServices(Context ctx, Subscriber sub) {
	    
	    Subscriber oldSub = null;

		try {
		    oldSub = (Subscriber)sub.clone();
			/**
			 * 1. For Failed provisioning services [ServiceStateEnum.PROVISIONEDWITHERRORS]
			 */
			final Collection<SubscriberServices> servicesToProv = getServicesToProvision(ctx,
					sub.getId());
			
			if (servicesToProv !=null && servicesToProv.size()>0 ){
				handleProvisioning (ctx, sub, servicesToProv);
				
				final Map<Long, SubscriberServices> provisionedServices = getProvSuccessServices(ctx, sub,
						servicesToProv);
				handleCharging(ctx,sub,provisionedServices);
			}
			
			/**
			 * 2. For Failed services which are supposed to be resumed
			 */
			
			final Collection<SubscriberServices> servicesToResume = getServicesToResume(ctx, sub.getId());
			
			if (servicesToResume !=null && servicesToResume.size()>0 ){
				handleResume(ctx, sub, servicesToResume);
			}
			
			
			/**
			 * 3. For Failed unprovisioned services [ServiceStateEnum.UNPROVISIONEDWITHERRORS] 
			 */
			final Collection<SubscriberServices> servicesToUnProv = getServicesToUnProvision(ctx,
					sub.getId());
			if(servicesToUnProv !=null && servicesToUnProv.size()>0) {
				
			    handleUnProvisioning (ctx, sub, servicesToUnProv);
				  
			    final Map<Long, SubscriberServices> unprovisionedServices = getUnProvSuccessServices(ctx, sub, servicesToUnProv);
				handleRefund(ctx, sub, unprovisionedServices);
				 
			}
			
			/**
			 * 4. For Failed suspended services [ServiceStateEnum.SUSPENDEDWITHERRORS] 
			 */
			final Collection<SubscriberServices> servicesToSuspend = getServicesToSuspend(ctx,
					sub.getId());
			
			if (servicesToSuspend !=null && servicesToSuspend.size()>0) {
				
				handleSuspension (ctx, sub, servicesToSuspend);
			}
			
			/**
			 * 5. For Failed Suspended services due to CLTC
			 */
			
			final Collection<SubscriberServices> servicesToSuspendDueToCLTC = getServicesToSuspendDueToCLTC(ctx,
					sub.getId());
			
			if (servicesToSuspendDueToCLTC !=null && servicesToSuspendDueToCLTC.size()>0) {
				handleSuspensionDueToCLTC (ctx, sub, servicesToSuspendDueToCLTC);
			}
			
		} catch (Exception ex){
			LogSupport.minor(ctx, this, "Couldn't provision/unprovision/suspend services for subscriber " + sub.getId(), ex);
		}
		
		finally
		{
		    try
            {
                logModificationER(ctx, oldSub, sub);
            }
            catch (HomeException e)
            {
                LogSupport.minor(ctx, this, "Unable to log ER for subscriber " + sub.getId(), e);
            }
		}

	}

	
    /**
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
	private void logModificationER(Context ctx,final Subscriber oldSub, final Subscriber newSub) throws HomeException
	{
	    
	    Context subCtx = ctx.createSubContext();
	    prepareProvisionResultCode(subCtx, newSub);
	    
	    int resultPricePlan = 0;
        int resultStateChange = 0;
        int resultCreditLimit = SubscriberProvisionResultCode.getProvisionCreditLimitResultCode(subCtx);
        int adjustMin = SubscriberProvisionResultCode.getProvisionAdjustMinutes(subCtx);
        long adjustAmt = SubscriberProvisionResultCode.getChargeAmount(subCtx);

        if (oldSub != null && newSub != null)
        {
            if (!EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub))
            {
                resultStateChange = SubscriberProvisionResultCode.getProvisionLastResultCode(subCtx);
            }
            if (!SubscriberSupport.isSamePricePlanVersion(ctx, oldSub, newSub))
            {
                resultPricePlan = SubscriberProvisionResultCode.getProvisionLastResultCode(subCtx);
            }
        }
        
	    long leftoverBalance = 0;
        if (oldSub.getState() != SubscriberStateEnum.INACTIVE
                && newSub.getState() == SubscriberStateEnum.INACTIVE)
        {
            // only populate the value if deactivation happened
            leftoverBalance = BalanceManagementSupport.getSubscriptionBalance(ctx,
                    ERLogger.class, oldSub);
        }
        ERLogger.logModificationER(ctx,
                newSub.getSpid(),
                oldSub,
                newSub,
                0,
                0,
                0,
                adjustMin,
                adjustAmt,
                resultPricePlan,
                oldSub.getState(),
                newSub.getState(),
                resultStateChange,
                oldSub.getDeposit(ctx),
                newSub.getDeposit(ctx),
                oldSub.getCreditLimit(ctx),
                newSub.getCreditLimit(ctx),
                resultCreditLimit,
                oldSub.getAccount(ctx).getCurrency(),
                oldSub.getAccount(ctx).getCurrency(),
                oldSub.getPricePlan(ctx).getServices(ctx),
                newSub.getPricePlan(ctx).getServices(ctx),
                0,
                oldSub.getSupportMSISDN(ctx),
                newSub.getSupportMSISDN(ctx),
                leftoverBalance // don't what this field for. 
                );
	}
	

	/**
	 * @param ctx
	 * @param sub
	 * @param servicesToSuspend
	 */
	private void handleSuspension(Context ctx, Subscriber sub,
			Collection<SubscriberServices> servicesToSuspend) {

		final List<Long> serviceIds = getServiceIdList(servicesToSuspend);
		try
		{
			LogSupport.info(ctx, this, " Suspending services " + serviceIds.toString());
			
			final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
			SubscriberServicesSupport.suspendSubscriberServices(ctx, null, new ArrayList<SubscriberServices>(servicesToSuspend), sub,  new HashMap<ExternalAppEnum, ProvisionAgentException>());
			parseProvisioningResult(ctx, resultCodes, sub, null);
		}
		catch (final Exception e)
		{
			LogSupport.major(ctx, this, "Exception caught when suspending services " + serviceIds.toString()
					+ " for subscriber " + sub.getId(), e);
		}
	}

	/**
	 * @param ctx
	 * @param sub
	 * @param servicesToSuspend
	 */
	private void handleSuspensionDueToCLTC(Context ctx, Subscriber sub,
			Collection<SubscriberServices> servicesToSuspend) {

		final List<Long> serviceIds = getServiceIdList(servicesToSuspend);
		try
		{
			LogSupport.info(ctx, this, " Suspending services Due To CLTC" + serviceIds.toString());

			ServiceCreditLimitCheckSupport.createSubscriberNoteCltcProvision(ctx, sub, Collections.EMPTY_LIST,
					servicesToSuspend);
			// Update those Subscriber Service records to suspended due to CLTC
			ServiceCreditLimitCheckSupport.changeServiceStateDueToCLTC(ctx, sub, servicesToSuspend);

		}
		catch (final Exception e)
		{
			LogSupport.major(ctx, this, "Exception caught when suspending services Due to CLTC" + serviceIds.toString()
					+ " for subscriber " + sub.getId(), e);
		}

	}


	/**
	 * Provisions subscriber services which failed earlier.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *            The subscriber to be provisioned.
	 * @param services
	 *            The services to be provisioned for the subscriber.
	 */
	private void handleProvisioning(final Context ctx, final Subscriber sub, final Collection<SubscriberServices> services)
	{
		final List<Long> serviceIds = getServiceIdList(services);

		try
		{
			LogSupport.info(ctx, this, " Provisioning services " + serviceIds.toString());
			final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
			SubscriberServicesSupport.provisionSubscriberServices(ctx, null, new ArrayList<SubscriberServices>(services), sub, resultCodes);
			parseProvisioningResult(ctx, resultCodes, sub, null);
		}
		catch (final Exception e)
		{
			LogSupport.major(ctx, this, "Exception caught when provisioning services " + serviceIds.toString()
					+ " for subscriber " + sub.getId(), e);
		}

	}


	/**
	 * Provisions subscriber services which failed earlier.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *            The subscriber to be provisioned.
	 * @param services
	 *            The services to be provisioned for the subscriber.
	 */
	private void handleResume(final Context ctx, final Subscriber sub, final Collection<SubscriberServices> services)
	{
		final List<Long> serviceIds = getServiceIdList(services);

		try
		{
			LogSupport.info(ctx, this, " Resuming services " + serviceIds.toString());
			final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
			SubscriberServicesSupport.resumeSubscriberServices(ctx, null, new ArrayList<SubscriberServices>(services), sub, resultCodes);
			//SubscriberServicesProvisioningHome.parseProvisioningResult(ctx, resultCodes, sub, null);
		}
		catch (final Exception e)
		{
			LogSupport.major(ctx, this, "Exception caught when resuming services " + serviceIds.toString()
					+ " for subscriber " + sub.getId(), e);
		}

	}
	
	/**
	 * Unprovision subscriber services.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *            The subscriber to be provisioned.
	 * @param services
	 *            The services to be provisioned for the subscriber.
	 */
	private void handleUnProvisioning(final Context ctx, final Subscriber sub,
			final Collection<SubscriberServices> services)
	{
		final List<Long> serviceIds = getServiceIdList(services);
		try
		{
			LogSupport.info(ctx, this, " Unprovisioning services " + serviceIds.toString());
			
			final Map<ExternalAppEnum, ProvisionAgentException> resultCodes = new HashMap<ExternalAppEnum, ProvisionAgentException>();
			SubscriberServicesSupport.unprovisionSubscriberServices(ctx, sub, new ArrayList<SubscriberServices>(services), null, resultCodes);
			parseProvisioningResult(ctx, resultCodes, sub, null);
		}
		catch (final Exception e)
		{
			LogSupport.major(ctx, this, "Exception caught when unprovisioning services " + serviceIds.toString()
					+ " for subscriber " + sub.getId(), e);
		}
	}

	/**
     * Refund the services.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber to be refund.
     * @param services
     *            The services the subscriber is refund for.
     */
    private void handleRefund(final Context ctx, final Subscriber sub, 
            final Map<Long, SubscriberServices> provisionedServices)
    {
        for (SubscriberServices subService: provisionedServices.values())
        {
            if(!isServiceRefunded(ctx, subService)){
                CrmCharger charger = new SubscriberServiceCharger(sub, subService);
                // use default handler
                charger.refund(ctx, null);    
            }
        }
    }
    /*
     * Currently we implemented logic that refund is possible when unprovision service done successfully otherwise not. 
     * and service state is "faild to unprove". before this logic implementation
     * refund is done if service state is "faild to unprove" or "unprovision"  (i.e change service from priceplan). 
     * this logic want to implement because when this code go to live at server, 
     * the "faild to unprove" service start to refund which already done when try to Unprovision service from GUI.
     */
    private boolean isServiceRefunded(Context ctx, SubscriberServices subService)
    {
        try
        {
            Home home = (Home) ctx.get(SubscriberSubscriptionHistoryHome.class);
            And and = new And();
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.SUBSCRIBER_ID, subService.getSubscriberId()));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.EVENT_TYPE, HistoryEventTypeEnum.REFUND));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_TYPE, ChargedItemTypeEnum.SERVICE));
            and.add(new EQ(SubscriberSubscriptionHistoryXInfo.ITEM_IDENTIFIER, subService.getServiceId()));
            and.add(new Limit(1));
            Home orderHome = new OrderByHome(ctx, new OrderBy(SubscriberSubscriptionHistoryXInfo.TIMESTAMP_,false), home); 
            
            Collection<SubscriberSubscriptionHistory> coll = orderHome.select(ctx, and);
            if(coll.size() > 0){
                SubscriberSubscriptionHistory history = coll.iterator().next();
                if(EventSuccessEnum.SUCCESS.equals(history.getEventSuccess()))
                {
                    return true;
                }
            }
        }
        catch (HomeException e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.minor(ctx, this, e);
            }
        }
        return false;
    }
    
	/**
	 * Charges the services.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param sub
	 *            The subscriber to be charged.
	 * @param services
	 *            The services the subscriber is charged for.
	 */
	private void handleCharging(final Context ctx, final Subscriber sub, 
			final Map<Long, SubscriberServices> provisionedServices)
	{
		for (SubscriberServices subService: provisionedServices.values())
		{
			CrmCharger charger = new SubscriberServiceCharger(sub, subService);
			// use default handler
			charger.charge(ctx, null);

			/**
			 * Check if services are moved to suspended state due to low balance and if Restrict provisioning flag is enabled then 
			 * move the service to unprovision state.
			 */
			Collection<SubscriberServices> suspendeServices = SubscriberServicesSupport.getSuspendedServices(ctx, sub.getId());

			if (subService.getService(ctx).isRestrictProvisioning()){
				for (SubscriberServices suspendService : suspendeServices){
					
					if (suspendService.getServiceId() == subService.getServiceId()){
						ArrayList<SubscriberServices> toUnprovision  = new ArrayList<SubscriberServices>();
						toUnprovision.add(suspendService);
						try {
							if (sub.isPrepaid()){
								SubscriberServicesSupport.unprovisionSubscriberServices(ctx, sub, toUnprovision, null, new HashMap<ExternalAppEnum, ProvisionAgentException>());
							} else if (sub.isPostpaid() && subService.getService(ctx).getChargeScheme() == ServicePeriodEnum.ONE_TIME){
								SubscriberServicesSupport.unprovisionSubscriberServices(ctx, sub, toUnprovision, null, new HashMap<ExternalAppEnum, ProvisionAgentException>());
							}
						}
						catch (HomeException e){
							if (LogSupport.isDebugEnabled(ctx)){
								LogSupport.debug(ctx, this, e);
							}
						}
						break;//because we are processing only one service at a time
					}
				}
			}
		}
	}


	/**
	 * Returns the set of service IDs from a collection of subscriber services.
	 *
	 * @param services
	 *            A collection of subscriber services.
	 * @return A set of service IDs corresponding to the the services.
	 */
	private List<Long> getServiceIdList(final Collection<SubscriberServices> services)
	{
		final List<Long> serviceIds = new ArrayList<Long>();
		for (final SubscriberServices subscriberServices : services)
		{
			serviceIds.add(Long.valueOf(subscriberServices.getServiceId()));
		}
		return serviceIds;
	}


	/**
	 * Returns the services which should be provisioned.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param subId
	 *            Subscriber ID.
	 * @return Set of services to be started.
	 */
	private Collection<SubscriberServices> getServicesToProvision(final Context ctx, final String subId)
	{
		Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
			filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONEDWITHERRORS));
			filter.add(new NEQ(SubscriberServicesXInfo.SUSPEND_REASON, SuspendReasonEnum.CLCT));

			result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
		}catch (HomeException exception) {
			LogSupport.minor(ctx, SubscriberServicesSupport.class,
					"Error getting services to be provisioned for subscriber " + subId, exception);
		}

		return result;
	}
	
	/**
	 * Returns the services which should be provisioned.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param subId
	 *            Subscriber ID.
	 * @return Set of services to be started.
	 */
	private Collection<SubscriberServices> getServicesToResume(final Context ctx, final String subId)
	{
		Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
			filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONEDWITHERRORS));
			filter.add(new EQ(SubscriberServicesXInfo.SUSPEND_REASON, SuspendReasonEnum.CLCT));

			result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
		}catch (HomeException exception) {
			LogSupport.minor(ctx, SubscriberServicesSupport.class,
					"Error getting services to be resume for subscriber " + subId, exception);
		}

		return result;
	}


	/**
	 * Returns the services which should be unprovisioned.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param subId
	 *            Subscriber ID.
	 * @return Set of services to be started.
	 */
	private Collection<SubscriberServices> getServicesToUnProvision(final Context ctx, final String subId)
	{
		Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
			filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONEDWITHERRORS));

			result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
		}catch (HomeException exception) {
			LogSupport.minor(ctx, SubscriberServicesSupport.class,
					"Error getting services to be unprovisioned for subscriber " + subId, exception);
		}

		return result;
	}

	/**
	 * Returns the services which should be suspended.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param subId
	 *            Subscriber ID.
	 * @return Set of services to be started.
	 */
	private Collection<SubscriberServices> getServicesToSuspend(final Context ctx, final String subId)
	{
		Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
			filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.SUSPENDEDWITHERRORS));
			filter.add(new NEQ(SubscriberServicesXInfo.SUSPEND_REASON, SuspendReasonEnum.CLCT));

			result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
		}catch (HomeException exception) {
			LogSupport.minor(ctx, SubscriberServicesSupport.class,
					"Error getting services to be suspended for subscriber " + subId, exception);
		}

		return result;
	}
	
	
	/**
	 * Returns the services which should be suspended.
	 *
	 * @param ctx
	 *            The operating context.
	 * @param subId
	 *            Subscriber ID.
	 * @return Set of services to be started.
	 */
	private Collection<SubscriberServices> getServicesToSuspendDueToCLTC(final Context ctx, final String subId)
	{
		Collection<SubscriberServices> result = new ArrayList<SubscriberServices>();
		try
		{
			final And filter = new And();
			filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId));
			filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.SUSPENDEDWITHERRORS));
			filter.add(new EQ(SubscriberServicesXInfo.SUSPEND_REASON, SuspendReasonEnum.CLCT));

			result = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, filter);
		}catch (HomeException exception) {
			LogSupport.minor(ctx, SubscriberServicesSupport.class,
					"Error getting services to be suspended for subscriber " + subId, exception);
		}

		return result;
	}

	/**
	 * Returns the subset of services which were successfully provisioned.
	 *
	 * @param sub
	 *            The subscriber provisioned.
	 * @param services
	 *            The set of services attempted for provisioning.
	 * @return The subset of services which were successfully provisioned.
	 */
	private Map<Long, SubscriberServices> getProvSuccessServices(final Context ctx, final Subscriber sub,
			final Collection<SubscriberServices> services)
		{
			final Map<Long, SubscriberServices> successful = new HashMap<Long, SubscriberServices>();
			final Set<Long> set = sub.getProvisionedServices(ctx);
		
			for (final SubscriberServices subService : services)
			{
				final Long serviceID = Long.valueOf(subService.getServiceId());
				if (set.contains(serviceID))
				{
					// the service has got provisioned properly
					successful.put(serviceID, subService);
				}
			}
			return successful;
		}
	
	/**
     * Returns the subset of services which were successfully unprovisioned.
     *
     * @param sub
     *            The subscriber
     * @param services
     *            The set of services attempted for unprovisioning.
     * @return The subset of services which were successfully unprovisioned.
     */
    private Map<Long, SubscriberServices> getUnProvSuccessServices(final Context ctx, final Subscriber sub,
            final Collection<SubscriberServices> services)
        {
            final Map<Long, SubscriberServices> subServices = new HashMap<Long, SubscriberServices>();
            
            final Set<Long> set = (Set)SubscriberServicesSupport.getUnprovisionedServices(ctx, sub.getId());
            
            for (final SubscriberServices subService : services)
            {
                final Long serviceID = Long.valueOf(subService.getServiceId());
                if (set.contains(serviceID))
                {
                    subServices.put(serviceID, subService);
                }
            }
            return subServices;
        }
    
	 /**
     * Put the ProvisionAgentException in the GUI, and generate an OM Log Message for it.
     * @param ctx
     * @param exception
     * @param oldSub
     * @param newSub
     * @return
     */
    private static int logProvisioningExceptions(final Context ctx, final ProvisionAgentException exception,
            final Subscriber oldSub, final Subscriber newSub)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to provision ");
        sb.append(getServiceTypeName(exception.getExternalApp()));
        sb.append("service to ");
        sb.append(getModuleName(exception.getExternalApp()));
        sb.append(": ");
        sb.append(exception.getMessage());
        
        if (LogSupport.isDebugEnabled(ctx)){
        	LogSupport.debug(ctx, sb.toString(), exception);
        }
                
        int result = exception.getSourceResultCode();
        
        if (result!=0)
        {
            //setProvisionErrorCode(ctx, result, exception.getExternalApp());
            result = exception.getResultCode();
            String measurementName = getOMMeasurementName(exception.getExternalApp());
            new OMLogMsg(Common.OM_MODULE, measurementName).log(ctx);
        }
        
        return result;
    }
    
    /**
     * Return module name.
     * @param key
     * @return
     */
    private static String getModuleName(final ExternalAppEnum externalApp)
    {
        String result = "UNKNOWN";
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = "URCS";
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = "URCS";
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = "NGRC";
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            result = "RIM Blackberry Provisioning System";
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = "HLR";
        }
        else if (ExternalAppEnum.VOICEMAIL.equals(externalApp))
        {
            result = "Voicemail server";
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            result = "Alcatel SSC";
        }

        return result;
    }
    

    /**
     * Return module name.
     * @param key
     * @return
     */
    private static String getServiceTypeName(final ExternalAppEnum externalApp)
    {
        String result = "";
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = "SMS ";
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = "voice ";
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = "data ";
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            result = "Blackberry ";
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = "";
        }
        else if (ExternalAppEnum.VOICEMAIL.equals(externalApp))
        {
            result = "voicemail ";
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            result = "";
        }

        return result;
    }
    
    private static String getOMMeasurementName(final ExternalAppEnum externalApp)
    {
    	String result = Common.OM_CRM_PROV_ERROR;
        
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            result = Common.OM_SMSB_ERROR;
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            result = Common.OM_ECP_ERROR;
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            result = Common.OM_IPC_ERROR;
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            result = Common.OM_HLR_PROV_ERROR;
        }

        return result;
    }
    
    
    /**
     * Set the provision error code for the given key.
     * @param ctx
     * @param resultCode
     * @param key
     */
    private static void setProvisionErrorCode(final Context ctx, final int resultCode, final ExternalAppEnum externalApp)
    {
        if (ExternalAppEnum.SMS.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionSMSBErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.VOICE.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionEcpErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.DATA.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionIpcErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.BLACKBERRY.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionBlackberryErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.ALCATEL_SSC.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionAlcatelErrorCode(ctx, resultCode);
        }
        else if (ExternalAppEnum.HLR.equals(externalApp))
        {
            SubscriberProvisionResultCode.setProvisionHlrResultCode(ctx, resultCode);
        }
    }
    
    /**
     * Parse the provisioning result
     * @param ctx
     * @param resultCodes
     * @param oldSub
     * @param newSub
     */
    private static void parseProvisioningResult(final Context ctx,
            final Map<ExternalAppEnum, ProvisionAgentException> resultCodes, final Subscriber oldSub, final Subscriber newSub)
    {        
        for (ProvisionAgentException exception : resultCodes.values())
        {
             logProvisioningExceptions(ctx, exception, oldSub, newSub);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public void execute(Context ctx) throws AgentException {
		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
		visit (ctx, sub);
	}
	

    void prepareProvisionResultCode(final Context ctx, final Subscriber newSub)
    {
        ProvisionResultCode el = new ProvisionResultCode();
        ctx.put(ProvisionResultCode.class, el);
        // newSub.setProvisionResultCode(el);
    }
}
