package com.trilogy.app.crm.api.rmi;

import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentsupportservice.DepositDetails;
import com.trilogy.app.crm.bean.DepositReference;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

public class DepositDetailsToDeositReferenceAdapter implements Adapter {

	@Override
	public Object adapt(final Context ctx, final Object obj) throws HomeException {

		DepositDetails depositDetails = (DepositDetails) obj;
		DepositReference depositReference = new DepositReference();

		depositReference.setCorrelationID(depositDetails.getCorrelationID());
		depositReference.setSubscriptionID(depositDetails.getSubscriptionID());

		if (depositDetails.getSubscriptionType() != null)
			depositReference.setSubscriptionType(depositDetails.getSubscriptionType());

		if (depositDetails.getProductID() != null)
			depositReference.setProductID(depositDetails.getProductID());

		if (depositDetails.getDepositType() != null)
			depositReference.setDepositType(depositDetails.getDepositType());

		if (depositDetails.getDepositAmount() != null)
			depositReference.setDepositAmount(depositDetails.getDepositAmount());

		if (depositDetails.getReasonCode() != null)
		depositReference.setReasonCode(depositDetails.getReasonCode());

		if (depositDetails.getExternalTransactionID() != null)
			depositReference.setExternalTransactionID(depositDetails.getExternalTransactionID());
		
        if(depositDetails.getDepositDate() != null)
		depositReference.setDepositDate(depositDetails.getDepositDate());

		return depositReference;
	}

	@Override
	public Object unAdapt(Context ctx, final Object obj) throws HomeException {
		throw new UnsupportedOperationException();
	}

}
