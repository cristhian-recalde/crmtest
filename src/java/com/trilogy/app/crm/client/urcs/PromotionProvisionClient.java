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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.client.RemoteServiceException;
import com.trilogy.app.urcs.promotion.PromotionStatus;

/**
 * CRM view of the URCS PromotionProvision interface.
 *
 * @author victor.stratan@redknee.com
 */
public interface PromotionProvisionClient
{
    /**
     * Set or remove subscription Promotions.
     *
     * @param ctx the operating context
     * @param subscription for which subscription to make the query
     * @param addOptions array of promotion IDs to add
     * @param removeOptions array of promotion IDs to remove
     * @throws RemoteServiceException thrown if received error result code
     */
    void setSubscriptionPromotions(Context ctx, Subscriber subscription, long[] addOptions, final long[] removeOptions)
        throws RemoteServiceException;

    /**
     * List all valid promotion IDs for subscription.
     *
     * @param ctx the operating context
     * @param subscription for which subscription to make the query
     * @return an array of IDs
     * @throws RemoteServiceException thrown if received error result code
     */
    long[] listSubscriptionPromotions(Context ctx, Subscriber subscription) throws RemoteServiceException;

    /**
     * List all valid promotion and their status for a subscription.
     *
     * @param ctx the operating context
     * @param subscription for which subscription to make the query
     * @return an array of PromotionStatus object belonging to the subscription
     * @throws RemoteServiceException thrown if received error result code
     */
    PromotionStatus[] listSubscriptionPromotionStatus(Context ctx, Subscriber subscription)
        throws RemoteServiceException;

    /**
     * Retrieve promotion status for a subscription and a particular promotion.
     *
     * @param ctx the operating context
     * @param subscription for which subscription to make the query
     * @param promotionId the ID of the promotion for which to make the query
     * @return a PromotionStatus object belonging to the subscription
     * @throws RemoteServiceException thrown if received error result code
     */
    PromotionStatus getSubscriptionPromotionStatus(Context ctx, Subscriber subscription, long promotionId)
        throws RemoteServiceException;
}
