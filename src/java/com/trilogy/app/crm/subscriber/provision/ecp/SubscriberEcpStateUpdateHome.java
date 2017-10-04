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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;

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
public  class SubscriberEcpStateUpdateHome extends SubscriberVoiceServiceParameterUpdateHome
{

	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberEcpStateUpdateHome(Context ctx, Home delegate) 
	{
		super(ctx, delegate);
	}
	
	@Override
    protected String getOperationMsg()
	{
		return "Subscription State";
	}
	
	/**
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	@Override
    protected void updateServiceParameter(Context ctx, Subscriber oldSub, Subscriber newSub) throws ProvisioningHomeException {
		updateECPState(ctx, newSub);
	}

	/**
	 * @param oldSub
	 * @param newSub
	 * @return
	 */
	@Override
    protected boolean parameterEquals(Context ctx, Subscriber oldSub, Subscriber newSub) 
	{
        boolean parameterEqual = true;
        if ( ! EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub) ) 
        {
            parameterEqual = false;
            if ( newSub.getSubscriberType() == SubscriberTypeEnum.PREPAID ) 
            {
                int [] prepaidLeavingStates = {
                    SubscriberStateEnum.AVAILABLE_INDEX,
                    SubscriberStateEnum.LOCKED_INDEX
                };

                /*
                 * For prepaid sub, we only need to update ecp if sub state changed to 
                 * Barred/Locked or Deactivated
                 * OR sub state goes to Active  from either Barred/Locked or Available state
                 */
                if ( (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.LOCKED )) && 
                   (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.INACTIVE )) && 
                   (! EnumStateSupportHelper.get(ctx).isTransition(oldSub,newSub,prepaidLeavingStates,
                   SubscriberStateEnum.ACTIVE_INDEX)))
                {
                    parameterEqual = true;
                }
            }
        }

        return parameterEqual;
    }
	
	/**
	 * Checks if parameters for this update are changed. If they are it will place a marker for
	 * the SubscriberEcpProfileUpdateHome.
	 * 
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	private void updateECPState(Context ctx,  final Subscriber newSub)
		throws ProvisioningHomeException
	{
	    ctx.put(Lookup.ECPPROFILE_NEWSTATE, AppEcpClientSupport.mapToEcpState(ctx, newSub) );
	}
	
    
}
