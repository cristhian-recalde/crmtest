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

import com.trilogy.framework.license.LicenseMgr;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Support class for user group operations.
 *
 * @author joe.chen@redknee.com
 * @author cindy.wong@redknee.com
 */
public final class UserGroupSupport
{
    /**
     * License key for credit limit permission.
     */
    public static final String CREDIT_LIMIT_PERMISSION_LICENSE_KEY = "CREDIT_LIMIT_PERMISSION_LICENSE";

    /**
     * Context key for temporarily disabling any credit limit permission-related activities.
     */
    public static final String CREDIT_LIMIT_PERMISSION_DISABLED_KEY = "CREDIT_LIMIT_PERMISSION_DISABLED";

    /**
     * Private utility class constructor to prevent instantiation.
     */
    private UserGroupSupport()
    {
        // do nothing
    }

    /**
     * Look up the adjustment limit of the logged-in user based on the group.
     *
     * @param ctx
     *            The operating context.
     * @return The adjustmentLimit value.
     * @throws HomeException
     *             Thrown if there are problems retrieving the adjustment limit.
     */
    public static long getAdjustmentLimit(final Context ctx) throws HomeException
    {
         CRMGroup group = (CRMGroup) ctx.get(Group.class);
        
        if (group != null)
        {
        	group = getCRMGroup(ctx, group.getName());
            return group.getAdjustmentLimit();
        }
        else
        {
            return Common.DEFAULT_ADJUST_LIMIT;
        }
    }

    /**
     * Look up the credit limit adjustment limit of the logged-in user based on the group.
     *
     * @param ctx
     *            The operating context.
     * @return The adjustmentLimit value.
     * @throws HomeException
     *             Thrown if there are problems retrieving the adjustment limit.
     */
    public static long getCreditLimitAdjustmentLimit(final Context ctx) throws HomeException
    {
        final CRMGroup group = (CRMGroup) ctx.get(Group.class);
        if (group != null)
        {
            return group.getCreditLimitAdjustmentLimit();
        }
        else
        {
            return Common.DEFAULT_ADJUST_LIMIT;
        }
    }


    /**
     * Retrieves the minimum deposit of the current user group.
     *
     * @param ctx
     *            The operating context.
     * @return The minimum deposit required by this user group, or 0 if none.
     */
    public static int getMinimumDeposit(final Context ctx)
    {
        final CRMGroup group = (CRMGroup) ctx.get(Group.class);
        if (group != null)
        {
            return group.getMinimumDeposit();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Check if Credit Limit Permission feature is enabled for this user's group.
     *
     * @param context
     *            The operating context.
     * @param group
     *            The user group in question.
     * @return <code>TRUE</code> if credit limit permission is enabled for this group, <code>FALSE</code> otherwise.
     */
    public static boolean isCreditLimitPermissionEnabled(final Context context, final CRMGroup group)
    {
        final LicenseMgr licenseManager = (LicenseMgr) context.get(LicenseMgr.class);
        return (group != null) && group.getDurationOfCreditLimitAdjustments() > 0
            && licenseManager.isLicensed(context, CREDIT_LIMIT_PERMISSION_LICENSE_KEY);
    }

    /**
     * Determines if the validation should be enabled.
     *
     * @param context
     *            The operating context.
     * @param oldSubscriber
     *            The old subscriber.
     * @param newSubscriber
     *            The new subscriber.
     * @return Whether the validation should be enabled or exempted.
     */
    public static boolean isCreditLimitCheckEnabled(final Context context, final Subscriber oldSubscriber,
        final Subscriber newSubscriber)
    {

        final LicenseMgr licenseManager = (LicenseMgr) context.get(LicenseMgr.class);
        final Boolean disabled = (Boolean) context.get(CREDIT_LIMIT_PERMISSION_DISABLED_KEY);

        boolean enabled = true;

        // check if it's licensed
        if (!licenseManager.isLicensed(context, CREDIT_LIMIT_PERMISSION_LICENSE_KEY))
        {
            enabled = false;
        }

        // key not set => enabled
        else if (disabled != null && disabled.booleanValue())
        {
            enabled = false;
        }

        // new subscriber
        else if (oldSubscriber == null)
        {
            enabled = false;
        }

        // TT 7012444194: Subscriber conversion is also exempted from this check.
        else if (oldSubscriber.getSubscriberType() != newSubscriber.getSubscriberType())
        {
            enabled = false;
        }

        // credit limit unchanged
        else if (oldSubscriber.getCreditLimit(context) == newSubscriber.getCreditLimit(context))
        {
            enabled = false;
        }

        return enabled;
    }

    /**
     * Disable credit limit permission for this context.
     *
     * @param context
     *            The operating context.
     */
    public static void disableCreditLimitPermission(final Context context)
    {
        context.put(CREDIT_LIMIT_PERMISSION_DISABLED_KEY, Boolean.TRUE);
    }

    /**
     * Enable credit limit permission for this context.
     *
     * @param context
     *            The operating context.
     */
    public static void enableCreditLimitPermission(final Context context)
    {
        context.put(CREDIT_LIMIT_PERMISSION_DISABLED_KEY, Boolean.FALSE);
    }

    /**
     * Returns the CRM user group.
     *
     * @param context The operating context.
     * @param name Group name.
     * @return The CRM user group.
     */
    public static CRMGroup getCRMGroup(final Context context, final String name) throws HomeException
    {
        Home home = (Home) context.get(CRMGroupHome.class);
        if (home == null)
        {
            throw new HomeException("CRMGroupHome not found in context.");
        }
        return (CRMGroup) home.find(context, name);
    }
}
