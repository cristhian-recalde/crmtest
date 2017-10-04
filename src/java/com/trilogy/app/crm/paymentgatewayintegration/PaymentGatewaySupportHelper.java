package com.trilogy.app.crm.paymentgatewayintegration;

import com.trilogy.app.crm.support.SupportHelper;
import com.trilogy.framework.xhome.context.Context;

/**
 * 
 * Helper class for {@link PaymentGatewaySupport}
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class PaymentGatewaySupportHelper extends SupportHelper 
{

	private PaymentGatewaySupportHelper() 
	{
		
	}
	
	public static PaymentGatewaySupport get(Context ctx)
	{
		return get(ctx, PaymentGatewaySupport.class, AdmerisPaymentGatewaySupport.instance());
	}

	public static PaymentGatewaySupport set(Context ctx , PaymentGatewaySupport instance)
	{
		return register(ctx, PaymentGatewaySupport.class, instance);
	}
}
