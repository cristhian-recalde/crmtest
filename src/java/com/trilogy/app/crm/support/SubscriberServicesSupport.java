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
package com.trilogy.app.crm.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.bas.recharge.RechargeConstants;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePackageHome;
import com.trilogy.app.crm.bean.ServicePackageXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesID;
import com.trilogy.app.crm.bean.SubscriberServicesXDBHome;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SuspendReasonEnum;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.provision.ProvisioningSupport;
import com.trilogy.app.crm.provision.SkipProvisioningException;
import com.trilogy.app.crm.provision.gateway.SPGSkippingException;
import com.trilogy.app.crm.refactoring.ServiceRefactoring_RefactoringClass;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.util.SubscriberServicesUtil;
import com.trilogy.framework.auth.AuthMgr;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.EQDay;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.elang.Not;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author rajith.attapattu@redknee.com
 */
public class SubscriberServicesSupport
{
    /**
     * Return a map of the (Service IDs, SubscriberServices) associated to the 
     * given Subscriber identifier.
     * @param ctx
     * @param subscriberId
     * @return
     */    
    public static Map<ServiceFee2ID, SubscriberServices> getSubscribersServices(final Context ctx, final String subscriberId)
    {
        return getAllSubscribersServices(ctx, subscriberId, false);
        
    }
    
    
    /**
     * Return a map of the (Service IDs, SubscriberServices) associated to the given
     * Subscriber identifier.
     * 
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Map<ServiceFee2ID, SubscriberServices> getAllSubscribersServices(final Context ctx, final String subscriberId,
            boolean includeUnprovision)
    {
    	if(LogSupport.isDebugEnabled(ctx))
    	{
    		LogSupport.debug(ctx, SubscriberServicesSupport.class.getName(), "[getAllSubscribersServices]subscriberId:" + subscriberId + " includeUnprovision:" + includeUnprovision);
    	}
    	return getAllSubscribersServices(ctx, subscriberId, includeUnprovision, null);
    }
    
    
    /**
     * 
     * @param ctx
     * @param subscriberId
     * @param includeUnprovision
     * @param nextRecurringChargeDate : this field is introduced specifically for Multi-Day services
     * @return
     */
    public static Map<ServiceFee2ID, SubscriberServices> getAllSubscribersServices(final Context ctx, final String subscriberId,
            boolean includeUnprovision, Date nextRecurringChargeDate)
    {
        if(LogSupport.isDebugEnabled(ctx))	
        {
    	   LogSupport.debug(ctx, SubscriberServicesSupport.class.getName(), "[getAllSubscribersServices]subscriberId:" + subscriberId 
    			   + " includeUnprovision:" + includeUnprovision + " nextRecurringChargeDate:" + nextRecurringChargeDate);
        }
    	
        
    	final Map<ServiceFee2ID, SubscriberServices> map = new HashMap<ServiceFee2ID, SubscriberServices>();
        if (subscriberId != null && subscriberId.trim().length() > 0)
        {
            try
            {
                final Home h = (Home) ctx.get(SubscriberServicesHome.class);
                final EQ subscriberPred = new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId);
                And condition = new And().add(subscriberPred);
                if (!includeUnprovision)
                {
                    final Predicate notInState = new Not(getUnProvisionedSubscriberServicesPredicate());
                    condition.add(notInState);
                }
                if(nextRecurringChargeDate != null && ctx.getBoolean(RecurringRechargeSupport.NOTIFICATION_ONLY))
                {
                	condition.add(new EQ(SubscriberServicesXInfo.NEXT_RECURRING_CHARGE_DATE, nextRecurringChargeDate));
                }
                else if ( nextRecurringChargeDate != null )
                {
                	LTE lte = new LTE(SubscriberServicesXInfo.NEXT_RECURRING_CHARGE_DATE,nextRecurringChargeDate);
                	condition.add(lte);                	
                }
                final Collection col = h.select(ctx, condition);
                for (final Iterator it = col.iterator(); it.hasNext();)
                {
                    final SubscriberServices bean = (SubscriberServices) it.next();
                    map.put(new ServiceFee2ID(bean.getServiceId(),bean.getPath()), bean);
                }
            }
            catch (final Exception e)
            {
                new MinorLogMsg(SubscriberServicesSupport.class.getName(), "Error getting services for subscriber ", e)
                        .log(ctx);
            }
        }
        return map;
    }
    
    
    /**
	 * Return a map of the all (even unprovisioned one's) (Service IDs, SubscriberServices) associated to the 
	 * given Subscriber identifier.
	 * @param ctx
	 * @param subscriberId
	 * @return
	 */    
	public static Map<ServiceFee2ID, SubscriberServices> getAllSubscribersServicesRecords(final Context ctx, final String subscriberId, boolean flag)
	{
		final Map<ServiceFee2ID, SubscriberServices> map = new HashMap<ServiceFee2ID, SubscriberServices>();
		Collection<SubscriberServices> subscriberServices=null;
		if (subscriberId != null && subscriberId.length() > 0)
		{
			try
			{

				Home h = (Home)ctx.get(RechargeConstants.SUBSCRIBER_SERVICES_HOME);
				if(h == null)
				{
					subscriberServices = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class,
							new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
				}else
				{
					final EQ subscriberPred = new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId);
					subscriberServices=h.select(ctx, subscriberPred);
				}
				for (SubscriberServices bean : subscriberServices)
				{
					map.put(new ServiceFee2ID(Long.valueOf(bean.getServiceId()),bean.getPath()), bean);
				}
			}
			catch (final Exception e)
			{
				new MinorLogMsg(SubscriberServicesSupport.class.getName(), "Error getting services for subscriber ", e)
				.log(ctx);
			}
		}
		return map;
	}
    
    /**
     * Return a map of the all (even unprovisioned one's) (Service IDs, SubscriberServices) associated to the 
     * given Subscriber identifier.
     * @param ctx
     * @param subscriberId
     * @return
     */    
    public static Map<ServiceFee2ID, SubscriberServices> getAllSubscribersServicesRecords(final Context ctx, final String subscriberId)
    {
        final Map<ServiceFee2ID, SubscriberServices> map = new HashMap<ServiceFee2ID, SubscriberServices>();
        if (subscriberId != null && subscriberId.length() > 0)
        {
            try
            {
                Collection<SubscriberServices> subscriberServices = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class,
                        new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
                for (SubscriberServices bean : subscriberServices)
                {
                    map.put(new ServiceFee2ID(bean.getServiceId(), bean.getPath()), bean);
                }
            }
            catch (final Exception e)
            {
                new MinorLogMsg(SubscriberServicesSupport.class.getName(), "Error getting services for subscriber ", e)
                        .log(ctx);
            }
        }
        return map;
    }
    
    /**
     * Return the "provisioned" SubscriberServices IDs for the given subscriber. 
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Collection getServicesEligibleForProvisioning(final Context ctx, final String subscriberId)
    {
        final Home h = (Home) ctx.get(SubscriberServicesHome.class);
        final List serviceIds = new ArrayList();
        
        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            condition.add(getEligibleSubscriberServicesPredicate());
            final Collection col = h.select(ctx, condition);

            for (final Iterator it = col.iterator(); it.hasNext();)
            {
                serviceIds.add(Long.valueOf(((SubscriberServices) it.next()).getServiceId()));
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }

        return serviceIds;
    }
    
    /**
     * Need list of eligible SubscriberServices that may get provisioned in near future.
     * Couldn't resuse getServicesEligibleForProvisioning() because of limitation of returning only service ids
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Map<com.redknee.app.crm.bean.core.ServiceFee2, SubscriberServices> getSubscribedSubscriberServicesMap(final Context ctx, final Subscriber subscriber)
    {
    	Collection <SubscriberServices> subServices = null;
    	Map <com.redknee.app.crm.bean.core.ServiceFee2, SubscriberServices> eligibleSubServices = new HashMap();
    	final Home h = (Home) ctx.get(SubscriberServicesHome.class);
        
        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriber.getId()));
            condition.add(getEligibleSubscriberServicesPredicate());
            subServices = h.select(ctx, condition);
            
            Map<ServiceFee2ID, ServiceFee2> serviceFeesMap = SubscriberServicesSupport.getServiceFees(ctx, subscriber);
            
            for (final Iterator<SubscriberServices> it = subServices.iterator(); it.hasNext();)
            {
            	SubscriberServices subSrvc = it.next();
            	eligibleSubServices.put(serviceFeesMap.get(subSrvc.getServiceId()), subSrvc);
            }
        }
        catch(final Exception e)
        {
        	new MajorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }
        return eligibleSubServices;
    }

    /*
     * I have no choice but to have this method so we are backward compatible
     * (who are you?) rattapattu
     *
     * The purpose of this method is as follows. We need to get chargable
     * service for the subscriber and it only takes one sql querry to look up
     * the SubscriberServices table.
     *
     * Unfortunately this was only introduced from 6.00 onwards so to be
     * backward compatible we chose the non chargeable optional services and
     * exclude them from the getServices method of the subscriber.
     *
     * Again we can't use the getProvisioned services method bcos some of the
     * mandatory services like voice maybe un-provisioned due to credit limit
     * isues but we still have to charge it.
     *
     * It's much more efficient to cache the whole home and walk it with a
     * visitor than to do a new SQL statement every time we need this.
     * [psperneac]
     *
     * No chaching will not work. If there are 100k subscribers, then there will
     * be 200k plus records assuming voice and sms for each, not counting any
     * additional services. So how are u going to cache the whole home. I do not
     * know why you did the visitor thing Sql is a lot faster than java
     * code[rattapattu] *
     */
    public static Collection getSubscriberNonChargableOptionalServices(
            final Context ctx, final String subscriberId)
    {
        final Home h = (Home) ctx.get(SubscriberServicesHome.class);

        final Collection ret = new ArrayList();

        try
        {
            final long now = System.currentTimeMillis();

            h.where(ctx, "subscriberId='" + subscriberId + "'").forEach(ctx,
                    new Visitor()
                    {
                        public void visit(final Context ctx, final Object obj)
                        {
                            SubscriberServices services = (SubscriberServices) obj;

                            if (services.getStartDate().getTime() > now
                                    || (services.getEndDate().getTime() < now && !services.isMandatory())
                                    || services.getProvisionedState() == ServiceStateEnum.PROVISIONEDWITHERRORS)
                            {
                                ret.add(Long.valueOf(services.getServiceId()));
                            }
                        }
                    });
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }

        return ret;
    }

    /**
     * Return a collection of optional, "provisioned" SubscriberServices for the given Subscriber,
     * who's start day is today.
     * @param ctx
     * @param newSub
     * @return
     */
    public static Collection<Long> getOptionalServicesStartingToday(final Context ctx, final Subscriber newSub)
    {
        final String subscriberId = newSub.getId();
        final Collection<Long> serviceIds = new ArrayList<Long>();

        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            condition.add(new EQDay(SubscriberServicesXInfo.START_DATE, new Date()));
            // exclude services that are already provisioned
            condition.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PENDING));

            condition.add(new EQ(SubscriberServicesXInfo.MANDATORY, Boolean.FALSE));

            final Collection<SubscriberServices> col = HomeSupportHelper.get(ctx).getBeans(ctx, SubscriberServices.class, condition);

            for (SubscriberServices service : col)
            {
                serviceIds.add(Long.valueOf(service.getServiceId()));
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }

        return serviceIds;
    }

    /**
     * Return a collection SubscriberServices IDs for the given Subscriber,
     * who's end date is today.
     * @param ctx
     * @param subscriberId
     * @param TRUE if we want only the collection of "provisioned" Subscriber Services
     * @return
     */
    public static Collection getOptionalServicesIDsEndingToday(final Context ctx, 
            final String subscriberId,
            final boolean onlyProvisioned)
    {
        final Home h = (Home) ctx.get(SubscriberServicesHome.class);
        final List serviceIds = new ArrayList();

        try
        {
            final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
            final Date tommarrow = CalendarSupportHelper.get(ctx).findDatesAfter(1);

            
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            condition.add(new EQ(SubscriberServicesXInfo.MANDATORY,"n"));
            condition.add(new GTE(SubscriberServicesXInfo.END_DATE, today));
            condition.add(new LTE(SubscriberServicesXInfo.END_DATE, tommarrow));


            final Collection col = h.where(ctx, condition).selectAll(ctx);

            for (final Iterator it = col.iterator(); it.hasNext();)
            {
                serviceIds.add(Long.valueOf(((SubscriberServices) it.next()).getServiceId()));
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }
        return serviceIds;
    }

    /**
     * Return a Map of ServiceFees for the given Subscriber.
     * @param ctx
     * @param sub
     * @return
     */
    public static Map<ServiceFee2ID, ServiceFee2> getServiceFees(final Context ctx, final Subscriber sub)
    {
        Map<ServiceFee2ID, ServiceFee2> serviceFees = null;
        try
        {
            final PricePlanVersion plan = sub.getRawPricePlanVersion(ctx);
            if (plan != null)
            {
                serviceFees = plan.getServiceFees(ctx);
            }
        }
        catch (final Exception e)
        {
            new DebugLogMsg(SubscriberServicesSupport.class, "Encountered a Exception during getServiceFees() call. [message=" + e.getMessage() + "]", e).log(ctx);
        }

        final Map<ServiceFee2ID, ServiceFee2> serviceFeesMap;
        if (serviceFees == null)
        {
            serviceFeesMap = new HashMap<ServiceFee2ID, ServiceFee2>();
        }
        else
        {
            serviceFeesMap = new HashMap<ServiceFee2ID, ServiceFee2>(serviceFees);
        }

        return serviceFeesMap;
    }

    /**
     * Returned a collection of Mandatory Subscriber Services for the given
     * Subscriber identifier (regardless of their state) 
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Collection getMandatoryServicesId(final Context ctx, final String subscriberId)
    {
        return getMandatoryServices(ctx, subscriberId, true);
    }

    /**
     * Returned a collection of Mandatory Subscriber Services for the given
     * Subscriber identifier (regardless of their state) 
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Collection<SubscriberServices> getMandatorySubscriberServices(final Context ctx, final String subscriberId)
    {
        return getMandatoryServices(ctx,subscriberId,false);
    }
    

    private static Collection getMandatoryServices( final Context ctx, final String subscriberId, 
            boolean returnIds)
    {
        final Home h = (Home) ctx.get(SubscriberServicesHome.class);
        final List services = new ArrayList();
        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            condition.add(new EQ(SubscriberServicesXInfo.MANDATORY, Boolean.TRUE));

            final Collection col = h.where(ctx, condition).selectAll(ctx);

            for (final Iterator it = col.iterator(); it.hasNext();)
            {
                if ( returnIds )
                {
                    services.add(Long.valueOf(((SubscriberServices) it.next()).getServiceId()));
                }
                else
                {
                    services.add(it.next());
                }
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting services for subscriber ", e).log(ctx);
        }

        return services;
    }
    
    
    /**
   	 * 
   	 * @param ctx
   	 * @param sub
   	 * @return
   	 */
   	public static Map<ServiceFee2ID, ServiceFee2> getServiceFeesWithSource(final Context ctx, final Subscriber sub, boolean inputBool)
   	{
   		final Map<ServiceFee2ID, ServiceFee2> serviceFeesMap = new HashMap<ServiceFee2ID, ServiceFee2>();
   		try
   		{
   			final PricePlanVersion ppv = sub.getRawPricePlanVersion(ctx);
   			if (ppv == null)
   			{
   				return serviceFeesMap;
   			}
   			final Collection serviceFees = ppv.getServicePackageVersion().getServiceFees().values();

   			Iterator it = serviceFees.iterator();
   			while (it.hasNext())
   			{
   				final ServiceFee2 serviceFee = (ServiceFee2) it.next();
   				long serviceId = Long.valueOf(serviceFee.getServiceId());
   				String path = serviceFee.getPath();
   				
   				serviceFeesMap.put(new ServiceFee2ID(serviceId, path), serviceFee);
   			}

   			final Map packageFees = ppv.getServicePackageVersion().getPackageFees();

   			Home home = (Home) ctx.get(ServicePackageHome.class);
   			home = home.where(ctx, new In(ServicePackageXInfo.ID, new HashSet(packageFees.keySet())));
   			/*
   			 * amedina : had to take out this line because remotely the Visitor works locally and
   			 * it doesn't return the objects created
   			 */
   			//home.forEach(new CollectServicesWithSource(serviceFeesMap, packageFees));

   			final Collection pkgs = home.selectAll();

   			it = pkgs.iterator();

   			final CollectServicesWithSource visitor = new CollectServicesWithSource(serviceFeesMap, packageFees);

   			while (it.hasNext())
   			{
   				visitor.visit(ctx, it.next());
   			}

   		}
   		catch (final HomeException e)
   		{
   			new MajorLogMsg(SubscriberServicesSupport.class, "Cannot access "
   					+ ServicePackageHome.class.getName() + " home!  Encountered a HomeException.", e).log(ctx);
   		}

   		return serviceFeesMap;
   	}
    
    public static Map<Long, ServiceFee2> getServiceFeesWithSource(final Context ctx, final Subscriber sub)
    {
        final Map<Long, ServiceFee2> serviceFeesMap = new HashMap<Long, ServiceFee2>();
        try
        {
            final PricePlanVersion ppv = sub.getRawPricePlanVersion(ctx);
            if (ppv == null)
            {
                return serviceFeesMap;
            }
            final Collection serviceFees = ppv.getServicePackageVersion().getServiceFees().values();

            Iterator it = serviceFees.iterator();
            while (it.hasNext())
            {
                final ServiceFee2 serviceFee = (ServiceFee2) it.next();
                serviceFeesMap.put(Long.valueOf(serviceFee.getServiceId()), serviceFee);
            }

            final Map packageFees = ppv.getServicePackageVersion().getPackageFees();

            Home home = (Home) ctx.get(ServicePackageHome.class);
            home = home.where(ctx, new In(ServicePackageXInfo.ID, new HashSet(packageFees.keySet())));
            /*
             * amedina : had to take out this line because remotely the Visitor works locally and
             * it doesn't return the objects created
             */
            //home.forEach(new CollectServicesWithSource(serviceFeesMap, packageFees));

            final Collection pkgs = home.selectAll();

            it = pkgs.iterator();

            final CollectServicesWithSource visitor = new CollectServicesWithSource(serviceFeesMap, packageFees);

            while (it.hasNext())
            {
                visitor.visit(ctx, it.next());
            }

        }
        catch (final HomeException e)
        {
            new MajorLogMsg(SubscriberServicesSupport.class, "Cannot access "
                    + ServicePackageHome.class.getName() + " home!  Encountered a HomeException.", e).log(ctx);
        }

        return serviceFeesMap;
    }

    public static Map<Long, ServiceFee2> getServiceFeesWithSource(final Context ctx, final ConvertAccountBillingTypeRequest conversion)
    {
        final Map<Long, ServiceFee2> serviceFeesMap = new HashMap<Long, ServiceFee2>();
        try
        {
            PricePlan pp = PricePlanSupport.getPlan(ctx, conversion.getPricePlan());
            if ( pp == null )
            {
                return serviceFeesMap;
            }
            
            final PricePlanVersion ppv = PricePlanSupport.getVersion(ctx, conversion.getPricePlan(), pp.getCurrentVersion());
            
            if (ppv == null)
            {
                return serviceFeesMap;
            }
            final Collection serviceFees = ppv.getServicePackageVersion().getServiceFees().values();

            Iterator it = serviceFees.iterator();
            while (it.hasNext())
            {
                final ServiceFee2 serviceFee = (ServiceFee2) it.next();
                serviceFeesMap.put(Long.valueOf(serviceFee.getServiceId()), serviceFee);
            }

            final Map packageFees = ppv.getServicePackageVersion().getPackageFees();

            Home home = (Home) ctx.get(ServicePackageHome.class);
            home = home.where(ctx, new In(ServicePackageXInfo.ID, new HashSet(packageFees.keySet())));
            /*
             * amedina : had to take out this line because remotely the Visitor works locally and
             * it doesn't return the objects created
             */
            //home.forEach(new CollectServicesWithSource(serviceFeesMap, packageFees));

            final Collection pkgs = home.selectAll();

            it = pkgs.iterator();

            final CollectServicesWithSource visitor = new CollectServicesWithSource(serviceFeesMap, packageFees);

            while (it.hasNext())
            {
                visitor.visit(ctx, it.next());
            }

        }
        catch (final HomeException e)
        {
            new MajorLogMsg(SubscriberServicesSupport.class, "Cannot access "
                    + ServicePackageHome.class.getName() + " home!  Encountered a HomeException.", e).log(ctx);
        }

        return serviceFeesMap;
    }
    
    
    public static boolean isClctService(Context ctx, Subscriber sub, long serviceId)
    {
        Collection<Long> clctSuspended = sub.getCLTCServices(ctx);
        return (clctSuspended.contains(serviceId));
    }

    
    
     /**
      * Return the predicate filtering for Provisioned Subscriber Services.
      * Provisioned with Errors is not considered as Provisioned Services
      * @return Elang Predicate
      */
     public static Predicate getProvisionedSubscriberServicesPredicate()
     {
         final Or predicate = new Or();
         predicate.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONED));
         ServiceRefactoring_RefactoringClass.defineProvisionedService();
         return predicate;
     }
     
     /**
      * Return the predicate filtering for UnProvisionedWithError Subscriber Services.
      * @return Elang Predicate
      */
     public static Predicate getUnProvisionedWithErrorSubscriberServicesPredicate()
     {
         final Or predicate = new Or();
         predicate.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONEDWITHERRORS));
         return predicate;
     }
     
     
     /**
      * Return the predicate filtering for "un-provisioned" Subscriber Services.
      * Those deprovisioned and those that failed to transition from the provisioned state
      * @return
      */
     public static Predicate getUnProvisionedSubscriberServicesPredicate()
     {
         return new In(SubscriberServicesXInfo.PROVISIONED_STATE, UNPROVISIONED_STATES);
     }
     
     /**
      * Return the predicate filtering for Subscriber Services eligible to be provisioned
      * @return
      */
     public static Predicate getEligibleSubscriberServicesPredicate()
     {
         final Or predicate = new Or();
         
         final And pendingFilter = new And();
         pendingFilter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PENDING));
         pendingFilter.add(new LTE(SubscriberServicesXInfo.START_DATE, new Date()));
         predicate.add(pendingFilter);
         
         HashSet<ServiceStateEnum> states = new HashSet<ServiceStateEnum>();
         /* These are already provisioned services why provisioning them again?
          * Prior to Service Refactoring (CRM 8.0), this query was made to get all "Active" SubscriberServices. 
          * If we add PROVISIONED, then unselecting the service fails to unprovision the service (the subservrecord remains, in PROVISIONED state)*/
         states.add(ServiceStateEnum.PROVISIONED);
         states.add(ServiceStateEnum.PROVISIONEDWITHERRORS);
         states.add(ServiceStateEnum.SUSPENDED);
         predicate.add(new In(SubscriberServicesXInfo.PROVISIONED_STATE, states));
         return predicate;
     }
     
     /**
      * Return the Subscriber Service Record identified by the Subscriber ID and the Service ID pair
      * @param ctx
      * @param subscriberId
      * @param serviceId
      * @return
      */
     public static SubscriberServices getSubscriberServiceRecord(
             final Context ctx, 
             final String subscriberId, 
             final Long serviceId, String path)
     {
         SubscriberServices service = null;
         if (subscriberId != null && subscriberId.trim().length() > 0)
         {
             try
             {
                /**
                 * Querying on composite Primary key so that it will be picked from LRUcache.
                 */
                 service = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberServices.class, new SubscriberServicesID(serviceId, subscriberId, path));
             }
             catch (HomeException e)
             {
                 new InfoLogMsg(SubscriberServicesSupport.class.getName(), 
                         "Failed to find SubscriberService record for subID=" + subscriberId +
                         " serviceId=" + serviceId + ", encountered a HomeException [message=" + e.getMessage() + "]", e).log(ctx);
             }
         }
         return service;
     }
     
     
     /**
      * Return the Subscriber Service Record identified by the Subscriber ID and the Service ID pair
      * @param ctx
      * @param subscriberId
      * @param serviceId
      * @return
      */
     public static SubscriberServices getSubscriberServiceRecordWithLookupinCacheFirst(
             final Context ctx, 
             final String subscriberId, 
             final Long serviceId,  String path)
     {
         SubscriberServices service = null;
         if (subscriberId != null && subscriberId.trim().length() > 0)
         {
             try
             {
                 final Home h = (Home) ctx.get(SubscriberServicesHome.class);

                 SubscriberServicesID ssID = new SubscriberServicesID(serviceId, subscriberId, path);
                 service = (SubscriberServices) h.find(ctx,ssID);

             }
             catch (HomeException e)
             {
                 new InfoLogMsg(SubscriberServicesSupport.class.getName(), 
                         "Failed to find SubscriberService record for subID=" + subscriberId +
                         " serviceId=" + serviceId + ", encountered a HomeException [message=" + e.getMessage() + "]", e).log(ctx);
             }
         }
         return service;
     }
     
     /**
      * Persist the Subscriber Service record to the database
      * @param ctx
      * @param subscriberId
      * @param service
      * @throws HomeException
      */
     public static void createSubscriberServiceRecord(
             final Context ctx,
             String subscriberId,
             SubscriberServices service)
         throws HomeException
     {
         try
         {
             Home ssHome = (Home)ctx.get(SubscriberServicesHome.class);
             ssHome.create(ctx, service);
             if(LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscriberServicesSupport.class.getName(), 
                         "Successfully created Subscriber Service record for SUBSCRIBER=" + subscriberId + 
                         " for SERVICE=" + service.getServiceId() + 
                         " in STATE=" + service.getProvisionedState().getDescription() + 
                         ", SUSPENDREASON=" + service.getSuspendReason().getDescription() + 
                         ", MANDATORY=" + service.getMandatory(), null).log(ctx);
             }
         }
         catch (HomeException e)
         {
             HomeException exception = 
                 new HomeException("Failed to create the Subscriber Service Record for subscriber=" 
                     + subscriberId + " for SERVICE=" + service.getServiceId() + 
                     " in STATE=" + service.getProvisionedState().getDescription() + 
                     ", SUSPENDREASON=" + service.getSuspendReason().getDescription() + 
                     ", MANDATORY=" + service.getMandatory());
             exception.initCause(e);
             throw exception;
         }  
     }
     
     public static void updateSubscriberServiceRecordOnXDBHmeDirectly(
             final Context ctx,
             SubscriberServices service)
        throws HomeException
        {
    	 	Home home = (Home) ctx.get(SubscriberServicesXDBHome.class);
    	 	
    	 	if(home != null)
    	 	{
    	 		home.store(ctx,service);
    	 	}else
    	 	{
    	 		throw new HomeException( "SubscriberServicesXDBHome not found in context!!");
    	 	}
        }
     
     /**
      * Persist the updated Subscriber Service record to the database
      * @param ctx
      * @param service
      * @throws HomeException
      */
     public static void updateSubscriberServiceRecord(
             final Context ctx,
             SubscriberServices service)
        throws HomeException
        {
         try
         {
             Home ssHome = (Home)ctx.get(SubscriberServicesHome.class);
             service = (SubscriberServices) ssHome.store(ctx,service);

             if(LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscriberServicesSupport.class.getName(), 
                         "Successfully saved Subscriber Service record for SUBSCRIBER=" + service.getSubscriberId() + 
                         " for SERVICE=" + service.getServiceId() + 
                         " to STATE=" + service.getProvisionedState().getDescription() + 
                         ", with SUSPENDREASON=" + service.getSuspendReason().getDescription() + 
                         ", MANDATORY=" + service.getMandatory(), null).log(ctx);
             }
         }
         catch (HomeException e)
         {
             if(LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscriberServicesSupport.class.getName(),  
                         "Failed to update the Subscriber Service Record for subscriber=" 
                         + service.getSubscriberId() + " for SERVICE=" + service.getServiceId() + 
                         " to STATE=" + service.getProvisionedState().getDescription() + 
                         ", with SUSPENDREASON=" + service.getSuspendReason().getDescription() + 
                         ", MANDATORY=" + service.getMandatory() + ". Due to error:" + e.getMessage(), e).log(ctx);
             }
             HomeException exception = new HomeException("Failed to update the Subscriber Service Record. "
                     + "Due to error: " + e.getMessage(),e);
             //exception.initCause(e);
             throw exception;
         }       
     }
          
     /**
      * Removes the specified SubscriberServices Record. 
      * Returns an error code.  -1 indicates an error.
      * @param ctx
      * @param subscriberId
      * @param serviceId
      * @return
      */
     public static int deleteSubscriberServiceRecord(
             final Context ctx, 
             final String subscriberId, 
             final long serviceId, String path)
     {
         int result = 0;
         Home home = (Home) ctx.get(SubscriberServicesHome.class);
         SubscriberServicesID identifier = new SubscriberServicesID(serviceId, subscriberId, path);
         try 
         {
             SubscriberServices subscriberServ = HomeSupportHelper.get(ctx).findBean(ctx, SubscriberServices.class, identifier);
             if (LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscriberServicesSupport.class.getName(),  
                         "Deleting the Subscriber Services, Subscriber=" + 
                         subscriberId + ", Service=" + serviceId, null).log(ctx);
             }
             
             if(subscriberServ != null) 
             {
            	 home.remove(ctx, subscriberServ);
             }
             else if (LogSupport.isDebugEnabled(ctx)) 
             {
            	 LogSupport.info(ctx,SubscriberServicesSupport.class.getName() , "SubscriberServices record is null for service=" 
            			 + serviceId);
             }

         }
         catch (HomeException e)
         {
             new MinorLogMsg(SubscriberServicesSupport.class.getName(), 
                     "Failed to delete the subscriber service record with subId=" + subscriberId
                     + " serviceID=" + serviceId, e).log(ctx);
             result = -1;
         }
         return result;
     }
     
     /**
      * Removes the specified SubscriberServices Record. 
      * Returns an error code.  -1 indicates an error.
      * @param ctx
      * @param subscriberId
      * @param serviceId
      * @return
      */
     public static int deleteSubscriberServiceRecord(final Context ctx, SubscriberServices subService)
     {
         int result = 0;
         
    	 if(subService == null )
    	 {
    		return result; 
    	 }
         
         Home home = (Home) ctx.get(SubscriberServicesHome.class);
         try 
         {
             if (LogSupport.isDebugEnabled(ctx))
             {
                 new DebugLogMsg(SubscriberServicesSupport.class.getName(),  
                         "Deleting the Subscriber Services, Subscriber=" + 
                         subService.getSubscriberId() + ", Service=" + subService.getServiceId(), null).log(ctx);
             }

             home.remove(subService);

         }
         catch (HomeException e)
         {
             new MinorLogMsg(SubscriberServicesSupport.class.getName(), 
                     "Failed to delete the subscriber service record with subId=" + subService.getSubscriberId()
                     + " serviceID=" + subService.getServiceId(), e).log(ctx);
             result = -1;
         }
         return result;
     }
     
     /**
      * Removes the SubscriberServices Records specified by the subscriberId and serviceIds
      * @param ctx
      * @param subscriberId
      * @param serviceIds
      */
     public static void deleteSubscriberServiceRecords(final Context ctx, final String subscriberId, final Set<Long> serviceIds)
     {
         for(Long serviceId : serviceIds)
         {
             deleteSubscriberServiceRecord(ctx, subscriberId, serviceId.longValue(), SubscriberServicesUtil.DEFAULT_PATH);
         }
     }
     
     /**
      * Return the "provisioned" SubscriberServices IDs for the given subscriber. 
      * @param ctx
     * @param subscriberId
      * @return
     */
    public static Collection getProvisionedServices(final Context ctx, final String subscriberId)
    {
        final Predicate choice = getProvisionedSubscriberServicesPredicate();
        return getServiceIdsByFilter(ctx, subscriberId, choice);
    }

    /**
     * Return the "unprovisionedwitherror" SubscriberServices IDs for the given subscriber. 
     * @param ctx
    * @param subscriberId
     * @return
    */
   public static Collection<SubscriberServices> getUnProvisionedWithErrorSubscriberServices(final Context ctx, final String subscriberId, boolean requireIds)
   {
       final Predicate choice = getUnProvisionedWithErrorSubscriberServicesPredicate();
       return getServicesByFilter(ctx, subscriberId, choice, requireIds);
   }
   
    
    /**
     * Return the "provisioned" SubscriberServices IDs for the given subscriber. 
     * @param ctx
    * @param subscriberId
     * @return
    */
   public static Collection<SubscriberServices> getProvisionedSubscriberServices(final Context ctx, final String subscriberId)
   {
       final Predicate choice = getProvisionedSubscriberServicesPredicate();
       return getServicesByFilter(ctx, subscriberId, choice, false);
   }
   

    /**
     * Return the "provisioned" SubscriberServices IDs for the given subscriber.
     * 
     * @param ctx
     * @param subscriberId
     * @return
     */
    public static Collection<SubscriberServices> getNonUnProvisionedSubscriberServices(final Context ctx,
            final String subscriberId)
    {
        And choice = new And();
        choice.add(new NEQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.UNPROVISIONED));

        return getServicesByFilter(ctx, subscriberId, choice, false);
    } 
    
   /**
    * Return the "provisioned" SubscriberServices IDs for the given subscriber. 
    * @param ctx
   * @param subscriberId
    * @return
   */
  public static Collection getProvisionedOrProvisionedWithErrorsSubscriberServices(final Context ctx, final String subscriberId)
  {
      final Or predicate = new Or();
      predicate.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONED));
      predicate.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.PROVISIONEDWITHERRORS));
      
      return getServicesByFilter(ctx, subscriberId, predicate, false);
  }
   
   
   
    /**
     * Return a list of Services Ids filtered by the given predicate.
     * @param ctx
     * @param subscriberId
     * @param filter
     * @return
     */
    public static Set<Long> getServiceIdsByFilter(final Context ctx,
            final String subscriberId,
            final Predicate filter)
    {
        return getServicesByFilter(ctx,subscriberId,filter, true);
    }

    /**
     * Return a list of Services Ids filtered by the given predicate.
     * @param ctx
     * @param subscriberId
     * @param filter
     * @return
     */
    public static Set<SubscriberServices> getSubscriberServicesByFilter(final Context ctx,
            final String subscriberId,
            final Predicate filter)
    {
        return getServicesByFilter(ctx,subscriberId,filter, false);
    }
    
    /**
     * Return a list of Services Ids filtered by the given predicate.
     * @param ctx
     * @param subscriberId
     * @param filter
     * @return
     */
    private static Set getServicesByFilter(final Context ctx,
            final String subscriberId,
            final Predicate filter,
            final boolean requireIds)
    {
        final Home h = (Home) ctx.get(SubscriberServicesHome.class);

        final Set services = new HashSet();
        try
        {
            final And condition = new And();
            condition.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            condition.add(filter);
            final Collection<SubscriberServices> col = h.select(ctx, condition);
            
            for (SubscriberServices subscriberService : col)
            {
                if ( requireIds)
                {
                    services.add(Long.valueOf((subscriberService.getServiceId())));   
                }
                else
                {
                    services.add(subscriberService);
                }
            }
        }
        catch (final Exception e)
        {
            new MinorLogMsg(SubscriberServicesSupport.class.getName(),
                    "Error getting Subscriber Services for subscriber=" + subscriberId, e).log(ctx);
        }
        return services;
    }
    
    
    
    /**
     * Gets all the subscriber services with state = com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDED
     * @param context the operating context
     * @param subscriberId the subscriber ID
     * @return the collection of all suspended subscribers; null if there is a problem with the subscriber service home
     */
    public static Collection getSuspendedServices(Context context, String subscriberId)
    {
        final Home subscriberServicesHome = (Home) context.get(SubscriberServicesHome.class);
        Collection suspendedSubscribers = null;

        if (subscriberServicesHome != null)
        {
            And filter = new And();
            filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
            filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.SUSPENDED));

            try
            {
                suspendedSubscribers = subscriberServicesHome.where(context, filter).selectAll();
            }
            catch (HomeException e)
            {
                new DebugLogMsg( SubscriberServicesSupport.class.getName(),
                        "HomeException when trying to get the suspended services for subscriber "
                        + subscriberId,
                        e).log(context);
            }
        }

        return suspendedSubscribers;
    }
    
    
    
    /**
     * Gets all the subscriber services with state = com.redknee.app.crm.bean.service.ServiceStateEnum.SUSPENDED
     * @param context the operating context
     * @param subscriberId the subscriber ID
     * @return the collection of all suspended subscribers; null if there is a problem with the subscriber service home
     */
    public static Collection getCLTCServices(Context context, String subscriberId)
    throws HomeException
    {
        final Home subscriberServicesHome = (Home) context.get(SubscriberServicesHome.class);
        final  And filter = new And();
        filter.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subscriberId));
        filter.add(new EQ(SubscriberServicesXInfo.PROVISIONED_STATE, ServiceStateEnum.SUSPENDED));
        filter.add(new EQ(SubscriberServicesXInfo.SUSPEND_REASON, SuspendReasonEnum.CLCT)); 
        return subscriberServicesHome.where(context, filter).selectAll();
         
    }
    /**
     * Moves the suspended services from the to-be inactive subscriber to the newly created subscriber
     * @param ctx the operating context
     * @param oldSubscriber the subscriber to be inactive 
     * @param newSubscriber the newly created subscriber
     */
    public static void moveSuspendedServices(Context ctx,
            Subscriber oldSubscriber,
            Subscriber newSubscriber)
    {
        Map suspendedServices = oldSubscriber.getSuspendedServices();
        Collection<ServiceFee2> entries = suspendedServices.values();
        for (ServiceFee2 svc : entries)
        {
            try
            {
                // Add Suspended Services to new subscriber
                final SubscriberServices oldRecord = 
                    SubscriberServicesSupport.getSubscriberServiceRecord(ctx, oldSubscriber.getId(), svc.getServiceId(), svc.getPath());

                oldRecord.setSubscriberId(newSubscriber.getId());
                
                Home ssHome = (Home)ctx.get(SubscriberServicesHome.class);
                ssHome.create(ctx, oldRecord); 
               
                
                // If adding was successful, remove suspended services from old subscriber.
                deleteSubscriberServiceRecord(ctx, oldSubscriber.getId(), svc.getServiceId(), svc.getPath());
            }
            catch (HomeException e)
            {
                new MajorLogMsg(SubscriberServicesSupport.class.getName(),
                        "Error when trying to add the service "  + svc.getServiceId() + 
                        " to subscriber=" + newSubscriber.getId() + 
                        " (new subscriber) during Subscriber Move Activity.", e).log(ctx);
            }
        }
    }
    

    /**
     * Gets the Subscriber Service from the list of Services to intended to provisio 
     * @param ctx the operating context
     * @param subscriber the subscriber to extract the service 
     * @param identifier the service id
     * @return the subscriber service, returns NULL if the Service is not in the list for Display
     */
    public static SubscriberServices getSubscriberServiceFromList(final Context ctx,
            Subscriber subscriber,
            long identifier)
    {
        Iterator iter = subscriber.getAllNonUnprovisionedStateServices().iterator();
        SubscriberServices service = null;
        
        while(iter.hasNext())
        {
            SubscriberServices obj = (SubscriberServices) iter.next();
            if (obj.getServiceId() == identifier)
            {
                service = obj;
            }
        }
        return service;
    }

    /**
     * Create or overwrite the Subscriber Service record for the given subscriber and service identifier.
     * 
     * @param ctx
     * @param subscriber
     * @param serviceId
     * @param state
     * @throws HomeException
     */
    public static SubscriberServices createOrModifySubcriberService(final Context ctx,
            final Subscriber subscriber,
            final long serviceId,
            final ServiceStateEnum state) throws HomeException
    {
        SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                subscriber.getId(),
                Long.valueOf(serviceId), SubscriberServicesUtil.DEFAULT_PATH);
        
        return createOrModifySubcriberService(ctx,
                subscriber,
                serviceId,
                state,
                (service != null ? service.getSuspendReason() : SuspendReasonEnum.NONE),
                service);
    }

    /**
     * Create or overwrite the Subscriber Service record for the given 
     * subscriber, service identifier and suspendReason.
     * 
     * A Subscriber Service Record won't be created unless the Service is an available
     * option of the Subscriber's Price Plan.
     * @param ctx
     * @param subscriber
     * @param serviceId
     * @param state
     * @param suspendReason
     * @throws HomeException
     */
    public static SubscriberServices createOrModifySubcriberService(final Context ctx,
            final Subscriber subscriber,
            final long serviceId,
            final ServiceStateEnum state,
            final SuspendReasonEnum suspendReason) throws HomeException
    {
        SubscriberServices service = SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                subscriber.getId(),
                Long.valueOf(serviceId), SubscriberServicesUtil.DEFAULT_PATH);

        return createOrModifySubcriberService(ctx,
                subscriber,
                serviceId,
                state,
                suspendReason,
                service);
    }

    /**
     * Create or overwrite the Subscriber Service record for the given
     * subscriber, service identifier and suspendReason.
     *
     * A Subscriber Service Record won't be created unless the Service is an available
     * option of the Subscriber's Price Plan.
     * @param ctx
     * @param subscriber
     * @param serviceId
     * @param state
     * @param suspendReason
     * @param service
     * @throws HomeException
     */
    public static SubscriberServices createOrModifySubcriberService(final Context ctx,
            final Subscriber subscriber,
            final long serviceId,
            final ServiceStateEnum state,
            final SuspendReasonEnum suspendReason,
            SubscriberServices service) throws HomeException
    {
     
        if (service != null)
        {
         
            service.setProvisionedState(state);
            service.assignSuspendReason(subscriber, state, suspendReason);
            if (SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                    subscriber.getId(),
                    Long.valueOf(serviceId), SubscriberServicesUtil.DEFAULT_PATH) != null)
            {
                updateSubscriberServiceRecord(ctx, service);
            } else 
            {
                createSubscriberServiceRecord(ctx, subscriber.getId(), service);
            }
            
        }
        else
        {
            //Only create if the Service is allowed by the Price Plan
            service = SubscriberServicesSupport.getSubscriberServiceFromList(ctx, subscriber, serviceId);
            if (service != null)
            {
                service.setSubscriberId(subscriber.getId());
                service.setProvisionedState(state);
                service.assignSuspendReason(subscriber, state, suspendReason);
                createSubscriberServiceRecord(ctx, subscriber.getId(), service);
            }
        }
        return service;
    }
    
    
    /**
     * 
     * @param ctx
     * @param service
     * @return
     * @throws HomeException
     */
    public static SubscriberServices createOrModifySubcriberService(final Context ctx,
            final SubscriberServices service) throws HomeException
    {
        SubscriberServices ret = null; 
        Home ssHome = (Home)ctx.get(SubscriberServicesHome.class);
        
        if (SubscriberServicesSupport.getSubscriberServiceRecord(ctx,
                service.getSubscriberId(),
                Long.valueOf(service.getServiceId()), service.getPath()) != null)
        {

            ret = (SubscriberServices) ssHome.store(ctx,service);
        }
        else
        {

            ret = (SubscriberServices)ssHome.create(ctx, service);
                
        }
        return ret;
    }

    /**
     * Updates the Subscriber Services record with the given value for MANDATORY field.
     * @param ctx
     * @param record
     * @param isMandatory
     * @return
     * @throws HomeException
     */
    public static SubscriberServices updateMandatoryField(final Context ctx, 
            final SubscriberServices record, final boolean isMandatory)
        throws HomeException
    {
        record.setMandatory(isMandatory);
        updateSubscriberServiceRecord(ctx, record);
        return record;
    }
    

    /**
     * Verifies if the Subscriber service is provisioned with errors 
     * @param ctx the operating context
     * @param serviceId the service to verify
     * @param subscriberId the subscriber to verify
     * @return true if the service is provisioned with errors
     */
    public static boolean isProvisionedWithErrors(Context ctx, long serviceId, String subscriberId)
    {
        SubscriberServices subsService = getSubscriberServiceRecord(ctx, subscriberId, serviceId, SubscriberServicesUtil.DEFAULT_PATH);
        final boolean result =  subsService != null && (subsService.getProvisionedState() == ServiceStateEnum.PROVISIONEDWITHERRORS);
        return result;
    }

    
    /**
     * Finds all subscriberservices within the provided list for the correct boolean value for cltcenabled
     */
    public static Collection<SubscriberServices> findAllWithinListByCLTC(final Context ctx, Collection currentList,
            boolean isCLTCEnabled)
    {
        Collection<SubscriberServices> filterList = new ArrayList<SubscriberServices>();
        for( Iterator<SubscriberServices> iter = currentList.iterator();iter.hasNext();)
        {
            SubscriberServices service = iter.next();
            if ( isCLTCEnabled == service.isEnableCLTC(ctx))
            {
                filterList.add(service);
            }
        }
        return filterList;
    }
    
    
    /**
     * Finds all non suspended subscriberservices within the provided list for the correct boolean value for cltcenabled
     */
    public static Collection<SubscriberServices> findSuspendedWithinListByCLTC(final Context ctx,
            Collection currentList, boolean isCLTCEnabled)
    {
        Collection<SubscriberServices> filterList = new ArrayList<SubscriberServices>();
        for (Iterator<SubscriberServices> iter = currentList.iterator(); iter.hasNext();)
        {
            SubscriberServices service = iter.next();
            if (isCLTCEnabled == service.isEnableCLTC(ctx)
                    && ServiceStateEnum.SUSPENDED.equals(service.getProvisionedState()))
            {
                filterList.add(service);
            }
        }
        return filterList;
    }
    
    
    public static Collection<SubscriberServices> getCLCTServicesWithThresholdAboveBalance(final Context ctx,
            Collection currentList, long newBlance)
    {
        Collection<SubscriberServices> filterList = new ArrayList<SubscriberServices>();
        for (Iterator<SubscriberServices> iter = currentList.iterator(); iter.hasNext();)
        {
            SubscriberServices subService = iter.next();
            Service service = subService.getService();
            // return only those suspended clct services which were suspended due to clct suspension
            if (subService.isEnableCLTC(ctx) && service.getClctThreshold() > newBlance)
            {
                filterList.add(subService);
            }
        }
        return filterList;
    }
    
    public static Collection<SubscriberServices> getCLCTServicesWithThresholdBelowBalance(final Context ctx,
            Collection currentList, long newBlance)
    {
        Collection<SubscriberServices> filterList = new ArrayList<SubscriberServices>();
        for (Iterator<SubscriberServices> iter = currentList.iterator(); iter.hasNext();)
        {
            SubscriberServices subService = iter.next();
            Service service = subService.getService();
            // return only those suspended clct services which were suspended due to clct suspension
            if (subService.isEnableCLTC(ctx) && service.getClctThreshold() < newBlance
                    && SuspendReasonEnum.CLCT.equals(subService.getSuspendReason()))
            {
                filterList.add(subService);
            }
        }
        return filterList;
    }
    
    /**
     * Provisions subscriber services
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param provServices
     *            The list of services to be provisioned.
     * @param newSub
     *            The new subscriber.
     * @param resultCodes
     *            Map of all provisioning result codes.
     * @throws HomeException
     *             Thrown if there are problems provisioning.
     */
    public static void provisionSubscriberServices(final Context ctx, final Subscriber oldSub, final List<SubscriberServices> provServices,
        final Subscriber newSub, final Map<ExternalAppEnum, ProvisionAgentException> resultCodes) throws HomeException
        {        
    	if (newSub == null)
    	{
    		throw new HomeException("System Error: no subscriber provided!");
    	}


    	if (LogSupport.isDebugEnabled(ctx))
    	{
    		StringBuilder sb = new StringBuilder();
    		sb.append("Provisioning services to subscriber '");
    		sb.append(newSub.getId());
    		sb.append("': ");
    		sb.append(provServices);
    		LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
    	}

    	for (SubscriberServices subService : provServices)
    	{
    		subService.setContext(ctx);
    		subService.setProvisionAction(ServiceProvisionActionEnum.PROVISION); 
    	}
    	Collections.sort(provServices, SubscriberServices.PROVISIONING_ORDER);

    	HTMLExceptionListener exceptionListener = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

    	for (final SubscriberServices subService : provServices)
    	{
    		ServiceStateEnum state = ServiceStateEnum.PROVISIONED;
    		Service newService = subService.getService(ctx);
    		SuspendReasonEnum restoreSuspendreasonOnFailure = subService.getSuspendReason(); //restore on failure
    		HTMLExceptionListener localExceptionListener = null;
    		try
    		{
    			if (LogSupport.isDebugEnabled(ctx))
    			{
    				StringBuilder sb = new StringBuilder();
    				sb.append("Provisioning service '");
    				sb.append(newService.getID());
    				sb.append(" - ");
    				sb.append(newService.getName());
    				sb.append("' to subscriber '");
    				sb.append(newSub.getId());
    				sb.append("'.");
    				LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
    			}

    			Context subCtx = ctx.createSubContext(); 

    			if (!subService.getSkipProvision())
    			{
    				if (exceptionListener != null)
    				{
    					MessageMgr manager = new MessageMgr(subCtx, SubscriberServicesSupport.class);
    					localExceptionListener = new HTMLExceptionListener(manager);
    					subCtx.put(HTMLExceptionListener.class, localExceptionListener);
    				}

    				ProvisioningSupport.provisionService(subCtx, newSub, newService, oldSub);
    			}

    			if (localExceptionListener!=null && localExceptionListener.hasErrors())
    			{
    				subService.setProvisionActionState(false);
    				state = ServiceStateEnum.PROVISIONEDWITHERRORS;
    				for (Throwable t : (List<Throwable>) localExceptionListener.getExceptions())
    				{
    					exceptionListener.thrown(t);
    				}
    			}
    			else
    			{

    				subService.setProvisionActionState(true);
    			}

    			subService.setSubscriberId(newSub.getId()); 
    			subService.setProvisionedState(state);
    			subService.setSuspendReason(SuspendReasonEnum.NONE);
    			createOrModifySubcriberService(ctx,subService);
    			if(oldSub != null)
    			{
    				SubscriptionNotificationSupport.sendServiceStateChangeNotification(subCtx, oldSub, newSub,subService.getService(),null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.PROVISIONED);
    			}
    		}
    		catch (SkipProvisioningException exception)
    		{
    			/*
    			 * Provisioning has been skipped for this service at this time. It
    			 * will be marked as "Provisioned with Errors" and provisioning will
    			 * be retried on the next update.
    			 */
    			if (LogSupport.isDebugEnabled(ctx))
    			{
    				StringBuilder sb = new StringBuilder();
    				sb.append("Skipping provisioning of service '");
    				sb.append(newService.getID());
    				sb.append(" - ");
    				sb.append(newService.getName());
    				sb.append("' to subscriber '");
    				sb.append(newSub.getId());
    				sb.append("': ");
    				sb.append(exception.getMessage());
    				LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString(), exception);
    			}

    			state = ServiceStateEnum.PROVISIONEDWITHERRORS;
    			subService.setSubscriberId(newSub.getId());
    			subService.setProvisionActionState(false);
    			subService.setSuspendReason(restoreSuspendreasonOnFailure);
    			newSub.updateSubscriberService(ctx, subService, state);

    		}catch (SPGSkippingException e)
    		{
    			// skip updating subscriber service, since spg has updated 
    		}
    		catch (final AgentException exception)
    		{
    			StringBuilder sb = new StringBuilder();
    			sb.append("Error while provisioning service '");
    			sb.append(newService.getID());
    			sb.append(" - ");
    			sb.append(newService.getName());
    			sb.append("' to subscriber '");
    			sb.append(newSub.getId());
    			sb.append("': ");
    			sb.append(exception.getMessage());
    			LogSupport.major(ctx, ProvisioningSupport.class, sb.toString());

    			if ( exception instanceof ProvisionAgentException)
    			{    
    				resultCodes.put( ((ProvisionAgentException)exception).getExternalApp(),(ProvisionAgentException) exception);
    			}
    			state = ServiceStateEnum.PROVISIONEDWITHERRORS;
    			subService.setSubscriberId(newSub.getId());
    			subService.setProvisionActionState(false);
    			subService.setSuspendReason(restoreSuspendreasonOnFailure);
    			newSub.updateSubscriberService(ctx, subService, state);

    		}
    		finally
    		{
    			SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub, 
    					HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, subService, state);
    		}
    	}

        }
    
    public static void provisionSubscriberServices(final Context ctx, final Subscriber oldSub, final List<SubscriberServices> provServices,
            final Subscriber newSub, final Map<ExternalAppEnum, ProvisionAgentException> resultCodes, boolean isCalledByBulkLoader) throws HomeException
        {        
            if (newSub == null)
            {
                throw new HomeException("System Error: no subscriber provided!");
            }

            
            if (LogSupport.isDebugEnabled(ctx))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Provisioning services to subscriber '");
                sb.append(newSub.getId());
                sb.append("': ");
                sb.append(provServices);
                LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
            }

            for (SubscriberServices subService : provServices)
            {
                subService.setContext(ctx);
                subService.setProvisionAction(ServiceProvisionActionEnum.PROVISION); 
            }
            Collections.sort(provServices, SubscriberServices.PROVISIONING_ORDER);

            HTMLExceptionListener exceptionListener = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

            for (final SubscriberServices subService : provServices)
            {
                ServiceStateEnum state = ServiceStateEnum.PROVISIONED;
                Service newService = subService.getService(ctx);
                SuspendReasonEnum restoreSuspendreasonOnFailure = subService.getSuspendReason(); //restore on failure
                HTMLExceptionListener localExceptionListener = null;
                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Provisioning service '");
                        sb.append(newService.getID());
                        sb.append(" - ");
                        sb.append(newService.getName());
                        sb.append("' to subscriber '");
                        sb.append(newSub.getId());
                        sb.append("'.");
                            LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
                        }
     
                        Context subCtx = ctx.createSubContext(); 
            
                        if (!subService.getSkipProvision())
                        {
                            if (exceptionListener != null)
                            {
                                MessageMgr manager = new MessageMgr(subCtx, SubscriberServicesSupport.class);
                                localExceptionListener = new HTMLExceptionListener(manager);
                                subCtx.put(HTMLExceptionListener.class, localExceptionListener);
                            }

                            ProvisioningSupport.provisionService(subCtx, newSub, newService, oldSub);
                        }
                        
                        if (localExceptionListener!=null && localExceptionListener.hasErrors())
                        {
                            subService.setProvisionActionState(false);
                            state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                            for (Throwable t : (List<Throwable>) localExceptionListener.getExceptions())
                            {
                                exceptionListener.thrown(t);
                            }
                        }
                        else
                        {
                            
                            subService.setProvisionActionState(true);
                        }

                        subService.setSubscriberId(newSub.getId()); 
                        subService.setProvisionedState(state);
                        subService.setSuspendReason(SuspendReasonEnum.NONE);
                        if (isCalledByBulkLoader != true)
                        {
                        	createOrModifySubcriberService(ctx,subService); // isCalledByBulkLoader should be true if method has call from bulkLoader 
                        }
                        if(oldSub!= null)
                        {
                        	SubscriptionNotificationSupport.sendServiceStateChangeNotification(subCtx, oldSub, newSub, subService.getService(),null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.PROVISIONED);
                            
                        }
                    }
                    catch (SkipProvisioningException exception)
                    {
                        /*
                     * Provisioning has been skipped for this service at this time. It
                     * will be marked as "Provisioned with Errors" and provisioning will
                     * be retried on the next update.
                     */
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Skipping provisioning of service '");
                        sb.append(newService.getID());
                        sb.append(" - ");
                        sb.append(newService.getName());
                        sb.append("' to subscriber '");
                        sb.append(newSub.getId());
                        sb.append("': ");
                        sb.append(exception.getMessage());
                        LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString(), exception);
                    }

                    state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                    subService.setSubscriberId(newSub.getId());
                    subService.setProvisionActionState(false);
                    newSub.updateSubscriberService(ctx, subService, state);
                    
                   
                }catch (SPGSkippingException e)
                {
                    // skip updating subscriber service, since spg has updated 
                }
                catch (final AgentException exception)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Error while provisioning service '");
                    sb.append(newService.getID());
                    sb.append(" - ");
                    sb.append(newService.getName());
                    sb.append("' to subscriber '");
                    sb.append(newSub.getId());
                    sb.append("': ");
                    sb.append(exception.getMessage());
                    LogSupport.major(ctx, ProvisioningSupport.class, sb.toString());

                    if ( exception instanceof ProvisionAgentException)
                    {    
                        resultCodes.put( ((ProvisionAgentException)exception).getExternalApp(),(ProvisionAgentException) exception);
                    }
                    state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                    subService.setSubscriberId(newSub.getId());
                    subService.setProvisionActionState(false);
                    subService.setSuspendReason(restoreSuspendreasonOnFailure);
                    newSub.updateSubscriberService(ctx, subService, state);
                    
                }
                finally
                {
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub, 
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, subService.getService(ctx), state);
                }
            }

        }
        

    
    /**
     * Resume subscriber services
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param resumeServices
     *            The list of services to be resumed.
     * @param newSub
     *            The new subscriber.
     * @param resultCodes
     *            Map of all provisioning result codes.
     * @throws HomeException
     *             Thrown if there are problems provisioning.
     */
    public static void resumeSubscriberServices(final Context ctx, final Subscriber oldSub, final List<SubscriberServices> resumeServices,
        final Subscriber newSub, final Map<ExternalAppEnum, ProvisionAgentException> resultCodes) throws HomeException
    {
        if (newSub == null)
        {
            throw new HomeException("System Error: no subscriber provided!");
        }
            // provision all services now
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(SubscriberServicesSupport.class, "resume services for subscriber " + newSub.getId() + " "
                    + newSub.getMSISDN(), null).log(ctx);
                new DebugLogMsg(SubscriberServicesSupport.class, "resumeSubscriberServices: Services to Resume = [" + resumeServices + "]", null).log(ctx);
            }

            for (SubscriberServices subService : resumeServices)
            {
                subService.setContext(ctx);
                subService.setProvisionAction(ServiceProvisionActionEnum.PROVISION);
            }

            Collections.sort(resumeServices, SubscriberServices.PROVISIONING_ORDER);
            
            HTMLExceptionListener exceptionListener = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

            for (final SubscriberServices subService : resumeServices)
            {
                ServiceStateEnum state = ServiceStateEnum.PROVISIONED;
                HTMLExceptionListener localExceptionListener = null;
                SuspendReasonEnum restoreSuspendreasonOnFailure = subService.getSuspendReason(); //restore on failure

                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(SubscriberServicesSupport.class, "resuming service " + subService.getServiceId()
                            + " for subscriber " + newSub.getId() + " " + newSub.getMSISDN(), null).log(ctx);
                    }
 
                    //  We need change subscriberservices provision state before we make the request
                    // Otherwsie, service will updated to suspend state because currently service state is suspended
                    // If the provisioning fails, then it would to provisioning failed errors 
                    Context subCtx = ctx.createSubContext();
                    
                    if (exceptionListener != null)
                    {
                        MessageMgr manager = new MessageMgr(subCtx, SubscriberServicesSupport.class);
                        localExceptionListener = new HTMLExceptionListener(manager);
                        subCtx.put(HTMLExceptionListener.class, localExceptionListener);
                    }

                    ProvisioningSupport.resumeService(subCtx, newSub, subService.getService(ctx), oldSub);
                    
                    if (localExceptionListener!=null && localExceptionListener.hasErrors())
                    {
                        subService.setProvisionActionState(false);
                        state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                        for (Throwable t : (List<Throwable>) localExceptionListener.getExceptions())
                        {
                            exceptionListener.thrown(t);
                        }
                    }
                    else
                    {
                        subService.setProvisionActionState(true);
                    }
                    
                    subService.setProvisionedState(state);
                    subService.setSuspendReason(SuspendReasonEnum.NONE);
                    createOrModifySubcriberService(ctx, subService);  
                   	SubscriptionNotificationSupport.sendServiceStateChangeNotification(subCtx, oldSub, newSub, subService.getService(),null,NotificationTypeEnum.SERVICE_STATE_CHANGE, ServiceStateEnum.PROVISIONED);
                    
                }
                catch (SPGSkippingException e)
                {
                    // skip updating subscriber service, since spg has updated 
                }
                catch (final AgentException exception)
                {
                    LogSupport.major(ctx, SubscriberServicesSupport.class, 
                            "Error trying to resuming the service " + subService.getServiceId()
                            + " for subcriber " + newSub.getId());

                    if ( exception instanceof ProvisionAgentException)
                    {    
                        resultCodes.put( ((ProvisionAgentException)exception).getExternalApp(),(ProvisionAgentException) exception);
                    }
                    state = ServiceStateEnum.PROVISIONEDWITHERRORS;
                    subService.setProvisionActionState(false);
                    subService.setSuspendReason(restoreSuspendreasonOnFailure);
                    newSub.updateSubscriberService(ctx, subService, state);
                    
                }
                finally
                {
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub, 
                            HistoryEventTypeEnum.PROVISION, ChargedItemTypeEnum.SERVICE, subService.getService(ctx), state);
                }
            }

    }
    
    
    /**
     * Resume subscriber services
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param resumeServices
     *            The list of services to be resumed.
     * @param newSub
     *            The new subscriber.
     * @param resultCodes
     *            Map of all provisioning result codes.
     * @throws HomeException
     *             Thrown if there are problems provisioning.
     */
    public static void suspendSubscriberServices(final Context ctx, final Subscriber oldSub, final List<SubscriberServices> suspendServices,
        final Subscriber newSub, final Map<ExternalAppEnum, ProvisionAgentException> resultCodes) throws HomeException
    {
        if (newSub == null)
        {
            throw new HomeException("System Error: no subscriber provided!");
        }

            // provision all services now
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(SubscriberServicesSupport.class, "suspend services for subscriber " + newSub.getId() + " "
                    + newSub.getMSISDN(), null).log(ctx);
                new DebugLogMsg(SubscriberServicesSupport.class, "suspendSubscriberServices: Services to Resume = [" + suspendServices + "]", null).log(ctx);
            }

            for (SubscriberServices subService : suspendServices)
            {
                subService.setContext(ctx);
                subService.setProvisionAction(ServiceProvisionActionEnum.SUSPEND); 
            }
            Collections.sort(suspendServices, SubscriberServices.UNPROVISIONING_ORDER);

            HTMLExceptionListener exceptionListener = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

            for (final SubscriberServices subService : suspendServices)
            {
                ServiceStateEnum state = ServiceStateEnum.SUSPENDED;
                HTMLExceptionListener localExceptionListener = null;

                try
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(SubscriberServicesSupport.class, "suspending service " + subService.getServiceId()
                            + " for subscriber " + newSub.getId() + " " + newSub.getMSISDN(), null).log(ctx);
                    }
                    
                      //  We need change subscriberservices provision state before we make the request
                    // Otherwsie, service will updated to suspend state because currently service state is suspended
                    // If the provisioning fails, then it would to provisioning failed errors 
                    Context subCtx = ctx.createSubContext();

                    if (exceptionListener != null)
                    {
                        MessageMgr manager = new MessageMgr(subCtx, SubscriberServicesSupport.class);
                        localExceptionListener = new HTMLExceptionListener(manager);
                        subCtx.put(HTMLExceptionListener.class, localExceptionListener);
                    }

                    ProvisioningSupport.suspendService(subCtx, newSub, subService.getService(ctx), oldSub);
                   
                    if (localExceptionListener!=null && localExceptionListener.hasErrors())
                    {
                        subService.setProvisionActionState(false);
                        state = ServiceStateEnum.SUSPENDEDWITHERRORS;
                        for (Throwable t : (List<Throwable>) localExceptionListener.getExceptions())
                        {
                            exceptionListener.thrown(t);
                        }
                    }
                    else
                    {
                        subService.setProvisionActionState(true);
                    }

                    subService.setProvisionedState(state);
                    subService.setSuspendReason(SuspendReasonEnum.NONE);
                    createOrModifySubcriberService(ctx,subService);  
                    if(oldSub!=null){
                    	SubscriptionNotificationSupport.sendServiceStateChangeNotification(subCtx, oldSub, newSub, subService.getService(),null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.SUSPENDED);
                    }
                }
                catch (SPGSkippingException e)
                {
                    // skip updating subscriber service, since spg has updated 
                }
                catch (final AgentException exception)
                {
                    LogSupport.major(ctx, SubscriberServicesSupport.class, 
                            "Error trying to suspend the service " + subService.getServiceId()
                            + " for subcriber " + newSub.getId());

                    if ( exception instanceof ProvisionAgentException)
                    {    
                        resultCodes.put( ((ProvisionAgentException)exception).getExternalApp(),(ProvisionAgentException) exception);
                    }
                    state = ServiceStateEnum.SUSPENDEDWITHERRORS;
                    subService.setProvisionActionState(false);
                    newSub.updateSubscriberService(ctx, subService, 
                            state);
                   
                }
                finally
                {
                    SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, newSub, 
                            HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, subService.getService(ctx), state);
                }
            }
       
    }
    
    /**
     * Unprovision subscriber services
     * @param ctx
     *            The operating context.
     * @param oldSub
     *            The old subscriber.
     * @param unprovServices
     *            The set of services to be unprovisioned.
     * @param newSub
     *            The new subscriber.
     * @param resultCodes
     *            Map of provisioning result codes.
     * @throws HomeException
     *             Thrown if there are problems unprovisioning.
     */
    public static void unprovisionSubscriberServices(final Context ctx, final Subscriber oldSub, final List<SubscriberServices> unprovServices,
            final Subscriber newSub, final Map<ExternalAppEnum, ProvisionAgentException> resultCodes) throws HomeException
    {
        if (oldSub == null)
        {
            throw new HomeException("No subscriber was provided.");
        }

        if (LogSupport.isDebugEnabled(ctx))
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Unprovisioning services to subscriber '");
            sb.append(oldSub.getId());
            sb.append("': ");
            sb.append(unprovServices);
            LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
        }
        
        for (final SubscriberServices subService : unprovServices)
        {
            subService.setContext(ctx);
            subService.setProvisionAction(ServiceProvisionActionEnum.UNPROVISION);
        }
        
        Collections.sort(unprovServices, SubscriberServices.UNPROVISIONING_ORDER);
        
        HTMLExceptionListener exceptionListener = (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);

        for (final SubscriberServices subService : unprovServices)
        {
            ServiceStateEnum state = ServiceStateEnum.UNPROVISIONED;
            Service service = subService.getService(ctx);
            HTMLExceptionListener localExceptionListener = null;
            try
            {
                if (LogSupport.isDebugEnabled(ctx))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unprovisioning service '");
                    sb.append(service.getID());
                    sb.append(" - ");
                    sb.append(service.getName());
                    sb.append("' from subscriber '");
                    sb.append(oldSub.getId());
                    sb.append("'.");
                    LogSupport.debug(ctx, ProvisioningSupport.class, sb.toString());
                }

                Context subCtx = ctx.createSubContext();
                if (!subService.getSkipProvision())
                {
                    if (exceptionListener != null)
                    {
                        MessageMgr manager = new MessageMgr(subCtx, SubscriberServicesSupport.class);
                        localExceptionListener = new HTMLExceptionListener(manager);
                        subCtx.put(HTMLExceptionListener.class, localExceptionListener);
                    }

                    ProvisioningSupport.unprovisionService(subCtx, oldSub, service, newSub);
                }
                
                if (localExceptionListener!=null && localExceptionListener.hasErrors())
                {
                    subService.setProvisionActionState(false);
                    state = ServiceStateEnum.UNPROVISIONEDWITHERRORS;
                    for (Throwable t : (List<Throwable>) localExceptionListener.getExceptions())
                    {
                        exceptionListener.thrown(t);
                    }
                }
                else
                {
                    
                    subService.setProvisionActionState(true);
                }
                oldSub.removeSuspendedService(ctx, subService.getService(ctx));
                oldSub.removeProvisionService(ctx, subService.getServiceId(), state, subService.getPath());
             	SubscriptionNotificationSupport.sendServiceStateChangeNotification(subCtx, oldSub, newSub, subService.getService(),null,NotificationTypeEnum.SERVICE_STATE_CHANGE,ServiceStateEnum.UNPROVISIONED);
            }
            catch (SPGSkippingException e)
            {
                // skip updating subscriber service, since spg has updated 
            }
            catch (final AgentException exception)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Error while unprovisioning service '");
                sb.append(service.getID());
                sb.append(" - ");
                sb.append(service.getName());
                sb.append("' from subscriber '");
                sb.append(oldSub.getId());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.major(ctx, ProvisioningSupport.class, sb.toString());

                // record the result code
                if ( exception instanceof ProvisionAgentException)
                {    
                    resultCodes.put( ((ProvisionAgentException)exception).getExternalApp(),(ProvisionAgentException) exception);
                }
                state = ServiceStateEnum.UNPROVISIONEDWITHERRORS;
                subService.setProvisionActionState(false);
                
                oldSub.removeSuspendedService(ctx, subService.getService(ctx));
                oldSub.removeProvisionService(ctx, service.getIdentifier(), state, SubscriberServicesUtil.DEFAULT_PATH);
            }
            finally
            {
                SubscriberSubscriptionHistorySupport.addProvisioningRecord(ctx, oldSub, 
                        HistoryEventTypeEnum.UNPROVISION, ChargedItemTypeEnum.SERVICE, subService.getService(ctx), state);
            }
        }

    }
    

    /**
     * Creates a Collection of Service for a Collection of
     * SubscriberServices.
     *
     * @param context
     *            The operating context.
     * @param associations
     *            A Collection of SubscriberServices.
     *
     * @return A Collection of Service.
     * @exception HomeException
     *                Thrown if there is a problem accessing Home information in the
     *                context.
     */
    public static Collection<Service> getServiceFromSubscriberServices(final Context context,
            final Collection<SubscriberServices> associations)
        throws HomeException
    {
        if (associations != null)
        {
            final Set<Long> idSet = new HashSet<Long>();

            final Iterator<SubscriberServices> associationIterator = associations.iterator();
            while (associationIterator.hasNext())
            {
                final SubscriberServices association = associationIterator.next();
                idSet.add(Long.valueOf(association.getServiceId()));
            }

            final Home home = (Home) context.get(ServiceHome.class);
            final And and = new And();
            and.add(new In(ServiceXInfo.ID, idSet));
           
            return home.select(context, and);
        }
        throw new HomeException("fail to retrieve service collection from invalid input");
    }
    
    /**
     * Verifies if the service is on the services for display
     * @param ctx
     * @param subscriber
     * @param serviceId
     * @return true if the service is on the services for display
     */
    public static boolean existsInServicesForIntentProvision(Context ctx, Subscriber subscriber, long serviceId)
    {
        Collection<ServiceFee2ID> collection = subscriber.getIntentToProvisionServiceIds();
        return collection.contains(Long.valueOf(serviceId));
    }

    
    /**
     * Return those Subscriber Services marked for Removal from a particular subscriber profile from the database
     * @param ctx the operating context
     * @param subscriberId the subscriber id
     * @return the list of services to be removed/unselected
     */
    public static Collection<Long> getUnprovisionedServices(final Context ctx, final String subscriberId)
    {
        final Predicate choice = getUnProvisionedSubscriberServicesPredicate();
        return getServiceIdsByFilter(ctx, subscriberId, choice);
    }

    public static String getServiceIdString(final Collection services)
    {
        final StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (final Iterator it = services.iterator(); it.hasNext();)
        {
            final Object obj = it.next();
            if (!firstTime)
            {
                sb.append(",");
            }
            else
            {
                firstTime = false;
            }

            if (obj instanceof SubscriberServices)
            {
                sb.append(((SubscriberServices) obj).getServiceId() + " threshold:"  + ((SubscriberServices) obj).getService().getClctThreshold());
            }
            else
            {
                throw new IllegalArgumentException("Invalid Service ID object, " + obj);
            }
        }
        return sb.toString();
    }
    
    /**
     * States that indicate an error has occurred during the Provisioning Process
     */
    public final static EnumCollection ERROR_STATES =
        new EnumCollection(new com.redknee.framework.xhome.xenum.Enum[]{
                ServiceStateEnum.PROVISIONEDWITHERRORS,
                ServiceStateEnum.SUSPENDEDWITHERRORS,
                ServiceStateEnum.UNPROVISIONEDWITHERRORS });
    
    /**
     * States that indicate an unprovisioned service
     */
    public static final Set<ServiceStateEnum> UNPROVISIONED_STATES;
    static
    {
        final Set<ServiceStateEnum> set = new HashSet<ServiceStateEnum>();
        set.add(ServiceStateEnum.UNPROVISIONED);
        set.add(ServiceStateEnum.UNPROVISIONEDWITHERRORS);
        UNPROVISIONED_STATES = Collections.unmodifiableSet(set);
    }
    
    public static Service getExternalPricePlanServiceCode(final Context ctx, final String subscriberId,
            boolean includeUnprovision)
    {
        final List<SubscriberServices> result = new ArrayList<SubscriberServices>();
        Collection<SubscriberServices> services = SubscriberServicesSupport.getAllSubscribersServices(ctx,subscriberId, includeUnprovision).values();
        if(services != null) 
        {
            for (final SubscriberServices subscriberService : services)
            {
                if(subscriberService != null)
                {
                    if (subscriberService.getService(ctx).getType().equals(ServiceTypeEnum.EXTERNAL_PRICE_PLAN))
                    {
                        result.add(subscriberService);
                    }
                }
            }
        }
        if(result.size() > 1) {
            Collections.sort(result, new ServiceStartDateComparator());
            return result.get(0).getService();
        } else  if(result.size() == 1) {
            return result.get(0).getService();
        } else {
        	return null;
        }
    }
    
    public static Service getExternalPricePlanServiceCode(final Context ctx, final Collection<SubscriberServices> services,
            boolean includeUnprovision)
    {
        final List<SubscriberServices> result = new ArrayList<SubscriberServices>();
        if(services != null)
        {
            for (final SubscriberServices subscriberService : services)
            {
                if(subscriberService != null) 
                {
                    if (subscriberService.getService(ctx).getType().equals(ServiceTypeEnum.EXTERNAL_PRICE_PLAN))
                    {
                        result.add(subscriberService);
                    }
                }
            }
        }
        if(result.size() > 1) {
            Collections.sort(result, new ServiceStartDateComparator());
            return result.get(0).getService();
        } else  if(result.size() == 1) {
            return result.get(0).getService();
        } else {
        	return null;
        }
    }


    public static boolean hasPermission(Context ctx, String permission)
    {
        AuthMgr authMgr = new AuthMgr(ctx);
        return authMgr.check(permission);
    }
}

class ServiceStartDateComparator implements Comparator<SubscriberServices>{

    @Override
    public int compare(SubscriberServices o1, SubscriberServices o2)
    {
        Date d1 = o1.getStartDate();        
        Date d2 = o2.getStartDate();

        if(d1.after(d2))
            return 1;
        else if(d1.before(d2))
            return -1;
        else
            return 0;    
    }
 }
