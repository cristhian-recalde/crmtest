package com.trilogy.app.crm.bean.paymentgatewayintegration.home;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayException;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayExceptionFactory;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.CreateTokenRequest;

/**
 * 
 * Validate if Account is responsible.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class AccountInfoValidatingHome extends HomeProxy {

	
	public AccountInfoValidatingHome() {
		// TODO Auto-generated constructor stub
	}

	public AccountInfoValidatingHome(Context ctx) {
		super(ctx);
		
	}

	public AccountInfoValidatingHome(Home delegate) {
		super(delegate);		
	}

	public AccountInfoValidatingHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	@Override
	public Object create(Context ctx, Object obj) throws HomeException,
			HomeInternalException {
		
		if (! (obj instanceof CreateTokenRequest) )
		{
			throw new HomeException("This can be used only for operations on types:"+CreateTokenRequest.class.getName());
		}
		
		validateAccount((CreateTokenRequest)obj);
		
		return super.create(ctx, obj);
	}
	
	/**
	 * 
	 * Validates if account is present and responsible.
	 * 
	 * @param request - Input Token
	 * @throws HomeException
	 * @throws HomeInternalException
	 */
	private void validateAccount(CreateTokenRequest request) throws HomeException, HomeInternalException
	{
		String ban = request.getAccountID();
		Account account = AccountSupport.getAccount(getContext(), ban);
		
		if(account == null)
		{
			throw PaymentGatewayExceptionFactory.createNestedHomeException(
					PaymentGatewayResponseCodes.NO_SUCH_BAN, "Account[" + ban + "]could not be found");			
		}
		
//		Fix for TT#13041033020
		if((account.getState().equals(AccountStateEnum.INACTIVE)))
		{
			throw PaymentGatewayExceptionFactory.createNestedHomeException(PaymentGatewayResponseCodes.INVALID_PARAMETER, "Account[" 
					+ ban + "] is not active.");
		}
		
		if(!account.isResponsible())
		{
			throw PaymentGatewayExceptionFactory.createNestedHomeException(
					PaymentGatewayResponseCodes.NON_RESPONSIBLE_ACCOUNT, "Account[" + ban + "]is not Responsible");
		}		
	}
	

}
