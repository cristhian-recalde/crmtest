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

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.beans.xi.PropertyInfoAware;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.NullWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.TableWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This web control outputs the default value for a property returned by
 * the containing bean.
 *
 * @author aaron.gourley@redknee.com
 * @since 8.6
 */
public class PropertyDefaultValueWebControl extends ProxyWebControl
{
    public PropertyDefaultValueWebControl()
    {
        super(NullOutputtingWebControl.instance());
    }
    
    public PropertyDefaultValueWebControl(PropertyInfo defaultProperty)
    {
        super(NullOutputtingWebControl.instance());
        defaultProperty_ = defaultProperty;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WebControl getDelegate(Context ctx)
    {
        WebControl delegate = null;

        PropertyInfo prop = getPropertyInfo(ctx);
        
        Object defaultValue = null;
        if (prop != null)
        {
            defaultValue = prop.getDefault();
        }
        
        if (prop != null
                && defaultValue != null)
        {
            delegate = (WebControl) prop.getInstanceOf(ctx, WebControl.class);
            if (delegate == null)
            {
                delegate = (WebControl) XBeans.getInstanceOf(ctx, prop.getType(), WebControl.class);
            }
        }
        
        if (delegate == null
                || delegate instanceof PropertyDefaultValueWebControl
                || (delegate instanceof TableWebControl
                        && !(defaultValue instanceof Collection)))
        {
            delegate = super.getDelegate();
        }
        
        return delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Object defaultValue = null;

        Context sCtx = ctx.createSubContext();
        
        PropertyInfo prop = getPropertyInfo(ctx);
        if (prop != null)
        {
            defaultValue = prop.getDefault();
            Object dummyParentBean = null;
            try
            {
                // Put a dummy parent bean in the context because some field-level web controls are
                // written specifically for a given field for a given bean.
                Class beanClass = prop.getXInfo().getBeanClass();
                dummyParentBean = XBeans.instantiate(beanClass, sCtx);
            }
            catch (Exception e)
            {
                // At least put null so that there won't be ClassCastExceptions
                // NullPointerExceptions may still be thrown by the property's web control
                dummyParentBean = null;
            }
            sCtx.put(AbstractWebControl.BEAN, dummyParentBean);
        }

        try
        {
            getDelegate(ctx).toWeb(sCtx, out, name, defaultValue);   
        }
        catch (Throwable t)
        {
            if (LogSupport.isDebugEnabled(sCtx))
            {
                new DebugLogMsg(this, "Error rendering web control for default " + (prop != null ? prop.getName() + " " : "") + "value=[" + defaultValue + "]: " + t.getMessage(), t).log(sCtx);
            }
            NullWebControl.instance().toWeb(sCtx, out, name, obj);
        }
    }
    
    protected PropertyInfo getPropertyInfo(Context ctx)
    {
        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean instanceof PropertyInfoAware)
        {
            PropertyInfo prop = ((PropertyInfoAware) bean).getPropertyInfo();
            return prop;
        }
        return defaultProperty_;
    }

    private PropertyInfo defaultProperty_ = null;
    
    private static final class NullOutputtingWebControl extends ProxyWebControl
    {
        private static NullOutputtingWebControl instance_ = new NullOutputtingWebControl();
        private static NullOutputtingWebControl instance()
        {
            return instance_;
        }
        
        private NullOutputtingWebControl()
        {
            super(NullWebControl.instance());
        }

        @Override
        public void toWeb(Context wcCtx, PrintWriter out, String name, Object obj)
        {
            if (obj == null)
            {
                out.print("#NULL#");
            }
            super.toWeb(wcCtx, out, name, obj);
        }
    }
}
