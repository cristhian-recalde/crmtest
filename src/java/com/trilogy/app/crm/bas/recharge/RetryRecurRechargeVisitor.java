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
package com.trilogy.app.crm.bas.recharge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.home.ExtendSubscriberExpiryHome;
import com.trilogy.app.crm.priceplan.BundleFeeExecutionOrderComparator;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesBulkUpdateSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Visitor to retry applying recurring charge.
 *
 * @author victor.stratan@redknee.com
 * @author sujeet.banerjee@redknee.com - Refactored
 */
public abstract class RetryRecurRechargeVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 
     * @param ctx
     * @param subscriber
     * @return
     */
    protected abstract Map<Long, Map<Long, SubscriberAuxiliaryService>> getSubscriberSuspendedAuxiliaryServices(
            Context ctx, Subscriber subscriber);

    /**
     * 
     * @param ctx
     * @param subscriber
     * @return
     */
    protected abstract Collection<BundleFee> getSubscriberSuspendedBundles(Context ctx,
            Subscriber subscriber);

    /**
     * 
     * @param ctx
     * @param subscriber
     * @return
     */
    protected abstract Map<ServiceFee2ID, ServiceFee2> getSubscriberSuspendedServices(Context ctx,
            Subscriber subscriber);

    /**
     * 
     * @param ctx
     * @param subscriber
     * @return
     */
    protected abstract Collection<ServicePackage> getSubscriberSuspendedPackages(
            Context ctx, Subscriber subscriber);


    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object object) throws AgentException, AbortVisitException
    {
        final Subscriber subscriber = (Subscriber) object;
        final Date billingDate = new Date();
        boolean isBulkServiceUpdate = Boolean.FALSE;
		List <SubscriberServices> newProvisionedServicesList = new ArrayList<SubscriberServices>(); 
		List <SubscriberServices> oldProvisionedServicesList = new ArrayList<SubscriberServices>();
		Map<ServiceFee2ID, SubscriberServices>  OSLMap = new HashMap<ServiceFee2ID, SubscriberServices>();
        
		try
		{
	        if (isRetryRecharge(ctx, subscriber))
	        {
	        	/*
	        	 *  Verizon feature - bulkServiceUpdate
	        	 */
	        	try 
	        	{
					isBulkServiceUpdate = SubscriberServicesBulkUpdateSupport.isBulkServiceUpdate(ctx, subscriber);
				} 
	        	catch (HomeException e) 
	        	{
					LogSupport.major(ctx, this,
								"Skipping bulkServiceUpdate for subscriber "+ subscriber.getId()
										+ " because of Exception while retrieving Provision Command : "+e.getMessage());
					isBulkServiceUpdate = Boolean.FALSE;
				}
	        	if (!isBulkServiceUpdate) 
	     		{
	     			LogSupport.info(ctx, this,
	     							"Skipping BulkServiceUpdate for subscriber " + subscriber.getId()
	     									+ " as bulkServiceUpdate provision command is not configured for the service provider "+ subscriber.getSpid()
	     									+ " OR HLR is disabled OR SkipBulkServiceUpdateForSubscriberCreation is enabled.");
	     		}
	        	else
	        	{
	        		try
	        		{
	        			OSLMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(ctx, subscriber.getId());
	        			if(LogSupport.isDebugEnabled(ctx))
	        	    	{
	        	    		LogSupport.debug(ctx, SubscriberServicesBulkUpdateSupport.class.getName(), "old Services List for subscriber [ "+subscriber.getId()+ "] is : "+OSLMap);
	        	    	}
	        			oldProvisionedServicesList = SubscriberServicesBulkUpdateSupport.preprocessing(ctx, OSLMap, subscriber);
	        		}
	        		catch (HomeException e) 
	        		{
	        			LogSupport
						.info(ctx,	SubscriberServicesBulkUpdateSupport.class.getName(),
								"Skipping "
										+ SubscriberServicesBulkUpdateSupport.class.getName()
										+ " as exception occured while looking up for bulkServiceUpdate provision command for service provider "
										+ subscriber.getSpid() + " : "+ e.getMessage()); 
	        			isBulkServiceUpdate=Boolean.FALSE;
	        		}
	        	}
	        	
	        	
	            try
	            {
	                final Context subContext = ctx.createSubContext();
	                
	                final PricePlanVersion currentVersion;
	                
	                currentVersion = PricePlanSupport.getCurrentVersion(ctx, subscriber.getPricePlan());
	                
	                if (subscriber.getPricePlanVersion() != currentVersion.getVersion())
	                {
	            
	                	LogSupport.debug(subContext, this, "Price plan version changed for subscriber: "
	                            + subscriber.getId()+",priceplan returning "+currentVersion.getVersion());
	                    subscriber.switchPricePlan(ctx, currentVersion.getId(), currentVersion.getVersion()); 
	                    Home subHome = (Home) subContext.get(SubscriberHome.class);
	                    subHome.store(subContext, subscriber);
	                    return;
	                }
	                
	                boolean goOn;
	                Map unsuspendPackages;
	                Map unsuspendServices = new HashMap();
	                Map unsuspendBundles = new HashMap();
	                Map<Long, Map<Long, SubscriberAuxiliaryService>> unsuspendAuxServices = new HashMap<Long, Map<Long, SubscriberAuxiliaryService>>();
	
	                ChargingCycleHandler monthlyHandler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MONTHLY);
	                ChargingCycleHandler weeklyHandler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.WEEKLY);
	                ChargingCycleHandler dailyHandler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.DAILY);
	                ChargingCycleHandler oneTimeHandler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.ONE_TIME);
	                ChargingCycleHandler multiDayHandler = ChargingCycleSupportHelper.get(ctx).getHandler(ChargingCycleEnum.MULTIDAY);
	                
	                final int spid = subscriber.getSpid();
	                int billCycleDay = subscriber.getAccount(ctx).getBillCycleDay(ctx);
	                
	                final double monthlyRate = monthlyHandler.calculateRate(ctx, billingDate, billCycleDay, spid);
	                final double weeklyRate = weeklyHandler.calculateRate(ctx, billingDate, billCycleDay, spid);
	                final double dailyRate = dailyHandler.calculateRate(ctx, billingDate, billCycleDay, spid);
	                final double multiDayRate = multiDayHandler.calculateRate(ctx, billingDate, billCycleDay, spid);
	
	                
	                final Date monthlyStartDate = monthlyHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay, spid);
	                final Date monthlyEndDate = monthlyHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid);
	                final Date weeklyStartDate = weeklyHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay, spid);
	                final Date weeklyEndDate = weeklyHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid);
	                final Date dailyStartDate = dailyHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay, spid);
	                final Date dailyEndDate = dailyHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid);
	                final Date multiDayStartDate = multiDayHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay, spid);
	                final Date multiDayEndDate = multiDayHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid);
	                
	                
		            // PickNPay feature: All Or Nothing Charging
	                boolean isAllOrNothingChargingFirstPass = Boolean.FALSE;
	                boolean suspendAll = Boolean.FALSE;
	                pickNPayChargeTotal = 0;
	               
	                PricePlan pp = HomeSupportHelper.get(ctx).findBean(ctx, PricePlan.class, new EQ(PricePlanXInfo.ID, subscriber.getPricePlan()));
	               
	                if(pp.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY))
	                {
		               	isAllOrNothingChargingFirstPass = Boolean.TRUE;
		               	
		               	Map<ServiceFee2ID, ServiceFee2> suspendServices = getSubscriberSuspendedServices(ctx, subscriber);
		               	Collection<ServiceFee2> serviceList = suspendServices.values();
		               	
		               	Map<ServiceFee2ID, ServiceFee2> pickNPayGroupServices = new HashMap<ServiceFee2ID, ServiceFee2>();
		               	Iterator i = serviceList.iterator();
		               	
		               	while(i.hasNext())
		               	{
		               		ServiceFee2 fee = (ServiceFee2) i.next();
		               		if(fee.isApplyWithinMrcGroup())
		               		{
		               			pickNPayGroupServices.put(new ServiceFee2ID(fee.getServiceId(),fee.getPath()), fee);
		               		}
		               	}
		               	
		               	rechargeServices(ctx, billingDate, subscriber, pickNPayGroupServices, monthlyRate, monthlyStartDate, monthlyEndDate, weeklyRate, weeklyStartDate, weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate, billCycleDay, 
		               			multiDayRate, multiDayStartDate, multiDayEndDate, spid, isAllOrNothingChargingFirstPass, suspendAll);
		               	
		               	
		               	
		               	Collection<BundleFee> suspendBundles = getSubscriberSuspendedBundles(ctx, subscriber);
		               	Collection <BundleFee> pickNPayGroupBundles = new ArrayList<BundleFee>();
		               	
		               	Iterator<BundleFee> bundleItr = suspendBundles.iterator();
		               	
		               	while(bundleItr.hasNext())
		               	{
		               		BundleFee fee = (BundleFee) bundleItr.next();
		               		if(fee.isApplyWithinMrcGroup())
		               		{
		               			pickNPayGroupBundles.add(fee);
		               		}
		               	}
		               	
		               	rechargeBundles(ctx, billingDate, subscriber, pickNPayGroupBundles, monthlyRate, monthlyStartDate, monthlyEndDate, weeklyRate, weeklyStartDate, weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate, billCycleDay, 
		               			multiDayRate, multiDayStartDate, multiDayEndDate, spid, isAllOrNothingChargingFirstPass);
		                   
		               	if(LogSupport.isDebugEnabled(subContext))
		               	{
		               		LogSupport.debug(subContext, this, "Total charge amount for unsuspension of PickNPay MRC group services and bundles of subscriber "+subscriber.getId()+" is : "+pickNPayChargeTotal);
		               	}
		               	
		               	if(pickNPayChargeTotal > 0)
		               	{
		               		long subscriberBalance = subscriber.getBalanceRemaining(ctx);
		               		if(subscriberBalance < pickNPayChargeTotal)
		   	            	{
		               			isAllOrNothingChargingFirstPass = Boolean.FALSE;
		               			suspendAll = Boolean.TRUE;
		               			
		               			LogSupport.info(ctx, this, "Subscriber "+subscriber.getId() +" balance "+subscriberBalance 
		               						+" is not enough to cover charges for PickNPay group services/ bundles. Unsuspension will be skipped.");
		               			
		               			return;
		               			
		   	            	} // else charge 
		               	}
		               	isAllOrNothingChargingFirstPass = Boolean.FALSE;
	                }
	                   
	                unsuspendPackages = rechargePackages(ctx, billingDate, subscriber, getSubscriberSuspendedPackages(ctx, subscriber), 
	                        monthlyRate, monthlyStartDate, monthlyEndDate, weeklyRate, weeklyStartDate,
	                        weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate);
	                SuspensionSupport.suspendPackages(ctx, subscriber, unsuspendPackages, false);
	
	                goOn = removeChargedPackages(ctx, subscriber, unsuspendPackages);
	
	                if (goOn)
	                {
	                    unsuspendServices = rechargeServices(ctx, billingDate, subscriber,
	                            getSubscriberSuspendedServices(ctx, subscriber), monthlyRate, monthlyStartDate, monthlyEndDate,
	                            weeklyRate, weeklyStartDate, weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate,
	                            billCycleDay,multiDayRate, multiDayStartDate, multiDayEndDate, spid, isAllOrNothingChargingFirstPass, suspendAll);
	                    SuspensionSupport.suspendServices(ctx, subscriber, unsuspendServices, false);
	                    goOn = removeChargedServices(ctx, subscriber, unsuspendServices);
	                }
	
	                if (goOn)
	                {
	                    unsuspendBundles = rechargeBundles(ctx, billingDate, subscriber, 
	                            getSubscriberSuspendedBundles(ctx, subscriber), monthlyRate, monthlyStartDate, monthlyEndDate,
	                            weeklyRate, weeklyStartDate, weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate,
	                            billCycleDay,multiDayRate, multiDayStartDate, multiDayEndDate, spid, isAllOrNothingChargingFirstPass);
	                    SuspensionSupport.suspendBundles(ctx, subscriber, unsuspendBundles, false);
	
	                    goOn = removeChargedBundles(ctx, subscriber, unsuspendBundles);
	                }
	
	                if (goOn)
	                {
	                    unsuspendAuxServices = rechargeAuxServices(ctx, billingDate, subscriber,
	                            getSubscriberSuspendedAuxiliaryServices(ctx,
	                                    subscriber), monthlyRate, monthlyStartDate, monthlyEndDate,
	                            weeklyRate, weeklyStartDate, weeklyEndDate, dailyRate, dailyStartDate, dailyEndDate,billCycleDay, multiDayRate, multiDayStartDate, multiDayEndDate, spid);
	                    removeChargedAuxServices(ctx, subscriber, unsuspendAuxServices);
	                    SuspensionSupport.suspendAuxServices(ctx, subscriber, unsuspendAuxServices, false, this);
	
	                }
	
	                if (unsuspendPackages.size() + unsuspendServices.size() + unsuspendBundles.size()
	                    + unsuspendAuxServices.size() > 0)
	                {
		                SubscriptionNotificationSupport.sendSuspendNotification(ctx, subscriber, unsuspendPackages, unsuspendServices,
		                        unsuspendBundles, unsuspendAuxServices, false);
		                
		                // Verizon integration feature
		                if(isBulkServiceUpdate)
		                {
		                	SubscriberServicesBulkUpdateSupport.postProcessing(ctx, subscriber, OSLMap, oldProvisionedServicesList, newProvisionedServicesList);
		                }
	
	                /* no need to save subscriber, because suspended fields are already saved */
	                }
	            }
	            catch (final Throwable throwable)
	            {
	                new MajorLogMsg(this, "Problem occurred while un-suspending entities for subscriber "
	                    + subscriber.getId() + " with insufficient balance.", throwable).log(ctx);
	            }
	        }
    	}
    	finally
    	{
    		Context appCtx = (Context) ctx.get("app");
            appCtx.remove(ExtendSubscriberExpiryHome.IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER + subscriber.getId());
    	}
    }


    /**
     * Determines whether the subscriber requires to be recharged.
     *
     * @param subscriber
     *            The subscriber to be recharged.
     * @return Whether the subscriber should be recharged.
     */
    private boolean isRetryRecharge(final Context ctx, final Subscriber subscriber)
    {
        boolean retry = false;

        final SubscriberStateEnum state = subscriber.getState();
        if (state == SubscriberStateEnum.INACTIVE)
        {
            // don't act on Inactive subscribers
            retry = false;
        }
        else
        {
            retry = subscriber.hasSuspended(ctx);
        }

        return retry;
    }
    
    
    private boolean isServiceToBeProvisioned(Context ctx, Subscriber sub, long serviceId)
    {
        boolean isClctService = SubscriberServicesSupport.isClctService(ctx, sub, serviceId);
        if(isClctService)
        {
            // Suspended Service is CLCT service, no need to Provision it
            return false;
        }
        else
        {
            return true;
        }        
    }


    /**
     * Remove charged packages from the suspended package list.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being charged.
     * @param charged
     *            Map of packages which have been charged.
     * @return Returns whether there are the subscriber has any suspended packages left.
     * @throws HomeException
     *             Thrown if there are problems removing a charged package from the
     *             suspended list.
     */
    private boolean removeChargedPackages(final Context ctx, final Subscriber sub, final Map charged)
        throws HomeException
    {
        final Iterator it = charged.values().iterator();
        while (it.hasNext())
        {
            sub.removeSuspendedPackage(ctx, (ServicePackage) it.next());
        }

        return sub.getSuspendedPackages(ctx).size() == 0;
    }


    /**
     * Remove charged services from the suspended services list.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being charged.
     * @param charged
     *            Map of services which have been charged.
     * @return Returns whether there are the subscriber has any suspended services left.
     * @throws HomeException
     *             Thrown if there are problems removing a charged service from the
     *             suspended list.
     */
    private boolean removeChargedServices(final Context ctx, final Subscriber sub, final Map charged)
        throws HomeException
    {
        final Iterator it = charged.values().iterator();
        while (it.hasNext())
        {
            sub.removeSuspendedService(ctx, (ServiceFee2) it.next());
        }

        return getSubscriberSuspendedServices(ctx, sub).size() == 0;
    }


    /**
     * Remove charged bundles from the suspended bundles list.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being charged.
     * @param charged
     *            Map of bundles which have been charged.
     * @return Returns whether there are the subscriber has any suspended bundles left.
     * @throws HomeException
     *             Thrown if there are problems removing a charged bundle from the
     *             suspended list.
     */
    private boolean removeChargedBundles(final Context ctx, final Subscriber sub, final Map charged)
        throws HomeException
    {
        final Iterator it = charged.values().iterator();
        while (it.hasNext())
        {
            sub.removeSuspendedBundles(ctx, (BundleFee) it.next());
        }

        return sub.getSuspendedBundles(ctx).size() == 0;
    }


    /**
     * Remove charged auxiliary services from the suspended auxiliary service list.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            The subscriber being charged.
     * @param charged
     *            Map of auxiliary services which have been charged.
     * @return Returns whether there are the subscriber has any suspended auxiliary
     *         services left.
     * @throws HomeException
     *             Thrown if there are problems removing a charged auxiliary services from
     *             the suspended list.
     */
    private boolean removeChargedAuxServices(final Context ctx, final Subscriber sub,
            final Map<Long, Map<Long, SubscriberAuxiliaryService>> charged)
        throws HomeException
    {
        for (final Map<Long, SubscriberAuxiliaryService> associations : charged.values())
        {
            for (final SubscriberAuxiliaryService association : associations.values())
            {
                sub.removeSuspendedAuxService(ctx, association);
            }
        }
        return sub.getNumSuspendedAuxService(ctx) == 0;
    }


    /**
     * Recharge packages.
     *
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            Billing date.
     * @param sub
     *            The subscriber being charged.
     * @param multiDayRate TODO
     * @param multiDayStartDate TODO
     * @param multiDayEndDate TODO
     * @param spid TODO
     * @param billCycleDay TODO
     * @param map
     *            Map of service packages to be recharged.
     * @return Returns map of charged service packages.
     * @throws HomeException
     *             Thrown if there are problems recharging the packages.
     */
    protected Map<Object, ServicePackage> rechargePackages(final Context ctx, final Date billingDate,
            final Subscriber sub, final Collection<ServicePackage> packages, final double monthlyRate, 
            final Date monthlyStartDate, final Date monthlyEndDate, final double weeklyRate,
            final Date weeklyStartDate, final Date weeklyEndDate, final double dailyRate, final Date dailyStartDate,
            final Date dailyEndDate) throws HomeException
    {
        final RechargeSubscriberServicePackageVisitor monthlyVisitor = new RechargeSubscriberServicePackageVisitor(
                billingDate, MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MONTHLY, sub, false, 
                monthlyStartDate, monthlyEndDate, monthlyRate, false, true, false);
        final RechargeSubscriberServicePackageVisitor weeklyVisitor = new RechargeSubscriberServicePackageVisitor(
                billingDate, WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.WEEKLY, sub, false, 
                weeklyStartDate, weeklyEndDate, weeklyRate, false, true, false);
        final RechargeSubscriberServicePackageVisitor dailyVisitor = new RechargeSubscriberServicePackageVisitor(billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.DAILY, sub, false, 
                dailyStartDate, dailyEndDate, dailyRate, false, true, false);


        final PricePlanVersion version = PricePlanSupport.getVersion(ctx,
                sub.getPricePlan(), sub.getPricePlanVersion());
        final Map<Integer, ServicePackageFee> packageFees = version.getServicePackageVersion().getPackageFees();

        final Map<Object, ServicePackage> result = new HashMap<Object, ServicePackage>();
        for (ServicePackage pack : packages)
        {
            final ServicePackageFee fee = packageFees.get(Integer.valueOf(pack.getId()));

            ServicePeriodHandler multiDayHandler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);
            
            
            boolean success = false;
            if (monthlyVisitor.isMatchingServicePeriod(fee))
            {
                if (monthlyVisitor.isChargeable(ctx, monthlyVisitor.getPack(ctx, fee.getPackageId()),
                        ChargedItemTypeEnum.SERVICEPACKAGE, monthlyVisitor.getPack(ctx, fee.getPackageId())
                                .getAdjustmentCode(), fee.getFee(), fee.getServicePeriod()))
                {
                    success = monthlyVisitor.handleServiceTransaction(ctx, fee);
                }
                else
                {
                    success = true;
                }
                success = monthlyVisitor.handleServiceTransaction(ctx, fee);
            }
            else if (weeklyVisitor.isMatchingServicePeriod(fee))
            {
                if (weeklyVisitor.isChargeable(ctx, weeklyVisitor.getPack(ctx, fee.getPackageId()),
                        ChargedItemTypeEnum.SERVICEPACKAGE, weeklyVisitor.getPack(ctx, fee.getPackageId())
                                .getAdjustmentCode(), fee.getFee(), fee.getServicePeriod()))
                {
                    success = weeklyVisitor.handleServiceTransaction(ctx, fee);
                }
                else
                {
                    success = true;
                }
            }
            else if (dailyVisitor.isMatchingServicePeriod(fee))
            {
                if (dailyVisitor.isChargeable(ctx, dailyVisitor.getPack(ctx, fee.getPackageId()),
                        ChargedItemTypeEnum.SERVICEPACKAGE, dailyVisitor.getPack(ctx, fee.getPackageId())
                        .getAdjustmentCode(), fee.getFee(), fee.getServicePeriod()))
                {
                    success = dailyVisitor.handleServiceTransaction(ctx, fee);
                }
                else
                {
                    success = true;
                }
            }

            if (success)
            {
                result.put(XBeans.getIdentifier(pack), pack);
            }
            else
            {
                break;
            }
        }
        return result;
    }



    /**
     * Recharge services.
     *
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            Billing date.
     * @param sub
     *            The subscriber being charged.
     * @param serviceFeeMap
     *            Map of services to be recharged.
     * @param multiDayRate TODO
     * @param multiDayStartDate TODO
     * @param multiDayEndDate TODO
     * @param spid TODO
     * @return Returns map of charged services.
     * @throws HomeException
     *             Thrown if the are problems recharging the services.
     */
    protected Map<Object, ServiceFee2> rechargeServices(final Context ctx, final Date billingDate, final Subscriber sub,
            final Map<ServiceFee2ID, ServiceFee2> serviceFeeMap, final double monthlyRate,       
            final Date monthlyStartDate, final Date monthlyEndDate, final double weeklyRate, 
            final Date weeklyStartDate, final Date weeklyEndDate, final double dailyRate, final Date dailyStartDate,
            final Date dailyEndDate, final int billCycleDay, double multiDayRate, Date multiDayStartDate, Date multiDayEndDate, int spid,
            boolean isAllOrNothingChargingFirstPass, boolean suspendAll) throws HomeException
    {
        ServicePeriodHandler oneTimeHandler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.ONE_TIME);        
        
        final RechargeSubscriberServiceVisitor monthlyVisitor = new RechargeSubscriberServiceVisitor(ctx, billingDate,
                MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MONTHLY, sub, false, monthlyStartDate, 
                monthlyEndDate, monthlyRate, false, true, false, false, false);
        final RechargeSubscriberServiceVisitor weeklyVisitor = new RechargeSubscriberServiceVisitor(ctx, billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.WEEKLY, sub, false, 
                weeklyStartDate, weeklyEndDate, weeklyRate, false, true, false, false, false);
        final RechargeSubscriberServiceVisitor dailyVisitor = new RechargeSubscriberServiceVisitor(ctx, billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.DAILY, sub, false, 
                dailyStartDate, dailyEndDate, dailyRate, false, true, false, false, false);


        final Map<Object, ServiceFee2> result = new HashMap<Object, ServiceFee2>();
        
        Collection<ServiceFee2> chargeableFees = sub.getChargeableFees(ctx, serviceFeeMap);

        Collection<ServiceFee2> sortedFees = PricePlanSupport.getServiceByExecutionOrder(ctx, chargeableFees);
        for (ServiceFee2 serviceFee : sortedFees)
        { // check if PP is pickNPay, do total of MRC group services, compare with balance, if not enough then suspend all, else continue
        	ServicePeriodHandler multiDayHandler  = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);
            final RechargeSubscriberServiceVisitor multiDayVisitor = new RechargeSubscriberServiceVisitor(ctx, billingDate,
                    MultiDayRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MULTIDAY, sub, false, 
                    multiDayStartDate, multiDayHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid, sub.getId(), serviceFee), multiDayRate, false, true, false, isAllOrNothingChargingFirstPass, suspendAll);        

            multiDayVisitor.setOrigServiceEndDate(multiDayVisitor.getEndDate());
            
            boolean success = false;
            
            Service service = ServiceSupport.getService(ctx, serviceFee.getServiceId());
            if (monthlyVisitor.isMatchingServicePeriod(serviceFee))
            {
            	if (service != null && service.isForceCharging())
            	{
            		success = monthlyVisitor.handleServiceTransaction(ctx, serviceFee);
            	}
            	else if (monthlyVisitor.isChargeable(ctx, serviceFee.getService(ctx),
                        ChargedItemTypeEnum.SERVICE, serviceFee.getService(ctx)
                                .getAdjustmentType(), serviceFee.getFee(), serviceFee.getServicePeriod()))
                {
                    success = monthlyVisitor.handleServiceTransaction(ctx, serviceFee);
                }
                else
                {
                    success = isServiceToBeProvisioned(ctx, sub, service.getID());
                }
            }
            else if (weeklyVisitor.isMatchingServicePeriod(serviceFee))
            {
            	if (service != null && service.isForceCharging())
            	{
            		success = weeklyVisitor.handleServiceTransaction(ctx, serviceFee);
            	}
            	else if (weeklyVisitor.isChargeable(ctx, serviceFee.getService(ctx),
                        ChargedItemTypeEnum.SERVICE, serviceFee.getService(ctx)
                                .getAdjustmentType(), serviceFee.getFee(), serviceFee.getServicePeriod()))
                {
                    success = weeklyVisitor.handleServiceTransaction(ctx, serviceFee);
                }
                else
                {
                    success = isServiceToBeProvisioned(ctx, sub, service.getID());
                }
            }
            else if (dailyVisitor.isMatchingServicePeriod(serviceFee))
            {
            	if (service != null && service.isForceCharging())
            	{
            		success = dailyVisitor.handleServiceTransaction(ctx, serviceFee);
            	}
            	else if (dailyVisitor.isChargeable(ctx, serviceFee.getService(ctx),
                        ChargedItemTypeEnum.SERVICE, serviceFee.getService(ctx)
                        .getAdjustmentType(), serviceFee.getFee(), serviceFee.getServicePeriod()))
                {
                    success = dailyVisitor.handleServiceTransaction(ctx, serviceFee);
                }
                else
                {
                    success = isServiceToBeProvisioned(ctx, sub, service.getID());
                }
            }
            else if (multiDayVisitor.isMatchingServicePeriod(serviceFee))
            {
            	if (service != null && service.isForceCharging())
            	{
            		success = multiDayVisitor.handleServiceTransaction(ctx, serviceFee);
            		if(isAllOrNothingChargingFirstPass)
                    {
                    	pickNPayChargeTotal = pickNPayChargeTotal + multiDayVisitor.getChargeAmountSuccess();
                    }
            	}
            	else if (multiDayVisitor.isChargeable(ctx, serviceFee.getService(ctx),
                        ChargedItemTypeEnum.SERVICE, serviceFee.getService(ctx)
                        .getAdjustmentType(), serviceFee.getFee(), serviceFee.getServicePeriod()))
                {
                    success = multiDayVisitor.handleServiceTransaction(ctx, serviceFee);
                    if(isAllOrNothingChargingFirstPass)
                    {
                    	pickNPayChargeTotal = pickNPayChargeTotal + multiDayVisitor.getChargeAmountSuccess();
                    }
                }
                else
                {
                    success = isServiceToBeProvisioned(ctx, sub, service.getID());
                }
            }            
            else if (SafetyUtil.safeEquals(serviceFee.getServicePeriod().getChargingCycle(), ChargingCycleEnum.ONE_TIME))
            {
                final Date otcStartDate = oneTimeHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay,
                        sub.getSpid(), sub.getId(), serviceFee);
                final Date otcEndDate = oneTimeHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay,
                        sub.getSpid(), sub.getId(), serviceFee);
                final RechargeSubscriberServiceVisitor otcVisitor = new RechargeSubscriberServiceVisitor(ctx,
                        billingDate, WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.ONE_TIME,
                        sub, false, otcStartDate, otcEndDate, 1.0, false, true, false, false, false);
                if (    //config ||
                        otcVisitor.isChargeable(ctx, serviceFee.getService(ctx),
                        ChargedItemTypeEnum.SERVICE, serviceFee.getService(ctx)
                        .getAdjustmentType(), serviceFee.getFee(), serviceFee.getServicePeriod()))
                {
                    success = otcVisitor.handleServiceTransaction(ctx, serviceFee);
                }
                else
                {
                    success = isServiceToBeProvisioned(ctx, sub, service.getID());
                }
                
            }
            if (success)
            {
                result.put(XBeans.getIdentifier(serviceFee), serviceFee);
            }
            else
            {
                break;
            }
        }
        return result;
    }


    /**
     * Recharge bundles.
     *
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            Billing date.
     * @param sub
     *            The subscriber being charged.
     * @param multiDayRate TODO
     * @param multiDayStartDate TODO
     * @param multiDayEndDate TODO
     * @param spid TODO
     * @param map
     *            Map of bundles to be recharged.
     * @return Returns map of charged bundles.
     * @throws HomeException
     *             Thrown if there are problems recharging the bundles.
     */
    protected Map<Object, BundleFee> rechargeBundles(final Context ctx, final Date billingDate, final Subscriber sub,
            final Collection<BundleFee> bundleFees, final double monthlyRate, final Date monthlyStartDate,
            final Date monthlyEndDate, final double weeklyRate, final Date weeklyStartDate, final Date weeklyEndDate,
            final double dailyRate, final Date dailyStartDate, final Date dailyEndDate, final int billCycleDay, 
            double multiDayRate, Date multiDayStartDate, Date multiDayEndDate, int spid, boolean isAllOrNothingChargingFirstPass ) throws HomeException
    {
        ServicePeriodHandler oneTimeHandler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.ONE_TIME);        
        
        final RechargeSubscriberBundleVisitor monthlyVisitor = new RechargeSubscriberBundleVisitor(billingDate,
                MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MONTHLY, sub, false,
                monthlyStartDate, monthlyEndDate, monthlyRate, false, true, false, false);
        final RechargeSubscriberBundleVisitor weeklyVisitor = new RechargeSubscriberBundleVisitor(billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.WEEKLY, sub, false,
                weeklyStartDate, weeklyEndDate, weeklyRate, false, true, false, false);
        final RechargeSubscriberBundleVisitor dailyVisitor = new RechargeSubscriberBundleVisitor(billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.DAILY, sub, false, dailyStartDate,
                dailyEndDate, dailyRate, false, true, false, false);

        
        final Map<Object, BundleFee> result = new HashMap<Object, BundleFee>();

        List<BundleFee> sortedBundleFee= new ArrayList<BundleFee>(bundleFees);
        Collections.sort(sortedBundleFee, new BundleFeeExecutionOrderComparator(ctx, true));
        
        for (BundleFee bundleFee : sortedBundleFee)
        {
        	ServicePeriodHandler multiDayHandler  = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);   
            final RechargeSubscriberBundleVisitor multiDayVisitor = new RechargeSubscriberBundleVisitor(billingDate,
                    MultiDayRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MULTIDAY, sub, false, multiDayStartDate,
                    multiDayHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid, sub.getId(), bundleFee), multiDayRate, false, true, false, isAllOrNothingChargingFirstPass);        	

            boolean success = false;
            try
            {
                BundleProfile bundle = bundleFee.getBundleProfile(ctx, spid);
                
                if (monthlyVisitor.isMatchingServicePeriod(bundleFee))
                {
                    if (monthlyVisitor.isChargeable(ctx, bundle,
                            bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE,
                                    bundle.getAdjustmentType(), bundleFee.getFee(), bundleFee.getServicePeriod()))
                    {
                        success = monthlyVisitor.handleBundleTransaction(ctx, bundleFee);
                    }
                    else
                    {
                        success = true;
                    }
                }
                else if (weeklyVisitor.isMatchingServicePeriod(bundleFee))
                {
                    if (weeklyVisitor.isChargeable(ctx, bundle,
                            bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE,
                                    bundle.getAdjustmentType(),  bundleFee.getFee(), bundleFee.getServicePeriod()))
                    {
                        success = weeklyVisitor.handleBundleTransaction(ctx, bundleFee);
                    }
                    else
                    {
                        success = true;
                    }
                }
                else if (dailyVisitor.isMatchingServicePeriod(bundleFee))
                {
                    if (dailyVisitor.isChargeable(ctx, bundle,
                            bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE,
                                    bundle.getAdjustmentType(),  bundleFee.getFee(), bundleFee.getServicePeriod()))
                    {
                        success = dailyVisitor.handleBundleTransaction(ctx, bundleFee);
                    }
                    else
                    {
                        success = true;
                    }
                }
                else if (multiDayVisitor.isMatchingServicePeriod(bundleFee))
                {
                	if (bundleFee != null && bundleFee.getBundleProfile(ctx).isForceCharging())
                	{
                		success = multiDayVisitor.handleBundleTransaction(ctx, bundleFee);
                        
                        if(!bundleFee.isAuxiliarySource() && isAllOrNothingChargingFirstPass)
                        {
                        	pickNPayChargeTotal = pickNPayChargeTotal + multiDayVisitor.getChargeAmountSuccess();
                        }
                	}
                	else if (multiDayVisitor.isChargeable(ctx, bundle,
                            bundleFee.isAuxiliarySource() ? ChargedItemTypeEnum.AUXBUNDLE : ChargedItemTypeEnum.BUNDLE,
                                    bundle.getAdjustmentType(),  bundleFee.getFee(), bundleFee.getServicePeriod()))
                    {
                        success = multiDayVisitor.handleBundleTransaction(ctx, bundleFee);
                        
                        if(!bundleFee.isAuxiliarySource() && isAllOrNothingChargingFirstPass)
                        {
                        	pickNPayChargeTotal = pickNPayChargeTotal + multiDayVisitor.getChargeAmountSuccess();
                        }
                    }
                    else
                    {
                        success = true;
                    }
                }                
                else if (SafetyUtil.safeEquals(bundleFee.getServicePeriod().getChargingCycle(), ChargingCycleEnum.ONE_TIME))
                {
                    final Date otcStartDate = oneTimeHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay,
                            sub.getSpid(), sub.getId(), bundleFee);
                    final Date otcEndDate = oneTimeHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay,
                            sub.getSpid(), sub.getId(), bundleFee);
                    final RechargeSubscriberBundleVisitor otcVisitor = new RechargeSubscriberBundleVisitor(billingDate,
                            WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.ONE_TIME, sub, false, otcStartDate,
                            otcEndDate, 1.0, false, true, false, false);
                    if ( // config ||
                    otcVisitor.isChargeable(ctx, bundle, bundleFee.isAuxiliarySource()
                            ? ChargedItemTypeEnum.AUXBUNDLE
                            : ChargedItemTypeEnum.BUNDLE, bundle.getAdjustmentType(),
                            bundleFee.getFee(), bundleFee.getServicePeriod()))
                    {
                        success = otcVisitor.handleBundleTransaction(ctx, bundleFee);
                    }
                    else
                    {
                        success = true;
                    }
                }
                if (success)
                {
                    result.put(XBeans.getIdentifier(bundleFee), bundleFee);
                }
                else
                {
                    break;
                }
            } 
            catch (BundleDoesNotExistsException e)
            {
                throw new HomeException("Bundle id " + bundleFee.getId() + " does not exist: " + e.getMessage(), e);
            }
            catch (Exception e)
            {
                throw new HomeException("Bundle id " + bundleFee.getId() + " error: " + e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * Recharge auxiliary services.
     *
     * @param ctx
     *            The operating context.
     * @param billingDate
     *            Billing date.
     * @param sub
     *            The subscriber being charged.
     * @param multiDayRate TODO
     * @param multiDayStartDate TODO
     * @param multiDayEndDate TODO
     * @param spid TODO
     * @param map
     *            Map of auxiliary services to be recharged.
     * @return Returns map of charged auxiliary services.
     */
    protected Map<Long, Map<Long, SubscriberAuxiliaryService>> rechargeAuxServices(final Context ctx,
            final Date billingDate, final Subscriber sub, final Map<Long,Map<Long,SubscriberAuxiliaryService>> auxServices, 
            final double monthlyRate, final Date monthlyStartDate, final Date monthlyEndDate, final double weeklyRate, 
            final Date weeklyStartDate, final Date weeklyEndDate, final double dailyRate, final Date dailyStartDate,
            final Date dailyEndDate, final int billCycleDay, double multiDayRate, Date multiDayStartDate, Date multiDayEndDate, int spid) throws HomeException
    {
        ServicePeriodHandler oneTimeHandler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.ONE_TIME);        
        
        final RechargeSubscriberAuxServiceVisitor monthlyVisitor = new RechargeSubscriberAuxServiceVisitor(billingDate,
                MonthlyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MONTHLY, sub, false, 
                monthlyStartDate, monthlyEndDate, monthlyRate, false, true, false);
        final RechargeSubscriberAuxServiceVisitor weeklyVisitor = new RechargeSubscriberAuxServiceVisitor(billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.WEEKLY, sub, false, 
                weeklyStartDate, weeklyEndDate, weeklyRate, false, true, false);
        final RechargeSubscriberAuxServiceVisitor dailyVisitor = new RechargeSubscriberAuxServiceVisitor(billingDate,
                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.DAILY, sub, false, 
                dailyStartDate, dailyEndDate, dailyRate, false, true, false);
       

        
        final Map<Long, Map<Long, SubscriberAuxiliaryService>> result = new HashMap<Long, Map<Long, SubscriberAuxiliaryService>>();
        for (final Long id : auxServices.keySet())
        {
            final Map<Long, SubscriberAuxiliaryService> associations = auxServices.get(id);
            final HashMap<Long, SubscriberAuxiliaryService> auxServiceResult = new HashMap<Long, SubscriberAuxiliaryService>();
            for (final Long secondaryId : associations.keySet())
            {
                final SubscriberAuxiliaryService association = associations.get(secondaryId);
                try
                {
                    AuxiliaryService service = association.getAuxiliaryService(ctx);
                    
                    ServicePeriodHandler multiDayHandler = ServicePeriodSupportHelper.get(ctx).getHandler(ServicePeriodEnum.MULTIDAY);
                    final RechargeSubscriberAuxServiceVisitor multiDayVisitor = new RechargeSubscriberAuxServiceVisitor(billingDate,
                            MultiDayRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.MULTIDAY, sub, false, 
                            multiDayStartDate, multiDayHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay, spid, sub.getId(), service), 
                            multiDayRate, false, true, false);                     
                    
                    boolean success = false;
                    if (monthlyVisitor.isMatchingServicePeriod(ctx, service))
                    {
                        if (monthlyVisitor.isChargeable(ctx, association, ChargedItemTypeEnum.AUXSERVICE,
                                service.getAdjustmentType(), service.getCharge(), 
                                service.getChargingModeType()))
                        {
                            success = monthlyVisitor.handleServiceTransaction(ctx, association);
                        }
                        else
                        {
                            success = true;
                        }
                    }
                    else if (weeklyVisitor.isMatchingServicePeriod(ctx, service))
                    {
                        if (weeklyVisitor.isChargeable(ctx, association, ChargedItemTypeEnum.AUXSERVICE, 
                                service.getAdjustmentType(),  service.getCharge(), 
                                service.getChargingModeType()))
                        {
                            success = weeklyVisitor.handleServiceTransaction(ctx, association);
                        }
                        else
                        {
                            success = true;
                        }
                    }
                    else if (dailyVisitor.isMatchingServicePeriod(ctx, service))
                    {
                        if (dailyVisitor.isChargeable(ctx, association, ChargedItemTypeEnum.AUXSERVICE, 
                                service.getAdjustmentType(),  service.getCharge(), 
                                service.getChargingModeType()))
                        {
                            success = dailyVisitor.handleServiceTransaction(ctx, association);
                        }
                        else
                        {
                            success = true;
                        }
                    }
                    else if (multiDayVisitor.isMatchingServicePeriod(ctx, service))
                    {
                        if (multiDayVisitor.isChargeable(ctx, association, ChargedItemTypeEnum.AUXSERVICE, 
                                service.getAdjustmentType(),  service.getCharge(), 
                                service.getChargingModeType()))
                        {
                            success = multiDayVisitor.handleServiceTransaction(ctx, association);
                        }
                        else
                        {
                            success = true;
                        }
                    }                    
                    else if (SafetyUtil.safeEquals(service.getChargingModeType().getChargingCycle(), ChargingCycleEnum.ONE_TIME))
                    {
                        if (service.getStartDate() == null)
                        {
                            service.setStartDate(association.getStartDate());
                        }
                        if (service.getEndDate() == null)
                        {
                            service.setEndDate(association.getEndDate());
                        }
                        final Date otcStartDate = oneTimeHandler.calculateCycleStartDate(ctx, billingDate, billCycleDay,
                                sub.getSpid(), sub.getId(), service);
                        final Date otcEndDate = oneTimeHandler.calculateCycleEndDate(ctx, billingDate, billCycleDay,
                                sub.getSpid(), sub.getId(), service);
                        final RechargeSubscriberAuxServiceVisitor otcVisitor = new RechargeSubscriberAuxServiceVisitor(billingDate,
                                WeeklyRecurringRechargesLifecycleAgent.AGENT_NAME, ChargingCycleEnum.ONE_TIME, sub, false, 
                                otcStartDate, otcEndDate, 1.0, false, true, false);
                        
                        if (otcVisitor.isChargeable(ctx, association, ChargedItemTypeEnum.AUXSERVICE, 
                                service.getAdjustmentType(),  service.getCharge(), 
                                service.getChargingModeType()))
                        {
                            success = otcVisitor.handleServiceTransaction(ctx, association);
                        }
                        else
                        {
                            success = true;
                        }
                    }
                    
                    if (success)
                    {
                        auxServiceResult.put(secondaryId, association);
                    }
                    else
                    {
                        break;
                    }
                }
                catch (HomeException e)
                {
                    new MajorLogMsg(this, "Problem occurred while suspending auxiliary service "
                            + association.getAuxiliaryServiceIdentifier() + " for prepaid subscriber "
                            + association.getSubscriberIdentifier() + " with insufficient balance: "
                            + e.getMessage(), e).log(ctx);
                }
            }
            
            if (!auxServiceResult.isEmpty())
            {
                result.put(id, auxServiceResult);
            }
        }
        return result;
    }
    
    private long pickNPayChargeTotal = 0;
    
}
