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

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.Lookup;

/**
 * This home decorator prevents removal of subscriber that are not Deactivated.
 * Also it prevents the call to removeAll.
 *
 * @author victor.stratan@redknee.com
 */
public class SubscriberRemoveValidator extends HomeProxy
{
    public SubscriberRemoveValidator(final Context ctx, final Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * Allows only remove on Inactive subscribers. Checks the state of the subscriber for the context
     * which should be loaded from the DB on the top of the pipeline.
     *
     * @param ctx the operating context
     * @param obj bean to be removed
     *
     * @throws HomeException thrown by call to delegate
     */
    public void remove(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber sub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER, obj);
        if (sub.getState() == SubscriberStateEnum.INACTIVE || sub.getState() == SubscriberStateEnum.AVAILABLE)
        {
            super.remove(ctx, obj);
        }
        else
        {
            throw new IllegalStateException("Remove is allowed only for "
                    + SubscriberStateEnum.INACTIVE.getDescription() + " and " + SubscriberStateEnum.AVAILABLE.getDescription() + " subscribers.");
        }
    }

    /**
     * Prevent calls to removeAll() otherwise it can be used to bypass this home decorator logic.
     *
     * @param ctx the opearating context
     * @param where a where condition which is ignored
     */
    public void removeAll(final Context ctx, final Object where)
    {
        throw new UnsupportedOperationException();
    }
}
