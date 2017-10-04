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
import com.trilogy.framework.xhome.xenum.EnumCollection;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.state.AbstractEnumStateAware;
import com.trilogy.app.crm.state.EnumStateTransitionSupport;
import com.trilogy.app.crm.state.StateAware;
import com.trilogy.app.crm.subscriber.state.postpaid.PostpaidSubscriberStateTransitionSupport;
import com.trilogy.app.crm.subscriber.state.prepaid.PrepaidSubscriberStateTransitionSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;


/**
 * Provides a number of utility functions for use with subscriber state transitions.
 *
 * @author joe.chen@redknee.com
 * @deprecated Use {@link SubscriberStateTransitionSupport} instead.
 */
@Deprecated
public class ConvergeSubscriberStateTransitionSupport extends EnumStateTransitionSupport
{

    /**
     * Create a new instance of <code>ConvergeSubscriberStateTransitionSupport</code>.
     *
     * @param ctx
     *            The operating context.
     * @param sub
     *            Subscriber.
     * @deprecated Use
     *             {@link SubscriberStateTransitionSupport#instance(Context, Subscriber)}
     *             instead.
     */
    @Deprecated
    public ConvergeSubscriberStateTransitionSupport(final Context ctx, final Subscriber sub)
    {
        this(ctx, sub.getSubscriberType().getIndex());
    }


    /**
     * Create a new instance of <code>ConvergeSubscriberStateTransitionSupport</code>.
     *
     * @param ctx
     *            The operating context.
     * @param subType
     *            Subscriber type.
     * @deprecated Use
     *             {@link SubscriberStateTransitionSupport#instance(Context, SubscriberTypeEnum)}
     *             instead.
     */
    @Deprecated
    protected ConvergeSubscriberStateTransitionSupport(final Context ctx, final int subType)
    {
        if (subType == SubscriberTypeEnum.PREPAID_INDEX)
        {
            delegate_ = PrepaidSubscriberStateTransitionSupport.instance();
        }
        else
        {
            delegate_ = PostpaidSubscriberStateTransitionSupport.instance();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EnumCollection getPossibleManualStateEnumCollection(final Context ctx, final StateAware stateOwner)
    {
        return delegate_.getPossibleManualStateEnumCollection(ctx, stateOwner);
    }


    /**
     * Should never been called.
     */
    @Override
    public AbstractEnumStateAware getEnumStateSupport()
    {
        // return delegate_.get;
        return null;
    }

    /**
     * Subscriber type.
     */
    int subType_ = -1;

    /**
     * Delegate.
     */
    EnumStateTransitionSupport delegate_ = null;

    /**
     * State support.
     */
    EnumStateSupportHelper stateSupport_ = null;

} // class
