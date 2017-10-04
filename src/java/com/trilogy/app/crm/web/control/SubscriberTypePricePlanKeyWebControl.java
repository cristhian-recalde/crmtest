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

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersion;
import com.trilogy.app.crm.bean.PricePlanXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;


public class SubscriberTypePricePlanKeyWebControl extends AbstractKeyWebControl
{
    private AbstractKeyWebControl delegate_ ;
    
    public SubscriberTypePricePlanKeyWebControl(final AbstractKeyWebControl delegate, final SubscriberTypeEnum subscriberTypeId)
    {
        setDelegate(delegate);
        this.subscriberType_ = subscriberTypeId;
    }

    public void toWeb(final Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        Context subCtx = filterPriceplanSubscriberType(ctx);
        subCtx = filterDisabledPricePlan(subCtx);

        getDelegate().toWeb(subCtx, out, name, obj);
    }
    


    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        Long ppid = (Long) getDelegate().fromWeb(ctx, req, name);
        if (ppid == null)
            return null;

        Long ppid_ = Long.valueOf(ppid.intValue());
        Home ppHome = (Home) ctx.get(PricePlanHome.class);

        PricePlan pp;
        try
        {
            pp = (PricePlan) ppHome.find(ctx, ppid_);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException(
                    "The new Priceplan is not a valid Price Plan!");
        }
        if (pp == null)
        {
            return null;
        }

       // PricePlanVersion ppv = pp.getVersions();
        int ppvid = pp.getCurrentVersion();
        if ( ppvid == PricePlan.DEFAULT_CURRENTVERSION)
        {
            throw new IllegalArgumentException("The Priceplan:[" + pp.getName()
                    + "] is not a valid Price Plan!");
        }

        return ppid;
    }
    /**
     * @return Returns the delegate.
     */
    public WebControl getDelegate()
    {
        return delegate_;
    }
    /**
     * @param delegate The delegate to set.
     */
    public void setDelegate(AbstractKeyWebControl delegate)
    {
        this.delegate_ = delegate;
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getDesc(com.redknee.framework.xhome.context.Context, java.lang.Object)
     */
    public String getDesc(Context ctx, Object bean)
    {
        return delegate_.getDesc(ctx,bean);
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getIdentitySupport()
     */
    public IdentitySupport getIdentitySupport()
    {
        return delegate_.getIdentitySupport();
    }

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.webcontrol.AbstractKeyWebControl#getHomeKey()
     */
    public Object getHomeKey()
    {
        return delegate_.getHomeKey();
    }

    void setPropertyReadOnly(Context ctx, String property)
    {
        ViewModeEnum mode = (ViewModeEnum) ctx.get(property + ".mode", ViewModeEnum.READ_WRITE);
        if (mode != ViewModeEnum.NONE)
        {
            ctx.put(property + ".mode", ViewModeEnum.READ_ONLY);
        }

    }
    


    /**
     * 
     * @param originalContext_
     * @return a sub context, and a filtered price plan home in context if its host webcontrol 
     * is subscriber. Make sure a selected disabled plan should remain in the filtered home.
     * 
     */
    public Context filterDisabledPricePlan(final Context originalContext_)
    {
        Context ctx = originalContext_;
        
        // changes done to make it similar as enabled price plan which is known as ACTIVE now.
        Predicate filter = new EQ(PricePlanXInfo.STATE, PricePlanStateEnum.ACTIVE);

        ctx = ctx.createSubContext();

        final Home originalHome = (Home) ctx.get(PricePlanHome.class);
        final Home newHome = new HomeProxy(ctx, originalHome).where(ctx, filter);
        ctx.put(PricePlanHome.class, newHome);

        return ctx;
    }


    /**
     * 
     */
    public Context filterPriceplanSubscriberType(final Context originalContext)
    {
        final Context newContext = originalContext.createSubContext();
        final Predicate predicate = new EQ(PricePlanXInfo.PRICE_PLAN_TYPE, subscriberType_);
        final Home originalHome = (Home) newContext.get(PricePlanHome.class);
        final Home newHome = new HomeProxy(newContext, originalHome).where(newContext, predicate);
        newContext.put(PricePlanHome.class, newHome);
        return newContext;
    }

    private final SubscriberTypeEnum subscriberType_;
}
