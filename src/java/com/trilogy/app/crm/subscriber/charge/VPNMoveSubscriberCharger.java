package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberAuxiliaryServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class VPNMoveSubscriberCharger extends AbstractSubscriberProvisioningCharger
{
    public VPNMoveSubscriberCharger(Context ctx, Subscriber newSub, Subscriber oldSub)
    {
        this.subscriber = newSub; 
        this.oldSub = oldSub; 
        ctx.put(Lookup.OLD_FROZEN_SUBSCRIBER, oldSub);
        
    }

    public int chargeAndRefund(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return OPERATION_NOT_SUPPORT; 
    }

    public int charge(Context ctx,
            ChargeRefundResultHandler handler)
    {
            this.calculationResult = calculateOnMoveCharge(ctx,  handler);
            
            this.chargeResult = createTransactionsForProvisioning(ctx, handler);           
            
            return getResult();
    }
    
    public int refund(Context ctx,
            ChargeRefundResultHandler handler)
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

        HashSet chargeWaived = new HashSet(); 


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
    
    protected int calculateOnMoveCharge(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        Collection<SubscriberAuxiliaryService> auxServices = SubscriberAuxiliaryServiceSupport.getProvisionedSubscriberAuxiliaryServices(ctx, subscriber.getId());
        Iterator<SubscriberAuxiliaryService> iter = auxServices.iterator();
        while (iter.hasNext())
        {
            SubscriberAuxiliaryService auxService = iter.next();
            try
            {
                if (!auxService.getAuxiliaryService(ctx).getType().equals(AuxiliaryServiceTypeEnum.Vpn))
                {
                    iter.remove();
                }
            }
            catch (Exception e)
            {
                iter.remove();
            }
        }
        auxServicesToBeCharged.addAll(auxServices); 
                
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

    protected int calculateOnMoveRefund(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        Collection<SubscriberAuxiliaryService> auxServices = getOldSubscriber(ctx).getProvisionedAuxiliaryServices(ctx);
        Iterator<SubscriberAuxiliaryService> iter = auxServices.iterator();
        while (iter.hasNext())
        {
            SubscriberAuxiliaryService auxService = iter.next();
            try
            {
                if (!auxService.getAuxiliaryService(ctx).getType().equals(AuxiliaryServiceTypeEnum.Vpn) || !auxService.getAuxiliaryService(ctx).isRefundable())
                {
                    iter.remove();
                    
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this, "Refund to be Waived off for AuxiliaryService ID : " + auxService.getIdentifier() + " for Subscriber ID : " + getOldSubscriber(ctx).getId()
                        		+ ", as either AuxillaryService is not of type VPN OR Is Refundable flag : " + auxService.getAuxiliaryService(ctx).isRefundable());
                    }
                }
            }
            catch (Exception e)
            {
                iter.remove();
            }
        }
        auxServicesToBeRefund.addAll(auxServices); 
        this.handleSuspendedEntity(ctx);
        
        return CALCULATION_SUCCESS;
        
        }

    /**
     * calculate entities to be charged and refund in case of creation
     * @param ctx
     * @param oldSubscriber
     */
    protected int calculateOnCreate(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
    
    /**
     * calculate entities to be charged and refund in case of delta service provisioning
     * @param ctx
     * @param oldSubscriber
     * @param getOldSub(ctx)
     */
    protected int calculateOnSimpleProvisionUnprovison(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }

    
    /**
     * calculate entities to be charged and refund in case of price plan or price plan version change
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected int calculateOnSimplePPVChange(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }

    
    
    /**
     * calculate entities to be charged and refund in case of subscriber state change 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected int calculateOnSimpleStateChange(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
 
    
    
    /**
     * calculate entities to be charged and refund in case both state and ppv changed. 
     * @param ctx
     * @param oldSubscriber
     * @param oldSub
     */
    protected int calculateOnMultipleUpdate(Context ctx, 
            ChargeRefundResultHandler handler)
    {
        return CALCULATION_SKIPPED; 
    }
}
