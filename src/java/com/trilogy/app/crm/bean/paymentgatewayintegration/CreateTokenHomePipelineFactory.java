package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.io.IOException;
import java.rmi.RemoteException;

import com.trilogy.app.crm.bean.CreditCardTokenHome;
import com.trilogy.app.crm.bean.CreditCardTokenXDBHome;
import com.trilogy.app.crm.bean.paymentgatewayintegration.home.AccountInfoValidatingHome;
import com.trilogy.app.crm.bean.paymentgatewayintegration.home.CreateTokenMandatoryParamsValidatingHome;
import com.trilogy.app.crm.bean.paymentgatewayintegration.home.SingleTokenImplValidatingHome;
import com.trilogy.app.crm.home.PipelineFactory;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import static com.redknee.app.crm.bean.paymentgatewayintegration.PaymentGatewayIntegrationConstants.TOKEN_TABLE_NAME;

/**
 * 
 * Creates a new Token after performing validations. 
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class CreateTokenHomePipelineFactory implements PipelineFactory {

	public CreateTokenHomePipelineFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Home createPipeline(Context ctx, Context serverCtx)
			throws RemoteException, HomeException, IOException, AgentException {
		
		Home home = (Home)ctx.get(CreditCardTokenHome.class);
		home = new AdapterHome(ctx ,home, new CreateTokenRequestAdapter());
		home = new SingleTokenImplValidatingHome(ctx, home);
		home = new AccountInfoValidatingHome(ctx, home);
		home = new CreateTokenMandatoryParamsValidatingHome(ctx, home);
		
		return home;
	}

}
