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

import java.util.Collection;

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.framework.xhome.context.Context;

/**
 * CRM view of URCS Promotion Management interface.
 * 
 * @author asim.mahmood@redknee.com
 * @since 8.5
 */
public interface PromotionManagementClientV2
{
    String version ();
    
    Collection<com.redknee.app.urcs.promotion.v2_0.Promotion> listAllPromotionsForSpid (Context ctx, int spid, com.redknee.app.urcs.promotion.v2_0.PromotionType type)  throws RemoteServiceException;
    
    com.redknee.app.urcs.promotion.v2_0.Promotion retrievePromotion (Context ctx, long promotionId) throws RemoteServiceException;

    com.redknee.app.urcs.promotion.v2_0.CounterProfile retrieveCounterProfile (Context ctx, long profileId) throws RemoteServiceException;

    com.redknee.app.urcs.promotion.v2_0.Counter retrieveCounterForSub (Context ctx, String msisdn, int subscriptionType, long counterProfileID) throws RemoteServiceException;

    Collection<com.redknee.app.urcs.promotion.v2_0.Counter> retrieveAllCountersForSub (Context ctx, String msisdn, int subscriptionType) throws RemoteServiceException;

    Collection<com.redknee.app.urcs.promotion.v2_0.Counter> updateCounters (Context ctx, String msisdn, int subscriptionType, Collection<com.redknee.app.urcs.promotion.v2_0.CounterDelta> deltas) throws RemoteServiceException;

    void deleteCounter (Context ctx, String msisdn, int subscriptionType, long counterProfileID) throws RemoteServiceException;

    void  deleteAllCountersForSub (Context ctx, String msisdn, int subscriptionType) throws RemoteServiceException;

    com.redknee.app.urcs.promotion.v2_0.CounterProfile createSystemCounter (Context ctx, com.redknee.app.urcs.promotion.v2_0.CounterProfile counterProfile) throws RemoteServiceException;

    void setSubscriberOptions (Context ctx, com.redknee.app.urcs.promotion.v2_0.SubscriberIdentity subscriberId, Collection<Long> addOptions, Collection<Long> removeOptions) throws RemoteServiceException;
    
    com.redknee.app.urcs.promotion.v2_0.Promotion retrievePromotionWithSPID (Context ctx, int spid, long promotionId) throws RemoteServiceException;

}
