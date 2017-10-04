/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee. No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeLimitProperty;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtensionHolder;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Support class for adjustment type limit.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public final class AdjustmentTypeLimitSupport
{
    private AdjustmentTypeLimitSupport()
    {
        // empty
    }

    /**
     * Returns whether the transaction is under the adjustment type limit as set
     * in the user group. If no limit is set for the adjustment type, keep
     * traversing up the adjustment type tree until we find one with limit set.
     * 
     * @param context
     *            Operating context.
     * @param transaction
     *            Transaction being created.
     * @return Whether the transaction is under the adjustment type limit.
     * @throws HomeException
     *             Thrown if there are problems looking up adjustment type
     *             hierarchy.
     */
    public static boolean isUnderLimit(Context context, Transaction transaction)
            throws HomeException
    {
        return isUnderLimit(context, transaction.getAdjustmentType(),
                transaction.getAmount());
    }

    /**
     * Returns whether the transaction is under the adjustment type limit as set
     * in the user group. If no limit is set for the adjustment type, keep
     * traversing up the adjustment type tree until we find one with limit set.
     * 
     * @param context
     *            Operating context.
     * @param adjustmentType
     *            Adjustment type of the transaction.
     * @param amount
     *            The monetary amount of the transaction.
     * @return Whether the transaction is under the adjustment type limit.
     * @throws HomeException
     *             Thrown if there are problems looking up adjustment type
     *             hierarchy.
     */
    public static boolean isUnderLimit(Context context, int adjustmentType,
            long amount) throws HomeException
    {
        // get user group
        final CRMGroup currentGroup = (CRMGroup) context.get(Group.class);
        if (currentGroup != null)
        {
            currentGroup.setContext(context);
            for (Object obj : currentGroup.getExtensions())
            {
                Extension extension = (Extension) obj;
                if (extension instanceof AdjustmentTypeLimitUserGroupExtension)
                {
                    AdjustmentTypeLimitUserGroupExtension ext = (AdjustmentTypeLimitUserGroupExtension) extension;
                    return isUnderLimit(context, ext, adjustmentType, amount);
                }
            }
        }
        // no user group or no limit set => assume OK
        return true;
    }

    /**
     * Returns whether the transaction is under the adjustment type limit as set
     * in the user group. If no limit is set for the adjustment type, keep
     * traversing up the adjustment type tree until we find one with limit set.
     * 
     * @param context
     *            Operating context.
     * @param extension
     *            The adjustment type limit user group extension.
     * @param adjustmentType
     *            Adjustment type of the transaction.
     * @param amount
     *            The monetary amount of the transaction.
     * @return Whether the transaction is under the adjustment type limit.
     * @throws HomeException
     *             Thrown if there are problems looking up adjustment type
     *             hierarchy.
     */
    public static boolean isUnderLimit(Context context,
            AdjustmentTypeLimitUserGroupExtension extension,
            int adjustmentType, long amount) throws HomeException
    {
        Integer key = new Integer(adjustmentType);
        if (extension.getLimits().containsKey(key))
        {
            AdjustmentTypeLimitProperty p = (AdjustmentTypeLimitProperty) extension.getLimits().get(key);
            return p.getLimit() >= Math.abs(amount);
        }

        AdjustmentType type = AdjustmentTypeSupportHelper.get(context).getAdjustmentType(context,
                adjustmentType);
        if (type != null && type.getCode() != 0)
        {
            return isUnderLimit(context, extension, type.getParentCode(),
                    amount);
        }

        // at root and root limit not set => assume OK
        return true;
    }
}
