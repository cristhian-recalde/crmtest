package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.extension.creditcategory.LateFeeCreditCategoryExtensionWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMLateFeeCreditCategoryExtensionWebControl extends LateFeeCreditCategoryExtensionWebControl
{
    @Override
    public WebControl getConfigurationsWebControl()
    {
        return CRM_custom_config_wc;
    }

    public static final WebControl CRM_custom_config_wc = new com.redknee.app.crm.bean.webcontrol.CRMLateFeeExtensionPropertyTableWebControl();
}
