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
package com.trilogy.app.crm.home;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.support.SubscriberSupport;

/**
 * An Abstract Home to add sub type to each Call Details and Transaction record.
 *
 * @author daniel.zhang@redknee.com
 */
public abstract class SetSubTypeHome extends HomeProxy
{
    /**
     * Constructor.
     *
     * @param delegate the next home in the chain
     */
    public SetSubTypeHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * Checks if parameters that are passed in are valid.
     *
     * @param ctx the operating context
     * @param ban Subscriber BAN
     * @param msisdn Subscriber MSISDN
     * @return false if any of the parameters is null
     */
    private boolean validateParams(final Context ctx, final String ban, final String msisdn)
    {
        boolean valid = true;
        if (ctx == null)
        {
            new MajorLogMsg(this,
                    "The context passed into SetSubTypeHome is null. Cannot continue.",
                    null).log(ctx);
            valid = false;
        }

        if (ban == null)
        {
            new MajorLogMsg(this,
                    "The ban passed into SetSubTypeHome.getSubscriberType is null. Cannot continue.",
                    null).log(ctx);
            valid = false;
        }

        if (msisdn == null)
        {
            new MajorLogMsg(this,
                    "The msisdn passed into SetSubTypeHome.getSubscriberType is null. Cannot continue.",
                    null).log(ctx);
            valid = false;
        }

        return valid;
    }

    /**
     * Determines the Subscriber Type of the Subscriber that has the passed in MSISDN and BAN.
     * First tries the Subscriber in the Context, then the database. Returns null if unable to locate Subscriber
     *
     * @param ctx the operating context
     * @param ban Subscriber BAN
     * @param msisdn Subscriber MSISDN
     * @param eventDate
     * @return Subscriber Type or null if unable to locate subscriber
     */
    public SubscriberTypeEnum getSubscriberType(final Context ctx, final String ban, final String msisdn,
            final Date eventDate)
    {
        SubscriberTypeEnum result = null;
        final boolean validInput = validateParams(ctx, ban, msisdn);

        if (validInput)
        {
            // try get Subscriber from context first
            Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
            if (sub != null)
            {
                // For some reason a wrong Subscriber is inserted in the context
                // we need to check if this is the right subscriber
                if (sub.getMSISDN().equals(msisdn) && sub.getBAN().equals(ban))
                {
                    result = sub.getSubscriberType();
                }
                else
                {
                    new DebugLogMsg(this, "WRONG Subscriber in the context. Current Subscriber in the context: "
                            + sub.getId() + " " + sub.getBAN() + " " + sub.getMSISDN() 
                            + ". Performing a new lookup for the subscriber with BAN: "
                            + ban + " and MSISDN=" + msisdn, null).log(ctx);
                }
            }

            // we may have subscriber in context but we cannot use it, look for the correct one
            if (result == null)
            {
                // not in context, try Database
                try
                {
                    sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn, eventDate);
                }
                catch (Exception e)
                {
                    new MajorLogMsg(this, "Unable to determine Subscriber", e).log(ctx);
                }

                if (sub == null)
                {
                    new MajorLogMsg(this, "The ban[" + ban + "] and msisdn[" + msisdn
                            + "] passed into SetSubTypeHome.getSubscriberType is invalid. Cannot continue.",
                            null).log(ctx);
                }
                else
                {
                    ctx.put(Subscriber.class, sub);
                    result = sub.getSubscriberType();
                }
            }
        }

        return result;
    }

    /**
     * Updates the passed bean by setting the Subscriber Type property before delegating call.
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        setSubType(ctx, obj);
        return getDelegate().create(ctx, obj);
    }


    /**
     * Updates the passed bean by setting the Subscriber Type property before delegating call.
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        setSubType(ctx, obj);
        return getDelegate().store(ctx, obj);
    }

    /**
     * Updates the passed bean by setting the Subscriber Type property. Polimorphic implementation.
     *
     * @param ctx the operating context
     * @param obj the bean to update
     */
    protected abstract void setSubType(final Context ctx, final Object obj) throws HomeException;
}
