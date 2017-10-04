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
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;


/**
 * Assigning Subscriber id based on BAN info.
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberIdAssignHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscriberIdAssignHome</code>.
     *
     * @param delegate
     *            Delegate of this home decorator.
     */
    public SubscriberIdAssignHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber subscriber = (Subscriber) obj;

        final CRMSpid sp = SpidSupport.getCRMSpid(ctx, subscriber.getSpid());
        /*
         * TT 7091400011: If no subscriber ID is specified, use auto-created ID.
         */
        if (SubscriberSupport.isAutoCreateSubscriberId(sp, subscriber)
                || !subscriber.isSubscriberIdSet())
        {
            final String identifier = SubscriberSupport.acquireNextSubscriberIdentifier(ctx, subscriber.getBAN());

            subscriber.setId(identifier);
            
            if (LogSupport.isDebugEnabled(ctx))
            {
                new DebugLogMsg(this, "Assign a new ID to subscriber " + subscriber, null).log(ctx);
            }
        }
        

        return super.create(ctx, obj);
    }
}
