/*
 *  SmsUnprovisionAgent.java
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
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.subscriber.provision.ProvisionResultCode;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.EntryLogMsg;

public class SmsUnprovisionAgent extends CommonUnprovisionAgent
{
   // REVIEW(cleanup): This constructor is not necessary.  The compiler will
   // create it implicitly. GEA
	public SmsUnprovisionAgent()
	{
	}

	/**
	 * Unprovision SMS services to HLR and AppSmsb
	 * Context must contain the subscriber to be installed keyed by Subscriber.class
	 * Context must contain Service to retrieve additional params needed associated with this service
	 * Context must contain AppSmsbClient to provision AppSmsb using CORBA
	 * Context must contain Account of the subscriber
	 * @param ctx
	 */
	public void execute(Context ctx)
		throws AgentException
	{
		AppSmsbClient appSmsbClient;
		Subscriber subscriber;
		Service service;
		String hlrCmds = null;

      // REVIEW(design): This methods are similar to those in
      // SmsProvisionAgent.  They could be factored-out to reduce duplicate code
      // and improve readability. GEA
		appSmsbClient = (AppSmsbClient)ctx.get(AppSmsbClient.class);
		if (appSmsbClient == null)
		{
			throw new AgentException("System error: AppSmsbClient not found in context");
		}

		subscriber = (Subscriber)ctx.get(Subscriber.class);
		if (subscriber == null)
		{
			throw new AgentException("System error: No subscriber to provision");
		}

		service = (Service)ctx.get(Service.class);
		if (service == null)
		{
		    service = (Service)ctx.get(com.redknee.app.crm.bean.core.Service.class);
		    if(service == null)
		    {
		        throw new AgentException("System error: Service for SMS provisioning not found in context");
		    }
		}

		// look for the SogClient for the subscriber's HLR ID
		short hlrId = subscriber.getHlrId();

		// add subscriber to SMSB
		int result = appSmsbClient.deleteSubscriber(
			subscriber.getMSISDN()
			);
        
		//The following OM code is moved from original provisioninghome.java
		//Apr 07, 2005
		if (result != 0)
		{
            ProvisionAgentException exception = new ProvisionAgentException(ctx, ExternalAppSupportHelper.get(ctx)
                    .getUnprovisionErrorMessage(ctx, ExternalAppEnum.SMS, result, service), result,
                    ExternalAppEnum.SMS);

            ProvisionResultCode rc = (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
			if (rc != null)
			{
				//SNMP trap: SmsUnprovisionAgent - fail to delete subscriber - result code = {0}
				new EntryLogMsg(13784, this, "", subscriber.toString(), new java.lang.String[]
				{
                    String.valueOf(result)
                }, null).log(ctx);
				rc.setResultSmsb(result);
				rc.setResultLast(exception.getResultCode());
				HTMLExceptionListener exps = (HTMLExceptionListener)ctx.get(HTMLExceptionListener.class);
				if (exps != null)
				{
                    SubscriberSupport.generateOM(ctx, exps,
							exception.getResultCode(),
							exception.getMessage(),
			                result,
			                Common.OM_SMSB_ERROR,
			                this);
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
        else if  (subscriber.getSubscriberType() == SubscriberTypeEnum.PREPAID)
        {
            hlrCmds = service.getPrepaidUnprovisionConfigs();
        }
        
        if ((hlrCmds == null) || (hlrCmds.length() == 0))
		{
			// no HLR commands means nothing to do
			return;
		}

		// execute HLR commands using HLR client
        callHlr(ctx,false, subscriber,service,null,null);
	}
}
