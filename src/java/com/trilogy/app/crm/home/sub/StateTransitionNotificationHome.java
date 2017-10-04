/*
 * Created on Nov 6, 2003
 * 
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.home.sub;

import com.trilogy.framework.xhome.beans.DefaultExceptionListener;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.notification.NotificationTypeEnum;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.SubscriptionNotificationSupport;
import com.trilogy.app.crm.support.Lookup;

/**
 * A home which sends out a SMS message when subsciber state changed. the
 * message is defined in SMSC/Message table.
 * 
 * @author Larry Xia
 */

public class StateTransitionNotificationHome extends HomeProxy
{

    public StateTransitionNotificationHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#create(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException,
            HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber result = (Subscriber) super.create(ctx, obj);
        SubscriptionNotificationSupport.sendStateTransitionNotification(ctx, null, result);
        // sendNewStateSms(ctx, (Subscriber)obj);
        return result;
    }

    /**
     * @see com.redknee.framework.xhome.home.HomeSPI#store(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object store(Context ctx, Object obj) throws HomeInternalException,
            HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber result = (Subscriber) super.store(ctx, obj);

        // Find the old Subscriber first so that we can tell which Services
        // changed
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

        SubscriptionNotificationSupport
                .sendStateTransitionNotification(ctx, oldSub, result);
        // sendNewStateSms(ctx, newSub);

        validatePreExpirySmsFlag(ctx, oldSub, result);
        return result;
    }

    private boolean hasErrors(Context ctx)
    {
        DefaultExceptionListener listener = SubscriberProvisionResultCode
                .getExceptionListener(ctx);

        if (LogSupport.isDebugEnabled(ctx))
        {
            if (listener != null)
            {
                new DebugLogMsg(this, "Listener not null ", null).log(ctx);
                new DebugLogMsg(this, "Number of errors on Listener = "
                        + listener.numOfErrors(), null).log(ctx);
                new DebugLogMsg(this, "Listener.has errors = "
                        + listener.hasErrors(), null).log(ctx);

            }
        }

        return listener.hasErrors();
    }

    /**
     * 
     * For all state change and expiry date updates, the flag will be reset
     * 
     * @param ctx
     * @param oldSub
     * @param newSub
     * @throws HomeException
     */
    void validatePreExpirySmsFlag(Context ctx, Subscriber oldSub,
            Subscriber newSub) throws HomeException
    {
        if (newSub != null && oldSub != null && newSub.getPreExpirySmsSent())
        {
            if (!newSub.getExpiryDate().equals(oldSub.getExpiryDate())
                    || !newSub.getState().equals(oldSub.getState()))
            {
                newSub.setPreExpirySmsSent(false);
                SubscriptionNotificationSupport.removePendingNotifications(ctx, oldSub.getId(), NotificationTypeEnum.PRE_EXPIRY);
            }
        }
    }
}
