package com.trilogy.app.crm.bean.webcontrol;

import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.ui.MsisdnWebControl;
import com.trilogy.app.crm.technology.SetTechnologyProxyWebControl;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * @author sbanerjee
 *
 */
public class CRMUIMsisdnWebControl extends ProxyWebControl
{
    public static final CRMUIMsisdnInternalWebControl groupWebControl = new CRMUIMsisdnInternalWebControl();

    static final class CRMUIMsisdnInternalWebControl extends MsisdnWebControl
    {
        @Override
        public WebControl getGroupWebControl()
        {
            WebControl groupWC = super.getGroupWebControl();
            WebControl wc = WebControlProxySupport.find(ContextLocator.locate(), groupWC, SetSpidProxyWebControl.class);
            return (wc instanceof ProxyWebControl) && ((ProxyWebControl)wc).getDelegate()!=null ? 
                    ((ProxyWebControl)wc).getDelegate() : groupWC;
        }
    }
    
    /**
     * 
     */
    protected CRMUIMsisdnWebControl()
    {
        super();
    }

    /**
     * @param ctx
     */
    public CRMUIMsisdnWebControl(Context ctx)
    {
        this();
        
        setDelegate(!GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx) ? 
                new SetTechnologyProxyWebControl((WebControl) XBeans.getInstanceOf(ctx, com.redknee.app.crm.bean.ui.Msisdn.class, WebControl.class)) :
                    groupWebControl);
    }

}
