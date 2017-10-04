package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.app.crm.subscriber.charge.customize.OneTimeChargeCustomize;
import com.trilogy.app.crm.subscriber.charge.support.AuxServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.BundleChargingSupport;
import com.trilogy.app.crm.subscriber.charge.support.ServiceChargingSupport;
import com.trilogy.app.crm.subscriber.charge.validator.DuplicationValidator;
import com.trilogy.app.crm.subscriber.charge.validator.OneTimeEntityValidator;
import com.trilogy.framework.xhome.context.Context;

public class RedirectOneTimeChargeHandler 
implements ChargeRefundResultHandler, ChargingConstants
{

	
	public void handleTransaction(Context ctx,  ChargableItemResult ret)
	{
		switch (ret.getChargableItemType())
		{
		case CHARGABLE_ITEM_SERVICE :
			ServiceChargingSupport.handleSingleServiceTransactionWithoutRedirecting(ctx, ret, null, null, customizer, validator);
		break;
		case CHARGABLE_ITEM_BUNDLE : 
			BundleChargingSupport.handleSingleBundleTransactionWithoutRedirecting(ctx, ret, null, null, customizer, validator);
		break;
		case CHARGABLE_ITEM_AUX_SERVICE:
			AuxServiceChargingSupport.handleSingleAuxServiceTransactionWithoutRedirecting(ctx, ret, null, null, customizer, validator);
		break; 
		case CHARGABLE_ITEM_PACKAGE:
		}
 	}


	@Override
	public void setDelegate(ChargeRefundResultHandler handler) {
	}    
	    

	
    public static RedirectOneTimeChargeHandler instance()
    {
        return INSTANCE;
    }

    private static final RedirectOneTimeChargeHandler INSTANCE = new RedirectOneTimeChargeHandler();
	static DuplicationValidator validator = new OneTimeEntityValidator();
	static OneTimeChargeCustomize customizer = new OneTimeChargeCustomize(); 

}