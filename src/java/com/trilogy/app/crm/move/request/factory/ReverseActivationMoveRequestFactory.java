/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.move.request.factory;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.ReverseActivationMoveRequest;
import com.trilogy.framework.xhome.context.Context;


/**
 * Creates an instance of a Reverse Activation MoveRequest from a given subscription.
 * 
 * If given subscription is null, then a default ReverseActivationMoveRequest is created.
 *
 * @author Mangaraj Sahoo
 * @since 9.2
 */
class ReverseActivationMoveRequestFactory
{

    static ReverseActivationMoveRequest getInstance(Context ctx, Subscriber subscription)
    {
        ReverseActivationMoveRequest request = new ReverseActivationMoveRequest();
        
        if (subscription != null)
        {
            request.setOldSubscriptionId(subscription);
        }

        /*
         * Set the Subscriber ID to a unique temporary 'unset' value so that it will be set during SubscriberHome.create()
         * Hash code is used because it is unique and DEFAULT_MOVE_PREFIX + hashCode < Subscriber.ID_WIDTH
         */
        request.setNewSubscriptionId(MoveConstants.DEFAULT_MOVE_PREFIX + request.hashCode());
        
        return request;
    }
}
