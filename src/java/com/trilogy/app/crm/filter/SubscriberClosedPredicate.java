/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.filter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.state.FinalStateAware;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * This predicate returns true if the given state or the Subscriber is 'final'
 * aka closed.
 * 
 * @author simar.singh@redknee.com
 * @since 8.2
 */
public class SubscriberClosedPredicate implements Predicate
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @{inheritDoc}
     *
     * TODO - This is a temporary solution for ticket 9022600013 (wallet)
     * A full solution will follow will closed state gets well defined for airtime also
     */
    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        if (obj instanceof Subscriber)
        {
            Subscriber sub = (Subscriber) obj;
            /*
             * Transient state may be final but irrespective of the persistent state Check
             * the state of the Subscriber in the store
             */
            try
            {
                sub = SubscriberSupport.lookupSubscriberForSubId(ctx, sub.getId());
                if (sub != null)
                {
                    return sub.isInFinalState();
                }
            }
            catch (HomeException e)
            {
                new MinorLogMsg(this, "Error fetching subscriber: " + sub.getId(), null).log(ctx);
            }
        }
        else if (obj instanceof FinalStateAware)
        {
            return ((FinalStateAware) obj).isInFinalState();
        }
        return false;
    }
}
