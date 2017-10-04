package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import com.trilogy.app.crm.api.rmi.support.APIGenericParameterSupport;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.Schedule;
import com.trilogy.util.crmapi.wsdl.v3_0.types.paymentgatewayintegration.Token;

/**
 * 
 * Adapt {@link CreditCardToken} to {@link Token}
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public final class PaymentGatewayEntitiesAdapter {

	public static void adapt(Token adaptee, CreditCardToken adapted)
	{
		adaptee.setAccountID(adapted.getBan());
		adaptee.setExpiryDate(adapted.getExpiryDate());
		adaptee.setMaskedCardNumber(adapted.getMaskedCreditCardNumber());
		adaptee.setTokenID(adapted.getId());
		adaptee.setTokenValue(adapted.getValue());
	}
	
	public static void adapt(Schedule adaptee, TopUpSchedule adapted)
	{
		adaptee.setAmount(adapted.getAmount());
		if (adapted.getNextApplication() != null)
		{
			Calendar nextApplication = new GregorianCalendar();
			nextApplication.setTime(adapted.getNextApplication());
			adaptee.setNextApplication(nextApplication);
		}
		else
		{
			adaptee.setNextApplication(null);
		}
		adaptee.setScheduleID(adapted.getId());
		adaptee.setTokenID(adapted.getTokenId());
		
		Collection<GenericParameter> parametersList = new ArrayList<GenericParameter>();
		parametersList.add(APIGenericParameterSupport.getIsPlanChangeScheduledParam(adapted.getPlanChangeScheduled()));
		parametersList.add(APIGenericParameterSupport.getIsUsePlanFeeParam(adapted.getUsePlanFees()));
		adaptee.setParameters(parametersList.toArray(new GenericParameter[]{}));
	}
}
