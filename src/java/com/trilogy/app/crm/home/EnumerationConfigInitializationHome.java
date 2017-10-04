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
package com.trilogy.app.crm.home;

import java.util.Iterator;

import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.*;


/**
 * Initialize the Home created for an Enumeration (which is required to be made
 * configurable).
 *
 * @author jimmy.ng@redknee.com
 */
public class EnumerationConfigInitializationHome
    extends HomeProxy
{
    /**
     * Creates a new EnumerationConfigInitializationHome for the given delegate.
     *
     * @param context The operating context.
     * @param beanClass The bean class to be used to initialize the
     * Enumeration Home.
     * @param enumCollection The enumCollection to be used to initialize
     * the Enumeration Home.
     * @param delegate The Home to which we delegate.
     */
    public EnumerationConfigInitializationHome(
        final Context ctx,
        final Class beanClass,
        final EnumCollection enumCollection,
        final Home delegate)
    {
        super(delegate);
        setContext(ctx);
        
        try
        {
            init(ctx,beanClass, enumCollection);
        }
        catch (Exception e)
        {
            new MajorLogMsg(
                this,
                "Failed to initialize home for \"" + beanClass.getName() + "\".",
                e).log(ctx);
        }
    }

    /**
     * Initialize the Enumeration Home with the given bean class (that is dervied
     * from EnumerationConfig) and an EnumCollection.
     *
     * @param beanClass The given bean class.
     * @param enumCollection The given EnumCollection.
     */
    private void init(final Context ctx,final Class beanClass, final EnumCollection enumCollection)
        throws Exception
    {
        final Iterator i = enumCollection.iterator();
        while (i.hasNext())
        {
            final Enum enumeration = (Enum) i.next();
            
            final boolean isEnumConfigFound = (find(ctx, Short.valueOf(enumeration.getIndex())) != null);
            
            if (!isEnumConfigFound)
            {
                final EnumerationConfig enumConfig =
                    (EnumerationConfig) XBeans.instantiate(beanClass.getName(), ctx);
                enumConfig.setIndex(enumeration.getIndex());
                enumConfig.setName(enumeration.getDescription());
                enumConfig.setLabel(enumConfig.getName());
                
                create(ctx,enumConfig);
            }
        }
    }
} // class
