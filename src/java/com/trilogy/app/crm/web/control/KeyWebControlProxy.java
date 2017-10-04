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
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.JavascriptFactory;
import com.trilogy.framework.xhome.webcontrol.WebControl;


/**
 * This proxy is slightly different than a regular FW proxy.  It is a pure proxy for
 * all key web control methods, as well as a proxy for getDelegate() and setDelegate()
 * methods.  Special get/setKeyWebControlDelegate() methods must be used to change the
 * key web control that this proxy is wrapping.  The regular methods will retrieve or 
 * change the key web control delegate's delegate.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class KeyWebControlProxy extends AbstractKeyWebControl
{
    public AbstractKeyWebControl keyWC_;

    public KeyWebControlProxy(AbstractKeyWebControl keyWC)
    {
        setKeyWebControlDelegate(keyWC);

        autoPreview_   = keyWC.isAutoPreview();
        isOptional_    = keyWC.getIsOptional();
        optionalValue_ = keyWC.getOptionalValue();
        allowCustom_   = keyWC.isAllowCustom();
    }

    public void setKeyWebControlDelegate(AbstractKeyWebControl keyWC)
    {
        keyWC_ = keyWC;
    }

    public AbstractKeyWebControl getKeyWebControlDelegate()
    {
        return keyWC_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl setDelegate(WebControl delegate)
    {
       return getKeyWebControlDelegate().setDelegate(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getDelegate()
    {
        return getKeyWebControlDelegate().getDelegate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getDelegate(Context ctx)
    {
        return getKeyWebControlDelegate().getDelegate(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        // Call super.toWeb() instead of getKeyWebControlDelegate().toWeb() on purpose.  This will ensure that all of the other KeyWebControl
        // Methods (e.g. getDesc()) are called on 'this' instead of the delegate (in which case none of the methods overridden
        // in 'this' would ever be called).  Note that this approach means that any customized toWeb() of the key web control delegate
        // will never be called.  If this behaviour is desired somehow, then extend this class and override toWeb() to define your own
        // behaviour.
        super.toWeb(ctx, out, name, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fromWeb(Context ctx, Object obj, ServletRequest req, String name)
    {
        getKeyWebControlDelegate().fromWeb(ctx, obj, req, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
        return getKeyWebControlDelegate().fromWeb(ctx, req, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDesc(Context ctx, Object bean)
    {
        return getKeyWebControlDelegate().getDesc(ctx, bean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentitySupport getIdentitySupport()
    {
        return getKeyWebControlDelegate().getIdentitySupport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getHomeKey()
    {
        return getKeyWebControlDelegate().getHomeKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void outputError(Context ctx, PrintWriter out, String msg)
    {
        getKeyWebControlDelegate().outputError(ctx, out, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Home getHome(Context ctx)
    {
        return getKeyWebControlDelegate().getHome(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setAllowCustom(boolean value)
    {
        getKeyWebControlDelegate().setAllowCustom(value);
        return super.setAllowCustom(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAllowCustom()
    {
        return getKeyWebControlDelegate().isAllowCustom();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setIsOptional(boolean value)
    {
        getKeyWebControlDelegate().setIsOptional(value);
        return super.setIsOptional(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setJavascriptForOnChange(JavascriptFactory factory)
    {
        getKeyWebControlDelegate().setJavascriptForOnChange(factory);
        return super.setJavascriptForOnChange(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsOptional()
    {
        return getKeyWebControlDelegate().getIsOptional();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setOptionalValue(Object value)
    {
        getKeyWebControlDelegate().setOptionalValue(value);
        return super.setOptionalValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getOptionalValue()
    {
        return getKeyWebControlDelegate().getOptionalValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setSelectWhenSize1(boolean value)
    {
        getKeyWebControlDelegate().setSelectWhenSize1(value);
        return super.setSelectWhenSize1(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSelectWhenSize1()
    {
        return getKeyWebControlDelegate().getSelectWhenSize1();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectWhenSize1()
    {
        return getKeyWebControlDelegate().isSelectWhenSize1();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNoSelectionMsg()
    {
        return getKeyWebControlDelegate().getNoSelectionMsg();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setNoSelectionMsg(String noSelectionMsg)
    {
        getKeyWebControlDelegate().setNoSelectionMsg(noSelectionMsg);
        return super.setNoSelectionMsg(noSelectionMsg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAutoPreview()
    {
        return getKeyWebControlDelegate().getAutoPreview();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setAutoPreview(boolean autoPreview)
    {
        getKeyWebControlDelegate().setAutoPreview(autoPreview);
        return super.setAutoPreview(autoPreview);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoPreview()
    {
        return getKeyWebControlDelegate().isAutoPreview();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableAutoPreview()
    {
        getKeyWebControlDelegate().enableAutoPreview();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableAutoPreview()
    {
        getKeyWebControlDelegate().disableAutoPreview();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractKeyWebControl setSelectFilter(Predicate value)
    {
        getKeyWebControlDelegate().setSelectFilter(value);
        return super.setSelectFilter(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate getSelectFilter(Context ctx)
    {
        return getKeyWebControlDelegate().getSelectFilter(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate getSelectFilter()
    {
        return getKeyWebControlDelegate().getSelectFilter();
    }

}
