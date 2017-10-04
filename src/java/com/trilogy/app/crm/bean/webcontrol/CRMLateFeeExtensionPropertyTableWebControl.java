package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.LateFeeExtensionPropertyTableWebControl;

public class CRMLateFeeExtensionPropertyTableWebControl extends
    LateFeeExtensionPropertyTableWebControl
{
	@Override
	public WebControl getConfigurationWebControl()
	{
		return CRMLateFeeExtensionPropertyWebControl.CRM_CUSTOM_CONFIG_WC;
	}
}
