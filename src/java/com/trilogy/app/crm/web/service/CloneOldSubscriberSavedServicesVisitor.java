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
package com.trilogy.app.crm.web.service;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServices;


/**
 * Clones the subscriber service from one subscriber to another.
 * @author danny.ng@redknee.com
 *
 */
public final class CloneOldSubscriberSavedServicesVisitor extends HomeVisitor
{


    /**
     * the serail version uid
     */
    private static final long serialVersionUID = -2133534807568972049L;

    /**
     * Creates a CloneOldSubscriberSavedServicesVisitor.
     * @param home the home to be visited
     * @param newSubscriber the subscriber to set the id
     */
    public CloneOldSubscriberSavedServicesVisitor(final Home home,
            final Subscriber newSubscriber)
    {
        super(home);
        newSubscriber_ = newSubscriber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(final Context ctx, final Object obj) throws AgentException
    {
        SubscriberServices ss = (SubscriberServices) obj;
        try
        {
            ss = (SubscriberServices) ss.deepClone();
            ss.setSubscriberId(newSubscriber_.getId());

            getHome().create(ctx, ss);
        }
        catch (CloneNotSupportedException e)
        {
            new MinorLogMsg(this, e.getMessage(), e).log(ctx);
        }
        catch (HomeException e)
        {
            new MinorLogMsg(this, e.getMessage(), e).log(ctx);
        }
    }

    /**
     * The subscriber to set the id
     */
    private final Subscriber newSubscriber_;

}
