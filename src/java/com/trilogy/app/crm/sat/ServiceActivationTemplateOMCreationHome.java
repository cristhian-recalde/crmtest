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


/**
 * Creates OMs as necessary for the create(), update(), remove() methodsin ServiceActivationTemplateHome chain.
 *
 * @author Lily Zou
 * @date   Dec 10, 2004
 */
public
class ServiceActivationTemplateOMCreationHome
    extends HomeProxy
{
    /**
     * Creates a new SubscriberAuxiliaryServiceOMCreationHome decorator.
     *
     * @param context
     * @param delegate The Home to which we delegate.
     */
    public ServiceActivationTemplateOMCreationHome(
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
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_CREATION_ATTEMPT).log(ctx);

            final Object result = super.create(ctx,obj);

             new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_CREATION_SUCCESS).log(ctx);

             return result;
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_CREATION_FAIL).log(ctx);

            throw exception;
        }
    }

    // INHERIT
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        try
        {
            new OMLogMsg(Common.OM_MODULE,Common.OM_SAT_MODIFICATION_ATTEMPT).log(ctx);

            Object ret=super.store(ctx,obj);

             new OMLogMsg(Common.OM_MODULE,Common.OM_SAT_MODIFICATION_SUCCESS).log(ctx);
             
             return ret;
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_MODIFICATION_FAIL).log(ctx);

            throw exception;
        }
    }


    // INHERIT
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        try
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_DELETION_ATTEMPT).log(ctx);

            super.remove(ctx,obj);

             new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_DELETION_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SAT_DELETION_FAIL).log(ctx);

            throw exception;
        }
    }

} // class
