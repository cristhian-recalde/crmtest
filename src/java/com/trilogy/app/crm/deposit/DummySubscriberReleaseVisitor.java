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

package com.trilogy.app.crm.deposit;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * A dummy visitor for testing purposes.
 *
 * @author cindy.wong@redknee.com
 */
public class DummySubscriberReleaseVisitor extends SubscriberReleaseVisitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4313253321349001940L;

    /**
     * {@inheritDoc}
     */
    @Override
    public final SubscriberReleaseVisitor prototype()
    {
        return new DummySubscriberReleaseVisitor();
    }

    /**
     * Upon each call, prints the visit details to debug log.
     *
     * @param context
     *            The operating context.
     * @param object
     *            The subscriber being visited.
     * @throws AgentException
     *             Thrown if there are problems.
     * @see com.redknee.app.crm.deposit.SubscriberReleaseVisitor#visit(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    public final void visit(final Context context, final Object object) throws AgentException
    {
        final Subscriber subscriber = (Subscriber) object;
        if (!isInitialized())
        {
            final AbortVisitException exception = new AbortVisitException("Visitor has not been initialized");
            new DebugLogMsg(this, exception.getMessage(), exception).log(context);
            throw exception;
        }

        // step 1: determines if this subscriber is eligible
        final CRMSpid serviceProvider = getServiceProvider(context);

        // subscriber is eligible
        if (validate(context, subscriber, serviceProvider, getSpidCriteria(), getActiveDate()))
        {
            incrementNumVisits();
            new DebugLogMsg(this, "Subscriber ID=" + subscriber.getId() + " deposit=" + subscriber.getDeposit(context)
                + " nextDepositReleaseDate=" + subscriber.getNextDepositReleaseDate(), null).log(context);
        }
        else
        {
            new DebugLogMsg(this, "Subscriber " + subscriber.getId() + " not eligible", null).log(context);
        }
    }
}
