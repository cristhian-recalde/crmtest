package com.trilogy.app.crm.subscriber.charge.support;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePackageFeeXInfo;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.ServicePackage;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.service.ServicePeriodHandler;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.CrmChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.SubscriberChargingAccumulator;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.CollectionSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.ServicePeriodSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.xhome.home.OcgTransactionException;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.CoreTransactionSupportHelper;

public abstract class PackageChargingSupport 
implements ChargingConstants
{
 
    public static int transferRefund(
            final Context ctx, 
            final Collection provisionoedPackages,
            final Subscriber newSub,
            final Subscriber oldSub, 
            final ChargeRefundResultHandler handler,
            final SubscriberChargingAccumulator accumulator)
    {
        try
        {
           
            Collection packageFees = getProvisionedPackages(ctx, 
                    oldSub.getRawPricePlanVersion(ctx).getServicePackageVersion().getPackageFees().values(),
                    provisionoedPackages);
            
            return applyPackagesCharge(ctx, packageFees, newSub, false, handler, 
                    ACTION_PROVISIONING_REFUND, accumulator, RUNNING_SUCCESS);
        }catch(HomeException e)
        {
            new MinorLogMsg(ctx, "fail to get PPV for sub" + oldSub.getId() + 
                    " when charging for package", e).log(ctx);
  
        }
 
        return RUNNING_ERROR_STOP; 
     
    }

    public static int applyPackagesChargeByIds(
            final Context ctx, 
            final Collection provisionoedPackages,
            final Subscriber sub,
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

        Collection packageFees;
        int continueState = parentContinueState; 
        try
        {
           
            packageFees = getProvisionedPackages(ctx, 
                    sub.getRawPricePlanVersion(ctx).getServicePackageVersion().getPackageFees().values(),
                    provisionoedPackages);
            
            continueState = applyPackagesCharge(ctx, packageFees, sub, isActivation, handler, 
                    action, accumulator, parentContinueState);
        }catch(HomeException e)
        {
            new MinorLogMsg(ctx, "fail to get PPV for sub" + sub.getId() + 
                    " when charging for package", e).log(ctx);
  
        }
 
        return continueState; 
     
    }
    
    
    public static int applyPackagesCharge(
            final Context ctx, 
            final Collection packageFees,
            final Subscriber sub,
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

        for (Iterator i = packageFees.iterator(); i.hasNext();)
        {
            final ServicePackageFee fee = (ServicePackageFee) i.next();
            ChargableItemResult ret = null; 

            if (isValidChargablePackage(ctx, sub, fee))
            {    
                if ( continueState == RUNNING_SUCCESS)
                {    
                   ret = handlePackageTransaction(ctx, fee, sub, isActivation, action, null);                     
               }  
                else if ( continueState == RUNNING_CONTINUE_SUSPEND)
                {
                     ret  = new ChargableItemResult( action, 
                            ChargingConstants.CHARGABLE_ITEM_PACKAGE,  
                            fee, fee.getPackageId(), sub); 
                     ret.chargeResult =TRANSACTION_SKIPPED_SUSPEND; 
                 }
            } else 
            {
                ret  = new ChargableItemResult(action, 
                        ChargingConstants.CHARGABLE_ITEM_PACKAGE,  
                        fee, fee.getPackageId(), sub); 
                 ret.chargeResult =TRANSACTION_SKIPPED_UNSUPPORTED_TYPE; 

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

        return continueState;
     }

    public static  ChargableItemResult handlePackageTransaction(
            final Context parentCtx, 
            final ServicePackageFee fee, 
            final Subscriber sub, 
            final boolean isActivation, 
            final int action, 
            final CrmChargingAccumulator accumulator)
    {    
        Context ctx = parentCtx.createSubContext();
        ctx.put(Subscriber.class, sub);
        
        final ChargableItemResult chargableItemResult = new ChargableItemResult( action, 
                ChargingConstants.CHARGABLE_ITEM_PACKAGE,  
                fee,fee.getPackageId(), sub);   
        

        int ret = TRANSACTION_SUCCESS; 
        try {
            
            final ServicePackage pack = ServicePackage.findServicePackage(ctx, fee.getPackageId()); 
            chargableItemResult.chargableObjectRef = pack;
            if (pack != null )
            {
                chargableItemResult.setChargingCycleType(getChargingCycleType(fee.getServicePeriod()));
                ServicePeriodHandler handler = SubscriberChargingSupport.getServicePeriodHandler(ctx, chargableItemResult.getChargingCycleType());
                Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
                final double ratio;
              
                
                  if (SubscriberChargingSupport.isRefund(action))
                {
                    ratio = handler.calculateRefundRate(ctx, runningDate, 
                        SubscriberSupport.getBillCycleDay(ctx, sub), 
                        sub.getSpid(), 
                        sub.getId(), fee);
                }
                else
                {
                    ratio = handler.calculateRate(ctx, runningDate, 
                            SubscriberSupport.getBillCycleDay(ctx, sub), 
                            sub.getSpid(), 
                            sub.getId(), fee);
                }
       

                final BigDecimal amount = SubscriberChargingSupport.getAmount( 
                        action, fee.getFee(), ratio); 
                
                         Transaction trans = SubscriberChargingSupport.createTransaction(ctx, 
                        pack.getAdjustmentCode(), amount, sub);
                
                trans.setServiceEndDate(handler.calculateCycleEndDate(ctx, runningDate, 
                        SubscriberSupport.getBillCycleDay(ctx, sub), 
                        sub.getSpid(), 
                        sub.getId(), fee));
                
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
                chargableItemResult.setTrans(trans);
                
                chargableItemResult.trans = trans; 
                if (!SubscriberChargingSupport.isRefund(action))
                {
                    ret = SubscriberChargingSupport.handleTransacton(ctx, chargableItemResult, null);
                }
                else{
                            
                
                    chargableItemResult.setTrans(CoreTransactionSupportHelper.get(ctx).createTransaction(ctx, chargableItemResult.getTrans()));
                }
            }
            else 
            {
                ret = TRANSACTION_FAIL_DATA_ERROR;
            }
        }catch (OcgTransactionException e )
        {
            ret = TRANSACTION_FAIL_OCG;  
         }
        catch ( Throwable t )
        {
            ret = TRANSACTION_FAIL_UNKNOWN;
        } finally 
        {
            if ( accumulator != null )
            {
                SubscriberChargingSupport.accumulate(accumulator,chargableItemResult); 
            }
        }

        chargableItemResult.chargeResult = ret;
        return chargableItemResult; 
    }    

    public static int chargeForPackage(Context ctx, 
            final ServicePackageFee fee, 
            Subscriber sub, 
            ChargeRefundResultHandler handler)
    {
        ChargableItemResult ret = null; 
  
        ret = handlePackageTransaction(ctx, fee, sub, false, 
                       ACTION_PROVISIONING_CHARGE, null);                     
        ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
        
        if ( realHandler != null )
        {    
          realHandler.handleTransaction(ctx, ret);
        }    
        return ret.chargeResult;
    }
    
  

    public static int refundForPackage(Context ctx, 
            final ServicePackageFee fee,
            Subscriber sub, 
            ChargeRefundResultHandler handler)
    {
        ChargableItemResult ret = null; 
        
        ret = handlePackageTransaction(ctx, fee, sub, false, 
                       ACTION_PROVISIONING_REFUND, null);                     
        ChargeRefundResultHandler realHandler = ChargeRefundResultHandlerSupport.getHandler(ctx, handler, ret);
        
        if ( realHandler != null )
        {    
          realHandler.handleTransaction(ctx, ret);
        }    
        return ret.chargeResult;
  
    }
  
    public static void accumulate(final SubscriberChargingAccumulator accumulator, 
            ChargableItemResult ret)
    {
        
        if ( ret.chargeResult == TRANSACTION_SUCCESS)
        {
            if ( ret.action != ACTION_PROVISIONING_REFUND )
            {    
                accumulator.addChargedPackage(ret.id);
            } 
            else 
            {
                accumulator.addRefundPackage(ret.id);
                
            }
         } 
        SubscriberChargingSupport.accumulate(accumulator, ret);        
        
    }
    
    
    public static Collection getProvisionedPackages(Context ctx, 
            Collection packagefees, Collection provisionedPackages  )
    {
        Collection ret = new HashSet();
        for ( Iterator i = provisionedPackages.iterator(); i.hasNext();)
        {
            Long packageId =  (Long) i.next();
           // long a = ((long) i.next()).longValue();

            CollectionSupportHelper.get(ctx).findAll(ctx, packagefees, 
                    new EQ(ServicePackageFeeXInfo.PACKAGE_ID, packageId),
                    ret); 
        }
        
        

        return ret;
    }
    public static Collection getProvisionedServicePackageIds(Context ctx,Subscriber sub) 
    {
        Collection provisionedBundleIdsBackup_ = new HashSet();
        try{
            
            
             provisionedBundleIdsBackup_.addAll(sub.getServicePackageIds(ctx));
        }
        catch(HomeException e)
        {
            
        }
        return provisionedBundleIdsBackup_;
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
    /**
     * this validation is not accurate in case a suspended package is unprovisoned. 
     * the current data structure won't be able to tell us if the package is suspened
     * before it is unprovisioned. However, it won't be a big issue, since the duplication
     * refund transaction will be rejected. 
     * @param ctx
     * @param sub
     * @param fee
     * @return
     */
    private static boolean isValidChargablePackage(Context ctx, Subscriber sub, ServicePackageFee  fee)
    {
        boolean ret = true; 
        return ret; 
    }       
}
