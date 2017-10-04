package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author sbanerjee
 *
 */
public class WebControlProxySupport
{
    public static WebControl find(Context ctx, WebControl wcChain, Class<? extends WebControl> delegateClzName)
    {
        if(delegateClzName.isInstance(wcChain))
            return wcChain;
        
        if(wcChain instanceof ProxyWebControl)
            return find(ctx, ((ProxyWebControl)wcChain).getDelegate(), delegateClzName);
        
        return null;
    }
}
