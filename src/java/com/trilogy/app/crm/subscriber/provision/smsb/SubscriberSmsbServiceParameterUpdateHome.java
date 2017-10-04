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
package com.trilogy.app.crm.subscriber.provision.smsb;


import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.smsb.AppSmsbClientSupport;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome;
import com.trilogy.app.crm.support.ServiceSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.OMLogMsg;

/**
 * @author jchen
 *
 *ECP and voice agent is somehow overlapping, we need more clarification
 *for these two parts, since add/remove are performed in voiceProvisionAgent
 *But for states update is done from ecp. 
 *
 *The new implementation is:
 *	This Agents only perfrom other parameters update if ECP is not newly provisioned.
 *That also means that VoiceAgent needs to update for all parameters if provision happens.
 */


public abstract class SubscriberSmsbServiceParameterUpdateHome extends SubscriberServiceParameterUpdateHome
{
	
	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberSmsbServiceParameterUpdateHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}
	

	/**
	 * @param ctx
	 * @param newSub
	 * @param oldSub
	 * @param exp
	 */
	protected void onUpdateError(Context ctx, Subscriber newSub, Subscriber oldSub, Exception exp) {
		String module = Common.OM_SMSB_ERROR;
		new OMLogMsg(Common.OM_MODULE, module).log(ctx);
		if (exp instanceof ProvisioningHomeException)
		{
			ProvisioningHomeException phe = (ProvisioningHomeException)exp;
			SubscriberProvisionResultCode.setProvisionSMSBErrorCode(ctx, phe.getResultCode());
		}
		SubscriberProvisionResultCode.addException(ctx, "Provision SMSB Exception, " + exp.getMessage(), exp, oldSub, newSub);
	}
	
	

	/**
	 * @param ctx
	 * @param newSub
	 * @return
	 * @throws HomeException
	 */
	protected boolean hasServiceJustProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException {
		return AppSmsbClientSupport.hasServiceJustProvisioned(ctx, newSub);
	}
	
	protected  boolean hasServiceProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		Service svc = ServiceSupport.findSubscriberProvisionedServiceType(ctx, newSub, ServiceBase.SERVICE_HANDLER_SMS);
		return (svc != null);
	}
	/* (non-Javadoc)
	 * @see com.redknee.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome#isProvisionOk(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.bean.Subscriber)
	 */
	protected boolean isProvisionOk(Context ctx, Subscriber oldSub,
			Subscriber newSub) throws HomeException 
	{
		return SubscriberProvisionResultCode.getProvisionSMSBErrorCode(ctx) == 0;
	}
}
