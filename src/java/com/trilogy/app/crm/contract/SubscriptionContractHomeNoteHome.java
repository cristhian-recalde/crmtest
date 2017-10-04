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
package com.trilogy.app.crm.contract;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.home.sub.SubscriberNoteSupport;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * Creates/Modifies/Deletes the notes associated with a subscription contract on every
 * subscriber operation.
 * 
 */
public class SubscriptionContractHomeNoteHome extends HomeProxy
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
    public SubscriptionContractHomeNoteHome(final Home delegate)
    {
        super(delegate);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object create(final Context ctx, final Object obj) throws HomeException
    {
        Object createdObj = super.create(ctx, obj);
        final Subscriber newSub = SubscriptionContractSupport.getSubscriber(ctx, (SubscriptionContract) obj);
        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        final SubscriptionContractTerm term = SubscriptionContractSupport.getSubscriptionContractTerm(ctx,(SubscriptionContract) obj);
        SubscriberNoteSupport.createAssignSubscriptionContractNote(ctx, this, oldSub, newSub,term.getName(), term.getId());
        return createdObj;
    }


    /**
     * Cascade deleting subscription contract notes when deleting a subscriber.
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
        final SubscriptionContract contract = (SubscriptionContract) obj;
        super.remove(ctx, obj);
        final SubscriptionContractTerm term = SubscriptionContractSupport.getSubscriptionContractTerm(ctx,(SubscriptionContract) obj);
        
        SubscriberNoteSupport.createRemoveSubscriptionContractNote(ctx, this,
                SubscriptionContractSupport.getSubscriber(ctx, contract), term.getName());
    }
}
