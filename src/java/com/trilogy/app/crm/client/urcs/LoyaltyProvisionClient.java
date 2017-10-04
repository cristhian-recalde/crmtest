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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;

/**
 * CRM view of URCS Loyalty Provision interface.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public interface LoyaltyProvisionClient
{
    String version ();
    
    /**
     * Creates a loyalty profile on URCS. The subscriber account must already exist on URCS.
     * 
     * @param ctx
     * @param profile This consist of all the relevant information required for registration of loyalty profile.
     * @return  copy of loyalty profile
     * @throws RemoteServiceException
     */
    com.redknee.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile createLoyaltyProfile(Context ctx, com.redknee.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile profile) throws RemoteServiceException;

    /**
     * Retrieves loyalty profile on URCS using the subscriber account id.
     * 
     * @param ctx
     * @param ban       Account ID or BAN, null if cardId is set
     * @param cardId    Loyalty Card ID, null if ban is set
     * @return copy of loyalty profile, null if none exists
     * @throws RemoteServiceException
     */
    com.redknee.product.bundle.manager.provision.v4_0.loyalty.LoyaltyCardProfile getLoyaltyProfile(Context ctx, String ban, String cardId, String programId) throws RemoteServiceException;
    
    
    /**
     * Updates loyalty profile on URCS. Individual arguments can be set to 'null' for no change.
     * 
     * @param ctx
     * @param ban       reference by Account ID or BAN, set to null if cardId is set
     * @param cardId    reference by Loyalty Card ID, set to null if ban is set
     * @param programId                 If not null, will be updated
     * @param enableAccumulation        If not null, will be updated
     * @param enableRedemption          If not null, will be updated
     * @throws RemoteServiceException
     */
    void updateLoyaltyProfile(Context ctx, String ban, String cardId, String programId, Boolean enableAccumulation, Boolean enableRedemption) throws RemoteServiceException;

}
