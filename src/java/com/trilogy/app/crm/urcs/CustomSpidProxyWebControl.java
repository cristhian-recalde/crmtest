/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.urcs;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

/**
 * The method overrides the toWeb method and set the spid value of AbstractWebControl.BEAN to the value of parent bean
 * TT #     13060611016
 * 
 * @author ankit.nagpal@redknee.com
 */
public class CustomSpidProxyWebControl extends ProxyWebControl
{
    
    public CustomSpidProxyWebControl(WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        SpidAware bean = (SpidAware) ctx.get(AbstractWebControl.BEAN);
        SpidAware service = null;
        if(ctx.get(AbstractWebControl.BEAN) instanceof com.redknee.app.crm.extension.service.ServiceExtension)
        {
            service = (SpidAware)(((com.redknee.app.crm.extension.service.ServiceExtension)ctx.get(AbstractWebControl.BEAN)).getParentBean(ctx));
        }
        else if(ctx.get(AbstractWebControl.BEAN) instanceof com.redknee.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension)
        {
            service = (SpidAware)(((com.redknee.app.crm.extension.auxiliaryservice.AuxiliaryServiceExtension)ctx.get(AbstractWebControl.BEAN)).getParentBean(ctx));
        }
        else
        {
            service = bean;
        }
        bean.setSpid(service.getSpid());
        getDelegate(ctx).toWeb(wrapContext(ctx), out, name, obj);
    }

}
