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
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;

import com.trilogy.app.crm.technology.Technology;
import com.trilogy.app.crm.technology.TechnologyAware;
import com.trilogy.app.crm.technology.TechnologyEnum;

/**
 * Filters the home by the technology of the enclosing bean. It is similar to
 * {@link com.redknee.app.crm.technology.SetTechnologyProxyWebControl}.
 *
 * @author cindy.wong@redknee.com
 */
public class TechnologyAwareFilterHomeWebControl extends ProxyWebControl
{

    /**
     * Creates a new <code>TechnologyAwareFilterHomeWebControl</code>.
     *
     * @param delegate
     *            The web control delegate.
     */
    public TechnologyAwareFilterHomeWebControl(final WebControl delegate)
    {
        super(delegate);
    }

    /**
     * If the current bean is technology-aware, put the technology into the context such that the technology-aware
     * property displayed by this web control gets filtered by technology.
     *
     * @param context
     *            The operating context.
     * @return A context with the technology of the current bean saved, if applicable.
     * @see com.redknee.framework.xhome.webcontrol.ProxyWebControl#wrapContext
     */
    @Override
    public final Context wrapContext(final Context context)
    {
        final Object obj = context.get(AbstractWebControl.BEAN);
        if (obj != null && obj instanceof TechnologyAware)
        {
            final TechnologyEnum techEnum = ((TechnologyAware) obj).getTechnology();
            if (techEnum != null)
            {
                Technology.setBeanTechnology(context, techEnum);
            }
        }
        return context;
    }
}
