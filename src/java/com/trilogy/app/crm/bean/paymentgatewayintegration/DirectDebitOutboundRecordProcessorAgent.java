package com.trilogy.app.crm.bean.paymentgatewayintegration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.CreditCardToken;
import com.trilogy.app.crm.bean.CreditCardTokenXInfo;
import com.trilogy.app.crm.bean.CurrencyPrecision;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupport;
import com.trilogy.app.crm.paymentgatewayintegration.PaymentGatewaySupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.product.s2100.oasis.param.Parameter;
import com.trilogy.product.s2100.oasis.param.ParameterID;

public class DirectDebitOutboundRecordProcessorAgent implements ContextAgent
{

	@Override
	public void execute(Context ctx) throws AgentException 
	{
		String record = (String) ctx.get(DirectDebitOutboundFileProcessorLifecycleAgent.OUTBOUND_RECORD);
		AdmerisDirectDebitInboundFileWriter writer = (AdmerisDirectDebitInboundFileWriter) ctx.get(AdmerisDirectDebitInboundFileWriter.class);
		AdmerisDirectDebitErrorFileWriter errorWriter = (AdmerisDirectDebitErrorFileWriter) ctx.get(AdmerisDirectDebitErrorFileWriter.class);

		try
		{
			if(writer == null)
			{
				throw new AgentException("Inbound file writer is null.");
			}

			if(errorWriter == null)
			{
				throw new AgentException("Error file writer is null.");
			}

			if(record == null)
			{
				throw new AgentException("Outbound file record is null.");
			}

			String ban = AdmerisDirectDebitOutboundFileReader.getBan(ctx, record);
			if(ban == null || ban.trim().length()== 0)
			{
				throw new AgentException("BAN is incorrect.");
			}

			CurrencyPrecision cp = (CurrencyPrecision) ctx.get(CurrencyPrecision.class);
			if(cp == null)
			{
				throw new AgentException("Unable to find Currency Precision in context");
			}

			long amount  = AdmerisDirectDebitOutboundFileReader.getAmount(ctx, record, cp);
			if(ban == null || ban.trim().length()== 0)
			{
				throw new AgentException("BAN is incorrect.");
			}

			try
			{

				TopUpSchedule schedule = HomeSupportHelper.get(ctx).findBean(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.BAN, ban));

				if(schedule == null)
				{
					throw new AgentException("Unable to get schedule for BAN :" + ban);
				}

				Account account = HomeSupportHelper.get(ctx).findBean(ctx, Account.class, ban);
				if(account == null)
				{
					throw new AgentException("Unable to get account for BAN :" + ban);
				}

				String msisdn = schedule.getMsisdn();
				if(msisdn == null || msisdn.trim().length()== 0)
				{
					throw new AgentException("Unable to find MSIDN from TopUpSchedule for schedule ID : "+ schedule.getId());
				}
				
				Map<Short, Parameter> outParams = new HashMap<Short, Parameter>();
				Subscriber subscriber = null;
				CreditCardToken token = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardToken.class, new EQ(CreditCardTokenXInfo.ID, schedule.getTokenId()));
				subscriber = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);

				if(token == null)
				{
					throw new AgentException("Token[id:" + schedule.getTokenId() + "] does not exist in the system.");
				}

				if(subscriber == null)
				{
					throw new AgentException("Cannot find subscriber with id:" + schedule.getSubscriptionId());
				}

				if (AccountStateEnum.ACTIVE.equals(account.getState()) || AccountStateEnum.SUSPENDED.equals(account.getState()) 
						|| AccountStateEnum.NON_PAYMENT_SUSPENDED.equals(account.getState()) || AccountStateEnum.PROMISE_TO_PAY.equals(account.getState())  
						|| AccountStateEnum.NON_PAYMENT_WARN.equals(account.getState()))
				{
					int result = PaymentGatewaySupportHelper.get(ctx).chargePaymentGateway(ctx, amount, 0, schedule.getMsisdn(),subscriber.getSubscriptionType(), null, true, 
							token.getMaskedCreditCardNumber(), token.getValue(),null, null, outParams);

					if(result != PaymentGatewaySupport.DEFAULT_SUCCESS)
					{
						throw PaymentGatewayExceptionFactory.createNestedAgentException( result , "PaymentGateway charging failed for msisdn:" + subscriber.getMsisdn() + ". An account note will be added with details of error shortly." );
					}


					Parameter gwTranId = null;
					Parameter ocgTranId = null;
					
					String externalTransactionId = "";
					if(outParams != null)
					{
						gwTranId = outParams.get(ParameterID.PG_TRANSACTION_ID);
						ocgTranId = outParams.get(ParameterID.TRANSACTION_ID);
					}
					
					StringBuilder message = new StringBuilder();
					if(gwTranId != null)
					{
						externalTransactionId = gwTranId.value.stringValue();
						message.append(" GW Transaction id:").append( externalTransactionId );
					}

					if(ocgTranId != null)
					{
						message.append(" OCG Transaction id:").append( ocgTranId.value.stringValue() );
					}

					LogSupport.info(ctx, this, message.toString());

					writer.printLine(ctx, ban, amount, new Date(),externalTransactionId);
				}
				else
				{
					throw new AgentException("Account state is not valid for processing outbound file for BAN : " + account.getBAN());
				}
			}
			catch (HomeException e) 
			{			
				throw new AgentException(e);
			}catch(PaymentGatewayException pge)
			{
				throw new AgentException(pge);

			}
		}catch(AgentException ae)
		{
			new MajorLogMsg(this, "Unable to process outbound TPS record :" + record + ". Reason : " + ae.getLocalizedMessage(), ae).log(ctx);
			if(errorWriter != null)
			{
				errorWriter.writeToErrorFile(ctx, record, ae.getLocalizedMessage());
			}
		}
	}
}
