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

package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUI;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPermissionEnum;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIProperty;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIPropertyWebControl;
import com.trilogy.app.crm.adjustmenttype.AdjustmentTypeEnhancedGUIUserGroupAdapter;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * Web control for Adjustment type Enhanced GUI.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.3
 */
public class AdjustmentTypeEnhancedGUIPropertyCustomWebControl extends
        AdjustmentTypeEnhancedGUIPropertyWebControl
{

    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        AdjustmentTypeEnhancedGUIProperty bean = (AdjustmentTypeEnhancedGUIProperty) obj;
        super.fromWeb(ctx, bean, req, name);
        AdjustmentType adjustmentType = null;
        try
        {
            adjustmentType = AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, bean
                    .getCode());
        }
        catch (HomeException exception)
        {
            throw new NullPointerException("No data");
        }
        
        if (adjustmentType == null)
        {
            AdjustmentTypeEnhancedGUIProperty p = AdjustmentTypeEnhancedGUI.createRoot(bean.getPermission() == AdjustmentTypeEnhancedGUIPermissionEnum.ALLOWED_INDEX);
            bean.setCategory(p.getCategory());
            bean.setName(p.getName());
            bean.setParentCode(p.getParentCode());
        }
        else
        {
            bean.setCategory(adjustmentType.isCategory());
            bean.setName(adjustmentType.getName());
            bean.setParentCode(adjustmentType.getParentCode());
        }
    }

    public WebControl getCodeWebControl()
    {
        return codeWebControl_;
    }

    protected WebControl codeWebControl_ = new AdjustmentTypeEnhancedGUIPropertyCodeWebControl();
}
