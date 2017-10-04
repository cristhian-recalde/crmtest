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
package com.trilogy.app.crm.subscriber.state.postpaid;

import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;


/**
 * Provides a number of utility functions for use with subscriber state transitions.
 *
 * @author jimmy.ng@redknee.com
 */
public final class PostpaidSubscriberStateTransitionSupport extends SubscriberStateTransitionSupport
{

    /**
     * Create a new instance of <code>PostpaidSubscriberStateTransitionSupport</code>.
     */
    protected PostpaidSubscriberStateTransitionSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>PostpaidSubscriberStateTransitionSupport</code>.
     *
     * @return An instance of <code>PostpaidSubscriberStateTransitionSupport</code>.
     */
    public static PostpaidSubscriberStateTransitionSupport instance()
    {
        if (instance == null)
        {
            instance = new PostpaidSubscriberStateTransitionSupport();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractEnumStateAware getEnumStateSupport()
    {
        return this.enumStateSupport_;
    }

    /**
     * Singleton instance.
     */
    private static PostpaidSubscriberStateTransitionSupport instance;

    /**
     * Enum state support.
     */
    private final AbstractEnumStateAware enumStateSupport_ = PostpaidSubscriberStateSupport.instance();

} // class
