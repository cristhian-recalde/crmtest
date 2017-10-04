package com.trilogy.app.crm.subscriber.charge.validator;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;

public class OneTimeEntityValidator 
implements DuplicationValidator, ChargingConstants
{
	
	public int validate(Context ctx, ChargableItemResult ret)
	throws Exception
	{
		if (ret.getAction() == ACTION_PROVISIONING_REFUND)
		{
			return TRANSACTION_SKIPPED_ONE_TIME; 

		}
		
		if (isChargable(ctx, ret))
		{
			return TRANSACTION_VALIDATION_SUCCESS; 
		}
		else 
		{
			return TRANSACTION_SKIPPED_ONE_TIME; 
		}
	}
	
	
	   public boolean isChargable(Context ctx, ChargableItemResult ret)
	    {
	    	Subscriber oldSub = (Subscriber)ctx.get(Lookup.OLD_FROZEN_SUBSCRIBER); 
	    	
	    	if (oldSub == null || SubscriberStateEnum.AVAILABLE.equals(oldSub.getState())) 
	    	{
	    		// in case subscriber creation/activation or unsuspension due to credition transaction made. 
	    		return true; 
	    	}
	    	
	    	switch ( ret.chargableItemType)
	    	{
	    	case CHARGABLE_ITEM_SERVICE:
	    		return	!oldSub.getProvisionedServicesBackup(ctx).containsKey(Long.valueOf(ret.getId())); 
	    	case CHARGABLE_ITEM_BUNDLE: 
	       		return  !oldSub.getBundles().keySet().contains(new Long(ret.getId())); 

	    	case CHARGABLE_ITEM_AUX_SERVICE:  	    		
	       		return AuxServiceChargingSupport.getSubscriberAuxiliaryService(ctx, 
	       				oldSub.getProvisionedAuxiliaryServices(ctx), ret.getId())==null; 

	    	case CHARGABLE_ITEM_PACKAGE: 
	       		return false; 

	    	}
	    	return false;
	    }
}
