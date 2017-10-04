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

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractKeyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Filters a home used by the WebControl delegate based on the bean property value and PropertyInfos
 * provided that guides this class to do the proper filtering.
 *
 * @author victor.stratan@redknee.com, aaron.gourley@redknee.com
 * @auditor ravi.patel@redknee.com
 */
public class FilterWebControl extends ProxyWebControl
{
    /**
     * This constructor is used if filtering is based on a referential value.  Lets take an example...
     * lets say the bean with this webcontrol is A and the A.a field is using this webcontrol.  Remember A.a is
     * using a key webcontrol and so A.a is actually the key to some B home record.  That being said A has 
     * a field A.b which is the key to the C Home.  And in the C record, with it's key being A.b, there is a 
     * value C.c which we want to use for filtering.  In this case we will want to filter the B home for records such
     * that B.b equals C.c.  So this would translate to:
     * <br>
     * <br>beanField would be A.b
     * <br>fieldObject would be C.c
     * <br>filteredField would be B.b
     * <br>
     * <br>Now isn't that simple! :) 
     * 
     * @param delegate
     * @param beanField 
     * @param fieldObject
     * @param filteredField
     */
    public FilterWebControl(final WebControl delegate,
            final PropertyInfo beanField, final PropertyInfo fieldObject, final PropertyInfo filteredField, final Object homeKey)
    {
        setDelegate(delegate);
        this.beanField_ = beanField;
        this.objectField_ = fieldObject;
        this.filteredField_ = filteredField;
        this.homeKey_ = homeKey;
        this.filteredValue_ = null;
    }
    
    /**
     *  @see com.redknee.app.crm.web.control.FilterWebControl.FilterWebControl(WebControl, PropertyInfo, PropertyInfo, PropertyInfo, Object)
     */
    public FilterWebControl(final WebControl delegate,
            final PropertyInfo beanField, final PropertyInfo fieldObject, final PropertyInfo filteredField)
    {
    	this(delegate, beanField, fieldObject, filteredField, null);
    }
    
    /**
     * Use this constructor when a referential value is not needed.  Lets take an example again...
     * lets say the bean with this webcontrol is A and the A.a field is using this webcontrol.  A.a is the key to the B home.
     * We want to filter the B home such that only records where B.b = A.b.  So this would translate to:
     * <br>
     * <br>beanfield would be A.b
     * <br>filtered field would be B.b
     * 
     * @param delegate
     * @param beanField
     * @param filteredField
     */
    public FilterWebControl(final WebControl delegate,
            final PropertyInfo beanField, final PropertyInfo filteredField)
    {
        this(delegate, beanField, null, filteredField, null);
    }
    
    /**
     * @see com.redknee.app.crm.web.control.FilterWebControl.FilterWebControl(WebControl, PropertyInfo, PropertyInfo)
     */
    public FilterWebControl(final WebControl delegate,
            final PropertyInfo beanField, final PropertyInfo filteredField, Object homeKey)
    {
    	this(delegate, beanField, null, filteredField, homeKey);
    }
    
    /**
     * Use this constructor when applying a static filter.  Lets take an example again...
     * We want to filter the B home such that only records where B.b = { b1 or b2 or ... or bn }
     * 
     * @param delegate
     * @param filteredField PropertyInfo for B.b
     * @param filteredValue Array of possible values that are acceptable for B.b
     */
    public FilterWebControl(final PropertyInfo filteredField, final WebControl delegate, final Object... filteredValue)
    {
        setDelegate(delegate);
        this.beanField_ = null;
        this.objectField_ = null;
        this.filteredValue_ = filteredValue;
        this.filteredField_ = filteredField;
        this.homeKey_ = null;
    }
    
    /**
     * Alters the Home stored in the context
     *
     * @param ctx the operating context
     * @return the altered subcontext
     */
    @Override
    public Context wrapContext(final Context ctx)
    {
        final Context subCtx = ctx.createSubContext();
        subCtx.setName(this.getClass().getSimpleName());
        final Object homeKey = getHomeKey(ctx);
        final Home home = (Home) subCtx.get(homeKey);
        final Object bean = ctx.get(AbstractWebControl.BEAN);
        try
        {
            Object filter = null;
            if (filteredValue_ != null)
            {
                filter = new Or();
                for (Object value : filteredValue_)
                {
                    ((Or)filter).add(new EQ(filteredField_, value));
                }
            }
            else
            {
                Object filteringValue = getFilteringValue(ctx, subCtx, bean);
                filter = new EQ(filteredField_, filteringValue);
            }
            
            final Home alteredHome = home.where(ctx, filter);
            subCtx.put(homeKey, alteredHome);
        }
        catch (HomeException e)
        {
            LogSupport.minor(ctx, this, "Unable to filter beans", e);
        }

        return super.wrapContext(subCtx);
    }

    private Object getFilteringValue(final Context ctx, final Context subCtx, final Object bean) throws HomeException
    {
        final Object value;
        if (beanField_ != null)
        {
            value = beanField_.get(bean);
        }
        else
        {
            value = null;
        }
        
        if(objectField_ == null)
        {
            // if the objectField is not specified then we simply want to use the bean field's value to filter the home.
            return value;
        }
        else
        {
            // if the objectField is provided then we need to retrieve the bean in the objectField's home with the key being beanField and
            // return the value from the objectField of that bean.
            final Object valueObject;
            if (value != null)
            {
                final Object valueHomeKey = XBeans.getClass(ctx, objectField_.getXInfo().getBeanClass(), Home.class);
                final Home valueHome = (Home) subCtx.get(valueHomeKey);
                valueObject = valueHome.find(ctx, value);
            }
            else
            {
                valueObject = null;
            }

            final Object filteringValue;
            if (valueObject == null)
            {
                filteringValue = objectField_.getDefault();
            }
            else
            {
                filteringValue = objectField_.get(valueObject);
            }
            return filteringValue;
        }
    }

    @Override
    public void toWeb(final Context ctx, final java.io.PrintWriter out, final String name, final Object bean)
    {
        delegate_.toWeb(wrapContext(ctx), out, name, bean);
    }
    
    private Object getHomeKey(Context ctx)
    {
    	if (homeKey_ != null)
		{
    		return homeKey_;
		}
    	else if (getDelegate() instanceof AbstractKeyWebControl)
        {
            return ((AbstractKeyWebControl)getDelegate()).getHomeKey();
        }
    	else
    	{
    		return XBeans.getClass(ctx, filteredField_.getBeanClass(), Home.class);
    	}
    }

    final PropertyInfo beanField_;
    final PropertyInfo objectField_;
    final PropertyInfo filteredField_;
    final Object homeKey_;
    final Object[] filteredValue_;
}
