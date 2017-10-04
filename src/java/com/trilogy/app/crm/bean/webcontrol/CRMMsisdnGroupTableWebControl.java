package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;


public class CRMMsisdnGroupTableWebControl extends CustomMsisdnGroupTableWebControl
{

    @Override
    public WebControl getFeeWebControl()
    {
        return CRMMsisdnGroupWebControl.CUSTOM_ADJUSTMENT_TYPE_WC;

    }

}
