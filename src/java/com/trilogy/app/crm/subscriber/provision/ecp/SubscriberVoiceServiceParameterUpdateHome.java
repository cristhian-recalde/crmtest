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
package com.trilogy.app.crm.subscriber.provision.ecp;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceBase;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.ProvisioningHomeException;
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
public  abstract class SubscriberVoiceServiceParameterUpdateHome extends SubscriberServiceParameterUpdateHome
{

	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberVoiceServiceParameterUpdateHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}
	
	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @param exp
	 */
	protected  void onUpdateError(Context ctx, Subscriber oldSub, Subscriber newSub, Exception exp)
	{
		String module = Common.OM_ECP_ERROR;
		new OMLogMsg(Common.OM_MODULE, module).log(ctx);
		if (exp instanceof ProvisioningHomeException)
		{
			ProvisioningHomeException phe = (ProvisioningHomeException)exp;
			SubscriberProvisionResultCode.setProvisionEcpErrorCode(ctx, phe.getResultCode());
		}
		SubscriberProvisionResultCode.addException(ctx, "Failed to update " + getOperationMsg() + " on URCS Voice -> " + exp.getMessage(), exp, oldSub, newSub);
	}


	/**
	 * @param ctx
	 * @param newSub
	 * @return
	 * @throws HomeException
	 */
	protected boolean hasServiceJustProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException {
		return AppEcpClientSupport.hasServiceJustProvisioned(ctx, newSub);
	}
	
	
	protected  boolean hasServiceProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		Service svc = ServiceSupport.findSubscriberProvisionedServiceType(ctx, newSub, ServiceBase.SERVICE_HANDLER_VOICE);
		return (svc != null);
	}
	
	protected boolean isProvisionOk(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException
	{
		return SubscriberProvisionResultCode.getProvisionEcpErrorCode(ctx) == 0;
	}
}
