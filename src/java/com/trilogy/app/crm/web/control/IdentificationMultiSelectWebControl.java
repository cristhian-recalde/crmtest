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
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationHome;
import com.trilogy.app.crm.bean.IdentificationIdentitySupport;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.bean.SpidIdentificationGroups;

/**
 * MultiSelect web control for identification (Spid filtered).
 * @author marcio.marques@redknee.com 
 *
 */
public class IdentificationMultiSelectWebControl extends MultiSelectWebControl
{
    public IdentificationMultiSelectWebControl()
    {
        super(IdentificationHome.class, IdentificationIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     Identification bean = (Identification)obj;
                     out.print(bean.getCode() + " - " + bean.getDesc());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of Identification List multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });
    }

    public Home getHome(Context ctx)
    {
       return (Home) filterSpid(ctx);  
    }

    private static Home filterSpid(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(IdentificationHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object spidIdentificationGroups = ctx.get(SpidIdentificationGroups.class);
        int spid = -1;
        
        if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
        else if (spidIdentificationGroups instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) spidIdentificationGroups;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(IdentificationXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            return newHome;
        }
        return originalHome;
    }


}
