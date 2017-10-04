package com.trilogy.app.crm.subscriber.charge.support;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.bean.service.SubscriberSubscriptionHistory;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.ActivationFeeModeEnum;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.bean.service.HistoryEventTypeEnum;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.extension.auxiliaryservice.core.MultiSimAuxSvcExtension;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.CrmChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.SubscriberChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.customize.TransactionCustomize;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.validator.DefaultAuxiliarServiceTransactionValidator;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.AuxiliaryServiceSupport;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.RecurringRechargeSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;

public abstract class AuxServiceChargingSupport 
implements ChargingConstants
{    

	private static final String LOGGING_CLASS_NAME = AuxServiceChargingSupport.class
			.getName();
	/** 
	 * apply charge or refund to all subscriber auxiliary services. 
	 * @param ctx
	 * @param subscriberAuxServices
	 * @param sub
	 * @param isActivation
	 * @param handler
	 * @param action
	 * @param accumulator
	 * @param parentState
	 * @return
	 */
    public static int applyAuxServicesChargeByIds(
            final Context ctx, 
            final Collection <SubscriberAuxiliaryService> subscriberAuxServices, 
            final Subscriber sub, 
            final Subscriber oldSub,
            final boolean isActivation, 
            final ChargeRefundResultHandler handler,
            final int action, 
            final SubscriberChargingAccumulator accumulator, 
            final int parentState)
    {
        
        if (parentState == RUNNING_ERROR_STOP)
        {
            return parentState;
        }
        
        int continueState = parentState;
        
        final Iterator<SubscriberAuxiliaryService> i = subscriberAuxServices.iterator();
        while (i.hasNext())
        {
            final SubscriberAuxiliaryService subService = i.next(); 
        	
            final AuxiliaryService service; 
            
            try 
            {
            	service = subService.getAuxiliaryService(ctx);
            } catch (Exception e)
            {
            	// should not happen. charge is not the first line for checking data integraty. 
            	continue; 
            }
            
            ChargableItemResult ret = null; 
 
            ret  = new ChargableItemResult(action, 
                    ChargingConstants.CHARGABLE_ITEM_AUX_SERVICE,  
                    service, subService.getAuxiliaryServiceIdentifier(), sub, oldSub); 
            ret.setChargableObjectRef(subService); 
            ret.setAction(action); 
            ret.setChargeResult(getEventType(ctx, sub,oldSub, service, subService));
            ret.isActivation= isActivation; 
              
            if  (ret.getChargeResult() == TRANSACTION_SUCCESS)
            {
                if (continueState == RUNNING_SUCCESS)
                {
                    initializedTransaction(ctx, ret); 

                    if (ret.getChargeResult() == TRANSACTION_SUCCESS)
                    {   
                        validateTransaction(ctx, ret, null); 
                    	SubscriberChargingSupport.updateTransactionValue(ctx, ret);

                        if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
                        {
                            handleAuxServiceTransaction(ctx, ret,  null);
                        }	
                    } 
                }  
                else if ( continueState == RUNNING_CONTINUE_SUSPEND)
                {
                    ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND); 
                    ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
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
            if ( accumulator != null )
            {
                accumulate(accumulator, ret);
            }
        }

        return continueState; 
        
    }


    public static void initializedTransaction(Context parentCtx, final ChargableItemResult result)
    {
        Context ctx = parentCtx.createSubContext();
        
    	AuxiliaryService service = (AuxiliaryService)result.chargableObject; 
        final SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService) result.getChargableObjectRef(); 
        
        result.setChargingCycleType(ServiceChargingSupport.getChargingCycleType(service.getChargingModeType()));

        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
    	int ret = TRANSACTION_SUCCESS; 
        try 
        {
           ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx, result.getChargingCycleType());
 
           final double ratio;
           Date transactionStartDate = null;
           
           final Subscriber subscriber = result.getSubscriber();
           ctx.put(Subscriber.class, subscriber);
           
           if (SubscriberChargingSupport.isRefund(result.action))
           {
               Subscriber chargedSubscriber = result.getChargedSubscriber();
               ratio = handler.calculateRefundRate(ctx, runningDate, 
                   SubscriberSupport.getBillCycleDay(ctx, chargedSubscriber), 
                   subscriber.getSpid(), 
                   subscriber.getId(), subService);
           }
           else
           {
               boolean chargeFull = ActivationFeeModeEnum.FULL.equals(service.getActivationFee()) && SubscriberChargingSupport.isAuxiliaryActivation(ctx, result, subService);
               int billCycleDay = SubscriberSupport.getBillCycleDay(ctx, result.getSubscriber());
               
               Date startDate = subService.getStartDate();

               if (result.getSubscriber().isPrepaid() || service.isPrivateCUG(ctx))
               {
                   startDate = runningDate;
               }
               
               if (chargeFull)
               {
            	   // Migrating to trunk from 9_9_tcb
            	   // BSS-2492. Calculate start Date based on runningDate. Handler uses runningDate to calculate cycle start date but
            	   // for Aux daily Services, daily handler simply returns sent date. By sending subService.startDate it was charging from the day service was provisioned.
            	   
            	   //startDate = handler.calculateCycleStartDate(ctx, subService.getStartDate(), billCycleDay, subscriber.getSpid(), subscriber.getId(), subService);
            	   
            	   startDate = handler.calculateCycleStartDate(ctx, runningDate, billCycleDay, subscriber.getSpid(), subscriber.getId(), subService);
            	   
                   // Setting transaction date to the past if it's a predated service.
                   if (CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(startDate).before(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(runningDate)))
                   {
                       transactionStartDate = startDate;
                   }
                   else
                   {
                	   startDate = runningDate;
                   }
                   
                   if(LogSupport.isDebugEnabled(ctx))
                   {
                       String msg = MessageFormat.format(
                           "AuxServiceChargingSupport : Subscriber {0}  Service {1}, startDate {2}, transactionStartDate {2}", 
                           subscriber.getId(), Long.valueOf(service.getIdentifier()), startDate, transactionStartDate);
                       LogSupport.debug(ctx, AuxServiceChargingSupport.class.getName(), msg);
                   }
               }
               else if (result.getSubscriber().isPostpaid() && result.getOldSubscriber()!=null)
               {
                   for (SubscriberAuxiliaryService provisionedAuxService : result.getOldSubscriber().getProvisionedAuxiliaryServices(ctx))
                   {
                       if (provisionedAuxService.getIdentifier() == subService.getIdentifier())
                       {
                           startDate = runningDate;
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
                       subscriber.getSpid(), 
                       subscriber.getId(), subService);
           }
           
           final BigDecimal amount = SubscriberChargingSupport.getAmount( 
                   result.action, service.getCharge(), ratio); 
           
           Transaction trans = SubscriberChargingSupport.createTransaction(ctx, service.getAdjustmentType(), amount, subscriber);
           
           
           if(LogSupport.isDebugEnabled(ctx))
           {
               String msg = MessageFormat.format(
                   "Restrict Provisioning {0} for Service {1}, for subscriber {2}", 
                       new Object[]{service.getRestrictProvisioning()? "ON" : "OFF",
                               Long.valueOf(service.getIdentifier()), subscriber.getId()});
               LogSupport.debug(ctx, AuxServiceChargingSupport.class.getName(), msg);
           }
           
           trans.setAllowCreditLimitOverride(!service.getRestrictProvisioning());
           trans.setServiceEndDate(           
                   handler.calculateCycleEndDate(ctx, runningDate, 
                   SubscriberSupport.getBillCycleDay(ctx, subscriber), 
                   subscriber.getSpid(), 
                   subscriber.getId(), subService));
           
           if (service.getChargingModeType().equals(ServicePeriodEnum.ONE_TIME) ) 
           {
               // changed for Dory, but supposed to be accepted by all customers. 
              trans.setServiceRevenueRecognizedDate(trans.getTransDate()); 
           } else
           {
               trans.setServiceRevenueRecognizedDate(trans.getServiceEndDate());    
           }
           
           trans.setRatio(ratio);
           trans.setFullCharge(service.getCharge());
           
           if (service.getType() == AuxiliaryServiceTypeEnum.MultiSIM)
           {
               trans.setCSRInput(subService.getMultiSimImsi());
           }
           
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
	}
    /**
     * The real method that will create individual transaction. 
     * validator is for duplication checking. 
     * customizer is for customize for special transactions 
     * 
     * @param ctx
     * @param result
     * @param accumulator
     * @return
     */
    private static ChargableItemResult handleAuxServiceTransaction(
            Context ctx, 
            final ChargableItemResult result,            
            final TransactionCustomize transCustomizer
            )
    {    
    	AuxiliaryService service = (AuxiliaryService)result.chargableObject; 
    	ctx = ctx.createSubContext();
    	ctx.put(RecurringRechargeSupport.RECURRING_RECHARGE_CHARGED_ITEM, (SubscriberAuxiliaryService) result.getChargableObjectRef());
    	int ret = TRANSACTION_SUCCESS; 
        try 
        {
           
           if (transCustomizer != null)
           {	   
        	   result.setTrans(transCustomizer.customize(ctx, result.getTrans())); 
           }
           
           if(result.getTrans().getSubscriberID().equals(result.getTrans().getSupportedSubscriberID()) && service.getAggPPServiceChargesToCUGOwner())
           {
        	   ret = TRANSACTION_FAIL_UNKNOWN;
           }
           else
           {
               SecondaryBalanceChargeOptionSupport.setSecondaryBalanceChargeOption(ctx, result.getSubscriber());
        	   result.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, result.getTrans()));
               
               ret = TRANSACTION_SUCCESS;
           }
        }
        catch (OcgTransactionException e )
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
    
    
    
    public static int handleSingleAuxServiceTransactionWithoutRedirecting(final Context ctx, 
    		final ChargableItemResult ret,
            final ChargeRefundResultHandler handler, 
            final CrmChargingAccumulator accumulator,
            final TransactionCustomize transCustomizer,
            final DuplicationValidator validator

          	)
    {
        initializedTransaction(ctx, ret); 

        if (ret.getChargeResult() == TRANSACTION_SUCCESS)
        {   
            validateTransaction(ctx, ret, validator); 
        	SubscriberChargingSupport.updateTransactionValue(ctx, ret);
			
    		if (ret.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS)
    		{	
			    handleAuxServiceTransaction(ctx, ret,transCustomizer);  
			}				
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
                accumulator.addChargedAuxiliaryService(ret.getId());
            } 
            else 
            {
                accumulator.addRefundAuxiliaryService(ret.getId());
                
            }
         } 
        SubscriberChargingSupport.accumulate(accumulator, ret);        
        
    }
    
    
    protected static int getEventType(Context ctx, Subscriber sub,Subscriber oldSub, AuxiliaryService  service, SubscriberAuxiliaryService subService)
    {
        if (AuxiliaryServiceTypeEnum.MultiSIM.equals(service.getType()))
        {
            boolean isChargePerSim = MultiSimAuxSvcExtension.DEFAULT_CHARGEPERSIM;
            MultiSimAuxSvcExtension multiSimAuxSvcExtension = ExtensionSupportHelper.get(ctx).getExtension(ctx, service, MultiSimAuxSvcExtension.class);
            if (multiSimAuxSvcExtension != null)
            {
                isChargePerSim = multiSimAuxSvcExtension.isChargePerSim();
            }
            else
            {
                LogSupport.minor(ctx, AuxServiceChargingSupport.class,
                        "Unable to find required extension of type '" + MultiSimAuxSvcExtension.class.getSimpleName()
                                + "' for auxiliary service " + service.getIdentifier());
            }

            if (subService.getSecondaryIdentifier() == SubscriberAuxiliaryService.DEFAULT_SECONDARYIDENTIFIER)
            {
                if (isChargePerSim)
                {
                    // In this case, we are not charging for the service.
                    // We are instead charging for each SIM in the service (i.e. the records with a secondary identifier)
                    return TRANSACTION_SKIPPED_MULTISIM_PER_SIM;
                }
            }
            else if (!isChargePerSim)
            {
                // We are not charging for the individual SIM in the service.
                // We are charging for the service itself (i.e. the record with no secondary identifier)
                return TRANSACTION_SKIPPED_MULTISIM_PER_SERVICE;
            }
        }
       
        if (service.isPrivateCUG(ctx))
        {
        	if(sub != null && oldSub !=null 
        			&& SubscriberStateEnum.AVAILABLE.equals(oldSub.getState()) 
        			&& SubscriberStateEnum.ACTIVE.equals(sub.getState()))
        	{
        		return TRANSACTION_SUCCESS;
        	}
        	else
        	{
        		return TRANSACTION_REDIRECT_PCUG; 
        	}
        }
        
        if (AuxiliaryServiceTypeEnum.Vpn.equals(service.getType()))
        {
        	return TRANSACTION_REDIRECT_VPN_GROUP; 
        }
        
        if (ServicePeriodEnum.ONE_TIME.equals(service.getChargingModeType()))
        {
        	return TRANSACTION_REDIRECT_ONE_TIME; 
        }

        
        return TRANSACTION_SUCCESS; 
    }
    
    
    public static AuxiliaryService getAuxiliaryServicById(Context ctx, long id)
    {
        try 
        {
            return AuxiliaryServiceSupport.getAuxiliaryServicById(ctx, id);

        }catch (Exception e)
        {
            new MinorLogMsg(AuxServiceChargingSupport.class, "fail to find aux service " + id, e).log(ctx);
        }
        
        return null; 
      
    }

    
    
    /**
     * Auxiliary service duplication checking is special for now. it can not use the one in SubscriberChargingSupport. 
     * 
     * @param ctx
     * @param sub
     * @param trans
     * @param action
     * @return
     * @throws Exception
     */
    public static void validateTransaction(
            final Context ctx,
            ChargableItemResult result, 
            DuplicationValidator validator)
    {
        if ( validator == null)
        {
        	validator = DefaultAuxiliarServiceTransactionValidator.instance(); 
        }
        
        try 
        {
        	result.setChargeResult(validator.validate(ctx, result));
        } catch(Exception e)
        {
        	result.setThrownObject(e); 
        	result.setChargeResult(TRANSACTION_FAIL_UNKNOWN);
        }

    }
 
    /**
     * fix me, could need change the method signature in order to use subscribersubscriptionhistory
     * @param ctx
     * @param sub
     * @param subscriberId
     * @param adjustmentType
     * @param isRefund
     * @param newCharge
     * @return
     */
    public static int isTransactionValid(
            final Context parentCtx, 
            final Subscriber sub,
            final Subscriber chargedSub, 
            final String subscriberId,
            final boolean isRefund, 
            final AuxiliaryService service,
            final SubscriberAuxiliaryService subService,
    		final LongHolder itemFee)
    throws Exception
    {
        Context ctx = parentCtx.createSubContext();
        
        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(service.getChargingModeType());

        ctx.put(Subscriber.class, sub);

        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
        Date startDate = handler.calculateCycleStartDate(ctx, runningDate, 
                SubscriberSupport.getBillCycleDay(ctx, chargedSub), 
                sub.getSpid(), 
                sub.getId(), subService);
        
        Date runningDateForEndDateCalculation = runningDate;
        /*
         * For performance optimization, dont want calculateCycleEndDate to invoke calculateCycleStartDate again which it does internally 
         * 	if CALCULATE_END_DATE_FROM_CYCLE_START is not set to false 
         */
        Context subContext = ctx.createSubContext();
        if(service.getChargingModeType().equals(ServicePeriodEnum.MULTIDAY))
        {
        	subContext.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
        	runningDateForEndDateCalculation = startDate;
        }
        
        Date endDate = handler.calculateCycleEndDate(subContext, runningDateForEndDateCalculation, 
                SubscriberSupport.getBillCycleDay(ctx, chargedSub), 
                sub.getSpid(), 
                sub.getId(), subService);
        
        if ( !isRefund)
        {
            return isChargableForCurrentBillingCycle(ctx, subscriberId, service, subService, startDate, endDate); 
        }
        
        return  isRefundableForCurrentBillingCycle(ctx, subscriberId, service, subService, startDate, endDate, itemFee); 
    
    }
    
    
    /**
     * not chargable only last transaction in current billing cycle is refund. 
     * fix me
     * @param ctx
     * @param subscriberId
     * @param adjustmentType
     * @return
     */
    private static int isChargableForCurrentBillingCycle(final Context ctx, 
            final String subscriberId, 
            final AuxiliaryService service,
            final SubscriberAuxiliaryService subService,
            final Date startDate,
            final Date endDate)
    throws Exception
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
        BooleanHolder holder = (BooleanHolder) ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
        boolean isCUGMember = holder.isBooleanValue();

   		if( SubscriberSubscriptionHistorySupport.isChargeable(ctx, subscriberId, ChargedItemTypeEnum.AUXSERVICE, subService,
   		     runningDate, startDate, endDate) || (AuxiliaryServiceTypeEnum.CallingGroup.equals(service.getType()) && isCUGMember && service.isPrivateCUG(ctx)))
   		{
   	       return TRANSACTION_VALIDATION_SUCCESS;    		
   		} else 
   		{
   		   return TRANSACTION_SKIPPED_DUPLICATE_CHARGE ; 
   		}

     }
    
    /**
     * refund if last transaction in current billingcycle is charge
     * fix me
     * @param ctx
     * @param subscriberId
     * @param adjustmentType
     * @return
     */
    private static int isRefundableForCurrentBillingCycle(final Context ctx, 
            final String subscriberId, 
            final AuxiliaryService service,
            final SubscriberAuxiliaryService subService,
            final Date startDate,
            final Date endDate,
            final LongHolder itemFee)
    throws HomeException 
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
       
        BooleanHolder holder = (BooleanHolder) ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false)); 
        boolean isCUGMember = holder.isBooleanValue(); 

        // this method sets the item fee it must be get called
        boolean isRefundable = SubscriberSubscriptionHistorySupport.isChargeable(ctx, subscriberId, ChargedItemTypeEnum.AUXSERVICE, subService,
                runningDate, startDate, endDate, itemFee);
        
        if(AuxiliaryServiceTypeEnum.CallingGroup.equals(service.getType()) && isCUGMember && service.isPrivateCUG(ctx))
        {
        	return TRANSACTION_VALIDATION_SUCCESS;
        }
        else if(isRefundable)
   		{
   	       return TRANSACTION_SKIPPED_DUPLICATE_REFUND;    		
   		} else 
   		{
   		   return TRANSACTION_VALIDATION_SUCCESS; 
   		}
    }
    
    
    static  public Collection<SubscriberAuxiliaryService> getCrossed( 
    		final Context ctx, 
    		final Collection <SubscriberAuxiliaryService> originals, 
    		final Collection <SubscriberAuxiliaryService> duplicates)
    {
        Collection <SubscriberAuxiliaryService> ret = new HashSet();
        
        for (SubscriberAuxiliaryService service : duplicates)
        {
            CollectionSupportHelper.get(ctx).findAll(ctx, originals, 
                    new EQ(SubscriberAuxiliaryServiceXInfo.IDENTIFIER, new Long(service.getIdentifier())),
                    ret); 
        }
       
        return ret; 
    	
    }
    
    static  public SubscriberAuxiliaryService getSubscriberAuxiliaryService( 
    		final Context ctx, 
    		final Collection <SubscriberAuxiliaryService> originals, 
    		final long id)
    {
    	SubscriberAuxiliaryService service = CollectionSupportHelper.get(ctx).findFirst(ctx, originals, 
                    new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, new Long(id))); 
    	return service; 
    	
    }
    
    static  public Collection<SubscriberAuxiliaryService> getCrossedByAuxServiceId( 
            final Context ctx, 
            final Collection <SubscriberAuxiliaryService> originals, 
            final Collection <SubscriberAuxiliaryService> duplicates)
    {
        Collection <SubscriberAuxiliaryService> ret = new HashSet();
        
        for (SubscriberAuxiliaryService service : duplicates)
        {
            CollectionSupportHelper.get(ctx).findAll(ctx, originals, 
                    new EQ(SubscriberAuxiliaryServiceXInfo.AUXILIARY_SERVICE_IDENTIFIER, Long.valueOf(service.getAuxiliaryServiceIdentifier())),
                    ret); 
        }
       
        return ret; 
        
    }
    
    public static int applyServicesPersonalizedChargeByIds(final Context ctx,
			final SubscriberAuxiliaryService subscriberAuxiliaryService,
			final Subscriber sub, final int action,
			final HistoryEventTypeEnum eventType,
			final Transaction incomingTrans,
			final SubscriberChargingAccumulator accumulator,
			final int parentContinueState, final boolean isChargeProrate,
			final boolean revertToServiceFee)

	{
		if (parentContinueState == RUNNING_ERROR_STOP) {
			return parentContinueState;
		}

		// Check service fee : chargeable object Ref is sent NULL Here
		ChargableItemResult result = new ChargableItemResult(action,
				ChargingConstants.CHARGABLE_ITEM_SERVICE, null,
				subscriberAuxiliaryService.getAuxiliaryServiceIdentifier(), sub);

		AuxiliaryService service;
		ServiceStateEnum serviceState = null;
		try {
			service = subscriberAuxiliaryService.getAuxiliaryService(ctx);
		} catch (Exception e) {
			LogSupport
					.major(ctx, LOGGING_CLASS_NAME, "Exception occured : ", e);
			return TRANSACTION_FAIL_DATA_ERROR;
		}

		result.setAction(action);
		result.isActivation = false;
		result.setChargableObjectRef(subscriberAuxiliaryService);

		// INIT TRANS CODE

		result.setChargingCycleType(ServiceChargingSupport
				.getChargingCycleType(service.getChargingModeType()));
		ServicePeriodHandler handler = SubscriberChargingSupport
				.getServicePeriodHandler(ctx, result.getChargingCycleType());

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, LOGGING_CLASS_NAME,
					"Received Personalized fee transaction for aux.service : "
							+ service + " and handler : " + handler);
		}

		long serviceCharge = 0;
		Transaction transToCreate = null;
		double ratio = 0;

		if (ACTION_PROVISIONING_REFUND == action
				|| ACTION_UNPROVISIONING_CHARGE == action) {
			ratio = -1.0d;
			serviceState = ServiceStateEnum.UNPROVISIONED;
			serviceCharge = incomingTrans.getAmount();
			// refund copy transaction
		} else if (ACTION_PROVISIONING_CHARGE == action) {
			serviceCharge = subscriberAuxiliaryService.getPersonalizedFee();
			serviceState = ServiceStateEnum.PROVISIONED;
			int billCycleDay = 0;
			try {
				billCycleDay = SubscriberSupport.getBillCycleDay(ctx,
						result.getSubscriber());
			} catch (HomeException e1) {
				LogSupport.major(ctx, LOGGING_CLASS_NAME,
						"Exception occured : ", e1);
				return TRANSACTION_FAIL_DATA_ERROR;
			}
			List<SubscriberAuxiliaryService> SubscriberAuxiliaryServiceList = new ArrayList<SubscriberAuxiliaryService>();
			SubscriberAuxiliaryServiceList.add(subscriberAuxiliaryService);

			try {
				if (isChargeProrate) {
					if (revertToServiceFee) {
						serviceCharge = subscriberAuxiliaryService
								.getAuxiliaryService(ctx).getCharge();
					}

					// Check if running date has to be same as start date, as
					// proration has to be calculated as per old
					// cycle and not todays date.
					/*setSubscriberServiceBillStartDate(ctx, result
							.getSubscriber().getSpid(),
							subscriberAuxiliaryService);*/
					/*if (action == ACTION_UNPROVISIONING_REFUND
							|| action == ACTION_PROVISIONING_REFUND)
						setSubscriberServiceBillEndDate(ctx, result
								.getSubscriber().getSpid(),
								subscriberAuxiliaryService);
					Date startDate = subscriberAuxiliaryService.getBillStartDate();*/
					Date startDate = new Date();
					if (startDate == null) {
						startDate = subscriberAuxiliaryService.getStartDate();
						//subscriberAuxiliaryService.setBillStartDate(startDate);
					}
					ratio = handler.calculateRate(
							ctx,
							startDate,
							subscriberAuxiliaryService.getStartDate(), // runningDate,
							billCycleDay, result.getSubscriber().getSpid(),
							result.getSubscriber().getId(),
							subscriberAuxiliaryService);
				} else {
					ratio = 1.0d;
				}

			} catch (Exception e) {
				LogSupport.major(ctx, LOGGING_CLASS_NAME,
						"Exception occured : ", e);
				return TRANSACTION_FAIL_DATA_ERROR;
			}
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, LOGGING_CLASS_NAME,
					"Creating transaction for PERSONALIZED FEE : "
							+ subscriberAuxiliaryService.isPersonalizedFeeSet()
							+ " with fee : " + serviceCharge);
		}

		BigDecimal amount = null;
		try {
			amount = SubscriberChargingSupport.getAmount(result.action,
					serviceCharge, ratio);
		} catch (HomeException e) {
			LogSupport
					.major(ctx, LOGGING_CLASS_NAME, "Exception occured : ", e);
			return TRANSACTION_FAIL_DATA_ERROR;
		}

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, LOGGING_CLASS_NAME,
					"Received Personalized fee transaction for aux.service : "
							+ service + " Service charge : " + serviceCharge
							+ " calculated amount : " + amount);
		}

		transToCreate = SubscriberChargingSupport.createTransaction(ctx,
				service.getAdjustmentType(), amount, result.getSubscriber());

		// Override date to be same as earlire trans date
		transToCreate.setReceiveDate(incomingTrans.getReceiveDate());
		transToCreate.setServiceEndDate(incomingTrans.getServiceEndDate());
		transToCreate.setServiceRevenueRecognizedDate(incomingTrans.getServiceRevenueRecognizedDate());
		transToCreate.setRatio(ratio);
		transToCreate.setTransDate(incomingTrans.getTransDate());
		transToCreate.setFullCharge(subscriberAuxiliaryService.getPersonalizedFee());
		transToCreate.setReceiveDate(incomingTrans.getReceiveDate());
		//transToCreate.setBillStartDate(incomingTrans.getBillStartDate());
		transToCreate.setTaxAuthority(incomingTrans.getTaxAuthority());
		/*Service associatedService = null;
		try {
			associatedService = ServiceSupport.getService(ctx,
					subscriberAuxiliaryService.getAssociatedServiceId());
            } catch (Exception e) {
			LogSupport
					.info(ctx,
							AuxServiceChargingSupport.class.getName(),
							"Exception occurred while fetching associated service infornmation.",
							e);
		}
		if (associatedService != null) {
			int linkedAdjustment = associatedService.getAdjustmentType();
			transToCreate.setLinkedAdjustmentType(linkedAdjustment);
		}*/

		// TODO Check this
		// transToCreate.setAllowCreditLimitOverride(fee.getServicePreference()==ServicePreferenceEnum.MANDATORY
		// ||
		// !service.getRestrictProvisioning());

		result.setTrans(transToCreate);
		result.setChargeResult(TRANSACTION_SUCCESS);

		Context subCtx = ctx.createSubContext();

		if (LogSupport.isDebugEnabled(ctx)) {
			LogSupport.debug(ctx, LOGGING_CLASS_NAME,
					"Received Personalized fee transaction for subscriber : "
							+ sub.getId() + " old transaction :"
							+ incomingTrans + " creating transaction : "
							+ transToCreate);
		}

		handleAuxServiceTransaction(subCtx, result, null);

		SubscriberSubscriptionHistorySupport.addChargingHistory(ctx, sub,
				eventType, ChargedItemTypeEnum.AUXSERVICE,
				subscriberAuxiliaryService, serviceState,
				subscriberAuxiliaryService.getPersonalizedFee(), sub.getId(),
				result.getTrans().getAmount(),
				Long.toString(result.getTrans().getReceiptNum()), new Date());

		return TRANSACTION_SUCCESS;
	}
   
}