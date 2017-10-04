package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

public class CRMMsisdnTableWebControl extends CustomMsisdnTableWebControl
{
    @Override
    public WebControl getStateWebControl()
    {
        return CRMMsisdnWebControl.CUSTOM_STATE_WC;
    }
}
