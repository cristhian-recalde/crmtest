package com.trilogy.app.crm.subscriber.charge.validator;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.SubscriberAuxiliaryService;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.framework.xhome.context.Context;

public class PCUGExternalMsisdnTransactionValidator 
implements DuplicationValidator, ChargingConstants
{
	public PCUGExternalMsisdnTransactionValidator(String msisdn)
	{
		msisdn_ = msisdn;
	}
	
	public int validate(Context ctx, ChargableItemResult result)
	throws Exception
	{
        return ChargingConstants.TRANSACTION_VALIDATION_SUCCESS; 
	}
	
	private String msisdn_;
}
