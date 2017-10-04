package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.MsisdnBulkWebControl;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author sbanerjee
 *
 */
public class CRMMsisdnBulkWebControl extends MsisdnBulkWebControl
{

    public CRMMsisdnBulkWebControl(Context ctx)
    {
    }
    
    public CRMMsisdnBulkWebControl()
    {
    }
    
    @Override
    public WebControl getGroupWebControl()
    {
        WebControl groupWC = super.getGroupWebControl();
        
        return unwrapSpidAwareness(groupWC);
    }


    @Override
    public WebControl getVanityGroupWebControl()
    {
        WebControl groupWC = super.getVanityGroupWebControl();
        
        return unwrapSpidAwareness(groupWC);
    }
    
    /**
     * @param groupWC
     * @return
     */
    protected WebControl unwrapSpidAwareness(WebControl groupWC)
    {
        if(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ContextLocator.locate()))
            return groupWC;
        
        
        WebControl wc = WebControlProxySupport.find(ContextLocator.locate(), groupWC, SetSpidProxyWebControl.class);
        return (wc instanceof ProxyWebControl) && ((ProxyWebControl)wc).getDelegate()!=null ? 
                ((ProxyWebControl)wc).getDelegate() : groupWC;
    }
    
}