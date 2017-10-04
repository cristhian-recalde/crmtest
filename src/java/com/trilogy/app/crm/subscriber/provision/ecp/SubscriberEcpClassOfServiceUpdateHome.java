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
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.AppEcpClientSupport;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.config.AppEcpClientConfig;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * @author danny.ng@redknee.com
 */
public class SubscriberEcpClassOfServiceUpdateHome extends
		SubscriberVoiceServiceParameterUpdateHome {

	/**
	 * @param ctx
	 * @param delegate
	 */
	public SubscriberEcpClassOfServiceUpdateHome(Context ctx, Home delegate) {
		super(ctx, delegate);
	}

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome#getOperationMsg()
	 */
	protected String getOperationMsg() {
		return "Class Of Service";
	}

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome#updateServiceParameter(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.bean.Subscriber)
	 */
	protected void updateServiceParameter(Context ctx, Subscriber oldSub,
			Subscriber newSub) throws HomeException, ProvisioningHomeException {
		updateClassOfService(ctx, newSub);

	}

	/* (non-Javadoc)
	 * @see com.redknee.app.crm.subscriber.provision.SubscriberServiceParameterUpdateHome#parameterEquals(com.redknee.framework.xhome.context.Context, com.redknee.app.crm.bean.Subscriber, com.redknee.app.crm.bean.Subscriber)
	 */
	protected boolean parameterEquals(Context ctx, Subscriber oldSub,
			Subscriber newSub) throws ProvisioningHomeException {
		if (oldSub != null && newSub!= null)
		{
			AppEcpClientConfig config = null;
			config = (AppEcpClientConfig)ctx.get(AppEcpClientConfig.class);
			if (config == null)
			{
				throw new ProvisioningHomeException(
						"Provisioning Error: Could not find ECP Configuration.",
						3006, 
						Common.OM_ECP_ERROR);
			}
			
			
			Account newSubAccount = null;
			Account oldSubAccount = null;
			
			// If the subscribers have same account number, fetch the account from the context
			if (SafetyUtil.safeEquals(newSub.getBAN(), oldSub.getBAN()))
			{
				oldSubAccount = (Account)ctx.get(Account.class);
				newSubAccount = oldSubAccount;
			}
			else
			{
				// If the subscribers have different account numbers, fetch the account from the database
				try {
					newSubAccount = (Account)ctx.get(Account.class);
					oldSubAccount = (Account)oldSub.getAccount(ctx);
				} catch (HomeException exception) {
					throw new ProvisioningHomeException(
							"Unable to fetch subscription's account",
							3006, 
							Common.OM_ECP_ERROR, exception);
				}
			}
			
			
			// Fetch subscriber class of service
			int newSubClassOfService = 0;
			int oldSubClassOfService = 0;
	        try
	        {
	        	newSubClassOfService
	                = config.getClassOfService(
	                       ctx,
	                       newSubAccount.getSpid(),
	                       newSubAccount.getType(),
	                       newSub.getSubscriberType());
	        	
	        	oldSubClassOfService
		            = config.getClassOfService(
		                   ctx,
		                   oldSubAccount.getSpid(),
		                   oldSubAccount.getType(),
		                   oldSub.getSubscriberType());
	        }
	        catch (HomeException e)
	        {
				throw new ProvisioningHomeException(
						"Provisioning Error: Could not fetch subscriber class of service.",
						3006, 
						Common.OM_ECP_ERROR, e);
	        }
	        
	        // Compare the new and old class of service
	        return newSubClassOfService == oldSubClassOfService;
		} // oldSub and newSub not null
        return false;
	}

	/**
	 * Places a marker for SubscriberEcpProfileUpdateHome to do the update.
	 * 
	 * @param ctx
	 * @param newSub
	 * @throws ProvisioningHomeException
	 */
	private void updateClassOfService(Context ctx, Subscriber newSub) throws ProvisioningHomeException
	{        
        try
        {
            ctx.put(Lookup.ECPPROFILE_NEWCOS, AppEcpClientSupport.getClassOfService(ctx, newSub) );
        }
        catch(HomeException e)
        {
            throw new ProvisioningHomeException(
                    "Unable to retrieve account's class of service",
                3006, Common.OM_ECP_ERROR, e);
        }
        /*
		int result = 0;
		try {
			result = AppEcpClientSupport.updateClassOfService(ctx, newSub);
		} catch (AgentException e) {
			throw new ProvisioningHomeException(
					"provisioning result 3006: failed to update ECP subscriber class of service ["
					+ result
					+ "], sub=" + newSub.getId() + " " + e.getMessage(),
				3006, Common.OM_ECP_ERROR);
		} catch (HomeException e) {
			throw new ProvisioningHomeException(
					"provisioning result 3006: failed to update ECP subscriber class of service ["
					+ result
					+ "], sub=" + newSub.getId() + " " + e.getMessage(),
				3006, Common.OM_ECP_ERROR);
		}
		
		if (result != 0)
		{
			throw new ProvisioningHomeException(
					"provisioning result 3006: failed to update ECP subscriber class of service ["
					+ result
					+ "], sub=" + newSub.getId(),
				3006, Common.OM_ECP_ERROR);
		}
        */
	}
}
