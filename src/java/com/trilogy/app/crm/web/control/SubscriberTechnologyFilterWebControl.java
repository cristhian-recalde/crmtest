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
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.technology.Technology;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Filters the home by the technology of the subscriber in the enclosing bean.
 *
 * @since 14th October 2013
 * @author nitin.agrawal@redknee.com
 */
public class SubscriberTechnologyFilterWebControl extends ProxyWebControl
{

    /**
     * Creates a new <code>SubscriberTechnologyFilterWebControl</code>.
     *
     * @param delegate
     *            The web control delegate.
     */
    public SubscriberTechnologyFilterWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * If the current bean is subscriber-technology-aware, put the technology into the context such that the subscriber-technology-aware
     * property displayed by this web control gets filtered by technology of subscriber.
     *
     * @param context
     *            The operating context.
     * @return A context with the technology of the current bean saved, if applicable.
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#wrapContext
     */
    @Override
    public final Context wrapContext(final Context context)
    {
        if (context != null)
        {
        	Subscriber subscriber = (Subscriber)context.get(com.redknee.app.crm.bean.Subscriber.class);
        	if(subscriber != null && subscriber.getTechnology() != null){
        		final TechnologyEnum techEnum = subscriber.getTechnology();
                    Technology.setBeanTechnology(context, techEnum);
        	}
        }
        return context;
    }
}