package com.trilogy.app.crm.adapters;

import com.trilogy.app.crm.bean.ChargeFailureActionEnum;
import com.trilogy.app.crm.bean.ProductPrice;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;

public class ProductPriceToServiceFeeAdapter implements Adapter{

	private static final long serialVersionUID = 1L;

	/*@Override
	public ServiceFee2 adapt(Context ctx, Object productPrice)
			throws HomeException {
		if(productPrice instanceof ProductPrice){
			ServiceFee2 serviceFee = XBeans.instantiate(ServiceFee2.class, ctx);
			// defaults
			serviceFee.setChargeFailureAction(ChargeFailureActionEnum.SUSPEND);
			serviceFee.setConsiderMinimumCharge(false);
			serviceFee.setFormulaName("");
			serviceFee.setPaidByContract(false);
			serviceFee.setPrimary(false);
			serviceFee.setRecurrenceInterval(2);
			serviceFee.setServicePeriod(ServicePeriodEnum.MONTHLY);
			//serviceFee.setServicePreference(ServicePreferenceEnum.MANDATORY);
			
			//values to be copied from Product Price
			serviceFee.setFee(0); // TODO: this will be copied from Product price in future sprints
			serviceFee.setServiceId(((ProductPrice) productPrice).getProductId());
			//serviceFee.setServicePreference(productPrice.get); // tobe asked to suyash
			

			//Values set from service
			Long serviceId =  ((ProductPrice) productPrice).getProductId();
			And filter = new And();
			filter.add(new EQ(ServiceXInfo.ID, serviceId));
			Service service = HomeSupportHelper.get(ctx).findBean(ctx, Service.class, filter);
			
			LogSupport.debug(ctx, this, "@@@@ Service Object [" + service + "]");
			
			return serviceFee;
		}
		
		return null;
	}*/
	
	@Override
	public ServiceFee2 adapt(Context ctx, Object productPrice)
			throws HomeException {
		if(productPrice instanceof ProductPrice){
			LogSupport.debug(ctx, this, "Creating instance of ServiceFee");
			
			ServiceFee2 serviceFee = XBeans.instantiate(ServiceFee2.class, ctx);
			Long serviceId =  ((ProductPrice) productPrice).getProductId();
			
			//Values from ProductPrice.
			serviceFee.setServiceId(serviceId);
			serviceFee.setServicePreference(((ProductPrice)productPrice).getPreference());
			serviceFee.setPath(((ProductPrice)productPrice).getPath());
			
			//Values set from service
			And filter = new And();
			filter.add(new EQ(ServiceXInfo.ID, serviceId));
			Service service = HomeSupportHelper.get(ctx).findBean(ctx, Service.class, filter);
			
			if(service != null){
				serviceFee.setServiceName(service.getName());
				serviceFee.setServicePeriod(service.getChargeScheme());
			}
			
			// defaults values are set
			serviceFee.setRecurrenceInterval(1);
			serviceFee.setChargeFailureAction(ChargeFailureActionEnum.SUSPEND);
			serviceFee.setPaidByContract(false);
			serviceFee.setPrimary(false);
			serviceFee.setConsiderMinimumCharge(false);
			serviceFee.setFormulaName("");
			
			
			//values to be copied from Product Price after price creation
			serviceFee.setFee(0); 
			LogSupport.debug(ctx, this, "Created instance of ServiceFee + [" + serviceFee + "]");
			return serviceFee;
		}
		
		return null;
	}

	@Override
	public Object unAdapt(Context paramContext, Object paramObject)
			throws HomeException {
		// TODO Auto-generated method stub
		return null;
	}

}
