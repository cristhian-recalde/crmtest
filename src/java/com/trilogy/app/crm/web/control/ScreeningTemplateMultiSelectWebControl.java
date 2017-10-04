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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.app.crm.bean.ScreeningTemplateHome;
import com.trilogy.app.crm.bean.ScreeningTemplateIdentitySupport;
import com.trilogy.app.crm.bean.ScreeningTemplateXInfo;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporterSubExtension;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Multi select web control used to display screening templates.
 * @author Marcio Marques
 * @since 8.5
 * 
 */

public class ScreeningTemplateMultiSelectWebControl extends DescriptionEnabledMultiSelectWebControl
{
    private Class parentBeanClass_ = null;
    public ScreeningTemplateMultiSelectWebControl()
    {
        super(ScreeningTemplateHome.class, ScreeningTemplateIdentitySupport.instance(), ScreeningTemplateXInfo.instance(),
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     ScreeningTemplate bean = (ScreeningTemplate)obj;
                     out.print(bean.getIdentifier() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of Screening Template multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        }
        ,
        new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     ScreeningTemplate bean = (ScreeningTemplate)obj;
                     out.print(bean.getDescription());
                 }
                 catch (Exception e)
                 {
                     if (LogSupport.isDebugEnabled(ctx)) {
                         new DebugLogMsg(this, "Error during output of Screening Template List multi-select webcontrol. ", e).log(ctx); 
                     } 
                 }
             }
        });
    }

    public Home getHome(Context ctx)
    {
       final Home originalHome = (Home) ctx.get(ScreeningTemplateHome.class);
       return (Home) filterDeprecated(ctx, filterSpid(ctx, originalHome));  
    }

    private static Home filterSpid(final Context ctx, final Home originalHome)
    {
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object subscriber = ctx.get(Subscriber.class);
        int spid = -1;
        
        if (subscriber instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) subscriber;
            spid = spidAware.getSpid();
        }
        else if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(ScreeningTemplateXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            return newHome;
        }
        return originalHome;
    }    

    private static Home filterDeprecated(final Context ctx, final Home originalHome)
    {
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        
        Set<Long> identifiers = new HashSet<Long>();
        if (obj instanceof PPSMSupporterSubExtension)
        {
            PPSMSupporterSubExtension extension = (PPSMSupporterSubExtension) obj;
            Set<String> templates = extension.getScreeningTemplates(ctx);
            for (String templateId : templates)
            {
                identifiers.add(Long.valueOf(templateId));
            }
        }
        
        final Or filterDisabled = new Or();
        filterDisabled.add(new EQ(ScreeningTemplateXInfo.ENABLED, true));
        if (identifiers.size()>0)
        {
            filterDisabled.add(new In(ScreeningTemplateXInfo.IDENTIFIER, identifiers));
        }
        final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterDisabled);
        return newHome;
    }    
}
