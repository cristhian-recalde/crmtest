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
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.msp.SpidAwareXInfo;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.support.HomeSupportHelper;


/**
 * Spid aware web control proxy that also includes spid -1 (ANY) in filtered selections
 * 
 * @author ltang
 */
public class SpidAwareKeyWebControlProxy extends KeyWebControlProxy
{

    public SpidAwareKeyWebControlProxy(AbstractKeyWebControl delegate)
    {
        super(delegate);
    }


    public SpidAwareKeyWebControlProxy(KeyWebControlProxy delegate)
    {
        super(delegate);
    }


    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Home home = getHome(ctx);
        if (home == null)
        {
            out.print(("<font color=\"red\">Error: No Home supplied in Context under key '" + getHomeKey() + "'.</font>"));
            return;
        }

            Object bean = ctx.get(AbstractWebControl.BEAN);
            if (bean instanceof SpidAware)
            {
                Or filter = new Or().add(new EQ(SpidAwareXInfo.SPID, -1));
                
                int spid = ((SpidAware) bean).getSpid();
                try
                {
                    if (HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, spid) != null)
                    {
                        filter.add(new EQ(SpidAwareXInfo.SPID, spid));
                    }
                    
                    ctx = ctx.createSubContext().put(getHomeKey(), home.where(ctx, filter));
                }
                catch (HomeException e)
                {
                    new MinorLogMsg(this, "Error filtering key web control by SPID " + spid, e).log(ctx);
                }
            }

        super.toWeb(ctx, out, name, obj);
    }

}
