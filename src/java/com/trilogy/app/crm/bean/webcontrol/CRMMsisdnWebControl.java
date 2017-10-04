package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.web.control.MSISDNStateEnumWebControl;

public class CRMMsisdnWebControl extends CustomMsisdnWebControl
{
    @Override
    public WebControl getStateWebControl()
    {
        return CUSTOM_STATE_WC;
    }
    
    public static final WebControl CUSTOM_STATE_WC = new MSISDNStateEnumWebControl(MsisdnStateEnum.COLLECTION);
    
}
