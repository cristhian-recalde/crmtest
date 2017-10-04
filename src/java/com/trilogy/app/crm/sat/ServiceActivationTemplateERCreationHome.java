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
package com.trilogy.app.crm.sat;

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;
import com.trilogy.app.crm.log.ServiceActivationTemplateCreationEventRecord;
import com.trilogy.app.crm.log.ServiceActivationTemplateDeletionEventRecord;
import com.trilogy.app.crm.log.ServiceActivationTemplateModificationEventRecord;

/**
 * Creates ERs as necessary for the create(), update(), remove() methods in ServiceActivationTemplateHome chain.
 *
 * @author Lily Zou
 * @date   Dec 15, 2004
 */
public
class ServiceActivationTemplateERCreationHome
    extends HomeProxy
{
    /**
     * Creates a new ServiceActivationTemplateERCreationHome decorator.
     *
     * @param context
     * @param delegate The Home to which we delegate.
     */
    public ServiceActivationTemplateERCreationHome(
        final Context context,
        final Home delegate)
    {
        super(context, delegate);
    }

    // INHERIT
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        try
        {
            final Object result = super.create(ctx,obj);

            return result;
        }
        finally
        {
           new ServiceActivationTemplateCreationEventRecord((ServiceActivationTemplate)obj).generate(ctx);
        }
    }

    // INHERIT
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        try
        {
            Object ret=super.store(ctx,obj);
            
            return ret;
        }
        finally
        {
            ServiceActivationTemplate old_template = null;
            
            try
            {
                old_template = (ServiceActivationTemplate)find(ctx,obj);
            }
            finally
            {
                new ServiceActivationTemplateModificationEventRecord(old_template, (ServiceActivationTemplate)obj).generate(ctx);
            }
        }
    }

    // INHERIT
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        try
        {
            super.remove(ctx,obj);
        }
        finally
        {
            new ServiceActivationTemplateDeletionEventRecord((ServiceActivationTemplate)obj).generate(ctx);
        }
    }
} // class
