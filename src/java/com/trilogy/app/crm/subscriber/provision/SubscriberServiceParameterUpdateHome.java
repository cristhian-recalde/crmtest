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
package com.trilogy.app.crm.subscriber.provision;

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
public abstract class SubscriberServiceParameterUpdateHome extends HomeProxy
{

	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberServiceParameterUpdateHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}
	
	/**
	 * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context, java.lang.Object)
	 */
	public Object store(Context ctx, Object obj) throws HomeException 
	{
		LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
		Subscriber newSub = (Subscriber)obj;
		Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
		
		//System.out.println(""   + getOperationMsg() + ", " + hasServiceProvisioned(ctx, oldSub, newSub) + "," + hasServiceJustProvisioned(ctx, oldSub, newSub));
		try
		{
			if (!parameterEquals(ctx, oldSub, newSub) &&
					hasServiceProvisioned(ctx, oldSub, newSub) && 
					!hasServiceJustProvisioned(ctx, oldSub, newSub)
					&&isProvisionOk(ctx, oldSub, newSub))
			{
				updateServiceParameter(ctx, oldSub, newSub);
			}
		}
		catch(Exception exp)
		{
			onUpdateError(ctx, oldSub, newSub, exp);
		}
		return super.store(ctx, obj);
	}

	/**
	 * @param ctx
	 * @param oldSub
	 * @param newSub
	 * @param exp
	 */
	abstract protected void onUpdateError(Context ctx, Subscriber oldSub, Subscriber newSub, Exception exp);
	
	/**
	 * for better report the name of this operation
	 * @return
	 */
	abstract protected  String getOperationMsg();
	

	/**
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	abstract protected  void updateServiceParameter(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException, ProvisioningHomeException;

	/**
	 * @param oldSub
	 * @param newSub
	 * @return
	 */
	abstract protected  boolean parameterEquals(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException;

	/**
	 * @param ctx
	 * @param newSub
	 * @return
	 * @throws HomeException
	 */
	abstract protected  boolean hasServiceJustProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException;
	
	abstract protected  boolean hasServiceProvisioned(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException;
	
	abstract protected boolean isProvisionOk(Context ctx, Subscriber oldSub, Subscriber newSub) throws HomeException;
	
	    
}
