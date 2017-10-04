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
package com.trilogy.app.crm.subscriber.state;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.state.EnumStateTransitionSupport;
import com.trilogy.app.crm.subscriber.state.hybrid.HybridPrepaidSubscriberStateTransitionSupport;
import com.trilogy.app.crm.subscriber.state.postpaid.PostpaidSubscriberStateTransitionSupport;
import com.trilogy.app.crm.subscriber.state.prepaid.PrepaidSubscriberStateTransitionSupport;
import com.trilogy.app.crm.support.SystemSupport;


/**
 * Provides a number of utility functions for use with subscriber state transitions.
 *
 * @author joe.chen@redknee.com
 */
public abstract class SubscriberStateTransitionSupport extends EnumStateTransitionSupport
{

    /**
     * Returns an instance of <code>SubscriberStateTransitionSupport</code>
     * corresponding to the subscriber's type.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber to look up a state transition support instance for.
     * @return An instance of <code>SubscriberStateTransitionSupport</code>.
     */
    public static SubscriberStateTransitionSupport instance(final Context ctx, final Subscriber sub)
    {
        return instance(ctx, sub.getSubscriberType());
    }


    /**
     * Returns an instance of <code>ConvergeSubscriberStateTransitionSupport</code>
     * corresponding to the subscriber type.
     *
     * @param ctx
     *            The operating context.
     * @param subscriberType
     *            Subcriber type to look up a state transition support instance for.
     * @return An instance of <code>ConvergeSubscriberStateTransitionSupport</code>.
     */
    public static SubscriberStateTransitionSupport instance(final Context ctx, final SubscriberTypeEnum subscriberType)
    {
        SubscriberStateTransitionSupport result = null;
        if (SubscriberTypeEnum.PREPAID.equals(subscriberType))
        {
            result = PrepaidSubscriberStateTransitionSupport.instance();
        }
        else if (SubscriberTypeEnum.POSTPAID.equals(subscriberType))
        {
            result = PostpaidSubscriberStateTransitionSupport.instance();
        }
        return result;
    }

} // class
