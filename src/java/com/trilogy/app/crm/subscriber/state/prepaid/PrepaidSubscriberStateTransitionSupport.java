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
package com.trilogy.app.crm.subscriber.state.prepaid;

import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;


/**
 * Provides a number of utility functions for use with subscriber state transitions.
 *
 * @author jimmy.ng@redknee.com
 */
public final class PrepaidSubscriberStateTransitionSupport extends SubscriberStateTransitionSupport
{

    /**
     * Create a new instance of <code>PrepaidSubscriberStateTransitionSupport</code>.
     */
    protected PrepaidSubscriberStateTransitionSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>PrepaidSubscriberStateTransitionSupport</code>.
     *
     * @return An instance of <code>PrepaidSubscriberStateTransitionSupport</code>.
     */
    public static PrepaidSubscriberStateTransitionSupport instance()
    {
        if (instance == null)
        {
            instance = new PrepaidSubscriberStateTransitionSupport();
        }
        return instance;
    }

    /**
     * Singleton instance.
     */
    private static PrepaidSubscriberStateTransitionSupport instance;


    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractEnumStateAware getEnumStateSupport()
    {
        return this.enumStateSupport_;
    }

    /**
     * Enum state support.
     */
    private final AbstractEnumStateAware enumStateSupport_ = PrepaidSubscriberStateSupport.instance();

} // class
