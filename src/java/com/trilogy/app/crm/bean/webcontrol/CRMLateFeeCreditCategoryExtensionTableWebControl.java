package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtensionTableWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMLateFeeCreditCategoryExtensionTableWebControl extends LateFeeCreditCategoryExtensionTableWebControl
{
    @Override
    public WebControl getConfigurationsWebControl()
    {
        return CRMLateFeeCreditCategoryExtensionWebControl.CRM_custom_config_wc;
    }
}
