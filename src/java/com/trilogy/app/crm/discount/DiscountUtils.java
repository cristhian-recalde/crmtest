package com.trilogy.app.crm.discount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.DiscountCheckEnum;
import com.trilogy.app.crm.bean.DiscountEvent;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountEventActivityStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventHome;
import com.trilogy.app.crm.bean.DiscountEventStatusEnum;
import com.trilogy.app.crm.bean.DiscountEventTypeEnum;
import com.trilogy.app.crm.bean.DiscountServiceTypeEnum;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.calculation.support.InvoiceSupport;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.FirstDeviceDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.MasterPackDiscountOutputHolder;
import com.trilogy.app.crm.core.ruleengine.PairedDiscountCriteriaHolder;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.LogSupport;

public class DiscountUtils {
	

	public static boolean containsAnyContract(Context ctx, Account account) {
		return account.getContract(ctx) == -1 ? false : true;
	}

	

	public static Map<Long, List<SubscriberIfc>> getSubscriptionMapByType(
			final Context ctx, List<SubscriberIfc> subList) {
		Map<Long, List<SubscriberIfc>> map = new HashMap<Long, List<SubscriberIfc>>();

		// Create map by subscriptionType
		for (SubscriberIfc subscriber : subList) {
			if(!subscriber.getState().equals(SubscriberStateEnum.SUSPENDED))
			{
				List<SubscriberIfc> subListByType = map.get(subscriber
						.getSubscriptionType());
				if (null == subListByType) {
					subListByType = new ArrayList<SubscriberIfc>();
				}
				subListByType.add(subscriber);
				map.put(subscriber.getSubscriptionType(), subListByType);
			}
		}

		// Sort the list present in map by priority
		Iterator<Long> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			Long subscriptionType = itr.next();
			map.put(subscriptionType,
					getSubscriptionListByPriority(ctx,
							map.get(subscriptionType)));
		}

		return map;
	}

	/**
	 * Returns the sorted list of subscription based on PP priority. In case of
	 * same PP priority, subscription activated first will get higher priority.
	 * In case of same activation date, subscription created first will get
	 * higher priority. Price plan Priority: 1 is highest priority.
	 * 
	 * @param ctx
	 * @param subList
	 * @return
	 */
	public static List<SubscriberIfc> getSubscriptionListByPriority(
			final Context ctx, List<SubscriberIfc> subList) {

		if (subList.isEmpty()) {
			return null;
		}

		if (subList.size() == 1) {
			return subList;
		}

		Collections.sort(subList, new Comparator<SubscriberIfc>() {

			@Override
			public int compare(SubscriberIfc o1, SubscriberIfc o2) {
				Subscriber subFirst = (Subscriber) o1;
				Subscriber subSecond = (Subscriber) o2;

				long ppidFirst = subFirst.getPricePlan();
				long ppidSecond = subSecond.getPricePlan();

				PricePlan plan;
				try {
					plan = PricePlanSupport.getPlan(ctx, ppidFirst);

					long priorityFirst = plan != null ? plan
							.getPricePlanPriority() : -1;

					plan = PricePlanSupport.getPlan(ctx, ppidSecond);
					long prioritySecond = plan != null ? plan
							.getPricePlanPriority() : -1;

					if (priorityFirst == prioritySecond) {
						/**
						 * prioritize based on subscriber activation date
						 */
						if (subFirst.getStartDate() != null
								&& subSecond.getStartDate() != null
								&& subFirst.getStartDate().before(
										subSecond.getStartDate())) {
							return -1;
						} else if (subFirst.getStartDate() != null
								&& subSecond.getStartDate() != null
								&& subFirst.getStartDate().after(
										subSecond.getStartDate())) {
							return 1;
						}
						/**
						 * Priority based on subscriber creation date
						 */
						else if (subFirst.getDateCreated().before(
								subSecond.getDateCreated())) {
							return -1;
						} else {
							return 1;
						}
					} else {
						return (int) (priorityFirst - prioritySecond);
					}
				} catch (HomeException e) {
					// TODO Auto-generated catch block
				}
				return 0;
			}
		});

		return subList;
	}


	/**
	 * Creates the DiscountEvent entry, used in case of Paired discount For each
	 * output an reference discount event entry is identified based on
	 * subscription type, this entry is used as a reference for creating new
	 * entry.
	 * 
	 * @param ctx
	 * @param outputList
	 * @param discountEvents
	 * @param discountEventType
	 * @return
	 */
/*	public static boolean updateElseCreatePairDiscountEvent(Context ctx,
			PairedDiscountOutputHolder output,
			List<DiscountEvent> discountEvents,
			DiscountEventTypeEnum discountEventType, SubscriberIfc subscriber,
			Account account, AdjustmentTypeEnum adjustmentType,
			Map<String, Boolean> trackDiscount) {
		// Update the discount event entry in the database
		try {
			DiscountEvent discountEvent = new DiscountEvent();
			boolean eventExist = false;
			// check if the discountEvent entry is already present
			for (DiscountEvent event : discountEvents) {
				if (event.getSubId().equals(subscriber.getId())
						&& event.getDiscountType().equals(discountEventType)
						&& ((Long) event.getServiceId()).equals(output
								.getServiceID())) {
					discountEvent = event;
					eventExist = true;
					break;
				}
			}
			// fill the discountEvent bean
			discountEvent.setBan(account.getBAN());
			discountEvent.setSpid(account.getSpid());
			discountEvent.setPricePlanId(subscriber.getPricePlan());
			discountEvent.setSubId(subscriber.getId());
			discountEvent.setSubscriptionType(subscriber.getSubscriptionType());
			discountEvent.setDiscountClass(output.getDiscountClass());
			discountEvent.setDiscountType(discountEventType);
			discountEvent.setServiceId(output.getServiceID());
			discountEvent.setAdjustmentType(adjustmentType);
			discountEvent.setState(DiscountEventStatusEnum.ACTIVE);
			discountEvent.setAuxiliaryServiceId(-1);
			discountEvent
					.setDiscountServiceType(DiscountServiceTypeEnum.SERVICE_DISCOUNT);
			discountEvent
					.setContractDiscountCheck(DiscountCheckEnum.DISCOUNT_CHECK_COMPLETE);
			// check if the service has payer attached
			SubscriberServices subService = SubscriberServicesSupport
					.getSubscriberServiceRecord(ctx, subscriber.getId(),
							output.getServiceID());

			if (null != subService && null != subService.getPayer()
					&& !subService.getPayer().isEmpty()) {
				discountEvent.setPayerId(subService.getPayer());
			}

			Home discountEventHome = (Home) ctx.get(DiscountEventHome.class);
			if (null == trackDiscount.get(discountEvent.getSubId())
					|| output.getMultipleDiscount()) {
				if (eventExist) {
					discountEventHome.store(discountEvent);
					LogSupport.debug(
							ctx,
							DiscountUtils.class.getName(),
							"[storeDiscountEvent] For BAN:"
									+ discountEvent.getBan()
									+ " updated discountEvent entry "
									+ discountEvent);
				} else {
					discountEventHome.create(discountEvent);
					LogSupport.debug(
							ctx,
							DiscountUtils.class.getName(),
							"[createDiscountEvent] For BAN:"
									+ discountEvent.getBan()
									+ " added discountEvent entry "
									+ discountEvent);
				}
				trackDiscount.put(discountEvent.getSubId(), true);
			}
		} catch (HomeException e) {

		}
		return true;
	}*/

		
  private static int getPairedSubscriptionCount(final List<PairedDiscountCriteriaHolder> listPairedDiscountCriteria,final List<SubscriberIfc> subscriberList)
	{
		if(listPairedDiscountCriteria!=null && !listPairedDiscountCriteria.isEmpty())
		{
			List<Long> pricePlanId = new ArrayList<Long>();
			List<SubscriberIfc> pairedSubscriberList = new ArrayList<SubscriberIfc>();
			Map<Integer,List<SubscriberIfc>> uniquePaired = new HashMap<Integer,List<SubscriberIfc>>();
			for(PairedDiscountCriteriaHolder pairedDiscountObject: listPairedDiscountCriteria)
			{
				pricePlanId.add(pairedDiscountObject.getPricePlanID());	
			}
			
			int count=0;
			List<Long> pairedPpId = new ArrayList<Long>();
		    for(SubscriberIfc sub: subscriberList)
			{
				  for(Long ppId: pricePlanId)
				  {
					  if(sub.getPricePlan()==ppId && !pairedPpId.contains(sub.getPricePlan()))
					  {
						  pairedSubscriberList.add(sub);
						  pairedPpId.add(ppId);
					  }
				  }
				if(pairedSubscriberList.size()==pricePlanId.size())
				{
					count++;
					uniquePaired.put(count,pairedSubscriberList);
					pairedSubscriberList.clear();
					pairedPpId.clear();
				}
			}
			if(uniquePaired.isEmpty())
			{
				return 0;
			}else
			{
				return uniquePaired.size();
			}
	       
		}
		return 0;
	}
		
		
		/*
		 *  This is used to update the Paired discount for the account
		 */
	/*public static boolean updatePairedDiscountEvent(Context ctx, Account account,List<PairedDiscountOutputHolder> outputList, List<DiscountEvent> discountEvents, AdjustmentTypeEnum adjustmentType, List<PairedDiscountCriteriaHolder> pairedDiscountCriteriaList,
			                          final List<SubscriberIfc> subscriberList, Map<String,Boolean> trackDiscount,DiscountEventTypeEnum discountEventType) {
		// Update the discount event entry in the database
		try
		{
			Home discountEventHome = (Home)ctx.get(DiscountEventHome.class);
		    Map<String,SubscriberIfc> pairedMap = new  HashMap<String,SubscriberIfc>();
			
			for(PairedDiscountOutputHolder output : outputList){
				
				
				int uniquePair=getPairedSubscriptionCount(pairedDiscountCriteriaList,subscriberList);
				List<SubscriberIfc> sortedSubList=getSortedSubscriberListByAge(output.getSubscriptionType(), subscriberList);
				if(sortedSubList!=null && !sortedSubList.isEmpty())
				{
					if(output.isMultipleDiscount()){
						int instanceCount=0;
						for(SubscriberIfc sub : sortedSubList)
						{

							PricePlanVersion version = PricePlanSupport.getCurrentVersion(ctx, sub.getPricePlan());
							// Assumption there will be only 1 mandatory service in PP
							
							ServiceFee2 serviceFee =  version.getMandatoryService(ctx);
							if(serviceFee.getServiceId() == output.getServiceID() && instanceCount<uniquePair)
							{
								instanceCount++;
								pairedMap.put(sub.getId(),sub);
							}
							
						}
						
					}else
					{
						int instanceCount=0;
						for(SubscriberIfc sub : sortedSubList)
						{
							if(!trackDiscount.containsKey(sub.getId()) && instanceCount<uniquePair)
							{
								instanceCount++;
								pairedMap.put(sub.getId(),sub);
							}
						}
					}
				}
				
				
				DiscountEvent discountEvent = null;
				for(DiscountEvent event : discountEvents){
					
					boolean eventExist = false;
					// check if the discountEvent entry is already present
					for (DiscountEvent event1 : discountEvents) {
						if (pairedMap.containsKey(event.getSubId())
								&& event1.getDiscountType().equals(discountEventType)
								&& ((Long) event1.getServiceId()).equals(output
										.getServiceID())) {
							discountEvent = event1;
							eventExist = true;
							break;
						}
					}
					if((DiscountEventStatusEnum.CALCULATION_PENDING.equals(event.getState()) || DiscountEventStatusEnum.REEVALUATION_PENDING
							.equals(event.getState()) || DiscountEventStatusEnum.ACTIVE.equals(event.getState())) && 
							DiscountServiceTypeEnum.SERVICE_DISCOUNT.equals(event.getDiscountServiceType())){
						if(pairedMap.containsKey(event.getSubId()) && (null==trackDiscount.get(event.getSubId())||output.getMultipleDiscount())){
							
							SubscriberIfc subscriber = pairedMap.get(event.getSubId());
							 discountEvent = new DiscountEvent();
							// fill the discountEvent bean
							discountEvent.setBan(account.getBAN());
							discountEvent.setSpid(account.getSpid());
							discountEvent.setPricePlanId(subscriber.getPricePlan());
							discountEvent.setSubId(subscriber.getId());
							discountEvent.setSubscriptionType(subscriber.getSubscriptionType());
							discountEvent.setDiscountClass(output.getDiscountClass());
							discountEvent.setDiscountType(discountEventType);
							discountEvent.setServiceId(output.getServiceID());
							discountEvent.setAdjustmentType(adjustmentType);
							discountEvent.setState(DiscountEventStatusEnum.ACTIVE);
							discountEvent.setAuxiliaryServiceId(-1);
							discountEvent.setDiscountServiceType(DiscountServiceTypeEnum.SERVICE_DISCOUNT);
				            discountEvent.setContractDiscountCheck(DiscountCheckEnum.DISCOUNT_CHECK_COMPLETE);
				            // check if the service has payer attached
				            SubscriberServices subService = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, subscriber.getId(), output.getServiceID());
				            
				            if(null != subService &&
				            		null != subService.getPayer() &&
				            		!subService.getPayer().isEmpty()){
				            	discountEvent.setPayerId(subService.getPayer());
				            }
				            if (eventExist) {
								discountEventHome.store(discountEvent);
								LogSupport.debug(
										ctx,
										DiscountUtils.class.getName(),
										"[storeDiscountEvent] For BAN:"
												+ discountEvent.getBan()
												+ " updated discountEvent entry "
												+ discountEvent);
							} else {
								discountEventHome.create(discountEvent);
								LogSupport.debug(
										ctx,
										DiscountUtils.class.getName(),
										"[createDiscountEvent] For BAN:"
												+ discountEvent.getBan()
												+ " added discountEvent entry "
												+ discountEvent);
							}
							trackDiscount.put(discountEvent.getSubId(), true);
						}else{
							LogSupport.minor(ctx, DiscountUtils.class.getName(), "Skipped to update the discount as subsriber is not of the same type mentioned in the output");
						}						
						
						}else{
							LogSupport.minor(ctx, DiscountUtils.class.getName(), "Skipped to update the discount as subsriber is not of the same type mentioned in the output");
						}
					}
				}
			
		}
		catch(HomeException e){

		}
		return true;
	}*/
  
   /*
    * 
    */
    public static boolean fillDiscountEventList(final Context ctx,
    		 List<DiscountEventActivity> newDiscountEventList,
    		 List<DiscountEventActivity> continuedDiscountEventList,
    		 List<DiscountEventActivity> proratedDiscountEventList,
    		 List<DiscountEventActivity> reverseDiscountEventList,
    		 final List<DiscountEventActivity> existingDiscountEventList,
    		 final Account account)
    {
    	if(null!=existingDiscountEventList && !existingDiscountEventList.isEmpty())
    	{
    		for(DiscountEventActivity eventObj:existingDiscountEventList)
    		{
    			try
    			{
    			if((eventObj.getDiscountAppliedTillDate()==null && eventObj.getDiscountAppliedFromDate() ==null)&& eventObj.getState().equals(DiscountEventActivityStatusEnum.ACTIVE))
    			{
    				if(eventObj.getDiscountEffectiveDate().before(InvoiceSupport.getCurrentBillingDate(account.getBillCycleDay(ctx))))
    				{
    					proratedDiscountEventList.add(eventObj);
    				}else
    				{
    					newDiscountEventList.add(eventObj);
    				}
    			}else if(eventObj.getState().equals(DiscountEventActivityStatusEnum.ACTIVE)
    					&& CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date()).
    					after(eventObj.getDiscountAppliedTillDate()))
    			{
    				continuedDiscountEventList.add(eventObj);
    			}else if(eventObj.getState().equals(DiscountEventActivityStatusEnum.CANCELLATION_PENDING))
    			{
    				reverseDiscountEventList.add(eventObj);
    			}
    			}catch(Exception ex)
    			{
    				
    			}
    		}
    	}
          
    	return true;
    }
}
