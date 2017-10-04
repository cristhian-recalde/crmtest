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

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.promotion.Promotion;
import com.trilogy.app.urcs.promotion.CounterProfile;
import com.trilogy.app.urcs.promotion.Counter;

/**
 * CRM view of the URCS PromotionProvision interface.
 *
 * @author victor.stratan@redknee.com
 * 
 * Support clustered corba client
 * Refactored reusable elements to an abstract class AbstractCrmClient<T>
 * @author rchen
 * @since June 24, 2009 
 */
public interface PromotionManagementClient
{
    /* Retrieve all prviate promtoions */
    Promotion[] listPromotions(Context ctx, int spid) throws RemoteServiceException;

    /* Create Promotion */
    void createPromotion(Context ctx, Promotion promotion);

    /* Update Existing Promotion */
    void updatePromotion(Context ctx, Promotion promotion);

    /* Retrieve Existing Promotion */
    Promotion retrievePromotion(Context ctx, long promotionId);

    /* Delete Existing Promotion */
    void deletePromotion(Context ctx, long promotionId);

    /* Create Counter Profile */
    void createCounterProfile(Context ctx, CounterProfile counterProfile);

    /* Update Existing Counter Profile */
    void updateCounterProfile(Context ctx, CounterProfile counterProfile);

    /* Retrieve Existing Counter Profile */
    CounterProfile retrieveCounterProfile(Context ctx, long profileId);

    /* Delete Counter Profile */
    void deleteCounterProfile(Context ctx, long profileId);

    /* Instantiate Counter */
    void createCounter(Context ctx, String msisdn, long subscriptionType, long profileId, Counter counter);

    /* Update existing Counter.  */
    void upateCounter(Context ctx, Counter counter);

    /* Update Mulitple counters with delta value */
    Counter[] updateCounters(Context ctx, com.redknee.app.urcs.promotion.CounterDelta[] deltas);

    /* Retrieve Existing Counter */
    int retrieveCounter(Context ctx, String msisdn, long subscriptionType);

    /* Retrieve Existing Counter */
    int retrieveCounter2(Context ctx, long counterId);

    /* Delete Exisitn Counter */
    void deleteCounter(Context ctx, String msisdn, long subscriptionType);

    /* Delete Exisitn Counter */
    void deleteCounter2(Context ctx, long counterId);
}