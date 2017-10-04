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
package com.trilogy.app.crm.bulkloader.generic.bean;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.bean.ServiceFee2;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionXInfo;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;

/**
 * 
 * 
 * @author sanjay.pagar
 * @since
 */

public class FindSubscriberServicesAdapterHome extends HomeProxy {

	private static final long serialVersionUID = 1L;

	FindSubscriberServicesAdapterHome(Context ctx) {
		super(ctx);
	}

	public SubscriberServices find(Context ctx, Object obj)
			throws HomeException {

		Subscriber sub = null;
		long serviceID;
		int spid ;
		SubscriberServices subscriberService = null;
		String Operation = null;

		if (obj instanceof SearchableSubscriberServices) {
			SearchableSubscriberServices criteria = (SearchableSubscriberServices) obj;

			Operation = criteria.getOperation();
			serviceID = criteria.getServiceID();
			spid = criteria.getSPID();
			try {

				sub = SubscriberSupport.lookupSubscriberForSubId(ctx,
						criteria.getSearchSubId());

				if (sub == null) {
					sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx,
							criteria.getSearchMsisdn());
				}

				if (sub == null)
				{
					throw new HomeException("Failed to find the Subscriber:- "+ criteria.getSearchSubId() +" using the SearchableSubscriberServices pipeline. ");
				}
				if(spid != sub.getSpid())
				{
					throw new IllegalArgumentException("Invalid Service provider:-" + spid);
				}
				

			} catch (HomeException e) {
				throw new HomeException(
						"Failed to find the Subscriber using the SearchableSubscriberServices pipeline. "
								+ e.getMessage(), e);
			}

			if (sub != null) {
				ServiceFee2 fee = null;
				long pricePlanID = 0;

				PricePlanVersion subscriberPPV = sub.getPricePlan(ctx);
				if (subscriberPPV != null) {
					pricePlanID = subscriberPPV.getId();

					PricePlan CurrentPricePlan = PricePlanSupport.getPlan(ctx,
							pricePlanID);

					int currentVersion = CurrentPricePlan.getCurrentVersion();

					And and = new And();
					and.add(new EQ(PricePlanVersionXInfo.ID, pricePlanID));
					and.add(new EQ(PricePlanVersionXInfo.VERSION,
							currentVersion));

					Home home = (Home) ctx.get(PricePlanVersionHome.class);
					PricePlanVersion ppv = (PricePlanVersion) home.find(ctx,
							and);
					ServicePackageVersion versionPackages = ppv
							.getServicePackageVersion();

					final Collection collServices = ppv
							.getServicePackageVersion().getServiceFees()
							.values();

					if (collServices != null) {
						for (final Iterator i = collServices.iterator(); i
								.hasNext();) {
							final ServiceFee2 service = (ServiceFee2) i.next();
							if (service.getServiceId() == serviceID) {
								fee = service;
								break;
							}
						}
					}
				} else {
					throw new IllegalArgumentException(
							"Failed to find the price plan for Subscriber:- " + sub.getId());
				}
				if (Operation.equalsIgnoreCase("ADD")) {
					if (fee != null) {
						subscriberService = SubscriberServicesSupport
								.getSubscriberServiceRecord(ctx, sub.getId(),
										serviceID, fee.getPath());
						if (subscriberService == null) {
							subscriberService = new SubscriberServices();
							Service subService = ServiceSupport.getService(ctx, serviceID);
							subscriberService.setService(subService);
							subscriberService.setServiceId(serviceID);
							subscriberService.setSubscriberId(sub.getId());
							addOldSubscriberToContext(ctx, sub);
							addServiceToContext(ctx, subscriberService);
							return subscriberService;
						} else {
							throw new IllegalArgumentException(serviceID + " " +
									"Service already provisioned to subscriber :- " + sub.getId());
						}
					} else {
						throw new IllegalArgumentException("Invalid serviceID:-" + serviceID);
					}

				} else {
					if (Operation.equalsIgnoreCase("REMOVE")) {
						// this is for remove
						if (fee != null) {
							subscriberService = SubscriberServicesSupport
									.getSubscriberServiceRecord(ctx,
											sub.getId(), serviceID, fee.getPath());
							if (subscriberService != null) {
								if (fee.getServicePreference() != ServicePreferenceEnum.MANDATORY) 
								{
									addOldSubscriberToContext(ctx, sub);
									addServiceToContext(ctx, subscriberService);
									return subscriberService;
								} 
								else 
								{
									throw new IllegalArgumentException(
											"cannot remove mandatory service :-" + serviceID);
								}
							} else {

								throw new IllegalArgumentException(
										"Service not provisioned to subscriber. Service Id: "
												+ serviceID);
							}
						} else {
							throw new IllegalArgumentException("Invalid serviceID :-" + serviceID);
						}
					} else {
						throw new IllegalArgumentException("Invalid operation:- " + Operation);
					}
				}
			}
		}
		return subscriberService;
	}
	
	 /**
     * Adds old subscriber to context for provisioning and charging
     *
     * @param subCtx
     *            The operating context.
     * @param subscriber
     *            the old subscriber.
     */
	 private void addOldSubscriberToContext(Context subCtx, Subscriber subscriber) throws HomeException
	    {
	        try
	        {
	            Subscriber oldSubscriber = (Subscriber) subscriber.deepClone();
	            oldSubscriber.setContext(subCtx);
	            oldSubscriber.getSuspendedBundles(subCtx);
	            oldSubscriber.getSuspendedPackages(subCtx);
	            oldSubscriber.getSuspendedAuxServices(subCtx);
	            oldSubscriber.getSuspendedServices(subCtx);
	            oldSubscriber.getCLTCServices(subCtx);
	            oldSubscriber.getAuxiliaryServices(subCtx);	            
	            oldSubscriber.getServices(subCtx);
	            oldSubscriber.resetProvisionedAuxServiceIdsBackup();
	            oldSubscriber.resetProvisionedAuxServiceBackup();
	            oldSubscriber.getProvisionedAuxServiceBackup(subCtx);
	            
	            oldSubscriber.resetBackupServices();
	            oldSubscriber.resetProvisionedServiceBackup();
	            oldSubscriber.getProvisionedServicesBackup(subCtx);
	            
	            	            
	            oldSubscriber.freeze();
	            subCtx.put(Lookup.OLD_FROZEN_SUBSCRIBER, oldSubscriber);
	        }
	        catch (CloneNotSupportedException e)
	        {
	            // should not happen.
	        	new DebugLogMsg(this, "FindSubscriberServicesAdapterHome doesn't allow the Clone operation" 
	                    + subscriber.getId(),null).log(subCtx);
	        }
	    }	    
	 
	 /**
	     * Adds Service to context for provisioning and charging
	     *
	     * @param ctx
	     *            The operating context.
	     * @param subscriberServices
	     *            the old SubscriberServices.
	     */
	 private void addServiceToContext(Context ctx, SubscriberServices obj)
	 {
	        final Service service = obj.getService(ctx);
	        
	        //ctx.put(Service.class, service);
	        ctx.put(com.redknee.app.crm.bean.Service.class, service);

	 }

	public Object create(Context ctx, Object obj) throws HomeException {
		throw new UnsupportedOperationException(
				"FindSubscriberServicesAdapterHome doesn't allow the Home.create operation.");
	}

	public Object store(Context ctx, Object obj) throws HomeException {
		throw new UnsupportedOperationException(
				"FindSubscriberServicesAdapterHome doesn't allow the Home.store operation.");
	}

	public void remove(Context ctx, Object obj) throws HomeException {
		throw new UnsupportedOperationException(
				"FindSubscriberServicesAdapterHome doesn't allow the Home.remove operation.");

	}

	public Collection select(Context ctx, Object obj) throws HomeException {
		throw new UnsupportedOperationException(
				"FindSubscriberServicesAdapterHome doesn't allow the Home.select operation.  Use the Home.find operartion.");

	}
}
