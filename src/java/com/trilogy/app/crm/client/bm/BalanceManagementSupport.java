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
package com.trilogy.app.crm.client.bm;

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.support.SystemStatusSupportHelper;
import com.trilogy.app.crm.support.FrameworkSupportHelper;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Provides general support for using the Balance Management APIs.
 *
 * @author gary.anderson@redknee.com
 */
public final class BalanceManagementSupport
{
    /**
     * Discourage instantiation.
     */
    private BalanceManagementSupport()
    {
        // EMPTY
    }


    /**
     * Installs a client for the SubscriberProfileProvision API.
     *
     * @param context The application context.
     */
    public static SubscriberProfileProvisionClient installSubscriberProfileProvisionClient(final Context context)
    {
        new InfoLogMsg(BalanceManagementSupport.class.getName(), "Installing SubscriberProfileProvisionClient.", null).log(context);
        SubscriberProfileProvisionClient client = null;
        try
        {
            // TODO -- Add decorators for logging, PMs, testing client, etc.
            client = new DefaultSubscriberProfileProvisionClient(context);

            context.put(SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY, client);
//            SystemStatusSupport.registerExternalService(context, SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY);

            new InfoLogMsg(BalanceManagementSupport.class.getName(), "Installation completed normally.", null).log(context);
        }
        catch (final RuntimeException exception)
        {
            new MajorLogMsg(BalanceManagementSupport.class.getName(), "Failure detected during installation.",
                exception).log(context);
        }
        return client;
    }


    /**
     * Gets the client for the SubscriberProfileProvision API.
     *
     * @param context The operating context.
     * @return The client for the SubscriberProfileProvision API.
     */
    public static SubscriberProfileProvisionClient getSubscriberProfileProvisionClient(final Context context)
    {
        final SubscriberProfileProvisionClient client;

        final LicenseMgr manager = (LicenseMgr)context.get(LicenseMgr.class);
        if (!manager.isLicensed(context, DEV_TEST_SUBSCRIBER_PROFILE_PROVISION_CLIENT_LICENSE))
        {
            client = (SubscriberProfileProvisionClient)context.get(SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY);
        }
        else
        {
            client = TEST_CLIENT;
        }

        return client;
    }


    /**
     * A testing stub, intended for use when the DEV testing license is enabled.
     */
    private static final SubscriberProfileProvisionClient TEST_CLIENT = new SubscriberProfileProvisionTestClient();

    /**
     * The name of the DEV testing license.
     */
    private static final String DEV_TEST_SUBSCRIBER_PROFILE_PROVISION_CLIENT_LICENSE =
        "DEV - Test SubscriberProfileProvision Client";

    /**
     * The key used for locating the SubscriberProfileProvisionClient in the
     * context.
     */
    private static final String SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY = "client.bm.SubscriberProfileProvisionClient";

    public static Parameters getSubscription(final Context ctx, final Object caller,
            final SubscriberProfileProvisionClient bmClient, final Subscriber subscriber)
    {
        Parameters subscription = null;
        try
        {
            subscription = bmClient.querySubscriptionProfile(ctx, subscriber);
            if (subscription == null)
            {
                final String msg = "Unable to retreive Subscription balance for subscription "
                        + subscriber.getId();
                LogSupport.minor(ctx, caller, msg);
                FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, new HomeException(msg));
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, caller, "Unable to retreive Subscription profile for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        catch (SubscriberProfileProvisionException e)
        {
            LogSupport.minor(ctx, caller, "Unable to retreive Subscription profile for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }

        return subscription;
    }

    public static boolean zeroSubscriberBalance(final Context ctx, final Object caller,
            final SubscriberProfileProvisionClient bmClient, final Subscriber subscriber)
    {
        boolean success = false;
        try
        {
            bmClient.updateBalance(ctx, subscriber, 0L);
            success = true;
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, caller, "Unable to zero Subscription balance for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        catch (SubscriberProfileProvisionException e)
        {
            LogSupport.minor(ctx, caller, "Unable to zero Subscription balance for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }

        return success;
    }


    public static boolean updatePooledGroupID(final Context ctx, final Object caller,
            final SubscriberProfileProvisionClient bmClient, final Subscriber subscriber, final String poolID)
    {
        boolean success = false;
        try
        {
            bmClient.updatePooledGroupID(ctx, subscriber, poolID, false);
            success = true;
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, caller, "Unable to update pooled group ID for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        catch (SubscriberProfileProvisionException e)
        {
            LogSupport.minor(ctx, caller, "Unable to update pooled group ID for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }

        return success;
    }
    
    public static Parameters removeSubscription(final Context ctx, final Object caller,
            final SubscriberProfileProvisionClient bmClient, final Subscriber subscriber, final String poolID)
    {
        Parameters params = null;
        try
        {
            params = bmClient.removeSubscriptionProfile(ctx, subscriber);
        }
        catch (SubscriberProfileProvisionException e)
        {
            LogSupport.minor(ctx, caller, "Unable to remove Subscription profile " + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        return params;
    }


    /**
     * Retrieves subscription balance on BM.
     * 
     * @param ctx the operating context
     * @param sub subscription
     * @return balance if any
     */
    public static long getSubscriptionBalance(final Context ctx, final Object caller, final Subscriber subscriber)
    {
        final SubscriberProfileProvisionClient client = getSubscriberProfileProvisionClient(ctx);

        try
        {
            final Parameters subscription = client.querySubscriptionProfile(ctx, subscriber);
            if (subscription != null)
            {
                return subscription.getBalance();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, caller, "Unable to retreive Subscription balance for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }
        catch (SubscriberProfileProvisionException e)
        {
            LogSupport.minor(ctx, caller, "Unable to retreive Subscription balance for subscription "
                    + subscriber.getId(), e);
            FrameworkSupportHelper.get(ctx).notifyExceptionListener(ctx, e);
        }

        return 0L;
    }
}
