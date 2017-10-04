package com.trilogy.app.crm.bean.paymentgatewayintegration.home;

import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.PaymentGatewayIntegrationConfig;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenRequest;

/**
 * 
 * Validate for Single/Multi Token creation.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class SingleTokenImplValidatingHome extends HomeProxy {

	public SingleTokenImplValidatingHome() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SingleTokenImplValidatingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
		// TODO Auto-generated constructor stub
	}

	public SingleTokenImplValidatingHome(Context ctx) {
		super(ctx);
		// TODO Auto-generated constructor stub
	}

	public SingleTokenImplValidatingHome(Home delegate) {
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		if(! ((PaymentGatewayIntegrationConfig)ctx.get(PaymentGatewayIntegrationConfig.class)).getMultiToken() )
		{
			// TODO Write Code here
			if( !(obj instanceof CreateTokenRequest) )
			{
				throw new HomeException("This home operation cannot be performed on object which are not of type:"
						+ CreateTokenRequest.class.getName());
			}
			
			CreateTokenRequest request = (CreateTokenRequest)obj;
			
			CreditCardToken accToken  = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.BAN, request.getAccountID()));
			
			if(accToken != null)
			{
				accToken.setBan(request.getAccountID());
				accToken.setExpiryDate(request.getExpiryDate());
				accToken.setMaskedCreditCardNumber(request.getMaskedCardNumber());
				accToken.setValue(request.getTokenValue());
				return HomeSupportHelper.get(ctx).storeBean(ctx, accToken);
			}
		}
		return super.create(ctx, obj);
	}

	
}
