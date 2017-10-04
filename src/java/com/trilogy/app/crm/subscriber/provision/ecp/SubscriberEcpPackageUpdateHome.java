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
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.AppEcpClientException;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.provision.ProvisionAgentException;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.state.StateChangeException;
import com.trilogy.app.crm.state.event.EnumStateChangeListener;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
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
public  class SubscriberEcpPackageUpdateHome extends SubscriberVoiceServiceParameterUpdateHome
{

	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberEcpPackageUpdateHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}
	
	protected String getOperationMsg()
	{
		return "IMSI";
	}
	
	/**
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	protected void updateServiceParameter(Context ctx, Subscriber oldSub, Subscriber newSub) throws ProvisioningHomeException {
		updateEcpPackageInfo(ctx, newSub);
	}

	/**
	 * @param oldSub
	 * @param newSub
	 * @return
	 */
	protected boolean parameterEquals(Context ctx, Subscriber oldSub, Subscriber newSub) 
	{
		return SafetyUtil.safeEquals(oldSub.getPackageId(), newSub.getPackageId());
	}
	
	/**
	 * Places a marker for SubscriberEcpProfileUpdateHome to do the update
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	private void updateEcpPackageInfo(Context ctx,  final Subscriber newSub)
	throws ProvisioningHomeException
	{
        ctx.put(Lookup.ECPPROFILE_PACKAGE, newSub.getIMSI());
			
	}
    
}
