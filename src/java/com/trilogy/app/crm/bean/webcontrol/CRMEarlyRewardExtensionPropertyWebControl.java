package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.EarlyRewardExtensionPropertyWebControl;
import com.trilogy.app.crm.extension.ExtensionSetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMEarlyRewardExtensionPropertyWebControl extends EarlyRewardExtensionPropertyWebControl
{
    @Override
    public WebControl getConfigurationWebControl()
    {
        return CRM_CUSTOM_CONFIG_WC;
    }

    public static final WebControl CRM_CUSTOM_CONFIG_WC = new ExtensionSetSpidProxyWebControl(new com.redknee.app.crm.bean.EarlyRewardConfigurationKeyWebControl());
}
