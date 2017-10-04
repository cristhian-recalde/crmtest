/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.bas.recharge;

import java.util.Map;

import com.trilogy.app.crm.bean.SubscriberBalances;
import com.trilogy.app.crm.bean.SubscriberBalancesForm;
import com.trilogy.app.crm.calculation.service.CalculationService;
import com.trilogy.app.crm.calculation.service.CalculationServiceException;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.Parameters;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.product.bundle.manager.provision.common.param.Parameter;


/**
 * Processor agent to get Subscriber Balances.
 * 
 * @author Vijay.Gote
 * 
 * @since 9_7_2
 */
public class SubscriberBalancesProcessorAgent implements ContextAgent 
{
	public SubscriberBalancesProcessorAgent()
	{
		
	}
	
	public void execute(Context context)
	{
		long amountOwing = 0;
		long realTimeBalance = 0;
		SubscriberBalances subscriberBalance = new SubscriberBalances();
		final Parameter[] inParamSet = new Parameter[0];
		final SubscriberProfileProvisionClient client = BalanceManagementSupport.getSubscriberProfileProvisionClient(context);
		CalculationService service = (CalculationService) context.get(CalculationService.class);
		
		String subscriberId = (String)context.get(SubscriberBalancesWebAgent.SUBSCRIBER_ID);
		String msisdn = (String)context.get(SubscriberBalancesWebAgent.SUBSCRIBER_MSISDN);
		SubscriberBalancesForm form = (SubscriberBalancesForm)context.get(SubscriberBalancesWebAgent.SUBSCRIBER_BALANCES_FORM);
		Map<String, SubscriberBalances> subscriberBalancesMap = (Map<String, SubscriberBalances>)context.get(SubscriberBalancesWebAgent.SUBSCRIBER_BALANCES_MAP);
		Parameters profile = null;
		try 
		{
			profile = client.getSubscriptionProfile(context, msisdn, (int)form.getSubscriptionType(), inParamSet);
		} 
		catch (final SubscriberProfileProvisionException exception)
        {
            new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + subscriberId,
                    exception).log(context);
            profile = null;
        }
		catch (HomeException exception) {
			new MinorLogMsg("SubscriberSupport", "Failed to query BM for subscription " + subscriberId,
                    exception).log(context);
            profile = null;
		} 
		if(profile != null)
		{
			realTimeBalance = profile.getBalance() * -1;

			try
			{
				amountOwing = service.getAmountOwedBySubscriber(context, subscriberId);
			}
			catch (CalculationServiceException e)
			{
				new MinorLogMsg(this, "Exception while fetching amountOwing for subscriber", e);
			}

			subscriberBalance.setSubscriberId(subscriberId);
			subscriberBalance.setRealTimeBalance(realTimeBalance);
			subscriberBalance.setAmountOwing(amountOwing);

			if(realTimeBalance != amountOwing)
			{
				subscriberBalancesMap.put(subscriberId, subscriberBalance);
			}
		}
		context.put(SubscriberBalancesWebAgent.SUBSCRIBER_BALANCES_MAP, subscriberBalancesMap);
	}
}
