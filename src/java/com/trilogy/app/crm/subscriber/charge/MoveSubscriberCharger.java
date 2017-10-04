package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bundle.SubscriberBundleSupport;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.TransactionSupport;


public class MoveSubscriberCharger extends AbstractSubscriberProvisioningCharger
{
    public MoveSubscriberCharger(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
        this.subscriber = newSub; 
        this.oldSub = oldSub; 
        ctx.put(Lookup.OLD_FROZEN_SUBSCRIBER, oldSub);
    }

    @Override
    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler)
    {
        return OPERATION_NOT_SUPPORT; 
    }

    @Override
    public int charge(Context ctx, ChargeRefundResultHandler handler)
    {
            this.calculationResult = calculateOnMoveCharge(ctx,  handler);
            
            this.chargeResult = createTransactionsForProvisioning(ctx, handler);           
            
            return getResult();
    }
    
    @Override
    public int refund(Context ctx, ChargeRefundResultHandler handler)
    {
        this.calculationResult = calculateOnMoveRefund(ctx,  handler);
        
        if ( calculationResult != CALCULATION_SKIPPED)
        {
            this.chargeResult = createTransactionsForProvisioning(ctx, handler);           
        }
        
        return getResult();
    }

    public int getChargerType()
    {
        return CHARGER_TYPE_MOVE_SUBSCRIBER;
    }
    
    protected void handleMoveCharge(Context ctx) throws HomeException
    {
        Date runningDate = CalendarSupportHelper.get(ctx).getRunningDate(ctx);
        
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, oldSub.getSpid());
        
        Map<ServiceFee2, SubscriberServices> serviceFees = ServiceChargingSupport.getProvisionedServices(ctx,
                subscriber.getPricePlan(ctx).getServiceFees(ctx).values(), servicesToBeCharged);
        HashSet chargeWaived = new HashSet(); 
        for ( Iterator<ServiceFee2> i = serviceFees.keySet().iterator(); i.hasNext();)
        {
            ServiceFee2 fee = i.next();
            Service service = fee.getService(ctx);
            if ( service == null || service.getChargeScheme().equals(ServicePeriodEnum.ONE_TIME) || 
                  (spid.getCarryOverBalanceOnMove() && 
                     SubscriberSupport.movedInCurrentBillCycle(ctx, oldSub, runningDate, service.getChargeScheme(), fee) &&
                     TransactionSupport.cycleNotRefunded(ctx, oldSub, fee, ChargedItemTypeEnum.SERVICE, service.getChargeScheme(), service.getAdjustmentType(), fee.getFee(), runningDate)
                  )
               )
            {
                    chargeWaived.add(serviceFees.get(fee));
            }
             
        }
        servicesToBeCharged.removeAll(ServiceChargingSupport.getCrossed(ctx, servicesToBeCharged, chargeWaived));  
        
        Collection bundleFees = BundleChargingSupport.getProvisionedBundles(ctx, SubscriberBundleSupport.getSubscribedBundles(ctx, subscriber).values(), 
                bundlesToBeCharged);
                
        chargeWaived.clear();
        for(Iterator i = bundleFees.iterator(); i.hasNext();)
        {
            BundleFee fee = (BundleFee) i.next();
            BundleProfile bundleProfile = null;
            try
            {
                bundleProfile = fee.getBundleProfile(ctx, oldSub.getSpid()); 
            }
            catch (Throwable t)
            {
                new MinorLogMsg(this, "Unable to retrieve bundle profile: " + t.getMessage(), t).log(ctx);
            }
            
            if ( bundleProfile == null || bundleProfile.getRecurrenceScheme().isOneTime() || 
                    (spid.getCarryOverBalanceOnMove() && 
                            SubscriberSupport.movedInCurrentBillCycle(ctx, subscriber, runningDate, fee.getServicePeriod(), fee) &&
                            TransactionSupport.cycleNotRefunded(ctx, subscriber, fee, fee.isAuxiliarySource()?ChargedItemTypeEnum.AUXBUNDLE:ChargedItemTypeEnum.BUNDLE, fee.getServicePeriod(), bundleProfile.getAdjustmentType(), fee.getFee(), runningDate)
                         )
                      )            
            {
                    chargeWaived.add(Long.valueOf(bundleProfile.getBundleId()));
            } 
        }        
        bundlesToBeCharged.removeAll(chargeWaived); 
        
        chargeWaived.clear();

        for ( Iterator i = auxServicesToBeCharged.iterator(); i.hasNext();)
        {
            SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) i.next();
            AuxiliaryService service = subAuxService.getAuxiliaryService(ctx);
            if ( service == null || service.getChargingModeType().equals(ServicePeriodEnum.ONE_TIME))
            {
                i.remove();
            }
        }
        
    }
    
    protected int calculateOnMoveCharge(Context ctx, ChargeRefundResultHandler handler)
    {
        packagesToBeCharged.addAll( subscriber.getProvisionedPackageIdsBackup()); 
        servicesToBeCharged.addAll( subscriber.getProvisionedSubscriberServices(ctx)); 
        bundlesToBeCharged.addAll( BundleChargingSupport.getSubscribedBundles(ctx, subscriber).keySet()); 
        // Add new subscriber provisioned to get new provisioned VPN aux services.
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId())); 
        // Add old subscriber provisioned to get old provisioned aux services, since by now they have not been moved to the new subscriber yet.
        auxServicesToBeCharged.addAll(SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, oldSub.getId())); 
                
        try
        {
            handleMoveCharge(ctx);
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error calculating charging exceptions for move: " + t.getMessage(), t).log(ctx);
            return CALCULATION_WITH_ERROR;
        }
        
        return CALCULATION_SUCCESS;
    }

    protected int calculateOnMoveRefund(Context ctx, ChargeRefundResultHandler handler)
    {
        packagesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedPackageIdsBackup()); 
        servicesToBeRefund.addAll( getOldSubscriber(ctx).getProvisionedServicesBackup(ctx).values()); 
        bundlesToBeRefund.addAll( BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)).keySet()); 
        auxServicesToBeRefund.addAll(getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx)); 

        handleRefundable(ctx);
        
        this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 
        this.handleSuspendedEntity(ctx);
        
        return CALCULATION_SUCCESS;
    }

    /**
     * calculate entities to be charged and refund in case of creation
     * @param ctx
     * @param oldSubscriber
     */
    @Override
    protected int calculateOnCreate(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
    
    /**
     * calculate entities to be charged and refund in case of delta service provisioning
     * @param ctx
     * @param oldSubscriber
     * @param getOldSub(ctx)
     */
    @Override
    protected int calculateOnSimpleProvisionUnprovison(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }

    
    /**
     * calculate entities to be charged and refund in case of price plan or price plan version change
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    @Override
    protected int calculateOnSimplePPVChange(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }

    
    
    /**
     * calculate entities to be charged and refund in case of subscriber state change 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    @Override
    protected int calculateOnSimpleStateChange(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
 
    
    
    /**
     * calculate entities to be charged and refund in case both state and ppv changed. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    @Override
    protected int calculateOnMultipleUpdate(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
 
    
}
