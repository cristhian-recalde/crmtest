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
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.support.Lookup;

/**
 * This is a temporary fix for GTAC(5012414697) in which a transition from
 * available to active causes an update to be sent to OCG that triggers an ER
 * 442 to be generated, which often is processed while the transition in still
 * in progress, resulting in a stale write to the database.  When a more general
 * solution to stale writes is created, this decorator will no longer be
 * necessary.
 *
 * @author gary.anderson@redknee.com
 */
public class ExpiryDateUpdateSynchronizationHome extends HomeProxy
{
    /**
     * This is the lock object used to create a zone of mutual exclusion around
     * the delegate store() method when a susbcriber is transitioning from the
     * available state to the active state.
     */
    public static final Object LOCK_OBJECT = new Object();

    /**
     * Creates a new ExpiryDateUpdateSynchronizationHome.
     *
     * @param context The operating context.
     * @param delegate The Home to which we delegate.
     */
    public ExpiryDateUpdateSynchronizationHome(final Home delegate)
    {
        super(delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx,final Object obj) throws HomeException
    {
        final Subscriber subscriber = (Subscriber)obj;

        final PMLogMsg pmLogMsg;

        Object ret=null;
        
        if (isActivationTransition(ctx,subscriber))
        {
            pmLogMsg = new PMLogMsg(PM_MODULE, "synchronized");

            synchronized (LOCK_OBJECT)
            {
                debug(ctx,"Delegating to next home in synchronized block.");
                ret=super.store(ctx,subscriber);
                debug(ctx,"Delegation in synchronized block complete.");
            }
        }
        else
        {
            pmLogMsg = new PMLogMsg(PM_MODULE, "unsynchronized");

            ret=super.store(ctx,subscriber);
        }

        // NOTE - 2005-01-28 - The PM is not logged in a "finally " block
        // because we're currently only interested in tracking the times of
        // successful updates.
        pmLogMsg.log(ctx);
        
        return ret;
    }


    /**
     * Provides a simplified method for debug output.  It is meant to be used
     * for simple statements.  If complex statements are necessary, then the
     * caller really should be guarding and calling the DebugLogMsg directly.
     *
     * @param message The debug message to display.
     */
    private void debug(Context ctx,final String message)
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, message, null).log(ctx);
        }
    }


    /**
     * Indicates whether or not the given subscriber is transitioning to the
     * active state from any other state.
     *
     * @param proposedSubscriber The subscriber profile being stored.
     *
     * @return True if the given subscriber is transitioning to the active state
     * from any other state.
     */
    private boolean isActivationTransition(Context ctx,final Subscriber proposedSubscriber)
    {
        final Subscriber currentSubscriber = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        final boolean isActivationTransition =
            (currentSubscriber.getState() != SubscriberStateEnum.ACTIVE
             && proposedSubscriber.getState() == SubscriberStateEnum.ACTIVE);

        return isActivationTransition;
    }

    /**
     * Identifies this class in the performance measures.
     */
    private static final String PM_MODULE = ExpiryDateUpdateSynchronizationHome.class.getName();

} // class
