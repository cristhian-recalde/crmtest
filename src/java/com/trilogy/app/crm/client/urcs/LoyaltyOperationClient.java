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
 * CRM view of URCS Loyalty Operation interface.
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public interface LoyaltyOperationClient
{
    String version ();
    
    LoyaltyParameters redeemLoyaltyPoints (Context ctx, String ban, String cardId, String programId, String extTransactionId, Integer sourceType, long points, String userId, String userNote, String userLocation) throws RemoteServiceException;
    
    LoyaltyParameters adjustLoyaltyPoints (Context ctx, String ban, String cardId, String programId, String extTransactionId, Integer sourceType, long points, String userId, String userNote, String userLocation) throws RemoteServiceException;
    
    LoyaltyParameters accumulateLoyaltyPoints (Context ctx, String ban, String cardId, String programId, String extTransactionId, Integer sourceType, long points, String userId, String userNote, String userLocation) throws RemoteServiceException;

    long queryLoyaltyPoints (Context ctx, String ban, String cardId, String programId) throws RemoteServiceException;    

    /**
     * Provides conversion method between points, currency amount and voucher.
     * 
     * @param ctx
     * @param ban               reference by Account Id, or null if cardId is set
     * @param cardId            reference by Loyalty Card Id, or null if ban is set
     * @param programId TODO
     * @param redemptionType    Payment, Transfer or Voucher
     * @param partnerId         required field
     * @param voucherType       required field
     * @param points            number of points to use
     * @param amount            currency amount to use
     * @return
     */
    LoyaltyParameters convertLoyaltyPoints (Context ctx, String ban, String cardId, 
            String programId, long redemptionType, String partnerId, 
            int voucherType, Long points, Long amount)  throws RemoteServiceException;
}
