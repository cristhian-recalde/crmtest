package com.trilogy.app.crm.discount.quote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.BillCycle;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.DiscountEventActivity;
import com.trilogy.app.crm.bean.DiscountTransactionHistHome;
import com.trilogy.app.crm.bean.DiscountTransactionHistTransientHome;
import com.trilogy.app.crm.bean.QuotationActionTypeEnum;
import com.trilogy.app.crm.bean.ServiceFee2ID;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesHome;
import com.trilogy.app.crm.bean.SubscriberServicesTransientHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTransientHome;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionTransientHome;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryHome;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistoryTransientHome;
import com.trilogy.app.crm.core.bean.ifc.SubscriberIfc;
import com.trilogy.app.crm.core.ruleengine.BusinessRule;
import com.trilogy.app.crm.core.ruleengine.BusinessRuleHome;
import com.trilogy.app.crm.core.ruleengine.DiscountPriority;
import com.trilogy.app.crm.discount.DiscountHandler;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.DiscountSupportImpl;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.DiscountResult;
import com.trilogy.util.crmapi.wsdl.v3_0.types.quotation.QuotationResult;

public class DiscountForecastingImpl extends AbstractDiscountForeCasting
{
	private List<QuotationResult> discountResultList = new ArrayList<QuotationResult>();

	private DiscountResult discountResults[] = null;
	private Account rootAccount;
	private Account account;
	private List<SubscriberIfc> subscribers = new ArrayList<SubscriberIfc>();
	private List<String> subscriberIds = new ArrayList<String>();
	private Map<String, Boolean> trackDiscount= new HashMap<String, Boolean>();
	private List<DiscountPriority> discountPriorityList = new ArrayList<DiscountPriority>();
	private List<QuoteDiscountResponse> responses = new ArrayList<QuoteDiscountResponse>();
	private Map<String, String> subscriberServicePath = new HashMap<String, String>();
	
	/**
	 *  this will hold the discount event activity that need to be created 
	 */
	private Collection<DiscountEventActivity> discountEventActivityForCreation = new ArrayList<DiscountEventActivity>();
	
	/**
	 * This will hold the discount event activity that need to be updated, specially the expired entries
	 */
	private Collection<DiscountEventActivity> discountEventActivityForUpdate = new ArrayList<DiscountEventActivity>();
	 
	/**
	 * this will hold the discount event activity that need to be continued,
	 * specially the entries which were applicable in past and in present also 
	 */
	private Collection<DiscountEventActivity> discountEventActivityContinued = new ArrayList<DiscountEventActivity>();
	
	private Collection<DiscountEventActivity> existingDiscountEventActivity = new ArrayList<DiscountEventActivity>();
	
	private List<DiscountHandler> discountEvaluationOrder = new ArrayList<DiscountHandler>();
	
	
	public DiscountForecastingImpl() 
	{
	}

	  
	@Override
	protected void prepareDataForDiscountForecasting(Context ctx,
			QuoteDiscountRequest req) throws QuoteDiscountException
	{
		List<QuoteAccount> accounts = req.getRequestedAccounts();
		for (QuoteAccount quoteAccount : accounts) 
		{
			
			try {
				rootAccount = AccountSupport.getAccount(ctx, quoteAccount.getRootBan());
				account = AccountSupport.getAccount(ctx, quoteAccount.getBan());
				
				BillCycle billCycle = BillCycleSupport.getBillCycleForBan(ctx, rootAccount.getBAN());
				Date cycleStartDate = BillCycleSupport.getCurrentCycleStartDate(ctx, new Date(), billCycle.getDayOfMonth());

				ctx.put(DiscountSupportImpl.DISCOUNT_GRADE, quoteAccount.getDiscountGrade());
				
				Collection<Subscriber> subList = rootAccount.getAllSubscribers(ctx);
				
				for(Subscriber sub:subList)
				{
					if(!SubscriberStateEnum.INACTIVE.equals(sub.getState()))
					{
						if (LogSupport.isDebugEnabled(ctx)){
							LogSupport.debug(ctx, this, "Existing Subscriber bean is :"+sub);
						}
						subscribers.add(sub);
//						subscriberIds.add(sub.getId());
					}
					else
					{
						new DebugLogMsg(this, "DiscountClassAssignment process skipped subscriber:'" + sub.getId()+ " as its in " +  sub.getState() + " state.").log(ctx);
					}
				}
				
				existingDiscountEventActivity= DiscountSupportImpl.findExistingDiscountEventActivity(ctx, rootAccount);
			    
				DiscountSupportImpl.initializeDiscountEvaluationOrder(ctx, rootAccount,discountEvaluationOrder,discountPriorityList);
				
				ctx.put(SubscriberHome.class, new SubscriberTransientHome(ctx));
				Home subscriberTransientHome = (Home) ctx.get(SubscriberHome.class);

			    
				Map<String, Map> subscriberServiceMap = new HashMap<String, Map>();
				for(SubscriberIfc sub : subscribers) 
				{
					    subscriberTransientHome.create(ctx, sub);
					    if (LogSupport.isDebugEnabled(ctx)){
					    	LogSupport.debug(ctx, this, "Adding Existing Subscriber "+sub.getId()+" into transient home");
					    }
				        Map<ServiceFee2ID, SubscriberServices> subscribersServices = SubscriberServicesSupport.getSubscribersServices(ctx,sub.getId());
			        	subscriberServiceMap.put(sub.getId(), subscribersServices);
				}
				
				
				
				ctx.put(SubscriberServicesHome.class, new SubscriberServicesTransientHome(ctx));
				Home subscriberServicesTransientHome = (Home)ctx.get(SubscriberServicesHome.class);
				
				subscriberServicesTransientHome = (Home)ctx.get(SubscriberServicesHome.class);

				Map<String, List<SubscriberSubscriptionHistory>> existingSubServices = 
						new HashMap<String, List<SubscriberSubscriptionHistory>>();
				
		        for(Map<ServiceFee2ID, SubscriberServices>  subServices : subscriberServiceMap.values())
		        {
			        for(SubscriberServices subService : subServices.values())
			        {
			        	subscriberServicesTransientHome.create(ctx,subService);
			        	
			        	List<SubscriberSubscriptionHistory> historyData = 
			        			(List<SubscriberSubscriptionHistory>) DiscountSupportImpl.findChargingHistoryRecordForService(
								ctx,
								SubscriberSupport.getSubscriber(ctx, subService.getSubscriberId()),
								subService.getService(ctx),
								cycleStartDate,
								new Date());
			        	
			        	existingSubServices.put(subService.getSubscriberId()+subService.getServiceId(), historyData);
			        }
		        }
		        
		        /**
		         * Put subscribersubscriptionhistory data in memory
		         */
		        ctx.put(SubscriberSubscriptionHistoryHome.class, new SubscriberSubscriptionHistoryTransientHome(ctx));
		        Home historyTransientHome = (Home)ctx.get(SubscriberSubscriptionHistoryHome.class);
		        for (List<SubscriberSubscriptionHistory> listOfSubService
		        		: existingSubServices.values()) 
		        {
		        	for (SubscriberSubscriptionHistory subscriberSubscriptionHistory : listOfSubService) 
		        	{
		        		historyTransientHome.create(subscriberSubscriptionHistory);
		        	}
		        }
		        
		        List<QuoteSubscriber> requestedSubscribers = req.getRequestedSubscribers();
				for (QuoteSubscriber quoteSubscriber : requestedSubscribers) 
				{
					if(quoteSubscriber.getActionType() == QuotationActionTypeEnum.ADD_INDEX)
					{
						Subscriber  dummySub = new Subscriber();
						dummySub.setId(quoteSubscriber.getCorrelationId());
						dummySub.setSubscriptionType(quoteSubscriber.getSubscriptionType());
						dummySub.setPricePlan(quoteSubscriber.getPriceplanId());
						dummySub.setBAN(account.getBAN());
						dummySub.setSpid(account.getSpid());
						subscriberTransientHome.create(ctx, dummySub);
						subscribers.add(dummySub);
						subscriberIds.add(dummySub.getId());
						
						if (LogSupport.isDebugEnabled(ctx)){
					    	LogSupport.debug(ctx, this, "Adding dummy subscriber "+dummySub.getId());
					    }
						
						List<QuoteSubscriberServiceDetail> items = quoteSubscriber.getItems();
						for (QuoteSubscriberServiceDetail quoteSubscriberServiceDetail : items)
						{
							SubscriberServices dummySubService = new SubscriberServices();
							dummySubService.setServiceId(quoteSubscriberServiceDetail.getServiceId());
							dummySubService.setPath(quoteSubscriberServiceDetail.getPath());
							dummySubService.setSubscriberId(dummySub.getId());
							subscriberServicesTransientHome.create(ctx, dummySubService);
							
							subscriberServicePath.put(dummySubService.getSubscriberId()
									+dummySubService.getServiceId(), dummySubService.getPath());
							
							/**
							 * After adding dummy subscriber service record, we also need to add dummy subscriber subscription history record 
							 * for charging of this service
							 */
							SubscriberSubscriptionHistory dummyHist = XBeans.instantiate(SubscriberSubscriptionHistory.class, ctx);
							dummyHist.setChargedAmount(quoteSubscriberServiceDetail.getCharge());
							dummyHist.setRemainingAmount(quoteSubscriberServiceDetail.getCharge());
							dummyHist.setPath(quoteSubscriberServiceDetail.getPath());
							dummyHist.setItemType(ChargedItemTypeEnum.SERVICE);
							dummyHist.setServiceState(ServiceStateEnum.PROVISIONED);
							dummyHist.setSubscriberId(dummySubService.getSubscriberId());
							dummyHist.setItemIdentifier(dummySubService.getServiceId());
							dummyHist.setTimestamp_(new Date());
							dummyHist.setEventType(HistoryEventTypeEnum.CHARGE);
			        		historyTransientHome.create(dummyHist);

						}
					}
					else if(quoteSubscriber.getActionType() == QuotationActionTypeEnum.REMOVE_INDEX)
					{
							Subscriber subscriber = SubscriberSupport.lookupSubscriberForSubId(
									ctx, quoteSubscriber.getCorrelationId());
							if (subscriber == null) {
								if (LogSupport.isDebugEnabled(ctx)){
							    	LogSupport.debug(ctx, this, "Subscriber with identifier "+quoteSubscriber.getCorrelationId()+" is not exist, we can not remove");
							    }	
								throw new QuoteDiscountException("Invalid Subscriber ID: "+quoteSubscriber.getCorrelationId() +" for remove");
							} else {
						
								for (Iterator iterator = subscribers.iterator(); iterator
										.hasNext();) 
								{
									SubscriberIfc sub = (SubscriberIfc) iterator.next();
									if(sub.getId().equalsIgnoreCase(quoteSubscriber.getCorrelationId()))
									{
										iterator.remove();
									}
								}
							}
					}
				}
			 } catch (HomeException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void findApplicableDiscounts(Context ctx) 
	{
		for(DiscountHandler handler : discountEvaluationOrder)
		{
			// if initialization fails no need to evaluate the discount
			if(handler.init(ctx, rootAccount, subscribers))
			{
				if (LogSupport.isDebugEnabled(ctx)) 
				{
					LogSupport.debug(
							ctx,
							this,"Discount Handler : " + handler.getClass().getName() +" for processing account is called");
				}
				
				handler.evaluate(ctx, rootAccount, subscribers,existingDiscountEventActivity, 
							trackDiscount,discountEventActivityForCreation, discountEventActivityForUpdate, 
			          		discountEventActivityContinued);
				
				for (DiscountEventActivity eventActivity : discountEventActivityForCreation) {
					if (LogSupport.isDebugEnabled(ctx)){
				    	LogSupport.debug(ctx, this, "Discount event activity : "+eventActivity);
				    }					
				} 
				
			}	
		}
		
	}

	@Override
	protected void calculateDiscountValue(Context ctx) 
	{
		Home ruleHome = (Home) ctx.get(BusinessRuleHome.class);
		
		for (DiscountEventActivity discountActivity : discountEventActivityForCreation)
		{
			try 
			{
				if(subscriberIds.contains(discountActivity.getSubId()))
				{
					Context subCtx = ctx.createSubContext();
					subCtx.put(TransactionHome.class, new TransactionTransientHome(subCtx));
					subCtx.put(DiscountTransactionHistHome.class, new DiscountTransactionHistTransientHome(subCtx));
					
					Transaction txn = DiscountSupportImpl.generateDiscountTransactions(subCtx, discountActivity);
					if(txn != null)
					{
						QuoteDiscountResponse response = new QuoteDiscountResponse();
						String ruleId = discountActivity.getDiscountRuleId();
						long serviceId = discountActivity.getServiceId();
						String subId = discountActivity.getSubId();
						String path = subscriberServicePath.get(subId+serviceId);
						
						BusinessRule rule = (BusinessRule) ruleHome.find(ruleId);
						response.setDiscountAmount(txn.getAmount());
						response.setDiscountRule(rule.getBusinessRuleDesc());
						response.setDiscountType(discountActivity.getDiscountType().getDescription());
						response.setServiceId(serviceId);
						response.setSubscriberId(subId);
						response.setPath(path);
						
						responses.add(response);
					}
				}
			}
			catch (HomeException e) 
			{
				LogSupport.minor(ctx, this, "Error while calculating discount amount ", e);
			}
		}
	}

	@Override
	protected void formatResponse(Context ctx) 
	{
		for (String subId : subscriberIds) 
		{
			int i = 0;
			discountResults = new DiscountResult[responses.size()] ;
			for (QuoteDiscountResponse bssResponseObject : responses)
			{
				if(bssResponseObject.getSubscriberId().equals(subId))
				{
					DiscountResult discountResult = new DiscountResult();
					discountResult.setAmount(bssResponseObject.getDiscountAmount());
					discountResult.setDiscountRule(bssResponseObject.getDiscountRule());
					discountResult.setDiscountType(bssResponseObject.getDiscountType());
					discountResult.setProductId(bssResponseObject.getServiceId());
					discountResult.setProductPath(bssResponseObject.getPath());
					discountResults[i] = discountResult;
					i++;
				}
			}
			
			QuotationResult quoteResult = new QuotationResult();
			quoteResult.setCorrelationID(subId);
			quoteResult.setDiscountResult(discountResults);
			discountResultList.add(quoteResult);
		}
		
	}
	
	public List<QuotationResult> getDiscountQuoteResult(Context ctx)
	{
		return discountResultList;
	}
}
