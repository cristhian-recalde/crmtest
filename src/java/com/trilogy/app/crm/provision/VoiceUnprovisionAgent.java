/*
 *  VoiceUnprovisionAgent.java
 *
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
package com.trilogy.app.crm.provision;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.EntryLogMsg;

public class VoiceUnprovisionAgent extends CommonUnprovisionAgent
{
	public VoiceUnprovisionAgent()
	{
	}

	/**
	 * Unprovision voice services to HLR and AppEcp
	 * Context must contain the subscriber to be installed keyed by Subscriber.class
	 * Context must contain Service to retrieve additional params needed associated with this service
	 * Context must contain AppEcpClient to provision AppEcp using CORBA
	 * Context must contain Account of the subscriber
	 * @param ctx
	 */
	public void execute(Context ctx)
		throws AgentException
	{
		AppEcpClient appEcpClient;
		Subscriber subscriber;
		Service service;
		String hlrCmds = null;

		appEcpClient = (AppEcpClient)ctx.get(AppEcpClient.class);
		if (appEcpClient == null)
		{
			throw new AgentException("System error: AppEcpClient not found in context");
		}

		subscriber = (Subscriber)ctx.get(Subscriber.class);

		if (subscriber == null)
		{
			throw new AgentException("System error: No subscriber to provision");
		}

		service = (Service)ctx.get(Service.class);
		if (service == null)
		{
			throw new AgentException("System error: Service for voice provisioning not found in context");
		}

		// look for the SogClient for the subscriber's HLR ID
		short hlrId = subscriber.getHlrId();

		// UMP-3348: Allow Data service for broadband and voice for wireline subscriptions
        // hack fix to support Multiplay capability, as legacy interfaces did not consider subscription type other than AIRTIME
        // msisdn|subscriptionType will be passed to URCS
		
		String voiceUnprovParams = null;
		if(allowMultiSubPerAccount(ctx, subscriber))
		{
			voiceUnprovParams = subscriber.getMsisdn()+"|"+subscriber.getSubscriptionType();
		}
		else
		{
			voiceUnprovParams = subscriber.getMsisdn();
		}
		
		// delete subscriber from ECP
		int result = appEcpClient.deleteSubscriber(voiceUnprovParams);
		
//		The following OM code is moved from original provisioninghome.java
		//Apr 07, 2005
		if (result != 0)
		{
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getUnprovisionErrorMessage(ctx, ExternalAppEnum.VOICE, result, service), result,
                    ExternalAppEnum.VOICE);
            
			ProvisionResultCode rc = (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
			
			if (rc != null)
			{
				//VoiceUnprovisionAgent - fail to delete subscriber - result code = {0}
		        new EntryLogMsg(13783, this, "", subscriber.toString(), new java.lang.String[]
                {
	                String.valueOf(result)
	            }, null).log(ctx);

		        rc.setResultEcp(result);
				rc.setResultLast(exception.getResultCode());
				
				HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
				
				if (exps != null)
				{
                    SubscriberSupport.generateOM(ctx, exps, exception.getResultCode(), exception.getMessage(),
			                result,Common.OM_ECP_ERROR,this);
				}
				else
				{
                    throw exception;
				}
			}
            else
            {
                throw exception;
            }
		}

        if (subscriber.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
		    hlrCmds = service.getUnprovisionConfigs();
        }
        else if (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            hlrCmds = service.getPrepaidUnprovisionConfigs();
        }
        if ((hlrCmds == null) || (hlrCmds.length() == 0))
		{
			// no HLR commands means nothing to do
			return;
		}
		
        callHlr(ctx,false,subscriber,service,null,null);
	}
	
	/**
	 * Multiplay capability
	 * @param context
	 * @param subscriberAccount
	 * @return
	 * @throws AgentException
	 */
	private boolean allowMultiSubPerAccount(final Context context, final Subscriber subscriberAccount)
    throws AgentException
	{
	    final int spid = subscriberAccount.getSpid();
	
	    final Home home = (Home)context.get(CRMSpidHome.class);
	    try
	    {
	    	final CRMSpid serviceProvider = (CRMSpid)home.find(context, Integer.valueOf(spid));
	    	if (serviceProvider == null)
		    {
		        throw new AgentException(
		            "Failed to locate service provider profile " + spid + " for account " + subscriberAccount.getBAN());
		    }
	    	return serviceProvider.isAllowMultiSubForOneAccount();
	    }
	    catch(HomeException he)
	    {
	    	throw new AgentException(
		            "Exception while looking for spid " + spid + " for account " + subscriberAccount.getBAN() +" "+ he.getMessage());
	    }
	}
    
	
}
