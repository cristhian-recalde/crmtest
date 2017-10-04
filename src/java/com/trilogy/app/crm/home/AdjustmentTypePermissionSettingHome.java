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

package com.trilogy.app.crm.home;

import java.util.Collection;

import com.trilogy.app.crm.adjustmenttype.UserGroupPermissionUpdateVisitor;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.auth.permission.PermissionInfo;
import com.trilogy.framework.auth.permission.PermissionInfoHome;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Sets the adjustment type's permission.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypePermissionSettingHome extends HomeProxy
{

    private static final long serialVersionUID = 1L;

    public AdjustmentTypePermissionSettingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException
    {
        AdjustmentType adjustmentType = (AdjustmentType) obj;
        String permission = this.getPermission(ctx, adjustmentType);
        adjustmentType.setPermission(permission);
        addPermissionInfo(ctx, permission);

        return getDelegate().create(ctx, adjustmentType);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
        AdjustmentType adjustmentType = (AdjustmentType) obj;

        AdjustmentType oldAdjustmentType = AdjustmentTypeSupportHelper.get(ctx)
                .getAdjustmentType(ctx, adjustmentType.getCode());

        if (oldAdjustmentType.getParentCode() != adjustmentType.getParentCode()
                || oldAdjustmentType.isCategory() != adjustmentType
                        .isCategory())
        {
            String oldPermission = getPermission(ctx, oldAdjustmentType);
            String newPermission = getPermission(ctx, adjustmentType);
            String oldWildcard = getPermissionWildcard(ctx, oldAdjustmentType);
            String newWildcard = getPermissionWildcard(ctx, adjustmentType);
            updateChildrenPermission(ctx, getDelegate(), adjustmentType
                    .getCode(), oldPermission, newPermission);
            updatePermissionInfo(ctx, oldPermission, newPermission,
                    oldWildcard, newWildcard);

            // update uesr group permissions.
            updateUserGroupPermission(ctx, oldPermission, newPermission);
        }
        return getDelegate().store(ctx, adjustmentType);
    }

    /**
     * Update the permission of all descendants of an adjustment type.
     * 
     * @param ctx
     *            Operating context.
     * @param home
     *            Adjustment type home.
     * @param adjustmentType
     *            Current adjustment type.
     * @param oldPermissionPrefix
     *            Old permission prefix.
     * @param newPermissionPrefix
     *            New permission prefix.
     * @throws HomeException
     *             Thrown by home.
     */
    private final void updateChildrenPermission(Context ctx, Home home,
            int adjustmentType, String oldPermissionPrefix,
            String newPermissionPrefix) throws HomeException
    {
        Collection<AdjustmentType> adjustmentTypes = home.select(ctx, new EQ(
                AdjustmentTypeXInfo.PARENT_CODE, adjustmentType));

        for (AdjustmentType t : adjustmentTypes)
        {
            String oldPermission = t.getPermission();
            String oldWildcard = getPermissionWildcard(ctx, t);
            String newPermission = oldPermission.replaceFirst(
                    oldPermissionPrefix, newPermissionPrefix);
            t.setPermission(newPermission);
            t = (AdjustmentType) getDelegate().store(ctx, t);
            String newWildcard = getPermissionWildcard(ctx, t);
            updatePermissionInfo(ctx, oldPermission, newPermission, oldWildcard, newWildcard);
            updateChildrenPermission(ctx, home, t.getCode(), oldPermission,
                    newPermission);
        }

    }

    /**
     * Updates the permissions in a user group to match the new adjustment type
     * hierarchy.
     * 
     * @param ctx
     *            Operating context.
     * @param oldPermission
     *            Old permission prefix.
     * @param newPermission
     *            New permission prefix.
     * @throws HomeException
     *             Thrown by home.
     */
    private static final void updateUserGroupPermission(Context ctx,
            String oldPermission, String newPermission) throws HomeException
    {
        Home home = (Home) ctx.get(CRMGroupHome.class);
        UserGroupPermissionUpdateVisitor visitor = new UserGroupPermissionUpdateVisitor(
                oldPermission, newPermission);
        visitor = (UserGroupPermissionUpdateVisitor) home.forEach(ctx, visitor);
        for (CRMGroup group : visitor.getUpdatedGroups())
        {
            home.store(ctx, group);
        }
    }

    /**
     * Construct the permission for this adjustment type.
     * 
     * @param ctx
     *            Operating context.
     * @param adjustmentType
     *            Adjustment type to look up permission for.
     * @return Permission for the provided adjustment type.
     * @throws HomeException
     *             Thrown by adjustment type home.
     */
    protected String getPermission(Context ctx, AdjustmentType adjustmentType)
            throws HomeException
    {
        StringBuilder sb = new StringBuilder();
        AdjustmentType current = adjustmentType;
        do
        {
            sb.insert(0, current.getCode());
            sb.insert(0, '.');
            current = (AdjustmentType) getDelegate().find(ctx,
                    current.getParentCode());
        }
        while (current != null);
        sb.insert(0, ADJUSTMENT_TYPE_PERMISSION_ROOT);
        return sb.toString();
    }

    /**
     * If this is a category, construct the wildcard permission for this
     * adjustment type. Otherwise, just return the regular permission.
     * 
     * @param ctx
     *            Operating context.
     * @param adjustmentType
     *            Adjustment type to look up permission for.
     * @return Permission for the provided adjustment type.
     * @throws HomeException
     *             Thrown by adjustment type home.
     */
    protected String getPermissionWildcard(Context ctx,
            AdjustmentType adjustmentType) throws HomeException
    {
        StringBuilder sb = new StringBuilder();
        AdjustmentType current = adjustmentType;
        do
        {
            sb.insert(0, current.getCode());
            sb.insert(0, '.');
            current = (AdjustmentType) getDelegate().find(ctx,
                    current.getParentCode());
        }
        while (current != null);
        sb.insert(0, ADJUSTMENT_TYPE_PERMISSION_ROOT);
        if (adjustmentType.isCategory())
        {
            sb.append(".*");
        }
        return sb.toString();
    }

    /**
     * Adds a new permission to PermissionInfoHome.
     * 
     * @param ctx
     *            Operating context.
     * @param permission
     *            Permission to add.
     * @throws HomeException
     *             Thrown by PermissionInfoHome.
     */
    protected void addPermissionInfo(Context ctx, String permission)
            throws HomeException
    {
        Home home = (Home) ctx.get(PermissionInfoHome.class);
        PermissionInfo info = new PermissionInfo();
        info.setName(permission);
        info.setEnabled(true);
        home.create(ctx, info);
    }

    /**
     * Updates permission in PermissionInfoHome.
     * 
     * @param ctx
     *            Operating context.
     * @param oldPermission
     *            Old permission.
     * @param newPermission
     *            New Permission.
     * @throws HomeException
     *             Thrown by PermissionInfoHome.
     */
    protected void updatePermissionInfo(Context ctx, String oldPermission,
            String newPermission, String oldWildcard, String newWildcard)
            throws HomeException
    {
        Home home = (Home) ctx.get(PermissionInfoHome.class);
        PermissionInfo p = new PermissionInfo();
        p.setName(oldPermission);
        home.remove(ctx, p);
        if (!SafetyUtil.safeEquals(oldPermission, oldWildcard))
        {
            p = new PermissionInfo();
            p.setName(oldWildcard);
            home.remove(ctx, p);
        }
        addPermissionInfo(ctx, newPermission);
        if (!SafetyUtil.safeEquals(newPermission, newWildcard))
        {
            addPermissionInfo(ctx, newWildcard);
    }
    }

    public static final String ADJUSTMENT_TYPE_PERMISSION_ROOT = "app.crm.adjustmentType";
}
