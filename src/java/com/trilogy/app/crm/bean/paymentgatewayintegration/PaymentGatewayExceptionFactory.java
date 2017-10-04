package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * Factory to create exceptions related to Payment Gateway Integration.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */

public final class PaymentGatewayExceptionFactory {

	private PaymentGatewayExceptionFactory()
	{
		
	}
	
	
	public static HomeException createNestedHomeException(int errorCode , String errorMessage)
	{
		PaymentGatewayException pge = new PaymentGatewayException(errorMessage, errorCode);
		return new HomeException(pge);
	}
	
	public static AgentException createNestedAgentException(int code , String message)
	{
		PaymentGatewayException pge = new PaymentGatewayException(message , code);
		return new AgentException(pge);
	}
}
