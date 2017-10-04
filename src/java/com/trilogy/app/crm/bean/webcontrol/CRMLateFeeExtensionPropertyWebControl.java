package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.LateFeeExtensionPropertyWebControl;
import com.trilogy.app.crm.extension.ExtensionSetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

public class CRMLateFeeExtensionPropertyWebControl extends
    LateFeeExtensionPropertyWebControl
{
	@Override
	public WebControl getConfigurationWebControl()
	{
		return CRM_CUSTOM_CONFIG_WC;
	}

	public static final WebControl CRM_CUSTOM_CONFIG_WC =
	    new ExtensionSetSpidProxyWebControl(new com.redknee.app.crm.bean.LateFeeConfigurationKeyWebControl());
}
