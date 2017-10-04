package com.trilogy.app.crm.adjustmenttype;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.app.crm.bean.CRMGroup;
import com.trilogy.framework.xhome.auth.bean.PermissionRow;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;

/**
 * Updates the permission in user group to match the new adjustment type
 * hierarchy.
 * 
 * @author cindy.wong@redknee.com
 * 
 */
public class UserGroupPermissionUpdateVisitor implements Visitor
{

    private static final long serialVersionUID = 1L;

    public UserGroupPermissionUpdateVisitor(String oldPermission,
            String newPermission)
    {
        this.oldPermission_ = oldPermission;
        this.newPermission_ = newPermission;
    }

    @Override
    public void visit(Context ctx, Object obj) throws AgentException,
            AbortVisitException
    {
        CRMGroup group = (CRMGroup) obj;
        boolean changed = false;
        List<PermissionRow> permissions = group.getPermissions();
        List<PermissionRow> newPermissions = new ArrayList<PermissionRow>(
                permissions.size());
        for (PermissionRow row : permissions)
        {
            String permission = row.getPermission();
            if (SafetyUtil.safeEquals(permission, oldPermission_))
            {
                row.setPermission(newPermission_);
                changed = true;
            }
            else if (permission.startsWith(oldPermission_))
            {
                row.setPermission(permission.replaceFirst(oldPermission_,
                        newPermission_));
                changed = true;
            }
            newPermissions.add(row);
        }

        if (changed)
        {
            group.setPermissions(newPermissions);
            groups_.add(group);
        }
    }

    public List<CRMGroup> getUpdatedGroups()
    {
        return this.groups_;
    }

    String oldPermission_;
    String newPermission_;
    List<CRMGroup> groups_ = new ArrayList<CRMGroup>();
}
