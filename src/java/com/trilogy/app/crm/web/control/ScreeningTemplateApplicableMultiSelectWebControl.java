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
import java.util.Set;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccntGrpScreeningTemp;
import com.trilogy.app.crm.bean.AccntGrpScreeningTempXInfo;
import com.trilogy.app.crm.bean.GroupScreeningTemplate;
import com.trilogy.app.crm.bean.GroupScreeningTemplateHome;
import com.trilogy.app.crm.bean.GroupScreeningTemplateIdentitySupport;
import com.trilogy.app.crm.bean.GroupScreeningTemplateXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Multi select web control used to display group screening template.
 * @author Ankit Nagpal
 * @since 9_9
 * 
 */
public class ScreeningTemplateApplicableMultiSelectWebControl extends MultiSelectWebControl
{
    
    public ScreeningTemplateApplicableMultiSelectWebControl()
    {
        super(GroupScreeningTemplateHome.class, GroupScreeningTemplateIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                	 GroupScreeningTemplate bean = (GroupScreeningTemplate)obj;
                     out.print(bean.getIdentifier() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                	 new MinorLogMsg(this, "Error during output of Screening template multi-select webcontrol. ", e).log(ctx); 
                 }
             }
        });    
    }

    public Home getHome(Context ctx)
    {
        Context subCtx = filterActiveScreeningTemplate(filterSpid(ctx));
       return (Home) subCtx.get(GroupScreeningTemplateHome.class);  
    }

    private static Context filterSpid(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(GroupScreeningTemplateHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object groupScreeningTemplate = ctx.get(GroupScreeningTemplate.class);
        int spid = -1;
        
        if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
        else if (groupScreeningTemplate instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) groupScreeningTemplate;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(GroupScreeningTemplateXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(GroupScreeningTemplateHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }
    
    private static Context filterActiveScreeningTemplate(final Context ctx)
    {
		final Home originalHome = (Home) ctx.get(GroupScreeningTemplateHome.class);

		And dcAnd = new And();
		dcAnd.add(new EQ(GroupScreeningTemplateXInfo.ACTIVE, Boolean.TRUE));

		final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, dcAnd);
		final Context subCtx = ctx.createSubContext();
		subCtx.put(GroupScreeningTemplateHome.class, newHome);
		return subCtx;
	}
    
    protected void outputSubList(Context ctx,
            PrintWriter out,
            Home home,
            String name,
            Set set)
    {
    	Account account = (Account) ctx.get(AbstractWebControl.BEAN);
        Set<GroupScreeningTemplate> groupScreeningTemplateHolder = new HashSet<GroupScreeningTemplate>();
        And and = new And();
        and.add(new EQ(AccntGrpScreeningTempXInfo.BAN, account.getBAN()));
        and.add(new EQ(AccntGrpScreeningTempXInfo.SPID, account.getSpid()));
        Collection<AccntGrpScreeningTemp> coll = null;
		try {
			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccntGrpScreeningTemp.class, and);
        
	        for (AccntGrpScreeningTemp accountsGroupScreeningTemplate : coll)
	        {
	        	GroupScreeningTemplate gst = HomeSupportHelper.get(ctx).findBean(ctx, GroupScreeningTemplate.class, new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, accountsGroupScreeningTemplate.getScreeningTemplate()));
	        	if (gst != null)
	        	{
	        		groupScreeningTemplateHolder.add(gst);
	        	}
	        }
	        super.outputSubList(ctx, out, home, name, groupScreeningTemplateHolder);
        
		} catch (HomeException e) {
			new MinorLogMsg(this, "Error during output of group screening template multi-select webcontrol. ", e).log(ctx); 
		}
        
    }
    
    @Override
	protected void outputWholeList(Context ctx, PrintWriter out, Home home,
			String name, Set selected) 
	{
    	Account account = (Account) ctx.get(AbstractWebControl.BEAN);
        Set<GroupScreeningTemplate> groupScreeningTemplateHolder = new HashSet<GroupScreeningTemplate>();
        And and = new And();
        and.add(new EQ(AccntGrpScreeningTempXInfo.BAN, account.getBAN()));
        and.add(new EQ(AccntGrpScreeningTempXInfo.SPID, account.getSpid()));
        Collection<AccntGrpScreeningTemp> coll = null;
		try {
			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccntGrpScreeningTemp.class, and);
        
	        for (AccntGrpScreeningTemp accountsGroupScreeningTemplate : coll)
	        {
	        	GroupScreeningTemplate gst = HomeSupportHelper.get(ctx).findBean(ctx, GroupScreeningTemplate.class, new EQ(GroupScreeningTemplateXInfo.IDENTIFIER, accountsGroupScreeningTemplate.getScreeningTemplate()));
	        	groupScreeningTemplateHolder.add(gst);
	        }
	        super.outputWholeList(ctx, out, home, name, groupScreeningTemplateHolder);
        
		} catch (HomeException e) {
			new MinorLogMsg(this, "Error during output of group screening template multi-select webcontrol ", e).log(ctx); 
		}
        
    
	}
    
}
