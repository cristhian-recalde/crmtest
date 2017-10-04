package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenRequest;

/**
 * 
 * Adapts {@link CreateTokenRequest} to {@link CreditCardToken} 
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CreateTokenRequestAdapter implements Adapter {

	public CreateTokenRequestAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object adapt(Context ctx, Object obj) throws HomeException {
		
		return obj;
	}

	@Override
	public Object unAdapt(Context ctx, Object obj) throws HomeException {

		CreateTokenRequest request = (CreateTokenRequest)obj;
		
		CreditCardToken token = new CreditCardToken();
		
		token.setBan(request.getAccountID());
		token.setExpiryDate(request.getExpiryDate());
		token.setMaskedCreditCardNumber(request.getMaskedCardNumber());
		token.setValue(request.getTokenValue());
		
		return token;		
		
	}

}
