package com.trilogy.app.crm.bean.paymentgatewayintegration;

import com.trilogy.app.crm.bean.CreditCardTokenHome;

/**
 * 
 * Constants.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public interface PaymentGatewayIntegrationConstants {

	public static final String TOKEN_TABLE_NAME = "CreditCardToken";
	public static final String PREFIX_RATE_MAP_TABLE_NAME = "CreditCardPrefixRateMap";
	public static final String SCHEDULE_TABLE_NAME = "TopUpSchedule";
	
	public static final String CREATE_TOKEN_HOME_PIPELINE_KEY = CreditCardTokenHome.class.getName() + ":CREATE";
}
