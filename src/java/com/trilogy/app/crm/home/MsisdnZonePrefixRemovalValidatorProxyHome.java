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
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;

import com.trilogy.app.crm.bean.BillingOptionMappingHome;
import com.trilogy.app.crm.bean.BillingOptionMappingXInfo;
import com.trilogy.app.crm.bean.MsisdnZonePrefix;


/**
 * Verifies if the MsisdnZonePrefix can be removed.
 *
 * @author victor.stratan@redknee.com
 */
public class MsisdnZonePrefixRemovalValidatorProxyHome
    extends HomeProxy
{
    /**
     * Creates a new MsisdnZonePrefixRemovalValidatorProxyHome.
     *
     * @param delegate The Home to which we delegate.
     */
    public MsisdnZonePrefixRemovalValidatorProxyHome(
		final Home delegate)
        throws HomeException
    {
        super(delegate);
    }

    //INHERIT
    @Override
    public void remove(Context ctx, Object obj) throws HomeException
    {
        final MsisdnZonePrefix msisdnZone = (MsisdnZonePrefix)obj;
        final Home home = (Home)ctx.get(BillingOptionMappingHome.class);
        final Object zone = home.find(ctx, new EQ(BillingOptionMappingXInfo.ZONE_IDENTIFIER, Long.valueOf(msisdnZone.getIdentifier())));

        // Throws HomeException.
        if (zone != null)
        {
            throw new HomeException("Cannot delete Destination Zone while it is IN USE.");
        }

        getDelegate(ctx).remove(ctx, obj);
    }

} // class
