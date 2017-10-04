package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.filter.SystemAdjustmentTypePredicate;
import com.trilogy.app.crm.web.control.AdjustmentTypeActionWebControl;
import com.trilogy.app.crm.web.control.ReadOnlyOnPredicateWebControl;

public class CRMAdjustmentTypeWebControl extends CustomAdjustmentTypeWebControl 
{

    @Override
    public WebControl getActionWebControl()
    {
        return CUSTOM_ACTION_WC;
    }
    
    public static final WebControl CUSTOM_ACTION_WC = new ReadOnlyOnPredicateWebControl(new AdjustmentTypeActionWebControl(), new SystemAdjustmentTypePredicate());
    
}
