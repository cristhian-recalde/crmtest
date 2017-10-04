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
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtension;
import com.trilogy.app.crm.extension.usergroup.UserGroupExtensionHolder;
import com.trilogy.app.crm.filter.AdjustmentTypeByAuth;
import com.trilogy.framework.xhome.auth.bean.Group;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * Support class for adjustment type permission.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public final class AdjustmentTypePermissionSupport
{
    private AdjustmentTypePermissionSupport()
    {
        // empty
    }

    /**
     * Returns whether the transaction can be created based on the adjustment
     * type permission.
     * 
     * @param context
     *            Operating context.
     * @param transaction
     *            Transaction being created.
     * @return Whether the transaction can be created based on the adjustment
     *         type permission.
     * @throws HomeException
     *             Thrown if there are problems looking up adjustment type
     *             hierarchy.
     */
    public static boolean isTransactionPermitted(Context context,
            Transaction transaction) throws HomeException
    {
        return isTransactionPermitted(context, transaction.getAdjustmentType());
    }

    /**
     * Returns whether the currently logged in user has permission to create a
     * transaction with the provided adjustment type.
     * 
     * @param context
     *            Operating context.
     * @param code
     *            Adjustment type of the transaction.
     * @return Whether the currently logged in user has permission to create a
     *         transaction with the provided adjustment type.
     * @throws HomeException
     *             Thrown if there are problems looking up adjustment type
     *             hierarchy.
     */
    public static boolean isTransactionPermitted(Context context, int code)
            throws HomeException
    {
        AdjustmentType adjustmentType = AdjustmentTypeSupportHelper.get(context)
                .getAdjustmentType(context, code);
        if (adjustmentType == null)
        {
            return false;
        }
        return new AdjustmentTypeByAuth(context, true).f(context, adjustmentType);
    }
        }
