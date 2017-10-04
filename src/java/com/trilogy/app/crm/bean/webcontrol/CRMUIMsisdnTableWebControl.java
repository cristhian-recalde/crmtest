package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.ui.MsisdnTableWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author sbanerjee
 *
 */
public class CRMUIMsisdnTableWebControl extends MsisdnTableWebControl
{

    public CRMUIMsisdnTableWebControl(Context ctx)
    {
    }
    
    @Override
    public WebControl getGroupWebControl()
    {
        WebControl groupWC = super.getGroupWebControl();
        WebControl wc = WebControlProxySupport.find(ContextLocator.locate(), groupWC, SetSpidProxyWebControl.class);
        return (wc instanceof ProxyWebControl) && ((ProxyWebControl)wc).getDelegate()!=null ? 
                ((ProxyWebControl)wc).getDelegate() : groupWC;
    }

}
