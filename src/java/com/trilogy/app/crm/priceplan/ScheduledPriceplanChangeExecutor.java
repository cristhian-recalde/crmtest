/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.priceplan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trilogy.app.crm.agent.CronConstant;
import com.trilogy.app.crm.api.rmi.PricePlanToApiAdapter;
import com.trilogy.app.crm.api.rmi.impl.SubscribersImpl;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanSubTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.SubModificationSchedule;
import com.trilogy.app.crm.bean.SubModificationScheduleHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.IntegerHolder;
import com.trilogy.framework.xhome.holder.StringHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;
import com.trilogy.util.crmapi.wsdl.v2_1.types.serviceandbundle.PricePlanOptionTypeEnum;
import com.trilogy.util.crmapi.wsdl.v2_2.types.serviceandbundle.PricePlanOptionUpdateResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlanOption;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionPricePlan;
import com.trilogy.util.selfcareapi.soap.common.xsd._2010._09.SelfcareRequestHeader;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.GenericParameter;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.OptionReference;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.PricePlanFilterQueryResult;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.SubscriptionReference;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd._2010._09.ValidateAndUpdateQueryResult;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.FilterPricePlans;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.FilterPricePlansE;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.FilterPricePlansResponseE;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.ValidateAndUpdateOptions;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.ValidateAndUpdateOptionsE;
import com.trilogy.util.selfcareapi.soap.priceplan.xsd.priceplan_v2_0.ValidateAndUpdateOptionsResponseE;
import com.trilogy.util.selfcareapi.wsdl.v2_0.api.PricePlanService;


/**
 * @author amoll
 * @since 9.9
 */

public class ScheduledPriceplanChangeExecutor extends ContextAwareSupport implements ScheduleTaskExecutor 
{

    private static final long serialVersionUID = 1L;

    /**
     * @param ctx
     * @param obj
     * @throws AgentException
     * @throws AbortVisitException
     * @see com.redknee.framework.xhome.visitor.Visitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public void execute(Context ctx, SubModificationSchedule subModificationSchedule) throws AgentException, AbortVisitException
    {
    	Home home = (Home)ctx.get(SubModificationScheduleHome.class);
    	String agentId = "";
    	String dealerCode = "";
    	int resultCode = CronConstant.SCHEDULED_PP_CHANGE_SUCCESS;
    	String resultDescription = "";
    	String notificationMessageDescription = "";
    	
    	Subscriber subscriber = null;
    	Account account = null;
    	long oldATUAmount = -1;
    	
    	try {
    		subModificationSchedule.setStartDate(new java.util.Date(System.currentTimeMillis()));
    		List snapshotList = subModificationSchedule.getSnapshot();
    		List supportingList = subModificationSchedule.getSupportingInformation();
    		String[] requestSnapshotArr = null;
    		String[] requestSupportingInfoArr = null;
    		PricePlanService pricePlanService = (PricePlanService)ctx.get(CronConstant.IVP_PRICEPLAN_SERVICE);
	    	
    		subscriber = SubscriberSupport.lookupSubscriberForSubId(ctx,subModificationSchedule.getSubscriptionId());
    		account = subscriber.getAccount(ctx);
    		
    		SelfcareRequestHeader selfcareRequestHeader = new SelfcareRequestHeader();
	    	
	    	selfcareRequestHeader.setUsername("rkadm");//CHECK WITH DCRM
	    	selfcareRequestHeader.setTransactionID(subscriber.getId());//CHECK WITH DCRM
	    	selfcareRequestHeader.setPassword("rkadm");//CHECK WITH DCRM
    		
	    	
    		if(snapshotList == null || snapshotList.size() != 1 || snapshotList.size() ==0){
    			resultDescription = "Scheduled priceplan data improper for subscriber: [" + subModificationSchedule.getSubscriptionId()
    					+ "] Proceeding to next entry.";
    			if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
    				LogSupport.debug(ctx,this, resultDescription);
    			resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    			SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    			notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG; 
    			return;
    		}
    		else
    			requestSnapshotArr = ((StringHolder)snapshotList.get(0)).getString().split(",");
    		
    		if(supportingList != null && supportingList.size() > 0)
    			requestSupportingInfoArr = ((StringHolder)supportingList.get(0)).getString().split(",");
    		
    		
    		Map<String, String> requestSnapshotMap = new HashMap<String, String>(requestSnapshotArr.length);
    		for(String requestSnapshot: requestSnapshotArr){
    			if (requestSnapshot.split("=").length > 1)
    	    	{
    				requestSnapshotMap.put(requestSnapshot.split("=")[0], requestSnapshot.split("=")[1]);
    	    	}
    		}
    		
    		Map<String, String> requestSupportSnapshotMap = null;
    		if(requestSupportingInfoArr != null){
    			requestSupportSnapshotMap = new HashMap<String, String>(requestSupportingInfoArr.length);
    			for(String requestSupport: requestSupportingInfoArr){
    				if (requestSupport.split("=").length > 1)
        	    	{
    					requestSupportSnapshotMap.put(requestSupport.split("=")[0], requestSupport.split("=")[1]);
        	    	}
        		}
    		}
    		
    		if(requestSupportSnapshotMap != null){
    			if(requestSupportSnapshotMap.get(CronConstant.OLD_ATU_AMOUNT) != null)
    				oldATUAmount = Long.parseLong(requestSupportSnapshotMap.get(CronConstant.OLD_ATU_AMOUNT));
    			if(!validateCurrentPriceplan(requestSupportSnapshotMap, subscriber)){
    				resultDescription = "Scheduled priceplan change fail due to current priceplan/version does not match with " +
    	    				" scheduled priceplan/version for subscriber: [" + subModificationSchedule.getSubscriptionId()
        					+ "] Proceeding to next entry.";
    				if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
    					LogSupport.debug(ctx,this, resultDescription);
    	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    	    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    	    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_PP_MISMATCH_ERR_MSG; 
    	    		return;
    			}
    		}
    		
    		long toProvisionPriceplan =  Long.parseLong(requestSnapshotMap.get(CronConstant.NEW_PRICEPLAN_ID));
    		
    		if(!SubscriberSupport.validateSusbcriberStateActive(subscriber)){
				resultDescription = "Scheduled priceplan change fail due to state not active " +
	    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
    					+ "] Proceeding to next entry.";
				if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
					LogSupport.debug(ctx,this, resultDescription);
	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
	    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
	    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_SUB_NOT_ACTIVE_ERR_MSG; 
	    		return;
			}
    		
    		if(!validateScheduledPriceplanState(ctx, toProvisionPriceplan)){
				resultDescription = "Scheduled priceplan change fail due to scheduled priceplan state not active " +
	    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
    					+ "] Proceeding to next entry.";
				if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
					LogSupport.debug(ctx,this, resultDescription);
	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
	    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
	    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_ACTIVE_ERR_MSG; 
	    		return;
			}
    		
    		StringHolder notificationMessageDescriptionHolder = new StringHolder();
    		IntegerHolder resultCodeHolder = new IntegerHolder();
	    	if(!filterPriceplan(ctx, subModificationSchedule, account, subscriber, requestSnapshotMap, selfcareRequestHeader,
	    			notificationMessageDescriptionHolder, resultCodeHolder)){
	    		notificationMessageDescription = notificationMessageDescriptionHolder.getValue();
	    		resultCode = resultCodeHolder.getValue();
	    		return;
	    	}
	    	
	    	PricePlan pricePlan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan); 
	    	boolean isPickNPayPricePlan = pricePlan.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY) ? true:false;
	    	
	    	if(isPickNPayPricePlan && isPricePlanSwitching(ctx, subscriber, toProvisionPriceplan))
	    	{
	    		//Call priceplan change API
	    		SubscriptionPricePlan subPlanOption;
	            subPlanOption = new SubscriptionPricePlan();
	            subPlanOption.setIsSelected(true);
	            
	            int currentVerionOfToProvisionPriceplan = -1;
	            PricePlan plan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan);  
	            PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(ctx, toProvisionPriceplan);
            	if(currentVersion == null)
            	{
            		currentVerionOfToProvisionPriceplan = -1;
            	}
            	else
            	{
            		currentVerionOfToProvisionPriceplan = currentVersion.getVersion();
            	}
	            PricePlanVersion version = PricePlanSupport.getVersion(ctx, plan.getId(), currentVerionOfToProvisionPriceplan);
	            List<com.redknee.app.crm.bean.PricePlanVersion> ppvList = new ArrayList<com.redknee.app.crm.bean.PricePlanVersion>();
	            ppvList.add(version);
	            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = PricePlanToApiAdapter
	                    .adaptPricePlanToApi(ctx, plan, ppvList);
	            
	            subPlanOption.setPricePlanDetails(apiPricePlan);
	            
	            subPlanOption.setIsSelected(true);
	            SubscribersImpl subscriberImpl = new SubscribersImpl(ctx);
	            com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, subscriber.getSpid());
	            PricePlanOptionUpdateResult[]  resultArr = 
	            		subscriberImpl.updateSubscriptionRating(ctx, subscriber, subPlanOption, true, "updateSubscriptionPricePlanOptions");
	            
	    	}
	    	
	    	
	    	//validateOptions
	    	int subscriptionType = (int)subscriber.getSubscriptionType();
	    	
	    	SubscriptionReference subscriptionReference = new SubscriptionReference();
	    	subscriptionReference.setAccountID(account.getBAN());
	    	subscriptionReference.setMobileNumber(subscriber.getMsisdn());
	    	subscriptionReference.setSpid(subscriber.getSpid());
	    	subscriptionReference.setSubscriptionIdentifierType(true);//Implies subscription identifier being sent
	    	subscriptionReference.setIdentifier(subscriber.getId());
	    	subscriptionReference.setSubscriptionType(subscriptionType);
	    	
	    	Set<Long> serviceMap = subscriber.getProvisionedServices(ctx);
	    	Set<Long> auxServiceMap = subscriber.getAuxiliaryServiceIds(ctx);
	    	
	    	//String packageId = subscriber.getPackageId();//package
	    	
	    	
	    	//bundles and aux bundles
	    	Map bundlesMap = SubscriberBundleSupport.getSubscribedBundlesWithPointsBundles(ctx, subscriber);

	    	Iterator<Long> serviceIterator = serviceMap.iterator();
	    	Iterator<Long> auxServiceIterator = auxServiceMap.iterator();
	    	Iterator bundleIterator = bundlesMap.values().iterator();
	    	
	    	int optionReferenceSize = bundlesMap.size() + serviceMap.size() + auxServiceMap.size();
	    	//if(packageId != null && packageId.length() > 0)
	    	//	optionReferenceSize += 1;
	    	
	    	int currentIndex = 0;
	    	int requestedOptionsSize = 0;
	    	OptionReference[] optionRefernceArr = new OptionReference[optionReferenceSize];
	    	
	    	/*
	    	 * this code is deprecated, service package is not in 9.x, actually below code is not proper, 
	    	 * it required service package not SIM package 
	    	 */
//	    	if(packageId != null && packageId.length() > 0){
//	    		optionRefernceArr[currentIndex] = getOptionReference(Long.parseLong(packageId), CronConstant.PRICEPLAN_OPTIONTYPE_PACKAGE, 
//	    																							CronConstant.INFORMATION_QUERY);
//	    		currentIndex++;
//	    	}
	    	while(serviceIterator.hasNext()){
	    		optionRefernceArr[currentIndex] = getOptionReference(serviceIterator.next(), CronConstant.PRICEPLAN_OPTIONTYPE_SERVICE, 
						CronConstant.INFORMATION_QUERY);
	    		currentIndex++;
	    	}
	    	
	    	while(auxServiceIterator.hasNext()){
	    		optionRefernceArr[currentIndex] = getOptionReference(auxServiceIterator.next(),CronConstant.PRICEPLAN_OPTIONTYPE_AUX_SERVICE, 
						CronConstant.INFORMATION_QUERY);
	    		currentIndex++;
	    		requestedOptionsSize++;
	    	}
	    	
	    	while(bundleIterator.hasNext()){
	    		BundleFee fee = (BundleFee)bundleIterator.next();
	    		optionRefernceArr[currentIndex] = getOptionReference(fee.getId(),
	    												fee.isAuxiliarySource() ? CronConstant.PRICEPLAN_OPTIONTYPE_AUX_BUNDLE : 
	    													CronConstant.PRICEPLAN_OPTIONTYPE_BUNDLE, CronConstant.INFORMATION_QUERY);
	    		currentIndex++;
	    		if (fee.isAuxiliarySource())
	    		{
	    			requestedOptionsSize++;
	    		}
	    	}
	    	
	    	int currentRequestedOptionIndex = 0;
	    	OptionReference[] requestedServiceOptionRefernceArr = null;//new OptionReference[optionReferenceSize];
	    	//Requested options START
	    	List<String> requestedServicesList = null;
	    	List<String> requestedBundlesList = null;
	    	int requestedServicesAndBundlesSize = 0; 
	    	
	    	if(requestSnapshotMap.get(CronConstant.SERVICES) != null && !requestSnapshotMap.get(CronConstant.SERVICES).equals("")){
	    		requestedServicesList = Arrays.asList(requestSnapshotMap.get(CronConstant.SERVICES).split("\\|"));
	    		requestedOptionsSize += requestedServicesList.size();
	    		requestedServicesAndBundlesSize = requestedServicesList.size();
	    	}
	    	if(requestSnapshotMap.get(CronConstant.BUNDLES) != null && !requestSnapshotMap.get(CronConstant.BUNDLES).equals("")){
	    		requestedBundlesList = Arrays.asList(requestSnapshotMap.get(CronConstant.BUNDLES).split("\\|"));
	    		requestedOptionsSize += requestedBundlesList.size();
	    		requestedServicesAndBundlesSize += requestedBundlesList.size();
	    	}
	    	
	    	// In case of request involving options/ membership change
	    	List<String> removeServicesList = null;
	    	List<String> removeBundlesList = null;
	    	int removedServicesAndBundlesSize = 0; 
	    	
	    	if(requestSnapshotMap.get(CronConstant.REMOVE_SERVICES) != null && !requestSnapshotMap.get(CronConstant.REMOVE_SERVICES).equals("")){
	    		removeServicesList = Arrays.asList(requestSnapshotMap.get(CronConstant.REMOVE_SERVICES).split("\\|"));
	    		requestedOptionsSize += removeServicesList.size();
	    		removedServicesAndBundlesSize = removeServicesList.size();
	    	}
	    	if(requestSnapshotMap.get(CronConstant.REMOVE_BUNDLES) != null && !requestSnapshotMap.get(CronConstant.REMOVE_BUNDLES).equals("")){
	    		removeBundlesList = Arrays.asList(requestSnapshotMap.get(CronConstant.REMOVE_BUNDLES).split("\\|"));
	    		requestedOptionsSize += removeBundlesList.size();
	    		removedServicesAndBundlesSize += removeBundlesList.size();
	    	}
    		
	    	requestedServiceOptionRefernceArr = new OptionReference[requestedOptionsSize];
	    	
	    	if(requestedServicesList != null){
	    		Iterator<String> requestedServiceIterator = requestedServicesList.iterator();
		    	while(requestedServiceIterator.hasNext()){
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = 
		    				getOptionReference(Long.parseLong(requestedServiceIterator.next()),
		    				CronConstant.PRICEPLAN_OPTIONTYPE_SERVICE, 
							CronConstant.INFORMATION_QUERY);
		    		currentRequestedOptionIndex++;
		    	}
	    	}
	    	
	    	if(requestedBundlesList != null){
	    		Iterator<String> requestedBundlesIterator = requestedBundlesList.iterator();
		    	while(requestedBundlesIterator.hasNext()){
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = 
		    				getOptionReference(Long.parseLong(requestedBundlesIterator.next()),
		    				CronConstant.PRICEPLAN_OPTIONTYPE_BUNDLE, 
							CronConstant.INFORMATION_QUERY);
		    		currentRequestedOptionIndex++;
		    	}
	    	}
	    	
	    	Iterator<Long> auxServiceItr = auxServiceMap.iterator();
	    	Iterator bundleItr = bundlesMap.values().iterator();
	    	
	    	while(auxServiceItr.hasNext()){
	    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = getOptionReference(auxServiceItr.next(),
	    				CronConstant.PRICEPLAN_OPTIONTYPE_AUX_SERVICE, 
						CronConstant.INFORMATION_QUERY);
	    		currentRequestedOptionIndex++;
	    	}
	    	
	    	while(bundleItr.hasNext()){
	    		BundleFee fee = (BundleFee)bundleItr.next();
	    		if (fee.isAuxiliarySource())
	    		{
	    			requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = getOptionReference(fee.getId(),
							CronConstant.PRICEPLAN_OPTIONTYPE_AUX_BUNDLE,
							CronConstant.INFORMATION_QUERY);
	    			currentRequestedOptionIndex++;
	    		}
	    	}
	    	
	    	// PickNPay feature: adding service and bundle list of unselected in case of options change within same price plan only 
	    	// In case of switch price plan: removed service and bundle list are empty
	    	
	    	if(removeServicesList != null){
	    		Iterator<String> removeServiceIterator = removeServicesList.iterator();
		    	while(removeServiceIterator.hasNext()){
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = 
		    				getOptionReference(Long.parseLong(removeServiceIterator.next()),
		    				CronConstant.PRICEPLAN_OPTIONTYPE_SERVICE, 
							CronConstant.INFORMATION_QUERY);
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex].setUpdateType(CronConstant.UPDATE_REMOVE_QUERY);
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex].setIsSelected(false);
		    		currentRequestedOptionIndex++;
		    	}
	    	}
	    	
	    	if(removeBundlesList != null){
	    		Iterator<String> removeBundlesIterator = removeBundlesList.iterator();
		    	while(removeBundlesIterator.hasNext()){
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex] = 
		    				getOptionReference(Long.parseLong(removeBundlesIterator.next()),
		    				CronConstant.PRICEPLAN_OPTIONTYPE_BUNDLE, 
							CronConstant.INFORMATION_QUERY);
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex].setIsSelected(false);
		    		requestedServiceOptionRefernceArr[currentRequestedOptionIndex].setUpdateType(CronConstant.UPDATE_REMOVE_QUERY);
		    		currentRequestedOptionIndex++;
		    	}
	    	}
	    	
	    	if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
	    	{
	    		for (int count =0 ; count < requestedOptionsSize; count ++)
	    		{
	    			LogSupport.debug(ctx,this, "Requested Array " + requestedServiceOptionRefernceArr[count].getIdentifier() + "is selected :" + 
	    					requestedServiceOptionRefernceArr[count].getIsSelected());
	    		}
	    	}
	    	
	    	// to differentiate between Future dated call or immediate call
	    	ctx.put(Lookup.FUTURE_DATED_PRICEPLAN_CHANGE, true);
	    	
	    	//Requested options END
	    	if(requestSnapshotMap.get(CronConstant.AGENT_ID) != null && !requestSnapshotMap.get(CronConstant.AGENT_ID).equals("")){
	    		agentId = requestSnapshotMap.get(CronConstant.AGENT_ID);
	    	}
	    	if(requestSnapshotMap.get(CronConstant.DEALER_CODE) != null && !requestSnapshotMap.get(CronConstant.DEALER_CODE).equals("")){
	    		dealerCode = requestSnapshotMap.get(CronConstant.DEALER_CODE);
	    	}
	    	ValidateAndUpdateOptionsE validateAndUpdateOptionsE =  new ValidateAndUpdateOptionsE();
	    	ValidateAndUpdateOptions validateAndUpdateOptions = new ValidateAndUpdateOptions();
	    	
	    	validateAndUpdateOptions.setHeader(selfcareRequestHeader);
	    	validateAndUpdateOptions.setChannel(subModificationSchedule.getChannel());
	    	validateAndUpdateOptions.setAgentID(agentId);	
	    	validateAndUpdateOptions.setDealerCode(dealerCode);
	    	validateAndUpdateOptions.setLanguage(account.getLanguage());    	
	    	validateAndUpdateOptions.setSubscriptionReference(subscriptionReference);
	    	validateAndUpdateOptions.setPricePlanId(toProvisionPriceplan);
	    	validateAndUpdateOptions.setCurrentOptions(optionRefernceArr);
	    	validateAndUpdateOptions.setRequestedUpdatedOptions(requestedServiceOptionRefernceArr);
	    	
	    	// for Pick n pay bypass balance check
	    	if(isPickNPayPricePlan)
	    	{
	    		validateAndUpdateOptions.setBypassBalanceCheck(true);
		    	validateAndUpdateOptions.setOperationType(true);	
		    	validateAndUpdateOptions.setAgentID(""); // setting it to empty string to check whether its a call from Task
	    	}
	    	else
	    	{
	    		validateAndUpdateOptions.setBypassBalanceCheck(false);
	    		validateAndUpdateOptions.setOperationType(false);	
	    	}
	    	
	    	validateAndUpdateOptionsE.setValidateAndUpdateOptions(validateAndUpdateOptions);
	    	
	    	
	    	ValidateAndUpdateOptionsResponseE validateAndUpdateOptionsResponseE = 
	    								pricePlanService.validateAndUpdateOptions(validateAndUpdateOptionsE);
	    	if(validateAndUpdateOptionsResponseE == null){
	    		resultDescription = "Scheduled priceplan change fail due to null response for validateAndUpdateOptions" +
	    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
    					+ "] Proceeding to next entry.";
	    		if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
	    			LogSupport.debug(ctx,this, resultDescription);
	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
	    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
	    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_ALLOW_ERR_MSG;
	    		return;
	    	}
	    	ValidateAndUpdateQueryResult validateAndUpdateQueryResult  = 
	    							validateAndUpdateOptionsResponseE.getValidateAndUpdateOptionsResponse().getValidateAndUpdateQueryResult();

	    	if(validateAndUpdateQueryResult.getBalanceCheckResultCode() == CronConstant.SCHEDULED_PP_VALIDATE_OPTIONS_BALANCE_CHECK_SUCCESS
	    			&& validateAndUpdateQueryResult.getValidationResultCode() == CronConstant.SCHEDULED_PP_VALIDATE_OPTIONS_SUCCESS && !isPickNPayPricePlan){
	    		//Call priceplan change API
	    		SubscriptionPricePlan subPlanOption;
	            subPlanOption = new SubscriptionPricePlan();
	            subPlanOption.setIsSelected(true);
	            
	            int currentVerionOfToProvisionPriceplan = -1;
	            PricePlan plan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan);  
	            PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(ctx, toProvisionPriceplan);
            	if(currentVersion == null)
            	{
            		currentVerionOfToProvisionPriceplan = -1;
            	}
            	else
            	{
            		currentVerionOfToProvisionPriceplan = currentVersion.getVersion();
            	}
	            PricePlanVersion version = PricePlanSupport.getVersion(ctx, plan.getId(), currentVerionOfToProvisionPriceplan);
	            List<com.redknee.app.crm.bean.PricePlanVersion> ppvList = new ArrayList<com.redknee.app.crm.bean.PricePlanVersion>();
	            ppvList.add(version);
	            com.redknee.util.crmapi.wsdl.v3_0.types.serviceandbundle.PricePlan apiPricePlan = PricePlanToApiAdapter
	                    .adaptPricePlanToApi(ctx, plan, ppvList);
	            
	            subPlanOption.setPricePlanDetails(apiPricePlan);
	            
	            //removedServicesAndBundlesSize is 0 in case of (legacy) switch price plan, for pickNpay update is done by an IVP call
	            PricePlanOption[] options = new PricePlanOption[requestedServicesAndBundlesSize + removedServicesAndBundlesSize];
	            
	            Map<ServiceFee2ID,ServiceFee2> serviceFee = version.getServiceFees(ctx);
	            Map<Long,BundleFee> bundlefees = version.getServicePackageVersion(ctx).getBundleFees();
	            int count = 0;
	            for(int index = 0; index < requestedServiceOptionRefernceArr.length; index++){
					if (requestedServiceOptionRefernceArr[index]
							.getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_SERVICE
							&& serviceFee != null && !serviceFee.isEmpty() && serviceFee.get(
									requestedServiceOptionRefernceArr[index]
											.getIdentifier())
									.getServicePreference() == ServicePreferenceEnum.MANDATORY)
	            	{
            			continue;
	            	}
	            	else if(requestedServiceOptionRefernceArr[index].getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_BUNDLE
	            			&& bundlefees != null && !bundlefees.isEmpty() &&  bundlefees.get(requestedServiceOptionRefernceArr[index].getIdentifier()).getServicePreference() == ServicePreferenceEnum.MANDATORY)
	            	{
	            		continue;
	            	}
	            	else if (requestedServiceOptionRefernceArr[index].getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_AUX_SERVICE 
	            			|| requestedServiceOptionRefernceArr[index].getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_AUX_BUNDLE)
	            	{
	            		continue;
	            	}
	            	options[count] = new PricePlanOption();
	            	options[count].setIdentifier(requestedServiceOptionRefernceArr[index].getIdentifier());
	            	options[count].setIsSelected(requestedServiceOptionRefernceArr[index].getIsSelected());
	            	if(requestedServiceOptionRefernceArr[index].getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_SERVICE)
	            	{
	            		options[count].setOptionType(PricePlanOptionTypeEnum.SERVICE.getValue());
	            	}
	            	else if(requestedServiceOptionRefernceArr[index].getOptionType() == CronConstant.PRICEPLAN_OPTIONTYPE_BUNDLE)
	            	{
	            		options[count].setOptionType(PricePlanOptionTypeEnum.BUNDLE.getValue());
	            	}
	            	count++;
	            }
	            
	            if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
		    	{
	            	LogSupport.debug(ctx, this, "size of the array is : " + requestedServicesAndBundlesSize);
		    		for (int counter =0 ; counter < count; counter ++)
		    	    {
		    			LogSupport.debug(ctx,this, "Services/Bundles to be provisioned identifier " + options[counter].getIdentifier() + 
		    					"is selected:" + options[counter].getIsSelected());
			    	}
		    	}
	            subPlanOption.setItems(options);
	            SubscribersImpl subscriberImpl = new SubscribersImpl(ctx);
	            com.redknee.framework.xhome.msp.MSP.setBeanSpid(ctx, subscriber.getSpid());
	            PricePlanOptionUpdateResult[]  resultArr = 
	            		subscriberImpl.updateSubscriptionRating(ctx, subscriber, subPlanOption,true, "updateSubscriptionPricePlanOptions");
	            resultCode = CronConstant.SCHEDULED_PP_CHANGE_SUCCESS;
	            SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, "PASS");
	            notificationMessageDescription = "Success";
	    		return;
	    	}
	    	else if(isPickNPayPricePlan && validateAndUpdateQueryResult.getValidationResultCode() == CronConstant.SCHEDULED_PP_VALIDATE_OPTIONS_SUCCESS
	    			&& validateAndUpdateQueryResult.getBalanceCheckResultCode() == CronConstant.SCHEDULED_PP_VALIDATE_OPTIONS_BALANCE_CHECK_SUCCESS
	    			&& validateAndUpdateQueryResult.getProvisionResultCode() == CronConstant.SCHEDULED_PP_VALIDATE_OPTIONS_PROVISION_SUCCESS)
	    	{
	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_SUCCESS;
	            SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, "PASS");
	            notificationMessageDescription = "Success";
	    		return;
	    	}
	    	else{
	    		resultDescription = "Scheduled priceplan change fail due to failure response for validateAndUpdateOptions" +
	    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
    					+ "]. Balance check result:[" + validateAndUpdateQueryResult.getBalanceCheckResultCode() + 
    					"]. Validation Result Code:[" + validateAndUpdateQueryResult.getValidationResultCode() +
    					"]. Provision Result Code:[" + validateAndUpdateQueryResult.getProvisionResultCode() +
    					"]. Proceeding to next entry.";
	    		if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
	    			LogSupport.debug(ctx,this, resultDescription);
	    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
	    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
	    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG;
	    		return;
	    	}
		
    	} catch (Exception e) {
    		resultDescription = "Scheduled priceplan exception for subscriber: [" + subModificationSchedule.getSubscriptionId()
					+ "] failed. Proceeding to next entry.";
    		if (LogSupport.isEnabled(ctx, SeverityEnum.MINOR))
    			LogSupport.minor(ctx,this, resultDescription, e);
    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    		notificationMessageDescription = CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG;
    		if(subscriber != null)
    			SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    		return;
		}
    	finally{
    		subModificationSchedule.setEndDate(new java.util.Date(System.currentTimeMillis()));
    		subModificationSchedule.setStatus(resultCode);
    		try {
				home.store(ctx, subModificationSchedule);
			} catch (Exception e) {
				resultDescription = "Scheduled priceplan exception for subscriber: [" + subModificationSchedule.getSubscriptionId()
						+ "] :Not able to update end time.Proceeding to next entry.";
				if (LogSupport.isEnabled(ctx, SeverityEnum.MINOR))
					LogSupport.minor(ctx,this, resultDescription, e);
	    		if(subscriber != null)
	    			SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
	    		
			}
    		try {
    			if(account != null){
    				TopUpSchedule existingSchedule = 
						HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.BAN, account.getBAN()));
    				if(existingSchedule != null){
    					existingSchedule.setPlanChangeScheduled(false);
    					if(resultCode != CronConstant.SCHEDULED_PP_CHANGE_SUCCESS && oldATUAmount != -1)
    						existingSchedule.setAmount(oldATUAmount);
    					HomeSupportHelper.get(ctx).storeBean(ctx, existingSchedule);
    				}
    			}
			} catch (Exception e) {
				resultDescription = "Scheduled priceplan exception for subscriber: [" + subModificationSchedule.getSubscriptionId()
						+ "] :Not able to update ATU schedule.Proceeding to next entry.";
				if (LogSupport.isEnabled(ctx, SeverityEnum.MINOR))
					LogSupport.minor(ctx,this, resultDescription, e);
	    		if(subscriber != null)
	    			SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);

			}
    		SubscriptionNotificationSupport.sendScheduledPPChangeNotification(ctx, subscriber, resultCode, notificationMessageDescription);
    	}
    }
    
    private PricePlanVersion getpricePlanversion(Context ctx,long toProvisionPriceplan) throws HomeException
    {
    	int currentVerionOfToProvisionPriceplan = -1;
        PricePlan plan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan);  
        PricePlanVersion currentVersion = PricePlanSupport.getCurrentVersion(ctx, toProvisionPriceplan);
    	if(currentVersion == null)
    	{
    		currentVerionOfToProvisionPriceplan = -1;
    	}
    	else
    	{
    		currentVerionOfToProvisionPriceplan = currentVersion.getVersion();
    	}
        PricePlanVersion version = PricePlanSupport.getVersion(ctx, plan.getId(), currentVerionOfToProvisionPriceplan);
        
        return version;
    }
    
    private boolean validateCurrentPriceplan(Map<String, String> requestSupportSnapshotMap, Subscriber subscriber){
    	boolean isValidPriceplan = false;
    	if(requestSupportSnapshotMap.get(CronConstant.OLD_PRICEPLAN_ID) != null && 
    			requestSupportSnapshotMap.get(CronConstant.OLD_PRICEPLAN_ID_VERSION) != null){
    		if((subscriber.getPricePlan() == Long.parseLong(requestSupportSnapshotMap.get(CronConstant.OLD_PRICEPLAN_ID))) &&
    			 (subscriber.getPricePlanVersion() == Integer.parseInt(requestSupportSnapshotMap.get(CronConstant.OLD_PRICEPLAN_ID_VERSION))))
    			isValidPriceplan = true;
    	}
    	return isValidPriceplan;
    }
    
    /*
     * return true is price plan is switched of current subscriber
     */
    private boolean isPricePlanSwitching(Context ctx, Subscriber subscriber , long toProvisionPriceplan) throws HomeException{
    	boolean isPricePlanSwitched = false;

    	PricePlan plan = PricePlanSupport.getPlan(ctx, toProvisionPriceplan); 

    	if (plan != null && plan.getIdentifier() != subscriber.getPricePlan() )
    		isPricePlanSwitched = true;

    	return isPricePlanSwitched;
    }

    private boolean validateScheduledPriceplanState(Context ctx, long scheduledPriceplan) throws Exception{
    	boolean isValidState = false;
		PricePlan priceplan = PricePlanSupport.getPlan(ctx, scheduledPriceplan);
		if(priceplan != null)
		{
			if(priceplan.getState().equals(PricePlanStateEnum.ACTIVE))
			{
				isValidState = true;
			}
			// PickNPay feature: If a subscriber creates a future dated plan change for PickNPay plan, 
			// and the new plan is grandfathered prior to the end of the subscriber's current cycle, we should still allow this switch to the grandfathered plan
			else if(priceplan.getState().equals(PricePlanStateEnum.GRANDFATHERED) && priceplan.getPricePlanSubType().equals(PricePlanSubTypeEnum.PICKNPAY))
			{
				isValidState = true;
			}
		}
    	return isValidState;
    }

    private boolean filterPriceplan(Context ctx, SubModificationSchedule subModificationSchedule, Account account, 
    		Subscriber subscriber, Map<String, String> requestSnapshotMap ,SelfcareRequestHeader selfcareRequestHeader, 
    		StringHolder notificationMessageDescriptionHolder, IntegerHolder resultCodeHolder) throws Exception{
    	
    	boolean isValidPriceplanSwitch = false;
    	PricePlanService pricePlanService = (PricePlanService)ctx.get(CronConstant.IVP_PRICEPLAN_SERVICE);
    	
    	int resultCode = CronConstant.SCHEDULED_PP_CHANGE_SUCCESS;
    	String resultDescription = "";
    	FilterPricePlansResponseE filterPricePlansResponseE = null;
    	
    	int deviceTypeId = (int)subscriber.getDeviceTypeId();
		int subscriberType  = subscriber.getSubscriberType().getIndex();//billing type [prepaid/postpaid/hybrid]
		int subscriptionType = (int)subscriber.getSubscriptionType();

		int accountType = (int)account.getType();
		int technologyType = (int)subscriber.getTechnology().getIndex();
		long toProvisionPriceplan =  Long.parseLong(requestSnapshotMap.get(CronConstant.NEW_PRICEPLAN_ID));
		
    	FilterPricePlansE filterPricePlansE = new FilterPricePlansE();
    	FilterPricePlans filterPricePlans = new FilterPricePlans();
    	
    	filterPricePlans.setHeader(selfcareRequestHeader);
    	filterPricePlans.setSpid(subscriber.getSpid());
    	filterPricePlans.setBillingType(subscriberType);
    	filterPricePlans.setSubscriptionType(subscriptionType);
    	filterPricePlans.setScEnabled(true);//for selfcare
    	filterPricePlans.setAccountType(accountType);
    	filterPricePlans.setTechnologyType(technologyType);
    	filterPricePlans.setDeviceType(deviceTypeId);
    	
    	if(requestSnapshotMap.get(CronConstant.DEVICE_MODEL_ID) != null && 
    			!requestSnapshotMap.get(CronConstant.DEVICE_MODEL_ID).equals(""))
    		filterPricePlans.setDeviceModelId(Long.parseLong(requestSnapshotMap.get(CronConstant.DEVICE_MODEL_ID)));

    	
    	filterPricePlans.setChannelType(subModificationSchedule.getChannel());
    	

    	if(requestSnapshotMap.get(CronConstant.CREDIT_CATEGORY_TYPE) != null && 
    			!requestSnapshotMap.get(CronConstant.CREDIT_CATEGORY_TYPE).equals(""))
    		filterPricePlans.setCreditCategoryType(Integer.parseInt(requestSnapshotMap.get(CronConstant.CREDIT_CATEGORY_TYPE)));
    	
    	filterPricePlans.setLanguage(subscriber.getBillingLanguage());
    	filterPricePlans.setMsisdn(subscriber.getMsisdn());
    	filterPricePlans.setContractId(subscriber.getSubscriptionContract(ctx));
    	
    	if(requestSnapshotMap.get(CronConstant.PROVINCE) != null && !requestSnapshotMap.get(CronConstant.PROVINCE).equals("")){
	    	GenericParameter regionParam = new GenericParameter();
	    	regionParam.setName(CronConstant.PROVINCE);
	    	regionParam.setStringvalue(requestSnapshotMap.get(CronConstant.PROVINCE));
	    	filterPricePlans.addParameter(regionParam);
	    }
    	filterPricePlansE.setFilterPricePlans(filterPricePlans);
    	
    	//Call IVP
    	if(pricePlanService == null){
    		resultDescription = "Scheduled priceplan change fail due to no priceplan service" +
    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
					+ "] Proceeding to next entry.";
            if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
            	LogSupport.debug(ctx,this, resultDescription);
    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    		notificationMessageDescriptionHolder.setString(CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG);
    		resultCodeHolder.setValue(resultCode);
    		
    		return isValidPriceplanSwitch;
    	}
    	filterPricePlansResponseE = pricePlanService.filterPricePlans(filterPricePlansE);
    	if(filterPricePlansResponseE == null){
    		resultDescription = "Scheduled priceplan change fail due to null response for filterPricePlan" +
    				" for subscriber: [" + subModificationSchedule.getSubscriptionId()
					+ "] Proceeding to next entry.";
    		if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
    			LogSupport.debug(ctx,this, resultDescription);
    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    		notificationMessageDescriptionHolder.setString(CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_INTERNAL_ERR_MSG);
    		resultCodeHolder.setValue(resultCode);
    		return isValidPriceplanSwitch;
    	}
    	PricePlanFilterQueryResult[] pricePlanFilterQueryResults = filterPricePlansResponseE.getFilterPricePlansResponse().get_return();
    	for(PricePlanFilterQueryResult pricePlanFilterQueryResult : pricePlanFilterQueryResults){
    		if(pricePlanFilterQueryResult.getPricePlan().getId() == toProvisionPriceplan){
    			isValidPriceplanSwitch = true;
    			break;
    		}
    	}
    	if(!isValidPriceplanSwitch){
    		resultDescription = "Scheduled priceplan change for subscriber:[" + subModificationSchedule.getSubscriptionId()
    				 + "] to priceplan: [" + toProvisionPriceplan + "] cancelled due to rejection from IVP during filter priceplan.";
    		if (LogSupport.isEnabled(ctx, SeverityEnum.DEBUG))
    			LogSupport.debug(ctx,this, resultDescription);
    		resultCode = CronConstant.SCHEDULED_PP_CHANGE_FAILED;
    		SubscriberNoteSupport.createScheduledPriceplanChangeNote(ctx, this, subscriber, resultCode, resultDescription);
    		notificationMessageDescriptionHolder.setString(CronConstant.SCHEDULED_PP_CHANGE_NOTIFICATION_PP_NOT_VALID_ERR_MSG);
    		resultCodeHolder.setValue(resultCode);
    	}
    	return isValidPriceplanSwitch;
    
    }
    private OptionReference getOptionReference(long identifier, int optionType, int queryType){
    	OptionReference optionReference = new OptionReference();
		optionReference.setIdentifier(identifier);
		optionReference.setOptionType(optionType);
		optionReference.setUpdateType(queryType);
		optionReference.setIsSelected(true);
		return optionReference;
    }
    
}
