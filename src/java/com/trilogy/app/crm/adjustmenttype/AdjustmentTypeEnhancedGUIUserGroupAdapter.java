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

package com.trilogy.app.crm.adjustmenttype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeLimitProperty;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.ExtensionHolder;
import com.trilogy.app.crm.extension.usergroup.AdjustmentTypeLimitUserGroupExtension;
import com.trilogy.app.crm.home.AdjustmentTypePermissionSettingHome;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.ExtensionSupportHelper;

/**
 * Adapts between Adjustment Type Enhanced GUI and User Group.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUIUserGroupAdapter implements Adapter
{

    /**
     * Converts to Adjustment Type Enhanced GUI from User Group.
     * 
     * @see com.redknee.framework.xhome.home.Adapter#adapt(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        CRMGroup group = (CRMGroup) obj;
        AdjustmentTypeEnhancedGUI bean = new AdjustmentTypeEnhancedGUI();
        bean.setUserGroup(group.getName());
        group.setContext(ctx);
        return bean;
    }


    /**
     * Converts from enhanced GUI properties back to adjustment type permissions
     * and limits.
     * 
     * @see com.redknee.framework.xhome.home.Adapter#unAdapt(com.redknee.framework.xhome.context.Context,
     *      java.lang.Object)
     */
    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        AdjustmentTypeEnhancedGUI bean = (AdjustmentTypeEnhancedGUI) obj;
        Home home = (Home) ctx.get(CRMGroupHome.class);
        CRMGroup group = (CRMGroup) home.find(ctx, bean.getUserGroup());

        Map<Integer, AdjustmentTypeLimitProperty> newLimits = new TreeMap<Integer, AdjustmentTypeLimitProperty>();
        Map<Integer, AdjustmentTypeLimitProperty> limits = new TreeMap<Integer, AdjustmentTypeLimitProperty>();
        Map<Integer, List<Integer>> tree = new TreeMap<Integer, List<Integer>>();
        List<Integer> permitted = new LinkedList<Integer>();

        // reset all the adjustment type related permissions
        List<PermissionRow> permissions = new LinkedList<PermissionRow>();
        List<PermissionRow> adjustmentTypePermissions = new LinkedList<PermissionRow>();
        for (Object o : group.getPermissions())
        {
            PermissionRow row = (PermissionRow) o;
            if (!row
                    .getPermission()
                    .startsWith(
                            AdjustmentTypePermissionSettingHome.ADJUSTMENT_TYPE_PERMISSION_ROOT))
            {
                permissions.add(row);
            }
        }

        for (Object o : bean.getAdjustmentTypes())
        {
            AdjustmentTypeEnhancedGUIProperty property = (AdjustmentTypeEnhancedGUIProperty) o;

            // set limit
            if (property.getLimitSet() == AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX)
            {
                AdjustmentTypeLimitProperty p = new AdjustmentTypeLimitProperty();
                p.setAdjustmentType(property.getCode());
                p.setLimit(property.getLimit());
                limits.put(new Integer(property.getCode()), p);
            }

            // set permission
            if (property.getPermission() == AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX)
            {
                AdjustmentType adjustmentType = AdjustmentTypeSupportHelper
                        .get(ctx).getAdjustmentType(ctx, property.getCode());
                String permission = null;
                if (adjustmentType != null)
                {
                	if (!permitted.contains(new Integer(adjustmentType.getParentCode())))
                	{
	                    permission = adjustmentType.getPermission();
	                    if (adjustmentType.isCategory())
	                    {
	                        permission = permission + ".*";
	                    }
                	}
                	else
                	{
                		permitted.add(new Integer(property.getCode()));
                	}
                }
                else if (property.getCode() == 0)
                {
                	permission = AdjustmentTypePermissionSettingHome.ADJUSTMENT_TYPE_PERMISSION_ROOT + ".*";
                }
                
                if (permission != null)
                {
                    PermissionRow row = new PermissionRow();
                    row.setPermission(permission);
                    adjustmentTypePermissions.add(row);
                    permitted.add(new Integer(property.getCode()));
                }
            }

            List<Integer> list = tree
                    .get(new Integer(property.getParentCode()));
            if (list == null)
            {
                list = new LinkedList<Integer>();
            }
            list.add(new Integer(property.getCode()));
            tree.put(new Integer(property.getParentCode()), list);
        }

        // only store limits of permitted adjustment types
        while (!permitted.isEmpty())
        {
            Integer k = permitted.remove(0);
            if (tree.containsKey(k))
            {
                permitted.addAll(tree.get(k));
            }
            if (limits.containsKey(k))
            {
                newLimits.put(k, limits.get(k));
            }
        }

        permissions.addAll(adjustmentTypePermissions);
        group.setPermissions(permissions);

        Collection<Extension> extensions = group.getExtensions();
        List<Extension> newExtensions = new ArrayList<Extension>();
        AdjustmentTypeLimitUserGroupExtension extension = null;
        
        for (Extension e : extensions)
        {
            if (e instanceof AdjustmentTypeLimitUserGroupExtension)
            {
                extension = (AdjustmentTypeLimitUserGroupExtension) e;
            }
            else
            {
                newExtensions.add(e);
            }
        }

        if (extension == null)
        {
            extension = new AdjustmentTypeLimitUserGroupExtension();
        }

        extension.setGroupName(group.getName());
        extension.setLimits(newLimits);
        newExtensions.add(extension);

        List<ExtensionHolder> newExtensionHolders = ExtensionSupportHelper.get(
                ctx).wrapExtensions(ctx, newExtensions);
        group.setUserGroupExtensions(newExtensionHolders);

        return group;
    }

}
