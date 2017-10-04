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

import java.security.Principal;
import java.util.Date;

import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CreditLimitAdjustment;
import com.trilogy.app.crm.bean.CreditLimitAdjustmentHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.support.UserGroupSupport;

/**
 * Creates Credit Limit Adjustment entries whenever subscriber credit limit has changed. This decorator should appear
 * <b>before</b> any decorator which modifies credit limit due to other changes (deposit, etc).
 *
 * @author cindy.wong@redknee.com
 */
public class CreditLimitAdjustmentCreationHome extends HomeProxy
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 4268767947111896416L;

    /**
     * Creates a new <code>CreditLimitAdjustmentCreationHome</code>.
     *
     * @param context The operating context.
     * @param delegate The home to which we delegate.
     */
    public CreditLimitAdjustmentCreationHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * Creates a Credit Limit Adjustment entry when a subscriber's credit limit is modified.
     *
     * @param context The operating context.
     * @param object The subscriber attempted to update.
     * @return The actual resulting subscriber.
     * @throws HomeException Thrown if there are problems updating the subscriber.
     */
    @Override
    public final Object store(final Context context, final Object object) throws HomeException
    {
        final User user = (User) context.get(Principal.class);
        final CRMGroup group = (CRMGroup) context.get(Group.class);

        Subscriber subscriber;
        try
        {
            subscriber = (Subscriber) object;
        }
        catch (ClassCastException e)
        {
            throw new HomeException("System Error: obj passed is not a Subscriber", e);
        }
        if (subscriber == null)
        {
            throw new HomeException("System Error: obj passed is null");
        }

        // get the old subscriber
        final Subscriber oldSubscriber;
        try
        {
            oldSubscriber = getSubscriber(context, subscriber.getId());
        }
        catch (HomeException e)
        {
            throw new HomeException("Cannot retrieve existing subscriber " + subscriber.getId(), e);
        }
        if (oldSubscriber == null)
        {
            throw new HomeException("Subcriber " + subscriber.getId() + " does not exist in home");
        }

        boolean createEntry = UserGroupSupport.isCreditLimitPermissionEnabled(context, group);
        createEntry &= UserGroupSupport.isCreditLimitCheckEnabled(context, oldSubscriber, subscriber);


        // 1. if the credit limit has changed, create the audit entry, but don't save it yet
        final CreditLimitAdjustment adjustment = new CreditLimitAdjustment();
        if (createEntry)
        {
            adjustment.setOldCreditLimit(oldSubscriber.getCreditLimit(context));
            adjustment.setNewCreditLimit(subscriber.getCreditLimit(context));
            adjustment.setSubscriber(subscriber.getId());
            adjustment.setAdjustmentDate(new Date());
            adjustment.setSpid(subscriber.getSpid());
            adjustment.setAgent(user.getId());
        }

        // 2. pass it through the rest of the pipeline
        final Subscriber updatedSubscriber = (Subscriber) super.store(context, subscriber);

        // 3. if the store was actually successful, save the audit entry
        if (createEntry && updatedSubscriber.getCreditLimit(context) != oldSubscriber.getCreditLimit(context))
        {
            subscriber = updatedSubscriber;

            // catch and swallow exception here; it does not affect normal store operation on SubscriberHome.
            try
            {
                ((Home) context.get(CreditLimitAdjustmentHome.class)).create(context, adjustment);
            }
            catch (HomeException exception)
            {
                new DebugLogMsg(this, "Cannot create credit limit adjustment entry", exception).log(context);
            }
        }

        return subscriber;
    }

    /**
     * Gets the subscriber for the given identifier.
     *
     * @param context The operating context.
     * @param identifier The subscriber identifier.
     * @return The subscriber.
     * @throws HomeException Thrown if there are problems accessing Home information in the context.
     */
    private Subscriber getSubscriber(final Context context, final String identifier) throws HomeException
    {
        final Home home = (Home) context.get(SubscriberHome.class);
        if (home == null)
        {
            throw new HomeException("Cannot find SubscriberHome in context");
        }
        try
        {
            final Subscriber subscriber = (Subscriber) home.find(context, identifier);
            return subscriber;
        }
        catch (HomeException e)
        {
            throw new HomeException("Cannot find subscriber " + identifier, e);
        }
    }
}
