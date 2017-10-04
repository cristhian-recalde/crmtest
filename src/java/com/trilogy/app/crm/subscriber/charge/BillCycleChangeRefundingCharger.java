package com.trilogy.app.crm.subscriber.charge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.ChargingCycleEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bean.core.custom.AuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.support.Lookup;

/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 9.1
 */
public class BillCycleChangeRefundingCharger extends AbstractSubscriberProvisioningCharger
{
    public BillCycleChangeRefundingCharger(Context ctx, Subscriber subscription)
    {
        this.subscriber = subscription; 
        this.oldSub = subscription; 
    }

    public int getChargerType()
    {
        return CHARGER_TYPE_CHANGE_BILL_CYCLE;
    }


    @Override
    public int refund(Context ctx,
            ChargeRefundResultHandler handler)
    {
        Context sCtx = ctx.createSubContext();
        sCtx.put(Lookup.OLD_FROZEN_SUBSCRIBER, oldSub);
        
        this.calculationResult = calculateOnBillCycleChangeRefund(sCtx,  handler);
        
        if ( calculationResult != CALCULATION_SKIPPED)
        {
            this.chargeResult = createTransactionsForProvisioning(sCtx, handler);           
        }
        
        return getResult();
    }


    private int calculateOnBillCycleChangeRefund(Context ctx, ChargeRefundResultHandler handler)
    { 
        Map<Long, SubscriberServices> monthlyServices = new HashMap<Long, SubscriberServices>(getOldSubscriber(ctx).getProvisionedServicesBackup(ctx));
        for (Iterator<Map.Entry<Long, SubscriberServices>> iter = monthlyServices.entrySet().iterator(); iter.hasNext(); )
        {
            Entry<Long, SubscriberServices> entry = iter.next();
            SubscriberServices value = entry.getValue();
            if (value != null)
            {
                ServicePeriodEnum servicePeriod = value.getServicePeriod();
                if (servicePeriod != null)
                {
                    ChargingCycleEnum chargingCycle = servicePeriod.getChargingCycle();
                    if (chargingCycle != null && (!ChargingCycleEnum.MONTHLY.equals(chargingCycle) || !value.getService(ctx).isRefundable()))
                    {
                        iter.remove();
                        
                    	if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Refund to be Waived off for Service ID : " + value.getServiceId() + " for Subscriber ID : " + getOldSubscriber(ctx).getId()
                            				+ ", as either Service is not Monthly OR Is Refundable flag : " + value.getService(ctx).isRefundable());
                        }
                        
                    }
                    
                }
            }
        }

        Map<Long, BundleFee> monthlyBundles = new HashMap<Long, BundleFee>(BundleChargingSupport.getSubscribedBundles(ctx, getOldSubscriber(ctx)));
        for (Iterator<Map.Entry<Long, BundleFee>> iter = monthlyBundles.entrySet().iterator(); iter.hasNext(); )
        {
            Entry<Long, BundleFee> entry = iter.next();
            BundleFee value = entry.getValue();
            Long bundleId = entry.getKey();
            if (value != null)
            {
                ServicePeriodEnum servicePeriod = value.getServicePeriod();
                if (servicePeriod != null)
                {
                    ChargingCycleEnum chargingCycle = servicePeriod.getChargingCycle();
                    BundleProfile bundleProfile = BundleChargingSupport.getBundleProfile(ctx, bundleId.longValue());
                    if (chargingCycle != null && (!ChargingCycleEnum.MONTHLY.equals(chargingCycle) || !bundleProfile.isRefundable()))
                    {
                        iter.remove();
                        
                    	if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this, "Refund to be Waived off for Bundle Profile ID : " + bundleId + " for Subscriber ID : " + getOldSubscriber(ctx).getId()
                            				+ ", as either Bundle is not Monthly OR Is Refundable flag : " + bundleProfile.isRefundable());
                        }

                    }
                }
            }
        }

        Collection<SubscriberAuxiliaryService> monthlyAuxServices = new ArrayList<SubscriberAuxiliaryService>(getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx));
        for (Iterator<SubscriberAuxiliaryService> iter = monthlyAuxServices.iterator(); iter.hasNext(); )
        {
            SubscriberAuxiliaryService value = iter.next();
            if (value != null)
            {
                try
                {
                    AuxiliaryService auxSvc = value.getAuxiliaryService(ctx);
                    ServicePeriodEnum servicePeriod = auxSvc.getChargingModeType();
                    if (servicePeriod != null)
                    {
                        ChargingCycleEnum chargingCycle = servicePeriod.getChargingCycle();
                        if (chargingCycle != null && (!ChargingCycleEnum.MONTHLY.equals(chargingCycle) || !auxSvc.isRefundable()))
                        {
                            iter.remove();
                            
                        	if (LogSupport.isDebugEnabled(ctx))
                            {
                                LogSupport.debug(ctx, this, "Refund to be Waived off for AuxiliaryService ID : " + auxSvc.getIdentifier() + " for Subscriber ID : " + getOldSubscriber(ctx).getId()
                                				+ ", as either AuxillaryService is not Monthly OR Is Refundable flag : " + auxSvc.isRefundable());
                            }

                        }
                    }
                }
                catch (HomeException e)
                {
                    new InfoLogMsg(this, "Error retrieving auxiliary service " + value.getAuxiliaryServiceIdentifier() + " for subscription " + value.getSubscriberIdentifier(), e).log(ctx);
                }
            }
        }

        servicesToBeRefund.addAll(monthlyServices.values()); 
        bundlesToBeRefund.addAll(monthlyBundles.keySet()); 
        auxServicesToBeRefund.addAll(monthlyAuxServices); 

        this.setBundleOverUsageCharge(subscriber.getBundleOverUsage()); 
        this.handleSuspendedEntity(ctx);

        return CALCULATION_SUCCESS;

    }


    @Override
    protected int calculateOnCreate(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED;
    }


    @Override
    protected int calculateOnSimpleProvisionUnprovison(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED;
    }


    @Override
    protected int calculateOnSimplePPVChange(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED;
    }


    @Override
    protected int calculateOnSimpleStateChange(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED;
    }


    @Override
    protected int calculateOnMultipleUpdate(Context ctx, ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED;
    }
}