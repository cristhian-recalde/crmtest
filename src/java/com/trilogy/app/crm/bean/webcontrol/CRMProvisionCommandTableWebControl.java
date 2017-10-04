package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMProvisionCommandTableWebControl extends CustomProvisionCommandTableWebControl
{
    @Override
    public WebControl getTypeWebControl()
    {
        return CRMProvisionCommandWebControl.CUSTOM_TYPE_WC;
    }
}
