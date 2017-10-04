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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.SubscriptionClass;
import com.trilogy.app.crm.move.request.ConvertAccountBillingTypeRequest;

/**
 * 
 * @author daniel.lee@redknee.com
 *
 */

public class FilterSubscriptionClassesOnAccountBillingType
    extends ProxyWebControl
{
    public FilterSubscriptionClassesOnAccountBillingType(final WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public Context wrapContext(final Context ctx)
    {
        
        Account account = null;
        String ban = "";
        boolean isConversion = false;
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
                isConversion = true;
            }
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to filter Subscription Class beans", e);
            return super.wrapContext(ctx);
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

        final Object homeKey = getHomeKey(ctx);
        final Context subCtx = SubscriptionClass.filterHomeOnBillingType(ctx, account.getSystemType(), homeKey, isConversion);

        return super.wrapContext(subCtx);
    }

    private Object getHomeKey(final Context ctx)
    {
        boolean found = false;
        Object ret = null;
        WebControl delegate = this;

        while(!found && null != delegate && (delegate instanceof ProxyWebControl))
        {
            delegate = ((ProxyWebControl)delegate).getDelegate();
            if(delegate instanceof AbstractKeyWebControl)
            {
                found = true;
            }
        }

        if(found)
        {
            ret = ((AbstractKeyWebControl)delegate).getHomeKey();
        }
        else
        {
            LogSupport.major(ctx, this, "System Error: This webcontrol must be used with a KeyWebControl in it's delegate list.");
        }

        return ret;
    }
}