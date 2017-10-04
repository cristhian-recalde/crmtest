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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.subscriber.state.SubscriberStateTransitionSupport;


/**
 * Some subscriber states are only valid for certain types. This validator checks that we
 * are not creating or storing a Subscriber with an invalid state for its type. Note: this
 * validator only validate whether the state transition is permitted programmatically
 * (e.g., dunning states). Further validation is required for manual state transitions.
 *
 * @author paul.sperneac@redknee.com
 * @author cindy.wong@redknee.com
 */
public final class SubscriberStateTypeValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberStateTypeValidator</code>.
     */
    protected SubscriberStateTypeValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberStateTypeValidator</code>.
     *
     * @return An instance of <code>SubscriberStateTypeValidator</code>.
     */
    public static SubscriberStateTypeValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriberStateTypeValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        final Subscriber sub = (Subscriber) obj;

        /*
         * [Cindy Wong] In addition to just verifying prepaid subscribers (true or hybrid)
         * are not allowed to be in payment plan states, generalize and use the EnumState
         * and EnumStateTransitionSupport subsystem to handle state transition validation.
         */
        if (!SubscriberStateTransitionSupport.instance(ctx, sub).isStateValid(ctx, sub.getState()))
        {
            final StringBuilder sb = new StringBuilder();
            sb.append("Subscriber ");
            sb.append(sub.getId());
            sb.append(" is not permitted to be in ");
            sb.append(sub.getState());
            sb.append(" state");
            throw new IllegalPropertyArgumentException(SubscriberXInfo.STATE, sb.toString());
        }
    }

    /**
     * Singleton instance.
     */
    private static SubscriberStateTypeValidator instance;
}
