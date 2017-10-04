package com.trilogy.app.crm.subscriber.charge.support;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.trilogy.app.crm.api.rmi.support.SubscribersApiSupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberStateAction;
import com.trilogy.app.crm.bean.SubscriberStateActionHome;
import com.trilogy.app.crm.bean.SubscriberStateActionID;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.service.MultiDayPeriodHandler;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.CrmChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.validator.DefaultTransactionValidator;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.subscriber.subscription.history.SubscriberSubscriptionHistorySupport;
import com.trilogy.app.crm.support.BooleanHolder;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.LongHolder;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public abstract class SubscriberChargingSupport 
implements ChargingConstants
{
 
    /** 
     * create a  transaction 
     * @param rate
     * @param adj
     * @param fee
     * @param agent
     * @return Transaction
     */
    static public Transaction createTransaction( 
            final Context ctx,  
            final int adj, 
            final BigDecimal chargeAmount,
            final Subscriber sub)
    {
 
        return TransactionSupport.createTransaction(ctx, adj, chargeAmount, sub); 
    }
    
    /** 
     * create a  transaction 
     * @param rate
     * @param adj
     * @param fee
     * @param agent
     * @param supportedAgent
     * @return Transaction
     */
    static public Transaction createCUGOwnerTransaction( 
            final Context ctx,  
            final int adj, 
            final BigDecimal chargeAmount,
            final Subscriber sub, String supportedSub)
    {
 
        return TransactionSupport.createCUGOwnerTransaction(ctx, adj, chargeAmount, sub, supportedSub); 
    }

    
    public static void validateTransaction(
            final Context ctx,
            final ChargableItemResult result, 
            DuplicationValidator validator)        
    {
        
        if ( validator == null)
        {
        	validator = DefaultTransactionValidator.instance(); 
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
    
    public static void updateTransactionValue (
            final Context ctx,
            ChargableItemResult result)
    {
        try 
        {
	        boolean isRefund = (result.action==ACTION_PROVISIONING_REFUND); 
	        
	        // 0.0 ratio means charging handler doesn't accept refund.
	        boolean shouldPerformUpdate = isRefund && result.getTrans().getRatio()!=0.0 && 
	        		result.getChargeResult() == TRANSACTION_VALIDATION_SUCCESS && result.getItemChargedFee()!=null && result.getTrans().getFullCharge() != result.getItemChargedFee().longValue();
	        
	        BooleanHolder holder = (BooleanHolder) ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
	        if(holder.isBooleanValue() && ( result.getItemChargedFee() == null || result.getItemChargedFee().longValue() == 0))
	        {
	            // hack to avoid updating transaction amount in case of refund on owner msisdn for cug member removal 
	            shouldPerformUpdate = false;
	        }
	        
			if (shouldPerformUpdate) 
			{
				Transaction trans = result.getTrans();
				trans.setFullCharge(result.getItemChargedFee());
	
				final BigDecimal amount = SubscriberChargingSupport.getAmount(
						result.action, trans.getFullCharge(), trans.getRatio());
	
				if (amount.signum() == -1) {
					trans.setAmount(-1
							* (amount.abs().setScale(0, BigDecimal.ROUND_HALF_UP)
									.longValue()));
				} else {
					trans.setAmount(amount.setScale(0, BigDecimal.ROUND_HALF_UP)
							.longValue());
				}
			}
        } 
        catch(Exception e)
        {
        	result.setThrownObject(e); 
        	result.setChargeResult(TRANSACTION_FAIL_UNKNOWN);
        }
    }
 

    
    public static int handleTransacton(
            final Context ctx,
            final ChargableItemResult result, 
            DuplicationValidator validator)        
    throws Exception
    {
        
        if ( validator == null)
        {
        	validator = DefaultTransactionValidator.instance(); 
        }
        
        int ret = validator.validate(ctx, result);
        
        if ( ret == TRANSACTION_VALIDATION_SUCCESS)
        {    
             result.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, result.getTrans()));
             ret = TRANSACTION_SUCCESS; 
         }
            
   
        return ret; 
    }
    
    
    public static Collection getSubscriberProvisionedAuxiliaryServiceIDs(Context ctx, Subscriber sub)
    {
        try {
            return SubscriberAuxiliaryServiceSupport.getSubscriberProvisionedAuxiliaryServiceIDs(ctx, sub);
        } catch (Exception e)
        {
            new MinorLogMsg(SubscriberChargingSupport.class, "fail to get subscriber aux service for sub" + 
                    sub.getId(), e).log(ctx);
        }
        
        return new HashSet(); 
    }
    
    
    public static BigDecimal getAmount(
            final int action, 
            final long amount, 
            final double rate)
    throws HomeException 
    {
        BigDecimal ret = BigDecimal.valueOf(amount); 
        if ( action != ACTION_RECURRING_CHARGE)
        {
             switch (action)
            {
            case ACTION_PROVISIONING_CHARGE: 
            case ACTION_UNSUSPENDING_CHARGE:
            case ACTION_PROVISIONING_REFUND:
                ret =  ret.multiply(BigDecimal.valueOf(rate));
                break;
            case ACTION_UNPROVISIONING_CHARGE:
            case ACTION_UNPROVISIONING_REFUND:
                ret =  ret.multiply(BigDecimal.valueOf(rate)); 
                break;
            }
        }    
        
        return ret; 
    }    
    
    public static int getChargeType(Context ctx, Subscriber newSub, Subscriber oldSub) 
    {
 
        if ( newSub == null )
        {
            return PROVISIONING_CHARGE_ACTION_TYPE_NOT_APPLICABLE; 
        } 
        else if ( oldSub == null )
        {
            return PROVISIONING_CHARGE_ACTION_TYPE_CREATE; 
        }
        else if (oldSub.getState().equals(newSub.getState()))
        {
            if (SubscriberSupport.isSamePricePlanVersion(oldSub, newSub))
            {
                if (SubscriberSupport.isCLCTChange(oldSub, newSub))
                {
                    // No charge / refund action for CLCT.
                    return PROVISIONING_CHARGE_ACTION_TYPE_NOT_APPLICABLE;
                }
                else
                {
                    return PROVISIONING_CHARGE_ACTION_TYPE_SIMPLE;
                }
            }
            else
            {
                return PROVISIONING_CHARGE_ACTION_TYPE_PPV_CHANGE;
            }
        }
        else
        {
            if (SubscriberSupport.isSamePricePlanVersion(oldSub, newSub))
            {
                return PROVISIONING_CHARGE_ACTION_TYPE_STATE_CHANGE;
            }
            else
            {
                return PROVISIONING_CHARGE_ACTION_TYPE_MULTI;
            }
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
            final String subscriberId,
            final boolean isRefund, 
            final Object item,
            final ChargedItemTypeEnum itemType,
            final ServicePeriodEnum servicePeriod,
            final LongHolder itemFee)
    throws Exception
    {
        Context ctx = parentCtx.createSubContext();
        
        ServicePeriodHandler handler = ServicePeriodSupportHelper.get(ctx).getHandler(servicePeriod);

        ctx.put(Subscriber.class, sub);

        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
        Date startDate = handler.calculateCycleStartDate(ctx, runningDate, 
                SubscriberSupport.getBillCycleDay(ctx, sub), 
                sub.getSpid(), 
                sub.getId(), item);
        
        Date runningDateForEndDateCalculation = runningDate;
        /*
         * For performance optimization, dont want calculateCycleEndDate to invoke calculateCycleStartDate again which it does internally 
         * 	if CALCULATE_END_DATE_FROM_CYCLE_START is not set to false 
         */
        Context subContext = ctx.createSubContext();
        if(servicePeriod.equals(ServicePeriodEnum.MULTIDAY))
        {
        	subContext.put(MultiDayPeriodHandler.CALCULATE_END_DATE_FROM_CYCLE_START, false);
        	runningDateForEndDateCalculation = startDate;
        }
        
    	
        Date endDate = handler.calculateCycleEndDate(subContext, runningDateForEndDateCalculation, 
                SubscriberSupport.getBillCycleDay(ctx, sub), 
                sub.getSpid(), 
                sub.getId(), item);
        
        if ( !isRefund)
        {
            return isChargableForCurrentBillingCycle(ctx, subscriberId, item, itemType, startDate, endDate, sub); 
        }
        
        return  isRefundableForCurrentBillingCycle(ctx, subscriberId, item, itemType, startDate, endDate, itemFee); 
    
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
            final Object item,
            final ChargedItemTypeEnum itemType,
            final Date startDate,
            final Date endDate,
            Subscriber sub)
    throws Exception
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        AuxiliaryServiceTypeEnum auxServiceType = null;
        ServiceTypeEnum serviceType = null;
        boolean isChargingApplicable = false;
        int transactionStatus = TRANSACTION_VALIDATION_SUCCESS;
        isChargingApplicable = SubscriberSubscriptionHistorySupport.isChargeable(ctx, subscriberId, itemType, item, runningDate,
                startDate, endDate);
        
        BooleanHolder holder = (BooleanHolder) ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
        boolean isCUGMember = holder.isBooleanValue();
        BooleanHolder quantityHolder = (BooleanHolder) ctx.get(SubscribersApiSupport.IS_SERVICE_QUANTITY_CHANGED, new BooleanHolder(false));
        boolean isQunatityChanged = quantityHolder.isBooleanValue();
        if (item instanceof SubscriberAuxiliaryService)
        {
            auxServiceType = ((SubscriberAuxiliaryService) item).getType(ctx);
            if (!isChargingApplicable && !(auxServiceType.equals(AuxiliaryServiceTypeEnum.CallingGroup) && isCUGMember))
            {
                transactionStatus = TRANSACTION_SKIPPED_DUPLICATE_CHARGE;
            }
        }
        else if (item instanceof ServiceFee2)
        {
            serviceType = ((ServiceFee2) item).getService(ctx).getType();
            if (!isChargingApplicable && !(serviceType.equals(ServiceTypeEnum.CALLING_GROUP) && isCUGMember))
            {
                transactionStatus = TRANSACTION_SKIPPED_DUPLICATE_CHARGE;
            }
        }
        else if (item instanceof SubscriberServices)
        {
            serviceType = ((SubscriberServices) item).getService(ctx).getType();
            if (!isChargingApplicable && !(serviceType.equals(ServiceTypeEnum.CALLING_GROUP) && isCUGMember) && !isQunatityChanged)
            {
                transactionStatus = TRANSACTION_SKIPPED_DUPLICATE_CHARGE;
            }
        }
        else if (item instanceof BundleFee)
        {
            if (!isChargingApplicable)
            {
            	
            	transactionStatus = TRANSACTION_SKIPPED_DUPLICATE_CHARGE;
                                
                AuxiliaryService auxSrv = SubscriberAuxiliaryServiceSupport
                        .findSubscriberProvisionedAuxiliaryServicesByType(ctx, sub,
                                AuxiliaryServiceTypeEnum.CallingGroup);
                if (auxSrv != null)
                {
                    if (auxSrv.getAggPPServiceChargesToCUGOwner() && isCUGMember)
                    {
                        transactionStatus = TRANSACTION_VALIDATION_SUCCESS;
                    }
                }
            }
        }
        else if(!isChargingApplicable)
        {
            transactionStatus = TRANSACTION_VALIDATION_SUCCESS;
        }
        return transactionStatus;
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
            final Object item,
            final ChargedItemTypeEnum itemType,
            final Date startDate,
            final Date endDate,
            final LongHolder itemFee)
    throws HomeException 
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        boolean validTransaction = false;
        validTransaction = SubscriberSubscriptionHistorySupport.isChargeable(ctx, subscriberId, itemType, item,
                runningDate, startDate, endDate, itemFee);
        BooleanHolder holder = (BooleanHolder) ctx.get(ClosedUserGroupSupport.SUBSCRIBER_AS_CUGMEMEBR, new BooleanHolder(false));
        boolean isCUGMember = holder.isBooleanValue();
        int transactionStatus = 0;
        boolean isCallingGroup = (((item instanceof SubscriberAuxiliaryService && (((SubscriberAuxiliaryService) item)
                .getType(ctx).equals(AuxiliaryServiceTypeEnum.CallingGroup))))
                || ((item instanceof ServiceFee2 && (((ServiceFee2) item).getService(ctx).getType()
                        .equals(ServiceTypeEnum.CALLING_GROUP))))) && isCUGMember;
        if (isCallingGroup)
        {
            return TRANSACTION_VALIDATION_SUCCESS;
        }
        if (validTransaction)
        {
            transactionStatus = TRANSACTION_SKIPPED_DUPLICATE_REFUND;
        }
        else
        {
            transactionStatus = TRANSACTION_VALIDATION_SUCCESS;
        }
        return transactionStatus;
    }
    
    public static SubscriberStateAction getSubscriberStateAction(Context ctx,  Subscriber newSub, Subscriber oldSub)
    {
        if ( newSub == null || oldSub == null)
        {
            new MajorLogMsg(SubscriberChargingSupport.class, "Can not fetch the subscriber state atction due to one ", null).log(ctx);
            return null; 
        }
        
         Home home = (Home) ctx.get(SubscriberStateActionHome.class);
        try {
            int subType = newSub.getSubscriberType().getIndex();
            return (SubscriberStateAction)home.find(
                    new SubscriberStateActionID(oldSub.getState().getIndex(),  
                            newSub.getState().getIndex(), subType)); 
        } catch (HomeException e)
        {
            new CritLogMsg(SubscriberChargingSupport.class, "system error: can not find state action :", e).log(ctx);
        }
        
        return null; 
    }
    
      
    public static boolean isActivation(Context ctx, Object obj)
    {
        Subscriber newSub = (Subscriber) obj; 
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER);

        return (oldSub == null && newSub.getSubscriberType().equals(SubscriberTypeEnum.POSTPAID)) ||
                ( oldSub != null && oldSub.getState().equals(SubscriberStateEnum.AVAILABLE)&& 
                  newSub.getState().equals(SubscriberStateEnum.ACTIVE));       
        
    }

    
    public static void accumulate(final CrmChargingAccumulator accumulator, 
            ChargableItemResult ret )
    {
        if ( ret.getTrans() == null )
        {
            return; 
        }
        
        if (ret.getAction() != ACTION_PROVISIONING_REFUND )
        {
            accumulator.accumulateToBeChargedAmount(ret.getTrans().getAmount());
            if ( ret.getChargeResult() == TRANSACTION_SUCCESS)
            {
                 accumulator.accumulateChargedAmount(ret.getTrans().getAmount()); 
            } else 
            {
                 accumulator.accumulateFailedChargeAmount(ret.getTrans().getAmount());                 
            }
         } else 
        {
            accumulator.accumulateRefundAmount(ret.getTrans().getAmount()); 
            if ( ret.getChargeResult() == TRANSACTION_SUCCESS)
            {
                accumulator.accumulateRefundAmount(ret.getTrans().getAmount()); 
            } else 
            {
                accumulator.accumulateFailedRefundAmount(ret.getTrans().getAmount());                 
            }
        
        }

    }


    public static boolean isRefund(
            final int action)
    throws HomeException 
    {
        boolean result = false; 
        if ( action != ACTION_RECURRING_CHARGE)
        {
             switch (action)
            {
            case ACTION_PROVISIONING_CHARGE: 
            case ACTION_UNSUSPENDING_CHARGE:
                result = false;
            break; 
            case ACTION_PROVISIONING_REFUND:
                result = true;            
            }
        }    
        
        return result; 
    }

    public static boolean isActivation(Context ctx,
            final ChargableItemResult ret)
    throws HomeException 
    {
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        return ( ret.getAction() == ACTION_PROVISIONING_CHARGE && 
                ret.getSubscriber().isPostpaid() && 
                SubscriberStateEnum.ACTIVE.equals(ret.getSubscriber().getState()) && 
                (oldSub==null || SubscriberStateEnum.PENDING.equals(oldSub.getState()))
           );
    }
    
    public static boolean isActivationForServiceQuantity(Context ctx,
            final ChargableItemResult ret)
    throws HomeException 
    {
        return ( ret.getAction() == ACTION_PROVISIONING_CHARGE && 
                ret.getSubscriber().isPostpaid() && 
                SubscriberStateEnum.ACTIVE.equals(ret.getSubscriber().getState()));
    }
    
    public static boolean isAuxiliaryActivation(Context ctx,
            final ChargableItemResult ret, SubscriberAuxiliaryService service)
    throws HomeException 
    {
        boolean result = false;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSub!=null)
        {
            if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, ret.getSubscriber(), SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.ACTIVE) ||
                    EnumStateSupportHelper.get(ctx).isTransition(oldSub, ret.getSubscriber(), SubscriberStateEnum.PENDING, SubscriberStateEnum.ACTIVE))
            {
                result = true;
            }
            else
            {
                result = true;
                Collection<SubscriberAuxiliaryService> services = oldSub.getAuxiliaryServices(ctx);
                for (SubscriberAuxiliaryService auxService : services)
                {
                    if (auxService.getIdentifier() == service.getIdentifier())
                    {
                        result = false;
                        break;
                    }
                }
            }
        }
        else
        {
            result = true;
        }
        return result;
    }

    public static boolean isAuxiliaryActivation(Context ctx,
            final ChargableItemResult ret, BundleFee bundleFee)
    throws HomeException 
    {          
        boolean result = false;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        if (oldSub!=null)
        {
            if (EnumStateSupportHelper.get(ctx).isTransition(oldSub, ret.getSubscriber(), SubscriberStateEnum.AVAILABLE, SubscriberStateEnum.ACTIVE) ||
                    EnumStateSupportHelper.get(ctx).isTransition(oldSub, ret.getSubscriber(), SubscriberStateEnum.PENDING, SubscriberStateEnum.ACTIVE))
            {
                result = true;
            }
            else
            {
                result = true;
                Collection<Long> bundleIds = oldSub.getBundles().keySet();
                if (bundleIds.contains(Long.valueOf(bundleFee.getId())))
                {
                    result = false;
                }
            }
        }
        else
        {
            result = true;
        }
        return result;
    }

    public static ServicePeriodHandler getServicePeriodHandler(Context ctx, int chargingCycle)
    {
        ServicePeriodEnum servicePeriod = SubscriberChargingSupport.getServicePeriodEnum(chargingCycle);
        return ServicePeriodSupportHelper.get(ctx).getHandler(servicePeriod);
    }


    public static ServicePeriodEnum getServicePeriodEnum(int chargingCycle)
    {
        switch (chargingCycle)
        {
        case CHARGING_CYCLE_WEEKLY:
            return ServicePeriodEnum.WEEKLY;
        case CHARGING_CYCLE_MONTHLY:
            return ServicePeriodEnum.MONTHLY;
        case CHARGING_CYCLE_MULTIMONTHLY:
            return ServicePeriodEnum.MULTIMONTHLY;
        case CHARGING_CYCLE_ONETIME:
            return ServicePeriodEnum.ONE_TIME;
        case CHARGING_CYCLE_ANNUAL:
            return ServicePeriodEnum.ANNUAL;
        case CHARGING_CYCLE_DAILY:
            return ServicePeriodEnum.DAILY;
        case CHARGING_CYCLE_MULTIDAY:
            return ServicePeriodEnum.MULTIDAY;
        default:
            return null;
        }
    }
    
    public static boolean isChargeAtUnProv(
            final int action)
    throws HomeException 
    {
        boolean result = false; 
        if ( action != ACTION_RECURRING_CHARGE)
        {
             switch (action)
            {
            case ACTION_PROVISIONING_CHARGE: 
            case ACTION_UNSUSPENDING_CHARGE:
                result = false;
            break; 
            case ACTION_UNPROVISIONING_CHARGE:
                result = true;
			break;            
            }
        }    
        
        return result; 
    }

    public static boolean isRefundAtUnProv(
            final int action)
    throws HomeException 
    {
        boolean result = false; 
        if ( action != ACTION_RECURRING_CHARGE)
        {
           switch (action)
            {
            case ACTION_UNPROVISIONING_REFUND:
                result = true;
			break;            
            }
        }    
        
        return result; 
    }

}
