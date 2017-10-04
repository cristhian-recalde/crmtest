package com.trilogy.app.crm.service.filter;

import com.trilogy.app.crm.bean.PaymentOptionEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ui.AuxiliaryService;
import com.trilogy.app.crm.bean.ui.Service;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class PaymentOptionFilterPredicate implements Predicate {

	@Override
	public boolean f(Context ctx, Object obj) throws AbortVisitException {
		
		Object beanObj = ctx.get(AbstractWebControl.BEAN); 
		
		PaymentOptionEnum option  = (PaymentOptionEnum)obj;
		
		if((beanObj instanceof AuxiliaryService)){
			
			AuxiliaryService auxService = (AuxiliaryService)beanObj;
			
			if(auxService.getChargingModeType() == ServicePeriodEnum.ONE_TIME
					&& (option == PaymentOptionEnum.PAY_IN_ADVANCE
					||option == PaymentOptionEnum.PAY_IN_ARREARS
					||option == PaymentOptionEnum.CHARGE_IN_ARREARS)){
				return false;
			}
			 
		}else if((beanObj instanceof Service)){
			
			Service service = (Service)beanObj;
			
			if(service.getChargeScheme() == ServicePeriodEnum.ONE_TIME
					&& (option == PaymentOptionEnum.PAY_IN_ADVANCE
					||option == PaymentOptionEnum.PAY_IN_ARREARS
					||option == PaymentOptionEnum.CHARGE_IN_ARREARS)){
				return false;
			}
			
		}
		
		return true;
	}

}
