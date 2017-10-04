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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.account.AccountRoleXInfo;
import com.trilogy.app.crm.bean.account.SubscriptionClassXInfo;
import com.trilogy.app.crm.bean.account.AccountRoleHome;
import com.trilogy.app.crm.bean.account.AccountRole;
import com.trilogy.app.crm.bean.account.SubscriptionClassRow;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;

/**
 * Filters the home used by the KeyWebControl delegate based on the bean property value and PropertyInfos
 * provided that guides this class to do the proper filtering.
 *
 * @auditor victor.stratan@redknee.com
 */
public class FilterSubTypesOnAccountRole extends ProxyWebControl
{
    public FilterSubTypesOnAccountRole(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * Alters the Home stored in the context
     *
     * @param ctx the operating context
     * @return the altered subcontext
     */
    public Context wrapContext(final Context ctx)
    {

        Account account = null;
        String ban = "";

        try
        {
            if (  ctx.get(AbstractWebControl.BEAN) instanceof Subscriber )
            {
                final Subscriber bean = (Subscriber) ctx.get(AbstractWebControl.BEAN);
                account = bean.getAccount(ctx);
                ban = account.getBAN();
            }
            else if ( ctx.get(AbstractWebControl.BEAN) instanceof ConvertAccountBillingTypeRequest )
            {
                account = (Account) ctx.get(Account.class);
                ban = account.getBAN();
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to filter Subscription Class beans", e);
            return ctx;
        }

        if (account == null)
        {
            if (LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Unable to filter Subscription Class because Account with BAN ["
                        + ban + "] cannot be located.");
            }
            return super.wrapContext(ctx);
        }

        final Context subCtx = ctx.createSubContext();
        subCtx.setName(this.getClass().getSimpleName());
        final Object homeKey = ((AbstractKeyWebControl) getDelegate()).getHomeKey();
        final Home home = (Home) subCtx.get(homeKey);

        final long roleID = account.getRole();
        final Object roleHomeKey = AccountRoleHome.class;

        final Home roleHome = (Home) subCtx.get(roleHomeKey);

        try
        {
            final AccountRole roleObject = (AccountRole) roleHome.find(ctx, Long.valueOf(roleID));
            final List filteringList = roleObject.getAllowedSubscriptionClass();
            final Set filteringSet = new HashSet(filteringList.size());
            final Iterator iter = filteringList.iterator();
            while (iter.hasNext())
            {
                final SubscriptionClassRow row = (SubscriptionClassRow) iter.next();
                filteringSet.add(Long.valueOf(row.getSubscriptionClass()));
            }
            final Home alteredHome = home.where(ctx, new In(SubscriptionClassXInfo.ID, filteringSet));
            subCtx.put(homeKey, alteredHome);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to filter Subscription Class beans", e);
        }

        return super.wrapContext(subCtx);
    }
}