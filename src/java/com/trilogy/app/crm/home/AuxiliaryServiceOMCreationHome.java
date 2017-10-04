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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.OMLogMsg;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.AuxiliaryService;


// NOTE - 2004-08-16 - This class should be generic so it can be reused with
// other Homes.


/**
 * Generates operational measurements (OMs) for AuxiliaryServicesHome
 * operations.
 *
 * @author gary.anderson@redknee.com
 */
public
class AuxiliaryServiceOMCreationHome
    extends HomeProxy
{
    /**
     * Creates a new AuxiliaryServiceOMCreationHome proxy.
     *
     * @param delegate The Home to which we delegate.
     */
    public AuxiliaryServiceOMCreationHome(final Home delegate)
    {
        super(delegate);
    }

    // INHERIT
    public Object create(Context ctx,final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_CREATION_ATTEMPT).log(ctx);

        final Object result;
        try
        {
            result = super.create(ctx,obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_CREATION_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_CREATION_FAIL).log(ctx);
            throw exception;
        }

        return result;
    }


    // INHERIT
    public Object store(Context ctx,final Object obj) throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_MODIFICATION_ATTEMPT).log(ctx);

        Object result=null;
        try
        {
            result=super.store(ctx,obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_MODIFICATION_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_MODIFICATION_FAIL).log(ctx);
            throw exception;
        }
        
        return result;
    }


    // INHERIT
    public void remove(Context ctx, final Object obj)
        throws HomeException
    {
        new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_DELETION_ATTEMPT).log(ctx);

        try
        {
            super.remove(ctx,obj);
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_DELETION_SUCCESS).log(ctx);
        }
        catch (final HomeException exception)
        {
            new OMLogMsg(Common.OM_MODULE, Common.OM_AUXILIARY_SERVICE_DELETION_FAIL).log(ctx);

            final AuxiliaryService service = (AuxiliaryService)obj;
            new EntryLogMsg(10996, this, "", "", new String[] { Long.toString(service.getIdentifier()) }, exception).log(ctx);
            
            throw exception;
        }
    }
} // class
