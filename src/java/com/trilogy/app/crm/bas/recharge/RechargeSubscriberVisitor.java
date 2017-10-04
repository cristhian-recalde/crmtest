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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.api.rmi.PricePlanToApiAdapter;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.RecurringRecharge;
import com.trilogy.app.crm.bean.SPGServiceStateMappingConfig;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePackageVersion;
import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.app.crm.bean.SubModificationScheduleHome;
import com.trilogy.app.crm.bean.SubModificationScheduleXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.paymentgatewayintegration.SubscriptionSupport;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.contract.SubscriptionContract;
import com.trilogy.app.crm.contract.SubscriptionContractXInfo;
import com.trilogy.app.crm.home.ExtendSubscriberExpiryHome;
import com.trilogy.app.crm.home.OcgForwardTransactionHome;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.priceplan.BundleFeeExecutionOrderComparator;
import com.trilogy.app.crm.service.ChargingCycleHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ChargingCycleSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberServicesBulkUpdateSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.holder.IntegerHolder;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitors;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.selfcareapi.soap.common.xsd._2010._09.SelfcareRequestHeader;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.OptionReference;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.SubscriptionReference;
import com.trilogy.util.selfcareapi.wsdl.v2_0.api.PricePlanService;


/**
 * Generate recurring charge for each subscriber.
 *
 * @author larry.xia@redknee.com
 */

public class RechargeSubscriberVisitor extends AbstractRechargeVisitor implements ContextAgent
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>RechargeSubscriberVisitor</code>.
     *
     * @param billingDate
     *            Billing date.
     * @param agentName
     *            Name of the agent invoking this recurring charge.
     * @param servicePeriod
     *            Service period to charge.
     */
    public RechargeSubscriberVisitor(final Date billingDate, final String agentName,
            final ChargingCycleEnum chargingPeriod, final Date startDate, final Date endDate, final boolean recurringRecharge, final boolean proRated, final boolean preWarnNotificationOnly)
    {
        super(billingDate, agentName, chargingPeriod, recurringRecharge, proRated, preWarnNotificationOnly);
        startDate_ = startDate;
        endDate_ = endDate;
        allTransactionIds_ = new HashSet<Long>();
    }
    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx) throws AgentException
    {
        final Subscriber subscriber = (Subscriber) ctx.get(Subscriber.class);
        
        /*
         * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
         * Reason: TT#12051459060; Table; last row
         * 
         * For prepaid, we will suspend anyways on recurring charge calculations    .
         * 
         * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
         * will be done only when the "restrict provisioning" flag is true. And we have the requirement
         * in the TT to move it to suspended state.
         * 
         */
        
        visit(ctx, subscriber);
        new SuspendEntitiesVisitor().visit(ctx, subscriber);
    }
    /**
     * {@inheritDoc}
     */
    public void visit(final Context ctx, final Object obj)
    {
        final Subscriber sub = (Subscriber) obj;
        sub.setContext(ctx);
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        
        final PMLogMsg pmLogMsg = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Process Subscriber", details.toString());
        try
        {
                	
        	final PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(ctx, sub.getPricePlan());
            
            if (sub.getPricePlanVersion() != currentVersion.getVersion())
            {
            	Subscriber subscriber = (Subscriber)sub.clone();
            	LogSupport.info(ctx, this, "Price plan version changed for subscriber: "
                        + subscriber.getId()+",priceplan returning "+currentVersion.getVersion());
            	subscriber.switchPricePlan(ctx, currentVersion.getId(), currentVersion.getVersion()); 
                Home subHome = (Home) ctx.get(SubscriberHome.class);
                subHome.store(ctx, subscriber);
                return;
            }

        	
            final Date billingDateWithNoTimeOfDay = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(getBillingDate());

            if (billingDateWithNoTimeOfDay.after(sub.getStartDate()))
            {
            	final Context subContext = ctx.createSubContext();
            	boolean notificationSendForScheduledPricePlan = false;

            	CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, sub.getSpid());

            	// Spid level flag to decide to check if any scheduled price plan is there then send the total cost of new price plan instead of 
            	//sending Pre notification for MRC            	
            	if(crmSpid.isScheduledPricePlanInsufficientChargeNotification() && isPreWarnNotificationOnly() && getChargingCycle().equals(ChargingCycleEnum.MULTIDAY))
            	{
            		notificationSendForScheduledPricePlan = sendScheduledPricePlanNotification(subContext, sub,billingDateWithNoTimeOfDay);
            	}

            	if(!notificationSendForScheduledPricePlan)
            		chargeSubscriber(subContext, sub);
            }
        }
        catch (final Throwable t)
        {
            LogSupport.minor(ctx, this, "Error when processing subscriber " + sub.getId(), t);
            handleException(ctx, sub, "Fail to charge subscriber " + t.getMessage());
        }
        finally
        {
            pmLogMsg.log(ctx);
            Context appCtx = (Context) ctx.get("app");
            appCtx.remove(ExtendSubscriberExpiryHome.IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER + sub.getId());
        }
    }


    /*
     * This method is used to send pre notification for scheduled price plan
     * Assumption : only MRC group services and bundles comes under Price PLan option change
     * 
     * TODO: Right now its working for multiday services only in future to make it work for all type of services
     * it has to calculate the fees by using specific handler for each service type
     */
    private boolean sendScheduledPricePlanNotification(final Context context, final Subscriber sub, final Date startDate) throws HomeInternalException, HomeException
    {
    	String resultDescription = "";		
    	String notificationMessageDescription = "";
    	boolean notificationSend = false;
    	RecurringRecharge recurringRecharge = new RecurringRecharge();

    	for(SubModificationSchedule schedule : getSubModificationSchedules(context, sub, startDate))
    	{
    		List snapshotList = schedule.getSnapshot();    			
    		String[] requestSnapshotArr = null;
    		long totalCharge = 0;
    		Map<Class, List<Object>> items = new HashMap<Class,List<Object>>();


    		if(snapshotList == null || snapshotList.size() != 1 || snapshotList.size() ==0){
    			resultDescription = "Scheduled priceplan data improper for subscriber: [" + schedule.getSubscriptionId() + "]";
    			if (LogSupport.isEnabled(context, SeverityEnum.DEBUG))
    				LogSupport.debug(context,this, resultDescription);	    			
    			notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG; 
    		}
    		else
    			requestSnapshotArr = ((StringHolder)snapshotList.get(0)).getString().split(",");  

    		// creating snapshot map with key value pair
    		Map<String, String> requestSnapshotMap = new HashMap<String, String>(requestSnapshotArr.length);
    		for(String requestSnapshot: requestSnapshotArr){
    			if (requestSnapshot.split("=").length > 1)
    			{
    				requestSnapshotMap.put(requestSnapshot.split("=")[0], requestSnapshot.split("=")[1]);
    			}
    		}

    		long toProvisionPriceplan =  Long.parseLong(requestSnapshotMap.get(CronConstant.NEW_PRICEPLAN_ID));

    		OptionReference[] requestedServiceOptionRefernceArr = null;//new OptionReference[optionReferenceSize];
    		List<String> requestedServicesList = null;
    		List<String> requestedBundlesList = null;

    		if(requestSnapshotMap.get(CronConstant.SERVICES) != null && !requestSnapshotMap.get(CronConstant.SERVICES).equals("")){
    			requestedServicesList = Arrays.asList(requestSnapshotMap.get(CronConstant.SERVICES).split("\\|"));
    		}
    		if(requestSnapshotMap.get(CronConstant.BUNDLES) != null && !requestSnapshotMap.get(CronConstant.BUNDLES).equals("")){
    			requestedBundlesList = Arrays.asList(requestSnapshotMap.get(CronConstant.BUNDLES).split("\\|"));
    		}	

    		PricePlan plan = PricePlanSupport.getPlan(context, toProvisionPriceplan);  
    		PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(context, toProvisionPriceplan);

    		int currentVerionOfToProvisionPriceplan = -1;
    		if(currentVersion != null)
    		{
    			currentVerionOfToProvisionPriceplan = currentVersion.getVersion();
    		}

    		PricePlanVersion version = PricePlanSupport.getVersion(context, plan.getId(), currentVerionOfToProvisionPriceplan);

    		recurringRecharge.setBillingDate(getBillingDate());
    		recurringRecharge.setChargingCycle(getChargingCycle());
    		recurringRecharge.setSubscriberId(sub.getId());
    		recurringRecharge.setTotalCharge(0);

    		List<Object> services = new ArrayList<Object>() ;
    		List<Object> bundles = new ArrayList<Object>() ;

    		// get all services and bundles under the price plan
    		Map<ServiceFee2ID,ServiceFee2> serviceFee = version.getServiceFees(context);
    		Map<Long,BundleFee> bundlefees = version.getServicePackageVersion(context).getBundleFees();

    		for (String serviceId : requestedServicesList)
    		{
    			long id = Long.parseLong(serviceId);
    			
    			ServiceFee2 fee = serviceFee.get(Long.parseLong(serviceId));
    			totalCharge += fee.getFee();
    			services.add(fee);
    		}
    		for (String bundleId : requestedBundlesList)
    		{
    			BundleFee fee = bundlefees.get(Long.parseLong(bundleId));
    			totalCharge += fee.getFee();
    			bundles.add(fee);
    		}

    		items.put(ServiceFee2.class, services);
    		items.put(BundleFee.class, bundles);    		

    		if(sub.getBalanceRemaining(context) < totalCharge && !SubscriptionSupport.isTopUpScheduledForSubscription(context, sub))
    		{
    			if(LogSupport.isDebugEnabled(context))
    			{
    				LogSupport.debug(context, this, "Insufficient balance notification is being sent" + " for Subscriber:[ID:" + sub.getId() 
    						+ "]/[MSISDN:" + sub.getMsisdn());
    			}
    			recurringRecharge.setTotalCharge(totalCharge);
    			SubscriptionNotificationSupport.sendPreWarnInsufficientBalanceNotification(context, sub, items, getBillingDate(),
    					getChargingCycle(), recurringRecharge);
    		}
    		else
    		{
    			if (LogSupport.isEnabled(context, SeverityEnum.DEBUG))
    				LogSupport.debug(context,this, "Balance is sufficient for scheduled price plan");
    		}
    		notificationSend = true;
    		break;
    	}    
    	
    	return notificationSend;
    }
    
    private boolean isPricePlanSwitching(Context ctx, Subscriber subscriber , long toProvisionPriceplan) throws HomeException{
    	boolean isPricePlanSwitched = false;

    	PricePlan plan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan); 

    	if (plan != null && plan.getIdentifier() != subscriber.getPricePlan() )
    		isPricePlanSwitched = true;

    	return isPricePlanSwitched;
    }
    
    
    private Collection<SubModificationSchedule> getSubModificationSchedules(final Context context,final Subscriber sub,
    		final Date startDate) throws HomeException
    {
    	And predicate = new And();

    	Calendar endCal = CalendarSupportHelper.get(context).dateToCalendar(startDate);
    	endCal.add(Calendar.DAY_OF_YEAR, 1);

    	predicate.add(new LT(SubModificationScheduleXInfo.SCHEDULED_TIME, 
    			CalendarSupportHelper.get(context).calendarToDate(endCal)));
    	predicate.add(new GTE(SubModificationScheduleXInfo.SCHEDULED_TIME,
    			startDate));
    	predicate.add(new EQ(SubModificationScheduleXInfo.STATUS, CronConstant.SCHEDULED_PENDING));
    	predicate.add(new EQ(SubModificationScheduleXInfo.SUBSCRIPTION_ID, sub.getId()));

    	return HomeSupportHelper.get(context).getBeans(context, SubModificationSchedule.class,predicate);
    }

    /**
     * Applies charges for the subscriber.
     *
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to be charged.
     */
    public void chargeSubscriber(final Context context, final Subscriber sub)
    {
        context.put(Subscriber.class, sub);

        Map<Class, List<Object>> chargedItems = new HashMap<Class,List<Object>>();
        Map<Class, List<Object>> chargingFailedItems = new HashMap<Class, List<Object>>();
        
        boolean isBulkServiceUpdate = Boolean.FALSE;
		List <SubscriberServices> newProvisionedServicesList = new ArrayList<SubscriberServices>(); 
		List <SubscriberServices> oldProvisionedServicesList = new ArrayList<SubscriberServices>();
		Map<ServiceFee2ID, SubscriberServices>  OSLMap = new HashMap<ServiceFee2ID, SubscriberServices>();
        
        /*
    	 *  Verizon feature - bulkServiceUpdate
    	 */
    	try 
    	{
			isBulkServiceUpdate = SubscriberServicesBulkUpdateSupport.isBulkServiceUpdate(context, sub);
		} 
    	catch (HomeException e) 
    	{
			LogSupport.major(context, this,
						"Skipping bulkServiceUpdate for subscriber "+ sub.getId()
								+ " because of Exception while retrieving Provision Command : "+e.getMessage());
			isBulkServiceUpdate = Boolean.FALSE;
		}
    	if (!isBulkServiceUpdate) 
 		{
 			LogSupport.info(context, this,
 							"Skipping BulkServiceUpdate for subscriber " + sub.getId()
 									+ " as bulkServiceUpdate provision command is not configured for the service provider "+ sub.getSpid()
 									+ " OR HLR is disabled OR SkipBulkServiceUpdateForSubscriberCreation is enabled.");
 		}
    	else
    	{
    		try
    		{
    			OSLMap =  SubscriberServicesSupport.getAllSubscribersServicesRecords(context, sub.getId());
    			if(LogSupport.isDebugEnabled(context))
    	    	{
    	    		LogSupport.debug(context, SubscriberServicesBulkUpdateSupport.class.getName(), "old Services List for subscriber [ "+sub.getId()+ "] is : "+OSLMap);
    	    	}
    			oldProvisionedServicesList = SubscriberServicesBulkUpdateSupport.preprocessing(context, OSLMap, sub);
    		}
    		catch (HomeException e) 
    		{
    			LogSupport
				.info(context,	SubscriberServicesBulkUpdateSupport.class.getName(),
						"Skipping "
								+ SubscriberServicesBulkUpdateSupport.class.getName()
								+ " as exception occured while looking up for bulkServiceUpdate provision command for service provider "
								+ sub.getSpid() + " : "+ e.getMessage()); 
    			isBulkServiceUpdate=Boolean.FALSE;
    		}
    	}
        
        RecurringRecharge recurringRecharge = new RecurringRecharge();
        recurringRecharge.setBillingDate(getBillingDate());
        recurringRecharge.setChargingCycle(getChargingCycle());
        recurringRecharge.setSubscriberId(sub.getId());
        recurringRecharge.setTotalCharge(0);

        final Date chargeStartTime = new Date(); 

        if (LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, this, "Processing subscriber " + sub.getId());
        }

        boolean isMonthlyCharge = ChargingCycleEnum.MONTHLY.equals(getChargingCycle());
        boolean isMultiDayCharge = ChargingCycleEnum.MULTIDAY.equals(getChargingCycle());
        
        try
        {
            int billCycleDay = SubscriberSupport.getBillCycleDay(context, sub);
            final double rate;
            if (isProRated())
            {
                if (context.has(RecurringRechargeSupport.PRORATED_RATE))
                {
                    rate = ((Double) context.get(RecurringRechargeSupport.PRORATED_RATE)).doubleValue();
                }
                else
                {
                    ChargingCycleHandler handler = ChargingCycleSupportHelper.get(context).getHandler(getChargingCycle());
                    rate = handler.calculateRate(context, getBillingDate(), 
                            billCycleDay, sub.getSpid()); 
                }
            }
            else
            {
                rate = 1.0;
            }
            
            Context subContext = context.createSubContext();
            subContext.put(RecurringRechargeSupport.NOTIFICATION_ONLY , isPreWarnNotificationOnly());
            if (doOneTimeOCGChargeForRecurringCharges(subContext,sub))
            {
                subContext.put(OcgForwardTransactionHome.IS_IGNORE_OCG_FW, true);
            }
            
            // PickNPay feature: All Or Nothing Charging
            boolean isAllOrNothingChargingFirstPass = Boolean.FALSE;
            boolean suspendAll = Boolean.FALSE;
            
            PricePlan pp = HomeSupportHelper.get(context).findBean(context, PricePlan.class, new EQ(PricePlanXInfo.ID, sub.getPricePlan()));
            
            if(pp.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY))
            {
            	isAllOrNothingChargingFirstPass = Boolean.TRUE;
            	
            	
            	applyRecurringServices(subContext, sub, rate, chargedItems, recurringRecharge,
                        chargingFailedItems, isAllOrNothingChargingFirstPass, suspendAll);
                if(isMonthlyCharge || isMultiDayCharge) 
                {
                	applyBundleAdjustment(subContext, sub, rate, chargedItems, recurringRecharge, chargingFailedItems, isAllOrNothingChargingFirstPass);
                }
                
                
            	if(LogSupport.isDebugEnabled(subContext))
            	{
            		LogSupport.debug(subContext, this, "Total recurring charge amount for PickNPay MRC group services of subscriber "+sub.getId()+" is : "+recurringRecharge.getTotalCharge());
            	}
            	
            	if(recurringRecharge.getTotalCharge() > 0)
            	{
            		if(sub.getBalanceRemaining(context) < recurringRecharge.getTotalCharge())
	            	{
            			isAllOrNothingChargingFirstPass = Boolean.FALSE;
            			suspendAll = Boolean.TRUE;
	            	} 
            		if(!suspendAll)
            		{
            			// reset total pickNPay for subscriber at this stage to start actual RC calculations, if balance is sufficient
            			// if not we are going to use this amount as primary service charge for subscriber expiry
            			recurringRecharge.setTotalCharge(0);
            		}
            	} 
            }

            isAllOrNothingChargingFirstPass = Boolean.FALSE;

            final boolean contractCreditPayment = applyContractCreditPayment(subContext, sub, recurringRecharge,
                    billCycleDay,rate);
            final boolean packageResult = isMonthlyCharge ? applyServicePackageAdjustment(subContext, sub, rate,
                    chargedItems, recurringRecharge, chargingFailedItems) : true;
            final boolean serviceResult = applyRecurringServices(subContext, sub, rate, chargedItems, recurringRecharge,
                    chargingFailedItems, isAllOrNothingChargingFirstPass, suspendAll);
            final boolean bundleResult = (isMonthlyCharge || isMultiDayCharge) ? applyBundleAdjustment(subContext, sub, rate, chargedItems,
                    recurringRecharge, chargingFailedItems, isAllOrNothingChargingFirstPass) : true;
            final boolean auxServiceResult = applyAuxServicesAdjustment(subContext, sub, rate, chargedItems,
                    recurringRecharge, chargingFailedItems);
            
            // Doing one time charge to OCG/CPS,rather multiple calls to OCG
            if (doOneTimeOCGChargeForRecurringCharges(subContext,sub) && recurringRecharge.getTotalCharge() != 0)
            {
                forwardToOcg(subContext, sub, recurringRecharge,chargeStartTime);
            }
            
            boolean insufficientBalanceNotification = context.getBoolean(RecurringRechargeSupport.INSUFFICIENT_BALANCE_NOTIFICATION);
            

            if(LogSupport.isDebugEnabled(subContext))
            {
            	LogSupport.debug(subContext, this, "CHARGED ITEMS FOR SUB:" + sub.getId() + " ... " + chargedItems.values());
            	LogSupport.debug(subContext, this, "CHARGING FAILED ITEMS FOR SUB:" + sub.getId() + " ... " + chargingFailedItems.values());
            }
            
            if (isPreWarnNotificationOnly() && hasChargedItems(chargedItems))
            {

                if(LogSupport.isDebugEnabled(context))
                {
                	LogSupport.debug(context, this, (insufficientBalanceNotification ? "Insufficient Balance Notification" : "Recurring Recharges Pre-Warn notification") 
                			+ " for Subscriber:[ID:" + sub.getId() + "]/[MSISDN:" + sub.getMsisdn() + "]/BALANCE[" + sub.getBalanceRemaining(context) + "]");
                }
            	
            	if( insufficientBalanceNotification && sub.getBalanceRemaining(context) < recurringRecharge.getTotalCharge() && !SubscriptionSupport.isTopUpScheduledForSubscription(context, sub) )
            	{            	
            	    if(LogSupport.isDebugEnabled(context))
                    {
                        LogSupport.debug(context, this, "Insufficient balance notification is being sent" + " for Subscriber:[ID:" + sub.getId() 
                                + "]/[MSISDN:" + sub.getMsisdn());
                    }
	                SubscriptionNotificationSupport.sendPreWarnInsufficientBalanceNotification(context, sub, chargedItems, getBillingDate(),
	                        getChargingCycle(), recurringRecharge);
            	}
            	else if( !insufficientBalanceNotification )
            	{
	                SubscriptionNotificationSupport.sendPreWarnNotification(context, sub, chargedItems, getBillingDate(),
	                        getChargingCycle(), recurringRecharge);
            	}
            	
            }
            else if(!isPreWarnNotificationOnly())
            {	
            	if ( hasChargedItems(chargedItems) ) // Recurring Charges applied and now it's time to notify the SUB.
            	{
            		SubscriptionNotificationSupport.sendRecurringRechargeNotification(context, sub, chargedItems, getBillingDate(),
            				getChargingCycle(), recurringRecharge);            	
            	} 
            
            	if( hasChargedItems(chargingFailedItems) )
            	{
            		SubscriptionNotificationSupport.sendRecurringRechargeFailureNotification(context, sub, chargingFailedItems, getBillingDate(),
            				getChargingCycle(), recurringRecharge);  
            	}
            }
            
            // 	Verizon integration feature
            if(isBulkServiceUpdate)
            {
            	SubscriberServicesBulkUpdateSupport.postProcessing(context, sub, OSLMap, oldProvisionedServicesList, newProvisionedServicesList);
            }
            
            accumulateForOne(packageResult && serviceResult && bundleResult && auxServiceResult
                    && contractCreditPayment);
        }       
        catch (final HomeException e)
        {
            LogSupport.major(context, this, "Couldn't calculate prorated rate and charge subscriber '" +  sub.getId() + "'");
            
        }
    }


    private void forwardToOcg(final Context ctx, final Subscriber sub, RecurringRecharge recurringCharge, Date startTime)
            throws OcgTransactionException
    {
        try
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Sub " + sub.getId() + " Forwarding Total Recurring charge to OCG for amount" + recurringCharge.getTotalCharge(), null).log(ctx);
            }
            CoreTransactionSupportHelper.get(ctx).forwardToOcg(ctx, sub.getMSISDN(), recurringCharge.getTotalCharge(),
                    sub.getSubscriptionType(ctx), sub.getAccount(ctx).getCurrency(), sub.getSubscriberType(), this,
                    (short) 0, true);
        }
        catch (final OcgTransactionException ocge)
        {
            handleOneTimeChargeFailure(ctx, sub, 0, ocge.getErrorCode(), recurringCharge.getTotalCharge());
            LogSupport.minor(ctx, this, "Error in charging the OCG for subscriber " + sub.getId(), ocge);
        }
        catch (final HomeException e)
        {
//            handleOneTimeChargeFailure(ctx, sub, RECHARGE_FAIL_XHOME, 0, recurringCharge.getTotalCharge());
            LogSupport.minor(ctx, this, "Error creating transaction, attempting to roll back OCG call", e);
        }
    }


    /**
     * Handle failure on One Time charge to OCG
     * @param ctx
     * @param sub
     * @param resultCode
     * @param ocgErrorCode
     */
    private void handleOneTimeChargeFailure(Context ctx, final Subscriber sub, final int resultCode,
            final int ocgErrorCode, final long amount)
    {
            RecurringRechargeSupport.handleRechargeFailure(ctx, getAgentName(), getBillingDate(), sub,
                    resultCode, ocgErrorCode, "One time OCG charging failed", -1, ChargedItemTypeEnum.SERVICE,amount);

    }


    private boolean doOneTimeOCGChargeForRecurringCharges(Context ctx, Subscriber sub)
    {
        return LicensingSupportHelper.get(ctx).isLicensed(ctx, LicenseConstants.RECURRING_ONETIME_OCG_CHARGE)
                && sub.isPostpaid();
    }

    
    /**
     * Applies credit monthly payments to subscribers contracts with prepayments
     * 
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to be charged.
     * 
     * @return Whether contract credit payment was successful
     */
    private boolean applyContractCreditPayment(final Context ctx, final Subscriber sub,
            final RecurringRecharge recurringRecharge, int billCycleDay, double rate)
    {
        long contractId = sub.getSubscriptionContract(ctx);
        boolean result = true;
        if (contractId >= 0)
        {
            And and = new And();
            and.add(new EQ(SubscriptionContractXInfo.SUBSCRIPTION_ID, sub.getId()));
            final AbstractRechargeItemVisitor visitor = new CreditContractPrepaymentVisitor(getBillingDate(),
                    getAgentName(), getChargingCycle(), sub, startDate_, endDate_, rate, isRecurringRecharge(),
                    isProRated(), billCycleDay);
            try
            {
                Collection<SubscriptionContract> contracts = HomeSupportHelper.get(ctx).getBeans(ctx,
                        SubscriptionContract.class, and);
                Visitors.forEach(ctx, contracts, visitor);
                recurringRecharge.setTotalCharge(recurringRecharge.getTotalCharge() + visitor.getChargeAmountSuccess());
            }
            catch (AgentException agentEx)
            {
                new MinorLogMsg(this, " Unable to apply monthly prepayment credit for subscriber " + sub.getId(),
                        agentEx).log(ctx);
                result = false;
            }
            catch (HomeException homeEx)
            {
                new MinorLogMsg(this, " Unable to apply monthly prepayment credit for subscriber " + sub.getId(),
                        homeEx).log(ctx);
                result = false;
            }
            finally
            {
                accumulate(visitor);
                allTransactionIds_.addAll(visitor.getAllSuccessfulTransactionIds());
            }
        }
        return result;
    }


    /**
     * Applies charges of service packages for subscriber.
     *
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to be charged.
     * @param chargingFailedItems TODO
     * @return Whether any of the service packages of the subscriber failed to charge.
     */
    private boolean applyServicePackageAdjustment(final Context context, final Subscriber sub,
            final double rate, final Map<Class, List<Object>> chargedItems, final RecurringRecharge recurringRecharge, Map<Class, List<Object>> chargingFailedItems)
    {
        final Context ctx = context;
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        
        final PMLogMsg pmLogMsg = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Charge Packages", details.toString());
        /*
         * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
         * Reason: TT#12051459060; Table; last row
         * 
         * For prepaid, we will suspend anyways on recurring charge calculations    .
         * 
         * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
         * will be done only when the "restrict provisioning" flag is true. And we have the requirement
         * in the TT to move it to suspended state.
         * 
         */
        final RechargeSubscriberServicePackageVisitor visitor = new RechargeSubscriberServicePackageVisitor(
            getBillingDate(), getAgentName(), getChargingCycle(), sub, true, startDate_, endDate_, rate, isRecurringRecharge(), isProRated(), isPreWarnNotificationOnly());

        try
        {
            final PricePlanVersion version = sub.getRawPricePlanVersion(ctx);
            if (version == null)
            {
                throw new HomeException("Cannot find price plan version for subscriber " + sub.getId());
            }
            final ServicePackageVersion servicePackageVersion = version.getServicePackageVersion();
            if (servicePackageVersion == null)
            {
                throw new HomeException("Cannot find service pacakge version for price plan " + version.getId()
                    + " version " + version.getVersion());
            }
            final Map packageFees = servicePackageVersion.getPackageFees();
            if (packageFees == null)
            {
                throw new HomeException("Cannot find package fees for service package " + servicePackageVersion.getId()
                    + " version " + servicePackageVersion.getVersion());
            }
            Visitors.forEach(ctx, packageFees, visitor);
            recurringRecharge.setTotalCharge(recurringRecharge.getTotalCharge() + visitor.getChargeAmountSuccess());
            
            //if (isPreWarnNotificationOnly())
            //{
                chargedItems.put(com.redknee.app.crm.bean.ServicePackageFee.class, visitor.getChargedItems());
            //}
                chargingFailedItems.put(com.redknee.app.crm.bean.ServicePackageFee.class, visitor.getChargingFailedItems());
        }
        catch (final Exception e)
        {
            // it is difficult
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }

            handleException(ctx, sub, "Fail to get subceriber price plan or service package" + e.getMessage());
        }
        finally
        {
            accumulate(visitor);
            allTransactionIds_.addAll(visitor.getAllSuccessfulTransactionIds());
            pmLogMsg.log(context);
        }

        return visitor.getPackagesFailedCount() > 0;
    }


    /**
     * Applies charges of service for subscriber.
     *
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to charge.
     * @param chargingFailedItems TODO
     * @return Whether any of the service failed to charge.
     * @throws HomeException 
     */
    private boolean applyRecurringServices(final Context context, final Subscriber sub, double rate, final Map<Class, List<Object>> chargedItems, final RecurringRecharge recurringRecharge, 
    		Map<Class, List<Object>> chargingFailedItems, boolean isAllOrNothingFirstPass, boolean suspendAll) throws HomeException
    {
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        final PMLogMsg pmLogMsg = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Charge Services", details.toString());

        /*
         * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
         * Reason: TT#12051459060; Table; last row
         * 
         * For prepaid, we will suspend anyways on recurring charge calculations    .
         * 
         * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
         * will be done only when the "restrict provisioning" flag is true. And we have the requirement
         * in the TT to move it to suspended state.
         * 
         */
        
        final AbstractRechargeItemVisitor visitor = new RechargeSubscriberServiceVisitor(context, getBillingDate(),
            getAgentName(), getChargingCycle(), sub, true, startDate_, endDate_, rate, isRecurringRecharge(), isProRated(), 
            isPreWarnNotificationOnly(), isAllOrNothingFirstPass, suspendAll);

        try
        {
            final PricePlanVersion pricePlan = sub.getPricePlan(context);
            if (pricePlan == null)
            {
                throw new HomeException("Cannot find price plan version for subscriber " + sub.getId());
            }
            final Map<ServiceFee2ID, ServiceFee2> serviceFees = pricePlan.getServiceFees(context);
            if (serviceFees == null)
            {
                throw new HomeException("Cannot retrieve service fees for price plan " + pricePlan.getId()
                    + " version " + pricePlan.getVersion());
            }
            
            Date nextRecurringChargeDate = null;
        	if(ChargingCycleEnum.MULTIDAY.equals(getChargingCycle()))
        	{
        		nextRecurringChargeDate = getBillingDate();
        		
        	}
            Collection<ServiceFee2> chargeableFees = sub.getChargeableFees(context, serviceFees,nextRecurringChargeDate);
            
            final Collection<ServiceFee2> sortedServiceFees = PricePlanSupport.getServiceByExecutionOrder(context, chargeableFees);
            
            if(isPreWarnNotificationOnly())
            {
	            /**
	             * Filer PP services for notification. Notification should be sent only if billingDate exact equals nextRecurringChargeDate.
	             * condition1: serviceNextRecurringChargeDate != billingDate/nextRecurringChargeDate.
	             * condition2: notificationOnly
	             */
	            
	            Iterator<ServiceFee2> feeIterator = sortedServiceFees.iterator();
	            
	            Map<ServiceFee2ID , SubscriberServices> serviceMap = SubscriberServicesSupport.getAllSubscribersServices(context, sub.getId(), true, nextRecurringChargeDate);
	            
	            
	            while(feeIterator.hasNext())
	            {
	            	ServiceFee2 fee = feeIterator.next();
	            	if(!serviceMap.containsKey(fee.getServiceId()))
	            	{
	            		feeIterator.remove();
	            	}
	            }
            }
            /**
             *  Added for PickNPay feature
             */
            else if(isAllOrNothingFirstPass)
            {
            	 Iterator<ServiceFee2> feeIterator = sortedServiceFees.iterator();
            	 
            	//Only MRC group services to be considered for all or nothing charging balance check
            	while(feeIterator.hasNext())
 	            {
 	            	ServiceFee2 fee = feeIterator.next();
 	            	if(!fee.isApplyWithinMrcGroup())
 	            	{
 	            		feeIterator.remove();
 	            	}
 	            }
            }
            else if(suspendAll)
            {
            	Iterator<ServiceFee2> feeIterator = sortedServiceFees.iterator();
           	 
            	// If balance is not sufficient to charge for total pick and pay amount, charge for primary service using this amount
            	// OCG will fail this transaction due to insufficient balance, primary service will be suspended and this will trigger subscriber expiry in transaction pipeline
            	while(feeIterator.hasNext())
 	            {
 	            	ServiceFee2 fee = feeIterator.next();
 	            	if(fee.isPrimary())
 	            	{
 	            		fee.setFee(recurringRecharge.getTotalCharge()); break;
 	            	}
 	            }
            }
            
            // Hack fix for FlyC TT to work around the restriction that MRC will currently consider only services matching the NRC of primary service
        	// For suspension, consider all the services that are part PickNPay MRCGroup of the priceplan
        	// This will suspend core services, such as voice, data, sms which do not match recurrence or interval with primary service currently at FLYC
        	
            if(suspendAll) // Scenario specific to PickNPay
            {
            	Collection<ServiceFee2> chargeablePnPPlanFees = sub.getChargeableFees(context, serviceFees);
            	
            	Iterator<ServiceFee2> feeItr = chargeablePnPPlanFees.iterator();
            	while(feeItr.hasNext())
            	{
            		ServiceFee2 fee = feeItr.next();
            		if(!fee.isApplyWithinMrcGroup())
            		{
            			feeItr.remove();
            		}
            	}
            	
            	final Collection<ServiceFee2> sortedMrcGroupServiceFees = PricePlanSupport.getServiceByExecutionOrder(context, chargeablePnPPlanFees);
            	
            	Visitors.forEach(context, sortedMrcGroupServiceFees, visitor);
            }
            else // legacy logic
            {
            	Visitors.forEach(context, sortedServiceFees, visitor);
            }
            
            recurringRecharge.setTotalCharge(recurringRecharge.getTotalCharge() + visitor.getChargeAmountSuccess());
            
            chargedItems.put(com.redknee.app.crm.bean.core.ServiceFee2.class, visitor.getChargedItems());
            chargingFailedItems.put(com.redknee.app.crm.bean.core.ServiceFee2.class, visitor.getChargingFailedItems());
        }
        catch (final Exception e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(context);
            handleException(context, sub, "Fail to get subscriber chargable services " + e.getMessage());
        }
        finally
        {
        	if(!isAllOrNothingFirstPass)
        	{   // no need to accumulate services count or charges during first pass done for balance check
        		accumulate(visitor);
        		allTransactionIds_.addAll(visitor.getAllSuccessfulTransactionIds());
        	}
            pmLogMsg.log(context);
        }

        return visitor.getServicesCountFailed() > 0;
    }


    /**
     * Applies charges of bundles for subscriber.
     *
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to charge.
     * @param chargingFailedItems TODO
     * @return Whether any of the bundles failed to charge.
     */
    private boolean applyBundleAdjustment(final Context context, final Subscriber sub, final double rate, final Map<Class, List<Object>> chargedItems, 
    		final RecurringRecharge recurringRecharge, Map<Class, List<Object>> chargingFailedItems, boolean isAllOrNothingFirstPass )
    {
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(sub.getId());
        details.append("'");
        final PMLogMsg pmLogMsg = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Charge Bundles", details.toString());
        final Map<Long, BundleFee> bundles;
        /*
         * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
         * Reason: TT#12051459060; Table; last row
         * 
         * For prepaid, we will suspend anyways on recurring charge calculations    .
         * 
         * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
         * will be done only when the "restrict provisioning" flag is true. And we have the requirement
         * in the TT to move it to suspended state.
         * 
         */
        final RechargeSubscriberBundleVisitor visitor = new RechargeSubscriberBundleVisitor(getBillingDate(),
            getAgentName(), getChargingCycle(), sub, true, startDate_, endDate_, rate, isRecurringRecharge(), 
            isProRated(), isPreWarnNotificationOnly(), isAllOrNothingFirstPass);

        try
        {
        	Date nextRecurringChargeDate = null;
         	if(ChargingCycleEnum.MULTIDAY.equals(getChargingCycle()))
         	{
         		nextRecurringChargeDate = getBillingDate();
         		
         	}
         	
            bundles = SubscriberBundleSupport.getSubscribedBundles(context, sub,nextRecurringChargeDate);
            List<BundleFee> sortedBundleFee= new ArrayList(bundles.values());
            Collections.sort(sortedBundleFee, new BundleFeeExecutionOrderComparator(context, true));
            
            if(isAllOrNothingFirstPass)
            {
            	 Iterator<BundleFee> feeIterator = sortedBundleFee.iterator();
            	 
            	 //Only MRC group services to be considered for all or nothing charging balance check
            	while(feeIterator.hasNext())
 	            {
            		BundleFee fee = feeIterator.next();
 	            	if(!fee.isApplyWithinMrcGroup())
 	            	{
 	            		feeIterator.remove();
 	            	}
 	            }
            }
            
            Visitors.forEach(context, sortedBundleFee, visitor);
            
            recurringRecharge.setTotalCharge(recurringRecharge.getTotalCharge() + visitor.getChargeAmountSuccess());

            chargedItems.put(com.redknee.app.crm.bean.core.BundleFee.class, visitor.getChargedItems());
            chargingFailedItems.put(com.redknee.app.crm.bean.core.BundleFee.class, visitor.getChargingFailedItems());
        }
        catch (final Exception e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(context);
            handleException(context, sub, "Fail to get subceriber chargable bundles " + e.getMessage());
        }
        finally
        { 
        	if(!isAllOrNothingFirstPass)
        	{ 
        		accumulate(visitor);
        		allTransactionIds_.addAll(visitor.getAllSuccessfulTransactionIds());
        		pmLogMsg.log(context); 
        	}
        }

        return visitor.getBundleCountFailed() > 0;
    }


    /**
     * Applies charge of auxiliary services for subscriber.
     *
     * @param context
     *            The operating context.
     * @param sub
     *            Subscriber to charge.
     * @param chargingFailedItems TODO
     * @return Whether any of the auxiliary services failed to charge.
     */
    protected boolean applyAuxServicesAdjustment(final Context context, final Subscriber sub,
            final double rate, final Map<Class, List<Object>> chargedItems, final RecurringRecharge recurringRecharge, Map<Class, List<Object>> chargingFailedItems)
    {
        StringBuilder details = new StringBuilder();
        details.append("SubscriberId='");
        details.append(sub.getId());
        details.append("'");

        final PMLogMsg pmLogMsg = new PMLogMsg(RecurringRechargeSupport.getRecurringRechargePMModule(isPreWarnNotificationOnly()), "Charge Auxiliary Services", details.toString());
        /*
         * Removed check from the SPID level flag: "Suspend Prepaid service on failed Recharge"
         * Reason: TT#12051459060; Table; last row
         * 
         * For prepaid, we will suspend anyways on recurring charge calculations    .
         * 
         * For postpaid, the failed charge will only happen if "charge-override = N" is sent to OCG, which
         * will be done only when the "restrict provisioning" flag is true. And we have the requirement
         * in the TT to move it to suspended state.
         * 
         */
        final RechargeSubscriberAuxServiceVisitor visitor = new RechargeSubscriberAuxServiceVisitor(getBillingDate(),
            getAgentName(), getChargingCycle(), sub, true, startDate_, endDate_, rate, isRecurringRecharge(), isProRated(), isPreWarnNotificationOnly());
        
        try
        {  	
        	visitor.setEnforceRecharging(SpidSupport.isEnforceAuxServiceRecharge(context, sub.getSpid()));
        	Collection<SubscriberAuxiliaryService> activeAssociations ;
        	Date nextRecurringChargeDate = null;
        	if(ChargingCycleEnum.MULTIDAY.equals(getChargingCycle()))
        	{
        		nextRecurringChargeDate = getBillingDate();
        		
        	}
            
        	activeAssociations = SubscriberAuxiliaryServiceSupport
                    .getActiveSubscriberAuxiliaryServices(context, sub, getBillingDate(), nextRecurringChargeDate);
        	/*
        	 * This is an overhead , just in case incoming collection is also not a type of list.
        	 */
        	if(!(activeAssociations instanceof List))
        		activeAssociations = new ArrayList<SubscriberAuxiliaryService>(activeAssociations);
        	
    	 	Collections.sort((List<SubscriberAuxiliaryService>)activeAssociations,new SubscriberAuxiliaryServiceRechargeOrder(context));
        		
            Visitors.forEach(context, activeAssociations, visitor);

            recurringRecharge.setTotalCharge(recurringRecharge.getTotalCharge() + visitor.getChargeAmountSuccess());

            chargedItems.put(com.redknee.app.crm.bean.AuxiliaryService.class, visitor.getChargedItems());
            chargingFailedItems.put(com.redknee.app.crm.bean.AuxiliaryService.class, visitor.getChargingFailedItems());
        }
        catch (final Exception e)
        {
            new MajorLogMsg(this, e.getMessage(), e).log(context);
            handleException(context, sub, "Fail to get subscriber chargable auxiliary services " + e.getMessage());
        }
        finally
        {
            accumulate(visitor);
            allTransactionIds_.addAll(visitor.getAllSuccessfulTransactionIds());
            pmLogMsg.log(context);
        }

        return visitor.getServicesCountFailed() > 0;
    }


    /**
     * Create recharge error report.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to charge.
     * @param reason
     *            Failure reason.
     */
    private void handleException(final Context ctx, final Subscriber sub, final String reason)
    {

        try
        {
            RechargeErrorReportSupport.createReport(ctx, getAgentName(), null, RECHARGE_RESULT_DATA_SUBSCRIBER_LEVEL,
                OCG_RESULT_UNKNOWN, reason, SUBSCRIBER_LEVEL_ERROR_DUMMY_CHARGED_ITEM_ID, sub.getBAN(), 
                sub.getAccount(ctx).getResponsibleBAN(), sub.getSpid(),
                sub.getMSISDN(), sub.getId(), this.getBillingDate(), ChargedItemTypeEnum.UNKNOWN);
        }
        catch (final HomeException e)
        {
            LogSupport.minor(ctx, this, "fail to create error report for transaction ", e);
        }
    }


    /**
     * Accumulate counters.
     *
     * @param success
     *            Whether the charge was successful.
     */
    protected synchronized void accumulateForOne(final boolean success)
    {
        if (success)
        {
            this.incrementSubscriberSuccessCount();
        }
        else
        {
            this.incrementSubscriberFailCount();
        }
        this.incrementSubscriberCount();
    }
    
    public boolean hasChargedItems(final Map<Class, List<Object>> chargedItems)
    {
        boolean result = false;
        for(Class c : chargedItems.keySet())
        {
            List classItems = chargedItems.get(c);
            if (classItems != null && !classItems.isEmpty())
            {
                result = true;
                break;
            }
        }
        return result;
    }
    
    private final Date startDate_;
    
    private final Date endDate_;
    
    private final Set<Long> allTransactionIds_;
    
    public final static String RECURRING_CHARGES_GROUPED_INTO_ONE_CHARGE = "Recurring_Charges_Grouped_Into_One";
    

}
