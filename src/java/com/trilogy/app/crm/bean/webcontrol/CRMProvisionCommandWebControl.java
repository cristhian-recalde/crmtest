package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.webcontrol.FinalWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Subscriber;


public class CRMProvisionCommandWebControl extends CustomProvisionCommandWebControl
{
    @Override
    public WebControl getTypeWebControl()
    {
        return CUSTOM_TYPE_WC;
    }
    
    public static final WebControl CUSTOM_TYPE_WC = new FinalWebControl(new EnumWebControl(
            Subscriber.SUBSCRIBERTYPE_COLLECTION));
}
