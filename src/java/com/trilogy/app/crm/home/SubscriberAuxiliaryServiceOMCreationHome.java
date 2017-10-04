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

import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;


/**
 * Creates OMs as necessary for the create() and remove() methods.
 *
 * @author gary.anderson@redknee.com
 */
public
class SubscriberAuxiliaryServiceOMCreationHome
    extends HomeProxy
{
    /**
     * Creates a new SubscriberAuxiliaryServiceOMCreationHome decorator.
     *
     * @param ctx
     * @param delegate The Home to which we delegate.
     */
    public SubscriberAuxiliaryServiceOMCreationHome(
        final Context ctx,
        final Home delegate)
    {
        super(ctx, delegate);
    }


    // INHERIT
    public Object create(Context ctx,final Object obj)
        throws HomeException
    {
        try
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_ATTEMPT).log(ctx);

            final Object result = super.create(ctx,obj);

             new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_SUCCESS).log(ctx);

             return result;
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_FAIL).log(ctx);

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
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_ATTEMPT).log(ctx);

            super.remove(ctx,obj);

             new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(
                Common.OM_MODULE,
                Common.OM_SUBSCRIBER_AUXILIARY_SERVICE_CHANGE_FAIL).log(ctx);

            throw exception;
        }
    }

} // class
