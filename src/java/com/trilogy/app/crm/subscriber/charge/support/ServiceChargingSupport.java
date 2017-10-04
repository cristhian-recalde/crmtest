package com.trilogy.app.crm.subscriber.charge.support;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.CommonTime;
import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.RefundCalcBasisEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberServicesXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.home.ExtendSubscriberExpiryHome;
import com.trilogy.app.crm.priceplan.ServiceFee2ExecutionOrderComparator;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.CrmChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.SubscriberChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import java.util.Calendar;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.DefaultCalendarSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;


public abstract class ServiceChargingSupport
implements ChargingConstants
{


    private static final String CLASS_NAME_FOR_LOGGING = ServiceChargingSupport.class.getName();
    public static final String SKIP_UNIFIED_PROVISIONING_AND_CHARGING = "SKIP_UNIFIED_PROVISIONING_AND_CHARGING";
    /**
     * get service fee from price plan from old subscriber, and apply 
     * refund to new subscriber. 
     * @param ctx
     * @param provisionedServices
     * @param newSub
     * @param oldSub
     * @param handler
     * @param accumulator
     * @return
     */
    public static int transferRefund(
            final Context ctx, 
            final Collection provisionedServices,
            final Subscriber newSub, 
            final Subscriber oldSub, 
            final ChargeRefundResultHandler handler,
            final SubscriberChargingAccumulator accumulator
            )
    
    {   
         
        try {
            Map services = getProvisionedServices(ctx, oldSub.getPricePlan(ctx).getServiceFees(ctx).values(), 
                provisionedServices);
            return applyServicesCharge(ctx, services, newSub, oldSub, false, handler, 
                    ACTION_PROVISIONING_REFUND, accumulator, RUNNING_SUCCESS);
        } catch ( HomeException e)
        {
            new MajorLogMsg(ctx, "fail to get PPV for sub" + oldSub.getId(), e).log(ctx);
             
        }
        
        return RUNNING_ERROR_STOP;

    }
    
    
    static  public Collection<SubscriberServices> getCrossed( 
            final Context ctx, 
            final Collection <SubscriberServices> originals, 
            final Collection <SubscriberServices> duplicates)
    {
        Collection <SubscriberServices> ret = new HashSet();
        
        for (SubscriberServices service : duplicates)
        {
            CollectionSupportHelper.get(ctx).findAll(ctx, originals, 
                    new EQ(SubscriberServicesXInfo.SERVICE_ID, new Long(service.getServiceId())),
                    ret); 
        }
       
        return ret; 
        
    }
    

    public static int applyServicesChargeByIds(
            final Context ctx, 
            final Collection provisionedServices,
            final Subscriber sub, 
            final Subscriber oldSub, 
            final boolean isActivation, 
            final ChargeRefundResultHandler handler,
            final int action, 
            final SubscriberChargingAccumulator accumulator, 
            final int parentContinueState)
    
    {   
        if (parentContinueState == RUNNING_ERROR_STOP)
        {
            return parentContinueState;
        }
        
        int continueState = parentContinueState;
               
        Map services; 
        
        try {
            if (action == ACTION_PROVISIONING_REFUND)
            {
                // For refunding we don't care if the service is provisioned or not. As long as it has been charged, we can refund.
                services = getProvisionedServices(ctx, PricePlanSupport.getVersion(ctx, sub.getPricePlan(), sub.getPricePlanVersion()).getServiceFees(ctx).values(), 
                        provisionedServices);
            }
            else
            {
                services = getProvisionedServices(ctx, sub.getPricePlan(ctx).getServiceFees(ctx).values(), 
                        provisionedServices);
            }
            continueState = applyServicesCharge(ctx, services, sub, oldSub, isActivation, handler, 
                    action, accumulator, parentContinueState);
        } catch ( HomeException e)
        {
            new MajorLogMsg(ctx, "fail to get PPV for sub" + sub.getId(), e).log(ctx);
             
        }
        
        return continueState;

    }
  
    
    
    public static int applyServicesCharge(
            final Context ctx, 
            final Map services,
            final Subscriber sub, 
            final Subscriber oldSub,
            final boolean isActivation, 
            final ChargeRefundResultHandler handler,
            final int action, 
            final SubscriberChargingAccumulator accumulator, 
            final int parentContinueState)
    
    {
    	ctx.put(SubscribersApiSupport.Quantity_Support_For_Full_Charge, new BooleanHolder(true));
        if (parentContinueState == RUNNING_ERROR_STOP)
        {
            return parentContinueState;
        }
        
        int continueState = parentContinueState;
        
        List<ServiceFee2> serviceFees = new ArrayList(services.keySet());
        
        if (ACTION_PROVISIONING_REFUND == action)
        {
            Collections.sort(serviceFees, new ServiceFee2ExecutionOrderComparator(ctx, false));
        }
        else if (ACTION_PROVISIONING_CHARGE == action)
        {
            Collections.sort(serviceFees, new ServiceFee2ExecutionOrderComparator(ctx, true));
        }
        
        for ( Iterator i = serviceFees.iterator(); i.hasNext();)
        {
                final ServiceFee2 fee = (ServiceFee2)   i.next();
                final SubscriberServices subService = (SubscriberServices) services.get(fee);
                Service service; 
                try 
                {
                	service = ServiceSupport.getService(ctx, fee.getServiceId());
                } catch (Exception e)
                {
                	// should not happen. charge is not the first line for checking data integraty. 
                	continue; 
                }

                
                ChargableItemResult ret = null; 
                
                ret  = new ChargableItemResult(action, 
                        ChargingConstants.CHARGABLE_ITEM_SERVICE,  
                        fee, fee.getServiceId(), sub, oldSub); 
                
                ret.setChargableObjectRef(service); 
                ret.setAction(action); 
                 ret.isActivation= isActivation; 
  				initializedTransaction(ctx, ret, subService); 

  				if ( ret.getChargeResult()  == TRANSACTION_SUCCESS)
  				{
  					ret.setChargeResult(getEventType(ctx,sub, fee, service, action));

  					if ( ret.getChargeResult()  == TRANSACTION_SUCCESS)
  					{	
 
  						if ( continueState == RUNNING_SUCCESS)
  						{   
  							if (isForceChargingApplicable(ctx,ret))
  							{
  								// TT#13021413063 - creating another transaction for forced charging will be skipped 
  								// if it has been applied already during this transaction  								
  								// TT#13021413063 - checks if subscription charging for primary service is already done in
  								// CoreTransaction pipeline (during extention of expiry date), if yes skip it
  								if(!ctx.has(ServiceChargingSupport.IS_ALREADY_FORCE_CHARGED))
  								{
  									Context subCtx = ctx.createSubContext();
  									subCtx.put(ServiceChargingSupport.IS_ALREADY_FORCE_CHARGED, Boolean.TRUE);
  									handleServiceTransaction(subCtx, ret,  null);
  								}
  							}
  							else
  							{
  								SubscriberChargingSupport.validateTransaction(ctx, ret, null); 
  								SubscriberChargingSupport.updateTransactionValue(ctx, ret);

  								if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
  								{
  									handleServiceTransaction(ctx, ret,  null);     
  								}
  							}
            			
  						}  
  						else if ( continueState == RUNNING_CONTINUE_SUSPEND)
  						{
  							ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
  							ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND); 
  						}
  					}
  				}	

                
                ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
                
                if ( realHandler != null )
                {    
                    realHandler.handleTransaction(ctx, ret);
                    continueState = ret.getRunningState(); 
                
                    if( continueState == RUNNING_ERROR_STOP )
                    {
                        return continueState; 
                    } 
                }      

                if ( accumulator != null)
                {
                    accumulate(accumulator, ret); 
                }


        }    
        
        Context appCtx = (Context) ctx.get("app");
        appCtx.remove(ExtendSubscriberExpiryHome.IS_SUBSCRIPTION_ALREADY_EXTENDED_FOR_SUBSCRIBER + sub.getId());
        if(LogSupport.isDebugEnabled(ctx))
        {
            LogSupport.debug(ctx, ServiceChargingSupport.CLASS_NAME_FOR_LOGGING, "Extended subscriber cleared from context.");
        }
        
        return continueState; 
    }
    
    
    
    public static void initializedTransaction(Context parentCtx, final ChargableItemResult result, SubscriberServices subService)
    {
    	if (LogSupport.isDebugEnabled(parentCtx)) {
        	LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport initializedTransaction");
    		LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport initializedTransaction. " +
    				"subService.getServiceId : "+subService.getServiceId()+" and SubscriberServices:: "+subService);
        	}
    	CRMSpid crmSpid = null;
		try {
			crmSpid = HomeSupportHelper.get(parentCtx).findBean(parentCtx, CRMSpid.class,
					new EQ(CRMSpidXInfo.ID, result.getSubscriber().getSpid()));
		} catch (HomeException e) {
			LogSupport.minor(parentCtx, CLASS_NAME_FOR_LOGGING, "Exception while looking up Service Provider with id "
					+ result.getSubscriber().getSpid());
		}
    	Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, result.getSubscriber());
        
        long quantityToChargeFor=(subService.getChangedServiceQuantity() == -1)
				? subService.getServiceQuantity()
				: subService.getChangedServiceQuantity();

		long quantityVal = quantityToChargeFor;

        if (LogSupport.isDebugEnabled(parentCtx)) {
        	LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Quantity to charge for is set to quantityVal :: "+quantityVal
        			+ "subService.getServiceQuantity():: "+subService.getServiceQuantity()+" subService.getChangedServiceQuantity():: "+subService.getChangedServiceQuantity());
        }
        
        int ret = TRANSACTION_SUCCESS; 
        try 
        {
            final Service service = (Service) result.getChargableObjectRef();
            final ServiceFee2 fee = (ServiceFee2) result.getChargableObject();

            result.setChargingCycleType(getChargingCycleType(fee.getServicePeriod()));
            ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx, result.getChargingCycleType());

            final double ratio;
            Date transactionStartDate = null;

            Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            Date runningDateForRefund = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
            boolean postPaidSubCreationOnly = service.getPostpaidSubCreationOnly();
            if(postPaidSubCreationOnly == false && result.getSubscriber().isPostpaid())
            {
            	/*
            	 * refund should be full 
            	 */
            	if (SubscriberChargingSupport.isRefund(result.action))
            	{
            		ratio = -1.0d;
            	}
            	/*
            	 * charge should be full 
            	 */
            	else
            	{
            		ratio = 1.0d;
            	}

            }else if (SubscriberChargingSupport.isRefund(result.action))
            {
            	if (LogSupport.isDebugEnabled(parentCtx)) {
            		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Inside isRefund condition");
            	}
            	quantityVal = -quantityToChargeFor;
                ratio = handler.calculateRefundRate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                    result.getSubscriber().getSpid(), 
                    result.getSubscriber().getId(), fee);
                if (LogSupport.isDebugEnabled(parentCtx)) {
            		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "exit isRefund condition ratio:: "+ratio);
            	}
            }
            else if (SubscriberChargingSupport.isChargeAtUnProv(result.action)) {
            	if (LogSupport.isDebugEnabled(parentCtx)) {
            		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Inside isChargeAtUnProv condition");
            	}
				ratio = 1.0;
			}
            else if (SubscriberChargingSupport.isRefundAtUnProv(result.action)){

				// Product catalogue II: Change affects only the refund
				// calculations, based on spid configuration
            	if (LogSupport.isDebugEnabled(parentCtx)) {
            		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Inside isRefundAtUnProv quantityToChargeFor:: "+quantityToChargeFor+" runningDateForRefund:: "+runningDateForRefund);
            	}
				quantityVal = -quantityToChargeFor;
				if (crmSpid != null
						&& crmSpid.getCalculateRefundBasedOn() == RefundCalcBasisEnum.USAGE_DAYS	
						&& (fee.getServicePeriod().equals(ServicePeriodEnum.MONTHLY) || fee.getServicePeriod().equals(
								ServicePeriodEnum.MULTIMONTHLY))) {
					Date startDate = subService.getStartDate();
					int unBilledDays = 0;
					
					/* if (service.getActivationFee().getIndex() ==
					 * ActivationFeeModeEnum.PRORATE_INDEX) { unBilledDays =
					 * ServiceSupport.getUnbilledDays(ctx,
					 * result.getSubscriber().getSpid(), startDate,
					 * runningDate); }
					 */
					 
					runningDate = subService.getEndDate();
					
					//consider change indicator
					if (considerChangeIndicator(ctx,subService.getServiceId())) {
						runningDate = applyChangeIndicatorLogic(ctx,runningDate);
						ratio = calculateRefundRatioForUsageDays(ctx, crmSpid, result, subService, handler, runningDate,startDate, unBilledDays);
					}else{
						ratio = calculateRefundRatioForUsageDays(ctx, crmSpid, result, subService, handler, runningDateForRefund,startDate, unBilledDays);
					}
				} else { // legacy and default behaviour
					Date startDate = subService.getStartDate();
					Date newRunningDate = runningDate;
					int unBilledDays = 0;
					/*
					 * if (service.getActivationFee().getIndex() ==
					 * ActivationFeeModeEnum.PRORATE_INDEX) { unBilledDays =
					 * ServiceSupport.getUnbilledDays(ctx,
					 * result.getSubscriber().getSpid(), startDate,
					 * runningDate); }
					 */
					if (unBilledDays != 0) {
						newRunningDate = CalendarSupportHelper.get(ctx).getDaysAfter(runningDate, unBilledDays);
					}
					newRunningDate = subService.getEndDate();

					//consider change indicator
					if (considerChangeIndicator(ctx,subService.getServiceId())) {
						newRunningDate = applyChangeIndicatorLogic(ctx,newRunningDate);
						ratio = calculateRefundRatio(ctx, crmSpid, result, fee, handler, newRunningDate);
					}else{
						ratio = calculateRefundRatio(ctx, crmSpid, result, fee, handler, runningDateForRefund);
					}					
				}if(LogSupport.isDebugEnabled(parentCtx)){
					LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Exit isRefundAtUnProv condition and ratio is set to:: "+ratio);
				}
            }
            else
            {
                int billCycleDay = SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber());
                
                boolean chargeFull = false;
                
                BooleanHolder quantitySupportFullCharge = (BooleanHolder) ctx.get(SubscribersApiSupport.Quantity_Support_For_Full_Charge, new BooleanHolder(false));
                boolean flagForQuantitySupportFullCharge = quantitySupportFullCharge.isBooleanValue();
                if(LogSupport.isDebugEnabled(parentCtx)){
					LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, " value for quantitySupportFullCharge:: "+flagForQuantitySupportFullCharge);
				}
                if(flagForQuantitySupportFullCharge){
                	chargeFull = ActivationFeeModeEnum.FULL.equals(service.getActivationFee()) && SubscriberChargingSupport.isActivationForServiceQuantity(ctx, result);
                }else{
                	chargeFull = ActivationFeeModeEnum.FULL.equals(service.getActivationFee()) && SubscriberChargingSupport.isActivation(ctx, result);
                }
				if (LogSupport.isDebugEnabled(parentCtx)) {
					LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING,"value of Charge Full:: "+chargeFull);
				}

				Date startDate = subService.getStartDate();
                
                if (result.getSubscriber().isPrepaid())
                {
                    startDate = runningDate;
                }
                
                if (chargeFull)
                {
                    // Setting transaction date to the past if it's a predated service.
                    if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                    {
                        transactionStartDate = startDate;
                    }

                    startDate = handler.calculateCycleStartDate(ctx, startDate, billCycleDay, result.getSubscriber().getSpid(), result.getSubscriber().getId(), fee);
                }
                // Don't predate if the service already existed.
                else if (result.getSubscriber().isPostpaid() && result.getOldSubscriber()!=null)
                {
                    for (SubscriberServices provisionedService : result.getOldSubscriber().getProvisionedServicesBackup(ctx).values())
                    {
                        if (provisionedService.getServiceId() == service.getID())
                        {
                            startDate = runningDate;
                            break;
                        }
                    }
                    
                    // Migrating to trunk from 9_9_tcb  BSS-2470
                    // iterate through suspended services to set up the appropriate start date
                    for (SubscriberServices suspendedService : result.getOldSubscriber().getSuspendedStateServicesBackup(ctx).values())
                    {
                        if (suspendedService.getServiceId() == service.getID())
                        {
                            startDate = runningDate;
                            transactionStartDate = runningDate;
                            if(LogSupport.isDebugEnabled(ctx))
                            {
                                String msg = MessageFormat.format(
                                    "Initialized startDate and transactionDate to {0} for Service {1}, for subscriber {2}", 
                                    runningDate, Long.valueOf(service.getIdentifier()), result.getSubscriber().getId());
                                LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, msg);
                            }
                            break;
                        }
                    }
                    
                    if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                    {
                        transactionStartDate = startDate;
                    }
                }
                else if (result.getSubscriber().isPostpaid())
                {
                    if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                    {
                        transactionStartDate = startDate;
                    }
                }
                
                ratio = handler.calculateRate(ctx, startDate, runningDate, 
                            billCycleDay, 
                            result.getSubscriber().getSpid(), 
                            result.getSubscriber().getId(), fee);
                if (LogSupport.isDebugEnabled(parentCtx)) {
                	   LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "start Date:: "+startDate+" runningDate:: "+runningDate
                			   +" billCycleDay:: "+billCycleDay+" ratio:: "+ratio+" fee:: "+fee);
                }
                
            }         
            
            Transaction trans = null;
            
            if(ServiceTypeEnum.CALLING_GROUP.equals(service.getType()))
            {
            	boolean aggPPCugToOwner = false;
            	ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, result.getSubscriber().getRootAccount(ctx).getBAN());
            	if(cug != null)
            	{
            		aggPPCugToOwner = cug.getAuxiliaryService(ctx).getAggPPServiceChargesToCUGOwner();
            		if(aggPPCugToOwner && !cug.getOwner(ctx).equals(result.getSubscriber()))
            		{
            		    BooleanHolder alreadyPresent = (BooleanHolder)ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR);
                        if(alreadyPresent !=null)
                        {
                            alreadyPresent.setBooleanValue(true);
                        }
                        else
                        {
                            ctx.put(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(true));
                        }
            			final BigDecimal amount = SubscriberChargingSupport.getAmount( 
            					result.action, fee.getFee(), ratio); 
            			AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
            	                AdjustmentTypeEnum.CUGPriceplanServiceCharges);
            			
            			trans = SubscriberChargingSupport.createCUGOwnerTransaction(ctx, type.getCode(), 
            					amount, cug.getOwner(ctx), result.getSubscriber().getId());
            			result.setSubscriber(cug.getOwner(ctx));
            		}
            		else
            		{
            			final BigDecimal amount = SubscriberChargingSupport.getAmount( 
                                result.action,fee.getFee(), ratio); 
                      
                        trans = SubscriberChargingSupport.createTransaction(ctx, service.getAdjustmentType(), 
                        		amount, result.getSubscriber());
            		}
            	}
            	//CUG Owner price plan change scenario in which CUG will be NULL for the select CUG PP service
            	else if(cug == null)
            	{
            		final BigDecimal amount = SubscriberChargingSupport.getAmount( 
        					result.action, fee.getFee(), ratio); 
        			AdjustmentType type = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx,
        	                AdjustmentTypeEnum.CUGPriceplanServiceCharges);
        			
        			trans = SubscriberChargingSupport.createTransaction(ctx, type.getCode(), 
                    		amount, result.getSubscriber());
            	}
            }
            else 
            {
            	long serviceFee = fee.getFee();  
            	
            	if (subService.isPersonalizedFeeSet()) {
					serviceFee = subService.getPersonalizedFee();
					if(serviceFee == -1)
					{
						serviceFee = fee.getFee();
					}
				}
            	if (LogSupport.isDebugEnabled(ctx)) {
					LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Creating transaction with fee : " + serviceFee+" quantity:: "+quantityToChargeFor+
							" and condition of isPersonalizedFeeSet()"+subService.isPersonalizedFeeSet()+" ratio:: "+ratio);
				}
            	BigDecimal amount = new BigDecimal(0);            	
            	if (SubscriberChargingSupport.isRefund(result.action)
						&& (subService.getEndDate().before(CalendarSupportHelper.get(ctx).getStartOfMonth(
								new Date())))) {
					if (null != subService.getNextRecurringChargeDate()) {

						double feeValue = fee.getFee() * quantityToChargeFor;
						long newFeeValue = (long) feeValue;
						amount = SubscriberChargingSupport.getAmount(result.action, newFeeValue, ratio);
						if (LogSupport.isDebugEnabled(ctx)) {
							LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "isRefund TRUE. amount value for subscriber : "+subService.getSubscriberId()+" and service: " +
								" "+subService.getServiceId()+" is : "+amount);
						}
					}
            	}else {
					double feeValue = serviceFee * quantityToChargeFor;
					serviceFee = (long) feeValue;
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Updated Service fee for service : "+ subService.getServiceId() + " is " + serviceFee);
					}
					amount = SubscriberChargingSupport.getAmount(result.action, serviceFee, ratio);
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "isRefund false. Amount value for subscriber : "+subService.getSubscriberId()+" and service: " +
							" "+subService.getServiceId()+" is : "+amount);
					}

				}
            	if (SubscriberChargingSupport.isRefundAtUnProv(result.action)
						|| SubscriberChargingSupport.isRefund(result.action)) {
            		if (crmSpid != null && crmSpid.getCalculateRefundBasedOn() == RefundCalcBasisEnum.USAGE_DAYS &&
							 (fee.getServicePeriod().equals(ServicePeriodEnum.MONTHLY) || fee.getServicePeriod()
									.equals(ServicePeriodEnum.MULTIMONTHLY)))
            		{					
            			double feeValue = serviceFee * quantityToChargeFor;
						long serviceFeeAtProv = (long) feeValue;
						// since it is refund, usage ratio is negative
						double usageAmount = amount.doubleValue() * -1.0;
						// Fix for ITSC-4568
						double refundAmount = 0.0;
						if (serviceFeeAtProv < 0) {
							// truncate the usage amount using floor and convert
							// double to long
							refundAmount = serviceFeeAtProv - Math.round(Math.ceil(usageAmount));
						} else {
							// truncate the usage amount using floor and convert
							// double to long
							refundAmount = serviceFeeAtProv - Math.round(Math.floor(usageAmount));
						}

						// since refund amount has to be negative
						amount = BigDecimal.valueOf(refundAmount * -1.0);
						
					}
            	}
            	
            	if (LogSupport.isDebugEnabled(ctx)) {
            		LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Amount value for subscriber : "+subService.getSubscriberId()+" for service: " +
						" "+subService.getServiceId()+" and amount is :: "+amount);
            	}
              
                trans = SubscriberChargingSupport.createTransaction(ctx, service.getAdjustmentType(), 
                		amount, result.getSubscriber());
            }
            if(LogSupport.isDebugEnabled(ctx))
            {
                String msg = MessageFormat.format(
                    "Restrict Provisioning {0} for Service {1}, for subscriber {2}", 
                        new Object[]{service.getRestrictProvisioning()? "ON" : "OFF",
                                Long.valueOf(service.getIdentifier()), result.getSubscriber().getId()});
                LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, msg);
            }
            
            trans.setAllowCreditLimitOverride(fee.getServicePreference()==ServicePreferenceEnum.MANDATORY || !service.getRestrictProvisioning());
            
            /**
             * TT#13042934033 fixed. For MultiDay period handler, we want to calculate SericeEndDate from current date instead of CycleStart date.
             */
            if(handler instanceof MultiDayPeriodHandler)
            {
            	ctx.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
            }
            trans.setServiceEndDate(handler.calculateCycleEndDate(ctx, runningDate, 
                    SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), 
                    result.getSubscriber().getSpid(), 
                    result.getSubscriber().getId(), fee));
            
            
            if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME) ) 
            {
                // changed for Dory, but supposed to be accepted by all customers. 
               trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
            } else
            {
                trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
            }
            
            trans.setRatio(ratio);
            trans.setFullCharge(fee.getFee());
            trans.setServiceQuantity(quantityVal);
            trans.setCatalogFee(fee.getFee());
            
            // Setting the transaction date to the past.
            if (transactionStartDate != null)
            {
                trans.setTransDate(transactionStartDate);
            }
            
            result.setTrans(trans);
        }         
        catch ( Throwable t )
        {
             ret = TRANSACTION_FAIL_UNKNOWN; 
             result.thrownObject = t;
        }   
       
        result.setChargeResult(ret);
        if (LogSupport.isDebugEnabled(ctx)) {
        	LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Ending ServiceChargingSupport initializedTransaction");
        }
    }	
    
    private static  ChargableItemResult handleServiceTransaction(
            final Context ctx, 
            final ChargableItemResult result,            
            final TransactionCustomize transCustomizer 
    )
    {    
         
        int ret = TRANSACTION_SUCCESS; 
        try 
        {
            final Service service = (Service) result.getChargableObjectRef();
            final ServiceFee2 fee = (ServiceFee2) result.getChargableObject();
             
            if (transCustomizer != null)
            {	   
         	   result.setTrans(transCustomizer.customize(ctx, result.getTrans())); 
            }
            
            SecondaryBalanceChargeOptionSupport.setSecondaryBalanceChargeOption(ctx, result.getSubscriber());
            result.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, result.getTrans()));
             
        }catch (OcgTransactionException e )
        {
            ret = TRANSACTION_FAIL_OCG;  
            result.thrownObject = e;
         }
        catch ( Throwable t )
        {
             ret = TRANSACTION_FAIL_UNKNOWN; 
             result.thrownObject = t;
        }   
       
        result.setChargeResult(ret); 
        
        return result; 
    }
    
    public static int handleSingleServiceTransactionWithoutRedirecting( 
    		final Context ctx, 
    		final ChargableItemResult ret,
    		final ChargeRefundResultHandler handler,
            final CrmChargingAccumulator accumulator,
            final TransactionCustomize transCustomizer,
            final DuplicationValidator validator     )
    {           
		SubscriberChargingSupport.validateTransaction(ctx, ret, validator); 
		SubscriberChargingSupport.updateTransactionValue(ctx, ret);
		
		if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
		{	
	        handleServiceTransaction(ctx, ret, transCustomizer);   			
		} 

        ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
        
        if ( realHandler != null )
        {    
          realHandler.handleTransaction(ctx, ret);
        }    
        
        if ( accumulator != null)
        {
            SubscriberChargingSupport.accumulate(accumulator, ret);
            
        }
        return ret.getChargeResult();
    }
    
 
    public static void accumulate(final SubscriberChargingAccumulator accumulator, 
            ChargableItemResult ret)
    {
        
        if ( ret.getChargeResult() == TRANSACTION_SUCCESS)
        {
            if ( ret.getAction() != ACTION_PROVISIONING_REFUND )
            {    
                accumulator.addChargedService(ret.getId());
            } 
            else 
            {
                accumulator.addRefundService(ret.getId());
                
            }
         } 
        SubscriberChargingSupport.accumulate(accumulator, ret);        
        
    }
    
    
    
    public static Map<ServiceFee2, SubscriberServices> getProvisionedServices(Context ctx, 
            Collection serviceFees, Collection provisionedService  )
    {
        Map ret = new HashMap();
        for ( Iterator i = provisionedService.iterator(); i.hasNext();)
        {
            SubscriberServices service = (SubscriberServices) i.next();
            Long serviceId = service.getServiceId();
            for (ServiceFee2 fee : (Collection<ServiceFee2>) serviceFees)
            {
                if (fee.getServiceId() == serviceId)
                {
                    ret.put(fee, service);
                }
            }
        }
        return ret;
    }
    
    public static Map<ServiceFee2, Service> getFutureServices(Context ctx, 
            Collection serviceFees, Collection futureService  )
    {
        Map ret = new HashMap();
        for ( Iterator i = futureService.iterator(); i.hasNext();)
        {
            Service service = (Service) i.next();
            Long serviceId = service.getID();
            for (ServiceFee2 fee : (Collection<ServiceFee2>) serviceFees)
            {
                if (fee.getServiceId() == serviceId)
                {
                    ret.put(fee, service);
                }
            }
        }
        return ret;
    }

    public static Service getServiceById(Context ctx, long id)
    {
        try 
        {
            return ServiceSupport.getService(ctx, id);

        }catch (Exception e)
        {
            new MinorLogMsg(ServiceChargingSupport.class, "fail to find service " + id, e).log(ctx);
        }
        
        return null; 
    }

    
    public static int getEventType(Context ctx, 
            Subscriber sub, 
            ServiceFee2 fee, 
            Service service, 
            int action)
    {

        try {
            if ( fee.getSource().startsWith("Package") )
            {
                return TRANSACTION_SKIPPED_IN_PACKAGE; 
            }
 
            if (service == null || ServiceTypeEnum.TRANSFER.equals(service.getType()))
            {
                return TRANSACTION_SKIPPED_UNSUPPORTED_TYPE;
            }
            
            // one time service. 
            if(fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME))
            {
                    return TRANSACTION_REDIRECT_ONE_TIME; 
            }
        } catch (Exception e)
        {
            new MinorLogMsg(ServiceChargingSupport.class, "fail when validating service " +
                    fee.getServiceId() + 
                    " for sub" + sub.getId(), e).log(ctx); 
        }
        return TRANSACTION_SUCCESS; 
    }
        
    public static short getChargingCycleType(ServicePeriodEnum type)
    {
    	switch (type.getIndex())
    	{
    	case ServicePeriodEnum.MONTHLY_INDEX:
    		return CHARGING_CYCLE_MONTHLY;
    	case ServicePeriodEnum.ONE_TIME_INDEX:
    		return CHARGING_CYCLE_ONETIME;
    	case ServicePeriodEnum.WEEKLY_INDEX:
    		return CHARGING_CYCLE_WEEKLY;
    	case ServicePeriodEnum.ANNUAL_INDEX:
    		return CHARGING_CYCLE_ANNUAL;
        case ServicePeriodEnum.MULTIMONTHLY_INDEX:
            return CHARGING_CYCLE_MULTIMONTHLY;
        case ServicePeriodEnum.DAILY_INDEX:
            return CHARGING_CYCLE_DAILY;
        case ServicePeriodEnum.MULTIDAY_INDEX:
            return CHARGING_CYCLE_MULTIDAY;

    	}
    	
    	return CHARGING_CYCLE_CONFIGURABLE; 
    }
    
    static  public SubscriberServices getProvisionedSubscriberService( 
    		final Context ctx, 
    		final Collection <SubscriberServices> originals, 
    		final String subId, 
    		final long srvId)
    {
    	// service state is not taking into account, not sure how the state is set. 
    	// Fix me
    	And and = new And();
    	and.add(new EQ(SubscriberServicesXInfo.SUBSCRIBER_ID, subId)); 
    	and.add(new EQ(SubscriberServicesXInfo.SERVICE_ID, new Long(srvId)));
        return CollectionSupportHelper.get(ctx).findFirst(ctx, originals, 
                  and  ); 
      }

    static private boolean isForceChargingApplicable(final Context ctx, final ChargableItemResult chargeableItemResult)
    {
    	
    	Subscriber oldSubscriber = chargeableItemResult.getOldSubscriber();
    	Subscriber newSubscriber = chargeableItemResult.getSubscriber();
    	Service service = (Service) chargeableItemResult.getChargableObjectRef();
    	
    	if (oldSubscriber == null || newSubscriber == null)
    	{
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Service ID : " + service.getIdentifier() + " is NOT applicable for Force Charging. Either Old or New subscriber is found to be null");
    		}
    		return false;
    	}
    	
    	if (chargeableItemResult.getAction() == ACTION_PROVISIONING_CHARGE && service.isForceCharging() 
    			&& (oldSubscriber.getPricePlan() != newSubscriber.getPricePlan()  
    			|| SubscriberChargingSupport.getChargeType(ctx, newSubscriber, oldSubscriber) == PROVISIONING_CHARGE_ACTION_TYPE_SIMPLE)) 
    	{
    		if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Service ID : " + service.getIdentifier() + 
                		" for Subscriber ID : " + newSubscriber.getId() + " is applicable for Force Charging");
            }
    		return true;
    	}
    	else
    	{
    		if(LogSupport.isDebugEnabled(ctx))
    		{
    			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Service ID : " + service.getIdentifier() + 
            		" for Subscriber ID : " + newSubscriber.getId() + " is NOT applicable for Force Charging");
    		}
    		return false;
    	}
    }
    
    /**
	 * @param ctx
	 * @param sub
	 * @param oldSub
	 * @param action
	 * @param continueState
	 * @param fee
	 * @param subService
	 * @param service
	 * @param ret
	 */
	public static void chargeOrRefund(final Context ctx, final Subscriber sub, final Subscriber oldSub,
			final int action, int continueState, final ServiceFee2 fee, final SubscriberServices subService,
			Service service, ChargableItemResult ret) throws HomeException {
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport chargeOrRefund");
		}
		initializedTransaction(ctx, ret, subService);
		ctx.put(SubscriberServices.class, subService);
		if (ret.getChargeResult() == TRANSACTION_SUCCESS) {
			ret.setChargeResult(getEventType(ctx, sub, fee, service, action));

			if (ret.getChargeResult() == TRANSACTION_SUCCESS) {
				if (ret.getChargeResult() == TRANSACTION_SUCCESS) {

					if (continueState == RUNNING_SUCCESS) {
						if (isForceChargingApplicable(ctx, ret)) {
							// TT#13021413063 - creating another transaction for
							// forced charging will be skipped
							// if it has been applied already during this
							// transaction
							// TT#13021413063 - checks if subscription charging
							// for primary service is already done in
							// CoreTransaction pipeline (during extention of
							// expiry date), if yes skip it
							if (!ctx.has(ServiceChargingSupport.IS_ALREADY_FORCE_CHARGED)) {
								Context subCtx = ctx.createSubContext();
								subCtx.put(ServiceChargingSupport.IS_ALREADY_FORCE_CHARGED, Boolean.TRUE);
								handleServiceTransaction(subCtx, ret, action, sub, oldSub, null, subService, true);
							} else {
								Context subCtx = ctx.createSubContext();
								handleServiceTransaction(subCtx, ret, action, sub, oldSub, null, subService, false);
							}
						} else {
							if (LogSupport.isDebugEnabled(ctx)){
								LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "ret.getChargeResult()  " + ret.getChargeResult());
							}
							SubscriberChargingSupport.validateTransaction(ctx, ret, null);
							SubscriberChargingSupport.updateTransactionValue(ctx, ret);

							if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS) {
								boolean needTransaction = false;
								if (service.isRefundable() || (action != ACTION_PROVISIONING_REFUND)) {
									if (!(!service.isRefundable() && ret.skipValidation && action == ACTION_UNPROVISIONING_REFUND)) {
										needTransaction = true;
									}
								}
								if (LogSupport.isDebugEnabled(ctx)){
									LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "need transaction " + needTransaction);
								}
								handleServiceTransaction(ctx, ret, action, sub, oldSub, null, subService,
										needTransaction);
							} else if (ret.getChargeResult() == TRANSACTION_SKIPPED_DUPLICATE_CHARGE
									|| ret.getChargeResult() == TRANSACTION_SKIPPED_DUPLICATE_REFUND) {
								handleServiceTransaction(ctx, ret, action, sub, oldSub, null, subService, false);
							}
						}

					} else if (continueState == RUNNING_CONTINUE_SUSPEND) {
						ret.setRunningState(RUNNING_CONTINUE_SUSPEND);
						ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND);
					}
				}
			} else if (fee.getServicePeriod().equals(ServicePeriodEnum.ONE_TIME)
					&& SubscriberChargingSupport.isRefund(action)) { // Condition
																		// added
																		// to
																		// fix
																		// TCBI-10104,
																		// TCBI-10558,
																		// TCBI-10622,
																		// TCBI-10623

				LogSupport.info(
						ctx,
						CLASS_NAME_FOR_LOGGING,
						"One Time Service. Action: " + action + " Subscriber:" + sub.getId() + " Service:"
								+ service.getIdentifier());

				// UnsubscribeUnifiedPackage request will be sent to charging
				// only when action is refund and SubscriberService state is
				// Unprovisioned

				SubscriberServices ser = SubscriberServicesSupport.getSubscriberServiceRecord(ctx, sub.getId(),
						subService.getServiceId(), subService.getPath());

				if (LogSupport.isDebugEnabled(ctx) && (ser != null)) {
					LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING,
							"One Time Service. Subscriber:" + sub.getId() + "Service:" + service.getIdentifier()
									+ " subService.getServiceId(): " + subService.getServiceId()
									+ ". ser.getProvisionedState(): " + ser.getProvisionedState());
				}
			}
		}
		if (LogSupport.isDebugEnabled(ctx)){
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Exiting ServiceChargingSupport chargeOrRefund");
		}
	}
    
	
	private static ChargableItemResult handleServiceTransaction(final Context parentCtx,
			final ChargableItemResult result, final int action, final Subscriber newSub, final Subscriber oldSub,
			final TransactionCustomize transCustomizer, SubscriberServices subscriberServices, boolean needTransaction) {
		if (LogSupport.isDebugEnabled(parentCtx)) {
			LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport handleServiceTransaction");
		}
		Context ctx = parentCtx.createSubContext();
		ctx.put(SubscriberServices.class, subscriberServices);

		int ret = TRANSACTION_SUCCESS;

		final Service service = (Service) result.getChargableObjectRef();
		final ServiceFee2 fee = (ServiceFee2) result.getChargableObject();
		try {
			CRMSpid crmSpid = (CRMSpid) ctx.get(CRMSpid.class);
			if (crmSpid == null || crmSpid.getSpid() != newSub.getSpid()) {
				try {
					crmSpid = SpidSupport.getCRMSpid(ctx, newSub.getSpid());
				} catch (HomeException e) {
					// should never happen
					LogSupport.info(ctx, CLASS_NAME_FOR_LOGGING,
							"Exception occured while fetching SPID for subscriber id : " + newSub.getId(), e);
				}
			}

			if (LogSupport.isDebugEnabled(ctx)) {
				LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING,
						"Creating transaction in BSS (with call to OCG) since Service " + service.getID()
								+ " of Subscriber " + newSub.getId() + " is Chargable in BSS.");
			}
			try {
				if (transCustomizer != null) {
					result.setTrans(transCustomizer.customize(ctx, result.getTrans()));
				}
				if (needTransaction) {
					if (LogSupport.isDebugEnabled(ctx)) {
						LogSupport.debug(
								parentCtx,
								CLASS_NAME_FOR_LOGGING,
								"Creating a new transaction :: Provision action :: " + action
										+ " isRefundable flag status in a service configuration :: "
										+ service.isRefundable());
					}
					result.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, result.getTrans()));
				}
			} catch (OcgTransactionException e) {
				ret = TRANSACTION_FAIL_OCG;
				result.thrownObject = e;
			} catch (Throwable t) {
				ret = TRANSACTION_FAIL_UNKNOWN;
				result.thrownObject = t;
			}
			result.setChargeResult(ret);
		} catch (Exception ae) {
				new MinorLogMsg(CLASS_NAME_FOR_LOGGING, "handleServiceTransaction"+ ae.getMessage(), ae).log(ctx);
		}
		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(parentCtx, CLASS_NAME_FOR_LOGGING, "Exiting ServiceChargingSupport handleServiceTransaction");
		}
		return result;
	}
	


public static int applyServicesPersonalizedChargeByIds(final Context ctx,
			final SubscriberServices subscriberServices, final Subscriber sub, final int action,
			final HistoryEventTypeEnum eventType, final Transaction incomingTrans,
			final SubscriberChargingAccumulator accumulator, final int parentContinueState,
			final boolean isChargeProrate, final boolean revertToServiceFee) throws HomeException

	{

		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport applyServicesPersonalizedChargeByIds");
		if (parentContinueState == RUNNING_ERROR_STOP) {
			return parentContinueState;
		}

		// Check service fee : chargeable object Ref is sent NULL Here
		ChargableItemResult result = new ChargableItemResult(action, ChargingConstants.CHARGABLE_ITEM_SERVICE, null,
				subscriberServices.getServiceId(), sub);

		Service service;
		ServiceStateEnum serviceState = null;
		try {
			service = ServiceSupport.getService(ctx, subscriberServices.getServiceId());
		} catch (Exception e) {
			LogSupport.major(ctx, CLASS_NAME_FOR_LOGGING, "Exception occured : ", e);
			return TRANSACTION_FAIL_DATA_ERROR;
		}

		result.setAction(action);
		result.setChargableObjectRef(service);
		result.isActivation = false;

		// INIT TRANS CODE

		result.setChargingCycleType(getChargingCycleType(subscriberServices.getServicePeriod()));
		ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx,
				result.getChargingCycleType());

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Received Personalized fee transaction for service : " + service
					+ " and handler : " + handler);
		}

		long serviceCharge = 0;
		Transaction transToCreate = null;
		double ratio = 0;

		if (ACTION_PROVISIONING_REFUND == action || ACTION_UNPROVISIONING_CHARGE == action) {
			ratio = -1.0d;
			serviceCharge = incomingTrans.getAmount();
			serviceState = ServiceStateEnum.UNPROVISIONED;
			// refund copy transaction
		} else if (ACTION_PROVISIONING_CHARGE == action) {
			serviceCharge = subscriberServices.getPersonalizedFee();
			serviceState = ServiceStateEnum.PROVISIONED;
			int billCycleDay = 0;
			try {
				billCycleDay = SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber());
			} catch (HomeException e1) {
				LogSupport.major(ctx, CLASS_NAME_FOR_LOGGING, "Exception occured : ", e1);
				return TRANSACTION_FAIL_DATA_ERROR;
			}
			List<SubscriberServices> subscriberServicesList = new ArrayList<SubscriberServices>();
			subscriberServicesList.add(subscriberServices);

			Map services = null;
			try {
				if (isChargeProrate) {
					services = getProvisionedServices(ctx,
							PricePlanSupport.getVersion(ctx, sub.getPricePlan(), sub.getPricePlanVersion())
									.getServiceFees(ctx).values(), subscriberServicesList);

					// Check if running date has to be same as start date, as
					// proration has to be calculated as per old
					// cycle and not todays date.
					if (revertToServiceFee) {
						List<ServiceFee2> serviceFee2 = new ArrayList<ServiceFee2>(services.keySet());
						for (Iterator i = serviceFee2.iterator(); i.hasNext();) {
							ServiceFee2 fee2 = (ServiceFee2) i.next();
							if (fee2.getServiceId() == subscriberServices.getServiceId()) {
								serviceCharge = fee2.getFee();
								break;
							}
						}
					}
					/*setSubscriberServiceBillStartDate(ctx, result.getSubscriber().getSpid(), subscriberServices);
					if (action == ACTION_UNPROVISIONING_REFUND || action == ACTION_PROVISIONING_REFUND)
						setSubscriberServiceBillEndDate(ctx, result.getSubscriber().getSpid(), subscriberServices);*/

					Date startDate = new Date();
							
							/*subscriberServices.getBillStartDate();*/
					if (startDate == null) {
						startDate = subscriberServices.getStartDate();
						//subscriberServices.setBillStartDate(startDate);
					}
					ratio = handler.calculateRate(ctx, startDate,
							subscriberServices.getStartDate(), // runningDate,
							billCycleDay, result.getSubscriber().getSpid(), result.getSubscriber().getId(),
							subscriberServices);
				} else {
					ratio = 1.0d;
				}

			} catch (Exception e) {
				LogSupport.major(ctx, CLASS_NAME_FOR_LOGGING, "Exception occured : ", e);
				return TRANSACTION_FAIL_DATA_ERROR;
			}
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Creating transaction for PERSONALIZED FEE : "
					+ subscriberServices.isPersonalizedFeeSet() + " with fee : " + serviceCharge);
		}

		BigDecimal amount = null;
		try {
			amount = SubscriberChargingSupport.getAmount(result.action, serviceCharge, ratio);
		} catch (HomeException e) {
			LogSupport.major(ctx, CLASS_NAME_FOR_LOGGING, "Exception occured : ", e);
			return TRANSACTION_FAIL_DATA_ERROR;
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Received Personalized fee transaction for service : " + service
					+ " Service charge : " + serviceCharge + " calculated amount : " + amount);
		}

		transToCreate = SubscriberChargingSupport.createTransaction(ctx, service.getAdjustmentType(), amount,
				result.getSubscriber());

		// Override date to be same as earlire trans date
		transToCreate.setReceiveDate(incomingTrans.getReceiveDate());
		transToCreate.setServiceEndDate(incomingTrans.getServiceEndDate());
		transToCreate.setServiceRevenueRecognizedDate(incomingTrans.getServiceRevenueRecognizedDate());
		transToCreate.setRatio(ratio);
		transToCreate.setTransDate(incomingTrans.getTransDate());
		transToCreate.setFullCharge(subscriberServices.getPersonalizedFee());
		transToCreate.setReceiveDate(incomingTrans.getReceiveDate());
		//transToCreate.setBillStartDate(incomingTrans.getBillStartDate());
		transToCreate.setTaxAuthority(incomingTrans.getTaxAuthority());
		// TODO Check this
		// transToCreate.setAllowCreditLimitOverride(fee.getServicePreference()==ServicePreferenceEnum.MANDATORY
		// ||
		// !service.getRestrictProvisioning());

		result.setTrans(transToCreate);
		result.setChargeResult(TRANSACTION_SUCCESS);

		Context subCtx = ctx.createSubContext();

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING,
					"Received Personalized fee transaction for subscriber : " + sub.getId() + " old transaction :"
							+ incomingTrans + " creating transaction : " + transToCreate);
		}

		handleServiceTransaction(subCtx, result, action, sub, sub, null, subscriberServices, true);

		SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, sub, eventType, ChargedItemTypeEnum.SERVICE,
				service, serviceState, subscriberServices.getPersonalizedFee(), sub.getId(), result.getTrans()
						.getAmount(), Long.toString(result.getTrans().getReceiptNum()), new Date());

		// INIT TRANS CODE END
		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Exiting ServiceChargingSupport applyServicesPersonalizedChargeByIds");
		return TRANSACTION_SUCCESS;
	
	}

	private static Date applyChangeIndicatorLogic(Context ctx, Date runningDate) {
		return CalendarSupportHelper.get(ctx).getDaysAfter(runningDate, -1);
	}
   
	private static boolean considerChangeIndicator(Context ctx, long serviceId) {
		Map<Long, Boolean> changeIndicatorCtxMap = (Map<Long, Boolean>) ctx.get(SubscribersApiSupport.CHANGE_INDICATOR_MAP);
			if (changeIndicatorCtxMap == null) {
				return false;
			}
			return changeIndicatorCtxMap.get(serviceId);
	}
	
	/**
	 * @param result
	 * @param ctx
	 * @param fee
	 * @param handler
	 * @param runningDate
	 * @param todayWithoutTime
	 * @return
	 * @throws HomeException
	 */
	private static double calculateRefundRatio(Context ctx, CRMSpid spid, final ChargableItemResult result,
			final ServiceFee2 fee, ServicePeriodHandler handler, Date runningDate) throws HomeException {
		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport calculateRefundRatio");
		final double ratio;
		ratio = handler.calculateRefundRate(ctx, runningDate,
					SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber()), result.getSubscriber().getSpid(),
					result.getSubscriber().getId(), fee);
		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Exiting ServiceChargingSupport calculateRefundRatio");
		return ratio;
	}
	
	
    /**
	 * @param ctx
	 * @param result
	 * @param subService
	 * @param handler
	 * @param runningDate
	 * @param startDate
	 * @param todayWithoutTime
	 * @param unBilledDays
	 * @return
	 * @throws HomeException
	 */
	private static double calculateRefundRatioForUsageDays(Context ctx, CRMSpid spid, final ChargableItemResult result,
			SubscriberServices subService, ServicePeriodHandler handler, Date runningDate, Date startDate,
			int unBilledDays) throws HomeException {
		
		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Entering ServiceChargingSupport calculateRefundRatioForUsageDays runningDate "+
				runningDate+" startDate:: "+startDate);
		final double ratio;
		Date defaultDate = CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE, runningDate); 
		Date todayWithNoTime = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());
			ratio = handler.calculateRefundRateBasedOnUsage(ctx, runningDate, SubscriberSupport.getBillCycleDay(ctx,
					result.getSubscriber()), startDate, result.getSubscriber().getSpid(), result.getSubscriber()
					.getId(), subService, unBilledDays);
		LogSupport.debug(ctx, CLASS_NAME_FOR_LOGGING, "Exiting ServiceChargingSupport calculateRefundRatioForUsageDays");
		return ratio;
	}
	
    public static final String IS_ALREADY_FORCE_CHARGED = "IS_ALREADY_FORCE_CHARGED";
}
