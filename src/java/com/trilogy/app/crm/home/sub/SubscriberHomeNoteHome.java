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
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionEndHome;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.Lookup;


/**
 * Creates/Modifies/Deletes the notes associated with a subscriber on every subscriber
 * operation.
 *
 * @author joe.chen@redknee.com
 */
public class SubscriberHomeNoteHome extends HomeProxy
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Create a new instance of <code>SubscriberHomeNoteHome</code>.
     *
     * @param delegate
     *            Delegate of this home.
     */
    public SubscriberHomeNoteHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        /*
         * TT 7031146097: Create subscriber note based on the subscriber actually created.
         */
        Object createdObj = null;
        try
        {
            createdObj = super.create(ctx, obj);
        }
        finally
        {
            final boolean crmResult = ctx.getBoolean(SubscriberProvisionEndHome.getSubscriberCreatedKey((Subscriber)obj), false);
            if (crmResult) 
            {
                SubscriberNoteSupport.createSubscriberCreationNote(ctx, (Subscriber) obj, this);
            }
        }
        return createdObj;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
        final Subscriber newSub = (Subscriber) obj;
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final PricePlanVersion oldPricePlan = oldSub.getPricePlan(ctx);
        final PricePlanVersion newPricePlan = newSub.getPricePlan(ctx);

        Object returnObj = newSub;
        boolean successful = false;
        try
        {
            returnObj = super.store(ctx, obj);
            successful = true;
        }
        finally
        {
            /*
             * TT 7031146097: Create subscriber note based on the subscriber actually
             * updated, if available.
             */
            final boolean crmResult = successful && SubscriberProvisionResultCode.getProvisionLastResultCode(ctx) == 0;

            SubscriberNoteSupport.createSubscriberUpdateNote(ctx, oldSub, (Subscriber) returnObj, oldPricePlan,
                newPricePlan, crmResult, this);
        }

        return returnObj;
    }


    /**
     * Cascade deleting subscriber notes when deleting a subscriber.
     *
     * @param ctx
     *            The operating context.
     * @param obj
     *            THe subscriber being deleted.
     * @throws HomeException
     *             Thrown if there are problems removing the subscriber.
     * @throws HomeInternalException
     *             Thrown if there are irrecoverable problems when removing the
     *             subscriber.
     */
    @Override
    public void remove(final Context ctx, final Object obj) throws HomeException, HomeInternalException
    {
        final Subscriber sub = (Subscriber) obj;

        /*
         * TT 7031146097: Do not remove subscriber notes unless the removal was
         * successful.
         */
        super.remove(ctx, obj);
        SubscriberNoteSupport.removeSubscriberNotes(ctx, sub);
    }
}
