package com.trilogy.app.crm.subscriber.charge.validator;

import java.math.BigDecimal;

import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.AuxiliaryService;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.SubscriberChargingSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.LongHolder;

public class DefaultAuxiliarServiceTransactionValidator 
implements DuplicationValidator, ChargingConstants
{
	public int validate(Context ctx, ChargableItemResult result)
	throws Exception 
	{
    	AuxiliaryService service = (AuxiliaryService)result.chargableObject; 
    	SubscriberAuxiliaryService subService = (SubscriberAuxiliaryService) result.getChargableObjectRef(); 
   
        boolean isRefund = (result.action==ACTION_PROVISIONING_REFUND); 
        
        int ret = TRANSACTION_VALIDATION_SUCCESS;
        
        // 0.0 ratio means charging handler doesn't accept refund.
        boolean shouldPerformCheck = !isRefund || result.getTrans().getRatio()!=0.0;
        
        if (shouldPerformCheck)
        {
	    	   LongHolder itemFee = new LongHolder(0);
    	       ret = AuxServiceChargingSupport.isTransactionValid(ctx, result.getSubscriber(), result.getChargedSubscriber(), result.getSubscriber().getId(), isRefund,service, subService, itemFee);
    	       // Updating transaction refund value in case it's a refund and the item value has changed since it was charged.
    	       if (isRefund && ret == TRANSACTION_VALIDATION_SUCCESS)
    	       {
        	       result.setItemChargedFee(itemFee.getValue());
    	       }
        }
        else
        {
            ret = TRANSACTION_SKIPPED_NO_REFUND;
        }
    
        return ret; 

	
	}
	
    public static DefaultAuxiliarServiceTransactionValidator instance()
    {
        return INSTANCE;
    }

    private static final DefaultAuxiliarServiceTransactionValidator INSTANCE = new DefaultAuxiliarServiceTransactionValidator();

}
