package com.trilogy.app.crm.subscriber.charge.validator;

import java.math.BigDecimal;

import com.trilogy.app.crm.bean.ChargedItemTypeEnum;
import com.trilogy.app.crm.bean.ServicePackageFee;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.SubscriberChargingSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.holder.LongHolder;


public class DefaultTransactionValidator 
implements DuplicationValidator, ChargingConstants
{
	public int validate(Context ctx, ChargableItemResult result)
	throws Exception 
	{
	       boolean isRefund = (result.getAction()==ACTION_PROVISIONING_REFUND); 
	       Transaction trans = result.getTrans();

	       Object item;
	       ChargedItemTypeEnum itemType;
	       if (result.getChargableItemType() == CHARGABLE_ITEM_AUX_SERVICE)
           {
               item = (SubscriberAuxiliaryService) result.getChargableObjectRef();
               itemType = ChargedItemTypeEnum.AUXSERVICE;
           }
	       else if (result.getChargableItemType() == CHARGABLE_ITEM_BUNDLE)
	       {
	           item = (BundleFee) result.getChargableObject();
	           if (((BundleFee) item).isAuxiliarySource())
	           {
	               itemType = ChargedItemTypeEnum.AUXBUNDLE;
	           }
	           else
	           {
                   itemType = ChargedItemTypeEnum.BUNDLE;
	           }
	       }
           else if (result.getChargableItemType() == CHARGABLE_ITEM_PACKAGE)
           {
               item = (ServicePackageFee) result.getChargableObject();
               itemType = ChargedItemTypeEnum.SERVICEPACKAGE;
           }
	       else
	       {
               item = (ServiceFee2) result.getChargableObject();
               itemType = ChargedItemTypeEnum.SERVICE;
	       }
	        // 0.0 ratio means charging handler doesn't accept refund.
           boolean shouldPerformCheck = !isRefund || trans.getRatio()!=0.0;
	       if (shouldPerformCheck)
	       {
	    	   LongHolder itemFee = new LongHolder(0);
	    	   ctx.put(ChargableItemResult.class, result);
    	       int resultCode = SubscriberChargingSupport.isTransactionValid(ctx, 
    	    		   result.getSubscriber(), 
    	    		   result.getSubscriber().getId(),
    	    		   isRefund, item, itemType, SubscriberChargingSupport.getServicePeriodEnum(result.getChargingCycleType()), itemFee);

    	       if (isRefund && resultCode == TRANSACTION_VALIDATION_SUCCESS)
               {
                   result.setItemChargedFee(itemFee.getValue());
               }
    	       
    	       return resultCode;
    	       
	       } 
	       else
	       {
	           return TRANSACTION_SKIPPED_NO_REFUND;
	       }
	}
	
    public static DefaultTransactionValidator instance()
    {
        return INSTANCE;
    }

    private static final DefaultTransactionValidator INSTANCE = new DefaultTransactionValidator();

}
