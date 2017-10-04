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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeLimitProperty;
import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.app.crm.bean.CRMGroupHome;
import com.trilogy.app.crm.home.AdjustmentTypePermissionSettingHome;
import com.trilogy.app.crm.support.AuthSupport;
import com.trilogy.framework.xhome.auth.SimplePermission;
import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * @author cindy.wong@redknee.com
 * 
 */
public class AdjustmentTypeEnhancedGUIBuildingVisitor implements Visitor
{

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public AdjustmentTypeEnhancedGUIBuildingVisitor(CRMGroup group,
            Map<Integer, AdjustmentTypeLimitProperty> limits,
            boolean rootAllowed)
    {
        this.group_ = group;
        this.limits_ = new HashMap<Integer, AdjustmentTypeLimitProperty>(limits);
        allowed_.put(new Integer(0), Boolean.valueOf(rootAllowed));
    }

    @Override
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        AdjustmentType adjustmentType = (AdjustmentType) obj;
        AdjustmentTypeEnhancedGUIProperty property = new AdjustmentTypeEnhancedGUIProperty();
        property.setParentCode(adjustmentType.getParentCode());
        property.setCode(adjustmentType.getCode());
        property.setName(adjustmentType.getName());
        property.setCategory(adjustmentType.isCategory());
        Integer key = new Integer(adjustmentType.getCode());
        Integer parentKey = new Integer(adjustmentType.getParentCode());

        // set depth
        if (adjustmentType.getParentCode() == 0)
        {
            property.setLevel(1);
        }
        else
        {
            property
                    .setLevel(levels_.get(
                            new Integer(adjustmentType.getParentCode()))
                            .intValue() + 1);
        }
        levels_.put(key, new Integer(property.getLevel()));

        // set limit
        if (limits_ != null && limits_.containsKey(key))
        {
            property
                    .setLimitSet(AdjustmentTypeEnhancedGUILimitEnum.CUSTOM_INDEX);
            property.setLimit(limits_.get(key).getLimit());
        }
        else
        {
            property
                    .setLimitSet(AdjustmentTypeEnhancedGUILimitEnum.INHERIT_INDEX);
            property.setLimit(limits_.get(parentKey).getLimit());
            AdjustmentTypeLimitProperty p = new AdjustmentTypeLimitProperty();
            p.setAdjustmentType(adjustmentType.getCode());
            p.setLimit(property.getLimit());
            limits_.put(key, p);
        }

        // set permission
        String permission = adjustmentType.getPermission();
        if (adjustmentType.isCategory())
        {
            permission = permission + ".*";
        }
        boolean permissionSet = AuthSupport
        .checkPermission(
                (Home) ctx.get(CRMGroupHome.class),
                group_.getName(),
                new SimplePermission(
                        permission));

        property
                .setPermission(permissionSet ? AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX
                        : AdjustmentTypeEnhancedGUIPermissionEnum.DENIED_INDEX);
        allowed_.put(key, Boolean.valueOf(permissionSet));
        property.setPermissionEditable(!allowed_.get(parentKey).booleanValue());

        SortedSet<AdjustmentTypeEnhancedGUIProperty> siblings = properties_
                .get(parentKey);
        if (siblings == null)
        {
            siblings = new TreeSet<AdjustmentTypeEnhancedGUIProperty>();
        }
        siblings.add(property);

        properties_.put(parentKey, siblings);
        queue_.add(key);
    }

    public Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> getProperties()
    {
        return properties_;
    }

    public List<Integer> getQueue()
    {
        return queue_;
    }

    public Integer dequeue()
    {
        if (!queue_.isEmpty())
        {
            return queue_.remove(0);
        }
        return null;
    }

    private Map<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>> properties_ = new TreeMap<Integer, SortedSet<AdjustmentTypeEnhancedGUIProperty>>();
    private Map<Integer, Integer> levels_ = new TreeMap<Integer, Integer>();
    private Map<Integer, AdjustmentTypeLimitProperty> limits_;
    private Map<Integer, Boolean> allowed_ = new TreeMap<Integer, Boolean>();
    private List<Integer> queue_ = new LinkedList<Integer>();
    private CRMGroup group_;

}
