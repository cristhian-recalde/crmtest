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

import com.trilogy.app.crm.bean.*;


/**
 * This class provides functionality for clearing the Unprovision Commands
 * (for postpaid subscribers) in the OICK Mapping table.
 *
 * @author jimmy.ng@redknee.com
 */
public class OICKMappingUnprovisionCommandClearingHome
    extends HomeProxy
    implements ContextAware
{
    /**
     * Creates a new OICKMappingUnprovisionCommandClearingHome for the given delegate.
     *
     * @param context The operating context.
     * @param delegate The delegate to which we pass searches.
     */
    public OICKMappingUnprovisionCommandClearingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }


    /**
     * INHERIT
     */
    public Object create(Context ctx, Object obj)
        throws HomeException
    {
        final OICKMapping mapping = (OICKMapping) obj;
        
        if (mapping.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
            mapping.setUnprovisionOICK("");
        }
        
        return super.create(ctx,mapping);
    }


    // INHERIT
    public Object store(Context ctx,final Object obj)
        throws HomeException
    {
        final OICKMapping mapping = (OICKMapping) obj;
        
        if (mapping.getSubscriberType() == SubscriberTypeEnum.POSTPAID)
        {
            mapping.setUnprovisionOICK("");
        }
        
        return super.store(ctx,mapping);
    }

} // class
