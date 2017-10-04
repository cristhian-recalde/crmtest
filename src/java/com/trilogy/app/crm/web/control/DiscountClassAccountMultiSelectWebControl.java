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
import com.trilogy.app.crm.bean.AccountsDiscount;
import com.trilogy.app.crm.bean.AccountsDiscountXInfo;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassHome;
import com.trilogy.app.crm.bean.DiscountClassIdentitySupport;
import com.trilogy.app.crm.bean.DiscountClassXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.MultiSelectWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * Multi select web control used to display bundle profiles.
 * @author Ankit Nagpal
 * @since 9_7_2
 * 
 */
public class DiscountClassAccountMultiSelectWebControl extends MultiSelectWebControl
{
    
    public DiscountClassAccountMultiSelectWebControl()
    {
        super(DiscountClassHome.class, DiscountClassIdentitySupport.instance(), 
                new com.redknee.framework.xhome.webcontrol.OutputWebControl()
        {
             public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
             {
                 try {
                     DiscountClass bean = (DiscountClass)obj;
                     out.print(bean.getId() + " - " + bean.getName());
                 }
                 catch (Exception e)
                 {
                	 new MinorLogMsg(this, "Error during output of discount class multi-select webcontrol. ", e).log(ctx); 
                 }
             }
        });    
    }

    public Home getHome(Context ctx)
    {
        Context subCtx = filterServiceDiscount(filterSpid(ctx));
       return (Home) subCtx.get(DiscountClassHome.class);  
    }

    private static Context filterSpid(final Context ctx)
    {
        final Home originalHome = (Home) ctx.get(DiscountClassHome.class);
        final Object obj = ctx.get(AbstractWebControl.BEAN);
        final Object discountClassTemplate = ctx.get(DiscountClass.class);
        int spid = -1;
        
        if (obj instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) obj;
            spid = spidAware.getSpid();
        }
        else if (discountClassTemplate instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) discountClassTemplate;
            spid = spidAware.getSpid();
        }
           
        if (spid!=-1)
        {
            final Predicate filterSpid = new EQ(DiscountClassXInfo.SPID, Integer.valueOf(spid));
            final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filterSpid);
            final Context subCtx = ctx.createSubContext();
            subCtx.put(DiscountClassHome.class, newHome);
            return subCtx;
        }
        return ctx;
    }
    
    private static Context filterServiceDiscount(final Context ctx)
    {
		final Home originalHome = (Home) ctx.get(DiscountClassHome.class);

		And dcAnd = new And();
		dcAnd.add(new EQ(DiscountClassXInfo.ENABLE_SERVICE_LEVEL_DISCOUNT, Boolean.TRUE));

		final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, dcAnd);
		final Context subCtx = ctx.createSubContext();
		subCtx.put(DiscountClassHome.class, newHome);
		return subCtx;
	}
    
    protected void outputSubList(Context ctx,
            PrintWriter out,
            Home home,
            String name,
            Set set)
    {
    	Account account = (Account) ctx.get(AbstractWebControl.BEAN);
        Set<DiscountClass> discountClassHolder = new HashSet<DiscountClass>();
        And and = new And();
        and.add(new EQ(AccountsDiscountXInfo.BAN, account.getBAN()));
        and.add(new EQ(AccountsDiscountXInfo.SPID, account.getSpid()));
        Collection<AccountsDiscount> coll = null;
		try {
			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
        
	        for (AccountsDiscount accountsDiscount : coll)
	        {
	        	DiscountClass dc = HomeSupportHelper.get(ctx).findBean(ctx, DiscountClass.class, new EQ(DiscountClassXInfo.ID, accountsDiscount.getDiscountClass()));
	        	if (dc != null)
	        	{
	        		discountClassHolder.add(dc);
	        	}
	        }
	        super.outputSubList(ctx, out, home, name, discountClassHolder);
        
		} catch (HomeException e) {
			new MinorLogMsg(this, "Error during output of discount class multi-select webcontrol. ", e).log(ctx); 
		}
        
    }
    
    @Override
	protected void outputWholeList(Context ctx, PrintWriter out, Home home,
			String name, Set selected) 
	{
    	Account account = (Account) ctx.get(AbstractWebControl.BEAN);
        Set<DiscountClass> discountClassHolder = new HashSet<DiscountClass>();
        And and = new And();
        and.add(new EQ(AccountsDiscountXInfo.BAN, account.getBAN()));
        and.add(new EQ(AccountsDiscountXInfo.SPID, account.getSpid()));
        Collection<AccountsDiscount> coll = null;
		try {
			coll = HomeSupportHelper.get(ctx).getBeans(ctx, AccountsDiscount.class, and);
        
	        for (AccountsDiscount accountsDiscount : coll)
	        {
	        	DiscountClass dc = HomeSupportHelper.get(ctx).findBean(ctx, DiscountClass.class, new EQ(DiscountClassXInfo.ID, accountsDiscount.getDiscountClass()));
	        	discountClassHolder.add(dc);
	        }
	        super.outputWholeList(ctx, out, home, name, discountClassHolder);
        
		} catch (HomeException e) {
			new MinorLogMsg(this, "Error during output of discount class multi-select webcontrol. ", e).log(ctx); 
		}
        
    
	}

}
