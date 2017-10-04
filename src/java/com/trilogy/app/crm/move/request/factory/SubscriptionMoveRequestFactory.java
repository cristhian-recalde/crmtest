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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionType;
import com.trilogy.app.crm.move.MoveConstants;
import com.trilogy.app.crm.move.request.ServiceBasedSubscriptionMoveRequest;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;


/**
 * Creates an appropriate instance of an SubscriptionMoveRequest from a given subscription.
 * 
 * If given subscription is null, then a default SubscriptionMoveRequest is created.
 *
 * @author Aaron Gourley
 * @since 8.1
 */
class SubscriptionMoveRequestFactory
{

    static SubscriptionMoveRequest getInstance(Context ctx, Subscriber subscription)
    {
        SubscriptionMoveRequest request = null;

        if (subscription != null)
        {
            try
            {
                SubscriptionType type = subscription.getSubscriptionType(ctx);
                if (type == null)
                {
                    new InfoLogMsg(SubscriptionMoveRequestFactory.class, 
                            "Subscription type " + subscription.getSubscriptionType()
                            + " for subscription " + subscription.getId() + ".", null).log(ctx);
                }
                else if (type.isService())
                {
                    ServiceBasedSubscriptionMoveRequest serviceBasedRequest = new ServiceBasedSubscriptionMoveRequest();
                    serviceBasedRequest.setNewDepositAmount(subscription.getDeposit(ctx));
                    request = serviceBasedRequest;
                }
            }
            catch (Throwable e)
            {
                new InfoLogMsg(SubscriptionMoveRequestFactory.class, 
                        "Error looking up subscription type " + subscription.getSubscriptionType()
                        + " for subscription " + subscription.getId() + ".", e).log(ctx);
            }
        }
        
        if (request == null)
        {
            request = new SubscriptionMoveRequest();
        }
        
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
