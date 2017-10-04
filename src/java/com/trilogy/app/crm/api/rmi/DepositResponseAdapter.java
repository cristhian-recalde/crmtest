package com.trilogy.app.crm.api.rmi;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.app.crm.bean.CreateDepositResponse;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.DepositResponse;

public class DepositResponseAdapter implements Adapter {
	
	@Override
	public Object adapt(final Context ctx, final Object obj) throws HomeException {
		
		CreateDepositResponse response = (CreateDepositResponse) obj ;
		DepositResponse depositResponse = new DepositResponse();
		
		depositResponse.setDepositID(response.getDepositID());
		depositResponse.setCorrelationID(response.getCorrelationID());
		return depositResponse;
	}

	@Override
	public Object unAdapt(Context ctx, final Object obj) throws HomeException {
		throw new UnsupportedOperationException();
	}
	
}