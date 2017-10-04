package com.trilogy.app.crm.subscriber.charge;

import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.subscriber.charge.handler.ChargeRefundResultHandler;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.util.snippet.log.Logger;

public class CUGCharger extends AbstractCrmCharger 
{

    public CUGCharger(ClosedUserGroup cug, ClosedUserGroup refundCug)
    {
        this.cug = cug; 
        this.refundCug = refundCug;
    }
    
    public int charge(Context ctx, ChargeRefundResultHandler handler) {
        
    	Collection <String>  msisdns = cug.getNewMsisdns(); 
		for (String msisdn : msisdns)
		{
	       	ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_CHARGE , msisdn); 

	       	if (ret.getChargeResult() == ChargingConstants.TRANSACTION_SUCCESS)
            {
               	AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, null, null, null, null);
               	
               	// Suspending auxiliary service.
               	if (ret.getChargeResult() == TRANSACTION_FAIL_OCG)
               	{
                    SubscriberAuxiliaryService subAuxService = (SubscriberAuxiliaryService) ret.getChargableObjectRef();  
                    try
                    {
                        ret.getSubscriber().insertSuspendedAuxService(ctx, subAuxService);
                    }
                    catch (HomeException e)
                    {
                        Logger.major(ctx, this, "Fail to suspend Auxiliary service " + subAuxService.getAuxiliaryServiceIdentifier()
                                + " for subscriber " + ret.getSubscriber().getId(), e);
                    }
               	}
            }	
          
        }  
        
        return OPERATION_SUCCESS; 
    }

    protected boolean isChargeAndRefundEnabled(Context ctx, AuxiliaryService auxSvc, Subscriber subscriber) throws HomeException
    {
        // Charging or refunding is only allowed when the following two conditions apply:
        // 1) auxiliary service uses smart suspension or subscriber is not in suspended state 
        // 2) owner is not in arrears nor in a non chargeable state.
        return ( (auxSvc.isSmartSuspension() || !SubscriberStateEnum.SUSPENDED.equals(subscriber.getState())) &&
                ( !EnumStateSupportHelper.get(ctx).isOneOfStates(subscriber.getState(), AbstractSubscriberProvisioningCharger.getNonChargeableStates()) )
               );
    }

    public int chargeAndRefund(Context ctx, ChargeRefundResultHandler handler) 
    {
     	refund(ctx, handler);
     	return charge(ctx, handler); 
    }

    public int getChargerType() {
        // TODO Auto-generated method stub
        return CHARGER_TYPE_CUG;
    }

    public int refund(Context ctx, ChargeRefundResultHandler handler) 
    {
       	Collection <String>  msisdns = refundCug.getRemoveddMsisdns(); 
       	
		for (String msisdn : msisdns)
		{
          	ChargableItemResult ret = initializeReturn(ctx, ChargingConstants.ACTION_PROVISIONING_REFUND , msisdn); 

            if (ret.getChargeResult() == ChargingConstants.TRANSACTION_SUCCESS)
            {
        		AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, null, null, null, null);
            }	
            
        }  
        
        return OPERATION_SUCCESS; 
    }

    
    public ChargableItemResult initializeReturn(Context ctx, int action, String msisdn)
    {
    	ChargableItemResult	ret  = new ChargableItemResult(); 
    	ret.setAction(action);
    	ret.setChargableItemType(CHARGABLE_ITEM_AUX_SERVICE);
   		ret.setAction(action); 
		ret.isActivation= false;
     	
    	try 
    	{
 			SubscriberAuxiliaryService subService;
 			
			// can not use auxiliaryservice from cug which could be changed when cug template changed. 
 			AuxiliaryService auxService;
 			if (ChargingConstants.ACTION_PROVISIONING_REFUND == action)
 			{
                ret.setChargableObject(refundCug.getAuxiliaryService(ctx));
                auxService = refundCug.getAuxiliaryService(ctx);
                subService = refundCug.getSubAuxServices().get(msisdn);
 			}
 			else
 			{
 			    ret.setChargableObject(cug.getAuxiliaryService(ctx)); 
                auxService = cug.getAuxiliaryService(ctx);
                subService = cug.getSubAuxServices().get(msisdn);
 			}

			if (subService != null )
			{	
				ret.setChargableObjectRef(subService);        	
				final Subscriber sub= SubscriberSupport.getSubscriber(ctx, subService.getSubscriberIdentifier());
         		ret.setSubscriber(sub);
         		if (ChargingConstants.ACTION_PROVISIONING_REFUND == action)
         		{
	         		if (!SubscriberStateEnum.SUSPENDED.equals(sub.getState()) && !EnumStateSupportHelper.get(ctx).isOneOfStates(sub.getState(), AbstractSubscriberProvisioningCharger.getNonChargeableStates()))
	         		{
	         			if (!auxService.isRefundable())
	         			{
	         				if (LogSupport.isDebugEnabled(ctx))
	                        {
	                            LogSupport.debug(ctx, this, "Refund to be Waived off for Auxiliary Service ID : " + auxService.getIdentifier() + " for Subscriber ID : " + sub.getId() + ", as Is Refundable flag : " + auxService.isRefundable());
	                        }
	         				ret.setChargeResult(TRANSACTION_SKIPPED_NO_REFUND);
	         				return ret;
	         			}
	         		}
         		}
         		if (isChargeAndRefundEnabled(ctx, auxService, sub))
         		{
         		    ret.setChargeResult( TRANSACTION_SUCCESS); 
         		}
         		else
         		{
         		   ret.setChargeResult(TRANSACTION_SKIPPED_SUSPEND);
         		}
        	} else 
        	{
          		ret.setChargeResult( TRANSACTION_SKIPPED_CUG_EXTERNAL_MSISDN); 
        	}
			
    	} catch (HomeException e)
    	{
    		ret.setChargeResult(TRANSACTION_FAIL_UNKNOWN); 
    		ret.thrownObject = e; 
    	}
        
        return ret; 
    }
    
    
    ClosedUserGroup cug; 
    ClosedUserGroup refundCug; 
}
