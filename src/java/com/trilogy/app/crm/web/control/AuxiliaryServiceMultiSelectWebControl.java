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
import java.util.Comparator;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceIdentitySupport;
import com.trilogy.app.crm.bean.AuxiliaryServiceXInfo;
import com.trilogy.app.crm.bean.ChargingTemplate;
import com.trilogy.framework.xhome.beans.XBeans;
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


/**
 * Multi select web control used to display auxiliary services.
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class AuxiliaryServiceMultiSelectWebControl extends MultiSelectWebControl
{
    public AuxiliaryServiceMultiSelectWebControl()
    {
        super(AuxiliaryServiceHome.class, AuxiliaryServiceIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     AuxiliaryService bean = (AuxiliaryService)obj;
                     out.print(bean.getIdentifier() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of auxiliary service multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });
        
        setComparator(new Comparator(){

            @Override
            public int compare(Object arg0, Object arg1)
            {
                AuxiliaryService b0= (AuxiliaryService) arg0;
                AuxiliaryService b1= (AuxiliaryService) arg1;
                
                if (b0==null || b1==null || b0.getIdentifier()==b1.getIdentifier())
                {
                    return 0;
                }
                else if (b0.getIdentifier()>b1.getIdentifier())
                {
                    return 1;
                }
                else
                {
                    return -1;
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
        final Home originalHome = (Home) ctx.get(AuxiliaryServiceHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object chargingTemplate = ctx.get(ChargingTemplate.class);
        int spid = -1;
        
        if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
        else if (chargingTemplate instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) chargingTemplate;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(AuxiliaryServiceXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            return newHome;
        }
        return originalHome;
    }


}
