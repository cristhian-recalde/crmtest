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
package com.trilogy.app.crm.client.alcatel;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;

/**
 * The Alcatel Provisioning interface is used to create/update/delete subscription account 
 * profiles on the Alcatel SSC and modify the service state.  
 * 
 * @author angie.li@redknee.com
 *
 */
public interface AlcatelProvisioning 
{
	/**
	 * Creates the profile on the Alcatel SSC so that the subscriber can have an active broadband service
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void createService(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Disables the given broadband service for the given subscriber on the Alcatel SSC. 
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void removeService(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Suspends the broadband service on the Alcatel SSC
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void suspendService(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Resume the suspended broadband Service on the Alcatel SSC
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void resumeService(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	
	/**
     * Creates the Account from the Alcatel SSC with a blocked Service Package
     * 
     * @param ctx
     * @param service
     * @param subscriber
     * @throws AlcatelProvisioningException
     */
	public void createAccount(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Deletes the Account from the Alcatel SSC 
	 * (and consequently the Users that are associated with this account).
	 * 
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void deleteAccount(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Update all non-service related parameters of the profile on the Alcatel SSC.
	 * For example, all contact information.  This command should avoid altering the passwords to 
	 * the Alcatel SSC profile. 
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public void updateAccount(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	
	/**
     * Return the (daily) Account Usage for the given subscription.
     * @param ctx
     * @param service
     * @param subscriber
     * @throws AlcatelProvisioningException
     */
    public AccountInfo getAccount(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
	/**
	 * Return the (daily) Account Usage for the given subscription.
	 * @param ctx
	 * @param service
	 * @param subscriber
	 * @throws AlcatelProvisioningException
	 */
	public AccountUsage queryAccountUsage(Context ctx, final Service service, final Subscriber subscriber) throws AlcatelProvisioningException;
	
    // Maps the error to one of the source functions
    public final int ERROR_TRANSLATOR_NOT_FOUND = 10;
    public final int ERROR_DATABASE_ERROR = 11;
    public final int ERROR_UNKNOWN_REQUEST = 12;
    public final int ERROR_UNKNOWN_RESPONSE = 12;
    public final int ERROR_UNREADABLE_DATA = 13;
    public final int ERROR_INVALID_RESPONSE_CODE = 14;
    public final int ERROR_INVALID_RESPONSE_OBJECT = 15;
    public final int ERROR_HLR = 16;
}
