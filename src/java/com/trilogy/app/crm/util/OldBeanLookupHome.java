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
package com.trilogy.app.crm.util;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Provides an abstract HomeProxy for automatically looking-up the old version
 * of a bean, and for placing that old bean in the operating context for
 * down-stream Homes.  This reduces the overhead associated with each individual
 * Home performing the same look-up in order to examine differences between the
 * old version of the bean and the new version.  For example usage, see {@link
 * com.redknee.app.crm.home.OldTDMAPackageLookupHome}.
 *
 * @author gary.anderson@redknee.com
 */
public abstract
class OldBeanLookupHome
    extends HomeProxy
{
    /**
     * Creates a new OldBeanLookupHome.
     *
     * @param context The operating context.
     * @param delegate The Home to which this proxy delegates.
     * @param beanClass The class of the bean handled by this Home.
     *
     * @exception IllegalArgumentException Thrown if any of the parameters are
     * null, and if the given bean class does not have a corresponding
     * IdentitySupport instance.
     */
    protected OldBeanLookupHome(final Context context, final Home delegate, final Class beanClass)
    {
        super(delegate);

        if (context == null)
        {
            throw new IllegalArgumentException("The context is mandatory.");
        }

        if (delegate == null)
        {
            throw new IllegalArgumentException("The delegate is mandatory.");
        }

        if (beanClass == null)
        {
            throw new IllegalArgumentException("The bean class is mandatory.");
        }

        identitySupport_ =
            (IdentitySupport)XBeans.getInstanceOf(
                context,
                beanClass,
                IdentitySupport.class);

        if (identitySupport_ == null)
        {
            throw new IllegalArgumentException("Could not find Identity support for " + beanClass);
        }
    }


    /**
     * Gets the key used to reference the old bean in the context.
     *
     * @return The key used to reference the old bean in the context.
     */
    protected abstract Object getKey();


    /**
     * {@inheritDoc}
     */
    public Object store(Context context, final Object newBean)
        throws HomeException
    {
        if (LogSupport.isDebugEnabled(context))
        {
            final Object oldBean = context.get(getKey());
            if (oldBean != null)
            {
                new DebugLogMsg(this, "Context masking for " + getKey(), null).log(context);
            }
        }

        context = context.createSubContext();
        context.setName("OldBeanLookupHome");

        final Object oldBean = lookupOldBean(context, newBean);
        context.put(getKey(), oldBean);

        return super.store(context, newBean);
    }


    /**
     * Looks-up the old version of the given bean in this Home.
     *
     * @param context The operating context.
     * @param newBean The new version of the bean.
     * @return The old version of the bean.
     *
     * @exception HomeException Thrown if there are problems looking-up the old
     * bean in this Home.
     */
    private Object lookupOldBean(final Context context, final Object newBean)
        throws HomeException
    {
        return find(context, identitySupport_.ID(newBean));
    }


    /**
     * The identity support used to derive the bean's key from an instance of
     * the bean.
     */
    private final IdentitySupport identitySupport_;

} // class
