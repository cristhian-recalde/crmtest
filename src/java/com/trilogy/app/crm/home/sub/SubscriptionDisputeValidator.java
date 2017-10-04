/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.home.sub;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeOperationEnum;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.exception.RethrowExceptionListener;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.transfer.TransferDisputeTransactionSupport;


/**
 * Some subscriber states are only valid for certain types. This validator checks that we
 * are not creating or storing a Subscriber with an invalid state for its type. Note: this
 * validator only validate whether the state transition is permitted programmatically
 * (e.g., dormant states). Further validation is required for manual state transitions.
 * 
 * @author simar.singh@redknee.com
 */
public final class SubscriptionDisputeValidator implements Validator
{

    /**
     * Create a new instance of <code>SubscriberStateTypeValidator</code>.
     */
    protected SubscriptionDisputeValidator()
    {
        // empty
    }


    /**
     * Returns an instance of <code>SubscriberStateTypeValidator</code>.
     * 
     * @return An instance of <code>SubscriberStateTypeValidator</code>.
     */
    public static SubscriptionDisputeValidator instance()
    {
        if (instance == null)
        {
            instance = new SubscriptionDisputeValidator();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx, final Object obj)
    {
        if (!HomeOperationEnum.STORE.equals(ctx.get(HomeOperationEnum.class)))
        {
            // validating only store() operations
            return;
        }
        final Subscriber sub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final RethrowExceptionListener exceptions = new RethrowExceptionListener();
        if (oldSub!=null && EnumStateSupportHelper.get(ctx).isOneOfStates(sub, INDISPUTABLESTATES_STATES)
                && EnumStateSupportHelper.get(ctx).isNotOneOfStates(oldSub, INDISPUTABLESTATES_STATES))
        {
            try
            {
                if (TransferDisputeTransactionSupport.isSubscriberInDispute(ctx, sub.getId()))
                {
                    final IllegalPropertyArgumentException ex;
                    ex = new IllegalPropertyArgumentException(SubscriberXInfo.STATE,
                            "All disputes against the Subscription must be settled before this state change.");
                    exceptions.thrown(ex);
                }
            }
            catch (HomeException e)
            {
                final IllegalStateException ex;
                ex = new IllegalStateException(
                        "Unable to check for disputes. All disputes against the Subscription must be settled before this state change",
                        e);
                exceptions.thrown(ex);
            }
        }
        exceptions.throwAllAsCompoundException();
    }

    /**
     * Singleton instance.
     */
    private static final Collection<SubscriberStateEnum> INDISPUTABLESTATES_STATES = Collections
            .unmodifiableSet(new HashSet<SubscriberStateEnum>(Arrays.asList(SubscriberStateEnum.PENDING,
                    SubscriberStateEnum.INACTIVE, SubscriberStateEnum.DORMANT)));
    private static SubscriptionDisputeValidator instance;
}