package com.trilogy.app.crm.paymentgatewayintegration;

import java.util.Map;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayException;
import com.trilogy.app.crm.support.Support;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.product.s2100.oasis.param.Parameter;

/**
 * 
 * Support interface for Payment Gateway Integration.
 * 
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public interface PaymentGatewaySupport extends Support
{
	
	int DEFAULT_SUCCESS = ErrorCode.NO_ERROR;

	/**
	 * 
	 * Charge PaymentGateway for a msisdn and creditcard/token.
	 * 
	 * @param ctx Current App Conext
	 * @param totalAmount amount to charge the payment gateway
	 * @param tax tax amount
	 * @param subscriber 
	 * @param recurring 
	 * @param cardNumber 
	 * @param token 
	 * @param outParams
	 * @return
	 * @throws PaymentGatewayException TODO
	 */
	public int chargePaymentGateway(Context ctx , long totalAmount , long tax , Subscriber subscriber , 
			boolean recurring, String cardNumber, String token, Map<Short, Parameter> outParams) throws PaymentGatewayException;
	
	/**
	 * 
	 * @param ctx
	 * @param totalAmount
	 * @param tax
	 * @param msisdn
	 * @param subscriptionType
	 * @param curreny
	 * @param recurring
	 * @param cardNumber
	 * @param token
	 * @param outParams
	 * @return
	 * @throws PaymentGatewayException
	 */
	public int chargePaymentGateway(Context ctx, long totalAmount,
			long tax, String msisdn, long subscriptionType, String curreny,
			boolean recurring, String cardNumber, String token, String fraudSessionId, Integer spidInRequest, Map<Short, Parameter> outParams) throws PaymentGatewayException ;
	
	
	/***
	 * 
	 * @param ctx
	 * @param totalAmount
	 * @param tax
	 * @param msisdn
	 * @param subscriptionType
	 * @param curreny
	 * @param recurring
	 * @param cardNumber
	 * @param token
	 * @param fraudSessionId
	 * @param spidInRequest
	 * @param outParams
	 * @param paymentGatewayMerchantId
	 * @return
	 * @throws PaymentGatewayException
	 */
	public int chargePaymentGateway(Context ctx, long totalAmount,
            long tax, String msisdn, long subscriptionType, String curreny,
            boolean recurring, String cardNumber, String token, String fraudSessionId, Integer spidInRequest, Map<Short, Parameter> outParams, String paymentGatewayMerchantId) throws PaymentGatewayException ;
	/**
	 * 
	 * @param ctx
	 * @param totalAmount
	 * @param tax
	 * @param msisdn
	 * @param subscriptionType
	 * @param curreny
	 * @param recurring
	 * @param cardNumber
	 * @param token
	 * @param fraudSessionId
	 * @param spidInRequest
	 * @param outParams
	 * @param paymentGatewayMerchantId
	 * @param genericParam
	 * @return
	 * @throws PaymentGatewayException
	 */
	public int chargePaymentGateway(Context ctx, long totalAmount,
            long tax, String msisdn, long subscriptionType, String curreny,
            boolean recurring, String cardNumber, String token, String fraudSessionId, Integer spidInRequest, Map<Short, Parameter> outParams, String paymentGatewayMerchantId, Map<Object, Object> genericParam) throws PaymentGatewayException ;
	
	
}
