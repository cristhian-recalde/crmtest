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

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.state.EnumState;


/**
 * Provides a number of utility functions for use with subscriber states.
 *
 * @author gary.anderson@redknee.com
 */
public final class PrepaidSubscriberStateSupport extends AbstractEnumStateAware
{

    /**
     * Create a new instance of <code>PrepaidSubscriberStateSupport</code>.
     */
    protected PrepaidSubscriberStateSupport()
    {
        // empty
    }


    /**
     * Returns an instance of <code>PrepaidSubscriberStateSupport</code>.
     *
     * @return An instance of <code>PrepaidSubscriberStateSupport</code>.
     */
    public static PrepaidSubscriberStateSupport instance()
    {
        if (instance == null)
        {
            instance = new PrepaidSubscriberStateSupport();
        }
        return instance;
    }

    /**
     * Singleton instance.
     */
    private static PrepaidSubscriberStateSupport instance;


    /**
     * {@inheritDoc}
     */
    @Override
    protected EnumCollection getEnumCollection()
    {
        return SubscriberStateEnum.COLLECTION;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumState getState(final Context context, final Enum type)
    {
        if (type == null)
        {
            return PrepaidNullSubscriberState.instance();
        }

        return findStateInstance(type, PREPAID_SUBSCRIBER_STATES);
    }

    /**
     * Valid prepaid subscriber states.
     */
    private static final Map PREPAID_SUBSCRIBER_STATES = new HashMap();

    /*
     * Java refresh course: the following is a class initialization block, which gets
     * executed once when the class is loaded.
     */
    {
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.INACTIVE, PrepaidInactiveSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.PENDING, PrepaidPendingSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.AVAILABLE, PrepaidAvailableSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.ACTIVE, PrepaidActiveSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.LOCKED, PrepaidLockedSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.SUSPENDED, PrepaidSuspendedSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.EXPIRED, PrepaidExpiredSubscriberState.instance());
        PREPAID_SUBSCRIBER_STATES.put(SubscriberStateEnum.DORMANT, PrepaidDormantSubscriberState.instance());
    }

} // class
