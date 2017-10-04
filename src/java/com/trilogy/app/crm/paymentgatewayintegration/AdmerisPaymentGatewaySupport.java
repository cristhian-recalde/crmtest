package com.trilogy.app.crm.paymentgatewayintegration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.LongHolder;

import com.trilogy.app.crm.bean.ChannelTypeEnum;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Msisdn;
import com.trilogy.app.crm.bean.paymentgatewayintegration.PaymentGatewayException;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.CreditCardTopUpTypeSpidExtension;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.product.s2100.ErrorCode;
import com.trilogy.product.s2100.oasis.param.Parameter;
import com.trilogy.product.s2100.oasis.param.ParameterID;
import com.trilogy.product.s2100.oasis.param.ParameterValue;

/**
 * 
 * Payment Gateway charging support for Admeris Payment Gateway.
 * 
 * @since 9.3
 * @author <a href='mailto:ameya.bhurke@redknee.com'>Ameya Bhurke</a>
 *
 */
public class AdmerisPaymentGatewaySupport implements PaymentGatewaySupport 
{

	private static AdmerisPaymentGatewaySupport instance = null;
	
	private AdmerisPaymentGatewaySupport()
	{
		
	}
	
	public static PaymentGatewaySupport instance()
	{
		if(instance == null)
		{
			instance = new AdmerisPaymentGatewaySupport();
		}
		
		return instance;
	}
	

    @Override
    public int chargePaymentGateway(Context ctx, long totalAmount, long tax, Subscriber subscriber, boolean recurring,
            String cardNumber, String token, Map<Short, Parameter> outParams) throws PaymentGatewayException
    {
        return chargePaymentGateway(ctx, totalAmount, tax, subscriber.getMSISDN(), subscriber.getSubscriptionType(),
                subscriber.getCurrency(ctx), recurring, cardNumber, token, null, null, outParams);
    }


    /**
     * {@inheritDoc}
     */
    public int chargePaymentGateway(Context ctx, long totalAmount, long tax, String banOrMsisdn, long subscriptionType,
            String curreny, boolean recurring, String cardNumber, String token, String fraudSessionId,
            Integer spidInRequest, Map<Short, Parameter> outParams) throws PaymentGatewayException
    {
    
        return chargePaymentGateway(ctx, totalAmount, tax, banOrMsisdn, subscriptionType,
                curreny, recurring, cardNumber, token, fraudSessionId,
                spidInRequest, outParams, null);
    }
	
    public int chargePaymentGateway(Context ctx, long totalAmount, long tax, String banOrMsisdn, long subscriptionType,
            String curreny, boolean recurring, String cardNumber, String token, String fraudSessionId,
            Integer spidInRequest, Map<Short, Parameter> outParams, String paymentGatewayMerchantId)
            throws PaymentGatewayException
    {
    	Map <Object,Object> map = new HashMap<Object,Object>();
    	map.put(ParameterID.INTERFACE_ID,Long.valueOf(ChannelTypeEnum.BSS.getIndex()));
    	 return chargePaymentGateway(ctx, totalAmount, tax, banOrMsisdn, subscriptionType,
                 curreny, recurring, cardNumber, token, fraudSessionId,
                 spidInRequest, outParams, paymentGatewayMerchantId, map);
    }
    
    public int chargePaymentGateway(Context ctx, long totalAmount, long tax, String banOrMsisdn, long subscriptionType,
            String curreny, boolean recurring, String cardNumber, String token, String fraudSessionId,
            Integer spidInRequest, Map<Short, Parameter> outParams, String paymentGatewayMerchantId,  Map<Object, Object> genericParam)
            throws PaymentGatewayException
    {
		int result = PaymentGatewaySupport.DEFAULT_SUCCESS;
		
		Map<Short, Parameter> inParams = new HashMap<Short, Parameter>();
		
		LogSupport.info(ctx, this, "Charging Payment Gateway for account/msisdn : " + banOrMsisdn);
		
		AppOcgClient client = (AppOcgClient)ctx.get(AppOcgClient.class);
		if(LogSupport.isDebugEnabled(ctx))
		{
		    LogSupport.debug(ctx, this, "AppOcgClient instance from context : " + client);
		}
		
		if(client == null)
		{
			throw new PaymentGatewayException("OCG Client is null", ErrorCode.UNKNOWN_ERROR);
		}
		    
		
		/**
		 * retrieve special subscriber type for credit card charging from spid extension
		 */
		int index = 3;
		int spid = -1;
		String currency = curreny;
		Account account =null;
        try
        {
            if (spidInRequest != null)
            {
                spid = spidInRequest;
                if (LogSupport.isDebugEnabled(ctx))
                {
                    LogSupport.debug(ctx, this, "SPID is available in the request, using SPID from request. SPID : ["
                            + spid + "]");
                }
            }
            else
            {
                account = AccountSupport.getAccount(ctx, banOrMsisdn);
                if (account != null)
                {
                    spid = account.getSpid();
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        LogSupport.debug(ctx, this,
                                "SPID is not available in the request, Getting SPID from Account. SPID : [" + spid
                                        + "]");
                    }

                }
                else
                {
                    Msisdn msisdnObj = MsisdnSupport.getMsisdn(ctx, banOrMsisdn);
                    if (msisdnObj != null)
                    {
                        spid = msisdnObj.getSpid();
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this,
                                    "SPID is not available in the request, Getting SPID from MSISDN. SPID : [" + spid
                                            + "]");
                        }
                    }
                }
                if (spid == -1)
                {
                    User user = (User) ctx.get(java.security.Principal.class);
                    if (user != null)
                    {
                        spid = user.getSpid();
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            LogSupport.debug(ctx, this,
                                    "SPID is not available in the request, also MSISDN not Found, Getting SPID from User, SPID : ["
                                            + spid + "]");
                        }
                    }
                    else
                    {
                        throw new PaymentGatewayException(
                                "SPID cannot be determined. The following BAN or MSISDN does not have an entry in the ACCOUNT/MSISDN table; BAN/MSISDN:"
                                        + banOrMsisdn,
                                com.redknee.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
                    }
                }
            }

            CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spid);
            if (crmSpid == null)
            {
                throw new PaymentGatewayException(
                        "SPID " + spid + " is not configured in billing system!",
                        com.redknee.app.crm.bean.paymentgatewayintegration.PaymentGatewayResponseCodes.NO_CHARGE_ATTEMPT_BSS_INTERNAL_ERROR);
            }

            if (currency == null)
            {
                currency = crmSpid.getCurrency();
            }

            /**
             * paymentGatewayMerchantId will be null for Admeris while for WPay it would be received from WSC.
             * In case of recurring payments initiated by BSS for WPay the merchantId will again be null, in that case 
             * it will be picked from SPID creditCardTopUP extension.
             */
            if(paymentGatewayMerchantId == null)
            {
                Collection<Extension> extensionList = crmSpid.getExtensions();
                for (Extension extension : extensionList)
                {
                    if (extension instanceof CreditCardTopUpTypeSpidExtension)
                    {
                        CreditCardTopUpTypeSpidExtension ccTopUpTypeExt = (CreditCardTopUpTypeSpidExtension) extension;
                        index = ccTopUpTypeExt.getAccountType();
                        paymentGatewayMerchantId = ccTopUpTypeExt.getMerchantId();
                        break;
                    }
                }
            }
        }
        catch (HomeException e)
        {
            LogSupport.info(ctx, this,
                    "HomeException retrieving spid extension. Using subscriber type as credit card.", e);
        }
		
		/*********************************************************************************************************************
		 * 
		 * POPULATE OCG INPUT PARAMS
		 * 
		 *********************************************************************************************************************
		 */
		
        if(cardNumber != null)
        {
            Parameter cardNumberParam = new Parameter();
    		cardNumberParam.parameterID = ParameterID.PG_CREDIT_CARD_NUMBER;
    		ParameterValue cardNumberValue = new ParameterValue();
    		cardNumberValue.stringValue(cardNumber);
    		cardNumberParam.value = cardNumberValue;		
    		inParams.put(cardNumberParam.parameterID, cardNumberParam);
        }
        
		Parameter tokenParam = new Parameter();
		tokenParam.parameterID = ParameterID.PG_STORAGE_TOKEN;
		ParameterValue tokenParamValue = new ParameterValue();
		tokenParamValue.stringValue(token);
		tokenParam.value = tokenParamValue;		
		inParams.put(tokenParam.parameterID, tokenParam);
		
		Parameter merchantIdParam = new Parameter();
		merchantIdParam.parameterID = ParameterID.PG_MERCHANT_ID;
		ParameterValue merchantIdParamValue = new ParameterValue();
		merchantIdParamValue.stringValue(paymentGatewayMerchantId);
		merchantIdParam.value = merchantIdParamValue;		
		inParams.put(merchantIdParam.parameterID, merchantIdParam);
		
		Parameter recurringParam = new Parameter();
		recurringParam.parameterID = ParameterID.PG_RECURRING_INDICATOR;
		ParameterValue recurringParamValue = new ParameterValue();
		recurringParamValue.booleanValue(recurring);
		recurringParam.value = recurringParamValue;
		inParams.put(recurringParam.parameterID, recurringParam);
		
		Parameter taxParam =  new Parameter();
		taxParam.parameterID = ParameterID.PG_TAX_AMOUNT;
		ParameterValue taxParamValue = new ParameterValue();
		taxParamValue.longValue(tax);
		taxParam.value = taxParamValue;
		inParams.put(taxParam.parameterID, taxParam);
		
		Parameter totalAmountParam =  new Parameter();
		totalAmountParam.parameterID = ParameterID.PG_TOTAL_AMOUNT;
		ParameterValue totalAmountParamValue = new ParameterValue();
		totalAmountParamValue.longValue(totalAmount);
		totalAmountParam.value = totalAmountParamValue;
		inParams.put(totalAmountParam.parameterID, totalAmountParam);
		
		Parameter recipientMsisdnParam = new Parameter();
		recipientMsisdnParam.parameterID = ParameterID.RECIPIENT_MSISDN;
		ParameterValue recipientMsisdnParamValue = new ParameterValue();
		recipientMsisdnParamValue.stringValue(banOrMsisdn);
		recipientMsisdnParam.value = recipientMsisdnParamValue;
		inParams.put(recipientMsisdnParam.parameterID , recipientMsisdnParam);
		
		Parameter spidParam = new Parameter();
		spidParam.parameterID = ParameterID.SPID;
		ParameterValue spidValue = new ParameterValue();
		spidValue.intValue(spid);
		spidParam.value = spidValue;		
		inParams.put(spidParam.parameterID, spidParam);
		
		if(fraudSessionId !=null  && !(fraudSessionId.trim().isEmpty())){
			Parameter fraudSessionParam = new Parameter();
			fraudSessionParam.parameterID = ParameterID.PG_FRAUD_SID   ;
			ParameterValue fraudSessionParamValue = new ParameterValue();
			fraudSessionParamValue.stringValue(fraudSessionId);
			fraudSessionParam.value = fraudSessionParamValue;
			inParams.put(fraudSessionParam.parameterID , fraudSessionParam);		
		}
		
		if(genericParam != null)
		{
			Long interfaceId = (Long)genericParam.get(ParameterID.INTERFACE_ID);
			if(interfaceId == null)
			{
				interfaceId = Long.valueOf(ChannelTypeEnum.BSS_INDEX);
			}
			Parameter interfaceIdParam =  new Parameter();
			interfaceIdParam.parameterID = ParameterID.INTERFACE_ID;
			ParameterValue interfaceIdParamValue = new ParameterValue();
			interfaceIdParamValue.longValue(interfaceId.longValue());
			interfaceIdParam.value = interfaceIdParamValue;
			inParams.put(interfaceIdParam.parameterID, interfaceIdParam);
		}
		
		
		/**********************************************************************************************************************************************
		 * 
		 * OCG I/P Params populate comeplete.
		 * 
		 * *********************************************************************************************************************************************
		 */
		
		
		if(LogSupport.isDebugEnabled(ctx))
		{
            LogSupport.debug(ctx, this, "Params for requestDebit() : " + banOrMsisdn + "-"
                    + index + " ("+ new SubscriberTypeEnum(index, "CreditCard", "CreditCard") + ") -" + totalAmount + "-" + currency
                    + "-" + recurring + "-" + subscriptionType + "- InParams : " + inParams + "- OutParams : " + outParams);
		}
		
		result = client.requestDebit(banOrMsisdn , new SubscriberTypeEnum(index, "CreditCard", "CreditCard"), totalAmount, currency, true,  
				recurring ? "Recurring Credit Card Top Up" : "One Time Credit Card Top Up", 
				subscriptionType, new LongHolder(), new LongHolder(), false, inParams , outParams);
		
		Parameter gwTranId = null;
		Parameter ocgTranId = null;
		if(outParams != null)
		{
			gwTranId = outParams.get(ParameterID.PG_TRANSACTION_ID);
			ocgTranId = outParams.get(ParameterID.TRANSACTION_ID);
		}
		
		if(result == 0)
		{
			StringBuilder message = new StringBuilder("Payment Gateway charged successfully.");
			if(gwTranId != null)
			{
				message.append(" GW Transaction id:").append( gwTranId.value.stringValue() );
			}
			
			if(ocgTranId != null)
			{
				message.append(" OCG Transaction id:").append( ocgTranId.value.stringValue() );
			}
			
			LogSupport.info(ctx, this, message.toString());
		}
		else
		{	
			StringBuilder message = new StringBuilder("Payment Gateway could not be charged ");
			
			message.append(" OCG result:").append(result);
			
			
			LogSupport.info(ctx, this, message.toString());
		}
		
		return result;
	}

}
