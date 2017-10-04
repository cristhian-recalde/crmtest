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

package com.trilogy.app.crm.support;

import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.extension.account.AbstractSubscriberLimitExtension;
import com.trilogy.app.crm.extension.account.AccountExtensionXInfo;
import com.trilogy.app.crm.extension.account.SubscriberLimitExtension;


/**
 * Support class for {@link SubscriberLimitExtension}.
 *
 * @author cindy.wong@redknee.com
 * @since 2008-09-11
 */
public final class SubscriberLimitSupport
{

    /**
     * Creates a new <code>SubscriberLimitSupport</code> instance. This method is made
     * private to prevent instantiation of utility class.
     */
    private SubscriberLimitSupport()
    {
        // empty
    }


    /**
     * Returns the number of immediate, non-inactive subscribers belonging to an account.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            BAN of the account.
     * @return The number of immediate, non-inactive subscribers belonging to an account.
     * @throws HomeException
     *             Thrown by home.
     */
    public static int getNumberOfSubscribersInAccount(final Context context, final String ban) throws HomeException
    {
        int result = 0;
        final Home subHome = (Home) context.get(SubscriberHome.class);

        final And where = new And();
        where.add(new EQ(SubscriberXInfo.BAN, ban));
        where.add(new NEQ(SubscriberXInfo.STATE, SubscriberStateEnum.INACTIVE));
        final Collection subs = subHome.select(context, where);
        if (subs != null)
        {
            result = subs.size();
        }
        return result;

    }


    /**
     * Validates whether adding a subscriber to an account would violate the subscriber
     * limit.
     *
     * @param context
     *            The operating context.
     * @param ban
     *            Account to be validated.
     */
    public static void validateAddSubscriberToAccount(final Context context, final String ban)
    {
        final List<SubscriberLimitExtension> extensions = ExtensionSupportHelper.get(context).getExtensions(context,
            SubscriberLimitExtension.class, new EQ(AccountExtensionXInfo.BAN, ban));
        for (final SubscriberLimitExtension extension : extensions)
        {
            if (extension == null)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, SubscriberLimitSupport.class, "No "
                        + ExtensionSupportHelper.get(context).getExtensionName(context, SubscriberLimitExtension.class)
                        + " exists for account " + ban);
                }
            }
            else if (extension.getMaxSubscribers() == AbstractSubscriberLimitExtension.DEFAULT_MAXSUBSCRIBERS)
            {
                if (LogSupport.isDebugEnabled(context))
                {
                    LogSupport.debug(context, SubscriberLimitSupport.class, extension.getName(context)
                        + " - limit not set properly for account " + ban + ", skipping validation.");
                }
            }
            else
            {
                int numSubs = 1;
                try
                {
                    numSubs += getNumberOfSubscribersInAccount(context, ban);
                }
                catch (final HomeException exception)
                {
                    if (LogSupport.isDebugEnabled(context))
                    {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(exception.getClass().getSimpleName());
                        sb.append(" caught in ");
                        sb.append("SubscriberLimitSupport.validateLimitOnAddSubscriber(): ");
                        if (exception.getMessage() != null)
                        {
                            sb.append(exception.getMessage());
                        }
                        LogSupport.debug(context, SubscriberLimitSupport.class, sb.toString(), exception);
                    }

                }
                if (numSubs > extension.getMaxSubscribers())
                {
                    throw new IllegalStateException("Subscriber creation failed: subscriber limit of account " + ban
                        + " has been exceeded.");
                }
            }
        }
    }

}
