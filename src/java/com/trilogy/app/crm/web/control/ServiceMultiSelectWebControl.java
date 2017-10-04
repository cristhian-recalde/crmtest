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

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceIdentitySupport;
import com.trilogy.app.crm.bean.ServiceXInfo;
import com.trilogy.app.crm.bean.SubscriptionTypeAware;
import com.trilogy.app.crm.bean.ui.ChargingTemplate;

/**
 * Multi select web control used to display services.
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class ServiceMultiSelectWebControl extends MultiSelectWebControl
{
    public ServiceMultiSelectWebControl()
    {
        super(ServiceHome.class, ServiceIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     Service bean = (Service)obj;
                     out.print(bean.getIdentifier() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of service multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });

        setComparator(new Comparator(){

            @Override
            public int compare(Object arg0, Object arg1)
            {
                Service b0= (Service) arg0;
                Service b1= (Service) arg1;
                
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

    @Override
    public Home getHome(Context ctx)
    {
       final Home originalHome = (Home) ctx.get(ServiceHome.class);
       return filterSubscriptionType(ctx, filterSpid(ctx, originalHome));  
    }


    public static Home filterSpid(final Context ctx, final Home originalHome)
    {
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
            final Predicate filterSpid = new EQ(ServiceXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            return newHome;
        }
        return originalHome;
    }    

    private static Home filterSubscriptionType(final Context ctx, final Home originalHome)
    {
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object chargingTemplate = ctx.get(ChargingTemplate.class);
        long subscriptionType = -1;
                   
        if (obj instanceof SubscriptionTypeAware)
        {
            final SubscriptionTypeAware subscriptionTypeAware = (SubscriptionTypeAware) obj;
            subscriptionType = subscriptionTypeAware.getSubscriptionType(ctx).getId();
        }
        else if (chargingTemplate instanceof SubscriptionTypeAware)
        {
            final SubscriptionTypeAware subscriptionTypeAware = (SubscriptionTypeAware) chargingTemplate;
            subscriptionType = subscriptionTypeAware.getSubscriptionType(ctx).getId();
        }

        if (subscriptionType!=-1)
        {
            final Predicate filter = new EQ(ServiceXInfo.SUBSCRIPTION_TYPE, Long.valueOf(subscriptionType));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filter);
            return newHome;
        }
        
        return originalHome;
    }    

}
