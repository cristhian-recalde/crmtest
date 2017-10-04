package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.EarlyRewardExtensionPropertyTableWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMEarlyRewardExtensionPropertyTableWebControl extends EarlyRewardExtensionPropertyTableWebControl
{
    @Override
    public WebControl getConfigurationWebControl()
    {
        return CRMEarlyRewardExtensionPropertyWebControl.CRM_CUSTOM_CONFIG_WC;
    }
}
