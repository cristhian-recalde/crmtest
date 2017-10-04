/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee, no
 * unauthorised use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the licence agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.function;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * Predicate that returns true if the object is a pool group leader subscriber
 * 
 * @author simar.singh@redknee.com
 */
public class IsSubscriberPoolLeaderPredicate implements Predicate
{

    private static final long serialVersionUID = 1L;


    /**
     * {@inheritDoc}
     */
    public boolean f(final Context ctx, final Object obj)
    {
        if (null == obj)
        {
            handleError(ctx, "Precicate Check Skipped. Testifying an null object but expected an instanc of type ["
                    + Subscriber.class.getName() + "]");
        }
        if (obj instanceof Subscriber)
        {
            return ((Subscriber) obj).isPooledGroupLeader(ctx);
        }
        else
        {
            handleError(ctx, "Precicate Check Skipped. Testifying an object of type [" + obj.getClass().getName()
                    + "] but expected type was [" + Subscriber.class.getName() + "]");
        }
        return true;
    }


    private void handleError(Context ctx, Throwable t)
    {
        new MinorLogMsg(this, t.getMessage(), t).log(ctx);
        ExceptionListener excl = (ExceptionListener) ctx.get(ExceptionListener.class);
        if (null != excl)
        {
            excl.thrown(t);
        }
    }


    private void handleError(Context ctx, String message)
    {
        handleError(ctx, new IllegalStateException(message));
    }
}