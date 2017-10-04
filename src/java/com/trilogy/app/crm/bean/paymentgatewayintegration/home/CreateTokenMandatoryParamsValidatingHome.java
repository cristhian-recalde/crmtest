package com.trilogy.app.crm.bean.paymentgatewayintegration.home;

import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayExceptionFactory;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenRequest;

/**
 * 
 * Validate mandatory request params.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CreateTokenMandatoryParamsValidatingHome extends HomeProxy {

	public CreateTokenMandatoryParamsValidatingHome() {
		// TODO Auto-generated constructor stub
	}

	public CreateTokenMandatoryParamsValidatingHome(Context ctx) {
		super(ctx);
		// TODO Auto-generated constructor stub
	}

	public CreateTokenMandatoryParamsValidatingHome(Home delegate) {
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	public CreateTokenMandatoryParamsValidatingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		if( !(obj instanceof CreateTokenRequest) )
		{
			throw new HomeException("This home cannot be used for objects which are not of type:"
					+ CreateTokenRequest.class.getName());
		}
		
		CreateTokenRequest request = (CreateTokenRequest)obj;
		
		String ban = request.getAccountID();
		String maskedCreditCardNumber = request.getMaskedCardNumber();
		String tokenValue = request.getTokenValue();
		
		StringBuilder error = new StringBuilder("Following parameters in request are null/empty: ");
		boolean create = true;
		
		if(tokenValue == null || "".equals(tokenValue))
		{
			create = false;
			error.append("tokenValue,");
		}
		
		if(ban == null || "".equals(ban))
		{
			create = false;
			error.append("accountId,");
		}
		
		if(maskedCreditCardNumber == null || "".equals(maskedCreditCardNumber))
		{
			create = false;
			error.append("maskedCreditCardNumber.");
		}
		
		if(!create)
		{
			throw PaymentGatewayExceptionFactory.createNestedHomeException(PaymentGatewayResponseCodes.INVALID_PARAMETER, error.toString());			
		}
		
		return super.create(ctx, obj);
	}

	
}
